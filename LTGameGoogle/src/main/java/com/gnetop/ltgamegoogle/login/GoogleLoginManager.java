package com.gnetop.ltgamegoogle.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.gnetop.ltgamecommon.base.Constants;
import com.gnetop.ltgamecommon.impl.OnLoginSuccessListener;
import com.gnetop.ltgamecommon.login.LoginBackManager;
import com.gnetop.ltgamecommon.util.DeviceIDUtil;
import com.gnetop.ltgamecommon.util.PreferencesUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;
import java.util.WeakHashMap;

public class GoogleLoginManager {

    private static final String TAG = GoogleLoginManager.class.getSimpleName();

    public static void initGoogle(Activity context, String clientID, int selfRequestCode) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        context.startActivityForResult(signInIntent, selfRequestCode);
        LoginBackManager.getUUID(context);
    }


    public static void onActivityResult(int requestCode, Intent data, int selfRequestCode,
                                        Context context, String LTAppID, String LTAppKey, String adID,
                                        String packageID,
                                        OnLoginSuccessListener mListener) {
        if (requestCode == selfRequestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (!TextUtils.isEmpty(adID)) {
                handleSignInResult(context, LTAppID, LTAppKey, adID, packageID,task, mListener);
            }
        }
    }


    private static void handleSignInResult(Context context, String LTAppID,
                                           String LTAppKey, String adID, String packageID,
                                           @NonNull Task<GoogleSignInAccount> completedTask,
                                           OnLoginSuccessListener mListener) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.e(TAG, idToken);
            Map<String, Object> map = new WeakHashMap<>();
            if (!TextUtils.isEmpty(adID)) {
                map.put("access_token", idToken);
                map.put("platform", 2);
                map.put("adid", DeviceIDUtil.getUniqueId(context));
                map.put("gps_adid", adID);
                map.put("platform_id", packageID);
            }
            LoginBackManager.googleLogin(context, LTAppID,
                    LTAppKey, map, mListener);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出登录
     */
    public static void GoogleSingOut(Context context, String clientID, final OnGoogleSignOutListener mListener) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mListener.onSignOutSuccess();
            }
        });
    }


}
