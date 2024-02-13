package com.mukicloud.mukitest.SFunc;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.mukicloud.mukitest.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

//https://github.com/google/cameraview
//https://github.com/CameraKit/camerakit-android
class SCamera {
    private SPhoto Act;
    private SMethods SM;
    private CameraView CV;

    SCamera(SPhoto act) {
        Act = act;
        SM = Act.SM;
    }

    void onResume() {
        if(CV != null) {
            CV.start();
        }
    }

    void onPause() {
        if(CV != null) {
            CV.stop();
        }
    }

    private Dialog CDialog;
    boolean ShowCameraDialog(){
        if(!CanUseCamera())return false;
        try {
            CDialog = new Dialog(Act, R.style.Dialog_Fullscreen);
            CDialog.setContentView(R.layout.dialog_scamera);
            CDialog.setCancelable(true);
            CDialog.setCanceledOnTouchOutside(true);
            CDialog.setOnDismissListener(dialog -> onPause());
            CV = CDialog.findViewById(R.id.CV);
            CV.addCameraKitListener(new CameraKitEventListener() {
                @Override
                public void onEvent(CameraKitEvent cameraKitEvent) {
                    //DismissCameraDialog();
                }

                @Override
                public void onError(CameraKitError cameraKitError) {
                    //DismissCameraDialog();
                }

                @Override
                public void onImage(CameraKitImage cameraKitImage) {
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try {
                                SM.SProgressDialog(true,"相片處理中...",0);
                                Bitmap BMP = cameraKitImage.getBitmap();
                                String CacheFolderPath = SM.GetFolderPath("Cache", true);
                                String PicName = System.currentTimeMillis() + ".jpg";
                                String PicPath = CacheFolderPath + File.separator + PicName;
                                if (SM.SaveBitmap(PicPath, BMP)) {
                                    if(OIR != null) {
                                        final Bitmap ScaleBMP = SM.GetResizedBitmap(PicPath, 512);//PreProcessBitmap
                                        Act.runOnUiThread(() -> {
                                            OIR.onReceive(PicPath);
                                            OIR.onReceive(PicPath,ScaleBMP);
                                        });
                                    }else{
                                        SM.UIToast(R.string.SC_Error_OIR);
                                    }
                                }else{
                                    SM.UIToast(R.string.SC_Error_SaveFailed);
                                }
                            }catch (Exception e){
                                SM.UIToast(R.string.SC_Error_Process);
                            }finally {
                                DismissCameraDialog();
                                SM.SProgressDialog();
                            }
                        }
                    }.start();

                }

                @Override
                public void onVideo(CameraKitVideo cameraKitVideo) {
                    DismissCameraDialog();
                }
            });

            CDialog.findViewById(R.id.BT_SC_Facing).setOnClickListener(v -> {
                if(CV.getFacing() == CameraKit.Constants.FACING_FRONT) {
                    CV.setFacing(CameraKit.Constants.FACING_BACK);
                }else{
                    CV.setFacing(CameraKit.Constants.FACING_FRONT);
                }
            });

            CDialog.findViewById(R.id.BT_SC_Capture).setOnClickListener(v -> {
                try {
                    CV.captureImage();
                }catch(Exception e){
                    SM.UIToast(R.string.ERR_PrepareData);
                }
            });

            Button BT_SC_Flash = CDialog.findViewById(R.id.BT_SC_Flash);
            BT_SC_Flash.setOnClickListener(v -> {
                if(CV.getFlash() == CameraKit.Constants.FLASH_OFF) {
                    CV.setFlash(CameraKit.Constants.FLASH_ON);
                }else{
                    CV.setFlash(CameraKit.Constants.FLASH_OFF);
                }
                BT_SC_Flash.setSelected(CV.getFlash() == CameraKit.Constants.FLASH_ON);
            });

            onResume();//Start Camera
            CDialog.show();
        }catch(Exception e){
            SM.UIToast(R.string.ERR_UpdateView);
        }
        return true;
    }

    private void DismissCameraDialog(){
        new Handler(Looper.getMainLooper()).post(()->{
            try{
                if(CDialog != null && CDialog.isShowing()) CDialog.dismiss();
            }catch (Exception e){
                e.printStackTrace();
                SM.UIToast(R.string.ERR_UpdateView);
            }
        });

    }

    private onImageReceive OIR;
    void setOnImageReceiveListener(onImageReceive OIR){
        this.OIR = OIR;
    }
    interface onImageReceive {
        void onReceive(String PicPath);
        void onReceive(String PicPath, Bitmap BMP);
    }

    private boolean CanUseCamera(){
        PackageManager PM = Act.getPackageManager();
        return PM.hasSystemFeature(PackageManager.FEATURE_CAMERA) && ContextCompat.checkSelfPermission(Act, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
