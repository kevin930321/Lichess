package org.mewx.wenku8.global.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for Linovelib Volume
 * A volume contains multiple chapters
 */
public class LinovelibVolume {
    public int vid;                      // Volume ID
    public String volumeName;            // Volume name/title
    public List<LinovelibChapter> chapters; // List of chapters in this volume
    
    public LinovelibVolume() {
        this.vid = -1;
        this.volumeName = "";
        this.chapters = new ArrayList<>();
    }
    
    /**
     * Get total number of chapters in this volume
     * @return Chapter count
     */
    public int getChapterCount() {
        return chapters != null ? chapters.size() : 0;
    }
    
    @Override
    public String toString() {
        return "LinovelibVolume{" +
                "vid=" + vid +
                ", volumeName='" + volumeName + '\'' +
                ", chapterCount=" + getChapterCount() +
                '}';
    }
}
