package com.mukicloud.mukitest.SFunc

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.mukicloud.mukitest.JSInterface
import com.mukicloud.mukitest.SFunc.SMethods.dateToSec
import com.mukicloud.mukitest.SFunc.SMethods.millisToDate
import com.mukicloud.mukitest.TD
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class SHealth(var act: Activity) {

    fun hasReadPermission(type: String): Boolean {
        val fitnessOptions = FitnessOptions.builder()
        when (type) {
            "bodyMass" -> fitnessOptions.addDataType(
                DataType.TYPE_WEIGHT,
                FitnessOptions.ACCESS_WRITE
            )
            "basalEnergyBurned" -> fitnessOptions.addDataType(
                DataType.AGGREGATE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_WRITE
            )
            "activeEnergyBurned" -> fitnessOptions.addDataType(
                DataType.TYPE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_WRITE
            )
        }
        val account = GoogleSignIn.getAccountForExtension(act, fitnessOptions.build())
        return GoogleSignIn.hasPermissions(account, fitnessOptions.build())
    }

    fun hasWritePermission(type: String): Boolean {

        val fitnessOptions = FitnessOptions.builder()
        when (type) {
            "bodyMass" -> fitnessOptions.addDataType(
                DataType.TYPE_WEIGHT,
                FitnessOptions.ACCESS_WRITE
            )
            "basalEnergyBurned" -> fitnessOptions.addDataType(
                DataType.AGGREGATE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_WRITE
            )
            "activeEnergyBurned" -> fitnessOptions.addDataType(
                DataType.TYPE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_WRITE
            )
        }
        val account = GoogleSignIn.getAccountForExtension(act, fitnessOptions.build())
        return GoogleSignIn.hasPermissions(account, fitnessOptions.build())
    }

    fun requestPermission() {
        //Request permissions
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(
                DataType.AGGREGATE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_READ
            )
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
            .addDataType(
                DataType.AGGREGATE_CALORIES_EXPENDED,
                FitnessOptions.ACCESS_WRITE
            )
            .build()
        val account = GoogleSignIn.getAccountForExtension(act, fitnessOptions)
        GoogleSignIn.requestPermissions(
            act, // your activity
            TD.RQC_GGFit, // e.g. 1
            account,
            fitnessOptions
        )
    }

    fun read(callbackId: String, value: String, jst: JSInterface?, jss: JSInterface?) {
        try {
            val jobJS = JSONObject(value)
            val fitnessOptions = FitnessOptions.builder()
            //Check permission
            val type = jobJS["type"] as String
            val hasPermission: Boolean = hasReadPermission(type)
            if (!hasPermission) {
                val job = JSONObject()
                job.put("res_code", "0")
                job.put("res_data", "no permission")
                if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                else jss?.JSHandlerCallBack(callbackId, job.toString())
                return
            }
            //DataType
            var field: Field = Field.FIELD_WEIGHT
            var dataType: DataType = DataType.TYPE_WEIGHT
            when (type) {
                "bodyMass" -> {
                    dataType = DataType.TYPE_WEIGHT
                    field = Field.FIELD_WEIGHT
                }
                "basalEnergyBurned" -> {
                    dataType = DataType.AGGREGATE_CALORIES_EXPENDED
                    field = Field.FIELD_CALORIES
                }
                "activeEnergyBurned" -> {
                    dataType = DataType.TYPE_CALORIES_EXPENDED
                    field = Field.FIELD_CALORIES
                }
            }
            fitnessOptions.addDataType(dataType, FitnessOptions.ACCESS_READ)

            val account = GoogleSignIn.getAccountForExtension(act, fitnessOptions.build())
            //Range
            val startSec = dateToSec(jobJS["start"] as String?)
            val endSec = dateToSec(jobJS["end"] as String?)

            //DataReadRequest
            val readRequest = DataReadRequest.Builder()
                .read(dataType)
                .setTimeRange(startSec, endSec, TimeUnit.SECONDS)
                .setLimit(1)
                .build()

            Fitness.getHistoryClient(act, account)
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    run {
                        val sm = SMethods(act);
                        val model = sm.JSONStrGetter(jobJS, "model")
                        val dataJA = JSONArray()
                        val dataSets = response.dataSets
                        var dataTotal = 0F
                        var dateTotal = ""
                        for (dataSet in dataSets) {
                            for (dataPoints in dataSet.dataPoints) {
                                val data = dataPoints.getValue(field)
                                val date = millisToDate(dataPoints.getStartTime(TimeUnit.SECONDS))
                                if (model == "dateTotal") {
                                    dataTotal += data.asFloat()
                                    if (dateTotal.isEmpty()) dateTotal = date
                                } else {
                                    dataTotal = data.asFloat()
                                    dateTotal = date
                                    break
                                }
                            }
                            if (model == "single") break
                        }
                        //dataJOB
                        val dataJOB = JSONObject()
                        dataJOB.put("uuid", "")//辨識碼
                        dataJOB.put("data", dataTotal)//不包含單位值(體重:Kg,能量:千卡路里)
                        dataJOB.put("date", dateTotal)//時間戳("yyyyMMddHHmmss")事件開始時間點
                        dataJA.put(dataJOB)
                        //Return
                        val job = JSONObject()
                        job.put("res_code", "1")
                        job.put("res_data", dataJA.toString())
                        if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                        else jss?.JSHandlerCallBack(callbackId, job.toString())
                    }
                }
                .addOnFailureListener { e ->
                    run {
                        val job = JSONObject()
                        job.put("res_code", "0")
                        job.put(
                            "res_data", e.toString()
                        )
                        if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                        else jss?.JSHandlerCallBack(callbackId, job.toString())
                    }
                }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val job = JSONObject()
            job.put("res_code", "0")
            job.put("res_data", e.toString())
            if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
            else jss?.JSHandlerCallBack(callbackId, job.toString())
        }
    }

    fun save(callbackId: String, value: String, jst: JSInterface?, jss: JSInterface?) {
        try {
            val jobJS = JSONObject(value)
            val fitnessOptions = FitnessOptions.builder()
            //Check permission
            val type = jobJS["type"] as String
            val hasPermission: Boolean = hasReadPermission(type)
            if (!hasPermission) {
                val job = JSONObject()
                job.put("res_code", "0")
                job.put("res_data", "no permission")
                if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                else jss?.JSHandlerCallBack(callbackId, job.toString())
                return
            }
            //DataType
            var dataType: DataType = DataType.TYPE_WEIGHT
            when (type) {
                "bodyMass" -> dataType = DataType.TYPE_WEIGHT
                "basalEnergyBurned" -> dataType = DataType.AGGREGATE_CALORIES_EXPENDED
                "activeEnergyBurned" -> dataType = DataType.TYPE_CALORIES_EXPENDED
            }
            fitnessOptions.addDataType(dataType, FitnessOptions.ACCESS_READ)

            val account = GoogleSignIn.getAccountForExtension(act, fitnessOptions.build())
            //Range
            val startSec = dateToSec(jobJS["start"] as String?)
            val endSec = dateToSec(jobJS["end"] as String?)
            // Create a data source
            val dataSource: DataSource = DataSource.Builder()
                .setAppPackageName(act)
                .setDataType(dataType)
                .setStreamName(type)
                .setType(DataSource.TYPE_RAW)
                .build()

            // Create a data set
            val dataPoint =
                DataPoint.builder(dataSource)
                    .setTimeInterval(startSec, endSec, TimeUnit.SECONDS)
            //Check field
            when (type) {
                "bodyMass" -> dataPoint.setField(
                    Field.FIELD_WEIGHT,
                    (jobJS["value"] as String).toFloat()
                )
                "basalEnergyBurned" -> {
                    val valueNum = (jobJS["value"] as String).toFloat()
                    dataPoint.setField(Field.FIELD_CALORIES, valueNum)
                }
                "activeEnergyBurned" -> {
                    val valueNum = (jobJS["value"] as String).toFloat()
                    dataPoint.setField(Field.FIELD_CALORIES, valueNum)
                }
            }
            //DataSet
            val dataSet = DataSet.builder(dataSource)
                .add(dataPoint.build())
                .build()
            //Insert Data
            Fitness.getHistoryClient(act, account)
                .insertData(dataSet)
                .addOnSuccessListener {
                    val job = JSONObject()
                    job.put("res_code", "1")
                    if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                    else jss?.JSHandlerCallBack(callbackId, job.toString())
                }
                .addOnFailureListener { e ->
                    val job = JSONObject()
                    job.put("res_code", "0")
                    job.put("res_data", e.toString())
                    if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
                    else jss?.JSHandlerCallBack(callbackId, job.toString())
                }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val job = JSONObject()
            job.put("res_code", "0")
            job.put("res_data", e.toString())
            if (jst != null) jst.JSHandlerCallBack(callbackId, job.toString())
            else jss?.JSHandlerCallBack(callbackId, job.toString())
        }
    }
}