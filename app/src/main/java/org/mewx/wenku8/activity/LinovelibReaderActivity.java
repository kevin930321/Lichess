package org.mewx.wenku8.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mewx.wenku8.R;
import org.mewx.wenku8.async.LinovelibChapterContentLoader;

/**
 * Linovelib Reader Activity
 * Displays chapter content for reading
 */
public class LinovelibReaderActivity extends BaseMaterialActivity {

    private int novelId;
    private int chapterId;
    private String novelTitle;
    private String chapterName;
    
    private TextView tvChapterTitle;
    private TextView tvContent;
    private ScrollView scrollView;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMaterialStyle(R.layout.layout_linovelib_reader);

        // Get intent data
        novelId = getIntent().getIntExtra("aid", 0);
        chapterId = getIntent().getIntExtra("cid", 0);
        novelTitle = getIntent().getStringExtra("title");
        chapterName = getIntent().getStringExtra("chapter_name");

        if (novelId == 0 || chapterId == 0) {
            Toast.makeText(this, "Invalid chapter ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvChapterTitle = findViewById(R.id.chapter_title);
        tvContent = findViewById(R.id.chapter_content);
        scrollView = findViewById(R.id.scroll_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(novelTitle != null ? novelTitle : "Reader");
        }
        
        if (tvChapterTitle != null && chapterName != null) {
            tvChapterTitle.setText(chapterName);
        }

        // Load content
        loadChapterContent();
    }

    private void loadChapterContent() {
        progressBar.setVisibility(View.VISIBLE);
        tvContent.setText("Loading...");
        
        LinovelibChapterContentLoader loader = new LinovelibChapterContentLoader(
            novelId,
            chapterId,
            new LinovelibChapterContentLoader.LoaderListener() {
                @Override
                public void onLoadStart() {
                    // Loading
                }

                @Override
                public void onLoadSuccess(@NonNull String content) {
                    progressBar.setVisibility(View.GONE);
                    displayContent(content);
                }

                @Override
                public void onLoadFailed(@Nullable String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    tvContent.setText("Failed to load content: " + errorMessage);
                    Toast.makeText(LinovelibReaderActivity.this,
                        "Failed to load content", Toast.LENGTH_SHORT).show();
                }
            }
        );
        loader.execute();
    }

    private void displayContent(String content) {
        if (content == null || content.isEmpty()) {
            tvContent.setText("No content available");
            return;
        }

        // Simple text display
        // Note: Images are marked as <!--image:URL--> in the content
        // For now, we'll just display the text
        // In a full implementation, you would parse and display images
        
        String displayText = content.replaceAll("<!--image:.*?-->", "[Image]");
        tvContent.setText(displayText);
        
        // Scroll to top
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
