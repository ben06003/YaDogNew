package com.mukicloud.mukitest.SFunc;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

public class SFirebaseAnalytics {
    private final FirebaseAnalytics mFirebaseAnalytics;

    public SFirebaseAnalytics(Activity act) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(act);
    }

    public void logEvent(String eventName, String screenName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName);
        mFirebaseAnalytics.logEvent(eventName, bundle);
    }

    //JSON Getter
    public String JSONStrGetter(JSONObject JOB, String Key) {
        return JSONStrGetter(JOB, Key, "");
    }
    public String JSONStrGetter(JSONObject JOB, String Key, String Init) {
        String Value = Init;
        try {
            if (JOB == null) {
                return "";
            }
            Value = JOB.getString(Key);
        } catch (Exception e) {
            //DebugToast("JSONStrGetter\n"+e.getMessage());
        }
        return Value.trim();
    }
    public void logEvent(String eventName, JSONObject JOB) {
        Bundle bundle = new Bundle();
        switch (eventName) {
            case "registerSubmit":
                bundle.putString("method", JSONStrGetter(JOB, "method"));
                break;
            case "register":
                bundle.putString("method", JSONStrGetter(JOB, "method"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerId"));
                bundle.putString("customerName", JSONStrGetter(JOB, "customerName"));
                bundle.putString("mobileNum", JSONStrGetter(JOB, "mobileNum"));
                break;
            case "depositSubmit":
                bundle.putString("customerName", JSONStrGetter(JOB, "customerId"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerName"));
                bundle.putString("revenue", JSONStrGetter(JOB, "revenue"));
                bundle.putString("value", JSONStrGetter(JOB, "value"));
                bundle.putString("af_revenue", JSONStrGetter(JOB, "af_revenue"));
                break;
            case "firstDeposit":
                bundle.putString("customerName", JSONStrGetter(JOB, "customerName"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerId"));
                bundle.putString("revenue", JSONStrGetter(JOB, "revenue"));
                bundle.putString("value", JSONStrGetter(JOB, "value"));
                bundle.putString("af_revenue", JSONStrGetter(JOB, "af_revenue"));
                break;
            case "withdraw":
                bundle.putString("customerName", JSONStrGetter(JOB, "customerName"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerId"));
                bundle.putString("amount", JSONStrGetter(JOB, "amount"));
                bundle.putString("value", JSONStrGetter(JOB, "value"));
                bundle.putString("af_revenue", JSONStrGetter(JOB, "af_revenue"));
                break;
            case "firstDepositArrival":
                bundle.putString("customerName", JSONStrGetter(JOB, "customerName"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerId"));
                bundle.putString("revenue", JSONStrGetter(JOB, "revenue"));
                bundle.putString("value", JSONStrGetter(JOB, "value"));
                bundle.putString("af_revenue", JSONStrGetter(JOB, "af_revenue"));
                break;
            case "deposit":
                bundle.putString("customerName", JSONStrGetter(JOB, "customerName"));
                bundle.putString("customerId", JSONStrGetter(JOB, "customerId"));
                bundle.putString("revenue", JSONStrGetter(JOB, "revenue"));
                bundle.putString("value", JSONStrGetter(JOB, "value"));
                bundle.putString("af_revenue", JSONStrGetter(JOB, "af_revenue"));
                break;
            default:
                break;

        }
        mFirebaseAnalytics.logEvent(eventName, bundle);
    }

    public void setUserId(String userId) {
        mFirebaseAnalytics.setUserId(userId);
    }

    public void setUserProperty(String value) {
        mFirebaseAnalytics.setUserProperty("user_properties", value);
    }
}
