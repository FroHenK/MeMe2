package org.whysosirius.meme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.TreeMap;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences preferences;
    private void onRegister(){
        finish();
    }
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        preferences = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_register);
        Button reg_btn = (Button) findViewById(R.id.reg_btn);
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String username = ((EditText)findViewById(R.id.textInput)).getText().toString();
                        String token = intent.getStringExtra("vk_auth_token");
                        TreeMap<String, ArrayList<String>> res = LoginActivity.loginRegisterVk(LoginActivity.HOST, token , username);
                        if (res.get("status").get(0).equals("fail"))
                            return;
                        preferences.edit().putString("user_id", res.get("user_id").get(0)).putString("auth_token", res.get("auth_token").get(0)).putString("username", res.get("username").get(0)).commit();
                        onRegister();
                    }
                }).start();
            }
        });
    }
}
