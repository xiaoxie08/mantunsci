package snd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import snd.serialservice.SerialThread;

public class ActivityAutoAddress1 extends ActivityBase {

	private ImageView iconView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);

		setContentView(R.layout.autoaddress1);

		iconView = (ImageView)findViewById(R.id.icon);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int height = 5*dm.heightPixels/8;
		android.view.ViewGroup.LayoutParams lp = iconView.getLayoutParams();
		lp.width = 353*height/721;
		lp.height = height;
		iconView.setLayoutParams(lp);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		Intent intent = new Intent(this, ActivitySetting.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}


	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：开始寻址按钮事件
	public void nextAction(View v) {
		SerialThread.START_ADDRESS = true;

		Intent intent = new Intent(this, ActivityAutoAddress2.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}

}
