package org.mewx.wenku8.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mewx.wenku8.R;
import org.mewx.wenku8.async.LinovelibChapterListLoader;
import org.mewx.wenku8.global.api.LinovelibChapter;
import org.mewx.wenku8.global.api.LinovelibVolume;

import java.util.ArrayList;
import java.util.List;

/**
 * Linovelib Chapter Activity
 * Displays chapter list for a specific volume
 */
public class LinovelibChapterActivity extends BaseMaterialActivity {

    private int novelId;
    private int volumeId;
    private String volumeName;
    private String novelTitle;
    private List<LinovelibVolume> volumes = new ArrayList<>();
    private LinovelibVolume currentVolume;
    
    private RecyclerView recyclerView;
    private View progressBar;
    private ChapterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMaterialStyle(R.layout.layout_linovelib_chapter);

        // Get intent data
        novelId = getIntent().getIntExtra("aid", 0);
        volumeId = getIntent().getIntExtra("vid", 0);
        volumeName = getIntent().getStringExtra("volume_name");
        novelTitle = getIntent().getStringExtra("title");

        if (novelId == 0) {
            Toast.makeText(this, "Invalid novel ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        recyclerView = findViewById(R.id.chapter_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(volumeName != null ? volumeName : "Chapters");
        }

        // Load chapters
        loadChapters();
    }

    private void loadChapters() {
        progressBar.setVisibility(View.VISIBLE);
        
        LinovelibChapterListLoader loader = new LinovelibChapterListLoader(
            new LinovelibChapterListLoader.LoaderListener() {
                @Override
                public void onLoadStart() {
                    // Loading
                }

                @Override
                public void onLoadSuccess(@NonNull List<LinovelibVolume> volumeList) {
                    progressBar.setVisibility(View.GONE);
                    volumes = volumeList;
                    
                    // Find the specific volume
                    for (LinovelibVolume vol : volumes) {
                        if (vol.vid == volumeId) {
                            currentVolume = vol;
                            break;
                        }
                    }
                    
                    if (currentVolume != null && currentVolume.chapters != null) {
                        adapter = new ChapterAdapter(currentVolume.chapters);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(LinovelibChapterActivity.this, "No chapters found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onLoadFailed(@Nullable String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LinovelibChapterActivity.this,
                        "Failed to load chapters: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        );
        loader.execute(novelId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Chapter Adapter
    private class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {
        private final List<LinovelibChapter> chapters;

        ChapterAdapter(List<LinovelibChapter> chapters) {
            this.chapters = chapters;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.view_linovelib_chapter_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LinovelibChapter chapter = chapters.get(position);
            holder.chapterName.setText(chapter.chapterName);
            
            holder.itemView.setOnClickListener(v -> {
                // Navigate to reader activity
                Intent intent = new Intent(LinovelibChapterActivity.this, LinovelibReaderActivity.class);
                intent.putExtra("aid", novelId);
                intent.putExtra("cid", chapter.cid);
                intent.putExtra("title", novelTitle);
                intent.putExtra("chapter_name", chapter.chapterName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chapters != null ? chapters.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView chapterName;

            ViewHolder(View itemView) {
                super(itemView);
                chapterName = itemView.findViewById(R.id.chapter_name);
            }
        }
    }
}
