package org.whysosirius.meme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public abstract class MemeAdapter extends RecyclerView.Adapter<MemeAdapter.ViewHolder> implements RecyclerViewContainer {
    protected ArrayList<Meme> memes;
    protected Context context;
    protected RecyclerView recyclerView;

    private HashMap<Meme, Integer> memeTotalPositionMap;
    protected HashMap<String, String> userIdsToUsernames;

    protected int totalPosition;
    protected long maxLoadedPosition;
    protected long totalMemesLoaded;
    protected static final int MEMES_IN_PAGE = 30;
    protected static final int TRIGGER_MEME = 10;//must be lesser than MEMES_IN_PAGE and greater than zero, otherwise -> butthurt
    private MemeFetcher memeFetcher;
    SharedPreferences sharedPreferences;

    public MemeAdapter(Context context, ArrayList<Meme> memes) {
        this.memes = memes;
        this.context = context;
        totalPosition = 0;
        totalMemesLoaded = memes.size();
        memeTotalPositionMap = new HashMap<>();
        userIdsToUsernames = new HashMap<>();
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.siriusmeme_preferences_key), MODE_PRIVATE);

    }

    public MemeAdapter(Context context, String url) {
        this(context, new ArrayList<>());
        memeFetcher = new MemeFetcher();
        memeFetcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);//start meme fetcher service (AsyncTask)
    }

    public void addMemeOnTop(Meme meme) {
        memes.add(0, meme);
        recyclerView.post(() -> {
            notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Meme meme = memes.get(position);
        holder.likeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                }
            }
        });
        if (!memeTotalPositionMap.containsKey(meme)) {
            memeTotalPositionMap.put(meme, totalPosition++);
        }
        holder.totalPosition = memeTotalPositionMap.get(meme);
        if (maxLoadedPosition < holder.totalPosition)
            maxLoadedPosition = holder.totalPosition;

        if (meme.getTitle() != null)
            holder.memeTitleTextView.setText(meme.getTitle());
        else
            holder.memeTitleTextView.setText("meme #" + holder.totalPosition);

        holder.memeAuthorTextView.setText(userIdsToUsernames.get(meme.getAuthorId().toHexString()));

        View.OnClickListener onClickListener = v -> {

        };
        holder.memeAuthorTextView.setOnClickListener(onClickListener);
        holder.memeAuthorImageView.setOnClickListener(onClickListener);

        Picasso.with(context).load(meme.getUrl()).into(holder.memeImageView);
    }

    @Override
    public int getItemCount() {
        return memes.size();
    }

    @Override
    public void onViewRecycled(MemeAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public long totalPosition;
        CheckBox likeCheckBox;
        CheckBox  dislikeCheckBox;
        ImageView memeAuthorImageView;
        TextView memeAuthorTextView;

        TextView memeTitleTextView;
        ImageView memeImageView;

        public ViewHolder(View view) {
            super(view);

            memeAuthorImageView = view.findViewById(R.id.meme_author_image);
            memeAuthorTextView = view.findViewById(R.id.meme_author_text);
            likeCheckBox = view.findViewById(R.id.like_check_box);
            dislikeCheckBox = view.findViewById(R.id.check_box_dislike);
            memeTitleTextView = (TextView) view.findViewById(R.id.meme_title_text);
            memeImageView = (ImageView) view.findViewById(R.id.meme_image);
            totalPosition = -1;
        }
    }


    protected class MemeFetcher extends AsyncTask<String, ArrayList<Meme>, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            while (true) {//FIXME endless loop
                try {
                    if (TRIGGER_MEME + maxLoadedPosition - totalMemesLoaded >= 0) {
                        if (sharedPreferences.contains("auth_token")) {
                            Log.i("siriusmeme", "started load" + strings[0]);
                            RequestFuture<String> future = RequestFuture.newFuture();
                            StringRequest request = new StringRequest(Request.Method.POST, strings[0], future, future) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("auth_token", sharedPreferences.getString("auth_token", null));
                                    map.put("count", String.valueOf(MEMES_IN_PAGE));
                                    if (MemeAdapter.this.memes.size() != 0)
                                        map.put("last", MemeAdapter.this.memes.get(MemeAdapter.this.memes.size() - 1).getId().toHexString());
                                    else
                                        map.put("last", "null");

                                    return map;
                                }
                            };
                            request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            VolleySingleton.getInstance(MemeAdapter.this.context).addToRequestQueue(request);
                            String response = future.get();
                            Log.i("siriusmeme", response);

                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
                            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

                            JsonNode node = objectMapper.readTree(response);

                            if (node.get("status").asText().equals("success")) {
                                JsonNode memesNode = node.get("links");
                                ArrayList<Meme> memes = new ArrayList<>();
                                for (int i = 0; i < memesNode.size(); i++) {
                                    Meme meme = objectMapper.treeToValue(memesNode.get(i), Meme.class);
                                    memes.add(meme);
                                }
                                totalMemesLoaded += memes.size();

                                int size = MemeAdapter.this.memes.size();
                                MemeAdapter.this.memes.addAll(memes);
                                recyclerView.post(() -> notifyItemRangeInserted(size, memes.size()));

                                {
                                    JsonNode usernames = node.get("usernames");
                                    Iterator<Map.Entry<String, JsonNode>> fields = usernames.fields();
                                    while (fields.hasNext()) {
                                        Map.Entry<String, JsonNode> value = fields.next();
                                        userIdsToUsernames.put(value.getKey(), value.getValue().textValue());
                                    }
                                }

                                publishProgress(memes);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.v(MemeAdapter.class.getName(), "siriusmeme", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(MemeAdapter.class.getName(), "siriusmeme", e);
                }
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<Meme>[] values) {
        }
    }
}
