package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;


public class FirstMemeAdapter extends MemeAdapter {

    private SecondMemeAdapter secondMemeAdapter;
    private HashMap<Meme, Integer> memeTotalPositionMap;
    private int totalPosition;
    private long maxLoadedPosition;
    private long totalMemesLoaded;
    private static final int MEMES_IN_PAGE = 30;
    private static final int TRIGGER_MEME = 10;//must be lesser than MEMES_IN_PAGE and greater than zero, otherwise -> butthurt

    @SuppressLint("UseSparseArrays")
    public FirstMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);
        totalPosition = 0;
        totalMemesLoaded = memes.size();
        memeTotalPositionMap = new HashMap<>();

    }

    @Override
    public void onBindViewHolder(MemeAdapter.ViewHolder holder, int position) {
        Meme meme = memes.get(position);

        if (!memeTotalPositionMap.containsKey(meme)) {
            memeTotalPositionMap.put(meme, totalPosition++);
        }
        holder.totalPosition = memeTotalPositionMap.get(meme);
        if (maxLoadedPosition < holder.totalPosition)
            maxLoadedPosition = holder.totalPosition;

        holder.memeTitleTextView.setText(meme.getTitle());
        Picasso.with(context).load(meme.getUrl()).into(holder.memeImageView);
    }

    public void setSecondMemeAdapter(SecondMemeAdapter secondMemeAdapter) {
        this.secondMemeAdapter = secondMemeAdapter;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.totalPosition < maxLoadedPosition) {
            Log.v("siriusmeme", holder.memeTitleTextView.getText() + " is being transferred to the SecondMemeAdapter");
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
}
