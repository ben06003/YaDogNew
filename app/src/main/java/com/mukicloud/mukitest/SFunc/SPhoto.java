package com.mukicloud.mukitest.SFunc;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.TD;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class SPhoto extends Activity {
    private SPhoto Act;
    public SMethods SM;
    private SCamera SC;
    private JSONObject RequestJOB;
    private int MaxMultiNum = 0;
    /*
    RequestJOB 以下參數
    BrowseType
    MaxMultiNum
    Width
    Height
    ReturnType => base64 blob
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphoto_edit);
        Act = this;
        SM = new SMethods(Act);
        SC = new SCamera(Act);
        FindViews();
        AskForPermission();
    }

    private void SonCreate() {
        LoadAllShownImagesPath();
        ProcessIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SC.onResume();
    }

    @Override
    protected void onPause() {
        SC.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SM.DeleteRecursive(new File(SM.GetFolderPath("Cache", true)));
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri uri = result.getUri();
                    if (uri != null) {
                        String Path = NFileUtils.getPath(Act, uri);// 利用 Uri 顯示 ImageView 圖片
                        if (Path.length() > 0) {
                            LastPathJA = new JSONArray().put(Path);
                            Intent ResultIntent = new Intent();
                            ResultIntent.putExtra("CallBackID", SM.JSONStrGetter(RequestJOB, "CallBackID"));
                            ResultIntent.putExtra("ReturnType", SM.JSONStrGetter(RequestJOB, "ReturnType"));
                            setResult(Activity.RESULT_OK, ResultIntent);
                            finish();
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    SM.UIToast("無法匯入此照片\n" + "Cropping failed: " + result.getError());
                } else if (resultCode == RESULT_CANCELED) {
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
            }
        } catch (Exception e) {
            SM.EXToast(R.string.CM_DetectError, "onActivityResult", e);
        }
    }

    private LinearLayout LN_SP_Camera, LN_SP_Multi;
    private FrameLayout FL_SP_Header, FL_SP_Func;
    private ImageView IMV_SP_SelectedPic;
    private ScrollView SCV_SP_Main;
    private RecyclerView RV_SP_Pic;

    private void FindViews() {
        RV_SP_Pic = findViewById(R.id.RV_SP_Pic);
        LN_SP_Camera = findViewById(R.id.LN_SP_Camera);
        LN_SP_Multi = findViewById(R.id.LN_SP_Multi);
        SCV_SP_Main = findViewById(R.id.SCV_SP_Main);
        FL_SP_Header = findViewById(R.id.FL_SP_Header);
        FL_SP_Func = findViewById(R.id.FL_SP_Func);
        IMV_SP_SelectedPic = findViewById(R.id.IMV_SP_SelectedPic);
        //Set IMV to Square
        int TargetHeight = SM.GetScreenSize()[0];
        if (TargetHeight > 0) {
            IMV_SP_SelectedPic.getLayoutParams().height = TargetHeight;
        }

        /*
        LN_SP_Crop.setOnClickListener(v -> {
            try {
                boolean MultiSelectMode = SPAdapter.isMultiSelectMode();
                if(!MultiSelectMode) {
                    boolean isSelected = LN_SP_Crop.isSelected();
                    String PicPath = (String) IMV_SP_SelectedPic.getTag();
                    if (PicPath != null && PicPath.length() > 0) {
                        if (isSelected) {
                            IMV_SP_SelectedPic.setImageBitmap(SM.GetResizedBitmap(PicPath, 512));
                        } else {
                            IMV_SP_SelectedPic.setImageBitmap(SM.CropToSquare(SM.GetResizedBitmap(PicPath, 768)));
                        }
                        //IMV_SP_SelectedPic.startAnimation(AnimationUtils.loadAnimation(Act,R.anim.fade_in));
                        LN_SP_Crop.setSelected(!isSelected);
                    }
                }else{
                    SM.SWToast("多選時無法選擇調整圖片");
                }
            }catch (Exception e){
                SM.UIToast(R.string.ERR_UpdateView);
            }
        });
        */

        LN_SP_Camera.setOnClickListener(view -> {
            if (SC.ShowCameraDialog()) {
                SC.setOnImageReceiveListener(new SCamera.onImageReceive() {
                    @Override
                    public void onReceive(String PicPath) {
                    }

                    @Override
                    public void onReceive(String PicPath, Bitmap BMP) {
                        try {
                            IMV_SP_SelectedPic.setTag(PicPath);
                            IMV_SP_SelectedPic.setImageBitmap(BMP);
                            IMV_SP_SelectedPic.startAnimation(AnimationUtils.loadAnimation(Act, R.anim.fade_in));
                            new Handler(Looper.getMainLooper()).postDelayed(() -> SCV_SP_Main.smoothScrollTo(0, 0), 300);
                        } catch (Exception e) {
                            SM.UIToast(R.string.ERR_UpdateView);
                        }
                    }
                });
            } else {
                SM.UIToast(R.string.ERR_CantUseCamera);
            }
        });

        LN_SP_Multi.setOnClickListener(v -> {
            try {
                //指定只更新部分介面 顯示or隱藏 選擇頁面
                SPAdapter.setMultiSelectMode(!SPAdapter.isMultiSelectMode());
                int FPos = GLM.findFirstVisibleItemPosition();
                int EPos = GLM.findLastVisibleItemPosition();
                for (int Pos = FPos; Pos <= EPos; Pos++) {
                    SPAdapter.notifyItemChanged(Pos, "ShowSelect");
                }
            } catch (Exception e) {
                SM.UIToast(R.string.ERR_UpdateView);
            }
        });

        findViewById(R.id.BT_SP_Close).setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            Act.finish();
        });
        findViewById(R.id.BT_SP_Next).setOnClickListener(view -> {
            try {
                JSONArray PathJA = new JSONArray();
                if (SPAdapter.isMultiSelectMode()) {//Multiple
                    SparseArray<Integer> SelectPicSA = SPAdapter.GetSelectPicSA();
                    ArrayList<String> PicPathAL = SPAdapter.GetPicPathAL();
                    for (int cnt = 0; cnt < SelectPicSA.size(); cnt++) {
                        String PicPath = PicPathAL.get(SelectPicSA.keyAt(cnt));
                        if (PicPath != null && PicPath.length() > 0) {
                            PathJA.put(PicPath);
                        }
                    }
                } else {//Single
                    String PicPath = (String) IMV_SP_SelectedPic.getTag();
                    if (PicPath != null && PicPath.length() > 0) {
                        File PicFile = new File(PicPath);
                        if (PicFile.exists()) {
                            PathJA.put(PicPath);
                        } else {
                            SM.SWToast("圖片不存在");
                        }
                    } else {
                        SM.SWToast("請先選擇圖片");
                    }
                }
                //Process CallBack Methods
                if (PathJA.length() > 0) {
                    String BrowseType = SM.JSONStrGetter(RequestJOB, "BrowseType");
                    if (PathJA.length() == 1 && BrowseType.equals("2")) {//Select & Crop
                        int Width = SM.JSONIntGetter(RequestJOB, "Width");
                        int Height = SM.JSONIntGetter(RequestJOB, "Height");
                        int Shape = SM.JSONIntGetter(RequestJOB, "Shape");
                        if (Width > 0 && Height > 0) {
                            // start picker to get image for cropping and then use the image in cropping activity
                            CropImage.ActivityBuilder AB = CropImage.activity(Uri.fromFile(new File(PathJA.getString(0))));
                            AB.setRequestedSize(Width, Height, CropImageView.RequestSizeOptions.RESIZE_EXACT);
                            AB.setAspectRatio(Width, Height);//設定指定比例
                            AB.setInitialCropWindowPaddingRatio(0);//設定截圖框靠邊
                            AB.setGuidelines(CropImageView.Guidelines.ON);
                            AB.setCropShape(Shape == 2 ? CropImageView.CropShape.OVAL : CropImageView.CropShape.RECTANGLE);
                            AB.start(Act);
                        } else {
                            SM.UIToast("設定參數有誤");
                            finish();
                        }
                    } else {//Single no Crop & Multiple
                        //CallBack
                        LastPathJA = PathJA;
                        Intent ResultIntent = new Intent();
                        ResultIntent.putExtra("CallBackID", SM.JSONStrGetter(RequestJOB, "CallBackID"));
                        ResultIntent.putExtra("ReturnType", SM.JSONStrGetter(RequestJOB, "ReturnType"));
                        setResult(Activity.RESULT_OK, ResultIntent);
                        finish();
                    }
                } else {
                    SM.SWToast("請先選擇圖片");
                }
            } catch (Exception e) {
                SM.UIToast(R.string.ERR_PrepareData);
                SM.DebugToast("BT_SP_Next\n" + e.getMessage());
            }
        });

    }

    private void InitRVAdapter(ArrayList<String> PicPathAL) {
        Act.runOnUiThread(() -> {
            try {
                SPAdapter = new RVAdapter(PicPathAL);
                String BrowseType = SM.JSONStrGetter(RequestJOB, "BrowseType");
                SPAdapter.setMultiSelectMode(BrowseType.equals("3"));//開啟多選
                GLM = new GridLayoutManager(Act, 4);
                RV_SP_Pic.setLayoutManager(GLM);
                RV_SP_Pic.setAdapter(SPAdapter);
                RV_SP_Pic.startAnimation(AnimationUtils.loadAnimation(Act, R.anim.slide_in_under));
                SM.DefineRVHeight(SCV_SP_Main, RV_SP_Pic, FL_SP_Func);
                //RV_SP_Pic.getRecycledViewPool().setMaxRecycledViews(SPAdapter.getItemViewType(0),50);//Set Cache Size
            } catch (Exception e) {
                SM.UIToast(R.string.ERR_PrepareData);
                SM.DebugToast("InitRVAdapter", e.getMessage());
            } finally {
                SM.SProgressDialog();
            }
        });

    }

    private RVAdapter SPAdapter;
    private GridLayoutManager GLM;

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.SViewHolder> {
        private ArrayList<String> PicPathAL;
        private SparseArray<Integer> SelectPicSA = new SparseArray<>();

        private int PicSize;
        private String CacheFolderPath;
        private boolean MultiSelectMode = false;
        private int SelectOrderCnt = 0;

        private RVAdapter(ArrayList<String> picPathAL) {
            PicPathAL = picPathAL;
            CacheFolderPath = SM.GetFolderPath("Cache", true);
            PicSize = (SM.GetScreenSize()[0] / 4) - SM.DpToPx(4);
            //Auto select first pic
            if (PicPathAL.size() > 0) {
                onPicSelect(0);
            }
        }

        class SViewHolder extends RecyclerView.ViewHolder {
            private SpinKitView SKV_SP_Pic;
            private ImageView IMV_SP_Pic;
            private FrameLayout FL_SP_Check;
            private TextView TV_SP_Check;
            private int Pos;

            private SViewHolder(View view) {
                super(view);
                SKV_SP_Pic = view.findViewById(R.id.SKV_SP_Pic);
                IMV_SP_Pic = view.findViewById(R.id.IMV_SP_Pic);
                TV_SP_Check = view.findViewById(R.id.TV_SP_Check);
                FL_SP_Check = view.findViewById(R.id.FL_SP_Check);
            }

            private int getPos() {
                return Pos;
            }

            private void setPos(int pos) {
                Pos = pos;
            }
        }

        @NonNull
        @Override
        public RVAdapter.SViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_sphoto, parent, false);
            return new RVAdapter.SViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SViewHolder SVH, int Pos, @NonNull List<Object> PayLoads) {
            if (PayLoads.isEmpty()) {
                onBindViewHolder(SVH, Pos);//更新整個 ViewHolder
            } else {
                String Type = (String) PayLoads.get(0);
                if (Type.equals("UpdateSelect")) {
                    UpdateSelectView(SVH, Pos);//更新指定部件
                } else if (Type.equals("ShowSelect")) {
                    SVH.FL_SP_Check.setVisibility(MultiSelectMode ? View.VISIBLE : View.INVISIBLE);
                    if (MultiSelectMode) {
                        UpdateSelectView(SVH, Pos);
                    } else {
                        SVH.IMV_SP_Pic.setOnClickListener(view -> {
                            onPicSelect(Pos);
                        });
                    }
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final SViewHolder SVH, final int Pos) {
            try {
                //Set View Width & Height
                SVH.setPos(Pos);
                ViewGroup.LayoutParams IMV_LP = SVH.IMV_SP_Pic.getLayoutParams();
                IMV_LP.height = PicSize;
                IMV_LP.width = PicSize;
                //SelectPicSection
                ViewGroup.LayoutParams FL_LP = SVH.FL_SP_Check.getLayoutParams();
                FL_LP.height = PicSize;
                FL_LP.width = PicSize;
                //Process Pic In Background
                StartProcessPicTask(SVH, Pos);
                //Select Pic
                SVH.FL_SP_Check.setVisibility(MultiSelectMode ? View.VISIBLE : View.INVISIBLE);
                if (MultiSelectMode) {
                    UpdateSelectView(SVH, Pos);
                } else {
                    SVH.IMV_SP_Pic.setOnClickListener(view -> {
                        onPicSelect(Pos);
                    });
                }
            } catch (Exception e) {
                SM.DebugToast(e.getMessage());
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull SViewHolder SVH) {
            try {
                int Pos = SVH.getPos();
                if (PicTaskSA.indexOfKey(Pos) != -1) {
                    PicTaskSA.remove(Pos);
                }
            } catch (Exception e) {
                SM.DebugToast("onViewDetachedFromWindow\n" + e.getMessage());
            }
            super.onViewDetachedFromWindow(SVH);
        }

        @Override
        public int getItemCount() {
            return PicPathAL.size();
        }

        //Pic Process Handler=======================================================================
        private boolean Processing = false;
        private SparseArray<SViewHolder> PicTaskSA = new SparseArray<>();
        private ArrayList<ProcessPicThread> ProcessPicTAL = new ArrayList<>();

        private void StartProcessPicTask(SViewHolder SVH, int Pos) {
            SVH.SKV_SP_Pic.setVisibility(ImageHaveCache(PicPathAL.get(Pos)) ? View.INVISIBLE : View.VISIBLE);
            SVH.IMV_SP_Pic.setVisibility(View.INVISIBLE);
            PicTaskSA.put(Pos, SVH);
            if (!Processing) {
                Processing = true;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            while (PicTaskSA.size() > 0) {
                                int Pos = PicTaskSA.keyAt(0);
                                SViewHolder SVH = PicTaskSA.get(Pos, null);
                                PicTaskSA.remove(Pos);
                                while (ProcessPicTAL.size() > 3) {
                                    sleep(10);
                                }
                                if (SVH != null) {
                                    ProcessPicThread PPT = new ProcessPicThread(SVH, Pos);
                                    ProcessPicTAL.add(PPT);
                                    PPT.start();
                                }
                            }
                        } catch (Exception e) {
                            //
                        } finally {
                            Processing = false;
                        }
                    }
                }.start();
            }
        }

        private class ProcessPicThread extends Thread {
            private SViewHolder SVH;
            private int Pos;

            private ProcessPicThread(SViewHolder SVH, int pos) {
                this.SVH = SVH;
                Pos = pos;
            }

            @Override
            public void run() {
                super.run();
                try {
                    File PicFile = new File(PicPathAL.get(Pos));
                    if (PicFile.exists()) {
                        String PicName = PicFile.getName();
                        if (PicName.length() > 0) {
                            File PicCacheFile = new File(CacheFolderPath, PicName);
                            Bitmap BMP;
                            if (PicCacheFile.exists()) {
                                BMP = BitmapFactory.decodeFile(PicCacheFile.getPath());
                            } else {
                                BMP = SM.CropToSquare(SM.GetResizedBitmap(PicFile.getPath(), 200));
                                SM.SaveBitmap(CacheFolderPath + File.separator + PicName, BMP);
                            }
                            if (BMP != null) {
                                new Handler(Act.getMainLooper()).post(() -> {
                                    try {
                                        if (SVH.getPos() == Pos) {
                                            SVH.IMV_SP_Pic.setImageBitmap(BMP);
                                            SVH.IMV_SP_Pic.setVisibility(View.VISIBLE);
                                            SVH.SKV_SP_Pic.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (Exception e) {
                                        SM.DebugToast("ProcessPicThread\n" + e.getMessage());
                                        IMVHideHandler(SVH, Pos);
                                    }
                                });
                            } else {
                                IMVHideHandler(SVH, Pos);
                            }
                        } else {
                            IMVHideHandler(SVH, Pos);
                        }
                    } else {
                        IMVHideHandler(SVH, Pos);
                    }
                } catch (Exception e) {
                    //
                } finally {
                    ProcessPicTAL.remove(this);
                }
            }
        }

        private void IMVHideHandler(SViewHolder SVH, int Pos) {
            if (SVH != null && SVH.getPos() == Pos) {
                new Handler(Act.getMainLooper()).post(() -> {
                    try {
                        SVH.SKV_SP_Pic.setVisibility(View.VISIBLE);
                        SVH.IMV_SP_Pic.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        //SM.UIToast(e.getMessage());
                    }
                });
            }
        }

        private boolean ImageHaveCache(String PicPath) {
            String PicName = PicPath.substring(PicPath.lastIndexOf("/") + 1);
            File PicCacheFile = new File(CacheFolderPath, PicName);
            return PicCacheFile.exists();
        }

        //Select Pic Section========================================================================
        private void onPicSelect(int Pos) {
            try {
                IMV_SP_SelectedPic.setTag(PicPathAL.get(Pos));
                IMV_SP_SelectedPic.setImageBitmap(SM.GetResizedBitmap(PicPathAL.get(Pos), 512));
                IMV_SP_SelectedPic.startAnimation(AnimationUtils.loadAnimation(Act, R.anim.fade_in));
                SCV_SP_Main.smoothScrollTo(0, 0);
            } catch (Exception e) {
                SM.UIToast(R.string.ERR_UpdateView);
            }
        }

        private void UpdateSelectView(SViewHolder SVH, int Pos) {
            //Show
            boolean isSelected = SelectPicSA.get(Pos, -1) != -1;
            SVH.FL_SP_Check.setSelected(isSelected);
            SVH.TV_SP_Check.setText(isSelected ? String.valueOf(GetOrder(Pos)) : "");//Show Num
            //SVH.TV_SP_Check.setText(isSelected?String.valueOf(SelectPicSA.indexOfKey(Pos)+1):"");//Show Num
            //OnClick
            SVH.IMV_SP_Pic.setOnClickListener(null);
            SVH.FL_SP_Check.setOnClickListener(v -> {
                //必須及時讀取是否已選擇 不可用上方的 isSelected
                boolean CisSelected = SelectPicSA.get(Pos, -1) != -1;
                if (CisSelected) {//Remove
                    SelectPicSA.remove(Pos);
                    CallUpdateSelectView(Pos, true);
                } else {//Select
                    if (MaxMultiNum == 0 || SelectPicSA.size() < MaxMultiNum) {
                        SelectPicSA.put(Pos, SelectOrderCnt++);
                        CallUpdateSelectView(Pos, false);
                    } else {
                        SM.SWToast("您最多只能選擇" + MaxMultiNum + "張");
                    }
                }
            });
        }

        private int GetOrder(int Pos) {
            int Val = SelectPicSA.get(Pos, -1);
            if (Val != -1) {
                int SmallerCounter = 1;
                for (int cnt = 0; cnt < SelectPicSA.size(); cnt++) {
                    int DVal = SelectPicSA.get(SelectPicSA.keyAt(cnt));
                    if (DVal < Val) {
                        SmallerCounter++;
                    }
                }
                return SmallerCounter;
            }
            return -1;
        }

        //Notify update without update image
        private void CallUpdateSelectView(int Pos, boolean isRemove) {
            for (int cnt = 0; cnt < SelectPicSA.size(); cnt++) {
                int SAPos = SelectPicSA.keyAt(cnt);
                notifyItemChanged(SAPos, "UpdateSelect");
            }
            //因為移除之後 SAPos 沒有被移除的Key故需要獨立出來更新
            if (isRemove) {
                notifyItemChanged(Pos, "UpdateSelect");
            }
        }

        private boolean isMultiSelectMode() {
            return MultiSelectMode;
        }

        private void setMultiSelectMode(boolean multiSelectMode) {
            MultiSelectMode = multiSelectMode;
            if (MultiSelectMode) {
                SelectPicSA.clear();
            }
        }

        //Data Exchange
        private SparseArray<Integer> GetSelectPicSA() {
            return SelectPicSA;
        }

        private ArrayList<String> GetPicPathAL() {
            return PicPathAL;
        }
    }

    private void LoadAllShownImagesPath() {
        SM.SProgressDialog(true, "正在取得圖庫資訊...", 500);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    ArrayList<String> PicPathAL = new ArrayList<>();
                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
                    Cursor CS = Act.getContentResolver().query(uri, projection, null, null, null);
                    if (CS != null) {
                        int column_index_data = CS.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        if (CS.moveToLast()) {//這也會吃掉一個項目
                            PicPathAL.add(CS.getString(column_index_data));
                        }
                        while (CS.moveToPrevious()) {
                            PicPathAL.add(CS.getString(column_index_data));
                        }
                        CS.close();
                    }
                    InitRVAdapter(PicPathAL);
                } catch (Exception e) {
                    SM.UIToast(R.string.ERR_PrepareData);
                    SM.DebugToast("GetAllShownImagesPath", e.getMessage());
                }
            }
        }.start();
    }

    //ProcessIntent=================================================================================
    /*
    BrowseType
    1 => Select One
    2 => Select One & Crop
    3 => Select Multiple
     */
    private void ProcessIntent() {
        try {
            //Process SelectMode
            RequestJOB = SM.JOBGetter(getIntent().getStringExtra("RequestJOB"));
            String BrowseType = SM.JSONStrGetter(RequestJOB, "BrowseType");
            if (BrowseType.equals("1") || BrowseType.equals("2")) {//Select | Select & Crop
                LN_SP_Multi.setVisibility(View.GONE);
            } else if (BrowseType.equals("3")) {//Select Multiple
                IMV_SP_SelectedPic.setVisibility(View.GONE);
                LN_SP_Camera.setVisibility(View.GONE);
                LN_SP_Multi.setVisibility(View.GONE);
                MaxMultiNum = SM.JSONIntGetter(RequestJOB, "MaxMultiNum", 1);
            }
        } catch (Exception e) {
            SM.UIToast(R.string.ERR_PrepareData);
            finish();
        }
    }

    //Permission====================================================================================
    private void AskForPermission() {
        boolean NeedAsk = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            NeedAsk = true;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            NeedAsk = true;
        }

        if (NeedAsk) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    TD.RQC_Permission);
        } else {
            SonCreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] Permissions, @NonNull int[] GrantResults) {
        try {
            if (requestCode == TD.RQC_Permission) {
                // If request is cancelled, the result arrays are empty.
                boolean AllAllow = true;
                for (int Result : GrantResults) {
                    if (Result != PackageManager.PERMISSION_GRANTED) {
                        AllAllow = false;
                        break;
                    }
                }
                if (AllAllow) {
                    SonCreate();
                } else {
                    SM.SWToast("請允許全部權限");
                    new Handler().postDelayed(() -> Act.finish(), 2500);
                }
            }
        } catch (Exception e) {
            SM.UIToast("取得權限結果發生錯誤");
            SM.DebugToast(e.getMessage());
        }
    }

    //Get File======================================================================================
    private static JSONArray LastPathJA;

    public static JSONArray GetLastSPhotoJA(SMethods SM, String ReturnType) throws Exception {
        JSONArray ResultJA = new JSONArray();
        if (LastPathJA != null) {
            if (SM.SContains(ReturnType, "Base64")) {
                ResultJA = GetBase64JA(LastPathJA);
            } else {
                JSONArray NewPathJA = new JSONArray();
                for (int cnt = 0; cnt < LastPathJA.length(); cnt++) {
                    String FilePath = LastPathJA.getString(cnt);
                    String Ext = FilePath.substring(FilePath.lastIndexOf(".") + 1);
                    JSONObject JOB = new JSONObject();
                    JOB.put("Data", FilePath);
                    JOB.put("Ext", Ext);
                    NewPathJA.put(JOB);
                }
                ResultJA = NewPathJA;
            }
            LastPathJA = null;
        }
        return ResultJA;
    }

    private static JSONArray GetBase64JA(JSONArray PathJA) {
        JSONArray Base64JA = new JSONArray();
        for (int cnt = 0; cnt < PathJA.length(); cnt++) {
            try {
                String FilePath = PathJA.getString(cnt);
                File file = new File(FilePath);
                FileInputStream FIS = new FileInputStream(file);
                byte[] buffer = new byte[(int) file.length() + 100];
                int length = FIS.read(buffer);
                String Data = Base64.encodeToString(buffer, 0, length, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
                String Ext = FilePath.substring(FilePath.lastIndexOf(".") + 1);

                JSONObject JOB = new JSONObject();
                JOB.put("Data", Data);
                JOB.put("Ext", Ext);
                Base64JA.put(JOB);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Base64JA;
    }
}
