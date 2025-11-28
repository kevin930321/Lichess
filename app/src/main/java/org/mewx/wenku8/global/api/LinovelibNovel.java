package org.mewx.wenku8.global.api;

import androidx.annotation.Nullable;

/**
 * Data model for Linovelib Novel
 */
public class LinovelibNovel {
    public int aid;                    // Novel ID
    public String title;               // Novel title
    public String author;              // Author name
    public String coverUrl;            // Cover image URL
    public String novelUrl;            // Novel detail page URL
    public String introduction;        // Novel introduction/description
    public LinovelibAPI.STATUS status; // Publishing status
    public String[] tags;              // Genre tags
    public String lastUpdate;          // Last update date
    public int totalHits;              // Total view count
    public int favoriteCount;          // Favorite count
    
    public LinovelibNovel() {
        this.aid = -1;
        this.title = "";
        this.author = "";
        this.coverUrl = "";
        this.novelUrl = "";
        this.introduction = "";
        this.status = LinovelibAPI.STATUS.NOT_FINISHED;
        this.tags = new String[0];
        this.lastUpdate = "";
        this.totalHits = 0;
        this.favoriteCount = 0;
    }
    
    @Override
    public String toString() {
        return "LinovelibNovel{" +
                "aid=" + aid +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", status=" + status +
                '}';
    }
}
