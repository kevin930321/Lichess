package org.mewx.wenku8.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.global.api.LinovelibAPI;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.global.api.LinovelibChapter;
import org.mewx.wenku8.global.api.LinovelibVolume;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML Parser for tw.linovelib.com
 * 
 * This class parses HTML content from linovelib website
 * to extract novel information, chapter lists, and content.
 */
public class LinovelibParser {
    
    /**
     * Parse novel list from home page or category page HTML
     * @param html HTML content
     * @return List of novels
     */
    @NonNull
    public static List<LinovelibNovel> parseNovelList(@Nullable String html) {
        List<LinovelibNovel> novels = new ArrayList<>();
        
        if (html == null || html.isEmpty()) {
            return novels;
        }
        
        try {
            // Pattern to match novel items
            // Looking for: <a class="module-slide-a" href="/novel/1234">
            Pattern novelPattern = Pattern.compile(
                "<a[^>]+class=\"[^\"]*module-slide-a[^\"]*\"[^>]+href=\"(/novel/(\\d+))\"[^>]*>([\\s\\S]*?)</a>",
                Pattern.MULTILINE
            );
            
            Matcher matcher = novelPattern.matcher(html);
            
            while (matcher.find()) {
                String novelUrl = matcher.group(1);
                String novelIdStr = matcher.group(2);
                String novelContent = matcher.group(3);
                
                if (novelIdStr == null || novelContent == null) continue;
                
                int novelId = Integer.parseInt(novelIdStr);
                LinovelibNovel novel = new LinovelibNovel();
                novel.aid = novelId;
                novel.novelUrl = LinovelibAPI.BASE_URL + novelUrl;
                
                // Extract cover image
                Pattern imgPattern = Pattern.compile("<img[^>]+src=\"([^\"]+)\"");
                Matcher imgMatcher = imgPattern.matcher(novelContent);
                if (imgMatcher.find()) {
                    novel.coverUrl = imgMatcher.group(1);
                }
                
                // Extract title from figcaption
                Pattern titlePattern = Pattern.compile("<figcaption[^>]*>([^<]+)</figcaption>");
                Matcher titleMatcher = titlePattern.matcher(novelContent);
                if (titleMatcher.find()) {
                    novel.title = titleMatcher.group(1).trim();
                }
                
                // Extract author from <p><span>author</span></p>
                Pattern authorPattern = Pattern.compile("<p[^>]*><span[^>]*>([^<]+)</span>");
                Matcher authorMatcher = authorPattern.matcher(novelContent);
                if (authorMatcher.find()) {
                    novel.author = authorMatcher.group(1).trim();
                }
                
                if (novel.title != null && !novel.title.isEmpty()) {
                    novels.add(novel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return novels;
    }
    
    /**
     * Parse novel detail information from novel detail page
     * @param html HTML content of novel detail page
     * @return LinovelibNovel object with detailed information
     */
    @Nullable
    public static LinovelibNovel parseNovelDetail(@Nullable String html) {
        if (html == null || html.isEmpty()) {
            return null;
        }
        
        try {
            LinovelibNovel novel = new LinovelibNovel();
            
            // Extract novel ID from URL or data attribute
            Pattern idPattern = Pattern.compile("data-novel-id=\"(\\d+)\"");
            Matcher idMatcher = idPattern.matcher(html);
            if (idMatcher.find()) {
                novel.aid = Integer.parseInt(idMatcher.group(1));
            }
            
            // Extract title
            Pattern titlePattern = Pattern.compile("<h1[^>]*class=\"[^\"]*book-title[^\"]*\"[^>]*>([^<]+)</h1>");
            Matcher titleMatcher = titlePattern.matcher(html);
            if (titleMatcher.find()) {
                novel.title = titleMatcher.group(1).trim();
            }
            
            // Extract author
            Pattern authorPattern = Pattern.compile("<span[^>]*class=\"[^\"]*author[^\"]*\"[^>]*>([^<]+)</span>");
            Matcher authorMatcher = authorPattern.matcher(html);
            if (authorMatcher.find()) {
                novel.author = authorMatcher.group(1).trim();
            }
            
            // Extract cover
            Pattern coverPattern = Pattern.compile("<img[^>]+class=\"[^\"]*book-cover[^\"]*\"[^>]+src=\"([^\"]+)\"");
            Matcher coverMatcher = coverPattern.matcher(html);
            if (coverMatcher.find()) {
                novel.coverUrl = coverMatcher.group(1);
            }
            
            // Extract introduction
            Pattern introPattern = Pattern.compile("<div[^>]*class=\"[^\"]*book-intro[^\"]*\"[^>]*>([\\s\\S]*?)</div>");
            Matcher introMatcher = introPattern.matcher(html);
            if (introMatcher.find()) {
                String intro = introMatcher.group(1);
                // Remove HTML tags
                intro = intro.replaceAll("<[^>]+>", "");
                novel.introduction = intro.trim();
            }
            
            // Extract status
            if (html.contains("已完結") || html.contains("已完成")) {
                novel.status = LinovelibAPI.STATUS.FINISHED;
            } else if (html.contains("連載中")) {
                novel.status = LinovelibAPI.STATUS.NOT_FINISHED;
            }
            
            // Extract tags
            Pattern tagsPattern = Pattern.compile("<span[^>]*class=\"[^\"]*tag[^\"]*\"[^>]*>([^<]+)</span>");
            Matcher tagsMatcher = tagsPattern.matcher(html);
            List<String> tags = new ArrayList<>();
            while (tagsMatcher.find()) {
                tags.add(tagsMatcher.group(1).trim());
            }
            novel.tags = tags.toArray(new String[0]);
            
            return novel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse chapter list from catalog page
     * Based on actual HTML structure from https://tw.linovelib.com/novel/{id}/catalog
     * 
     * The catalog page uses a simple structure:
     * - Volume headers like "第一卷" or "第二卷"
     * - Chapter links in markdown format: [章節名稱](URL)
     * 
     * @param html HTML content from catalog page
     * @param novelId Novel ID
     * @return List of volumes containing chapters
     */
    @NonNull
    public static List<LinovelibVolume> parseChapterList(@Nullable String html, int novelId) {
        List<LinovelibVolume> volumes = new ArrayList<>();
        
        if (html == null || html.isEmpty()) {
            return volumes;
        }
        
        try {
            // Split by volume headers (e.g., "第一卷", "第二卷")
            // Pattern matches: - 第X卷 or ### 第X卷
            Pattern volumeHeaderPattern = Pattern.compile("[-#]+\\s*第(.+?)卷", Pattern.MULTILINE);
            Pattern chapterLinkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(https://tw\\.linovelib\\.com/novel/" + novelId + "/(\\d+)\\.html\\)");
            
            Matcher volumeMatcher = volumeHeaderPattern.matcher(html);
            
            int volumeIndex = 0;
            int lastVolumeEnd = 0;
            
            while (volumeMatcher.find()) {
                // If we found a previous volume, parse its chapters
                if (volumeIndex > 0) {
                    String volumeContent = html.substring(lastVolumeEnd, volumeMatcher.start());
                    parseChaptersForVolume(volumes.get(volumeIndex - 1), volumeContent, novelId, chapterLinkPattern);
                }
                
                // Create new volume
                LinovelibVolume volume = new LinovelibVolume();
                volume.vid = volumeIndex;
                volume.volumeName = "第" + volumeMatcher.group(1) + "卷";
                volume.chapters = new ArrayList<>();
                volumes.add(volume);
                
                lastVolumeEnd = volumeMatcher.end();
                volumeIndex++;
            }
            
            // Parse chapters for the last volume
            if (volumeIndex > 0 && lastVolumeEnd < html.length()) {
                String lastVolumeContent = html.substring(lastVolumeEnd);
                parseChaptersForVolume(volumes.get(volumeIndex - 1), lastVolumeContent, novelId, chapterLinkPattern);
            }
            
            // If no volumes found, try to parse all chapters as a single volume
            if (volumes.isEmpty()) {
                LinovelibVolume defaultVolume = new LinovelibVolume();
                defaultVolume.vid = 0;
                defaultVolume.volumeName = "全部章節";
                defaultVolume.chapters = new ArrayList<>();
                parseChaptersForVolume(defaultVolume, html, novelId, chapterLinkPattern);
                
                if (!defaultVolume.chapters.isEmpty()) {
                    volumes.add(defaultVolume);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return volumes;
    }
    
    /**
     * Helper method to parse chapters within a volume section
     */
    private static void parseChaptersForVolume(LinovelibVolume volume, String content, int novelId, Pattern chapterLinkPattern) {
        Matcher chapterMatcher = chapterLinkPattern.matcher(content);
        
        while (chapterMatcher.find()) {
            String chapterName = chapterMatcher.group(1);
            String chapterIdStr = chapterMatcher.group(2);
            
            if (chapterName != null && chapterIdStr != null) {
                LinovelibChapter chapter = new LinovelibChapter();
                chapter.cid = Integer.parseInt(chapterIdStr);
                chapter.chapterName = chapterName.trim();
                chapter.volumeId = volume.vid;
                chapter.novelId = novelId;
                volume.chapters.add(chapter);
            }
        }
    }
    
    /**
     * Parse chapter content from chapter page
     * @param html HTML content
     * @return Chapter content with processed images
     */
    @Nullable
    public static String parseChapterContent(@Nullable String html) {
        if (html == null || html.isEmpty()) {
            return null;
        }
        
        try {
            // Extract content from main reading area
            Pattern contentPattern = Pattern.compile(
                "<div[^>]*class=\"[^\"]*read-content[^\"]*\"[^>]*>([\\s\\S]*?)</div>",
                Pattern.MULTILINE
            );
            
            Matcher contentMatcher = contentPattern.matcher(html);
            if (!contentMatcher.find()) {
                return null;
            }
            
            String content = contentMatcher.group(1);
            
            if (content == null) {
                return null;
            }
            
            // Convert <img> tags to our internal format
            content = content.replaceAll(
                "<img[^>]+src=\"([^\"]+)\"[^>]*>",
                "<!--image-->$1<!--image-->"
            );
            
            // Remove script tags
            content = content.replaceAll("<script[^>]*>([\\s\\S]*?)</script>", "");
            
            // Convert <p> tags to line breaks
            content = content.replaceAll("<p[^>]*>", "");
            content = content.replaceAll("</p>", "\n");
            
            // Convert <br> tags
            content = content.replaceAll("<br[^>]*>", "\n");
            
            // Remove remaining HTML tags
            content = content.replaceAll("<[^>]+>", "");
            
            // Clean up HTML entities
            content = content.replace("&nbsp;", " ");
            content = content.replace("&quot;", "\"");
            content = content.replace("&amp;", "&");
            content = content.replace("&lt;", "<");
            content = content.replace("&gt;", ">");
            
            // Clean up excessive whitespace
            content = content.replaceAll("[ \\t]+", " ");
            content = content.replaceAll("\\n{3,}", "\n\n");
            
            return content.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse search results
     * @param html HTML content from search page
     * @return List of novels from search results
     */
    @NonNull
    public static List<LinovelibNovel> parseSearchResults(@Nullable String html) {
        // Search results use Google Custom Search, which has different structure
        // For now, we'll use the same parser as novel list
        return parseNovelList(html);
    }
    
    /**
     * Extract image URLs from chapter content
     * @param content Chapter content
     * @return List of image URLs
     */
    @NonNull
    public static List<String> extractImageURLs(@Nullable String content) {
        List<String> imageUrls = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return imageUrls;
        }
        
        Pattern imagePattern = Pattern.compile("<!--image-->([^<]+)<!--image-->");
        Matcher matcher = imagePattern.matcher(content);
        
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isEmpty()) {
                imageUrls.add(url);
            }
        }
        
        return imageUrls;
    }
}
