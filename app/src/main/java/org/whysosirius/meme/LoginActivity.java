package org.whysosirius.meme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String LOGIN_HOST = "http://192.168.43.23:8080/get_auth_token";
    private static final int RC_SIGN_IN = 42; // meaning of life


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        });
    }

    private void onGetAuthTokenResponse(String response) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), response, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                user.getIdToken(true).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        StringRequest request = new StringRequest(Request.Method.POST, LOGIN_HOST, this::onGetAuthTokenResponse, error -> {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Something went horribly wrong. Wrong Google Token", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("token",token);
                                return map;
                            }
                        };
                        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error getting ID Token", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Couldn't login error code: " + (response != null ? response.getErrorCode() : null), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }


}
