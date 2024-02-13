package com.mukicloud.mukitest.Activity;

import static com.mukicloud.mukitest.TD.PKG_MKTest;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.mukicloud.mukitest.SFunc.SMethods;
import com.mukicloud.mukitest.TD;

import java.util.ArrayList;

public class ActivityAutoGo extends AppCompatActivity {
    private boolean isSecCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckPermissions();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getFlags() != Intent.FLAG_ACTIVITY_SINGLE_TOP) {// 非点击icon调用activity时才调用newintent事件
            setIntent(intent);
        }
    }

    private void CheckPermissions() {
        boolean NeedSecCheck = false;//部分應用需要二次檢查
        String AppID = getApplicationInfo().packageName;
        ArrayList<String> PermissionAL = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionAL.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        //Can hasPermissions
        boolean canCheckHasPermissions = !NeedSecCheck || isSecCheck;
        //Start Check
        String[] RequestPermissionAR = PermissionAL.toArray(new String[0]);
        if (canCheckHasPermissions && ActivityWeb.hasPermissions(this, RequestPermissionAR)) {//Go Activity
            CheckSystemFeature();
        } else {//Request permissions
            ActivityCompat.requestPermissions(ActivityAutoGo.this, RequestPermissionAR, TD.RQC_Permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TD.RQC_Permission) {
            if (ActivityWeb.CheckAllGrant(grantResults)) {//Success
                //部分權限必須先允許其他權限後才能請求
                if (!isSecCheck) {
                    isSecCheck = true;
                    String AppID = getApplicationInfo().packageName;
//                    if (AppID.contains(PKG_GaufengRFID)) {
//                        CheckPermissions();
//                        return;
//                    }
                }
            }
            //Go Activity
            CheckSystemFeature();
        }
    }

    private void CheckSystemFeature() {
        boolean available = true;
        if (!getPackageManager().hasSystemFeature("android.software.webview")) {
            available = false;
            Toast.makeText(ActivityAutoGo.this, "您的裝置不支援WebView", Toast.LENGTH_SHORT).show();
        }
        if (available) {
            SonCreate();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
        }

    }

    private void SonCreate() {
        String packageName = getApplicationInfo().packageName;
        Intent intent = getIntent();
        intent.setClass(this, ActivityWeb.class);
        startActivity(intent);
        finish();
    }
}
