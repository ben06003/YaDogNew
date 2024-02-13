package com.mukicloud.mukitest;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.mukicloud.mukitest.Activity.ActivityWeb.hasPermissions;
import static com.mukicloud.mukitest.SFunc.LineLoginHandler.LineLogout;
import static com.mukicloud.mukitest.SFunc.LineLoginHandler.LineOnActivityResult;
import static com.mukicloud.mukitest.SFunc.LineLoginHandler.LoginViaLine;
import static com.mukicloud.mukitest.TD.MainURL;


import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.mukicloud.mukitest.Activity.ActivityWeb;
import com.mukicloud.mukitest.SFunc.FBLoginHandler;
import com.mukicloud.mukitest.SFunc.GoogleLoginHandler;
import com.mukicloud.mukitest.SFunc.NFileUtils;
import com.mukicloud.mukitest.SFunc.SCameraOCR;
import com.mukicloud.mukitest.SFunc.SFile;
import com.mukicloud.mukitest.SFunc.SHealth;
import com.mukicloud.mukitest.SFunc.SLocService;
import com.mukicloud.mukitest.SFunc.SMethods;
import com.mukicloud.mukitest.SFunc.SPhoto;
import com.mukicloud.mukitest.SFunc.SQRCode;
import com.mukicloud.mukitest.SFunc.SUploader;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import java.net.MalformedURLException;
import java.net.URL;

