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


public class FirstMemeAdapter extends MemeAdapter {

    private SecondMemeAdapter secondMemeAdapter;
    private HashMap<Meme, Integer> memeTotalPositionMap;
    private int totalPosition;
    private long maxLoadedPosition;

    @SuppressLint("UseSparseArrays")
    public FirstMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);
        totalPosition = 0;
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
        Log.i("siriusmeme", meme.getTitle() + " : " + holder.totalPosition + " " + String.valueOf(position));
    }

    public FirstMemeAdapter setSecondMemeAdapter(SecondMemeAdapter secondMemeAdapter) {
        this.secondMemeAdapter = secondMemeAdapter;
        return this;
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

}
