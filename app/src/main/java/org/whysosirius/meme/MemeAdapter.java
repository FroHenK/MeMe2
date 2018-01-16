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

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;
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
        memeFetcher.execute(url);
    }

    public void addMemeOnTop(Meme meme) {
        memes.add(0, meme);
        recyclerView.post(() -> notifyItemInserted(0));
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
                        Log.i("siriusmeme", "started load");
                        RequestFuture<JSONObject> future = RequestFuture.newFuture();
                        JsonObjectRequest request = new JsonObjectRequest(strings[0], new JSONObject(), future, future);
                        VolleySingleton.getInstance(MemeAdapter.this.context).addToRequestQueue(request);
                        JSONObject response = future.get();
                        if (response.getString("status").equals("success")) {
                            JSONArray memes = response.getJSONArray("links");

                        }
                    }
                    Thread.sleep(400);
                } catch (JSONException | InterruptedException | ExecutionException e) {
                    Log.e(MemeAdapter.class.getName(), "siriusmeme", e);
                }
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<Meme>[] values) {
            int size = memes.size();
            memes.addAll(values[0]);
            recyclerView.post(() -> notifyItemRangeInserted(size, values[0].size()));
        }
    }
}
