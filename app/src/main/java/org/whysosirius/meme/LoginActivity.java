package org.whysosirius.meme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String HOST = "memkekkekmem.herokuapp.com";
    public static final int RegMode = 1488;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        ImageButton imageButton = (ImageButton) findViewById(R.id.vk_login);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 1337);
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
