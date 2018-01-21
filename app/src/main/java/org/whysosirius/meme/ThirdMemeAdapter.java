package org.whysosirius.meme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 21.01.2018.
 */

public class ThirdMemeAdapter extends MemeAdapter {

    private SecondMemeAdapter secondMemeAdapter;

    @SuppressLint("UseSparseArrays")
    public ThirdMemeAdapter(Context context, ArrayList<Meme> memes) {
        super(context, memes);
    }

    public ThirdMemeAdapter(Context context, String url) {
        super(context, url);
    }

    @Override
    public void onBindViewHolder(MemeAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Meme meme = memes.get(position);
    }

    public void setSecondMemeAdapter(SecondMemeAdapter secondMemeAdapter) {
        this.secondMemeAdapter = secondMemeAdapter;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.totalPosition < maxLoadedPosition) {
            Log.v("siriusmeme", holder.memeTitleTextView.getText() + " is being transferred to the SecondMemeAdapter");
            secondMemeAdapter.addMemeOnTop(memes.get(0));//usually it's the first element from top that gets recycled
            secondMemeAdapter.userIdsToUsernames.put(memes.get(0).getAuthorId().toHexString(), userIdsToUsernames.get(memes.get(0).getAuthorId().toHexString()));
            secondMemeAdapter.memeIdsToIsLiked.put(memes.get(0).getId().toHexString(), memeIdsToIsLiked.get(memes.get(0).getId().toHexString()));
            setViewedOnServer(memes.get(0));
            memes.remove(0);

            recyclerView.post(() -> notifyItemRemoved(0));
        }
    }

    private void setViewedOnServer(Meme meme) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, context.getString(R.string.set_viewed_url), future, future) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("auth_token", sharedPreferences.getString("auth_token", null));
                map.put("meme_id", meme.getId().toHexString());
                return map;
            }
        };


        VolleySingleton.getInstance(this.context).addToRequestQueue(request);
    }
}
