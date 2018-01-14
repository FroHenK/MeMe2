package org.whysosirius.meme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.whysosirius.meme.MainActivity.APP_PREFERENCES;


public class LoginActivity extends AppCompatActivity {
    public static int port = 80;
    public static final String HOST = "memkekkekmem.herokuapp.com";

    public void showProblems() {
        Toast toast = Toast.makeText(this,
                "Проблемы с соединением к серверу. Проверьте настройки Интернета", Toast.LENGTH_SHORT);
        toast.show();
    }

    private SharedPreferences sharedPreferences;

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getRef();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    protected static TreeMap<String, ArrayList<String>> loginRegisterVk(String url, String code, String username) {
        TreeMap<String, String> req = new TreeMap<>();
        req.put("type", "vk_register");
        req.put("vk_code", code);
        req.put("username", username);
        return doRequest(url, req);
    }

    protected static TreeMap<String, ArrayList<String>> doRequest(String url, TreeMap<String, String> request) {
        TreeMap<String, ArrayList<String>> responce = new TreeMap<>();
        try {
            InetAddress ipAddress = InetAddress.getByName(url);
            Socket socket = new Socket(ipAddress, port);

            OutputStream sout = socket.getOutputStream();
            InputStream sin = socket.getInputStream();

            sout.write(("GET /chat HTTP/1.1\r\n" +
                    "Host: " + url + "\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" +
                    "Origin: " + url + "\r\n" +
                    "Sec-WebSocket-Protocol: chat, superchat\r\n" +
                    "Sec-WebSocket-Version: 13\r\n\r\n").getBytes());

            sout.flush();
            ArrayList<Integer> data = new ArrayList<Integer>();
            String string = "";
            while (true) {
                data.add(sin.read());
                if (data.size() >= 4 && data.get(data.size() - 1) == '\n' && data.get(data.size() - 2) == '\r' && data.get(data.size() - 3) == '\n' && data.get(data.size() - 4) == '\r')
                    break;
                string += (char) (int) data.get(data.size() - 1);
            }
            //System.out.println(data);
            System.out.println(string);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            out.writeUTF(gson.toJson(request));
            out.flush();
            String res = in.readUTF();
            responce = (TreeMap<String, ArrayList<String>>) new Gson().fromJson(res, responce.getClass());
            request.clear();
            request.put("type", "close");
            out.writeUTF(gson.toJson(request));
            out.flush();
            socket.close();
            return responce;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responce;
    }

    protected static TreeMap<String, ArrayList<String>> validateAuthToken(String url, String authToken) {
        TreeMap<String, String> req = new TreeMap<>();
        req.put("type", "validate_auth_token");
        req.put("auth_token", authToken);
        return doRequest(url, req);
    }

    protected static TreeMap<String, ArrayList<String>> checkRegistered(String url, String vk_auth_token) {
        return loginRegisterVk(url, vk_auth_token, "kek"); // neponatno
    }

    private void onLogin() {
        finish();
    }

    private int RegMode = 1337;

    private void Register(String accessToken) {
        Intent in = new Intent(LoginActivity.this, RegisterActivity.class);
        in.putExtra("vk_auth_token", accessToken);
        startActivityForResult(in, RegMode);
    }

    ImageButton loginButton;

    public void checkConnection() {
        if (sharedPreferences.contains("auth_token")) {
            loginButton.setVisibility(View.GONE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TreeMap<String, ArrayList<String>> stringArrayListTreeMap = validateAuthToken(HOST, sharedPreferences.getString("auth_token", null));
                    if (stringArrayListTreeMap.isEmpty()) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LoginActivity.this.showProblems();
                                loginButton.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                    if (stringArrayListTreeMap.get("status").get(0).equals("fail"))
                        return;
                    sharedPreferences.edit().putString("user_id", stringArrayListTreeMap.get("user_id").get(0)).putString("auth_token", stringArrayListTreeMap.get("auth_token").get(0)).putString("username", stringArrayListTreeMap.get("username").get(0)).commit();
                    onLogin();
                }
            }).start();
        }
    }

    public void connect() {
        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        final WebView wv = new LocalWebView(LoginActivity.this);
        android.webkit.CookieManager.getInstance().removeAllCookie();//TODO Don't forget about me. If you do may be frustrated
        wv.loadUrl("https://oauth.vk.com/authorize?" +
                "client_id=6327207&" +
                "scope=nohttps,offline&" +
                "redirect_uri=https://oauth.vk.com/blank.html&" +
                "display=touch&" +
                "v=5.69&" +
                "response_type=code");


        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        final String originalUrl = wv.getOriginalUrl();

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!url.startsWith("https://oauth.vk.com/blank.html"))
                    return;
                alertDialog.dismiss();
                try {
                    System.out.println(url);
                    final Map<String, String> stringStringMap = splitQuery(new URL(url));
                    if (stringStringMap.containsKey("error")) {
                        //TODO Process error
                        throw new IOException(stringStringMap.get("error_description"));
                    }
                    final String accessToken = stringStringMap.get("code");
                    final String username = "kek";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TreeMap<String, ArrayList<String>> stringArrayListTreeMap = loginRegisterVk(HOST, accessToken, username);
                            if (stringArrayListTreeMap.get("status").get(0).equals("fail")) {
                                Register(accessToken);
                            } else {
                                sharedPreferences.edit().putString("user_id", stringArrayListTreeMap.get("user_id").get(0)).putString("auth_token", stringArrayListTreeMap.get("auth_token").get(0)).putString("username", stringArrayListTreeMap.get("username").get(0)).commit();
                                onLogin();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    //TODO Process error
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        loginButton = (ImageButton) findViewById(R.id.vk_login);
        //checkConnection();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RegMode) {
            onLogin();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public static class LocalWebView extends WebView {
        public LocalWebView(Context context) {
            super(context);
        }

        @Override
        public boolean onCheckIsTextEditor() {
            return true;
        }
    }
}
