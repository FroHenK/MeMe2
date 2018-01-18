package org.whysosirius.meme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String HOST = "memkekkekmem.herokuapp.com";
    public static final int RegMode = 1488;
    private SharedPreferences sharedPreferences;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;

    private void onLogin(FirebaseUser user) {
        //TODO ZAPROS NA SERVER
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("auth_token"))
            finish();
    }

    public static final int EMAIL_CODE = 1;
    public static final int GOOGLE_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        Button emailLogin = findViewById(R.id.login_wia_email_btn);
        Button googleLogin = findViewById(R.id.google_login_btn);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            onLogin(auth.getCurrentUser());
        } else {
            emailLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                    .build(), EMAIL_CODE);
                }
            });
            googleLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(), GOOGLE_CODE);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EMAIL_CODE) {
            Log.i("email", "konsid");
            if (resultCode == RESULT_OK) {
                onLogin(FirebaseAuth.getInstance().getCurrentUser());
            }
        } else if (requestCode == GOOGLE_CODE) {
            Log.i("google", "konsid");
            if (resultCode == RESULT_OK) {
                onLogin(FirebaseAuth.getInstance().getCurrentUser());
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}
