package org.mewx.wenku8.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.global.api.LinovelibAPI;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.util.LinovelibNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Async task to load novel list from Linovelib
 */
public class LinovelibNovelListLoader extends AsyncTask<Void, Integer, List<LinovelibNovel>> {
    
    public interface LoaderListener {
        void onLoadStart();
        void onLoadSuccess(@NonNull List<LinovelibNovel> novels, int currentPage, int totalPage);
        void onLoadFailed(@Nullable String errorMessage);
    }
    
    private final LinovelibAPI.NOVELSORTBY sortBy;
    private final int page;
    private final LoaderListener listener;
    
    public LinovelibNovelListLoader(@NonNull LinovelibAPI.NOVELSORTBY sortBy, int page, @NonNull LoaderListener listener) {
        this.sortBy = sortBy;
        this.page = page;
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
    protected List<LinovelibNovel> doInBackground(Void... voids) {
        try {
            return LinovelibNetwork.getNovelList(sortBy, page);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(List<LinovelibNovel> novels) {
        super.onPostExecute(novels);
        
        if (listener == null) {
            return;
        }
        
        if (novels == null || novels.isEmpty()) {
            listener.onLoadFailed("Failed to load novel list");
        } else {
            // For now, we don't know the total pages from HTML parsing
            // We'll assume there are multiple pages until we get an empty result
            int totalPages = page + 1; // Assume at least one more page exists
            listener.onLoadSuccess(novels, page, totalPages);
        }
    }
}
