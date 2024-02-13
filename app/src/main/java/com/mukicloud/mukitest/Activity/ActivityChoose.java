package com.mukicloud.mukitest.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.SFunc.SMethods;
import com.mukicloud.mukitest.TD;

public class ActivityChoose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        CheckPermission();
    }

    private void SonCreate(){
        FindViews();
    }

    private void FindViews(){
        findViewById(R.id.BT_CHO_Web).setOnClickListener(BT-> startActivity(new Intent(this, ActivityWeb.class)));
        findViewById(R.id.BT_CHO_WebMenu).setOnClickListener(BT-> {
            Intent intent = new Intent(this, ActivityWeb.class);
            intent.putExtra("ViewType","Menu");
            startActivity(intent);
        });
    }

    private void CheckPermission(){
        boolean AllGranted = true;
        if (ActivityCompat.checkSelfPermission(ActivityChoose.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            AllGranted = false;
        }else if (ActivityCompat.checkSelfPermission(ActivityChoose.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            AllGranted = false;
        }
        if(AllGranted){
            SonCreate();
        } else {
            ActivityCompat.requestPermissions(ActivityChoose.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, TD.RQC_Permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    SonCreate();
                } else {
                    new SMethods(this).UIToast("請允許全部權限");
                    new Handler().postDelayed(this::finish, 1500);
                }
            }
        }
    }

}
