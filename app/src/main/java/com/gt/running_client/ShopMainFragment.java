package com.gt.running_client;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopMainFragment extends Fragment {


    private static final String TAG = "TAG_MainFragment";
    private static final int REQ_SIGN_IN = 101;
    private Activity activity;
    private TextView textView;
    private GoogleSignInClient client;
    private FirebaseAuth auth;

    //  要記得至登入方式設定允許一個電子郵件使用多種的登入方式
    //  因為同一個信箱會用FB、GOOGLE登入

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出
                // 這是JSON預設值
                .requestIdToken(getString(R.string.default_web_client_id))
                // 請求驗證身分的認證碼
                // 要求輸出email
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(activity, options);
        // 取得CLIENT物件
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle("Login Google");
        return inflater.inflate(R.layout.fragment_shop_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = view.findViewById(R.id.textView);

        view.findViewById(R.id.btSignInGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
    }

    // 跳出Google登入畫面
    private void signInGoogle() {
        Intent signInIntent = client.getSignInIntent();

        startActivityForResult(signInIntent, REQ_SIGN_IN);
        //GO！
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // DATA又是登入後的結果惹
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SIGN_IN) {
            // 取得裝置上的Google登入帳號
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // 專門在座解析的API，總之呢要解析結果要費兩次功
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // 取出帳務的資訊
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                    // 登入已成功，現在要將資料寫入FIREBASE(留存在伺服器)
                } else {
                    Log.e(TAG, "GoogleSignInAccount is null");
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed");
            }
        }
    }

    // 使用Google帳號完成Firebase驗證
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // get the unique ID for the Google account
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        //從帳務中拿出ID
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        // 取得憑證，也就是所謂的TOKEN，才能拿來存入AUTH
        auth.signInWithCredential(credential)
                //特別針對使用憑證登入(IDTOKEN)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //監聽已記錄完畢
                        // 登入成功轉至下頁；失敗則顯示錯誤訊息
                        if (task.isSuccessful()) {
                            //記錄成功
                            textView.setText("Success");
                        } else {
                            Exception exception = task.getException();
                            String message = exception == null ? "Sign in fail." : exception.getMessage();
                            textView.setText(message);
                        }

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // 檢查user是否已經登入，是則FirebaseUser物件不為null
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            textView.setText("Success");

        }
    }



}
