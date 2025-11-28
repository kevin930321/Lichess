package org.mewx.wenku8.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.util.LinovelibNetwork;

/**
 * Async task to load chapter content from Linovelib
 */
public class LinovelibChapterContentLoader extends AsyncTask<Void, Void, String> {
    
    public interface LoaderListener {
        void onLoadStart();
        void onLoadSuccess(@NonNull String content);
        void onLoadFailed(@Nullable String errorMessage);
    }
    
    private final int novelId;
    private final int chapterId;
    private final LoaderListener listener;
    
    public LinovelibChapterContentLoader(int novelId, int chapterId, @NonNull LoaderListener listener) {
        this.novelId = novelId;
        this.chapterId = chapterId;
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
    protected String doInBackground(Void... voids) {
        try {
            return LinovelibNetwork.getChapterContent(novelId, chapterId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(String content) {
        super.onPostExecute(content);
        
        if (listener == null) {
            return;
        }
        
        if (content == null || content.isEmpty()) {
            listener.onLoadFailed("Failed to load chapter content");
        } else {
            listener.onLoadSuccess(content);
        }
    }
}
