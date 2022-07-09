package snd.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import snd.fragment.HistoryFragment;
import snd.fragment.RealFragment;
import snd.adapter.ViewPagerAdapter;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.util.HomeListener;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.HistoryPopupWindow;
import snd.view.tablayout.AppBarStateChangeListener;
import snd.view.tablayout.XTabLayout;

public class ActivitySwitchDetail extends AppCompatActivity {

	private int address;
	private Resources resources;

	private AppBarLayout appBarLayout;
	private RelativeLayout titleBgView;
	private RelativeLayout topBgView;

	private ImageView iconView;
	private TextView nameView;
	private TextView statusView;
	private TextView infoView;

	private RealFragment realFragment;
	private HistoryFragment historyFragment;

    private HomeListener homeListener;
	
	private MyBroadcastReciver myBroadcastRecive;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		APP.activity = this;
		ActivityBase.SleepTimeOut = 0;
		APP.isNewScreen = true;
		APP.isShowAlertDialog = false;

		View view = this.getWindow().getDecorView();
		Tooles.hideBottomUIMenu(view);

		Intent intent = getIntent();
		address = intent.getIntExtra("address", 1);

		resources = getResources();

		setContentView(R.layout.switch_detail);

		appBarLayout = (AppBarLayout)findViewById(R.id.app_bar_topic);
		titleBgView = (RelativeLayout)findViewById(R.id.titleBg);
		topBgView = (RelativeLayout)findViewById(R.id.topBg);

		iconView = (ImageView)findViewById(R.id.icon);
		nameView = (TextView)findViewById(R.id.name);
		statusView = (TextView)findViewById(R.id.status);
		infoView = (TextView)findViewById(R.id.info);

		ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
		XTabLayout mTabLayout = (XTabLayout) findViewById(R.id.tablayout);

		mTabLayout.setxTabDisplayNum(2);

		Breaker channelData = APP.distributbox.Breakers.get(address);
		realFragment = new RealFragment();
		realFragment.breaker = channelData;
		historyFragment = new HistoryFragment();
		historyFragment.breaker = channelData;

		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		viewPagerAdapter.addItem(realFragment, resources.getString(R.string.realtime_tig1));
		viewPagerAdapter.addItem(historyFragment, resources.getString(R.string.realtime_tig2));
		mViewPager.setAdapter(viewPagerAdapter);
		mTabLayout.setupWithViewPager(mViewPager);

		showData(channelData);

		appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
			@Override
			public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
				if( state == State.EXPANDED ) { //展开状态

				}else if(state == State.COLLAPSED){ //折叠状态

				}else { //中间状态

				}
			}
		});

		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Second1BroadcastReceiver.Msg_1S);
        myBroadcastRecive = new MyBroadcastReciver();
        this.registerReceiver(myBroadcastRecive, intentFilter);
	}

	@Override
	public void onUserInteraction()
	{
		super.onUserInteraction();

		ActivityBase.SleepTimeOut = 0;
		APP.isNewScreen = true;

		int systemBrightness = Tooles.getSystemBrightness(this);
		if(systemBrightness < 255) {
			Tooles.changeAppBrightness(this, 255);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		ActivityBase.SleepTimeOut = 0;
		APP.isNewScreen = true;

		APP.isWifiConnection = true;
	}

    @Override
    public void onResume()
    {
        super.onResume();

        ActivityBase.SleepTimeOut = 0;
        APP.isNewScreen = true;

        if (homeListener == null) {
            homeListener = new HomeListener(this);
            homeListener.setOnHomePressedListener(new HomeListener.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    //处理按了home后的事
                    //log.error("tag==Home键");

                    Intent intent = new Intent(ActivitySwitchDetail.this, ActivityMenu.class);
                    intent.putExtra("currentPage", 0);
                    startActivity(intent);
                    ActivitySwitchDetail.this.finish();
                }

                @Override
                public void onHomeLongPressed() {
                    //处理按了任务键后的事
                    //log.error("tag==任务切换键");
                }
            });
            homeListener.startWatch(); //注册广播
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (homeListener != null) {
            homeListener.stopWatch(); //注销广播
            homeListener = null;
        }
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(myBroadcastRecive != null) { //在结束时可取消广播
	        this.unregisterReceiver(myBroadcastRecive);
		}

        ActivityBase.SleepTimeOut = 0;
        APP.isNewScreen = true;

        if (homeListener != null) {
            homeListener.stopWatch(); //注销广播
            homeListener = null;
        }
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if(hasFocus && APP.popdialog != null && APP.popdialog.isShowing()) {
			APP.popdialog.dismiss();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		super.onTouchEvent(ev);

		ActivityBase.SleepTimeOut = 0;
		APP.isNewScreen = true;
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == 93) { //巡检按键
			APP.isGesturespassword = false;
			startActivity(new Intent(this, ActivityLeak.class));
			this.finish();
		}
		else if(keyCode == 92 || keyCode == 0) { //负载按键
			APP.isGesturespassword = false;
			startActivity(new Intent(this, ActivityPower.class));
			this.finish();
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //返回按键
			backAction(null);

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		Intent intent = new Intent(this, ActivityCtrlSwitch.class);
		intent.putExtra("address", address);
		this.startActivity(intent);
		this.finish();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：历史数据的日期按钮事件
	public void timeClickAction(View v) {
		new HistoryPopupWindow(this, v, historyFragment).show();
	}
	
	private void showData(Breaker channelData) {
		if(channelData != null) {
			int type = channelData.TYPE;
			String name = channelData.title;
			boolean oc = channelData.OpenClose;

			name = LanguageHelper.changeLanguageNode(name);
			String status1 = LanguageHelper.changeLanguageText("已通");
			String status2 = LanguageHelper.changeLanguageText("已断");

			nameView.setText(name);

			if (oc) { //已通
				titleBgView.setBackgroundColor(Color.parseColor("#fbdcc9"));
				topBgView.setBackgroundColor(Color.parseColor("#fbdcc9"));
				nameView.setTextColor(Color.parseColor("#ec1b28"));
				statusView.setText(status1);
			}else { //已断
				titleBgView.setBackgroundColor(Color.parseColor("#c6dd95"));
				topBgView.setBackgroundColor(Color.parseColor("#c6dd95"));
				nameView.setTextColor(Color.parseColor("#49a260"));
				statusView.setText(status2);
			}

			if (type == 1) { //1P开关
				iconView.setImageResource(R.drawable.p1);
			}else if (type == 2) { //2P开关
				iconView.setImageResource(R.drawable.p2);
			}else { //3/4P开关
				iconView.setImageResource(R.drawable.p3);
			}

			int specification = channelData.specification;
			if(specification == 64) specification = 63;
			String info = LanguageHelper.changeLanguageText("开关编号: ")+channelData.switchID;
			info += "\n"+LanguageHelper.changeLanguageText("产品型号: ")+Tooles.getModel(channelData);
			info += "\n"+LanguageHelper.changeLanguageText("额定电压: ")+channelData.lineType+"V";
			info += "\n"+LanguageHelper.changeLanguageText("额定电流: ")+specification+"A";
			infoView.setText(info);

			realFragment.showData(channelData);
		}
	}
	
	private class MyBroadcastReciver extends BroadcastReceiver {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		      String action = intent.getAction();
		      if(action.equals(Second1BroadcastReceiver.Msg_1S)) {
				  Breaker channelData = APP.distributbox.Breakers.get(address);
		  	      showData(channelData);
		      }
		  }
	}
	
}
