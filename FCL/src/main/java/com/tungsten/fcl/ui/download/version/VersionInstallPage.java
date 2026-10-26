package com.tungsten.fcl.ui.download.version;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tungsten.fcl.R;
import com.tungsten.fcl.ui.UIManager;
import com.tungsten.fclcore.download.ComponentRemoteVersion;
import com.tungsten.fclcore.game.GameComponentType;
import com.tungsten.fcllibrary.component.ui.FCLPage;
import com.tungsten.fcllibrary.component.ui.PageFlows;
import com.tungsten.fcllibrary.component.view.FCLEditText;
import com.tungsten.fcllibrary.component.view.FCLImageButton;
import com.tungsten.fcllibrary.component.view.FCLProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 游戏版本安装页：只渲染 {@link DownloadListViewModel} 的清单状态并触发加载。
 * 清单数据与加载状态挂 Activity 级 ViewModel，页面随 ViewPager 回收重建时直接恢复，
 * 不重新网络拉取（刷新按钮才是显式 force 通道）。
 *
 * <p>发行策略（{@code GameVersionPolicy}）：只展示 1.8.9、1.21.11 及更高的正式版，
 * 快照/旧版本/愚人节版本不提供，因此类型复选框在此页已无意义，不再显示；
 * 组件安装页（{@link InstallerListPage}）继续使用同一布局的类型过滤。</p>
 */
public class VersionInstallPage extends FCLPage implements View.OnClickListener {

    private static final String VM_KEY = "download_list_game";

    private FCLImageButton refresh;
    private FCLImageButton failedRefresh;
    private FCLProgressBar progressBar;
    private RecyclerView recyclerView;
    private FCLEditText search;

    private RemoteVersionListAdapter.OnRemoteVersionSelectListener listener;
    private RemoteVersionListAdapter adapter;
    private DownloadListViewModel viewModel;

    /** 最近一次 Loaded 的全量清单（未按复选框/搜索过滤），过滤重算的数据源 */
    private List<ComponentRemoteVersion> versions = Collections.emptyList();

    public VersionInstallPage(Context context, int id) {
        super(context, id, R.layout.page_install_version);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        refresh = findViewById(R.id.refresh);
        failedRefresh = findViewById(R.id.failed_refresh);
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.list);
        search = findViewById(R.id.search);

        // 类型复选框是组件安装页的过滤器；游戏版本页受发行策略约束只展示正式版，
        // 留着就是永远点不出差异的假控件，直接隐藏
        findViewById(R.id.release).setVisibility(View.GONE);
        findViewById(R.id.snapshot).setVisibility(View.GONE);
        findViewById(R.id.old).setVisibility(View.GONE);
        findViewById(R.id.april_fools).setVisibility(View.GONE);

        refresh.setOnClickListener(this);
        failedRefresh.setOnClickListener(this);

        listener = remoteVersion -> {
            VersionInstallInfoPage page = new VersionInstallInfoPage(getContext(), FCLPage.PAGE_ID_TEMP, remoteVersion.getGameVersion());
            UIManager.getInstance().getDownloadUI().showTempPage(page);
        };

        search.stringProperty().addListener(observable -> applyFilters());

        adapter = new RemoteVersionListAdapter(getContext(), listener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(getActivity()).get(VM_KEY, DownloadListViewModel.class);
        PageFlows.observe(this, viewModel.getState(), this::render);
        viewModel.load("", GameComponentType.GAME, false);
    }

    private void render(DownloadListViewModel.State state) {
        if (state instanceof DownloadListViewModel.State.Loaded loaded) {
            versions = loaded.getVersions();
            applyFilters();
            recyclerView.setVisibility(View.VISIBLE);
            failedRefresh.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            refresh.setEnabled(true);
        } else if (state instanceof DownloadListViewModel.State.Failed failed) {
            progressBar.setVisibility(View.GONE);
            refresh.setEnabled(true);
            if (failed.getLastList().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                failedRefresh.setVisibility(View.VISIBLE);
            } else {
                // 保留已加载的旧列表（adapter 未被触碰），仅显示重试入口，瞬时网络故障不掩盖可用数据
                recyclerView.setVisibility(View.VISIBLE);
                failedRefresh.setVisibility(View.VISIBLE);
            }
        } else {
            // Idle / Loading：清单拉取中
            recyclerView.setVisibility(View.GONE);
            failedRefresh.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            refresh.setEnabled(false);
        }
    }

    /** 清单已由 {@link DownloadListViewModel} 按发行策略过滤（仅提供范围内的正式版），此处只做搜索过滤与排序 */
    private List<ComponentRemoteVersion> loadVersions() {
        return versions.stream()
                // getStringValue 在从未 setText 时为 null（原 refreshList 靠先 setText("") 掩盖）
                .filter(it -> it.getGameVersion().contains(Optional.ofNullable(search.getStringValue()).orElse("")))
                .sorted().collect(Collectors.toList());
    }

    private void applyFilters() {
        adapter.submitList(new ArrayList<>(loadVersions()));
    }


    @Override
    public void onClick(View view) {
        if (view == refresh || view == failedRefresh) {
            search.setText("");
            viewModel.load("", GameComponentType.GAME, true);
        }
    }
}
