package org.whysosirius.meme;

import android.content.Context;
import android.view.View;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.List;

public class SecondMemeAdapter extends MemeAdapter {


    public View rootView;
    public SecondMemeAdapter(Context context, List<Meme> memes) {
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
