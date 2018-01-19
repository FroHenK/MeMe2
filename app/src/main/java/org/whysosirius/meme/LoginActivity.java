package org.whysosirius.meme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String LOGIN_HOST = "memkekkekmem.herokuapp.com/login";
    public static final String REG_HOST = "memkekkekmem.herokuapp.com/register";
    public static final int RegMode = 1488;
    private SharedPreferences sharedPreferences;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private JsonNode doVolleyRequest(HashMap<String, String> map) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, LOGIN_HOST, future, future) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        JsonNode node = null;

        try {
            String res = future.get();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            node = objectMapper.readTree(res);
        }catch (Exception e) {
            Log.e("chto to ne tak", "with login");
        }finally {
            return node;
        }
    }
    private void onLogin(FirebaseUser user) {
            HashMap<String, String>hm = new HashMap<>();
            hm.put("uid", user.getUid());
            JsonNode node = doVolleyRequest(hm);
            if (node.get("status").asText().equals("success")) {
                sharedPreferences.edit().putString("auth_token", node.get("auth_token").asText())
                        .putString("username", node.get("username").asText()).apply();
            }else
            {
                if (node.get("status").asText().equals("missing")){
                    hm.clear();
                    hm.put("uid", user.getUid());
                    hm.put("username",user.getDisplayName());
                    JsonNode req_node = doVolleyRequest(hm);
                    if (req_node.get("status").asText().equals("success")){
                        sharedPreferences.edit().putString("auth_token", node.get("auth_token").asText())
                                .putString("username", hm.get("username")).apply();
                        finish();
                    }else{
                        int col = 0; // nemnogo_kostil
                        while(true) {
                            col++;
                            if (req_node.get("status").asText().equals("username_used")) {
                                hm.clear();
                                hm.put("uid", user.getUid());
                                hm.put("username",user.getDisplayName() + "#" + col);
                                req_node = doVolleyRequest(hm);
                            }else
                                break;
                        }
                        sharedPreferences.edit().putString("auth_token", node.get("auth_token").asText()).putString("username", hm.get("username")).apply();
                        finish();
                    }
                }
            }
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().apply(); // TODO UBRAAAAAATTTTTT
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
