package com.mukicloud.mukitest.SFunc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.mukicloud.mukitest.JSInterface;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.Executor;

public class FBLoginHandler {
    private final LoginManager LM;
    private static CallbackManager CBM;
    private final Activity Act;
    private final SMethods SM;
    private final JSInterface JST;
    private final JSInterface JSS;
    private String CallBackID = "";

    private static FirebaseAuth mAuth;

    public FBLoginHandler(Activity Act, JSInterface JST, JSInterface JSS) {
        this.Act = Act;
        this.JST = JST;
        this.JSS = JSS;
        SM = new SMethods(Act);
//        FacebookSdk.sdkInitialize(Act.getApplicationContext());
        CBM = CallbackManager.Factory.create();
        LM = LoginManager.getInstance();
    }

    public void CallLoginFB(String CallBackID) {
        this.CallBackID = CallBackID;
        AccessToken AccTok = AccessToken.getCurrentAccessToken();
        if (AccTok == null || AccTok.isExpired()) {
            LM.setLoginBehavior(LoginBehavior.NATIVE_ONLY);
            LM.registerCallback(CBM, new FacebookCallback<>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
//                    handleFacebookAccessToken(loginResult.getAccessToken());
                    RequestFBUserInfo();
                }

                @Override
                public void onCancel() {
                    SM.UIToast("已取消登入");
                }

                @Override
                public void onError(FacebookException exception) {
                    SM.UIToast("沒有發現FacebookApp或其他錯誤\n" + exception.getLocalizedMessage());
                }
            });
            LM.logInWithReadPermissions(Act, Arrays.asList("public_profile", "email"));
        } else {
//            handleFacebookAccessToken(AccTok);
            RequestFBUserInfo();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("handleFacebookAccessToken", "handleFacebookAccessToken:" + token.getToken());
        mAuth = FirebaseAuth.getInstance();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signInWithCredential", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("FirebaseUser", "FirebaseUser:" + user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signInWithCredential", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }


    public static void FBonActivityResult(int requestCode, int resultCode, Intent data) {
        if (CBM != null) {
            CBM.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void RequestFBUserInfo() {
        AccessToken AccToken = AccessToken.getCurrentAccessToken();
        //當RESPONSE回來的時候
        GraphRequest request = GraphRequest.newMeRequest(AccToken,
                (JOB, response) -> {
                    try {
                        if (JOB != null) {
                            Log.d("RequestFBUserInfo", "RequestFBUserInfo: "+JOB);
                            String id = SM.JSONStrGetter(JOB, "id");
//                            JSONObject ResResult = new JSONObject();
//                            JSONObject pictureJOB = SM.JOBGetter(JOB, "picture");
//                            JSONObject dataJOB = SM.JOBGetter(pictureJOB, "data");
//                            String pictureUrl = SM.JSONStrGetter(dataJOB, "url");
//                            JOB.put("picture", pictureUrl);
//                            JSONObject JOBNew = new JSONObject();
//                            JOBNew = SM.JSInterfaceBackObj(JOB);
                            if (JST != null) JST.JSHandlerCallBack(CallBackID, id);
                            else if (JSS != null) JSS.JSHandlerCallBack(CallBackID, id);
                        } else {
                            FacebookRequestError FRE = response.getError();
                            if (FRE != null) {
                                String ErrorMsg = FRE.getErrorMessage();
                                SM.UIToast(ErrorMsg);
                            }
                        }
                    } catch (Exception e) {
                        Log.d("RequestFBUserInfo", "RequestFBUserInfo: "+e);
//                        SM.UIToast("取得資訊發生錯誤");
//                        SM.DebugToast("RequestFBUserInfo\n" + e.getMessage());
                    }
                });

        //包入你想要得到的資料 送出request
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,first_name,last_name,email,gender,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void FBLogout() {
        LM.logOut();
    }
}
