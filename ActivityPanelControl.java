package snd.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import seasnake.loger.Logger;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.serialservice.SerialPanelThread;

public class ActivityPanelControl extends ActivityBase
{
	private static final String TAG = ActivityPanelControl.class.getName();
    private static Logger log = Logger.getLogger(ActivityPanelControl.class); //日志

	private ImageView imageView1;
	private ImageView imageView2;
	private ImageButton button;

    BroadcastReceiver bcReceiver;

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setContentView(R.layout.panelcontrol);

		imageView1 = (ImageView)findViewById(R.id.img1);
		imageView2 = (ImageView)findViewById(R.id.img2);
		button = (ImageButton)findViewById(R.id.img3);

		imageView2.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int image2Width = imageView2.getMeasuredWidth();
        if (APP.language.equals("en") && image2Width>387) {
			image2Width = 387;
		}
		imageView2.getLayoutParams().height = 380*image2Width/580+110;

		button.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int buttonWidth = button.getMeasuredWidth();
		if (APP.language.equals("en") && buttonWidth>378) {
			buttonWidth = 378;
		}
		button.getLayoutParams().height = 143*buttonWidth/567+30;

        bcReceiver = new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action  = intent.getAction();
				if(action.equals(Second1BroadcastReceiver.Msg_1S))
				{
					refrenshView();
				}
			}
        };
	}

	@Override
	public void onStart()
	{
		super.onStart();

		this.registerReceiver(bcReceiver, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
		refrenshView();
	}

	@Override
    public void onDestroy()
	{
    	super.onDestroy();

    	this.unregisterReceiver(bcReceiver);
    }

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：控制按钮事件
	public void controlAction(View v) {
		if(button.isSelected()) { //智慧面板当前状态是打开，现在进行闭合操作
			SerialPanelThread.CmdQueue(SerialPanelThread.CTR_OFF);
		}else { //智慧面板当前状态是闭合，现在进行打开操作
			SerialPanelThread.CmdQueue(SerialPanelThread.CTR_ON);
		}
	}

	private void refrenshView() {
		if(APP.panel_state == 1 && button.isSelected()) { //闭合
			imageView1.setSelected(false);
			imageView2.setSelected(false);
			button.setSelected(false);
		}else if(APP.panel_state == 2 && !button.isSelected()) { //打开
			imageView1.setSelected(true);
			imageView2.setSelected(true);
			button.setSelected(true);
		}
	}
	
}
