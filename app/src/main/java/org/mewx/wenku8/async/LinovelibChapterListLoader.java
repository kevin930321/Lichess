package org.mewx.wenku8.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.global.api.LinovelibVolume;
import org.mewx.wenku8.util.LinovelibNetwork;

import java.util.List;

/**
 * Async task to load chapter list from Linovelib
 */
public class LinovelibChapterListLoader extends AsyncTask<Integer, Void, List<LinovelibVolume>> {
    
    public interface LoaderListener {
        void onLoadStart();
        void onLoadSuccess(@NonNull List<LinovelibVolume> volumes);
        void onLoadFailed(@Nullable String errorMessage);
    }
    
    private final LoaderListener listener;
    
    public LinovelibChapterListLoader(@NonNull LoaderListener listener) {
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
    protected List<LinovelibVolume> doInBackground(Integer... novelIds) {
        if (novelIds == null || novelIds.length == 0) {
            return null;
        }
        
        try {
            return LinovelibNetwork.getChapterList(novelIds[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(List<LinovelibVolume> volumes) {
        super.onPostExecute(volumes);
        
        if (listener == null) {
            return;
        }
        
        if (volumes == null || volumes.isEmpty()) {
            listener.onLoadFailed("Failed to load chapter list");
        } else {
            listener.onLoadSuccess(volumes);
        }
    }
}
