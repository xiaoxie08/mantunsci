package snd.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import snd.database.DBAdvertising;
import snd.model.AdvertisingData;
import snd.util.Common;
import snd.util.Tooles;

public class ActivityAdvertising2 extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private int currentVideoIndex; //当前播放到的视频段落数
    private List<AdvertisingData> VideoListQueue; //存放所有视频端
    private int mode = 1; //播放模式

    private Resources resources;
    private Window window;

    private MediaPlayer mPlayer;

    private SurfaceView sfv_show;
    private Button button;
    private TextView textView;
    private SurfaceHolder surfaceHolder;
    private Button soundButton;
    private RelativeLayout seekBarBg;
    private SeekBar seekBar;
    private RelativeLayout progressBar;

    private RelativeLayout linkBgView;
    private WebView webView;
    private Button deleteButton;

    private boolean isPlay = false;
    private boolean isClose = true;
    private AudioManager am;
    private Handler myHandler = new Handler();

    private MyBroadcastReciver myBroadcastRecive;

    private boolean isLocal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        window = getWindow();
        window.setBackgroundDrawable(resources.getDrawable(R.color.advertising_bg));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;//保持屏幕亮着
        lp.alpha = 1.0f;//透明度  0.0全透明 1.0不透明
        window.setAttributes(lp);

        View view = window.getDecorView();
        Tooles.hideBottomUIMenu(view);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;   //屏幕宽度（像素）
        int height = metric.heightPixels; //屏幕高度（像素）

        Intent intent = getIntent();
        currentVideoIndex = intent.getIntExtra("index", 0);
        VideoListQueue = (List<AdvertisingData>)intent.getSerializableExtra("datas");
        mode = intent.getIntExtra("mode", 1);

        setContentView(R.layout.advertising);

        sfv_show = (SurfaceView)findViewById(R.id.sfv_show);
        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.message);
        soundButton = (Button)findViewById(R.id.sound);
        seekBarBg = (RelativeLayout)findViewById(R.id.seedBarBg);
        seekBar = (SeekBar)findViewById(R.id.seedBar);
        progressBar = (RelativeLayout)findViewById(R.id.progressbar);

        linkBgView = (RelativeLayout)findViewById(R.id.linkBg);
        webView = (WebView)findViewById(R.id.webView);
        deleteButton = (Button)findViewById(R.id.delete);

        if (VideoListQueue.size()>1) {
            String text = (currentVideoIndex+1)+"/"+VideoListQueue.size();
            textView.setText(text);
        }

        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxValue = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); //得到听筒模式的最大值
        int value = am.getStreamVolume(AudioManager.STREAM_MUSIC); //得到听筒模式的当前值
        seekBar.setMax(maxValue);
        seekBar.setProgress(value);
        if(value == 0) soundButton.setBackgroundResource(R.drawable.ad_sound_off);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); //允许使用js
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //不使用缓存，只从网络获取数据

        button.setOnClickListener(this);
        soundButton.setOnClickListener(this);
        linkBgView.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        sfv_show.setOnTouchListener(touchListener);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        //初始化SurfaceHolder类，SurfaceView的控制器
        surfaceHolder = sfv_show.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(width, height); //显示的分辨率,不设置为视频默认

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.dismiss.advertising");
        myBroadcastRecive = new MyBroadcastReciver();
        registerReceiver(myBroadcastRecive, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myBroadcastRecive);

        if(isClose) close();

        if(myHandler != null) {
            myHandler.removeCallbacks(updateTime);
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }

        if(sfv_show != null) {
            sfv_show.setVisibility(View.GONE);
            sfv_show = null;
        }

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

        System.gc();
        System.runFinalization();
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (tag.equals("1")) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 0.0f;
            window.setAttributes(lp);

            this.finish();
        }else if (tag.equals("2")) {
            if (seekBarBg.isShown()) {
                seekBarBg.setVisibility(View.INVISIBLE);
            }else
                seekBarBg.setVisibility(View.VISIBLE);
        }else if (tag.equals("4")) {
            linkBgView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus  && isClose && !isCamera()) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 0.0f;
            window.setAttributes(lp);

            this.finish();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

        String source = getAdvertisingURL(currentVideoIndex);

        if (source.length() == 0) {
            nextPlayer(false);
            return;
        }

        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(source);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDisplay(surfaceHolder); //设置显示视频显示在SurfaceView上

            //监听播放错误
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //nextPlayer(true);
                    return false;
                }
            });

            //监听播放完成
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextPlayer(true);
                }
            });

            asyncPrepare();
        }catch (Exception e) {
            APP.isAdvertising = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：视频播放异步准备的行为
    private void asyncPrepare() {
        mPlayer.prepareAsync(); //准备：是操作硬件在播放，所以需要准备

        //监听异步准备，一旦准备完成，就会调用此方法
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);

                mPlayer.start(); //开始播放
                isPlay = true;

                setTime();
                myHandler.postDelayed(updateTime, 1000);
            }
        });
    }

    private void close() {
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

        if(mPlayer != null && isPlay) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        if(surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
            surfaceHolder.getSurface().release();
            surfaceHolder = null;
        }

        if(!isLocal) DBAdvertising.updateIsPlay();
    }

    private void nextPlayer(boolean mark) {
        currentVideoIndex++;

        if (mode == 2 && currentVideoIndex >= VideoListQueue.size()) currentVideoIndex = 0; //列表循环

        if (currentVideoIndex < VideoListQueue.size()) {
            isClose = false;
            close();

            myHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    Intent i = new Intent(ActivityAdvertising2.this, ActivityAdvertising1.class);
                    i.putExtra("index", currentVideoIndex);
                    i.putExtra("datas", (Serializable)VideoListQueue);
                    i.putExtra("mode", mode);
                    ActivityAdvertising2.this.startActivity(i);

                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.alpha = 0.0f;
                    window.setAttributes(lp);
                    ActivityAdvertising2.this.finish();
                }
            }, 1000);
        }else {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 0.0f;
            window.setAttributes(lp);

            this.finish();
        }
    }

    private int setTime() {
        ActivityBase.SleepTimeOut = 0;

        int time = mPlayer.getDuration()-mPlayer.getCurrentPosition();
        long min = TimeUnit.MILLISECONDS.toMinutes((long) time);
        long sec = TimeUnit.MILLISECONDS.toSeconds((long) time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) time));

        String tig1 = resources.getString(R.string.advertising_tig1); //广告剩余时间：
        String tig2 = resources.getString(R.string.advertising_tig2); //分
        String tig3 = resources.getString(R.string.advertising_tig3); //秒

        SpannableStringBuilder message = new SpannableStringBuilder("");
        SpannableString text1 = new SpannableString(tig1);
        text1.setSpan(new ForegroundColorSpan(0xffffffff), 0, text1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        message.append(text1);

        String[] strs = null;
        if(min>0) {
            strs = new String[]{min+"", tig2, sec+"", tig3};
        }else {
            strs = new String[]{sec+"", tig3};
        }
        for(int i=0; i<strs.length; i++) {
            SpannableString text2 = new SpannableString(strs[i]);
            if(i % 2 == 0) {
                text2.setSpan(new ForegroundColorSpan(0xffff0000), 0, text2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }else
                text2.setSpan(new ForegroundColorSpan(0xffffffff), 0, text2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            message.append(text2);
        }

        if (VideoListQueue.size()>1) {
            int index = currentVideoIndex+1;
            SpannableString text3 = new SpannableString("  "+index+"/"+VideoListQueue.size());
            text3.setSpan(new ForegroundColorSpan(0xffffffff), 0, text3.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            message.append(text3);
        }

        textView.setText(message);

        return time/1000;
    }

    private Runnable updateTime = new Runnable() {
        public void run() {
            if(mPlayer != null) {
                int time = setTime();
                if(time > 0) myHandler.postDelayed(this, 1000);
            }
        }
    };

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            float y = motionEvent.getY();
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (seekBarBg.isShown()) {
                    seekBarBg.setVisibility(View.INVISIBLE);
                }else if (!linkBgView.isShown() && webView.getUrl() != null && webView.getUrl().length()>0 && y > 100) {
                    if(currentVideoIndex < VideoListQueue.size()) {
                        AdvertisingData data = VideoListQueue.get(currentVideoIndex);
                        String link = data.getLink();
                        if (link != null && link.length()>0 && (link.contains("http:") || link.contains("https:"))
                                && !link.equals(webView.getUrl()) && !webView.getUrl().equals(link+"/")) {
                            webView.loadUrl(link);
                        }
                    }

                    linkBgView.setVisibility(View.VISIBLE);
                }
            }

            return true;
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);

            if (progress == 0) {
                soundButton.setBackgroundResource(R.drawable.ad_sound_off);
            }else
                soundButton.setBackgroundResource(R.drawable.ad_sound_on);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private String getAdvertisingURL(int index) {
        String advertisingURL = "";

        if(!(index < VideoListQueue.size())) return advertisingURL;

        AdvertisingData data = VideoListQueue.get(index);
        if (data != null) {
            String advertisingId = data.getAdvertisingId();
            String url = data.getUrl();
            String path = data.getPath();
            String link = data.getLink();
            isLocal = data.isLocal();

            if (path.length()>0 && new File(path).exists()) {
                advertisingURL = path;
            }else if(url.length()>0 && Tooles.checkNetState(this) != 0 && !isLocal) { //判断是否有网络
                advertisingURL = url;

                boolean isDownload = true;
                AdvertisingData data1 = DBAdvertising.getAdvertising(advertisingId);
                if (path.length()>0 && (data1 == null || data1.getPath().length() == 0)) {
                    isDownload = false; //当这条广告已删除或者广告链接已更新的时候，本次播放不下载到本地
                }

                if(isDownload) Common.goToDownloadAdvertising(data, url);
            }

            if (link != null && link.length()>0 && (link.contains("http:") || link.contains("https:"))) {
                webView.loadUrl(link);
            }
        }

        return advertisingURL;
    }

    private boolean isCamera() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (topActivity.getPackageName().equals("com.ichano.eg.streamer")
                        || topActivity.getClassName().equals("snd.ui.ActivitySleep")) {
                    return true;
                }
            }
        }catch(Exception e){
            return false;
        }

        return false;
    }

    private class MyBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("cn.dismiss.advertising")) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.alpha = 0.0f;
                window.setAttributes(lp);

                ActivityAdvertising2.this.finish();
            }
        }
    }

}
