package org.whysosirius.meme;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class MemeAdapter extends RecyclerView.Adapter<MemeAdapter.ViewHolder> implements RecyclerViewContainer {
    protected ArrayList<Meme> memes;
    protected Context context;
    protected RecyclerView recyclerView;

    private HashMap<Meme, Integer> memeTotalPositionMap;

    protected int totalPosition;
    protected long maxLoadedPosition;
    protected long totalMemesLoaded;
    protected static final int MEMES_IN_PAGE = 30;
    protected static final int TRIGGER_MEME = 10;//must be lesser than MEMES_IN_PAGE and greater than zero, otherwise -> butthurt
    private MemeFetcher memeFetcher;


    public MemeAdapter(Context context, ArrayList<Meme> memes) {
        this.memes = memes;
        this.context = context;
        totalPosition = 0;
        totalMemesLoaded = memes.size();
        memeTotalPositionMap = new HashMap<>();
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
        TextView memeTitleTextView;
        ImageView memeImageView;

        public ViewHolder(View view) {
            super(view);

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
                        Log.i("siriusmeme", "started load" + strings[0]);
                        RequestFuture<String> future = RequestFuture.newFuture();
                        StringRequest request = new StringRequest(Request.Method.POST, strings[0], future, future) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("auth_token", "QJZUXOcca4wKSzy0CkFUU");
                                map.put("count", "30");
                                if (MemeAdapter.this.memes.size() != 0)
                                    map.put("last", MemeAdapter.this.memes.get(MemeAdapter.this.memes.size() - 1).getId().toHexString());
                                else
                                    map.put("last", "null");

                                return map;
                            }
                        };


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
                            MemeAdapter.this.memes.addAll(memes);
                            publishProgress(memes);
                        }
                    }
                } catch (InterruptedException | IOException | ExecutionException e) {
                    Log.e(MemeAdapter.class.getName(), "siriusmeme", e);
                }

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<Meme>[] values) {
            int size = memes.size();
            recyclerView.post(() -> notifyItemRangeInserted(size, values[0].size()));
            this.cancel(true);
        }
    }
}
