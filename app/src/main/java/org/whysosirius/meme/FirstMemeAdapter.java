package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;


public class FirstMemeAdapter extends MemeAdapter {

    private SecondMemeAdapter secondMemeAdapter;

    @SuppressLint("UseSparseArrays")
    public FirstMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);

    }

    @Override
    public void onBindViewHolder(MemeAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Meme meme = memes.get(position);

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

            recyclerView.post(() -> notifyItemRemoved(0));
        }
    }
}
