package com.aries.library.fast.module.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aries.library.fast.FastConfig;
import com.aries.library.fast.R;
import com.aries.library.fast.util.FastUtil;
import com.aries.library.fast.util.ToastUtil;
import com.aries.ui.view.title.TitleBarView;
import com.aries.ui.widget.action.sheet.UIActionSheetView;
import com.just.library.AgentWeb;
import com.just.library.ChromeClientCallbackManager;

/**
 * Created: AriesHoo on 2017/8/5 19:42
 * Function: App内快速实现WebView功能
 * Desc:
 */
public abstract class FastWebActivity extends FastTitleActivity {

    protected LinearLayout lLayoutContainer;
    protected String url = "";
    protected String mCurrentUrl;
    protected AlertDialog mAlertDialog;
    protected AgentWeb mAgentWeb;
    protected AgentWeb.CommonAgentBuilder mAgentBuilder;
    protected UIActionSheetView mActionSheetView;

    protected static void start(Activity mActivity, Class<? extends FastWebActivity> activity, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        FastUtil.startActivity(mActivity, activity, bundle);
    }

    protected abstract void setAgentWeb(AgentWeb mAgentWeb, AgentWeb.CommonAgentBuilder mAgentBuilder);

    @Override
    public void beforeInitView() {
        lLayoutContainer = findViewByViewId(R.id.lLayout_containerFastWeb);
        url = getIntent().getStringExtra("url");
        mCurrentUrl = url;
        initAgentWeb();
        super.beforeInitView();

    }

    @Override
    public void beforeSetTitleBar(TitleBarView titleBar) {
        super.beforeSetTitleBar(titleBar);
        ImageView img = new ImageView(mContext);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageDrawable(FastUtil.getTintDrawble(getResources().getDrawable(R.drawable.fast_ic_close),
                FastConfig.getInstance(mContext).getTitleTextColor()));
        titleBar.setTitleMainTextMarquee(true)
                .setOnLeftTextClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                })
                .setOnRightTextClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showActionSheet();
                    }
                })
                .addLeftAction(titleBar.new ViewAction(img, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                }));
        titleBar.getTextView(Gravity.RIGHT).setCompoundDrawablesWithIntrinsicBounds(null, null,
                FastUtil.getTintDrawble(getResources().getDrawable(R.drawable.fast_ic_more),
                        FastConfig.getInstance(mContext).getTitleTextColor())
                , null);
    }

    @Override
    public int getContentLayout() {
        return R.layout.fast_activity_fast_web;
    }

    protected void initAgentWeb() {
        mAgentBuilder = AgentWeb.with(this)//
                .setAgentWebParent(lLayoutContainer, new LinearLayout.LayoutParams(-1, -1))//
                .useDefaultIndicator()//
                .defaultProgressBarColor();
        mAgentWeb = mAgentBuilder
                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        mCurrentUrl = view.getUrl();
                        mTitleBar.setTitleMainText(title);
                    }
                })
                .setWebChromeClient(null)
                .setWebViewClient(null)
                .setSecutityType(AgentWeb.SecurityType.default_check)
                .createAgentWeb()//
                .ready()
                .go(url);
        setAgentWeb(mAgentWeb, mAgentBuilder);
    }

    protected void showDialog() {
        if (mAlertDialog == null)
            mAlertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.fast_web_alert_title)
                    .setMessage(R.string.fast_web_alert_msg)
                    .setNegativeButton(R.string.fast_web_alert_left, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAlertDialog != null)
                                mAlertDialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.fast_web_alert_right, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (mAlertDialog != null)
                                mAlertDialog.dismiss();
                            mContext.finish();
                        }
                    }).create();
        mAlertDialog.show();
        //show之后可获取对应Button对象设置文本颜色--show之前获取对象为null
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    protected void showActionSheet() {
        if (mActionSheetView == null) {
            mActionSheetView = new UIActionSheetView(mContext, UIActionSheetView.STYLE_NORMAL)
                    .setItems(R.array.fast_arrays_web_more, new UIActionSheetView.OnSheetItemListener() {
                        @Override
                        public void onClick(int i) {
                            switch (i) {
                                case 0:
                                    mAgentWeb.getLoader().reload();
                                    break;
                                case 1:
                                    FastUtil.copyToClipboard(mContext, mCurrentUrl);
                                    ToastUtil.show(R.string.fast_copy_success);
                                    break;
                                case 2:
                                    FastUtil.startShareText(mContext, mCurrentUrl);
                                    break;
                            }
                        }
                    })
                    .setItemsTextColorResource(R.color.colorTabTextSelect)
                    .setCancelColorResource(R.color.colorTabTextSelect)
                    .setTitleColorResource(R.color.colorTabTextSelect)
                    .setCancelMessage(R.string.fast_cancel)
                    .setCancelMessageTextSize(TypedValue.COMPLEX_UNIT_DIP, 16)
                    .setItemsTextSize(TypedValue.COMPLEX_UNIT_DIP, 16)
                    .setBackgroundResource(android.R.color.darker_gray);
        }
        mActionSheetView.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mAgentWeb.back()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }
}
