package snd.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import seasnake.loger.Logger;
import snd.util.HomeListener;
import snd.util.Tooles;

public class ActivityBase extends Activity
{
	private static final String TAG = ActivityBase.class.getName();
	private static Logger log = Logger.getLogger(ActivityBase.class); //日志

	public static int SleepTimeOut;
	public int currentPage = 0; //主页面当前显示的页数

	private HomeListener homeListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		APP.activity = this;
		SleepTimeOut = 0;
		APP.isNewScreen = true;
		APP.isShowAlertDialog = false;

		View view = this.getWindow().getDecorView();
		Tooles.hideBottomUIMenu(view);
	}
	
	@Override
	public void onUserInteraction()
	{
		super.onUserInteraction();

		SleepTimeOut = 0;
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

		SleepTimeOut = 0;
		APP.isNewScreen = true;

		APP.isWifiConnection = true;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		SleepTimeOut = 0;
		APP.isNewScreen = true;

		if (homeListener == null) {
			homeListener = new HomeListener(this);
			homeListener.setOnHomePressedListener(new HomeListener.OnHomePressedListener() {
				@Override
				public void onHomePressed() {
					//处理按了home后的事
					//log.error("tag==Home键");

					if(!(ActivityBase.this instanceof ActivitySleep)
						&& !(ActivityBase.this instanceof ActivityMenu)
						&& !(ActivityBase.this instanceof ActivityGesturesPassword)
						&& !(ActivityBase.this instanceof ActivityAutoAddress2)
						&& APP.isWifiConnection) {
						Intent intent = new Intent(ActivityBase.this, ActivityMenu.class);
						intent.putExtra("currentPage", currentPage);
						startActivity(intent);
						ActivityBase.this.finish();
					}else if(ActivityBase.this instanceof ActivityGesturesPassword && APP.isWifiConnection) {
						Intent intent = new Intent(ActivityBase.this, ActivitySleep.class);
						startActivity(intent);
						ActivityBase.this.finish();
					}
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
	public void onDestroy()
	{
		super.onDestroy();

		SleepTimeOut = 0;
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

		SleepTimeOut = 0;
		APP.isNewScreen = true;
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		log.error("------onKeyUp触摸按键key：" + keyCode);

		if(this instanceof ActivityGesturesPassword || this instanceof ActivityAutoAddress2) {
            return super.onKeyUp(keyCode, event);
        }

		if((this instanceof ActivitySleep) && APP.isGesturespassword
			&& (keyCode == 0 || keyCode == 92 || keyCode == 93)) {

			SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
			String password = preferences.getString("app_password", "");
			if(password != null && password.length()>0) {
				Intent intent = new Intent(this, ActivityGesturesPassword.class);
				if (keyCode == 0 || keyCode == 92) {
					intent.putExtra("type", 2);
				}else
					intent.putExtra("type", 3);
				this.startActivity(intent);
				this.finish();

				return super.onKeyUp(keyCode, event);
			}
		}

		if(keyCode == 93 && !(this instanceof ActivityLeak)) { //巡检按键
			APP.isGesturespassword = false;
			startActivity(new Intent(this, ActivityLeak.class));
			this.finish();
		}
		else if((keyCode == 92 || keyCode == 0) && !(this instanceof ActivityPower)) { //负载按键
			APP.isGesturespassword = false;
			startActivity(new Intent(this, ActivityPower.class));
			this.finish();
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		log.error("------onKeyDown触摸按键keyCode："+keyCode);

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //返回按键
			if(!(this instanceof ActivitySleep) && !(this instanceof ActivityMenu) && !(this instanceof ActivityGesturesPassword)) {
				backAction(null);
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void backAction(View v) {

	}

}
