package com.mukicloud.mukitest.SFunc;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.linecorp.linesdk.LineProfile;
import com.linecorp.linesdk.Scope;
import com.linecorp.linesdk.api.LineApiClientBuilder;
import com.linecorp.linesdk.auth.LineAuthenticationParams;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;
import com.mukicloud.mukitest.JSInterface;
import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.TD;

import org.json.JSONObject;

import java.util.Collections;

public class LineLoginHandler {
    private static String CallBackID;

    public static void LoginViaLine(Activity Act, String CallBackIDin) {
        try {
            CallBackID = CallBackIDin;
            Intent loginIntent = LineLoginApi.getLoginIntent(
                    Act,
                    Act.getResources().getString(R.string.line_channel_id),
                    new LineAuthenticationParams.Builder()
                            .scopes(Collections.singletonList(Scope.PROFILE))
                            .build());
            Act.startActivityForResult(loginIntent, TD.RQC_LineLogin);
        } catch (Exception e) {
            new SMethods(Act).EXToast(R.string.CM_DetectError, "loginViaLINE", e);
        }
    }

    public static void LineOnActivityResult(JSInterface JST, JSInterface JSS, Intent data) {
        try {
            LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);
            switch (result.getResponseCode()) {
                case SUCCESS:
                    // Login successful
                    if (CallBackID != null) {
                        LineProfile LP = result.getLineProfile();
                        //LineCredential LC = result.getLineCredential();
                        if (LP != null) {
                            JSONObject JOBNew = new JSONObject();
                            JSONObject JOB = new JSONObject();
                            JOBNew.put("name", LP.getDisplayName());
                            JOBNew.put("id", LP.getUserId());
                            JOBNew.put("email", "");
                            JOBNew.put("picture", LP.getPictureUrl());
                            if (JST != null) {
                                JOB = JST.SM.JSInterfaceBackObj(JOBNew);
                                JST.JSHandlerCallBack(CallBackID, JOB);
                            }
                            else if (JSS != null) {
                                JOB = JSS.SM.JSInterfaceBackObj(JOBNew);
                                JSS.JSHandlerCallBack(CallBackID, JOB);
                            }
                        } else {
                            if (JST != null) JST.SM.UIToast("Line登入遺失資訊");
                            else if (JSS != null) JSS.SM.UIToast("Line登入遺失資訊");
                        }
                    }
                    break;
                case CANCEL:
                    // Login canceled by user
                    Log.e("ERROR", "LINE Login Canceled by user.");
                    break;
                default:
                    // Login canceled due to other error
                    String ErrorMsg = result.getErrorData().toString();
//                    JS.SM.UIToast("Line登入發生錯誤\n" + ErrorMsg);
            }
        } catch (Exception e) {
            if (JST != null) JST.SM.EXToast(R.string.CM_DetectError, "LineOnActivityResult", e);
            else if (JSS != null)
                JSS.SM.EXToast(R.string.CM_DetectError, "LineOnActivityResult", e);
        }
    }

    public static void LineLogout(Activity Act) {
        String LINE_CHANNEL_ID = Act.getResources().getString(R.string.line_channel_id);
        LineApiClientBuilder apiClientBuilder = new LineApiClientBuilder(Act, LINE_CHANNEL_ID);
        apiClientBuilder.build().logout();
    }
}
