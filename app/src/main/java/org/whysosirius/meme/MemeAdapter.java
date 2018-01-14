package org.whysosirius.meme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;

public abstract class MemeAdapter extends RecyclerView.Adapter<MemeAdapter.ViewHolder> implements RecyclerViewContainer {
    protected ArrayList<Meme> memes;
    protected Context context;
    protected RecyclerView recyclerView;

    public MemeAdapter(Context context, ArrayList<Meme> memes) {
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
    public abstract void onBindViewHolder(ViewHolder holder, int position);

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
}
