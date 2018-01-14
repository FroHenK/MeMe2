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

public class SecondMemeAdapter extends RecyclerView.Adapter<SecondMemeAdapter.ViewHolder> implements RecyclerViewContainer {
    private ArrayList<Meme> memes;
    private Context context;
    private RecyclerView recyclerView;

    public SecondMemeAdapter(Context context, ArrayList<Meme> memes) {
        this.memes = memes;
        this.context = context;
    }

    public void addMemeOnTop(Meme meme) {
        memes.add(0, meme);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(0);
            }
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
        holder.memeTitleTextView.setText(meme.getTitle());
        Picasso.with(context).load(meme.getUrl()).into(holder.memeImageView);
    }

    @Override
    public int getItemCount() {
        return memes.size();
    }

    @Override
    public void onViewRecycled(SecondMemeAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView memeTitleTextView;
        ImageView memeImageView;
        public long localId;

        public ViewHolder(View view) {
            super(view);

            memeTitleTextView = (TextView) view.findViewById(R.id.meme_title_text);
            memeImageView = (ImageView) view.findViewById(R.id.meme_image);
            localId = -1;
        }
    }
}
