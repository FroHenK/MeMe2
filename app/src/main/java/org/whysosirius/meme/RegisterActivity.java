package org.whysosirius.meme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences preferences;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        setContentView(R.layout.activity_register);
        Button reg_btn = (Button) findViewById(R.id.reg_btn);

    }
}
