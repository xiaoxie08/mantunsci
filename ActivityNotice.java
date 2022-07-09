package snd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

public class ActivityNotice extends ActivityBase {

    private TextView titleView;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");

        setContentView(R.layout.notice_view);

        titleView = (TextView) findViewById(R.id.title);
        webView = (WebView)findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); //允许使用js
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //不使用缓存，只从网络获取数据

        if (title.length()>0) {
            titleView.setText(title);
        }

        webView.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(webView != null) {
            webView.stopLoading();
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.freeMemory();
            webView.pauseTimers();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivityNotices.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

}
