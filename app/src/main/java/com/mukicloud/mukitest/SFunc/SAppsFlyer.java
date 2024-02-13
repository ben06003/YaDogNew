package com.mukicloud.mukitest.SFunc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.appsflyer.AppsFlyerLib;

import org.json.JSONObject;
public class SAppsFlyer {
    private final AppsFlyerLib mAppsFlyer;

    public SAppsFlyer(Activity act) {
        mAppsFlyer = AppsFlyerLib.getInstance();
    }
}
