package org.whysosirius.meme;

import android.content.Context;

import com.squareup.picasso.Picasso;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;

public class SecondMemeAdapter extends MemeAdapter {

    public SecondMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Meme meme = memes.get(position);
        holder.memeTitleTextView.setText(meme.getTitle());
        Picasso.with(context).load(meme.getUrl()).into(holder.memeImageView);
    }
}