import me.leolin.shortcutbadger.ShortcutBadger;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSInterface {
    private final ActivityWeb Act;
    public SMethods SM;
    private SFile SF;
    private WebView WBV;
    private final LocationManager LM;
    private final SHealth SH;

    private String CallBackID_BAC;

    // region MegaCat JS

    // 開啟預設瀏覽起
    @JavascriptInterface
    public void openAndroid(String Url) {
        try {
            if (Url != null && Url.length() > 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Act.startActivity(intent);
            }
        } catch (Exception e) {
            SM.UIToast(R.string.JS_ERR_OpenLink);
        }
    }
    @JavascriptInterface
    public void openWebView(String GoToURL) {
        try {
            Log.d("TAG", "openWindow: " + GoToURL);
            if (GoToURL.length() > 0) {
                SM.SPClearStringData("appLoginInfo");//清空
                SM.SPSaveStringData("openWindow", GoToURL);//最後呼叫前往外網的網址
                SendJSB("openWindow", GoToURL, "True");
            } else {
                SM.UIToast(R.string.ERR_LostInfo);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openWindow", e);
        }
    }
    @JavascriptInterface
    public void closeWebView() {
        try {
            Act.RestoreWBV(0, 0);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openWindow", e);
        }
    }
    @JavascriptInterface
    public void facebookLogin(String CallBackID) {
        try {
//            Act.buyProducy("9.9cattest");
            FBLoginHandler FBH = new FBLoginHandler(Act, null, this);
            FBH.CallLoginFB(CallBackID);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "loginViaFB", e);
        }
    }

    @JavascriptInterface
    public void googleLogin(String CallBackID) {
        try {
            GoogleLoginHandler GLH = new GoogleLoginHandler(Act);
            GLH.GGLogin(Act, new GoogleLoginHandler.OnGGLoginResult() {
                @Override
                public void onLogin(JSONObject ProfileJOB) {
                    Log.d("googleLogin", "onLogin: "+ProfileJOB);
                    String id = SM.JSONStrGetter(ProfileJOB, "id");
                    Log.d("googleLogin", "id: "+id);
//                    JSONObject JOB = new JSONObject();
//                    JOB = SM.JSInterfaceBackObj(ProfileJOB);
                    JSHandlerCallBack(CallBackID, id);
                }

                @Override
                public void onLogout() {

                }
            });
        } catch (Exception e) {
            Log.d("googleLogin", "onLogin: "+e);
//            SM.SProgressDialog();
//            SM.EXToast(R.string.CM_DetectError, "googleLogin", e);
        }

    }
    @JavascriptInterface
    public void eventTracker(String key,String value) {
        try {
            JSONObject JOB = SM.JOBGetter(value);
            Act.FA.logEvent(key, JOB);
            Act.AppsFlyerLogEvent(key, JOB);
        } catch (Exception e) {
            Log.d("googleLogin", "onLogin: "+e);
        }

    }

    @JavascriptInterface
    public void inAppPurchase(String CallBackID,String productId,String token) {
        try {
            Act.billingBcakName = CallBackID;
            Act.billingToken = token;
            Act.buyProducy(productId);
        } catch (Exception e) {
            Log.d("googleLogin", "onLogin: "+e);
        }
    }

    @JavascriptInterface
    public void getFcmToken(String CallBackID) {
        try {
            String fcmToken = SM.SPReadStringData("token");
            JSHandlerCallBack(CallBackID, fcmToken);
        } catch (Exception e) {
            Log.d("getFcmToken", "error: "+e);
        }
    }

    // endregion

//    @SuppressLint("HardwareIds")

    // region 共用區塊

    public JSInterface(ActivityWeb act) {
        Act = act;
        SM = new SMethods(Act);
        SH = new SHealth(Act);
        LM = (LocationManager) Act.getSystemService(LOCATION_SERVICE);
        InitJSBroadcastReceiver();
        googleFitAskInit();
    }

    private void InitJSBroadcastReceiver() {
        if (Act != null) {
            Act.registerReceiver(Act.JBR, new IntentFilter("JSB"));
            WBV = Act.WBV_Main;
            SF = Act.SF;
        }
    }

    private void SendJSB(String Func, String... Value) {
        ArrayList<String> ValueAL = new ArrayList<>(Arrays.asList(Value));
        Intent intent = new Intent("JSB");
        intent.putExtra("Func", Func);
        intent.putStringArrayListExtra("ValueAL", ValueAL);
        Act.sendBroadcast(intent);
    }

    public void onDestroy() {
        if (Act != null) Act.unregisterReceiver(Act.JBR);
    }

    private boolean isPackageExisted(String targetPackage) {
        PackageManager pm = Act.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public void JSHandlerCallBack(String CallBackID, JSONObject JOB) {
        JSHandlerCallBack(CallBackID, JOB.toString());
    }

    public void JSHandlerCallBack(String CallBackID, String Value) {
        Log.d("JSHandlerCallBack", "CallBackID: "+CallBackID+",Value:"+Value);
        Log.d("JSHandlerCallBack", "javascript:window."+CallBackID+"(" + Value + ")");
        //String Value = JOB.toString().replace("\\", "\\\\");//防止跳脫字元
        new Handler(Looper.getMainLooper()).post(() -> {
            if (Act.WBV_Main != null)

//                Act.WBV_Main.evaluateJavascript("javascript:jsHandlerFunc(" + Value + "," + CallBackID + ")", value -> Log.d("JSHandlerCallBack", value));
                Act.WBV_Main.evaluateJavascript("javascript:window."+CallBackID+"('" + Value + "')", value -> Log.d("JSHandlerCallBack", value));
        });
    }

    public void JSHandlerCallBackF(String Func) {
        JSHandlerCallBackF(Func, "");
    }

    public void JSHandlerCallBackF(String Func, String Value) {
        try {
            String FValue = Uri.encode(Value);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (Act.WBV_Main != null)
                    Act.WBV_Main.evaluateJavascript("javascript:" + Func + "('" + FValue + "');", value -> Log.d("JSHandlerCallBack", value));
            });
        } catch (Exception e) {
            SM.UIToast(e.getMessage());
        }
    }
    public static String extractFileNameFromUrl(String urlString) {
        String fileName = null;
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    // endregion


    // region 預設使用
    @JavascriptInterface
    public void backAction(String CallBackID, int BackType) {
        try {
            if (BackType == 1) CallBackID_BAC = CallBackID;//點一下執行callback
            else if (BackType == 2) CallBackID_BAC = null;//由裝置告知操作者點第2下將離開APP
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "backAction", e);
        }
    }

    public boolean CallBack() {
        if (CallBackID_BAC != null && CallBackID_BAC.length() > 0) {
            JSONObject ReturnJOB = new JSONObject();
            SM.JSONValueAdder(ReturnJOB, "res_code", "1");
            JSHandlerCallBack(CallBackID_BAC, ReturnJOB);
            return true;
        }
        return false;
    }
    // endregion

    // region JS 相機
    @JavascriptInterface
    public void cameraPermission(String CallBackID,String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String failAsk = SM.JSONStrGetter(SendJOB, "failAsk");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        int resCode = -1;
        try {
            resCode = ActivityCompat.checkSelfPermission(Act, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ? 1 : -1;
            if (resCode == 1) {
                JOB = SM.JSInterfaceBackObj(ResResult);
            }else{
                JOB = SM.JSInterfaceErrorBackObj("無相機權限");
                if (failAsk.equals("true")) {
                    ActivityCompat.requestPermissions(Act, new String[]{
                            Manifest.permission.CAMERA}, TD.RQC_Permission_Camera);
                }
            }
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
            SM.EXToast(R.string.CM_DetectError, "cameraPermission", e);
        }
        JSHandlerCallBack(CallBackID, JOB);
    }
    //Scan QRCode===================================================================================
    @JavascriptInterface
    public void openScan(String CallBackID, String SendData) {
        try {
            //Start Scan
            Intent intent = new Intent(Act, SQRCode.class);
            intent.putExtra("QRInfo", SendData);
            Act.startActivity(intent);
            SQRCode.SetScanCompleteListener(Result -> {
                JSONObject JOB = new JSONObject();
                JSONObject ResResult = new JSONObject();
                try {
                    ResResult.put("scanStr", Result);
                    JOB = SM.JSInterfaceBackObj(ResResult);
                } catch (Exception e) {
                    SM.EXToast(R.string.ERR_ProcessData, "SetScanCompleteListener", e);
                    JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
                }
                JSHandlerCallBack(CallBackID, JOB);
            });
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_ProcessData, "openScan", e);
            JSONObject JOB = new JSONObject();
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
            JSHandlerCallBack(CallBackID, JOB);
        }
    }
    //OCR文字
    @JavascriptInterface
    public void UseOCR(String CallBackID) {//相容網站似乎有大小寫的問題
        useOCR(CallBackID);
    }

    @JavascriptInterface
    public void useOCR(String CallBackID) {
        new Handler(Looper.getMainLooper()).post(() -> {
            SCameraOCR SCO = new SCameraOCR(Act);
            if (SCO.ShowCameraDialog()) {
                SCO.setOnImageReceiveListener(new SCameraOCR.onImageReceive() {
                    @Override
                    public void onReceive(String PicPath) {

                    }

                    @Override
                    public void onReceive(String PicPath, Bitmap BMP) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            OCR_MLKit(CallBackID, BMP);
                        });
                    }
                });
            } else {
                SM.UIToast(R.string.ERR_CantUseCamera);
            }
        });
    }

    private void OCR_MLKit(String CallBackID, Bitmap BMP) {
        try {
            // When using Chinese script library
            TextRecognizer recognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
            InputImage image = InputImage.fromBitmap(BMP, 0);
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        //Callback
                        try {
                            ArrayList<JSONObject> BlockAL = new ArrayList<>();
                            for (Text.TextBlock block : visionText.getTextBlocks()) {
                                for (Text.Line line : block.getLines()) {
                                    for (Text.Element element : line.getElements()) {
                                        String elementText = element.getText();
                                        Point[] elementCornerPoints = element.getCornerPoints();
                                        if (elementCornerPoints != null && elementCornerPoints.length > 0) {
                                            int elementX = elementCornerPoints[0].x;
                                            int elementY = elementCornerPoints[0].y;
                                            JSONObject JOB = new JSONObject();
                                            JOB.put("X", elementX);
                                            JOB.put("Y", elementY);
                                            JOB.put("Val", elementText);
                                            BlockAL.add(JOB);
                                        }
                                    }
                                }
                            }

                            //根據XY解析位置排序
                            Collections.sort(BlockAL, (obj1, obj2) -> {
                                int OBJ1_X = SM.JSONIntGetter(obj1, "X");
                                int OBJ1_Y = SM.JSONIntGetter(obj1, "Y");
                                int OBJ2_X = SM.JSONIntGetter(obj2, "X");
                                int OBJ2_Y = SM.JSONIntGetter(obj2, "Y");
                                if (OBJ1_Y > OBJ2_Y) return 1;
                                else if (OBJ1_Y < OBJ2_Y) return -1;
                                else {
                                    return OBJ1_X > OBJ2_X ? 1 : -1;
                                }
                            });
                            //Turn JA
                            JSONArray BlockJA = new JSONArray();
                            for (JSONObject JOB : BlockAL) {
                                BlockJA.put(JOB);
                            }

                            JSONObject JOB = new JSONObject();
                            JSONObject returnJOB = new JSONObject();
                            returnJOB.put("OCR", visionText.getText());
                            returnJOB.put("OCRSort", BlockJA.toString());
                            JOB = SM.JSInterfaceBackObj(returnJOB);
                            JSHandlerCallBack(CallBackID, JOB);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> SM.UIToast("識別失敗\n" + e.getMessage()));
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_UpdateView);
        }
    }
    // endregion

    // region JS 其他

    // 取得APP裝置INFO
    @JavascriptInterface
    public void getAppInfo(String CallBackID) {
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        try {
            String OSVersion = String.valueOf(Build.VERSION.SDK_INT); // OS version
            String Device = Build.DEVICE;          // Device
            String Model = Build.MODEL;            // Model
            String Product = Build.PRODUCT;        // Product
            String ANDROID_ID = Settings.Secure.getString(Act.getContentResolver(), Settings.Secure.ANDROID_ID);
            String NotificationPermission = NotificationManagerCompat.from(Act).areNotificationsEnabled() == true ? "1" : "0";
            //Set JSONObject
            ResResult.put("token", SM.SPReadStringData("token"));//推播Token
            ResResult.put("PushyToken", SM.SPReadStringData("PushyToken"));//Pushy推播Token
            ResResult.put("device", Device);//裝置型號資訊
            ResResult.put("model", Model);//裝置型號資訊
            ResResult.put("product", Product);//裝置型號資訊
            ResResult.put("os_version", OSVersion);//系統版本
            ResResult.put("device_id", ANDROID_ID);//ANDROID_ID
            ResResult.put("application_version", BuildConfig.VERSION_CODE);//App版本號
            ResResult.put("application_version_name", BuildConfig.VERSION_NAME);//App版本名
            ResResult.put("notification_permission", NotificationPermission);//裝置的接收推播權限
            JOB = SM.JSInterfaceBackObj(ResResult);
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    // 另開APP內部瀏覽器
    @JavascriptInterface
    public void openWindow(String SendData) {
        //Data
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String GoToURL = SM.JSONStrGetter(SendJOB, "url_link");

        try {
            Log.d("TAG", "openWindow: " + GoToURL);
            if (GoToURL.length() > 0) {
                SM.SPClearStringData("appLoginInfo");//清空
                SM.SPSaveStringData("openWindow", GoToURL);//最後呼叫前往外網的網址
                SendJSB("openWindow", GoToURL, "True");
            } else {
                SM.UIToast(R.string.ERR_LostInfo);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openWindow", e);
        }
    }

    // 開啟預設瀏覽起
    @JavascriptInterface
    public void openUrlByBrowser(String SendData) {
        //Data
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Url = SM.JSONStrGetter(SendJOB, "url_link");
        try {
            if (Url != null && Url.length() > 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Act.startActivity(intent);
            }
        } catch (Exception e) {
            SM.UIToast(R.string.JS_ERR_OpenLink);
        }
    }

    // 開啟Loading動畫
    @JavascriptInterface
    public void startLoading() {
        try {
            SM.SWebProgress(true);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "startLoading", e);
        }
    }

    // 關閉Loading動畫
    @JavascriptInterface
    public void stopLoading() {
        try {
            SM.SWebProgress(false);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "stopLoading", e);
        }
    }

    // 撥號
    @JavascriptInterface
    public void dial(String SendData) {
        //Data
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Phone = SM.JSONStrGetter(SendJOB, "number");
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Phone));
            Act.startActivity(intent);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "dial", e);
        }
    }

    // 設定通知數字
    @JavascriptInterface
    public void setBadgeNum(String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String BadgeNum = SM.JSONStrGetter(SendJOB, "badge");
        try {
            int BadgeNumVal = SM.StI(BadgeNum);
            if (BadgeNumVal > 0) {
                ShortcutBadger.applyCount(Act, BadgeNumVal); //for 1.1.4+
                SM.SPSaveStringData("BadgeNum", BadgeNum);
            } else {
                ShortcutBadger.removeCount(Act); //for 1.1.4+
                SM.SPSaveStringData("BadgeNum", "0");
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "setBadgeNum", e);
        }
    }

    // 取得裝置設定權限(亮度)
    @JavascriptInterface
    public void getSettingsPermission(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String failAsk = SM.JSONStrGetter(SendJOB, "failAsk");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(Act)) {
                    JOB = SM.JSInterfaceBackObj(ResResult);
                }else{
                    JOB = SM.JSInterfaceErrorBackObj("");
                    if (failAsk.equals("true"))  {
                        SweetAlertDialog SAD = SM.SWToastCreator(new JSONObject()
                                .put("title", "需要裝置設定權限")
                                .put("content", "亮度控制服務需要裝置設定權限")
                                .put("confirmText", "去開啟")
                                .put("type", SweetAlertDialog.WARNING_TYPE));
                        SAD.setConfirmClickListener(sweetAlertDialog -> {
                                    SAD.cancel();
                                    //跳轉畫面到權限頁面給使用者勾選
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    intent.setData(Uri.parse("package:" + Act.getPackageName()));
                                    Act.startActivity(intent);
                                }
                        );
                    }
                }
            }else{
                JOB = SM.JSInterfaceBackObj(ResResult);
            }
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    // 取得裝置目前螢幕亮度
    @JavascriptInterface
    public void getBrightness(String CallBackID) {
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        int brightnessValue = 0;
        try {
            ContentResolver contentResolver = Act.getApplicationContext().getContentResolver();
            brightnessValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
            //Set JSONObject
            ResResult.put("brightness", brightnessValue);// 亮度
            JOB = SM.JSInterfaceBackObj(ResResult);
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    // 設定裝置螢幕亮度
    @JavascriptInterface
    public void setBrightness(String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Brightness = SM.JSONStrGetter(SendJOB, "brightness");
        int BrightnessInt = SM.StI(Brightness);
        try {
            Settings.System.putInt(Act.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, BrightnessInt);
        } catch (Exception e) {
            Log.d("TAG", "setBrightness: "+e);
            SM.EXToast(R.string.CM_DetectError, "setBrightness", e);
        }
    }

    // 內建分享(文字，檔案)
    @JavascriptInterface
    public void openShare(String SendData) {
        Log.d("TAG", "openShare:"+SendData);

        JSONObject SendJOB = SM.JOBGetter(SendData);
        String share_content = SM.JSONStrGetter(SendJOB, "share_content");
        String share_type = SM.JSONStrGetter(SendJOB, "share_type");

        if (share_type.equals("text")) {// 文字
            Log.d("TAG", "文字");

            try {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, share_content);
                Act.startActivity(Intent.createChooser(sharingIntent, "Share"));
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "openShareA", e);
            }
        } else {// 檔案
            Log.d("TAG", "檔案");

            try {
                String fileName = extractFileNameFromUrl(share_content);
                File FolderFile = Act.getFilesDir();
                // 下載檔案
                SF.RunDownload = true;
                SF.SFileDownloader(Act, share_content, FolderFile, true, false, false);
                SF.RunDownload = false;

                // 取得下載檔案
                File FolderFile2 = new File(Act.getFilesDir(), fileName);
                Uri uri = FileProvider.getUriForFile(Act, Act.getApplicationContext().getPackageName() + ".fileprovider", FolderFile2);
                // 分享檔案
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing File");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                Act.startActivity(Intent.createChooser(shareIntent, "Share File"));
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "openShareB", e);
            }
        }
    }

    @JavascriptInterface
    public void transferVariable(String CallBackID, String SendData) {
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Key = SM.JSONStrGetter(SendJOB, "key");
        String Value = SM.JSONStrGetter(SendJOB, "value");
        try {
            SM.SPSaveStringData(Key, Value);
            String transferVariableStr = SM.SPReadStringData(Key);
            JOB = SM.JSInterfaceBackObj(ResResult);
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    @JavascriptInterface
    public void getVariable(String CallBackID, String SendData) {
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Key = SM.JSONStrGetter(SendJOB, "key");
        try {
            String transferVariableStr = SM.SPReadStringData(Key);
            if (transferVariableStr != "") {
                ResResult.put("value",transferVariableStr);
                JOB = SM.JSInterfaceBackObj(ResResult);
            }else{
                ResResult.put("value","");
                JOB = SM.JSInterfaceBackObj(ResResult);
            }
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    @JavascriptInterface
    public void openFingerprint(String CallBackID) {
        SM.UIToast("待製作");
    }

    @JavascriptInterface
    public void alertDialog(String Title, String Content) {
        try {
            SM.SWToast(Title, Content, SweetAlertDialog.WARNING_TYPE, 3000);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "alertDialog", e);
        }
    }

    @JavascriptInterface
    public void alertToast(String Content, String Position, String Time) {
        try {
            int ToastGravity = Gravity.NO_GRAVITY;
            switch (Position) {
                case "start":
                    ToastGravity = Gravity.START;
                    break;
                case "end":
                    ToastGravity = Gravity.END;
                    break;
                case "bottom":
                    ToastGravity = Gravity.BOTTOM;
                    break;
                case "top":
                    ToastGravity = Gravity.TOP;
                    break;
                case "center":
                    ToastGravity = Gravity.CENTER;
                    break;
            }
            Toast toast = Toast.makeText(Act, Content, SM.StI(Time));
            toast.setGravity(ToastGravity, 0, 0);
            toast.show();
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "alertToast", e);
        }
    }

    @JavascriptInterface
    public void goTestSite(String ProjectID) {
        try {
            SendJSB("goTestSite", ProjectID);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "dial", e);
        }
    }

    @JavascriptInterface
    public String shareByLine(String SendData) {
        JSONObject JOB = new JSONObject();
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Text = SM.JSONStrGetter(SendJOB, "share_content");
        try {
            String PKGName = "jp.naver.line.android";
            if (isPackageExisted(PKGName)) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                //Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(Act.getContentResolver(), bitmap, null,null));
                //shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                //shareIntent.setType("image/jpeg"); //图片分享
                intent.setPackage(PKGName);
                intent.setType("text/plain"); // 纯文本
                intent.putExtra(Intent.EXTRA_TEXT, Text);
                //intent.putExtra(Intent.EXTRA_SUBJECT, "分享的标题");
                //intent.putExtra(Intent.EXTRA_TEXT, "分享的内容");
                Act.startActivity(intent);
                SM.JSONValueAdder(JOB, "ResCode", "Success");
            } else {
                Toast.makeText(Act, "請安裝Line", Toast.LENGTH_SHORT).show();
                SM.JSONValueAdder(JOB, "ResCode", "NoLine");
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "shareByLine", e);
            SM.JSONValueAdder(JOB, "ResCode", "Exception");
        }
        return JOB.toString();
    }

    @JavascriptInterface
    public String shareByFacebook(String SendData) {
        JSONObject JOB = new JSONObject();
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Text = SM.JSONStrGetter(SendJOB, "share_content");
        try {
            String PKGName = "com.facebook.katana";
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage(PKGName);
            shareIntent.setType("text/plain"); // 纯文本
            shareIntent.putExtra(Intent.EXTRA_TEXT, Text);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isPackageExisted(PKGName)) {
                Act.startActivity(shareIntent);
                SM.JSONValueAdder(JOB, "ResCode", "Success");
            } else {
                String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + Text;
                Intent URLIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                URLIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Act.startActivity(URLIntent);
                SM.JSONValueAdder(JOB, "ResCode", "ByUrl");
            }
        } catch (ActivityNotFoundException e) {
            SM.UIToast("無法使用Facebook應用程式");
            SM.JSONValueAdder(JOB, "ResCode", "ActivityNotFoundException");
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "shareByFacebook", e);
            SM.JSONValueAdder(JOB, "ResCode", "Exception");
        }
        return JOB.toString();
    }

    @JavascriptInterface
    public String shareByFacebook(String Link, String Quote) {
        JSONObject JOB = new JSONObject();
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(Link))
                    .setQuote(Quote)
                    .build();
            ShareDialog shareDialog = new ShareDialog(Act);
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            SM.JSONValueAdder(JOB, "ResCode", "Success");
        } else {
            SM.JSONValueAdder(JOB, "ResCode", "Failed");
        }
        return JOB.toString();
    }

    // endregion JS-其他

    // region JS 第三方登入

    @JavascriptInterface
    public void loginViaFB(String CallBackID) {
        try {
            FBLoginHandler FBH = new FBLoginHandler(Act, null, this);
            FBH.CallLoginFB(CallBackID);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "loginViaFB", e);
        }
    }

    @JavascriptInterface
    public void loginViaLINE(String CallBackID) {
        try {
            LoginViaLine(Act, CallBackID);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "loginViaLINE", e);
        }
    }

    @JavascriptInterface
    public void loginViaGoogle(String CallBackID) {
        try {
            GoogleLoginHandler GLH = new GoogleLoginHandler(Act);
            GLH.GGLogin(Act, new GoogleLoginHandler.OnGGLoginResult() {
                @Override
                public void onLogin(JSONObject ProfileJOB) {
                    JSONObject JOB = new JSONObject();
                    JOB = SM.JSInterfaceBackObj(ProfileJOB);
                    JSHandlerCallBack(CallBackID, JOB);
                }

                @Override
                public void onLogout() {

                }
            });
        } catch (Exception e) {
            SM.SProgressDialog();
            SM.EXToast(R.string.CM_DetectError, "loginViaGoogle", e);
        }
    }

    @JavascriptInterface
    public void logoutAll(String CallBackID) {
        try {
            //Google
            new GoogleLoginHandler(Act).GGLogout();
            //Facebook
            new FBLoginHandler(Act, null, this).FBLogout();
            //Line
            LineLogout(Act);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "loginViaGoogle", e);
        }
    }

    // endregion


    // region JS 健康
    //Health========================================================================================
    private void googleFitAskInit() {
        String value = SM.SPReadStringData("googleFitAsk");
        if (value.length() == 0) SM.SPSaveStringData("googleFitAsk", "0");
    }

    //導向健康權限設定
    @JavascriptInterface
    public void setHealthWritePermission() {
        SH.requestPermission();
    }

    //健康寫入權限查詢
    @JavascriptInterface
    public void healthWritePermission(String callBackId, String type) {
        JSONObject JOB = new JSONObject();
        boolean hasPermission = SH.hasWritePermission(type);
        SM.JSONValueAdder(JOB, "res_code", hasPermission ? "1" : "0");
        JSHandlerCallBack(callBackId, JOB.toString());
    }

    //健康讀取權限查詢
    @JavascriptInterface
    public void healthReadPermission(String callBackId, String type) {
        JSONObject JOB = new JSONObject();
        boolean hasPermission = SH.hasReadPermission(type);
        SM.JSONValueAdder(JOB, "res_code", hasPermission ? "1" : "0");
        JSHandlerCallBack(callBackId, JOB.toString());
    }

    //取得健康數據
    @JavascriptInterface
    public void getHealth(String callBackId, String value) {
        SH.read(callBackId, value, null, this);
    }

    //寫入健康數據
    @JavascriptInterface
    public void saveHealth(String callBackId, String value) {
        SH.save(callBackId, value, null, this);
    }

    private void onActivityResultHealth(int resultCode) {
        if (resultCode != RESULT_OK) {
            SM.UIToast("請求App健康權限失敗");
        }
    }

    private void onActivityResultGGFit(int resultCode) {
        if (resultCode != RESULT_OK) {
            SM.UIToast("請求Google Fit權限失敗");
        }
        SM.SPSaveStringData("googleFitAsk", "1");
        JSHandlerCallBackF("HealthPermissionChange");
    }
    // endregion

    // region JS 檔案
    //File==========================================================================================
    @JavascriptInterface
    public void downloadFiles(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String path_list = SM.JSONStrGetter(SendJOB, "path_list");
        String progress_display = SM.JSONStrGetter(SendJOB, "progress_display");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        try {
            JSONArray DownloadListJA = SM.JAGetter(path_list);
            Log.d("downloadFiles", "downloadFiles: "+DownloadListJA);
            if (DownloadListJA.length() > 0) {
                SF.SetOnDownloadStatusListener(Status -> {
//                    JSONObject JOB = new JSONObject();
//                    SM.JSONValueAdder(JOB, "res_code", "1");
//                    JSHandlerCallBack(CallBackID, JOB);
                });
                SF.StartDownload(Act, DownloadListJA);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "getDownloadList", e);
        }
    }

    @JavascriptInterface
    public void getFilesSize(String CallBackID) {
        try {
            //getTotalInternalMemorySize
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long blockCount = stat.getBlockCountLong();
            float TotalInternalMemorySize = (float) ((double) (blockCount * blockSize) / (1024 * 1024));

            JSONObject JOB = new JSONObject();
            JOB.put("TotalInternalMemorySize", TotalInternalMemorySize);
            File FolderCache = Act.getExternalFilesDir("Cache");
            if (FolderCache != null) {
                float TotalFileSize = (float) ((double) SM.GetFolderSize(FolderCache) / (1024 * 1024));
                JOB.put("TotalFileSize", TotalFileSize);
            }
            JSHandlerCallBack(CallBackID, JOB);
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "getFilesSize", e);
        }
    }

    @JavascriptInterface
    public void isFile(String CallBackID, String FileName) {
        try {
            JSONObject JOB = new JSONObject();
            JOB.put("FileName", FileName);
            File FolderCache = Act.getExternalFilesDir("Cache");
            if (FolderCache != null) {
                File file = new File(FolderCache, FileName);
                JOB.put("Exist", file.exists());
            } else {
                JOB.put("Exist", false);
            }
            JSHandlerCallBack(CallBackID, JOB);
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "isFile", e);
        }
    }

    @JavascriptInterface
    public void openFile(String FileName) {
        try {
            File file = new File(Act.getExternalFilesDir("Cache"), FileName);
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(Act, Act.getApplicationContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String Mime = "*/*";
                MimeTypeMap MTM = MimeTypeMap.getSingleton();
                if (MTM.hasExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))) {
                    Mime = MTM.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                }
                intent.setDataAndType(uri, Mime);
                Act.startActivity(intent);
            } else {
                SM.UIToast(R.string.JS_ERR_NoOfflineFile);
            }
        } catch (ActivityNotFoundException e) {
            SM.UIToast("您的裝置沒有可以開啟此檔案的應用");
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openFile", e);
        }
    }

    //下載到Download 資料夾
    @JavascriptInterface
    public void downloadUserFile(String FileUrl) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (Environment.getExternalStorageState() != null) {
                        File FolderFile = new File(Environment.getExternalStorageDirectory() + "/Download");

                        SFile.RunDownload = true;//允許開始下載
                        SF.SetOnDownloadStatusListener(Status -> {
                            if (Status.equals("3")) {
                                SM.SWToast(R.string.SF_Hint_DownloadFinish, SweetAlertDialog.SUCCESS_TYPE);
                            } else if (Status.equals("4")) {
                                SM.SWToast(R.string.SF_Hint_DownloadFailed, SweetAlertDialog.ERROR_TYPE);
                            }
                        });
                        SF.SFileDownloader(Act, FileUrl, FolderFile, true, false, true);
                    }
                } catch (Exception e) {
                    SM.EXToast(R.string.ERR_ProcessData, "downloadFile", e);
                } finally {
                    SFile.RunDownload = false;//關閉允許下載
                    SF.SProgressDialog(Act);//Close Progress
                }
            }
        }.start();


    }

    @JavascriptInterface
    public void downloadFile(String CallBackID, String FileUrl) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (Environment.getExternalStorageState() != null) {
                        SFile.RunDownload = true;//允許開始下載
                        SF.SetOnDownloadStatusListener(Status -> {
                            try {
                                if (Status.equals("3")) {
                                    SM.SWToast(R.string.SF_Hint_DownloadFinish, SweetAlertDialog.SUCCESS_TYPE);
                                    JSHandlerCallBack(CallBackID, new JSONObject().put("result_code", 1));//Success
                                } else if (Status.equals("4")) {
                                    SM.SWToast(R.string.SF_Hint_DownloadFailed, SweetAlertDialog.ERROR_TYPE);
                                    JSHandlerCallBack(CallBackID, new JSONObject().put("result_code", 2));//Failed
                                }
                            } catch (Exception e) {
                                SM.EXToast(R.string.ERR_ProcessData, "downloadFile", e);
                            }
                        });
                        File FolderFile = Act.getExternalFilesDir("Cache");
                        SF.SFileDownloader(Act, FileUrl, FolderFile, true, false, true);
                        SF.SProgressDialog(Act);//Close Progress Dialog
                    }
                } catch (Exception e) {
                    SM.EXToast(R.string.ERR_ProcessData, "downloadFile", e);
                } finally {
                    SFile.RunDownload = false;//關閉允許下載
                    SF.SProgressDialog(Act);//Close Progress
                }
            }
        }.start();
    }

    @JavascriptInterface
    public void deleteSingleFile(String FilePath) {
        try {
            File DeleteFile = new File(Act.getExternalFilesDir("Cache"), FilePath);
            if (DeleteFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                DeleteFile.delete();
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "deleteSingleFile", e);
        }
    }

    @JavascriptInterface
    public void deleteAllFiles() {
        try {
            File FolderCache = Act.getExternalFilesDir("Cache");
            if (FolderCache != null) {
                SM.DeleteRecursive(FolderCache);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "deleteAllFiles", e);
        }
    }

    private String uploadFileCID, uploadFileUrl;
    private final String[] UploadMimeTypes = {"application/pdf", "image/*"};

    @JavascriptInterface
    public void uploadFile(String CallBackID, String UploadUrl) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, TD.RQC_Permission);
                        return;
                    }
                    uploadFileCID = CallBackID;
                    uploadFileUrl = UploadUrl;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, UploadMimeTypes);
                    StringBuilder TypeDB = new StringBuilder();
                    for (int cnt = 0; cnt < UploadMimeTypes.length; cnt++) {
                        TypeDB.append(UploadMimeTypes[cnt]);
                        if (cnt < UploadMimeTypes.length - 1) TypeDB.append("|");
                    }
                    intent.setType(TypeDB.toString());
                    Intent destIntent = Intent.createChooser(intent, "選擇檔案");
                    //destIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Act.startActivityForResult(destIntent, TD.RQC_SelectFile);
                } catch (Exception e) {
                    SM.EXToast(R.string.ERR_ProcessData, "downloadFile", e);
                } finally {
                    SFile.RunDownload = false;//關閉允許下載
                    SF.SProgressDialog(Act);//Close Progress
                }
            }
        }.start();
    }

    private void uploadFileOnFileSelect(Intent data) {
        try {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null && uploadFileCID != null) {
                    String FilePath = NFileUtils.getPath(Act, uri);
                    if (FilePath.length() > 0) {
                        //Upload
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    SM.SProgressDialog(true, "檔案上傳中", 1000);
                                    SUploader SU = new SUploader();
                                    JSONObject ResJOB = SU.SUpload(uploadFileUrl, null, FilePath);
                                    boolean Success = SM.GetResultJOBAvailable(ResJOB);
                                    JSONObject JOB = new JSONObject();
                                    if (Success) {
                                        ResJOB.remove("SResult");
                                        JOB.put("result_code", 1);
                                        JOB.put("files", SM.JOBGetter(ResJOB, "file"));
                                    } else {
                                        JOB.put("result_code", 2);
                                    }
                                    JSHandlerCallBack(uploadFileCID, JOB);//Success:Failed
                                } catch (Exception e) {
                                    SM.EXToast(R.string.ERR_PrepareData, "transferWebDataUpload", e);
                                } finally {
                                    SM.SProgressDialog();
                                }
                            }
                        }.start();
                    } else {
                        SM.UIToast(R.string.ERR_LostInfo);
                    }
                } else {
                    SM.UIToast(R.string.ERR_LostInfo);
                }
            }
        } catch (SecurityException e) {
            SM.EXToast("錯誤的匯入方式", "transferWebData_Security", e);
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "transferWebData", e);
        }
    }
    // endregion

    // region JS 位置
    private String CallBackID_RGP;

    //Location======================================================================================
    @JavascriptInterface
    public void isGPS(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String failAsk = SM.JSONStrGetter(SendJOB, "failAsk");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        LocationManager LM = (LocationManager) Act.getSystemService(Context.LOCATION_SERVICE);
        try  {
            if (LM != null) {
                if (LM.isProviderEnabled(LocationManager.GPS_PROVIDER) && LM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    JOB = SM.JSInterfaceBackObj(ResResult);
                } else {
                    JOB = SM.JSInterfaceErrorBackObj("用戶未開啟定位功能","-1");
                    if (failAsk.equals("true")) {
                        SweetAlertDialog SAD = SM.SWToastCreator(new JSONObject()
                                .put("title", "需要裝置定位權限")
                                .put("content", "已便使用取的裝置座標")
                                .put("confirmText", "去開啟")
                                .put("type", SweetAlertDialog.WARNING_TYPE));
                        SAD.setConfirmClickListener(sweetAlertDialog -> {
                                    SAD.cancel();
                                    //跳轉畫面到權限頁面給使用者勾選
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Act.startActivity(intent);
                                }
                        );
                    }
                }
            } else {
                JOB = SM.JSInterfaceErrorBackObj("無法取得 LocationManager","-2");
            }
        } catch (Exception e){
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage(),"-3");
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    @JavascriptInterface
    public void isGPSPermission(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String failAsk = SM.JSONStrGetter(SendJOB, "failAsk");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        boolean FineLoc = ActivityCompat.checkSelfPermission(Act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        try  {
            if (FineLoc) {
                JOB = SM.JSInterfaceBackObj(ResResult);
            } else {
                JOB = SM.JSInterfaceErrorBackObj("用戶未開啟定位功能","-1");
                if (failAsk.equals("true")) {
                    ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TD.RQC_Permission_LocService);
                }
            }
        } catch (Exception e){
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage(),"-2");
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    @JavascriptInterface
    public void requestGps() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Act.startActivity(intent);
    }


    @JavascriptInterface
    public void requestGpsPermission(String CallBackID) {
        CallBackID_RGP = CallBackID;
        ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TD.RQC_Permission_LocService);
    }

    @JavascriptInterface
    public void getCoordinateCond(String CallBackID, String Data) {
        try {
            String resCode, resContent;
            JSONObject TaskJOB = SM.JOBGetter(Data);
            int StartLoc = SM.JSONIntGetter(TaskJOB, "coordinateCondKey", 0);
            if (StartLoc == 1) {
                boolean FineLoc = ActivityCompat.checkSelfPermission(Act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (FineLoc) {
                    LocationManager LM = (LocationManager) Act.getSystemService(Context.LOCATION_SERVICE);
                    if (LM != null) {
                        if (LM.isProviderEnabled(LocationManager.GPS_PROVIDER) && LM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            SLocService.StartService(Act, TaskJOB);
                            resCode = "1";
                            resContent = "成功";
                        } else {
                            SM.SWToast(R.string.SLS_OpenLoc);
                            resCode = "-1";
                            resContent = "用戶未開啟定位功能";
                        }
                    } else {
                        resCode = "-2";
                        resContent = "無法取得 LocationManager";
                    }
                } else { //Ask Permission
                    ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TD.RQC_Permission_LocService);
                    resCode = "-3";
                    resContent = "為戶尚未提供權限";
                }
            } else {
                SLocService.StopService(Act);
                resCode = "2";
                resContent = "關閉成功";
            }
            JSONObject ReturnJOB = new JSONObject();
            ReturnJOB.put("res_code", resCode);
            ReturnJOB.put("res_content", resContent);
            JSHandlerCallBack(CallBackID, ReturnJOB);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "getCoordinateCond", e);
        }
    }

    @JavascriptInterface
    public void getCoordinate(String CallBackID) {
        JSONObject JOB = new JSONObject();
        try {
            if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(Act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                JOB = SM.JSInterfaceErrorBackObj("無取得座標權限");
                JSHandlerCallBack(CallBackID, JOB);
            } else {
                if (LM.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || LM.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationListener LL = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location Loc) {
                            JSONObject JOB = new JSONObject();
                            JSONObject ResultJOB = new JSONObject();
                            try {
                                if (Loc != null) {
                                    ResultJOB.put("latitude", Loc.getLatitude());
                                    ResultJOB.put("longitude", Loc.getLongitude());
                                    JOB = SM.JSInterfaceBackObj(ResultJOB);
                                    JSHandlerCallBack(CallBackID, JOB);
                                    LM.removeUpdates(this);
                                }
                            } catch (Exception e) {
                                JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
                                JSHandlerCallBack(CallBackID, JOB);
                            }
                        }
                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {
                        }

                        @Override
                        public void onProviderEnabled(String s) {
                        }

                        @Override
                        public void onProviderDisabled(String s) {
                        }
                    };
                    LM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, LL);
                    LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, LL);
                } else {
                    JOB = SM.JSInterfaceErrorBackObj("Provider Not Available");
                    JSHandlerCallBack(CallBackID, JOB);
                }
            }
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage());
            JSHandlerCallBack(CallBackID, JOB);
        }
    }

    @JavascriptInterface
    public void openCoordinateByMap(String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String Lat = SM.JSONStrGetter(SendJOB, "latitude");
        String Lng = SM.JSONStrGetter(SendJOB, "longitude");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Lat + "," + Lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (mapIntent.resolveActivity(Act.getPackageManager()) != null) {
                Act.startActivity(mapIntent);
            } else {
                SM.UIToast(R.string.JS_ERR_InstallGMap);
            }
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_PrepareData);
        }
    }

    // endregion

    @JavascriptInterface
    public void goBackUrl(String BackUrl) {
        try {
            SendJSB("goBackUrl", BackUrl);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "goBackUrl", e);
        }
    }

    @JavascriptInterface
    public void setBackUrl(String BackUrl) {
        try {
            SendJSB("setBackUrl", BackUrl);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "goBackUrl", e);
        }
    }


    @JavascriptInterface
    public void callSelectPic(String CallBackID, String BrowseType, String PicWidth, String PicHeight, String MaxMultiNum, String ReturnType) {
        callSelectPic(CallBackID, BrowseType, PicWidth, PicHeight, MaxMultiNum, ReturnType, "1");
    }

    /*
        Shape 1 => 方形 2 => 圓形
     */
    @JavascriptInterface
    public void callSelectPic(String CallBackID, String BrowseType, String PicWidth, String PicHeight, String MaxMultiNum, String ReturnType, String Shape) {
        try {
            JSONObject RequestJOB = new JSONObject();
            RequestJOB.put("CallBackID", CallBackID);
            RequestJOB.put("BrowseType", BrowseType);
            RequestJOB.put("Width", PicWidth);
            RequestJOB.put("Height", PicHeight);
            RequestJOB.put("MaxMultiNum", MaxMultiNum);
            RequestJOB.put("ReturnType", ReturnType);
            RequestJOB.put("Shape", Shape);

            Intent SPhotoIntent = new Intent(Act, SPhoto.class);
            SPhotoIntent.putExtra("CallBackID", CallBackID);
            SPhotoIntent.putExtra("RequestJOB", RequestJOB.toString());
            Act.startActivityForResult(SPhotoIntent, TD.RQC_SPhoto);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "callSelectPic", e);
        }
    }




    //Offline=======================================================================================
    @JavascriptInterface
    public void transferWebData(String UrlLink, JSONArray PathListJA) {
        try {
            //JSONArray PathListJA = SM.JAGetter("");
            if (PathListJA.length() > 0) {
                JSONArray DownloadListJA = new JSONArray();
                for (int cnt = 0; cnt < PathListJA.length(); cnt++) {
                    DownloadListJA.put(UrlLink + PathListJA.getString(cnt));
                }
                //Delete Old Data First
                File FolderFile = Act.getExternalFilesDir("Cache");
                if (FolderFile != null) SM.DeleteRecursive(FolderFile);
                //StartDownload
                SF.StartDownload(Act, DownloadListJA);
                SM.SPSaveStringData("PreTransferWebTime", String.valueOf(System.currentTimeMillis()));
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "transferWebData", e);
        }
    }

    @JavascriptInterface
    public void getTransferDatetime(String CallBackID) {
        JSONObject JOB = new JSONObject();
        try {
            String PreTransferWebTimeStr = SM.SPReadStringData("PreTransferWebTime");
            long PreTransferWebTime = SM.StL(PreTransferWebTimeStr);
            String res_code = PreTransferWebTime != 0 ? "1" : "0";
            JOB.put("res_code", res_code);
            if (res_code.equals("1")) {
                String Time = SM.MillisToTime(PreTransferWebTimeStr, "Both");
                JOB.put("res_content", Time);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "getTransferDatetime", e);
        }
        JSHandlerCallBack(CallBackID, JOB);
    }





    @JavascriptInterface
    public void setFirstPage(String FirstPageUrl) {
        try {
            SendJSB("setFirstPage", FirstPageUrl);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "setFirstPage", e);
        }
    }

    @JavascriptInterface
    public void openSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(Act, R.raw.opensound);
            mediaPlayer.start();
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openSound", e);
        }
    }

    @JavascriptInterface
    public void openVibration() {
        try {
            Vibrator VB = (Vibrator) Act.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            if (VB != null) VB.vibrate(1000);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openVibration", e);
        }
    }

    //WiFi==========================================================================================
    @JavascriptInterface
    public void getWifiInfo(String CallBackID) {
        JSONObject JOB = new JSONObject();
        JSONObject ReturnJOB = new JSONObject();
        try {
            WifiManager WM = (WifiManager) Act.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (WM != null) {
                WifiInfo wifiInfo = WM.getConnectionInfo();
                ReturnJOB.put("ssid", wifiInfo.getSSID().replace("\"", ""));
                ReturnJOB.put("bssid", wifiInfo.getBSSID());
                JOB = SM.JSInterfaceBackObj(ReturnJOB);
            } else {
                JOB = SM.JSInterfaceErrorBackObj("找不到WIFI訊息");
            }
        } catch (Exception e) {
            JOB = SM.JSInterfaceErrorBackObj("找不到WIFI訊息:"+e.getMessage());
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    // region JS 日曆
    //https://androidraja.blogspot.com/2017/04/android-calendar-provider.html

    private String CallBack_CLP;
    private int CCalendarID = -1;

    @JavascriptInterface
    public void calendarPermission(String CallBackID,  String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String failAsk = SM.JSONStrGetter(SendJOB, "failAsk");
        JSONObject JOB = new JSONObject();
        JSONObject ResResult = new JSONObject();
        try {
            CallBack_CLP = CallBackID;
            if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                JOB = SM.JSInterfaceBackObj(ResResult);
                JSHandlerCallBack(CallBack_CLP, JOB);
            } else {//請求權限
                ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, TD.RQC_Permission_Calendar);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "calendarPermission", e);
        }
    }

    @JavascriptInterface
    public void getGoogleCalendarEvent(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String StartTime = SM.JSONStrGetter(SendJOB, "start_time");
        String EndTime = SM.JSONStrGetter(SendJOB, "end_time");
        try {
            if (CCalendarID != -1)
                calendarGetEvents(CallBackID, StartTime, EndTime);
            else calendarGetAccount(() -> calendarGetEvents(CallBackID, StartTime, EndTime));
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "getGoogleCalendarEvent", e);
        }
    }

    @JavascriptInterface
    public void googleCalendarEventCreate(String CallBackID, String SendData) {
        try {
            if (CCalendarID != -1)
                calendarInsertEvent(CallBackID, SM.JOBGetter(SendData));
            else
                calendarGetAccount(() -> calendarInsertEvent(CallBackID, SM.JOBGetter(SendData)));
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "googleCalendarEventCreate", e);
        }
    }

    @JavascriptInterface
    public void googleCalendarEventDelete(String CallBackID, String SendData) {
        JSONObject SendJOB = SM.JOBGetter(SendData);
        String EventId = SM.JSONStrGetter(SendJOB, "identifier");
        if (CCalendarID != -1)
            calendarDeleteEvent(CallBackID, EventId);
        else
            calendarGetAccount(() -> calendarDeleteEvent(CallBackID, EventId));
    }

    private interface GetAccountListener {
        void onFinish();
    }

    //選擇帳號
    private GetAccountListener GAL;

    private void calendarGetAccount(GetAccountListener GAL) {
        try {
            this.GAL = GAL;
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = AccountManager.newChooseAccountIntent(null, null,
                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, null, null, null, null);
            } else {
                intent = AccountManager.newChooseAccountIntent(null, null,
                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            }
            Act.startActivityForResult(intent, TD.RQC_Calendar_GetAccount);  //GET_ACCOUNT_NAME_REQUEST是一個自訂的int, 用作分辨所返回的結果
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "calendarGetAccount", e);
        }
    }

    //選擇日曆
    private void calendarGetAccountHandler(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            CCalendarID = -1;//Clear CalendarID
            String targetAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);//使用者所選的帳戶名稱
            // 設定要返回的資料
            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Calendars._ID,                             // 0 日歷ID
                    CalendarContract.Calendars.ACCOUNT_NAME,                // 1 日歷所屬的帳戶名稱
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,       // 2 日歷名稱
                    CalendarContract.Calendars.OWNER_ACCOUNT,                  // 3 日歷擁有者
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,       // 4 對此日歷所擁有的權限
            };
            // 根據上面的設定，定義各資料的索引，提高代碼的可讀性
            int PROJECTION_ID_INDEX = 0;
            // 查詢日歷
            Cursor cur;
            ContentResolver cr = Act.getContentResolver();
            Uri uri = CalendarContract.Calendars.CONTENT_URI;
            // 定義查詢條件，找出屬於上面Google帳戶及可以完全控制的日歷
            String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " = ?))";
            String[] selectionArgs = new String[]{targetAccount,
                    "com.google",
                    Integer.toString(CalendarContract.Calendars.CAL_ACCESS_OWNER)};
            // 因為targetSDK=25，所以要在Apps運行時檢查權限
            int permissionCheck = ContextCompat.checkSelfPermission(Act, Manifest.permission.READ_CALENDAR);
            // 建立List來暫存查詢的結果
            final List<Integer> calendarIdList = new ArrayList<>();
            // 如果使用者給了權限便開始查詢日歷
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
                if (cur != null) {
                    while (cur.moveToNext()) {
                        calendarIdList.add((int) cur.getLong(PROJECTION_ID_INDEX));
                    }
                    cur.close();
                }
                if (calendarIdList.size() != 0) {
                    CCalendarID = calendarIdList.get(0);
                    GAL.onFinish();
                } else {
                    SM.UIToast("找不到日歷");
                }
            } else {
                SM.UIToast("沒有日曆所需的權限");
            }
        }
    }

    private void calendarGetEvents(String CallBackID, String StartTime, String EndTime) {
        JSONObject ReturnJOB = new JSONObject();
        try {
            // 設定要返回的資料
            String[] INSTANCE_PROJECTION = new String[]{
                    CalendarContract.Instances.EVENT_ID,    // 0 活動ID
                    CalendarContract.Instances.BEGIN,       // 1 活動開始日期時間
                    CalendarContract.Instances.END,         // 2 活動結束日期時間
                    CalendarContract.Instances.ALL_DAY,     // 3 是否為整天事件
                    CalendarContract.Instances.TITLE,       // 4 活動標題
                    CalendarContract.Instances.DESCRIPTION, // 5 活動內文
            };
            // 根據上面的設定，定義各資料的索引，提高代碼的可讀性
            int INDEX_ID = 0;
            int INDEX_BEGIN = 1;
            int INDEX_END = 2;
            int INDEX_ALL_DAY = 3;
            int INDEX_TITLE = 4;
            int INDEX_DESC = 5;
            // 指定一個時間段，查詢以下時間內的所有活動
            long startMillis = SM.DateToMillis(StartTime);
            long endMillis = SM.DateToMillis(EndTime);
            // 定義查詢條件，找出上面日歷中指定時間段的所有活動
            String selection = CalendarContract.Events.CALENDAR_ID + " = ?";
            String[] selectionArgs = new String[]{SM.ItS(CCalendarID)};
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, startMillis);
            ContentUris.appendId(builder, endMillis);
            // 因為targetSDK=25，所以要在Apps運行時檢查權限
            int permissionCheck = ContextCompat.checkSelfPermission(Act, Manifest.permission.READ_CALENDAR);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                final JSONArray eventJA = new JSONArray();
                // 查詢活動
                ContentResolver cr = Act.getContentResolver();
                Cursor cur = cr.query(builder.build(),
                        INSTANCE_PROJECTION,
                        selection,
                        selectionArgs,
                        null);
                if (cur != null) {
                    while (cur.moveToNext()) {
                        long startM = cur.getLong(INDEX_BEGIN);
                        long EndM = cur.getLong(INDEX_END);
                        String startDate = SM.MillisToTime(String.valueOf(startM), "Both");
                        String endDate = SM.MillisToTime(String.valueOf(EndM), "Both");
                        JSONObject JOB = new JSONObject();
                        JOB.put("identifier", cur.getLong(INDEX_ID));
                        JOB.put("startDate", startDate);
                        JOB.put("endDate", endDate);
                        JOB.put("isAllDay", cur.getString(INDEX_ALL_DAY));
                        JOB.put("lastModified", "");
                        JOB.put("title", cur.getString(INDEX_TITLE));
                        JOB.put("note", cur.getString(INDEX_DESC));
                        eventJA.put(JOB);
                    }
                    cur.close();
                }
                JSONObject ReturnJOBTidy = new JSONObject();
                ReturnJOBTidy.put("events", eventJA);
                ReturnJOB = SM.JSInterfaceBackObj(ReturnJOBTidy);
            } else {
                ReturnJOB = SM.JSInterfaceErrorBackObj("沒有日曆所需的權限","-2");
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "calendarGetAccount", e);
            ReturnJOB = SM.JSInterfaceErrorBackObj(e.getMessage());
        }
        JSHandlerCallBack(CallBackID, ReturnJOB);
    }

    //新增活動
    private void calendarInsertEvent(String CallBackID, JSONObject DataJOB) {
        JSONObject JOB = new JSONObject();
        JSONObject ReturnJOB = new JSONObject();
        try {
            String title = SM.JSONStrGetter(DataJOB, "title");
            String notes = SM.JSONStrGetter(DataJOB, "notes");
            String startDate = SM.JSONStrGetter(DataJOB, "start_time");
            String endDate = SM.JSONStrGetter(DataJOB, "end_time");
            String isAllDay = SM.JSONStrGetter(DataJOB, "is_all_day");

            Boolean AllDay = isAllDay.equals("true") ? true : false ;
            long startM = SM.DateToMillis(startDate);
            long endM = SM.DateToMillis(endDate);

            // 新增活動
            ContentResolver cr = Act.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startM);
            values.put(CalendarContract.Events.DTEND, endM);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, notes);
            values.put(CalendarContract.Events.ALL_DAY,AllDay);
            values.put(CalendarContract.Events.CALENDAR_ID, CCalendarID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
            // 因為targetSDK=25，所以要在Apps運行時檢查權限
            int permissionCheck = ContextCompat.checkSelfPermission(Act, Manifest.permission.WRITE_CALENDAR);
            // 如果使用者給了權限便開始新增日歷
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                if (uri != null) { // 返回新建活動的ID
                    long eventID = Long.parseLong(uri.getLastPathSegment());
                    ReturnJOB.put("identifier", eventID);
                    JOB = SM.JSInterfaceBackObj(ReturnJOB);
                } else {//新增失敗
                    JOB = SM.JSInterfaceErrorBackObj("新增失敗","-1");
                }
            } else {//沒有權限
                JOB = SM.JSInterfaceErrorBackObj("沒有權限","-2");
            }
        } catch (Exception e) {
            Log.e("calendarInsertEvent", "", e);
            SM.EXToast(R.string.CM_DetectError, "calendarInsertEvents", e);
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage(),"-3");
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    //刪除活動
    public void calendarDeleteEvent(String CallBackID, String EventId) {
        JSONObject JOB = new JSONObject();
        JSONObject ReturnJOB = new JSONObject();
        try {
            long eventId = Long.parseLong(EventId);
            ContentResolver cr = Act.getContentResolver();
            int permissionCheck = ContextCompat.checkSelfPermission(Act, Manifest.permission.WRITE_CALENDAR);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                int result = cr.delete(uri, null, null);
                if (result > 0) {
                    JOB = SM.JSInterfaceBackObj(ReturnJOB);
                }else{
                    JOB = SM.JSInterfaceErrorBackObj("刪除失敗，沒有對應事件");
                }
            } else {
                JOB = SM.JSInterfaceErrorBackObj("刪除失敗，沒有刪除權限","-2");
            }
        } catch (Exception e) {
            Log.e("calendarDeleteEvent", "calendarDeleteEvent", e);
            SM.EXToast(R.string.CM_DetectError, "calendarDeleteEvent", e);
            JOB = SM.JSInterfaceErrorBackObj(e.getMessage(),"-3");
        }
        JSHandlerCallBack(CallBackID, JOB);
    }

    // endregion


    //開啟設定
    @JavascriptInterface
    public void openSet() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", Act.getPackageName(), null);
            intent.setData(uri);
            Act.startActivity(intent);
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "openFile", e);
        }
    }

    //背景式 => 開始掃描Beacon背景服務
    @JavascriptInterface
    public void startScanBeacon() {
        try {
            Act.StartSBeaconService();
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "startScanBeacon", e);
        }
    }

    //背景式 => 停止掃描Beacon
    @JavascriptInterface
    public void stopScanBeacon() {
        try {
            Act.StopBeaconService();
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "stopScanBeacon", e);
        }
    }

    @JavascriptInterface
    public void fetchBeaconTotal(String CallBackID) {
        try {
            //Check permissions
            if (!hasPermissions(Act, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
                fetchBeaconTotalHandler(CallBackID, "didFailWithError", null, "需要定位權限");//Location Error
                //申請定位
                SweetAlertDialog SAD = SM.SWToastCreator(new JSONObject()
                        .put("title", "需要定位權限")
                        .put("content", "Beacon服務需要使用定位權限")
                        .put("confirmText", "去開啟")
                        .put("type", SweetAlertDialog.WARNING_TYPE));
                SAD.setConfirmClickListener(sweetAlertDialog -> {
                            Act.startActivity(new Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", Act.getPackageName(), null)));
                            SAD.cancel();
                        }
                );
                return;
            }
            //Check Permissions 12+ bluetooth permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasPermissions(Act, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)) {
                    fetchBeaconTotalHandler(CallBackID, "rangingBeaconsDidFailFor", null, "需要藍芽權限");//Bluetooth Error
                    //申請藍芽
                    SweetAlertDialog SAD = SM.SWToastCreator(new JSONObject()
                            .put("title", "需要藍芽權限")
                            .put("content", "Beacon服務需要使用藍芽權限")
                            .put("confirmText", "去開啟")
                            .put("type", SweetAlertDialog.WARNING_TYPE));
                    SAD.setConfirmClickListener(sweetAlertDialog -> {
                                Act.startActivity(new Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", Act.getPackageName(), null)));
                                SAD.cancel();
                            }
                    );
                    return;
                }
            }
            //Check Bluetooth Status
            boolean hasBLE = Act.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!hasBLE) {
                SM.SWToast("此裝置不支援藍芽BLE");
                fetchBeaconTotalHandler(CallBackID, "rangingBeaconsDidFailFor", null, "此裝置不支援藍芽BLE");//Bluetooth Error
                return;
            } else if (mBluetoothAdapter == null) {
                SM.SWToast("此裝置不支援藍芽");
                fetchBeaconTotalHandler(CallBackID, "rangingBeaconsDidFailFor", null, "此裝置不支援藍芽");//Bluetooth Error
                return;
            } else if (!mBluetoothAdapter.isEnabled()) {
                SM.SWToast("您尚未開啟藍芽");
                fetchBeaconTotalHandler(CallBackID, "rangingBeaconsDidFailFor", null, "尚未開啟藍芽");//Bluetooth Error
                return;
            }
            //Check Location Provider
            LocationManager LM = (LocationManager) Act.getSystemService(Context.LOCATION_SERVICE);
            boolean gps = LM.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network = LM.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gps && !network) {
                fetchBeaconTotalHandler(CallBackID, "didFailWithError", null, "未開啟定位服務");//Location Error
                //申請開啟定位服務
                SweetAlertDialog SAD = SM.SWToastCreator(new JSONObject()
                        .put("title", "您尚未開啟定位服務")
                        .put("content", "Beacon服務需要使用定位服務")
                        .put("confirmText", "去開啟")
                        .put("type", SweetAlertDialog.WARNING_TYPE));
                SAD.setConfirmClickListener(sweetAlertDialog -> {
                    Act.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    SAD.cancel();
                });
                return;
            }
            //Callback
            fetchBeaconTotalHandler(CallBackID, "rangingRemoteBeacons", Act.SBS.getBeaconJA());
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_ProcessData, "fetchBeaconTotal", e);
        }
    }

    //前景式 => 單次 掃描Beacon + 回傳
    private boolean isBeaconScanning = false;

    //這邊未更新 可能需要修正
    @JavascriptInterface
    public void fetchBeaconTotalInstant(String CallBackID) {
        //Check Bluetooth Status
        boolean hasBLE = Act.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!hasBLE || mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            fetchBeaconTotalHandler(CallBackID, "rangingBeaconsDidFailFor", null);//Bluetooth Error
            return;
        }
        //Check Permissions
        String[] BLUETOOTH_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermissions(Act, BLUETOOTH_PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, TD.RQC_Permission_Beacon_Single);
            } else {
                ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TD.RQC_Permission_Beacon_Single);
            }
            fetchBeaconTotalHandler(CallBackID, "didFailWithError", null);//Location Error
            return;
        }
        //Check Permissions 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] BLUETOOTH_PERMISSIONS_S = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
            if (!hasPermissions(Act, BLUETOOTH_PERMISSIONS_S)) {
                ActivityCompat.requestPermissions(Act, BLUETOOTH_PERMISSIONS_S, TD.RQC_Permission_Beacon);
                return;
            }
        }
        //Check Location
        LocationManager LM = (LocationManager) Act.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = LM.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = LM.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps && !network) {
            fetchBeaconTotalHandler(CallBackID, "didFailWithError", null);//Location Error
            return;
        }
        //Start Scan
        if (!isBeaconScanning) {
            isBeaconScanning = true;
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    //Auto Off
                    Handler autoOffHandler = new Handler();
                    //Init Beacon
                    String filterUUID = "288333B2-82B2-445F-A1D0-3B3BEFA92CF5";
                    String iBeacon_Format = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
                    Region region = new Region(filterUUID, null, null, null);
                    BeaconManager beaconManager = BeaconManager.getInstanceForApplication(Act);
                    //Start Scan
                    beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(iBeacon_Format));
                    beaconManager.setBackgroundScanPeriod(3000L);//背景時，掃描一次的時間，時間越長取樣率越平均
                    beaconManager.setBackgroundBetweenScanPeriod(60000L);//背景時，掃描與掃描間的間隔時間
                    beaconManager.setForegroundScanPeriod(3000L);//掃描一次的時間，時間越長取樣率越平均
                    beaconManager.setForegroundBetweenScanPeriod(5000L);//掃描與掃描間的間隔時間
                    beaconManager.getRegionViewModel(region).getRangedBeacons().observe((LifecycleOwner) Act, beacons -> {
                        try {
                            JSONArray beaconJA = new JSONArray();
                            for (Beacon beacon : beacons) {
                                JSONObject beaconJOB = new JSONObject();
                                beaconJOB.put("bluetoothName", beacon.getBluetoothName());
                                beaconJOB.put("bluetoothAddress", beacon.getBluetoothAddress());
                                beaconJOB.put("uuid", beacon.getId1());
                                beaconJOB.put("major", beacon.getId2());
                                beaconJOB.put("minor", beacon.getId3());
                                beaconJOB.put("rssi", beacon.getRssi());
                                beaconJOB.put("distance", beacon.getDistance());//meter
                                beaconJA.put(beaconJOB);
                            }

                            if (beaconJA.length() > 0) {
                                fetchBeaconTotalHandler(CallBackID, "rangingRemoteBeacons", beaconJA);//Callback
                                //Stop scan
                                autoOffHandler.removeCallbacksAndMessages(null);
                                beaconManager.stopRangingBeacons(region);
                                beaconManager.getRegionViewModel(region).getRangedBeacons().removeObservers((LifecycleOwner) Act);
                                isBeaconScanning = false;
                            }
                        } catch (Exception e) {
                            isBeaconScanning = false;
                            SM.EXToast(R.string.CM_DetectError, "fetchBeaconTotal onChanged", e);
                        }
                    });
                    beaconManager.startRangingBeacons(region);
                    //timeout auto off
                    Runnable autoOffRunnable = () -> {
                        //Stop scan
                        try {
                            fetchBeaconTotalHandler(CallBackID, "timeout", null);//Error timeout
                            beaconManager.stopRangingBeacons(region);
                            beaconManager.removeAllRangeNotifiers();
                            beaconManager.getRegionViewModel(region).getRangedBeacons().removeObservers((LifecycleOwner) Act);
                            isBeaconScanning = false;
                        } catch (Exception e) {
                            isBeaconScanning = false;
                            SM.EXToast(R.string.CM_DetectError, "fetchBeaconTotal autoOffRunnable", e);
                        }
                    };
                    autoOffHandler.postDelayed(autoOffRunnable, 20000);
                } catch (Exception e) {
                    isBeaconScanning = false;
                    SM.EXToast(R.string.CM_DetectError, "fetchBeaconTotal", e);
                }
            });
        }
    }

    private void fetchBeaconTotalHandler(String CallBackID, String state, JSONArray total) {
        fetchBeaconTotalHandler(CallBackID, state, total, "");
    }

    private void fetchBeaconTotalHandler(String CallBackID, String state, JSONArray total, String msg) {
        JSONObject JOB = new JSONObject();
        SM.JOBValueAdder(JOB, "state", state);
        SM.JOBValueAdder(JOB, "total", total);
        SM.JOBValueAdder(JOB, "msg", msg);
        JSHandlerCallBack(CallBackID, JOB);
    }

    //詢問麥克風權限
    private String CallBack_MIC;

    @JavascriptInterface
    public void microphonePermissions(String CallBackID) {
        try {
            CallBack_MIC = CallBackID;
            if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                JSONObject ReturnJOB = new JSONObject();
                ReturnJOB.put("res_code", "1");
                JSHandlerCallBack(CallBack_MIC, ReturnJOB);
            } else {//請求權限
                ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.RECORD_AUDIO}, TD.RQC_Permission_Mic);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "microphonePermissions", e);
        }
    }

    //前往網址並設定主頁===============================================================================
    @JavascriptInterface
    public void goMainUrl(String Url) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                MainURL = Url;
                WBV.loadUrl(Url);
                firstPageHandler();
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "goMainUrl", e);
            }
        });
    }

    private void firstPageHandler() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Object TagOBJ = WBV.getTag(R.id.tag_wbv_load);
                if (TagOBJ != null) {
                    String Tag = (String) TagOBJ;
                    if (Tag.equals("Finish")) {//網頁加載完成
                        Log.d("SinyoMuki", "Finish");
                        WBV.clearHistory();
                        Act.FirstPageUrl = WBV.getUrl();
                    } else {//網頁加載中
                        firstPageHandler();
                    }
                }
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "goMainUrl", e);
            }
        }, 1000);
    }

    //語音辨識========================================================================================
    private String CallBack_SPR;
    private boolean isRecognizing = false;

    @JavascriptInterface
    public void speechRecognition(String CallBackID) {
        if (!isRecognizing) {
            CallBack_SPR = CallBackID;
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    String language = "zh-TW";
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                    intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                    Act.startActivityForResult(intent, TD.RQC_Speech);
                    isRecognizing = true;
                } catch (Exception e) {
                    isRecognizing = false;
                    SM.EXToast(R.string.CM_DetectError, "speechRecognition", e);
                    JSHandlerCallBackF("onSpeechRecognitionClose");//回報語音識別結束
                }
            });
        }
    }

    private void speechRecognitionHandler(Intent data) {
        try {
            if (data != null) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && results.size() > 0) {
                    JSONObject ReturnJOB = new JSONObject();
                    ReturnJOB.put("speechRecognizerStr", results.get(0));
                    JSHandlerCallBack(CallBack_SPR, ReturnJOB);
                }
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "speechRecognitionHandler", e);
        } finally {
            isRecognizing = false;
            JSHandlerCallBackF("onSpeechRecognitionClose");//回報語音識別結束
        }
    }



    //onRequestPermissionsResult
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        try {
            if (requestCode == TD.RQC_Permission_LocService) {
                Log.d("requestCode", "onRequestPermissionsResult: "+grantResults);
//                ActivityWeb.CheckAllGrant(grantResults);
//                if (CallBackID_RGP != null && CallBackID_RGP.length() > 0) {
//                    String resCode, resContent;
//                    if (ActivityWeb.CheckAllGrant(grantResults)) {
//                        resCode = "1";
//                        resContent = "成功取得GPS權限";
//                    } else {
//                        resCode = "-1";
//                        resContent = "無法取得GPS權限";
//                    }
//                    JSONObject ReturnJOB = new JSONObject();
//                    ReturnJOB.put("res_code", resCode);
//                    ReturnJOB.put("res_content", resContent);
//                    JSHandlerCallBack(CallBackID_RGP, ReturnJOB);
//                    CallBackID_RGP = "";
//                }
            } else if (requestCode == TD.RQC_Permission_Calendar) {
                if (CallBack_CLP != null && CallBack_CLP.length() > 0) {
                    String resCode, resContent;
                    JSONObject JOB = new JSONObject();
                    JSONObject ReturnJOB = new JSONObject();
                    if (ActivityWeb.CheckAllGrant(grantResults)) {
                        JOB = SM.JSInterfaceBackObj(ReturnJOB);
                    } else {
                        JOB = SM.JSInterfaceErrorBackObj("無日歷訪問權限");
                    }
                    JSHandlerCallBack(CallBack_CLP, JOB);
                    CallBack_CLP = "";
                }
            } else if (requestCode == TD.RQC_Permission_Mic) {
                if (CallBack_MIC != null && CallBack_MIC.length() > 0) {
                    String resCode, resContent;
                    if (ActivityWeb.CheckAllGrant(grantResults)) {
                        resCode = "1";
                        resContent = "成功取得錄音權限";
                    } else {
                        resCode = "0";
                        resContent = "無法取得錄音權限";
                    }
                    JSONObject ReturnJOB = new JSONObject();
                    ReturnJOB.put("res_code", resCode);
                    ReturnJOB.put("res_content", resContent);
                    JSHandlerCallBack(CallBack_MIC, ReturnJOB);
                    CallBack_MIC = "";
                }
            }
        } catch (Exception e) {
            SM.EXToast(R.string.SC_Error_Process, "onRequestPermissionsResult", e);
        }
    }

    //SPhoto========================================================================================
    private void ShowPicOnResult(int resultCode, Intent data) {
        try {
            if (resultCode == RESULT_OK) {
                String CallBackID = data.getStringExtra("CallBackID");
                String ReturnType = data.getStringExtra("ReturnType");
                if (CallBackID != null && CallBackID.length() > 0) {
                    JSONArray ResultJA = SPhoto.GetLastSPhotoJA(SM, ReturnType);
                    if (ResultJA.length() > 0) {
                        JSONObject PicInfoJOB = new JSONObject();
                        PicInfoJOB.put("ReturnType", ReturnType);
                        PicInfoJOB.put("ResultJA", ResultJA);
                        //PicInfoJOB.put("Orientation", SM.GetPicOrientation(PicPath));
                        JSHandlerCallBack(CallBackID, PicInfoJOB);
                    } else {
                        SM.UIToast(R.string.SP_ERR_NoPhotoSelect);
                    }
                } else {
                    SM.UIToast(R.string.ERR_NoReturnID);
                }
            }


        } catch (Exception e) {
            SM.UIToast("ShowPicOnResult\n" + e.getMessage());
        }
    }

    //Functions=====================================================================================


    //關閉所有外開分頁，回到最初頁面
    @JavascriptInterface
    public void closeWindow() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                //直接恢復顯示主瀏覽頁面
                Act.RestoreWBV(0, 0);
                //主動回呼 appLoginInfoRes
                String appLoginInfo = SM.SPReadStringData("appLoginInfo");
                if (appLoginInfo.length() > 0) {//有資料才需回傳
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            appLoginInfo("appLoginInfoRes");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 1000);
                }
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "closeWindow", e);
            }
        });
    }

    //關閉所有外開分頁，回到最初頁面
    @JavascriptInterface
    public void appLoginInfo(String CallBackID) {
        try {
            //appLoginInfo
            String appLoginInfo = SM.SPReadStringData("appLoginInfo");
            JSONObject returnJOB = new JSONObject();
            returnJOB.put("res_code", appLoginInfo.length() > 0 ? "1" : "0");
            returnJOB.put("appLoginInfo", appLoginInfo);
            //Callback
            JSHandlerCallBack(CallBackID, returnJOB.toString());
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "appLoginInfo", e);
        }
    }

    //
    @JavascriptInterface
    public void viewDidAppear() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Act.RestoreWBV(0, 0);
            } catch (Exception e) {
                SM.EXToast(R.string.CM_DetectError, "closeWindow", e);
            }
        });
    }

    //刪除APP瀏覽器所有cookies
    @JavascriptInterface
    public void deleteWebCookies() {
        new Handler(Looper.getMainLooper()).post(() -> {
            // Clear all the Application Cache, Web SQL Database and the HTML5 Web Storage
            WebStorage.getInstance().deleteAllData();
            // Clear all the cookies
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            //WebView
            WebView WBV = new WebView(Act);
            WBV.clearCache(true);
            WBV.clearFormData();
            WBV.clearHistory();
            WBV.clearSslPreferences();
        });
    }

    //打開其他APP
    @JavascriptInterface
    public void openDeepLink(String deepLinkURL, String packageName) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Uri uri = Uri.parse(deepLinkURL);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage(packageName);
            try {
                Act.startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                try {
                    Act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                } catch (Exception ex) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Firebase紀錄
    @JavascriptInterface
    public void firebaseAnalyticsEvent(String mode, String name, String value) {
        try {
            JSONObject JOB = SM.JOBGetter(value);
            if (mode.equals("event")) {
                if (name.equals("page_view"))
                    Act.FA.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, SM.JSONStrGetter(JOB, "page_title"));
                else Act.FA.logEvent(name, value);
            } else if (mode.equals("set")) {
                if (name.equals("user_id")) {
                    Act.FA.setUserId(SM.JSONStrGetter(JOB, "user_id"));
                } else if (name.equals("user_properties")) {
                    Act.FA.setUserProperty(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //App主動呼叫功能=================================================================================
    //自訂義瀏覽器關閉時觸發
    public void windowOpenBack() {
        try {
            String appLoginInfo = SM.SPReadStringData("appLoginInfo");
            if (appLoginInfo.length() == 0) {//appLoginInfo 為空 才能呼叫 windowOpenBack
                JSONObject returnJOB = new JSONObject();
                returnJOB.put("res_code", "1");
                returnJOB.put("windowOpenUrl", SM.SPReadStringData("shouldStartLoadWithUrl"));
                //Callback
                JSHandlerCallBack("windowOpenBack", returnJOB.toString());
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "windowOpenBack", e);
        }
    }



    //ClipBoard=====================================================================================
    @JavascriptInterface
    public void setClipboardText(String text, String callBackId) {
        JSONObject JOB = new JSONObject();
        try {
            // 獲取剪貼板管理器
            ClipboardManager clipboardManager = (ClipboardManager) Act.getSystemService(Context.CLIPBOARD_SERVICE);
            // 創建一個ClipData對象
            ClipData clipData = ClipData.newPlainText("text", text);
            // 設置剪貼板內容
            clipboardManager.setPrimaryClip(clipData);
            SM.JSONValueAdder(JOB, "res_code", "1");
        } catch (Exception e) {
            SM.JSONValueAdder(JOB, "res_code", "0");
        }
        JSHandlerCallBack(callBackId, JOB.toString());
    }

    //onActivityResult==============================================================================
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                switch (requestCode) {
                    case TD.RQC_SPhoto:
                        ShowPicOnResult(resultCode, data);
                        break;
                    case TD.RQC_LineLogin:
                        LineOnActivityResult(null, this, data);
                        break;
                    case TD.RQC_GGSignIn:
                        GoogleLoginHandler.onActivityResult(Act, data);
                        break;
                    case TD.RQC_SelectFile:
                        uploadFileOnFileSelect(data);
                        break;
                    case TD.RQC_Calendar_GetAccount:
                        calendarGetAccountHandler(resultCode, data);
                        break;
                    case TD.RQC_Speech:
                        speechRecognitionHandler(data);
                        break;
                    case TD.RQC_Permission_Health:
                        onActivityResultHealth(resultCode);
                        break;
                    case TD.RQC_GGFit:
                        onActivityResultGGFit(resultCode);
                        break;
                    default:
                        FBLoginHandler.FBonActivityResult(requestCode, resultCode, data);//Facebook
                        break;
                }
            } catch (Exception e) {
                SM.UIToast(R.string.ERR_PrepareData);
            }
        });
    }


}
