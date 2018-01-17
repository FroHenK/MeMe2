package org.whysosirius.meme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String HOST = "memkekkekmem.herokuapp.com";
    public static final int RegMode = 1488;
    private SharedPreferences sharedPreferences;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private void onLogin(){

    }
    @Override
    public void onStart(){
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
            onLogin();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();''
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        Button button = findViewById(R.id.button2);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            AuthUI.getInstance().signOut(this);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(
                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                .build(), 1337);
            }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337) {
            Log.i("gg","konsid");
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}
