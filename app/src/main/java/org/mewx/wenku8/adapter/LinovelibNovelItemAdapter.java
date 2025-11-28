package org.mewx.wenku8.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.mewx.wenku8.R;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.listener.MyItemClickListener;
import org.mewx.wenku8.listener.MyItemLongClickListener;

import java.util.List;

/**
 * Adapter for displaying Linovelib novel items in RecyclerView
 */
public class LinovelibNovelItemAdapter extends RecyclerView.Adapter<LinovelibNovelItemAdapter.ViewHolder> {
    
    private final List<LinovelibNovel> novels;
    private MyItemClickListener mItemClickListener;
    private MyItemLongClickListener mItemLongClickListener;
    
    private final DisplayImageOptions options;
    
    public LinovelibNovelItemAdapter(List<LinovelibNovel> novels) {
        this.novels = novels;
        
        // Image loading options
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty_image)
                .showImageOnFail(R.drawable.ic_empty_image)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(200))
                .build();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_novel_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LinovelibNovel novel = novels.get(position);
        
        // Set title
        holder.title.setText(novel.title);
        
        // Set author
        holder.author.setText(novel.author != null && !novel.author.isEmpty() 
                ? novel.author 
                : "Unknown Author");
        
        // Set status (if available)
        if (novel.status != null) {
            String statusText = novel.status.toString();
            holder.update.setText(statusText);
        } else {
            holder.update.setText("");
        }
        
        // Set intro (tags or other info)
        if (novel.tags != null && novel.tags.length > 0) {
            holder.introShort.setText(String.join(", ", novel.tags));
        } else if (novel.introduction != null && !novel.introduction.isEmpty()) {
            String shortIntro = novel.introduction.length() > 50 
                    ? novel.introduction.substring(0, 50) + "..." 
                    : novel.introduction;
            holder.introShort.setText(shortIntro);
        } else {
            holder.introShort.setText("");
        }
        
        // Load cover image
        if (novel.coverUrl != null && !novel.coverUrl.isEmpty()) {
            ImageLoader.getInstance().displayImage(novel.coverUrl, holder.cover, options);
        } else {
            holder.cover.setImageResource(R.drawable.ic_empty_image);
        }
    }
    
    @Override
    public int getItemCount() {
        return novels != null ? novels.size() : 0;
    }
    
    public void setOnItemClickListener(MyItemClickListener listener) {
        this.mItemClickListener = listener;
    }
    
    public void setOnItemLongClickListener(MyItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final ImageView cover;
        public final TextView title;
        public final TextView author;
        public final TextView update;
        public final TextView introShort;
        
        public ViewHolder(View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.novel_cover);
            title = itemView.findViewById(R.id.novel_title);
            author = itemView.findViewById(R.id.novel_author);
            update = itemView.findViewById(R.id.novel_update);
            introShort = itemView.findViewById(R.id.novel_intro_short);
            
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
        
        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                mItemLongClickListener.onItemLongClick(v, getAdapterPosition());
            }
            return true;
        }
    }
}
