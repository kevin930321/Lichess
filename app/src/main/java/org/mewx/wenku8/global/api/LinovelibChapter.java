package org.mewx.wenku8.global.api;

/**
 * Data model for Linovelib Chapter
 */
public class LinovelibChapter {
    public int cid;              // Chapter ID
    public String chapterName;   // Chapter title
    public int volumeId;         // Volume ID this chapter belongs to
    public int novelId;          // Novel ID
    public String chapterUrl;    // Full URL to chapter content
    public String content;       // Chapter content (loaded when needed)
    
    public LinovelibChapter() {
        this.cid = -1;
        this.chapterName = "";
        this.volumeId = -1;
        this.novelId = -1;
        this.chapterUrl = "";
        this.content = "";
    }
    
    /**
     * Get the full URL for this chapter
     * @return Chapter URL
     */
    public String getChapterUrl() {
        if (chapterUrl != null && !chapterUrl.isEmpty()) {
            return chapterUrl;
        }
        return LinovelibAPI.getChapterContentURL(novelId, cid);
    }
    
    @Override
    public String toString() {
        return "LinovelibChapter{" +
                "cid=" + cid +
                ", chapterName='" + chapterName + '\'' +
                ", volumeId=" + volumeId +
                ", novelId=" + novelId +
                '}';
    }
}
