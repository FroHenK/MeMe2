package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        intent = getIntent();
        userId = intent.getStringExtra("user_id");
        userAvatarUrl = intent.getStringExtra("user_avatar_url");

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        setTitle(intent.getStringExtra("username"));

        fab = (FloatingActionButton) findViewById(R.id.profile_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
    }
}
