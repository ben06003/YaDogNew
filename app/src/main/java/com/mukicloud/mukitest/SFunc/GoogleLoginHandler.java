package com.mukicloud.mukitest.SFunc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mukicloud.mukitest.R;
import com.mukicloud.mukitest.TD;

import org.json.JSONObject;

public class GoogleLoginHandler {
    private static FirebaseAuth mAuth;
    private static OnGGLoginResult GLR;
    private final GoogleSignInClient mGoogleSignInClient;

    public GoogleLoginHandler(Activity act) {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(act.getString(R.string.default_web_client_id))//default_web_client_id
                .requestEmail()
                .requestProfile()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(act, gso);
    }

    public interface OnGGLoginResult {
        void onLogin(JSONObject ProfileJOB);

        void onLogout();
    }

    public void GGLogin(Activity Act, OnGGLoginResult GLR) {
            SMethods SM = new SMethods(Act);
        try {
            GoogleLoginHandler.GLR = GLR;
            if (isLoggedIn(Act)) {
                GoogleSignInSuccess(Act);
            } else {
//                SM.SProgressDialog(true, R.string.LOADING, 0);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                Act.startActivityForResult(signInIntent, TD.RQC_GGSignIn);
            }
        } catch (Exception e) {
            Log.d("GGLogin", "GGLogin: "+e);
//            SM.EXToast(R.string.CM_DetectError, "loginViaGoogle", e);
//            SM.SProgressDialog();
        }
    }

    public static void onActivityResult(Activity Act, Intent data) {
        SMethods SM = new SMethods(Act);
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                FirebaseAuthWithGoogle(Act, account.getIdToken());
            } else {
//                SM.SProgressDialog();
            }
        } catch (ApiException e) {
//            SM.SProgressDialog();
            Log.e("GoogleLoginHandler", e.getMessage());
            SM.EXToast(R.string.ERR_ProcessData, "GoogleLoginHandler", e);
        }
    }

    private static void FirebaseAuthWithGoogle(Activity Act, String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Act, task -> {
                    if (task.isSuccessful()) {
                        GoogleSignInSuccess(Act);
                    } else {
                        if (GLR != null) GLR.onLogout();
                    }
                });
    }

    private static void GoogleSignInSuccess(Activity Act) {
        SMethods SM = new SMethods(Act);
        try {
            //GoogleSignInAccount GGAccount = GetGGAccount(Act);
            FirebaseUser FireUser = GetFireUser();
            Uri PhotoUri = FireUser.getPhotoUrl();
            String picture = "";
            if (PhotoUri != null) picture = PhotoUri.toString();
            JSONObject ProfileJOB = new JSONObject();
            SM.JOBValueAdder(ProfileJOB, "id", FireUser.getUid());
            SM.JOBValueAdder(ProfileJOB, "email", FireUser.getEmail());
            SM.JOBValueAdder(ProfileJOB, "name", FireUser.getDisplayName());
            SM.JOBValueAdder(ProfileJOB, "picture", picture);
            if (GLR != null) GLR.onLogin(ProfileJOB);
        } catch (Exception e) {
            SM.EXToast(R.string.ERR_ProcessData, "GoogleSignInSuccess", e);
        } finally {
//            SM.SProgressDialog();
        }
    }

    public void GGLogout() {
        mGoogleSignInClient.signOut();
    }

    public boolean isLoggedIn(Activity Act) {
        return GetFireUser() != null && GetGGAccount(Act) != null;
    }

    public static FirebaseUser GetFireUser() {
        return mAuth.getCurrentUser();
    }

    public static GoogleSignInAccount GetGGAccount(Activity Act) {
        return GoogleSignIn.getLastSignedInAccount(Act);
    }
}
