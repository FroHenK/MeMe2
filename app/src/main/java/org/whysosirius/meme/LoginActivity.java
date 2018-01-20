package org.whysosirius.meme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    public String validateSessionHost;
    public String setUsernameHost;
    public String getAuthTokenHost;

    private static final int RC_SIGN_IN = 42; // meaning of life
    private SharedPreferences preferences;
    private View setUsernameForm;
    private View progressBar;
    private View loginForm;
    private ObjectMapper objectMapper;
    private Button setUsernameButton;
    private EditText usernameText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        validateSessionHost = getString(R.string.validate_session_url);
        setUsernameHost = getString(R.string.set_username_url);
        getAuthTokenHost = getString(R.string.get_auth_token_url);

        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.login_button);
        loginForm = findViewById(R.id.login_activity_login_form);
        progressBar = findViewById(R.id.login_progress);
        setUsernameForm = findViewById(R.id.login_activity_login_set_username_form);

        setUsernameButton = findViewById(R.id.username_edit_confirm_button);
        usernameText = findViewById(R.id.username_edit_text);

        setUsernameButton.setOnClickListener(v -> {
            showProgress(true, setUsernameForm);
            StringRequest request = new StringRequest(Request.Method.POST, setUsernameHost, this::onSetUsernameResponse, error -> {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error while trying to communicate with server", Snackbar.LENGTH_LONG);
                snackbar.show();
                showProgress(false, setUsernameForm);
                Log.e("siriusmeme", "error while trying to communicate with server", error.getCause());
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("auth_token", preferences.getString("auth_token", null));
                    map.put("username", usernameText.getText().toString());
                    return map;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
        });

        loginButton.setOnClickListener(v -> {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
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

        });

        preferences = getSharedPreferences(getString(R.string.siriusmeme_preferences_key), MODE_PRIVATE);

        if (preferences.contains("auth_token"))
            onAuthTokenReception(preferences.getString("auth_token", null));
        else
            loginForm.setVisibility(View.VISIBLE);
    }

    private void onSetUsernameResponse(String response) {
        showProgress(false, setUsernameForm);
        Log.i("siriusmeme", "response from get_auth_token: " + response);

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), response, Snackbar.LENGTH_LONG);
        snackbar.show();

        try {
            JsonNode node = objectMapper.readTree(response);
            String status = node.get("status").asText();

            if (status.equals("success")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            String message = node.get("message").asText();
            if ((status.equals("fail") && message.equals("username_already_set"))) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            if (status.equals("fail") && message.equals("username_invalid")) {
                usernameText.setError("Username is invalid");
                usernameText.requestFocus();
                return;
            }
            if (status.equals("fail") && message.equals("username_is_occupied")) {
                usernameText.setError("Username is occupied");
                usernameText.requestFocus();
                return;
            }
            throw new IOException("error while setting username");
        } catch (IOException e) {
            Log.e("siriusmeme", "error reading json", e);
            Snackbar.make(findViewById(R.id.activity_login_layout), "Error while reading response from server...", Snackbar.LENGTH_LONG).show();
        }

    }

    @SuppressLint("ApplySharedPref")
    private void onGetAuthTokenResponse(String response) {
        showProgress(false, loginForm);
        Log.i("siriusmeme", "response from get_auth_token: " + response);

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), response, Snackbar.LENGTH_LONG);
        snackbar.show();

        try {
            JsonNode node = objectMapper.readTree(response);
            if (!node.get("status").asText().equals("success"))
                throw new IOException("unsuccessful operation");
            String authToken = node.get("auth_token").asText();
            preferences.edit().putString("auth_token", authToken).commit();
            onAuthTokenReception(authToken);
        } catch (IOException e) {
            Log.e("siriusmeme", "error reading json", e);
            Snackbar.make(findViewById(R.id.activity_login_layout), "Error while reading response from server...", Snackbar.LENGTH_LONG).show();
        }
    }

    private void onAuthTokenReception(String authToken) {
        Log.i("siriusmeme", "got auth_token: " + authToken);
        showProgress(true, loginForm);
        StringRequest request = new StringRequest(Request.Method.POST, validateSessionHost, this::onValidateSessionResponse, error -> {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error while trying to communicate with server", Snackbar.LENGTH_LONG);
            snackbar.show();
            showProgress(false, loginForm);
            Log.e("siriusmeme", "error while trying to communicate with server", error.getCause());
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("auth_token", authToken);
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
    }

    private void onValidateSessionResponse(String response) {
        showProgress(false, loginForm);
        Log.i("siriusmeme", "response from validate_session: " + response);

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), response, Snackbar.LENGTH_LONG);
        snackbar.show();


        try {
            JsonNode node = objectMapper.readTree(response);
            String status = node.get("status").asText();
            if (status.equals("success")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            if (status.equals("void_username")) {
                setUsernameForm.setVisibility(View.VISIBLE);
                loginForm.setVisibility(View.GONE);

                return;
            }
            throw new IOException("unsuccessful operation");

        } catch (IOException e) {
            Log.e("siriusmeme", "error reading json", e);
            Snackbar.make(findViewById(R.id.activity_login_layout), "Error while reading response from server...", Snackbar.LENGTH_LONG).show();
        }
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
                        showProgress(true, loginForm);
                        String token = task.getResult().getToken();
                        StringRequest request = new StringRequest(Request.Method.POST, getAuthTokenHost, this::onGetAuthTokenResponse, error -> {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error while trying to communicate with server", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            showProgress(false, loginForm);
                            Log.e("siriusmeme", "error while trying to communicate with server", error.getCause());
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("token", token);
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


    private void showProgress(final boolean show, final View view) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        view.setVisibility(show ? View.GONE : View.VISIBLE);
        view.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //    view.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //     progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}
