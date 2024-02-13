package com.mukicloud.mukitest.Activity;

import static android.content.Intent.ACTION_VIEW;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static com.mukicloud.mukitest.FCMService.NOTIFICATION_CHANNEL_NAME;
import static com.mukicloud.mukitest.FCMService.PrepareNotificationChannel;
import static com.mukicloud.mukitest.TD.MainURL;

import static com.mukicloud.mukitest.TD.PKG_MKTest;


import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.cazaea.sweetalert.SweetAlertDialog;
import com.google.common.collect.ImmutableList;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mukicloud.mukitest.BuildConfig;
import com.mukicloud.mukitest.CusJSInterface;
import com.mukicloud.mukitest.JSInterface;
import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.SFunc.CommentKeyBoardFix;
import com.mukicloud.mukitest.SFunc.SAppsFlyer;
import com.mukicloud.mukitest.SFunc.SBeaconService;
import com.mukicloud.mukitest.SFunc.SBiometric;
import com.mukicloud.mukitest.SFunc.SFile;
import com.mukicloud.mukitest.SFunc.SFirebaseAnalytics;
import com.mukicloud.mukitest.SFunc.SForegroundService;
import com.mukicloud.mukitest.SFunc.SLocService;
import com.mukicloud.mukitest.SFunc.SMethods;
import com.mukicloud.mukitest.SFunc.SUpdater;
import com.mukicloud.mukitest.SFunc.SVideoPlayer;
import com.mukicloud.mukitest.TD;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ActivityWeb extends AppCompatActivity {
    public String billingBcakName = "";
    public String billingToken = "";
    public BillingClient billingClient;
    public static ActivityWeb Act;
    public SMethods SM;
    public SFile SF;
    private SUpdater SU;
    public JSInterface JS;
    public CusJSInterface CJS;
    public SBiometric SB;
    public SVideoPlayer SV;
    public SBeaconService SBS;
    public SFirebaseAnalytics FA;

    private SBeaconServiceConn SBSConn;
    private String ViewType = "Normal";
    private final SparseArray<WBVHolder> WebViewSA = new SparseArray<>();
    //Temp Value

    private Integer LoadUrlNum = 0;

    private boolean ReadIntentDelay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        new CommentKeyBoardFix(this);//矯正鍵盤問題
        Act = this;
        FA = new SFirebaseAnalytics(Act);
        SM = new SMethods(Act);
        SU = new SUpdater(Act);
        SB = new SBiometric(Act);
        SV = new SVideoPlayer(Act);
        SF = new SFile(Act);
        SF.DownloadHandler();
        //onCreate
        StartListenNetworkState();
        InitBTRequestEnable();
        ShowWelcomePage(true);
        KeyboardListener();
        FindViews();
        InitPushy();
        ClearBadge();
        GetFcmToken();
        ReadViewType();
        ReadProjectCode();
        GetFBHashCode();
        GetSHA1Key();
        // 啟動IAP
        OpenBilling();
        // appsflyer
        InitAppsFlyer();
    }

    @Override
    public void onResume() {
        super.onResume();
        StartReceiveSBeacon();
        StartReceiveFCMService();
        if (JS != null) JS.JSHandlerCallBackF("enterForeground"); //JS enterForeground 通知進入前景
        //部分App需要執行特定功能
        String AppID = getApplicationInfo().packageName;
        if (AppID.contains(PKG_MKTest)) {
            NotifyPermissionChecker();
            ForegroundServiceHandler();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StopReceiveSBeacon();
        StopReceiveFCMService();
    }

    @Override
    public void onDestroy() {
        if (JS != null) JS.onDestroy();
        if (CJS != null) CJS.onDestroy();
        StopBeaconService();
        SLocService.StopService(Act);
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getFlags() != Intent.FLAG_ACTIVITY_SINGLE_TOP) {// 非点击icon调用activity时才调用newintent事件
            setIntent(intent);
            ReadIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            JS.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_PrepareData);
        }
    }

    public WebView WBV_Main;
    private TextView TV_Title;
    private SwipeRefreshLayout SR_Main;
    private LinearLayout LN_WebMain, LN_Foot;
    private FrameLayout FL_TitleBar;

    private void FindViews() {
        FL_TitleBar = findViewById(R.id.FL_TitleBar);
        LN_WebMain = findViewById(R.id.LN_WebMain);
        TV_Title = findViewById(R.id.TV_Title);
        LN_Foot = findViewById(R.id.LN_Foot);
        WBV_Main = findViewById(R.id.WBV_Main);
        SR_Main = findViewById(R.id.SR_Main);
        SR_Main.setEnabled(false);
        SR_Main.setOnRefreshListener(() -> WBV_Main.reload());

        //Functions
        findViewById(R.id.IMV_Close).setOnClickListener(view ->
                WBV_Main.evaluateJavascript("javascript:typeof appCloseFunction === 'function'", value -> {
                    if (value.equals("true")) { //執行網頁的關閉函式
                        WBV_Main.evaluateJavascript("javascript:appCloseFunction()", null);
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (WBVBackUrl.length() > 0) {
                                WBVBackNow = true;
                                WBV_Main.loadUrl(WBVBackUrl);
                            } else {
                                //直接恢復顯示主瀏覽頁面
                                Act.RestoreWBV(0, 0);
                            }
                        });
                    }
                }));

        findViewById(R.id.IMV_Refresh).setOnClickListener(view -> {
            if (ViewType.equals("Normal")) {
                WBV_Main.reload();
            } else {
                ShowMenuDialog();
            }
        });

        findViewById(R.id.IMV_Back).setOnClickListener(view -> BackPrePage());

        findViewById(R.id.IMV_Forward).setOnClickListener(view -> {
            if (WBV_Main.canGoForward()) WBV_Main.goForward();
        });

        findViewById(R.id.IMV_CopyUrl).setOnClickListener(view -> {
            String url = WBV_Main.getUrl();
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label", url);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                ShowIsCopyDialog();
            } else {
                SM.UIToast(R.string.ERR_PrepareData);
            }
        });

        findViewById(R.id.IMV_OpenBrowser).setOnClickListener(view -> {
            String Url = WBV_Main.getUrl();
            Intent intent = new Intent(ACTION_VIEW, Uri.parse(Url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Act.startActivity(intent);
        });

        findViewById(R.id.IMV_Share).setOnClickListener(view -> {
            String Url = WBV_Main.getUrl();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, WBV_Main.getTitle());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Url);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.AWB_Title_ShareUse)));
        });
    }

    //Web===========================================================================================
    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void InitWebView(WebView WBV, boolean New) {
        String ApplicationID = getApplicationInfo().packageName;
        boolean WebViewDebug = ApplicationID.contains("dev") || ApplicationID.contains("test");
        WebView.setWebContentsDebuggingEnabled(WebViewDebug);
        WBV.setWebViewClient(new SWBVClient());
        WBV.setWebChromeClient(new SWebChromeClient());
        WBV.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> WBVDownloadHandler(url, contentDisposition, mimeType));
//        WBV.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//關閉硬件加速
        if (ViewType.equals("Normal")) WBV.setOnTouchListener(new SWebOnTouchListener());
        //新建立的WebView才需要Add
        if (New) {
            WBV.addJavascriptInterface(JS = new JSInterface(this), "Android");
//            WBV.addJavascriptInterface(CJS = new CusJSInterface(this), "CJS");
        }

        WebSettings WS = WBV.getSettings();
        WS.setSupportMultipleWindows(true);
        WS.setLoadWithOverviewMode(true);
        WS.setBuiltInZoomControls(true);
        WS.setSupportZoom(true);
        WS.setDisplayZoomControls(false);
        WS.setJavaScriptEnabled(true);
        WS.setJavaScriptCanOpenWindowsAutomatically(true);
        WS.setUseWideViewPort(true);
        WS.setGeolocationEnabled(true);
        WS.setGeolocationDatabasePath(Act.getFilesDir().getPath());
        WS.setAllowFileAccess(true);
