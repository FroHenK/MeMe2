package org.whysosirius.meme;

import android.content.Context;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;

public class SecondMemeAdapter extends MemeAdapter {

    public SecondMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);
    }

    public SecondMemeAdapter(Context applicationContext, String url) {
        super(applicationContext, url);
    }

    @Override
    public void onBindViewHolder(MemeAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
