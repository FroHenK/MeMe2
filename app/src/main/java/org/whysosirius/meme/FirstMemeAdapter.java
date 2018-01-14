package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by frohenk on 1/14/18.
 */

public class FirstMemeAdapter extends RecyclerView.Adapter<FirstMemeAdapter.ViewHolder> implements RecyclerViewContainer {


    private ArrayList<Meme> memes;
    private Context context;
    private SecondMemeAdapter secondMemeAdapter;
    private HashMap<Meme, Integer> memeTotalPositionMap;
    private int totalPosition;
    private int maxLoadedPosition;
    private RecyclerView recyclerView;

    @SuppressLint("UseSparseArrays")
    public FirstMemeAdapter(Context context, ArrayList<Meme> memes) {
        this.memes = memes;
        this.context = context;
        totalPosition = 0;
        memeTotalPositionMap = new HashMap<>();
    }

    public FirstMemeAdapter setSecondMemeAdapter(SecondMemeAdapter secondMemeAdapter) {
        this.secondMemeAdapter = secondMemeAdapter;
        return this;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Meme meme = memes.get(position);

        //long itemId = getItemId(position);
        if (!memeTotalPositionMap.containsKey(meme)) {
            memeTotalPositionMap.put(meme, totalPosition++);
        }
        holder.totalPosition = memeTotalPositionMap.get(meme);
        if (maxLoadedPosition < holder.totalPosition)
            maxLoadedPosition = holder.totalPosition;

        holder.memeTitleTextView.setText(meme.getTitle());
        Picasso.with(context).load(meme.getUrl()).into(holder.memeImageView);
        Log.i("siriusmeme", meme.getTitle() + " : " + holder.totalPosition + " " + String.valueOf(position));
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.totalPosition < maxLoadedPosition) {
            Log.i("siriusmeme", holder.memeTitleTextView.getText() + " to next second page");
            secondMemeAdapter.addMemeOnTop(memes.get(0));
            memes.remove(0);//usually it's the first element from top that gets recycled

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(0);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return memes.size();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView memeTitleTextView;
        ImageView memeImageView;
        public int totalPosition;

        public ViewHolder(View view) {
            super(view);

            memeTitleTextView = (TextView) view.findViewById(R.id.meme_title_text);
            memeImageView = (ImageView) view.findViewById(R.id.meme_image);
            totalPosition = -1;
        }
    }
}
