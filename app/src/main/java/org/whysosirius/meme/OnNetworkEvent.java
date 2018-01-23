package org.whysosirius.meme;

/**
 * Created by User on 23.01.2018.
 */

public class OnNetworkEvent {
    private boolean networkState;
    public OnNetworkEvent(boolean networkState) {
        this.networkState = networkState;
    }
    public boolean isNetworkState() {
        return networkState;
    }
}
