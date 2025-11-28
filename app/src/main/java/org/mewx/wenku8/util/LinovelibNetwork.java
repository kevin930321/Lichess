package org.mewx.wenku8.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.global.api.LinovelibAPI;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.global.api.LinovelibChapter;
import org.mewx.wenku8.global.api.LinovelibVolume;

import java.util.List;

/**
 * Network request handler for Linovelib
 * 
 * This class handles HTTP requests to linovelib.com
 * and parses the responses into data models.
 */
public class LinovelibNetwork {
    
    /**
     * Fetch and parse novel list from home page or category
     * @param sortBy Sort option
     * @param page Page number
     * @return List of novels, or null if error
     */
    @Nullable
    public static List<LinovelibNovel> getNovelList(@NonNull LinovelibAPI.NOVELSORTBY sortBy, int page) {
        try {
            String url = LinovelibAPI.getNovelListURL(sortBy, page);
            byte[] htmlBytes = LightNetwork.LightHttpDownload(url);
            
            if (htmlBytes == null) {
                return null;
            }
            
            String html = new String(htmlBytes, "UTF-8");
            return LinovelibParser.parseNovelList(html);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch and parse novel detail information
     * @param novelId Novel ID
     * @return LinovelibNovel with detailed info, or null if error
     */
    @Nullable
    public static LinovelibNovel getNovelDetail(int novelId) {
        try {
            String url = LinovelibAPI.getNovelDetailURL(novelId);
            byte[] htmlBytes = LightNetwork.LightHttpDownload(url);
            
            if (htmlBytes == null) {
                return null;
            }
            
            String html = new String(htmlBytes, "UTF-8");
            return LinovelibParser.parseNovelDetail(html);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch and parse chapter list for a novel
     * @param novelId Novel ID
     * @return List of volumes with chapters, or null if error
     */
    @Nullable
    public static List<LinovelibVolume> getChapterList(int novelId) {
        try {
            // IMPORTANT: Chapter list是在獨立的 /catalog 頁面！
            String url = LinovelibAPI.BASE_URL + "/novel/" + novelId + "/catalog";
            byte[] htmlBytes = LightNetwork.LightHttpDownload(url);
            
            if (htmlBytes == null) {
                return null;
            }
            
            String html = new String(htmlBytes, "UTF-8");
            return LinovelibParser.parseChapterList(html, novelId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch and parse chapter content
     * @param novelId Novel ID
     * @param chapterId Chapter ID
     * @return Chapter content as string, or null if error
     */
    @Nullable
    public static String getChapterContent(int novelId, int chapterId) {
        try {
            String url = LinovelibAPI.getChapterContentURL(novelId, chapterId);
            byte[] htmlBytes = LightNetwork.LightHttpDownload(url);
            
            if (htmlBytes == null) {
                return null;
            }
            
            String html = new String(htmlBytes, "UTF-8");
            return LinovelibParser.parseChapterContent(html);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Search novels by keyword
     * @param keyword Search keyword
     * @return List of novels, or null if error
     */
    @Nullable
    public static List<LinovelibNovel> searchNovels(@NonNull String keyword) {
        try {
            String url = LinovelibAPI.getSearchURL(keyword);
            byte[] htmlBytes = LightNetwork.LightHttpDownload(url);
            
            if (htmlBytes == null) {
                return null;
            }
            
            String html = new String(htmlBytes, "UTF-8");
            return LinovelibParser.parseSearchResults(html);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Download image from URL
     * @param imageUrl Image URL
     * @return Image bytes, or null if error
     */
    @Nullable
    public static byte[] downloadImage(@NonNull String imageUrl) {
        try {
            return LightNetwork.LightHttpDownload(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
