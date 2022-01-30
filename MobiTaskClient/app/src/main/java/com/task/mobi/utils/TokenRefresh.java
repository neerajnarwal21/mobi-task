package com.task.mobi.utils;

/**
 * Created by neeraj on 13/9/17.
 */
public class TokenRefresh {
    private static TokenRefresh tokenRefresh;
    private TokenListener tokenListener;

    public static TokenRefresh getInstance() {
        if (tokenRefresh == null) {
            tokenRefresh = new TokenRefresh();
        }
        return tokenRefresh;
    }

    public void setTokenListener(TokenListener tokenListener) {
        this.tokenListener = tokenListener;
    }

    public void tokenRefreshComplete() {
        if (tokenListener != null)
            tokenListener.onTokenRefresh();
    }

    public interface TokenListener {
        void onTokenRefresh();
    }
}
