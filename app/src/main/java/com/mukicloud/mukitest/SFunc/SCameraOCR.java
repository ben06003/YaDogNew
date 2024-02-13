package com.mukicloud.mukitest.SFunc;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mukicloud.mukitest.R;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.size.Size;

import java.util.ArrayList;
import java.util.List;


//https://github.com/google/cameraview
//https://github.com/CameraKit/camerakit-android
public class SCameraOCR {
    private final AppCompatActivity Act;
    private final SMethods SM;
    private CameraView CV;

    public SCameraOCR(AppCompatActivity act) {
        Act = act;
        SM = new SMethods(act);
    }

    void onResume() {
        if (CV != null) {
            CV.open();
        }
    }

    void onDestroy() {
        if (CV != null) {
            CV.close();
            CV.destroy();
        }
    }


    private Dialog CDialog;

    public boolean ShowCameraDialog() {
        if (!CanUseCamera()) return false;
        try {
            CDialog = new Dialog(Act, R.style.Dialog_Fullscreen);
            CDialog.setContentView(R.layout.dialog_scamera_ocr);
            CDialog.setCancelable(true);
            CDialog.setCanceledOnTouchOutside(true);
            CDialog.setOnDismissListener(dialog -> onDestroy());
            CV = CDialog.findViewById(R.id.CV);
            CV.setLifecycleOwner(Act);
            CV.setPreviewStreamSize(source -> {
                if (source.size() > 0) {
                    List<Size> newList = new ArrayList<>();
                    newList.add(source.get(0));
                    return newList;
                }
                return source;
            });
            CV.addCameraListener(new CameraListener() {
                public void onCameraError(@NonNull CameraException error) {
                    SM.UIToast(error.getReason());
                    DismissCameraDialog();
                }

                @Override
                public void onPictureTaken(@NonNull PictureResult result) {
                    result.toBitmap(2048, 2048, new BitmapCallback() {
                        @Override
                        public void onBitmapReady(@Nullable @org.jetbrains.annotations.Nullable Bitmap bitmap) {
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        SM.SProgressDialog(true, "相片處理中...", 0);
                                        if (bitmap != null) {
                                            Bitmap BMP = CropBMPTargetSec(bitmap, CDialog);
                                            ShowConfirmPage(CDialog, BMP);
                                        } else {
                                            SM.UIToast("無法擷取圖像");
                                        }
                                    } catch (Exception e) {
                                        SM.UIToast(R.string.SC_Error_Process);
                                    } finally {
                                        SM.SProgressDialog();
                                    }
                                }
                            }.start();
                        }
                    });
                }
            });

            CDialog.findViewById(R.id.BT_SC_Cancel).setOnClickListener(v -> {
                DismissCameraDialog();
            });

            CDialog.findViewById(R.id.BT_SC_Capture).setOnClickListener(v -> {
                try {
                    CV.takePictureSnapshot();
                } catch (Exception e) {
                    SM.UIToast(R.string.ERR_PrepareData);
                }
            });

            //自動變更視窗大小
            LinearLayout LN_SC_CameraView = CDialog.findViewById(R.id.LN_SC_CameraView);
            ImageView IMV_Confirm = CDialog.findViewById(R.id.IMV_Confirm);
            DisplayMetrics displayMetrics = Act.getResources().getDisplayMetrics();
            int targetSize = (int) (displayMetrics.widthPixels * 0.8);
            //LN_SC_CameraView
            ViewGroup.LayoutParams paramsA = LN_SC_CameraView.getLayoutParams();
            paramsA.height = targetSize;
            paramsA.width = targetSize;
            LN_SC_CameraView.setLayoutParams(paramsA);
            //IMV_Confirm
            ViewGroup.LayoutParams paramsB = IMV_Confirm.getLayoutParams();
            paramsB.height = targetSize;
            paramsB.width = targetSize;
            IMV_Confirm.setLayoutParams(paramsB);
            //Show
            CDialog.show();
            onResume();//Start Camera
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_UpdateView);
        }
        return true;
    }

    private void DismissCameraDialog() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (CDialog != null && CDialog.isShowing()) CDialog.dismiss();
                CDialog = null;
            } catch (Exception e) {
                e.printStackTrace();
                SM.UIToast(R.string.ERR_UpdateView);
            }
        });

    }

    private void ShowConfirmPage(Dialog CDialog, Bitmap BMP) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                //Pause Camera
                CV.close();
                //Show
                FrameLayout FL_Camera = CDialog.findViewById(R.id.FL_Camera);
                FrameLayout FL_Confirm = CDialog.findViewById(R.id.FL_Confirm);
                ImageView IMV_Confirm = CDialog.findViewById(R.id.IMV_Confirm);
                FL_Camera.setVisibility(View.GONE);
                IMV_Confirm.setImageBitmap(BMP);
                CDialog.findViewById(R.id.BT_SC_CF_Cancel).setOnClickListener(v -> {
                    CV.open();
                    FL_Confirm.setVisibility(View.GONE);
                    FL_Camera.setVisibility(View.VISIBLE);
                });

                CDialog.findViewById(R.id.BT_SC_CF_Confirm).setOnClickListener(v -> {
                    try {
                        //Return
                        if (OIR != null) {
                            new Handler(Looper.getMainLooper()).post(() -> OIR.onReceive("", BMP));
                        } else {
                            SM.UIToast(R.string.SC_Error_OIR);
                        }
                    } catch (Exception e) {
                        SM.EXToast(R.string.ERR_PrepareData, "BT_SC_CF_Conofirm", e);
                    } finally {
                        DismissCameraDialog();
                    }
                });
                FL_Confirm.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                SM.EXToast(R.string.ERR_UpdateView, "ShowConfirmPage", e);
            }
        });
    }

    private onImageReceive OIR;

    public void setOnImageReceiveListener(onImageReceive OIR) {
        this.OIR = OIR;
    }

    public interface onImageReceive {
        void onReceive(String PicPath);

        void onReceive(String PicPath, Bitmap BMP);
    }

    private boolean CanUseCamera() {
        PackageManager PM = Act.getPackageManager();
        return PM.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) && ContextCompat.checkSelfPermission(Act, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private Bitmap CropBMPTargetSec(Bitmap BMP, Dialog CDialog) {
        FrameLayout FL_Camera = CDialog.findViewById(R.id.FL_Camera);
        LinearLayout LN_SC_CameraView = CDialog.findViewById(R.id.LN_SC_CameraView);
        float imageWidth = BMP.getWidth();
        float imageHeight = BMP.getHeight();
        float rootWidth = FL_Camera.getWidth();
        float rootHeight = FL_Camera.getHeight();
        float ratioWidth = imageWidth / rootWidth;
        float ratioHeight = imageHeight / rootHeight;
        //解析指定區域位置
        int targetWidth = (int) (LN_SC_CameraView.getWidth() * ratioWidth);
        int targetHeight = (int) (LN_SC_CameraView.getHeight() * ratioHeight);
        int targetSize = Math.max(targetWidth, targetHeight);
        int targetLeft = (int) (getRelativeLeft(LN_SC_CameraView) * ratioWidth);
        int targetTop = (int) (getRelativeTop(LN_SC_CameraView) * ratioHeight);
        //裁切指定區域
        return Bitmap.createBitmap(BMP, targetLeft, targetTop, targetSize, targetSize);
    }

    private int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }
}
