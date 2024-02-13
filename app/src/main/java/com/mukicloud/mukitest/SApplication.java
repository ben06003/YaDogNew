package com.mukicloud.mukitest;
import com.appsflyer.AppsFlyerLib;

import android.app.Application;

public class SApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppsFlyerLib.getInstance().init("PjV6pjhqzpQPUpAEU9q76S", null, this);
        AppsFlyerLib.getInstance().start(this);
    }
}
