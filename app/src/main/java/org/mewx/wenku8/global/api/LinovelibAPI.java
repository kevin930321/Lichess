package org.mewx.wenku8.global.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Linovelib API Service
 * 
 * This class provides methods to construct URLs and handle requests for tw.linovelib.com
 * Since linovelib.com doesn't have an official API, we need to parse HTML pages.
 */
public class LinovelibAPI {
    
    // Base URLs
    public static final String BASE_URL = "https://tw.linovelib.com";
    public static final String NOVEL_BASE_URL = BASE_URL + "/novel";
    public static final String SEARCH_URL = BASE_URL + "/search";
    
    // Language setting (Linovelib only supports Traditional Chinese)
    public enum LANG {
        TC  // Traditional Chinese only
    }
    
    /**
     * Novel status enum
     */
    public enum STATUS {
        FINISHED,      // 已完結
        NOT_FINISHED   // 連載中
    }
    
    /**
     * Sort options for novel lists
     */
    public enum NOVELSORTBY {
        lastUpdate,    // 最近更新
        popular,       // 熱門
        newest,        // 最新
        allVisit       // 總點擊
    }
    
    /**
     * Get novel cover URL
     * @param novelId Novel ID
     * @return Full URL to the novel cover image
     */
    @NonNull
    public static String getCoverURL(int novelId) {
        // Cover images are typically stored in a predictable pattern
        // We'll need to extract this from the novel detail page
        return BASE_URL + "/cover/" + novelId + ".jpg";
    }
    
    /**
     * Get novel detail page URL
     * @param novelId Novel ID
     * @return Full URL to the novel detail page
     */
    @NonNull
    public static String getNovelDetailURL(int novelId) {
        return NOVEL_BASE_URL + "/" + novelId;
    }
    
    /**
     * Get chapter content URL
     * @param novelId Novel ID
     * @param chapterId Chapter ID
     * @return Full URL to the chapter content page
     */
    @NonNull
    public static String getChapterContentURL(int novelId, int chapterId) {
        return NOVEL_BASE_URL + "/" + novelId + "/" + chapterId + ".html";
    }
    
    /**
     * Get search URL with query
     * @param keyword Search keyword
     * @return Full URL for search
     */
    @NonNull
    public static String getSearchURL(@NonNull String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            return SEARCH_URL + "?keyword=" + encodedKeyword;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return SEARCH_URL;
        }
    }
    
    /**
     * Get home page URL with optional sort and page parameters
     * @param sortBy Sort option
     * @param page Page number (starting from 1)
     * @return Full URL for novel list
     */
    @NonNull
    public static String getNovelListURL(@NonNull NOVELSORTBY sortBy, int page) {
        String sortParam;
        switch (sortBy) {
            case lastUpdate:
                sortParam = "update";
                break;
            case popular:
                sortParam = "popular";
                break;
            case newest:
                sortParam = "new";
                break;
            case allVisit:
            default:
                sortParam = "click";
                break;
        }
        return BASE_URL + "/list/" + sortParam + "?page=" + page;
    }
    
    /**
     * Get category/genre URL
     * @param categoryId Category ID
     * @param page Page number
     * @return Full URL for category list
     */
    @NonNull
    public static String getCategoryURL(int categoryId, int page) {
        return BASE_URL + "/category/" + categoryId + "?page=" + page;
    }
    
    /**
     * Get the main page URL
     * @return Main page URL
     */
    @NonNull
    public static String getMainPageURL() {
        return BASE_URL;
    }
    
    /**
     * Check if a URL is from linovelib domain
     * @param url URL to check
     * @return true if URL is from linovelib
     */
    public static boolean isLinovelibURL(@Nullable String url) {
        return url != null && url.startsWith(BASE_URL);
    }
    
    /**
     * Extract novel ID from URL
     * @param url Novel detail page URL
     * @return Novel ID, or -1 if cannot parse
     */
    public static int extractNovelIdFromURL(@Nullable String url) {
        if (url == null || !url.contains(NOVEL_BASE_URL)) {
            return -1;
        }
        
        try {
            // URL format: https://tw.linovelib.com/novel/1234
            String[] parts = url.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i].matches("\\d+")) {
                    return Integer.parseInt(parts[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Extract chapter ID from URL
     * @param url Chapter content page URL
     * @return Chapter ID, or -1 if cannot parse
     */
    public static int extractChapterIdFromURL(@Nullable String url) {
        if (url == null) {
            return -1;
        }
        
        try {
            // URL format: https://tw.linovelib.com/novel/1234/56789.html
            String[] parts = url.replace(".html", "").split("/");
            if (parts.length >= 2) {
                String lastPart = parts[parts.length - 1];
                if (lastPart.matches("\\d+")) {
                    return Integer.parseInt(lastPart);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return -1;
    }
}