//        WS.setAppCacheEnabled(true);
        WS.setDomStorageEnabled(true);
        WS.setDatabaseEnabled(true);
        WS.setLoadsImagesAutomatically(true);
        WS.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        WS.setCacheMode(WebSettings.LOAD_NO_CACHE); // load online by default
//        WS.setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "Cache");
        WS.setSaveFormData(false);//避免儲存敏感資料
        WS.setUserAgentString(getUseragent(WBV));///AndroidMuki
    }


    public static String getUseragent(WebView WBV) {
        String userAgent;
        String uuid = Settings.Secure.getString(Act.getContentResolver(), Settings.Secure.ANDROID_ID);
        String version = String.valueOf(BuildConfig.VERSION_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WBV.getSettings().getUserAgentString();
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        final StringBuilder sb = new StringBuilder();
        assert userAgent != null;
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        String replace = sb.toString().replace("; wv", "; xx-xx");
        Log.d("getUseragent", String.format("%s/%s AppShellVer:%s UUID/%s", replace, android.os.Build.BRAND, version, uuid));
        return String.format("%s/%s AppShellVer:%s UUID/%s", replace, android.os.Build.BRAND, version, uuid);
    }

    private void WBVDownloadHandler(String url, String contentDisposition, String mimeType) {
        try {
            //Check Permission
            boolean AllGranted = true;
            if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AllGranted = false;
            } else if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AllGranted = false;
            }
            //Start Download
            if (AllGranted) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                /* Let's have some Cookies !!!*/
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                final String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                SM.UIToast("檔案下載中");
            } else {
                SM.UIToast("請允許檔案存取權限後重試");
                ActivityCompat.requestPermissions(Act, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, TD.RQC_Permission_File);
            }
        } catch (SecurityException e) {
            SM.UIToast("請允許檔案存取權限後重試");
        }
    }

    private void ReadProjectCode() {
//        GoMainUrl("https://wsd-demo.netlify.app/app-test");
//        GoMainUrl("https://www.1029988.com");
        GoMainUrl("http://192.168.1.123");
//        AlertDialog.Builder builder = new AlertDialog.Builder(Act);
//        builder.setTitle("請選擇測試環境");
//        builder.setPositiveButton("API 測試", (dialog, which) ->  GoMainUrl("https://wsd-demo.netlify.app/app-test"));
//        builder.setNegativeButton("MegaCat測試站", (dialog, which) ->  GoMainUrl("https://www.1029988.com"));
//        builder.create().show();
    }

    private void ReadViewType() {
        //Read View Type
        Intent NFIntent = getIntent();
        if (NFIntent != null) {
            String IntentViewType = NFIntent.getStringExtra("ViewType");
            if (IntentViewType != null && IntentViewType.length() > 0) {
                ViewType = IntentViewType;
                if (ViewType.equals("Menu")) {
                    LN_Foot.setVisibility(View.GONE);
                    ((ImageView) findViewById(R.id.IMV_Refresh)).setImageResource(R.drawable.ic_more);
                }
            }
        }
    }

    private void ReadIntent() {
        if (LoadUrlNum == 0) { // 延後調用
            ReadIntentDelay = true;
        }else{
            //Read Intent & Load Url
            String TargetURL;
            Intent NFIntent = getIntent();
            if (NFIntent != null) {
                //Normal Intent
                String Action = NFIntent.getAction();
                if (Action != null) {
                    if (Action.equals(ACTION_VIEW)) {
                        Uri uri = NFIntent.getData();
                        String linkStr = uri.getQueryParameter("link");
                        Log.d("linkStr", "ReadIntent: "+linkStr);
                        if (uri != null) {
                            String dataString = NFIntent.getDataString();
                            if (linkStr != null)
                                dataString = linkStr;
                            if (dataString != null) {
                                Log.d("dataString", "ReadIntent: "+dataString);
                                if (dataString.contains("goPage")) {
                                    // 获取"goPage/"后面的值
                                    Pattern pattern = Pattern.compile("goPage/(.+)");
                                    Matcher matcher = pattern.matcher(dataString);
                                    if (matcher.find()) {
                                        String Fvalue = matcher.group(1);
                                        System.out.println(Fvalue);
                                        JSONObject JOB = new JSONObject();
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            if (WBV_Main != null)
                                                WBV_Main.evaluateJavascript("javascript:m_refresh_page_tidy('" + Fvalue + "');", value -> Log.d("m_refresh_page_tidy", value));
                                        }, 500);
                                    }
                                } else if (dataString.startsWith(SM.IDStr(R.string.schema_name) + "://")) {
                                    TargetURL = dataString.substring(SM.IDStr(R.string.schema_name).length() + 3);
                                    LoadUrlHandler(TargetURL);
                                } else if (dataString.startsWith(SM.IDStr(R.string.schema_name_test) + "://")) {
                                    TargetURL = dataString.substring(SM.IDStr(R.string.schema_name_test).length() + 3);
                                    LoadUrlHandler(TargetURL);
                                } else if (dataString.startsWith(SM.IDStr(R.string.schema_name_dev) + "://")) {
                                    TargetURL = dataString.substring(SM.IDStr(R.string.schema_name_dev).length() + 3);
                                    LoadUrlHandler(TargetURL);
                                } else if (dataString.startsWith("http://") || dataString.startsWith("https://")) {
                                    String path = dataString.substring(dataString.indexOf("/", 8));
                                    onUniversalLinkOpenHandler(path);
                                    //同時也判斷是否是Dynamic Link
                                    DynamicLinkHandler();
                                }
                            }
                        }
                    } else if (Action.equals("Notify")) {
                        String GoUrl = NFIntent.getStringExtra("GoUrl");
                        if (GoUrl != null && GoUrl.length() > 0) {
                            TargetURL = GoUrl;
                            if (TargetURL.contains("goPage")) {
                                // 获取"goPage/"后面的值
                                Pattern pattern = Pattern.compile("goPage/(.+)");
                                Matcher matcher = pattern.matcher(TargetURL);
                                if (matcher.find()) {
                                    String Fvalue = matcher.group(1);
                                    System.out.println(Fvalue);
                                    JSONObject JOB = new JSONObject();
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        if (WBV_Main != null)
                                            WBV_Main.evaluateJavascript("javascript:m_refresh_page_tidy('" + Fvalue + "');", value -> Log.d("m_refresh_page_tidy", value));
                                    }, 500);
                                }
                            }else{
                                LoadUrlHandler(TargetURL);
                            }
                        }
                    }
                }
            }
        }
    }

    public void CreateWBV(String Url) {
        CreateWBV(Url, "Default");
    }

    public void CreateWBV(String Url, String UseBrowser) {
        try {
            //Update Current
            if (WebViewSA.size() > 0) {
                WBVHolder holder = WebViewSA.get(WebViewSA.size() - 1, null);
                holder.WBV = WBV_Main;
                WebViewSA.put(WebViewSA.size() - 1, holder);
            }
            //Parse Url
            if (Url.endsWith(".pdf")) {
                SM.SWebProgress(true);
                Url = "https://drive.google.com/viewerng/viewer?embedded=true&url=" + Url;
            } else if (Url.endsWith(".xls") || Url.endsWith(".xlsx") || Url.endsWith(".doc") || Url.endsWith(".docx") || Url.endsWith(".ppt") || Url.endsWith(".pptx")) {
                SM.SWebProgress(true);
                Url = "https://view.officeapps.live.com/op/view.aspx?src=" + Url;
            }
            //Create New
            WebView WBV = new WebView(Act);
            WBV_Main = WBV;
            WBV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            InitWebView(WBV, true);
            WBVLoadUrl(WBV, Url);
            WebViewSA.put(WebViewSA.size(), new WBVHolder(WBV, UseBrowser));
            LN_WebMain.removeAllViews();
            LN_WebMain.addView(WBV);
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_PrepareData, "CreateWBV", e);
        }
    }

    public boolean RestoreWBV(int WebViewID, int BackStep) {
        WBVHolder holder = WebViewSA.get(WebViewID, null);
        if (holder != null && holder.WBV != null) {
            InitWebView(holder.WBV, false);
            LN_WebMain.setVisibility(View.INVISIBLE);
            LN_WebMain.removeAllViews();
            LN_WebMain.addView(holder.WBV);
            WBV_Main = holder.WBV;

            //Check Go Back Steps
            if (BackStep < 0) {
                SM.SWebProgress(true);
                WBV_Main.goBackOrForward(BackStep);//如果WBV頁面還需要返回 則在此返回
            } else {//Show page with animation
                LoadFinish(holder.WBV.getTitle());//Handle Browser ToolBar & Title
            }
            //Remove
            WBVHolder LastWBVHolder = WebViewSA.get(WebViewID + 1, null);
            if (LastWBVHolder != null) {
                WebViewSA.remove(WebViewID + 1);
            }
            return true;
        }
        return false;
    }

    private void LoadUrlHandler(String url) {
        if (url.length() > 0 && url.contains("http")) {//檢查是否有正確網址
            //Check is in domain
            String DomainUrl = GetDomain(MainURL);
            boolean isInDomain = url.contains(DomainUrl);
            if (isInDomain) WBV_Main.loadUrl(url);
            else Act.CreateWBV(url, "True");
        } else {
            WBV_Main.loadUrl(MainURL);
        }
    }

    private void WBVLoadUrl(WebView WBV, String url) {
        if (url.contains("goPage/")) {
            String goPage = url.substring(url.indexOf("goPage/") + 7);
            WBV.evaluateJavascript("javascript:m_refresh_page_tidy(" + goPage + ")", value -> Log.d("JSHandlerCallBack", value));
            return;
        }
        WBV.loadUrl(url);
    }

    private void LoadFinish(String Title) {
        try {
            SR_Main.setRefreshing(false);
            SM.SWebProgress();
            ShowWelcomePage(false);
            DomainHandler();
            onUniversalLinkOpenHandler(universalLink);
            TV_Title.setText(Title);
            //WBVBack Handler
            if (WBVBackNow && WBVBackUrl.length() > 0) {
                WBV_Main.clearHistory();
                WBVBackNow = false;
                WBVBackUrl = "";
            }
            //Show WebPage
            if (LN_WebMain.getVisibility() != View.VISIBLE) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    LN_WebMain.setVisibility(View.VISIBLE);
                    LN_WebMain.startAnimation(AnimationUtils.loadAnimation(Act, R.anim.fade_in));
                }, 250);
            }
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_UpdateView, "LoadFinish", e);
        }
    }

    private boolean preInDomain = true;

    private void DomainHandler() {
        new Handler(Looper.getMainLooper()).post(() -> {
            boolean isInDomain = isInDomain();
            FL_TitleBar.setVisibility(isInDomain ? View.GONE : View.VISIBLE);
            LN_Foot.setVisibility(isInDomain ? View.GONE : View.VISIBLE);
            //如果偵測到離開了外部瀏覽
            if (!preInDomain && isInDomain) JS.windowOpenBack();
            preInDomain = isInDomain;
        });
    }

    private static boolean mainIsInDomain;

    private boolean isInDomain() {
        //Get WBV Settings From SA
        WBVHolder CHolder = null;
        for (int cnt = WebViewSA.size() - 1; cnt >= 0; cnt--) {
            WBVHolder holder = WebViewSA.get(cnt, null);
            if (holder != null && holder.WBV == WBV_Main) {
                CHolder = holder;
            }
        }
        //Check Domain
        String CUrl = WBV_Main.getUrl();
        String DomainUrl = GetDomain(MainURL);
        mainIsInDomain = CUrl.contains(DomainUrl) || CUrl.contains("mobile");
        //Read Settings
        if (CHolder != null) {
            if (CHolder.UseBrowser.equals("True")) {
                mainIsInDomain = false;
            } else if (CHolder.UseBrowser.equals("False")) {
                mainIsInDomain = true;
            }
        }
        //Filter
        String ApplicationID = getApplicationInfo().packageName;
        if (CUrl.contains("://muki001.com")) {
            mainIsInDomain = true;
        } else if (CUrl.contains("paynow.com.tw")) {//Pay now 金流
            mainIsInDomain = true;
        }
        return mainIsInDomain;
    }

    public class SWBVClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView WBV, String url) {
            try {
                if (url.startsWith("mailto:")) {
                    try {
                        MailTo mt = MailTo.parse(url);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, mt.getTo());
                        intent.putExtra(Intent.EXTRA_TEXT, mt.getBody());
                        intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
                        intent.putExtra(Intent.EXTRA_CC, mt.getCc());
                        //intent.setType("message/rfc822");
                        startActivity(intent);
                    } catch (Exception e) {
                        SM.UIToast(R.string.ERR_NoMailApp);
                    }
                } else if (url.startsWith("tel:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        SM.UIToast(R.string.ERR_NoPhoneApp);
                    }
                } else if (url.endsWith(".pdf")) {
                    SM.SWebProgress(true);
                    WBV.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
                } else if (url.endsWith(".xls") || url.endsWith(".xlsx") || url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".ppt") || url.endsWith(".pptx")) {
                    SM.SWebProgress(true);
                    WBV.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url);
                } else if (url.startsWith((SM.IDStr(R.string.schema_name) + "://")) || url.startsWith((SM.IDStr(R.string.schema_name_test) + "://")) || url.startsWith((SM.IDStr(R.string.schema_name_dev) + "://"))) {
                    String GoUrl = url.substring(url.indexOf("://") + 3);
                    WBVLoadUrl(WBV, GoUrl);
                } else if (url.startsWith("intent://")) {
                    //針對Firebase處理 避免跳轉到外部瀏覽器
                    if (url.startsWith("intent://" + SM.IDStr(R.string.schema_http_a)) || url.startsWith("intent://" + SM.IDStr(R.string.schema_http_b))) {
                        int startPos = url.indexOf("link=") + 5;
                        int endPos = url.indexOf("#");
                        String targetUrl = url.substring(startPos, endPos);
                        WBVLoadUrl(WBV, targetUrl);
                        return true;
                    }
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        WBV.stopLoading();
                        PackageManager packageManager = getPackageManager();
                        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        if (info != null) {
                            startActivity(intent);
                        } else {
                            SM.UIToast("您尚未安裝此應用程式");
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            WBV.loadUrl(fallbackUrl);
                        }
                    }
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    WBVLoadUrl(WBV, url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            } catch (Exception e) {
//                SM.UIToast(R.string.ERR_PrepareData);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.setTag(R.id.tag_wbv_load, "Load");
            view.getSettings().setBlockNetworkImage(false);//開始加載圖片
//            view.getSettings().setBlockNetworkImage(true);//阻塞圖片先不加載
            //顯示Loading
            if (DialogWelcome == null || !DialogWelcome.isShowing()) {// 歡迎頁顯示期間不載入
                SM.SWebProgress(true);
            }
            //載入過久自動隱藏Loading
//            String PKGName = Act.getPackageName();
//            if (PKGName.contains(PKG_Click108) || PKGName.contains(PKG_Click108Beta)) {
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                    if (SM.isLoading()) {//如果還在Loading時
//                        LoadFinish(view.getTitle());
//                    }
//                }, 1000);
//            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("TAG", "onPageFinished: 網站載入完成");

            LoadUrlNum += 1;

            Log.d("LoadUrlNum", "onPageFinished: "+LoadUrlNum);

            view.setTag(R.id.tag_wbv_load, "Finish");
//            view.getSettings().setBlockNetworkImage(false);//開始加載圖片
            LoadFinish(view.getTitle());

            if (ReadIntentDelay) {
                ReadIntentDelay = false;
                ReadIntent();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (url.contains(".m3u8")) {
                SR_Main.setVisibility(View.GONE);
                SV.PV_Player.setVisibility(View.VISIBLE);
                SV.startVideo(url);
            }
        }

        // 旧版本，会在新版本中也可能被调用，所以加上一个判断，防止重复显示
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 在这里显示自定义错误页
                LoadFinish("Error");
                ShowNetErrorPage(errorCode, description);
            }
        }

        // 新版本，只会在Android6及以上调用
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request.isForMainFrame()) {
                LoadFinish("Error");
                ShowNetErrorPage(error.getErrorCode(), error.getDescription().toString());
            }
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            SM.SWToast(R.string.ERR_SSLTitle);
            AlertDialog.Builder builder = new AlertDialog.Builder(Act);
            builder.setTitle(R.string.ERR_SSLTitle);
            builder.setMessage(R.string.ERR_SSLContent);
            builder.setPositiveButton(R.string.CM_Continue, (dialog, which) -> handler.proceed());
            builder.setNegativeButton(R.string.CM_Cancel, (dialog, which) -> handler.cancel());
            builder.create().show();
        }

        //如果有本地快取 使用本地快取加速瀏覽
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }
    }

    private ValueCallback<Uri[]> SFilePathCallback;
    private WebChromeClient.CustomViewCallback mCallBack;

    private class SWebChromeClient extends WebChromeClient {

        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    Act);
            builder.setMessage(message)
                    .setPositiveButton(R.string.CM_Continue, (arg0, arg1) -> arg0.dismiss()).show();
            result.cancel();
            return true;
        }

        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView tempWebView = new WebView(Act);
            tempWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    openSystemBrowser(request.getUrl());
                    return true;
                }

                @Override
                public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                    SM.SWToast(R.string.ERR_SSLTitle);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Act);
                    builder.setTitle(R.string.ERR_SSLTitle);
                    builder.setMessage(R.string.ERR_SSLContent);
                    builder.setPositiveButton(R.string.CM_Continue, (dialog, which) -> sslErrorHandler.proceed());
                    builder.setNegativeButton(R.string.CM_Cancel, (dialog, which) -> sslErrorHandler.cancel());
                    builder.create().show();
