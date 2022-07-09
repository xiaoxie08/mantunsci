package snd.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import snd.serialservice.SerialThread;
import snd.view.MyAlertDialog;

public class ActivityAutoAddress2 extends ActivityBase {

	private Resources resources;
	private ImageView iconView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);

		setContentView(R.layout.autoaddress2);

		iconView = (ImageView)findViewById(R.id.icon);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int height = 3*dm.heightPixels/5;
		android.view.ViewGroup.LayoutParams lp = iconView.getLayoutParams();
		lp.width = 506*height/355;
		lp.height = height;
		iconView.setLayoutParams(lp);

		AnimationDrawable animaition = (AnimationDrawable) iconView.getBackground();
		animaition.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		if(SerialThread.START_ADDRESS) {
			AlertDialog alert = new MyAlertDialog(this)
					.setTitle(resources.getString(R.string.tig7))
					.setMessage(resources.getString(R.string.alert35))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();

							if(SerialThread.START_ADDRESS) SerialThread.END_ADDRESS = true;

							Intent intent = new Intent(ActivityAutoAddress2.this, ActivitySetting.class);
							intent.putExtra("currentPage", currentPage);
							ActivityAutoAddress2.this.startActivity(intent);
							ActivityAutoAddress2.this.finish();
						}
					})
					.setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();
						}
					})
					.show();

			final int message = this.getResources().getIdentifier("message","id","android") ;
			TextView messageTextView = (TextView) alert.findViewById(message);
			messageTextView.setTextSize(27);
			alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
			alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}else {
			Intent intent = new Intent(this, ActivitySetting.class);
			intent.putExtra("currentPage", currentPage);
			this.startActivity(intent);
			this.finish();
		}

	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：结束寻址按钮事件
	public void nextAction(View v) {
		if(SerialThread.START_ADDRESS) SerialThread.END_ADDRESS = true;

		Intent intent = new Intent(this, ActivitySetting.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}

}
