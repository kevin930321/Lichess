package org.mewx.wenku8.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.util.LinovelibNetwork;

/**
 * Async task to load novel detail from Linovelib
 */
public class LinovelibNovelDetailLoader extends AsyncTask<Integer, Void, LinovelibNovel> {
    
    public interface LoaderListener {
        void onLoadStart();
        void onLoadSuccess(@NonNull LinovelibNovel novel);
        void onLoadFailed(@Nullable String errorMessage);
    }
    
    private final LoaderListener listener;
    
    public LinovelibNovelDetailLoader(@NonNull LoaderListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onLoadStart();
        }
    }
    
    @Override
    protected LinovelibNovel doInBackground(Integer... novelIds) {
        if (novelIds == null || novelIds.length == 0) {
            return null;
        }
        
        try {
            return LinovelibNetwork.getNovelDetail(novelIds[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(LinovelibNovel novel) {
        super.onPostExecute(novel);
        
        if (listener == null) {
            return;
        }
        
        if (novel == null) {
            listener.onLoadFailed("Failed to load novel detail");
        } else {
            listener.onLoadSuccess(novel);
        }
    }
}
