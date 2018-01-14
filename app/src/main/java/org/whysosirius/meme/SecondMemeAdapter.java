package org.whysosirius.meme;

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

/**
 * Created by frohenk on 1/14/18.
 */

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