//                    sslErrorHandler.proceed();
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(tempWebView);
            resultMsg.sendToTarget();
            return true;
        }

        //允許裝置定位
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (ContextCompat.checkSelfPermission(ActivityWeb.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                callback.invoke(origin, false, false);
            } else {
                callback.invoke(origin, true, true);
            }
        }

        @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String Msg = consoleMessage.message() + " -- From line "
                    + consoleMessage.lineNumber() + " of "
                    + consoleMessage.sourceId();
            Log.d("ActWeb", Msg);
            return super.onConsoleMessage(consoleMessage);
        }

        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            SFilePathCallback = filePathCallback;
            requestSelectFileLauncher.launch("*/*");
            return true;
        }


        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            SR_Main.setVisibility(View.GONE);
            SV.PV_Player.setVisibility(View.VISIBLE);
            mCallBack = callback;
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            SV.PV_Player.setVisibility(View.GONE);
            SR_Main.setVisibility(View.VISIBLE);
            if (mCallBack != null) mCallBack.onCustomViewHidden();
            super.onHideCustomView();
        }
    }

    private void openSystemBrowser(Uri uri) {
        Intent intent;
        try {
            intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    //如果選擇檔案沒回傳的話 必須返回空值 不然無法再次選擇
    private void ReturnFileNull() {
        if (SFilePathCallback != null) {
            SFilePathCallback.onReceiveValue(new Uri[]{});
            SFilePathCallback = null;
        }
    }

    private final ActivityResultLauncher<String> requestSelectFileLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), result -> {
                if (SFilePathCallback != null) {
                    Uri[] UriAry = new Uri[result.size()];
                    result.toArray(UriAry);
                    SFilePathCallback.onReceiveValue(UriAry);
                    SFilePathCallback = null;
                } else {
                    ReturnFileNull();
                }
            });

    private static final String UrlCheckerUrl = "https://geturl.muki001.com/website/geturl";

    private void MainUrlCheckerBTHandler(String ProjectCode) {
        MainUrlCheckerBTHandler(ProjectCode, false);
    }

    private void MainUrlCheckerBTHandler(String ProjectCode, boolean FormalMode) {
        String ApplicationID = getApplicationInfo().packageName;
        if (ApplicationID.contains("test")) {
            SM.UIToast(ProjectCode + "-test");
            MainUrlChecker(ProjectCode + "-test");
        } else if (ApplicationID.contains("dev")) {
            SM.UIToast(ProjectCode + "-dev");
            MainUrlChecker(ProjectCode + "-dev");
        } else {//Release
            if (FormalMode) ProjectCode += "-formal";
            TD.ShowDebugTos = false;
            MainUrlChecker(ProjectCode);
        }
    }

    public void MainUrlChecker(String ProjectCode) {
        new Thread() {
            @Override
            public void run() {

                super.run();

                try {
                    JSONObject SendJOB = new JSONObject();
                    SendJOB.put("project_code", ProjectCode);
                    JSONObject ResJOB = SM.SFetch(UrlCheckerUrl, "", new JSONObject().put("json_data", SendJOB));
                    if (SM.GetResultJOBAvailable(ResJOB)) {
                        String ResCode = SM.JSONStrGetter(ResJOB, "res_code");
                        JSONObject ResDataJOB = ResJOB.getJSONObject("res_data");
                        String ProjectUrl = SM.JSONStrGetter(ResDataJOB, "project_url");
                        if (ResCode.equals("1") && ProjectUrl.length() > 0) {
                            GoMainUrl(ProjectUrl);
                            SU.MVersionChecker(ResDataJOB);//Check Version
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void GoMainUrl(String NewMainUrl) {
        MainURL = NewMainUrl;
        new Handler(Looper.getMainLooper()).post(() -> {
            CreateWBV(MainURL);
            new Handler(Looper.getMainLooper()).postDelayed(() -> ReadIntent(), 2000);
        });
    }


    //Basic Functions===============================================================================
    private Dialog DialogWelcome;
    private long ShowWelcomeMillis;

    private void ShowWelcomePage(boolean Show) {
        if (Show) {
            String ApplicationID = getApplicationInfo().packageName;
            DialogWelcome = new Dialog(this, R.style.DialogWelcome);
            DialogWelcome.setContentView(R.layout.dialog_welcome);
            DialogWelcome.setCancelable(false);
            DialogWelcome.show();
            ShowWelcomeMillis = System.currentTimeMillis();
            new Handler().postDelayed(() -> {
                try {
                    if (DialogWelcome != null && DialogWelcome.isShowing()) {
                        DialogWelcome.dismiss();
                        DialogWelcome = null;
                    }
                } catch (Exception e) {
                    //
                }
            }, 20000);
            //
            new Handler().postDelayed(() -> {
                if (DialogWelcome != null && DialogWelcome.isShowing()) {
                    try {
                        SM.WelcomeLoading(true);
                    } catch (Exception e) {
                        //
                    }
                }
            }, 7000);
        } else {
            long GapTime = System.currentTimeMillis() - ShowWelcomeMillis;
            long DelayMillis = 0;
            if (System.currentTimeMillis() - ShowWelcomeMillis <= 2000) {
                DelayMillis = 2000 - GapTime;
            }
            if (SM.WelcomeLoadingIsShowing()) {
                SM.WelcomeLoading(false);
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (DialogWelcome != null && DialogWelcome.isShowing()) {
                        DialogWelcome.dismiss();
                        DialogWelcome = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, DelayMillis);
        }
    }

    private Dialog DialogNetError;

    private void ShowNetErrorPage(int ErrorCode, String ErrorDes) {
        try {
            //Show Dialog
            if (DialogNetError == null || !DialogNetError.isShowing()) {
                DialogNetError = new Dialog(this, android.R.style.Theme_Black_NoTitleBar);
                DialogNetError.setContentView(R.layout.dialog_net_error);
                DialogNetError.setCancelable(false);
                DialogNetError.setCanceledOnTouchOutside(false);
                TextView TV_DNR_ErrorCode = DialogNetError.findViewById(R.id.TV_DNR_ErrorCode);
                String ErrorMsg = "Error Code: " + ErrorCode + "\n" + ErrorDes;
                TV_DNR_ErrorCode.setText(ErrorMsg);
                Button BT_DNR_Retry = DialogNetError.findViewById(R.id.BT_DNR_Retry);
                BT_DNR_Retry.setOnClickListener(view -> {
                    try {
                        if (SM.isNetworkAvailable()) {
                            WBV_Main.reload();
                            if (DialogNetError != null && DialogNetError.isShowing()) {
                                DialogNetError.dismiss();
                            }
                        }
                    } catch (Exception e) {
                        SM.DebugToast("重試讀取網頁發生錯誤");
                    }
                });
                DialogNetError.show();
            }
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_UpdateView);
            SM.DebugToast("ShowNetErrorPage", e.getMessage());
        }
    }

    private void ShowIsCopyDialog() {
        //Show PreDialog
        Dialog CPDialog = new Dialog(Act, R.style.DialogFloatFast);
        CPDialog.setContentView(R.layout.dialog_copy);
        CPDialog.setCanceledOnTouchOutside(true);
        CPDialog.setCancelable(false);
        //FindViews
        LottieAnimationView LAV_Copy = CPDialog.findViewById(R.id.LAV_Copy);
        LAV_Copy.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (CPDialog.isShowing()) CPDialog.dismiss();//Dismiss Pre Dialog
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        CPDialog.show();
    }

    private void ShowMenuDialog() {
        Dialog MenuDialog = new Dialog(Act, R.style.DialogMenu);
        MenuDialog.setContentView(R.layout.dialog_menu);
        MenuDialog.setCanceledOnTouchOutside(true);
        MenuDialog.setCancelable(true);
        //FindViews
        ImageView IMV_MNU_Back = MenuDialog.findViewById(R.id.IMV_MNU_Back);
        ImageView IMV_MNU_Refresh = MenuDialog.findViewById(R.id.IMV_MNU_Refresh);
        ImageView IMV_MNU_Forward = MenuDialog.findViewById(R.id.IMV_MNU_Forward);
        TextView TV_MNU_OpenBrowser = MenuDialog.findViewById(R.id.TV_MNU_OpenBrowser);
        TextView TV_MNU_CopyUrl = MenuDialog.findViewById(R.id.TV_MNU_CopyUrl);
        TextView TV_MNU_ShareBy = MenuDialog.findViewById(R.id.TV_MNU_ShareBy);

        IMV_MNU_Back.setOnClickListener(IMV -> {
            MenuDialog.dismiss();
            BackPrePage();
        });
        IMV_MNU_Forward.setOnClickListener(IMV -> {
            MenuDialog.dismiss();
            if (WBV_Main.canGoForward()) WBV_Main.goForward();
        });
        IMV_MNU_Refresh.setOnClickListener(IMV -> {
            MenuDialog.dismiss();
            WBV_Main.reload();
        });
        TV_MNU_OpenBrowser.setOnClickListener(TV -> {
            MenuDialog.dismiss();
            String Url = WBV_Main.getUrl();
            Intent intent = new Intent(ACTION_VIEW, Uri.parse(Url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Act.startActivity(intent);
        });
        TV_MNU_CopyUrl.setOnClickListener(TV -> {
            MenuDialog.dismiss();
            String url = WBV_Main.getUrl();
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label", url);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                ShowIsCopyDialog();
            } else {
                SM.UIToast(R.string.ERR_PrepareData);
            }
        });
        TV_MNU_ShareBy.setOnClickListener(TV -> {
            MenuDialog.dismiss();
            String Url = WBV_Main.getUrl();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, WBV_Main.getTitle());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Url);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.AWB_Title_ShareUse)));
        });
        MenuDialog.setOnShowListener(dialog -> {
            Window window = MenuDialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.END);
                WindowManager.LayoutParams params = window.getAttributes();
                params.x = SM.DpToPx(10);
                params.y = SM.DpToPx(60);
                window.setAttributes(params);
            }
        });
        MenuDialog.show();
    }

    //WebView=======================================================================================
    public String FirstPageUrl = "";//偵測返回離開App的頁面
    //WBVBackUrl
    private boolean WBVBackNow = false;
    private String WBVBackUrl = "";//按下返回鍵要前往的頁面

    private void BackPrePage() {
        String CUrl = WBV_Main.getUrl();
        if (CUrl.equals(FirstPageUrl)) {
            LeaveProcedure();
        } else if (WBV_Main.canGoBack()) {
            WBV_Main.goBack();
        } else if (WebViewSA.size() >= 2) {//Back Pre WebView
            if (!RestoreWBV(WebViewSA.size() - 2, 0)) LeaveProcedure();//如果不能返回 則離開
        } else {
            LeaveProcedure();
        }
    }

    public void BackPrePage(String TUrl) {
        int TKeyPos = -1, BackStep = 0;
        for (int cnt = WebViewSA.size() - 1; cnt >= 0; cnt--) {
            WBVHolder holder = WebViewSA.get(WebViewSA.keyAt(cnt), null);
            if (holder != null && holder.WBV != null) {
                WebBackForwardList WBL = holder.WBV.copyBackForwardList();
                if (WBL.getSize() > 0) {
                    for (int CntHis = WBL.getSize() - 1; CntHis >= 0; CntHis--) {
                        String HisUrl = WBL.getItemAtIndex(CntHis).getUrl();
                        HisUrl = RemoveLastSlash(HisUrl);
                        if (HisUrl.equals(TUrl)) {
                            TKeyPos = cnt;
                            BackStep = -(WBL.getSize() - CntHis - 1);
                            break;
                        }
                    }
                }
            }
        }
        if (TKeyPos >= 0) {
            //Remove the url newer than this
            for (int cnt = TKeyPos + 1; cnt < WebViewSA.size(); cnt++) {
                WebViewSA.removeAt(cnt);
            }
            RestoreWBV(TKeyPos, BackStep);//恢復到指定的WBV
        } else {
            WebViewSA.clear();
            CreateWBV(TUrl);
        }
    }

    private String RemoveLastSlash(String Url) {
        if (Url.charAt(Url.length() - 1) == '/') {//Remove slash
            Url = Url.substring(0, Url.length() - 1);
        }
        return Url;
    }

    private class SWebOnTouchListener implements View.OnTouchListener {
        private int PrePos;
        private final int ScrollUpPx = SM.DpToPx(150);

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!mainIsInDomain) {//外網才啟用
                    int Pos = WBV_Main.getScrollY();
                    if (Pos + ScrollUpPx < PrePos) {
                        LN_Foot.setVisibility(View.VISIBLE);
                        PrePos = Pos;
                    } else if (Pos > PrePos) {
                        LN_Foot.setVisibility(View.GONE);
                        PrePos = Pos;
                    } else if (Pos == 0) {
                        LN_Foot.setVisibility(View.VISIBLE);
                        PrePos = Pos;
                    }
                }
            }
            return false;
        }
    }

    static class WBVHolder {
        WebView WBV;
        String UseBrowser = "Default";

        WBVHolder(WebView WBV, String UseBrowser) {
            this.WBV = WBV;
            if (UseBrowser.length() > 0) this.UseBrowser = UseBrowser;
        }
    }

    //SBeaconService================================================================================
    public ActivityResultLauncher<Intent> ARL_OpenBT;

    private void InitBTRequestEnable() {
        ARL_OpenBT = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        StartSBeaconService();
                    }
                });
    }

    public void StartSBeaconService() {
        StartSBeaconService(false);
    }

    public void StartSBeaconService(boolean isConfirmed) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //檢查可否調用藍芽
            BluetoothManager BM = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (BM == null) {
                SM.SWToast(R.string.ERR_NotSupportBLE, SweetAlertDialog.ERROR_TYPE);
                return;
            }
            //檢查可否調用藍芽
            BluetoothAdapter BLEAdapter = BM.getAdapter();
            if (BLEAdapter == null) {
                SM.SWToast(R.string.ERR_NotSupportBLE, SweetAlertDialog.ERROR_TYPE);
                return;
            }
            //檢查定位權限
            String[] BLUETOOTH_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            if (!hasPermissions(Act, BLUETOOTH_PERMISSIONS)) {
                if (isConfirmed)
                    ActivityCompat.requestPermissions(Act, BLUETOOTH_PERMISSIONS, TD.RQC_Permission_Beacon);
                else ShowUseLocationDialog();
                return;
            }
            //檢查 Android 12+ 藍芽權限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                String[] BLUETOOTH_PERMISSIONS_S = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
                if (!hasPermissions(Act, BLUETOOTH_PERMISSIONS_S)) {
                    ActivityCompat.requestPermissions(Act, BLUETOOTH_PERMISSIONS_S, TD.RQC_Permission_Beacon);
                    return;
                }
            }
            //檢查藍芽是否開啟
            if (!BLEAdapter.isEnabled()) {
                ARL_OpenBT.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                return;
            }
            //啟用藍芽服務
            SBSConn = new SBeaconServiceConn();
            SBeaconService.startService(Act);//The Service will not be destroyed by unbind if call this
            Intent intent = new Intent(this, SBeaconService.class);
            bindService(intent, SBSConn, BIND_AUTO_CREATE);
        } else {
            SM.SWToast(R.string.ERR_NotSupportBLE, SweetAlertDialog.ERROR_TYPE);
        }
    }

    private class SBeaconServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder mBinder) {
            SBeaconService.SBinder SVCBinder = (SBeaconService.SBinder) mBinder;
            SBS = SVCBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            SBS = null;
        }
    }

    public void StopBeaconService() {
        if (SBSConn != null) {
            unbindService(SBSConn);
            stopService(new Intent(this, SBeaconService.class));
            SBSConn = null;
        }
    }

    private void ShowUseLocationDialog() {
        try {
            String Content = "為了提供您最佳的服務，本APP會使用您的位置資料以計算出您和校園景點之間的距離、以及iBeacon相關服務應用，" +
                    "在應用程式關閉或未使用的狀態下也會持續收集，" +
                    "若您不願意接受分享位置資料，上述功能將無法正常使用。";
            SweetAlertDialog SAD_Permission = SM.SWToastCreator(
                    new JSONObject()
                            .put("title", "位置資料之使用")
                            .put("content", Content)
                            .put("type", SweetAlertDialog.NORMAL_TYPE)
                            .put("cancelButton", true)
                            .put("confirmText", "確認")
                            .put("cancelText", "離開App")
                            .put("showMillis", 0)
            );
            SAD_Permission.setConfirmClickListener(sweetAlertDialog -> {
                SAD_Permission.dismissWithAnimation();
                StartSBeaconService(true);
            });
            SAD_Permission.setCancelClickListener(sweetAlertDialog -> Act.finish());
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_ProcessData, "ShowGoBeaconPermission", e);
        }
    }

    //onRequestPermissionsResult====================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (JS != null) JS.onRequestPermissionsResult(requestCode, grantResults);
        if (requestCode == TD.RQC_Permission_Camera) {
            if (!CheckAllGrant(grantResults)) {
                new Handler(Looper.getMainLooper()).post(this::ShowGoChangePermission);
            }
        } else if (requestCode == TD.RQC_Permission_Beacon) {
            if (permissions.length > 0) {
                if (CheckAllGrant(grantResults)) StartSBeaconService();
                else ShowGoBeaconPermission();
            }
        }
    }

    public static boolean CheckAllGrant(int[] grantResults) {
        boolean AllGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                AllGranted = false;
                break;
            }
        }
        return AllGranted;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void ShowGoChangePermission() {
        SweetAlertDialog SAD = new SweetAlertDialog(Act, SweetAlertDialog.WARNING_TYPE);
        SAD.setTitleText("需要獲取相關權限");
        SAD.showContentText(true);
        SAD.setContentText("請允許相機、儲存權限使用此功能");
        SAD.setCancelable(true);
        SAD.setCanceledOnTouchOutside(true);
        SAD.showCancelButton(true);
        SAD.setConfirmText("設定");
        SAD.setCancelText("取消");
        SAD.setConfirmClickListener(BT -> {
            Uri uri = Uri.fromParts("package", Act.getPackageName(), null);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(uri);
            Act.startActivity(intent);
        });
        SAD.show();
    }

    private void ShowGoBeaconPermission() {
        try {
            SweetAlertDialog SAD_Permission = SM.SWToastCreator(
                    new JSONObject()
                            .put("title", "請先允許必要權限")
                            .put("content", "Beacon服務需要使用藍芽與定位服務")
                            .put("type", SweetAlertDialog.WARNING_TYPE)
                            .put("cancelButton", true)
                            .put("confirmText", "去設定")
                            .put("cancelText", "離開App")
                            .put("showMillis", 0)
            );
            SAD_Permission.setConfirmClickListener(sweetAlertDialog -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                SAD_Permission.dismissWithAnimation();
            });
            SAD_Permission.setCancelClickListener(sweetAlertDialog -> Act.finish());
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_ProcessData, "ShowGoBeaconPermission", e);
        }
    }

    //IntentJOB=====================================================================================
    /*
    public JSONObject IntentJOB;

    public JSONObject GetIntentJOB() {
        if (IntentJOB == null) IntentJOB = new JSONObject();
        return IntentJOB;
    }
    */
    //OnKey=========================================================================================
    @SuppressLint("WebViewApiAvailability")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (WBVBackUrl.length() > 0) {
                    WBVBackNow = true;
                    WBV_Main.loadUrl(WBVBackUrl);
                } else if (!JS.CallBack()) {
                    BackPrePage();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Objects.requireNonNull(WBV_Main.getWebChromeClient()).onHideCustomView();
                    }
                }
                return true;
            }
        } catch (Exception e) {
            Log.e("onKeyDown", Objects.requireNonNull(e.getMessage()));
        }
        return super.onKeyDown(keyCode, event);
    }

    private long PreCallBackMillis;

    private void LeaveProcedure() {
        if (System.currentTimeMillis() - PreCallBackMillis < 1500) {
            StopForegroundServiceService();//關閉前景服務
            finish();
        } else {
            SM.UIToast("再按一下離開");
            PreCallBackMillis = System.currentTimeMillis();
        }
    }

    //Other=========================================================================================
    private void GetFcmToken() {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            SM.SPSaveStringData("token", token);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GetDomain(String Url) {
        int StartIndex = Url.indexOf("://") + 3;
        int EndIndex = Url.indexOf("/", StartIndex);
        if (StartIndex != 2 && EndIndex != -1) {
            return Url.substring(StartIndex, EndIndex);
        } else {
            return Url.substring(StartIndex);
        }
    }

    private void ClearBadge() {
        ShortcutBadger.removeCount(Act); //for 1.1.4+
        SM.SPSaveStringData("BadgeNum", "0");
    }

    //Receiver======================================================================================
    public JSBroadcastReceiver JBR = new JSBroadcastReceiver();

    public class JSBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String Func = intent.getStringExtra("Func");
                ArrayList<String> ValueAL = intent.getStringArrayListExtra("ValueAL");
                if (Func != null && ValueAL != null) {
                    switch (Func) {
                        case "openWindow":
                            String GoToURL = GetALValue(ValueAL, 0);
                            String UseBrowser = GetALValue(ValueAL, 1);
                            new Handler(Looper.getMainLooper()).post(() -> Act.CreateWBV(GoToURL, UseBrowser));
                            break;
                        case "goTestSite":
                            String ProjectID = GetALValue(ValueAL, 0);
                            MainUrlChecker(ProjectID);
                            break;
                        case "goBackUrl":
                            String BackUrl = GetALValue(ValueAL, 0);
                            Act.BackPrePage(BackUrl);
                            break;
                        case "setBackUrl":
                            WBVBackUrl = GetALValue(ValueAL, 0);
                            break;
                        case "setFirstPage":
                            FirstPageUrl = GetALValue(ValueAL, 0);
                            break;
                    }
                }
            } catch (Exception e) {
                SM.EXToast(R.string.ERR_ProcessData, "JSBroadcastReceiver", e);
            }
        }

        private String GetALValue(ArrayList<String> ValueAL, int Pos) {
            String Value = "";
            if (ValueAL.size() > Pos) Value = ValueAL.get(Pos);
            if (Value == null) Value = "";
            return Value;
        }

    }

    //Pushy============================================================================================
    private void InitPushy() {
        String ApplicationID = getApplicationInfo().packageName;
    }

    //Facebook======================================================================================
    //http://oilcut123.pixnet.net/blog/post/360255329-%5Bcode%5D-fb-hash-key-%E5%8F%96%E5%BE%97%E6%96%B9%E6%B3%95-%28%E4%B8%8D%E9%9C%80%E5%AE%89%E8%A3%9Dopenssl%29-key-ha
    @SuppressLint("PackageManagerGetSignatures")
    private void GetFBHashCode() {
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String KeyResult = new String(Base64.encode(md.digest(), 0));//String something = new String(Base64.encodeBytes(md.digest()));
                Log.i("FBHash", KeyResult);//FB hash key
                //Toast.makeText(this,"My FB Key is \n"+ KeyResult , Toast.LENGTH_LONG ).show();
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    // region AppsFlyer
    private void InitAppsFlyer() {
        Log.d("AppsFlyer", "InitAppsFlyer");
        AppsFlyerLib.getInstance().start(getApplicationContext(), "PjV6pjhqzpQPUpAEU9q76S", new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d("AppsFlyer", "Launch sent successfully, got 200 response code from server");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("AppsFlyer", "Launch failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        });
    }
    public void AppsFlyerLogEvent(String eventName, JSONObject JOB) {
        Map<String, Object> eventValues = new HashMap<String, Object>();
        switch (eventName) {
            case "registerSubmit":
                eventValues.put("method", SM.JSONStrGetter(JOB, "method"));
                break;
            case "register":
                eventValues.put("method", SM.JSONStrGetter(JOB, "method"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("mobileNum", SM.JSONStrGetter(JOB, "mobileNum"));
                break;
            case "depositSubmit":
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("revenue", SM.JSONStrGetter(JOB, "revenue"));
                eventValues.put("value", SM.JSONStrGetter(JOB, "value"));
                eventValues.put("af_revenue", SM.JSONStrGetter(JOB, "af_revenue"));
                break;
            case "firstDeposit":
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("revenue", SM.JSONStrGetter(JOB, "revenue"));
                eventValues.put("value", SM.JSONStrGetter(JOB, "value"));
                eventValues.put("af_revenue", SM.JSONStrGetter(JOB, "af_revenue"));
                break;
            case "withdraw":
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("amount", SM.JSONStrGetter(JOB, "amount"));
                eventValues.put("value", SM.JSONStrGetter(JOB, "value"));
                eventValues.put("af_revenue", SM.JSONStrGetter(JOB, "af_revenue"));
                break;
            case "firstDepositArrival":
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("revenue", SM.JSONStrGetter(JOB, "revenue"));
                eventValues.put("value", SM.JSONStrGetter(JOB, "value"));
                eventValues.put("af_revenue", SM.JSONStrGetter(JOB, "af_revenue"));
                break;
            case "deposit":
                eventValues.put("customerName", SM.JSONStrGetter(JOB, "customerName"));
                eventValues.put("customerId", SM.JSONStrGetter(JOB, "customerId"));
                eventValues.put("revenue", SM.JSONStrGetter(JOB, "revenue"));
                eventValues.put("value", SM.JSONStrGetter(JOB, "value"));
                eventValues.put("af_revenue", SM.JSONStrGetter(JOB, "af_revenue"));
                break;
            default:
                eventValues.put("value", SM.JSONStrGetter(JOB, "eventValue"));
                break;

        }
        AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
                eventName , eventValues, new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("AppsFlyer", "Launch sent successfully, got 200 response code from server name:"+eventName);
                    }

                    @Override
                    public void onError(int i, @NonNull String s) {
                        Log.d("AppsFlyer", "Launch failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);
                    }
                });
    }
    // endregion

    // region Billing
    private String[] IAPProudctIDList = {"9.9cat","9.9cattest","9.9cattest1"};
    public List<ProductDetails> IAPProudctMap = new ArrayList<>();

    private void OpenBilling(){
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()  // Optional: Handle pending purchases
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
                        Log.d("Billing", "onPurchasesUpdated");
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && purchases != null) {
                            for (Purchase purchase : purchases) {
                                handlePurchase(purchase);
                            }
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                            // Handle an error caused by a user cancelling the purchase flow.
                            Log.d("Billing", "onPurchasesUpdated:取消購買流程");
                        } else {
                            Log.d("Billing", "onPurchasesUpdated:購買流程ERROR COED:"+billingResult.getResponseCode());
                        }
//                        Log.d("Billing", "onPurchasesUpdated > billingResult: "+billingResult+",list:"+list);
                    }
                })
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d("Billing", "onBillingSetupFinished"+billingResult.getResponseCode());
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    setBillingProduct();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.d("Billing", "onBillingServiceDisconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }
    private void setBillingProduct(){
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for (String stringValue : IAPProudctIDList) {
            Log.d("Billing", "加入商品ID: "+stringValue);
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(stringValue)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
            );
        }
//        productList.add(
//                QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId("9.9cattests")
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build()
//        );

        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {
                        Log.d("Billing", "billingClient isR: "+billingClient.isReady());
                        Log.d("Billing", "onProductDetailsResponse:"+productDetailsList);
                        if (!productDetailsList.isEmpty()) {
                            IAPProudctMap = productDetailsList;
                        }
                    }
                }
        );
    }
    private void LaunchPurchaseFlow (ProductDetails productDetails) {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        productList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
        );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void  handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "onConsumeResponse:購買成功");
                    try {
                        JSONObject ResResult = new JSONObject();
                        ResResult.put("orderId", purchase.getOrderId());//訂單ID
                        ResResult.put("packageName", purchase.getPackageName());//訂單ID
                        ResResult.put("productId", purchase.getProducts().get(0));//產品ID
                        ResResult.put("purchaseTime", purchase.getPurchaseTime());//購買時間
                        ResResult.put("purchaseState", purchase.getPurchaseState());//購買狀態
                        ResResult.put("purchaseToken", purchase.getPurchaseToken());//訂單token
                        ResResult.put("quantity", purchase.getQuantity());//訂單購買數量
                        ResResult.put("token",billingToken);
                        String backJson  = ResResult.toString();
                        Log.d("Billing", "onConsumeResponse :"+backJson);
                        JS.JSHandlerCallBack(billingBcakName, backJson);
                    }catch (Exception e) {

                    }
                }else{
                    Log.d("Billing", "onConsumeResponse:購買失敗");
                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    public void buyProducy (String productId) {
        Log.d("Billing", "buyProducy : "+productId+"IAPProudctMap size:"+IAPProudctMap.size());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IAPProudctMap.forEach(proudct->{
                Log.d("Billing", "proudctID : "+proudct.getProductId());
                if (proudct.getProductId().equals(productId)) {
                    LaunchPurchaseFlow(proudct);
                }
            });
        }
    }


    // endregion


    /**
     * string like: SHA1, SHA256, MD5.
     */
    @SuppressLint("PackageManagerGetSignatures")
    private void GetSHA1Key() {
        try {
            final String Key = "SHA1";
            final PackageInfo info = Act.getPackageManager()
                    .getPackageInfo(Act.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance(Key);
                md.update(signature.toByteArray());

                final byte[] digest = md.digest();
                final StringBuilder toRet = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    if (i != 0) toRet.append(":");
                    int b = digest[i] & 0xff;
                    String hex = Integer.toHexString(b);
                    if (hex.length() == 1) toRet.append("0");
                    toRet.append(hex);
                }
                Log.i("SHA1Key", Key + " " + toRet);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //監聽鍵盤開啟收起=================================================================================
    private void KeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(
                Act,
                isOpen -> {
                    if (!isOpen) JS.JSHandlerCallBackF("onShoftKeyboardClose");
                });
    }

    //SBeaconService Broadcast======================================================================
    private void StartReceiveSBeacon() {
        IntentFilter filter = new IntentFilter("SBeaconService");
        SBeaconReceiver = new SBeaconBroadcastReceiver();
        registerReceiver(SBeaconReceiver, filter);
    }

    private void StopReceiveSBeacon() {
        if (SBeaconReceiver != null) unregisterReceiver(SBeaconReceiver);//移除廣播接收元件
    }

    //接收來自SBeaconService的資訊
    private SBeaconBroadcastReceiver SBeaconReceiver;

    public class SBeaconBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey("ValueJA")) {
                        String ValueJAStr = extras.getString("ValueJA");
                        Log.i("SBeacon", "Found: " + ValueJAStr);
                        JS.JSHandlerCallBackF("beaconFound", ValueJAStr);
                    }
                } catch (Exception e) {
                    SM.DebugToast("SBeaconBroadcastReceiver", e.getMessage());
                }
            });
        }
    }

    //FCMService Broadcast======================================================================
    private void StartReceiveFCMService() {
        IntentFilter filter = new IntentFilter("FCMService");
        FCMServiceReceiver = new FCMServiceBroadcastReceiver();
        registerReceiver(FCMServiceReceiver, filter);
    }

    private void StopReceiveFCMService() {
        if (FCMServiceReceiver != null) unregisterReceiver(FCMServiceReceiver);//移除廣播接收元件
    }

    //接收來自SBeaconService的資訊
    private FCMServiceBroadcastReceiver FCMServiceReceiver;

    public class FCMServiceBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey("ValueJOB")) {
                        String ValueJOBStr = extras.getString("ValueJOB");
                        JS.JSHandlerCallBack("notificationCallBack", ValueJOBStr);
                    }
                } catch (Exception e) {
                    SM.DebugToast("FCMServiceBroadcastReceiver", e.getMessage());
                }
            });
        }
    }

    //通知權限========================================================================================
    private SweetAlertDialog SAD_Notify;

    private void NotifyPermissionChecker() {
        try {
            String AskedNotifyPermission = SM.SPReadStringData("AskedNotifyPermission");
            if (AskedNotifyPermission.length() == 0 && (SAD_Notify == null || !SAD_Notify.isShowing())) {
                NotificationManagerCompat from = NotificationManagerCompat.from(this);
                boolean isOpened = from.areNotificationsEnabled();
                if (isOpened) {
                    //確認是否有開啟橫幅通知
                    NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android 8.0及以上
                        NotificationChannel channel = mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_NAME);
                        //如果 Channel尚未建立
                        if (channel == null) {
                            //建立 NotificationChannel
                            NotificationChannel notificationChannel = PrepareNotificationChannel(Act);
                            if (notificationChannel != null) {
                                mNotificationManager.createNotificationChannel(notificationChannel);
                                channel = notificationChannel;
                            }
                        }
                        //確認 NotificationChannel 權限
                        if (channel != null) {
                            if (channel.getImportance() == NotificationManager.IMPORTANCE_DEFAULT) {//未开启
                                SAD_Notify = SM.SWToastCreator(
                                        new JSONObject()
                                                .put("title", "請先開啟懸浮通知權限")
                                                .put("type", SweetAlertDialog.WARNING_TYPE)
                                                .put("cancelButton", true)
                                                .put("confirmText", "去設定")
                                                .put("showMillis", 0)
                                );
                                NotificationChannel finalChannel = channel;
                                SAD_Notify.setConfirmClickListener(sweetAlertDialog -> {
                                    SAD_Notify.dismissWithAnimation();
                                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, finalChannel.getId());
                                    startActivity(intent);
                                });
                                SAD_Notify.setCancelClickListener(sweetAlertDialog -> {
                                    //拒絕後不再詢問
                                    SM.SPSaveStringData("AskedNotifyPermission", "T");
                                    SAD_Notify.dismissWithAnimation();
                                });
                            }
                        }
                    }
                } else {
                    SAD_Notify = SM.SWToastCreator(
                            new JSONObject()
                                    .put("title", "請先開啟通知權限")
                                    .put("type", SweetAlertDialog.WARNING_TYPE)
                                    .put("cancelButton", true)
                                    .put("confirmText", "去設定")
                                    .put("showMillis", 0)
                    );
                    SAD_Notify.setConfirmClickListener(sweetAlertDialog -> {
                        SAD_Notify.dismissWithAnimation();
                        Intent intent = new Intent();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        } else {
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("app_package", getApplicationContext().getPackageName());
                            intent.putExtra("app_uid", getApplicationInfo().uid);
                        }
                        startActivity(intent);
                    });
                    SAD_Notify.setCancelClickListener(sweetAlertDialog -> {
                        //拒絕後不再詢問
                        SM.SPSaveStringData("AskedNotifyPermission", "T");
                        SAD_Notify.dismissWithAnimation();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //前景服務(防止被殺================================================================================
    private void ForegroundServiceHandler() {
        String ApplicationID = getApplicationInfo().packageName;
        if (ApplicationID.contains(PKG_MKTest)) {
            if (!isServiceRunning()) {
                RegisterTimeTickReceiver();
                IgnoreBatteryOptimization();
                SForegroundService.startService(Act);//The Service will not be destroyed by unbind if call this
            }
        }
    }

    private void StopForegroundServiceService() {
        stopService(new Intent(this, SForegroundService.class));
    }

    private void IgnoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                try {//先调用系统显示 电池优化权限
                    @SuppressLint("BatteryLife")
                    Intent intent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {//如果失败了则引导用户到电池优化界面
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        ComponentName cn = ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity");
                        intent.setComponent(cn);
                        startActivity(intent);
                    } catch (Exception ex) {//如果全部失败则说明没有电池优化功能
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void RegisterTimeTickReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(timeTickReceiver, filter);
    }

    private final BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                ForegroundServiceHandler();
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                ForegroundServiceHandler();
            }
        }
    };

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void DynamicLinkHandler() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    if (pendingDynamicLinkData != null) {
                        Uri deepLink = pendingDynamicLinkData.getLink();
                        if (deepLink != null) {
                            String targetUrl = deepLink.toString();
                            String ApplicationID = getApplicationInfo().packageName;
                            LoadUrlHandler(targetUrl);
                        }
                    }
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private String universalLink = "";

    private void onUniversalLinkOpenHandler(String url) {
        if (JS != null) {
            if (url.length() > 0) {
                JS.JSHandlerCallBack("onUniversalLinkOpen", url);
            }
        } else {
            universalLink = url;
        }
    }

    private void StartListenNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
    }

    //監聽網絡訊號是否正常 不正常時需要跳出提示
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            ShowNetworkLostDialog(false);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            ShowNetworkLostDialog(true);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
//            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

    private Dialog NLDialog;

    private void ShowNetworkLostDialog(boolean show) {
        try {
            if (show) {
                if (NLDialog != null && NLDialog.isShowing()) return;
                NLDialog = new Dialog(Act, R.style.Dialog_Fullscreen);
                NLDialog.setContentView(R.layout.dialog_net_lost);
                NLDialog.setCancelable(false);
                NLDialog.setCanceledOnTouchOutside(false);
                NLDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//背景透明
                NLDialog.show();
            } else {
                if (NLDialog != null && NLDialog.isShowing()) {
                    NLDialog.dismiss();
                }
            }
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_UpdateView);
        }
    }

//    public void setBrightness(Integer BrightnessInt){
//        WindowManager.LayoutParams layoutParams = Act.getWindow().getAttributes();
//        float float_value = BrightnessInt.floatValue();
//        layoutParams.screenBrightness = 0.5f;
//        Act.getWindow().setAttributes(layoutParams);
//    }
    //自動全螢幕 + 半透明通知欄=========================================================================
    //https://www.cnblogs.com/dingxiansen/p/9929828.html
//    private void RequestFullScreenHandler() {
//        String ApplicationID = getApplicationInfo().packageName;
//        if (ApplicationID.contains(PKG_MKTest)) {
//            //沉浸式代码配置
//            //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
//            StatusBarUtil.setRootViewFitsSystemWindows(this, true);
//            //设置状态栏透明
//            StatusBarUtil.setTranslucentStatus(this);
//            //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
//            //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
//            if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
//                //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
//                //这样半透明+白=灰, 状态栏的文字能看得清
//                StatusBarUtil.setStatusBarColor(this, 0x55000000);
//            }
//        }
//    }

    //StatusBar 處理===================================================================================
//    private int GetStatusBarHeight() {
//        int result = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }
}
