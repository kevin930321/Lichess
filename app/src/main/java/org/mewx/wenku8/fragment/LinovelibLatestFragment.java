package org.mewx.wenku8.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.mewx.wenku8.R;
import org.mewx.wenku8.activity.MainActivity;
import org.mewx.wenku8.activity.NovelInfoActivity;
import org.mewx.wenku8.adapter.LinovelibNovelItemAdapter;
import org.mewx.wenku8.async.LinovelibNovelListLoader;
import org.mewx.wenku8.global.api.LinovelibAPI;
import org.mewx.wenku8.global.api.LinovelibNovel;
import org.mewx.wenku8.listener.MyItemClickListener;
import org.mewx.wenku8.listener.MyItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment for displaying latest novels from Linovelib
 */
public class LinovelibLatestFragment extends Fragment implements MyItemClickListener, MyItemLongClickListener {

    // Components
    private MainActivity mainActivity = null;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private TextView mTextView;

    // Novel list
    private List<LinovelibNovel> listNovelInfo = new ArrayList<>();
    private LinovelibNovelItemAdapter mAdapter;
    private int currentPage = 1;
    private int totalPage = 1;

    // Loading state
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private int pastVisibleItems, visibleItemCount, totalItemCount;

    public LinovelibLatestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listNovelInfo = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        // Hide relay warning (not needed for Linovelib)
        View relayWarning = rootView.findViewById(R.id.relay_warning);
        if (relayWarning != null) {
            relayWarning.setVisibility(View.GONE);
        }

        // Get views
        mRecyclerView = rootView.findViewById(R.id.novel_item_list);
        mTextView = rootView.findViewById(R.id.list_loading_status);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Scroll listener for pagination
        mRecyclerView.addOnScrollListener(new MyOnScrollListener());

        // Retry button
        rootView.findViewById(R.id.btn_loading).setOnClickListener(v -> {
            if (!isLoading.compareAndSet(true, false)) {
                // Reload from first page
                currentPage = 1;
                totalPage = 1;
                listNovelInfo.clear();
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                loadNovelList(currentPage);
            }
        });

        // Initial load
        currentPage = 1;
        totalPage = 1;
        isLoading.set(false);
        loadNovelList(currentPage);

        return rootView;
    }

    /**
     * Load novel list from Linovelib
     */
    private void loadNovelList(int page) {
        if (!isLoading.compareAndSet(false, true)) {
            return; // Already loading
        }
        hideRetryButton();

        LinovelibNovelListLoader loader = new LinovelibNovelListLoader(
                LinovelibAPI.NOVELSORTBY.lastUpdate,
                page,
                new LinovelibNovelListLoader.LoaderListener() {
                    @Override
                    public void onLoadStart() {
                        // Loading started
                    }

                    @Override
                    public void onLoadSuccess(@NonNull List<LinovelibNovel> novels, int currentP, int totalP) {
                        if (!isAdded()) return;

                        // Add novels to list
                        int startPosition = listNovelInfo.size();
                        listNovelInfo.addAll(novels);

                        // Update adapter
                        if (mAdapter == null || mRecyclerView.getAdapter() == null) {
                            mAdapter = new LinovelibNovelItemAdapter(listNovelInfo);
                            mAdapter.setOnItemClickListener(LinovelibLatestFragment.this);
                            mAdapter.setOnItemLongClickListener(LinovelibLatestFragment.this);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyItemRangeInserted(startPosition, novels.size());
                        }

                        // Update page info
                        currentPage++;
                        totalPage = totalP;
                        isLoading.set(false);

                        // Hide loading view
                        if (mainActivity != null) {
                            View listLoadingView = mainActivity.findViewById(R.id.list_loading);
                            if (listLoadingView != null) {
                                listLoadingView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onLoadFailed(@NonNull String errorMessage) {
                        if (!isAdded()) return;

                        mTextView.setText(getString(R.string.system_parse_failed));
                        showRetryButton();
                        isLoading.set(false);
                    }
                }
        );
        loader.execute();
    }

    @Override
    public void onItemClick(View view, final int position) {
        if (position < 0 || position >= listNovelInfo.size()) {
            Toast.makeText(getActivity(), "ArrayIndexOutOfBoundsException: " + position + " in size " + listNovelInfo.size(), Toast.LENGTH_SHORT).show();
            return;
        }

        LinovelibNovel novel = listNovelInfo.get(position);
        
        // Go to detail activity
        Intent intent = new Intent(getActivity(), LinovelibNovelInfoActivity.class);
        intent.putExtra("aid", novel.aid);
        intent.putExtra("title", novel.title);
        
        if (Build.VERSION.SDK_INT < 21) {
            startActivity(intent);
        } else {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    Pair.create(view.findViewById(R.id.novel_cover), "novel_cover"),
                    Pair.create(view.findViewById(R.id.novel_title), "novel_title")
            );
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        onItemClick(view, position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
        isLoading.set(false);
    }

    /**
     * Scroll listener for infinite scrolling
     */
    private class MyOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = mLayoutManager.getChildCount();
            totalItemCount = mLayoutManager.getItemCount();
            pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

            // Load more when scrolled to near bottom (3 items remaining)
            if (!isLoading.get() && visibleItemCount + pastVisibleItems + 3 >= totalItemCount) {
                if (currentPage <= totalPage) {
                    Snackbar.make(mRecyclerView, getString(R.string.list_loading) + "(" + currentPage + "/" + totalPage + ")", Snackbar.LENGTH_SHORT).show();
                    loadNovelList(currentPage);
                } else {
                    Snackbar.make(mRecyclerView, getString(R.string.loading_done), Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showRetryButton() {
        if (mainActivity == null || mainActivity.findViewById(R.id.btn_loading) == null || !isAdded()) {
            return;
        }

        ((TextView) mainActivity.findViewById(R.id.btn_loading)).setText(getString(R.string.task_retry));
        mainActivity.findViewById(R.id.google_progress).setVisibility(View.GONE);
        mainActivity.findViewById(R.id.btn_loading).setVisibility(View.VISIBLE);
    }

    private void hideRetryButton() {
        if (mainActivity == null || mainActivity.findViewById(R.id.btn_loading) == null) {
            return;
        }

        mTextView.setText(getString(R.string.list_loading));
        mainActivity.findViewById(R.id.google_progress).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.btn_loading).setVisibility(View.GONE);
    }
}
