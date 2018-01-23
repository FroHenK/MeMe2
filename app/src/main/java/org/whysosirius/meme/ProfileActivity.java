package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Intent intent;
    private android.support.v7.app.ActionBar supportActionBar;
    private String userId;
    private String userAvatarUrl;
    private FloatingActionButton fab;
    private ImageView avatarImageView;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RecyclerView profileRecyclerView;
    private ProfileMemeAdapter adapter;
    private boolean isSubscribed;
    private SharedPreferences preferences;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firstTime = true;
        intent = getIntent();
        userId = intent.getStringExtra("user_id");
        userAvatarUrl = intent.getStringExtra("user_avatar_url");

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        setSupportActionBar(toolbar);
        preferences = getSharedPreferences(getString(R.string.siriusmeme_preferences_key), MODE_PRIVATE);


        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        setTitle(intent.getStringExtra("username"));

        fab = findViewById(R.id.profile_fab);

        fab.setBackgroundTintList(ColorStateList.valueOf(0x00000000));

        profileRecyclerView = findViewById(R.id.profile_recycler_view);
        collapsingToolbarLayout = findViewById(R.id.profile_collapsing);
        appBarLayout = findViewById(R.id.profile_appbar);
        avatarImageView = findViewById(R.id.profile_backdrop);


        adapter = new ProfileMemeAdapter(this, getString(R.string.get_user_memes_list_url));

        RecyclerView recyclerView = findViewById(R.id.profile_recycler_view);
        adapter.setRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);//if memes become expandable delete this

        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        Picasso.with(this).load(userAvatarUrl).into(avatarImageView);
    }

    private void setIsSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        this.runOnUiThread(() -> {
            if (isSubscribed) {
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cardview_dark_background)));
                fab.setImageResource(R.drawable.ic_person_remote_black_24dp);
                fab.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(view -> {
                    setIsSubscribed(false);
                    StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.unsubscribe_url), null, null) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("auth_token", preferences.getString("auth_token", null));
                            map.put("user_id", userId);
                            return map;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    VolleySingleton.getInstance(ProfileActivity.this).addToRequestQueue(request);
                });
            } else {
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab.setImageResource(R.drawable.ic_person_add_black_24dp);
                fab.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(view -> {
                    setIsSubscribed(true);
                    StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.subscribe_url), null, null) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("auth_token", preferences.getString("auth_token", null));
                            map.put("user_id", userId);
                            return map;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    VolleySingleton.getInstance(ProfileActivity.this).addToRequestQueue(request);
                });
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        adapter.memeFetcher.cancel(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.memeFetcher.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.memeFetcher != null && adapter.memeFetcher.isCancelled())
            adapter.startKek();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.memeFetcher.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private class ProfileMemeAdapter extends MemeAdapter {

        @SuppressLint("UseSparseArrays")
        public ProfileMemeAdapter(Context context, ArrayList<Meme> memes) {
            super(context, memes);
        }

        public ProfileMemeAdapter(Context context, String url) {
            super(context, url);
        }

        @Override
        public void onBindViewHolder(MemeAdapter.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            Meme meme = memes.get(position);
        }


        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            if (memes.size() == 0)
                return;

            if (holder.totalPosition < maxLoadedPosition) {
                Log.v("siriusmeme", holder.memeTitleTextView.getText() + " is being transferred to the SecondMemeAdapter");
                setViewedOnServer(memes.get(0));
            }
        }

        private void setViewedOnServer(Meme meme) {
            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.POST, context.getString(R.string.set_viewed_url), future, future) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("auth_token", sharedPreferences.getString("auth_token", null));
                    map.put("meme_id", meme.getId().toHexString());
                    return map;
                }
            };

            VolleySingleton.getInstance(this.context).addToRequestQueue(request);
        }

        @Override
        protected HashMap<String, String> getInitialMap() {
            HashMap<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            return map;
        }

        @Override
        protected void onResponse(JsonNode node) {
            if (node.get("status").asText().equals("success") && firstTime) {
                setIsSubscribed(node.get("is_subscribed").asBoolean());
                firstTime = false;
            }
        }
    }
}
