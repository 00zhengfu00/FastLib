package com.aries.library.fast.demo.module.mine;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.aries.library.fast.demo.R;
import com.aries.library.fast.demo.adapter.WidgetAdapter;
import com.aries.library.fast.demo.base.BaseItemTouchQuickAdapter;
import com.aries.library.fast.demo.constant.GlobalConstant;
import com.aries.library.fast.demo.constant.SPConstant;
import com.aries.library.fast.demo.entity.WidgetEntity;
import com.aries.library.fast.demo.module.WebViewActivity;
import com.aries.library.fast.demo.touch.ItemTouchHelperCallback;
import com.aries.library.fast.demo.touch.OnItemTouchHelperListener;
import com.aries.library.fast.manager.LoggerManager;
import com.aries.library.fast.manager.RxJavaManager;
import com.aries.library.fast.module.activity.FastRefreshLoadActivity;
import com.aries.library.fast.retrofit.FastObserver;
import com.aries.library.fast.util.SPUtil;
import com.aries.library.fast.util.ToastUtil;
import com.aries.ui.view.title.TitleBarView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @Author: AriesHoo on 2018/8/10 10:17
 * @E-Mail: AriesHoo@126.com
 * Function: 项目使用其他第三方库列表
 * Description:
 */
public class ThirdLibraryActivity extends FastRefreshLoadActivity<WidgetEntity> {

    @BindView(R.id.rv_contentFastLib) RecyclerView mRvContent;
    @BindView(R.id.smartLayout_rootFastLib) SmartRefreshLayout mSmartLayout;
    private BaseItemTouchQuickAdapter mAdapter;
    private int animationIndex = GlobalConstant.GLOBAL_ADAPTER_ANIMATION_VALUE;

    @Override
    public boolean isLoadMoreEnable() {
        return false;
    }

    @Override
    public void setTitleBar(TitleBarView titleBar) {
    }

    @Override
    public int getContentLayout() {
        return R.layout.fast_layout_title_refresh_recycler;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelperCallback()
                        .setAdapter(mAdapter)
                        .setOnItemTouchHelperListener(new OnItemTouchHelperListener() {
                            @Override
                            public void onStart(int start) {
                                mRefreshLayout.setEnableRefresh(false);
                                LoggerManager.i(TAG, "onStart-start:" + start);
                            }

                            @Override
                            public void onMove(int from, int to) {
                                LoggerManager.i(TAG, "onMove-from:" + from + ";to:" + to);
                            }

                            @Override
                            public void onMoved(int from, int to) {
                                LoggerManager.i(TAG, "onMoved-from:" + from + ";to:" + to);
                            }

                            @Override
                            public void onEnd(int star, int end) {
                                mRefreshLayout.setEnableRefresh(true);
                                LoggerManager.i(TAG, "onEnd-star:" + star + ";end:" + end);
                                ToastUtil.show("从---" + star + "---拖拽至---" + end + "---");
                            }
                        }));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public BaseQuickAdapter<WidgetEntity, BaseViewHolder> getAdapter() {
        mAdapter = new WidgetAdapter();
        animationIndex = (int) SPUtil.get(mContext, SPConstant.SP_KEY_ACTIVITY_ANIMATION_INDEX, animationIndex - 1) + 1;
        mAdapter.openLoadAnimation(animationIndex);
        return mAdapter;
    }

    @Override
    public void loadData(int page) {
        mAdapter.openLoadAnimation(animationIndex);
        List<WidgetEntity> list = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.arrays_library_list_title);
        String[] contents = getResources().getStringArray(R.array.arrays_library_list_content);
        String[] urls = getResources().getStringArray(R.array.arrays_library_list_url);
        for (int i = 0; i < titles.length; i++) {
            list.add(new WidgetEntity(titles[i], contents[i], urls[i]));
        }
        //此处只是为演示两种快速观察者使用、实际情况一般只会使用一种一般列表展示性使用FastObserver、
        // 类登录等待校验的使用FastLoadingObserver
        //当然可以自定义
        RxJavaManager.getInstance().getDelayObservable(list, 1)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new FastObserver<List<WidgetEntity>>() {
                    @Override
                    public void _onNext(List<WidgetEntity> entity) {
                        mAdapter.openLoadAnimation(animationIndex);
                        mStatusManager.showSuccessLayout();
                        mRefreshLayout.finishRefresh();
                        mRefreshLayout.finishLoadMore();
                        mAdapter.loadMoreComplete();
                        if (mRefreshLayout.isRefreshing()) {
                            mAdapter.setNewData(entity);
                        } else {
                            mAdapter.addData(entity);
                        }
                    }
                });
    }

    @Override
    public void onItemClicked(BaseQuickAdapter<WidgetEntity, ? extends BaseViewHolder> adapter, View view, int position) {
        super.onItemClicked(adapter, view, position);
        WidgetEntity entity = adapter.getItem(position);
        WebViewActivity.start(mContext, entity.url);
    }
}
