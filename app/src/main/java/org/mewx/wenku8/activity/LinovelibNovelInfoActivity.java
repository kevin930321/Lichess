package org.mewx.wenku8.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.mewx.wenku8.R;
import org.mewx.wenku8.async.LinovelibChapterListLoader;
import org.mewx.wenku8.async.LinovelibNovelDetailLoader;
import org.mewx.wenku8.global.GlobalConfig;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.global.api.LinovelibVolume;

import java.util.ArrayList;
import java.util.List;

/**
 * Linovelib Novel Info Activity
 * Displays novel details and chapter list
 */
public class LinovelibNovelInfoActivity extends BaseMaterialActivity {

    private int novelId;
    private String novelTitle;
    private LinovelibNovel novelInfo;
    private List<LinovelibVolume> volumes = new ArrayList<>();
    
    private boolean isLoading = false;
    
    // Views
    private ImageView ivCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvStatus;
    private TextView tvIntro;
    private TextView tvTags;
    private LinearLayout llVolumeList;
    private FloatingActionButton fabBookmark;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMaterialStyle(R.layout.layout_linovelib_novel_info);

        // Get intent data
        novelId = getIntent().getIntExtra("aid", 0);
        novelTitle = getIntent().getStringExtra("title");
        
        if (novelId == 0) {
            Toast.makeText(this, "Invalid novel ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UIL setting
        if (ImageLoader.getInstance() == null || !ImageLoader.getInstance().isInited()) {
            GlobalConfig.initImageLoader(this);
        }

        // Initialize views
        initViews();
        
        // Set initial title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(novelTitle != null ? novelTitle : "Novel Info");
        }

        // Load novel data
        loadNovelInfo();
    }

    private void initViews() {
        ivCover = findViewById(R.id.novel_cover);
        tvTitle = findViewById(R.id.novel_title);
        tvAuthor = findViewById(R.id.novel_author);
        tvStatus = findViewById(R.id.novel_status);
        tvIntro = findViewById(R.id.novel_intro);
        tvTags = findViewById(R.id.novel_tags);
        llVolumeList = findViewById(R.id.volume_list);
        fabBookmark = findViewById(R.id.fab_bookmark);
        progressBar = findViewById(R.id.progress_bar);

        // Set bookmark icon based on local status
        updateBookmarkIcon();
        
        // Bookmark click listener
        fabBookmark.setOnClickListener(v -> toggleBookmark());
        
        // Author click - make it underlined
        if (tvAuthor != null) {
            tvAuthor.setPaintFlags(tvAuthor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    private void loadNovelInfo() {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        
        // Load novel details
        LinovelibNovelDetailLoader detailLoader = new LinovelibNovelDetailLoader(
            new LinovelibNovelDetailLoader.LoaderListener() {
                @Override
                public void onLoadStart() {
                    // Already showing progress
                }

                @Override
                public void onLoadSuccess(@NonNull LinovelibNovel novel) {
                    novelInfo = novel;
                    displayNovelInfo();
                    loadChapterList();
                }

                @Override
                public void onLoadFailed(@Nullable String errorMessage) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LinovelibNovelInfoActivity.this, 
                        "Failed to load novel: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        );
        detailLoader.execute(novelId);
    }

    private void displayNovelInfo() {
        if (novelInfo == null) return;

        // Title
        if (tvTitle != null && novelInfo.title != null) {
            tvTitle.setText(novelInfo.title);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(novelInfo.title);
            }
        }

        // Author
        if (tvAuthor != null && novelInfo.author != null) {
            tvAuthor.setText(novelInfo.author);
        }

        // Status
        if (tvStatus != null && novelInfo.status != null) {
            tvStatus.setText(novelInfo.status.toString());
        }

        // Introduction
        if (tvIntro != null && novelInfo.introduction != null) {
            tvIntro.setText(novelInfo.introduction);
        }

        // Tags
        if (tvTags != null && novelInfo.tags != null && novelInfo.tags.length > 0) {
            tvTags.setText(String.join(" · ", novelInfo.tags));
        }

        // Cover image
        if (ivCover != null && novelInfo.coverUrl != null) {
            ImageLoader.getInstance().displayImage(novelInfo.coverUrl, ivCover);
        }
    }

    private void loadChapterList() {
        LinovelibChapterListLoader chapterLoader = new LinovelibChapterListLoader(
            new LinovelibChapterListLoader.LoaderListener() {
                @Override
                public void onLoadStart() {
                    // Still loading
                }

                @Override
                public void onLoadSuccess(@NonNull List<LinovelibVolume> volumeList) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    volumes = volumeList;
                    displayVolumeList();
                }

                @Override
                public void onLoadFailed(@Nullable String errorMessage) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LinovelibNovelInfoActivity.this,
                        "Failed to load chapters: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        );
        chapterLoader.execute(novelId);
    }

    private void displayVolumeList() {
        if (llVolumeList == null || volumes == null || volumes.isEmpty()) {
            return;
        }

        llVolumeList.removeAllViews();

        for (final LinovelibVolume volume : volumes) {
            View volumeView = getLayoutInflater().inflate(R.layout.view_linovelib_volume_item, llVolumeList, false);
            
            TextView tvVolumeName = volumeView.findViewById(R.id.volume_name);
            TextView tvChapterCount = volumeView.findViewById(R.id.chapter_count);
            
            tvVolumeName.setText(volume.volumeName);
            tvChapterCount.setText(volume.getChapterCount() + " 章節");
            
            volumeView.setOnClickListener(v -> {
                // Navigate to chapter list activity
                Intent intent = new Intent(LinovelibNovelInfoActivity.this, LinovelibChapterActivity.class);
                intent.putExtra("aid", novelId);
                intent.putExtra("vid", volume.vid);
                intent.putExtra("volume_name", volume.volumeName);
                intent.putExtra("title", novelInfo != null ? novelInfo.title : novelTitle);
                startActivity(intent);
            });
            
            llVolumeList.addView(volumeView);
        }
    }

    private void toggleBookmark() {
        if (novelInfo == null) {
            Toast.makeText(this, "Please wait for novel info to load", Toast.LENGTH_SHORT).show();
            return;
        }

        if (GlobalConfig.testInLocalBookshelf(novelId)) {
            // Remove from bookshelf
            GlobalConfig.removeFromLocalBookshelf(novelId);
            Toast.makeText(this, "Removed from bookshelf", Toast.LENGTH_SHORT).show();
        } else {
            // Add to bookshelf
            GlobalConfig.addToLocalBookshelf(novelId);
            Toast.makeText(this, "Added to bookshelf", Toast.LENGTH_SHORT).show();
        }
        
        updateBookmarkIcon();
    }

    private void updateBookmarkIcon() {
        if (fabBookmark == null) return;
        
        if (GlobalConfig.testInLocalBookshelf(novelId)) {
            fabBookmark.setImageResource(R.drawable.ic_favorate_pressed);
        } else {
            fabBookmark.setImageResource(R.drawable.ic_favorate_normal);
        }
        fabBookmark.setColorFilter(getResources().getColor(R.color.default_white), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (Build.VERSION.SDK_INT < 21) {
                finish();
            } else {
                finishAfterTransition();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < 21) {
            finish();
        } else {
            finishAfterTransition();
        }
    }
}
