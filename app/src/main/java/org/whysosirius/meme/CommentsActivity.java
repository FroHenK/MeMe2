package org.whysosirius.meme;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.whysosirius.meme.database.Comment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private View sendCommentLayout;
    private EditText commentEditText;
    private ImageButton sendCommentButton;
    private SwipeRefreshLayout commentsSwipeRefreshLayout;
    private RecyclerView commentsRecyclerView;

    private ArrayList<Comment> comments;
    private HashMap<String, String> userIdsToUsernames;
    protected HashMap<String, String> userIdsToAvatarUrls;
    private String memeId;
    private View progressBar;

    private String getCommentsHost;
    private String postCommentsHost;
    private SharedPreferences preferences;
    private ObjectMapper objectMapper;
    private CommentsAdapter commentsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        userIdsToAvatarUrls = new HashMap<>();
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Intent intent = getIntent();
        memeId = intent.getStringExtra("meme_id");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.comments_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSupportActionBar(myToolbar);
            myToolbar.setTitle(intent.getStringExtra("title"));
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(intent.getStringExtra("title"));
        }

        preferences = getSharedPreferences(getString(R.string.siriusmeme_preferences_key), MODE_PRIVATE);

        postCommentsHost = getString(R.string.post_comments_url);
        getCommentsHost = getString(R.string.get_comments_url);


        comments = new ArrayList<>();
        userIdsToUsernames = new HashMap<>();


        progressBar = findViewById(R.id.login_progress);

        sendCommentLayout = findViewById(R.id.send_comment_layout);
        commentEditText = findViewById(R.id.comment_edit_text);
        sendCommentButton = findViewById(R.id.send_comment_image_button);
        commentsSwipeRefreshLayout = findViewById(R.id.comments_swipe_refresh);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setAdapter(commentsAdapter);

        commentsSwipeRefreshLayout.setOnRefreshListener(() -> startCommentsLoad());

        commentsSwipeRefreshLayout.setRefreshing(true);
        startCommentsLoad();
        sendCommentButton.setOnClickListener(view -> {
            postComments(commentEditText.getText().toString());
        });


    }

    private void startCommentsLoad() {
        Log.i("siriusmeme", "started loading comments of meme_id: " + memeId);
        StringRequest request = new StringRequest(Request.Method.POST, getCommentsHost, this::onGetCommentsResponse, error -> {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error while trying to communicate with server", Snackbar.LENGTH_LONG);
            snackbar.show();
            Log.e("siriusmeme", "error while trying to communicate with server", error.getCause());
            commentsSwipeRefreshLayout.setRefreshing(false);
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("auth_token", preferences.getString("auth_token", null));
                map.put("meme_id", memeId);
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void onGetCommentsResponse(String response) {
        commentsSwipeRefreshLayout.setRefreshing(false);
        Log.i("siriusmeme", "response from get_comments: " + response);


        JsonNode node = null;
        try {
            node = objectMapper.readTree(response);
            String status = node.get("status").asText();
            if (!status.equals("success"))
                throw new IOException("ohno");
            JsonNode commentsNode = node.get("comments");
            ArrayList<Comment> comments = new ArrayList<>();
            for (int i = 0; i < commentsNode.size(); i++) {
                Comment comment = objectMapper.treeToValue(commentsNode.get(i), Comment.class);
                comments.add(comment);
            }

            {
                JsonNode usernames = node.get("usernames");
                Iterator<Map.Entry<String, JsonNode>> fields = usernames.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> value = fields.next();
                    userIdsToUsernames.put(value.getKey(), value.getValue().textValue());
                }
            }
            {
                JsonNode avatarUrls = node.get("avatar_urls");
                Iterator<Map.Entry<String, JsonNode>> fields = avatarUrls.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> value = fields.next();
                    userIdsToAvatarUrls.put(value.getKey(), value.getValue().textValue());
                }
            }

            Collections.reverse(comments);

            this.comments = comments;

            commentsRecyclerView.post(() -> {
                //commentsAdapter.notifyItemRangeRemoved(0, formerSize);
                commentsAdapter.notifyItemRangeChanged(0, this.comments.size());
            });


        } catch (IOException e) {
            Log.e("siriusmeme", "error while reading servers response", e);
        }
    }


    private void postComments(String text) {
        commentsSwipeRefreshLayout.setRefreshing(true);
        // showProgress(true);
        sendCommentLayout.setEnabled(false);
        commentEditText.setEnabled(false);
        StringRequest request = new StringRequest(Request.Method.POST, postCommentsHost, this::onPostCommentsResponse, error -> {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_login_layout), "Error while trying to communicate with server", Snackbar.LENGTH_LONG);
            snackbar.show();
            //showProgress(false);
            sendCommentLayout.setEnabled(true);
            commentEditText.setEnabled(true);
            commentsSwipeRefreshLayout.setRefreshing(false);
            Log.e("siriusmeme", "error while trying to communicate with server", error.getCause());
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("auth_token", preferences.getString("auth_token", null));
                map.put("meme_id", memeId);
                map.put("text", text);

                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void onPostCommentsResponse(String response) {
        sendCommentLayout.setEnabled(true);
        commentEditText.setEnabled(true);
        commentsSwipeRefreshLayout.setRefreshing(false);
        JsonNode node = null;
        try {
            node = objectMapper.readTree(response);
            String status = node.get("status").asText();
            if (!status.equals("success"))
                throw new IOException("ohno");
            commentEditText.setText("");
            startCommentsLoad();
        } catch (IOException e) {
            Log.e("siriusmeme", "error while reading servers response", e);
        }

    }


    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.commentAuthorTextView.setText(userIdsToUsernames.get(comment.getAuthorId().toHexString()));
            holder.commentTextTextView.setText(comment.getText());

            Picasso.with(CommentsActivity.this).load(userIdsToAvatarUrls.get(comment.getAuthorId().toHexString())).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.commentAuthorImageView.setBackground(new BitmapDrawable(CommentsActivity.this.getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView commentTextTextView;
            final TextView commentAuthorTextView;
            final View commentAuthorImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                commentAuthorImageView = itemView.findViewById(R.id.comment_author_image_view);
                commentAuthorTextView = itemView.findViewById(R.id.comment_author_text_view);
                commentTextTextView = itemView.findViewById(R.id.comment_text_text_view);
            }
        }
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        sendCommentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        commentsSwipeRefreshLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
