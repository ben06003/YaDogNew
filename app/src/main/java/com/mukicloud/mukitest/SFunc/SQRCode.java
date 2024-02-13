package com.mukicloud.mukitest.SFunc;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.TD;
import com.mylhyl.zxing.scanner.ScannerOptions;
import com.mylhyl.zxing.scanner.ScannerView;
import com.mylhyl.zxing.scanner.common.Scanner;

import org.json.JSONObject;

public class SQRCode extends Activity {
    private SQRCode Act;
    private SMethods SM;
    private String QRInfo;
    private static ScanCompleteListener SSCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Act = this;
        SM = new SMethods(Act);
        //Get Intent
        Intent intent = getIntent();
        QRInfo = intent.getStringExtra("QRInfo");
        //Check Permission
        CheckPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == TD.RQC_Permission) {
            if (grantResults.length > 0) {
                boolean AllGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        AllGranted = false;
                        break;
                    }
                }
                if (AllGranted) {
                    ShowScanQR();
                } else {
                    new SMethods(this).UIToast("請允許拍照與儲存權限");
                    new Handler().postDelayed(this::finish, 1500);
                }
            }
        }
    }

    private void CheckPermission() {
        boolean AllGranted = true;
        if (ActivityCompat.checkSelfPermission(Act, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            AllGranted = false;
        }
        if (AllGranted) {
            ShowScanQR();
        } else {
            ActivityCompat.requestPermissions(Act, new String[]{
                    android.Manifest.permission.CAMERA}, TD.RQC_Permission);
        }
    }

    private Dialog ScanQRDialog;
    private ScannerView SV_QR_View;
    private Button BT_QR_FlashLight;

    public void ShowScanQR() {
        try {
            //Data
            JSONObject QRInfoJOB = SM.JOBGetter(QRInfo);
            //View
            ScanQRDialog = new Dialog(Act, R.style.Dialog_Fullscreen);
            ScanQRDialog.setContentView(R.layout.dialog_qrcode);
            //ScanQRDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            ScanQRDialog.setCancelable(true);
            ScanQRDialog.setOnDismissListener(dialog -> {
                SV_QR_View.onPause();
                ScanQRDialog = null;
                finish();
            });

            //FlashLight
            BT_QR_FlashLight = ScanQRDialog.findViewById(R.id.BT_QR_FlashLight);
            BT_QR_FlashLight.setSelected(false);
            BT_QR_FlashLight.setOnClickListener(view -> {
                boolean isSelected = BT_QR_FlashLight.isSelected();
                SV_QR_View.toggleLight(!isSelected);
                BT_QR_FlashLight.setSelected(!isSelected);
            });

            //Check package name
            String packageName = Act.getPackageName();
//            BT_QR_ManualAdd.setVisibility(View.VISIBLE);

            //ScannerOptions
            ScannerOptions.Builder SOB = new ScannerOptions.Builder();
            SOB.setTipText(SM.JSONStrGetter(QRInfoJOB, "tip", "請將條碼置於框內"));
            SOB.setFrameCornerColor(Act.getResources().getColor(R.color.colorWhite));
            SOB.setFrameStrokeColor(Act.getResources().getColor(R.color.colorWhiteE));
            String Type = SM.JSONStrGetter(QRInfoJOB, "type");
            if (Type.equals("qrcode")) SOB.setScanMode(Scanner.ScanMode.QR_CODE_MODE);//只能掃2維條碼
            else if (Type.equals("barcode")) SOB.setScanMode(Scanner.ScanMode.ONE_D_MODE);//只能掃1維條碼
            //ScannerView
            SV_QR_View = ScanQRDialog.findViewById(R.id.SV_QR_View);
            SV_QR_View.setScannerOptions(SOB.build());
            SetScanCompleteListener(SV_QR_View);
            SV_QR_View.onResume();
            //View
            try {
                TextView TV_QR_Topic = ScanQRDialog.findViewById(R.id.TV_QR_Topic);
                ImageView IMV_QR_Topic = ScanQRDialog.findViewById(R.id.IMV_QR_Topic);
                //Topic
                String Topic = SM.JSONStrGetter(QRInfoJOB, "topic");
                TV_QR_Topic.setText(Topic.length() > 0 ? Topic : "");
                TV_QR_Topic.setVisibility(Topic.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                //ImageUrl
                String ImageUrl = SM.JSONStrGetter(QRInfoJOB, "img_url");
                IMV_QR_Topic.setVisibility(ImageUrl.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                if (ImageUrl.length() > 0) {
                    SM.getBitmapFromURL(IMV_QR_Topic, ImageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ScanQRDialog.show();
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_UpdateView, "ShowScanQR", e);
        }
    }

    private void SetScanCompleteListener(ScannerView SV_QR_View) {
        SV_QR_View.setOnScannerCompletionListener((rawResult, parsedResult, barcode) -> {
            try {
                String QRResult = rawResult.getText();
                if (QRResult.length() > 0) {
                    if (ScanQRDialog != null && ScanQRDialog.isShowing()) {
                        ScanQRDialog.dismiss();
                    }
                    if (SSCL != null) {
                        SSCL.onScanComplete(QRResult);
                    }
                    finish();
                } else {
                    //"請掃描正確格式之QRCode", "", SweetAlertDialog.ERROR_TYPE, 2500
                    SweetAlertDialog SAD = SM.SWToastCreator(
                            new JSONObject()
                                    .put("title", "請掃描正確格式之QRCode")
                                    .put("type", SweetAlertDialog.ERROR_TYPE)
                                    .put("showMillis", 2500)
                    );
                    SAD.setOnDismissListener(dialogInterface -> SV_QR_View.restartPreviewAfterDelay(1000));
                }
            } catch (Exception e) {
                SM.EXToast(R.string.ERR_UpdateView, "SetScanCompleteListener", e);
            }
        });
    }

    public static void SetScanCompleteListener(ScanCompleteListener SSCLin) {
        SSCL = SSCLin;
    }

    public interface ScanCompleteListener {
        void onScanComplete(String Result);
    }
}
