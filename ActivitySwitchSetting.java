package snd.ui;

import snd.adapter.SwitchSettingAdapter;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBswitchsetting;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

public class ActivitySwitchSetting extends ActivityBase {

    private BroadcastReceiver myBroadcastRecive;
	
	private GridView listView;
	
	private SwitchSettingAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.switchsetting);
		
	    listView = (GridView)findViewById(R.id.switchsetting_list);
	    
	    adapter = new SwitchSettingAdapter(this);
    	listView.setAdapter(adapter);
    	
    	myBroadcastRecive = new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				if(intent.getAction().equals(Second1BroadcastReceiver.Msg_1S))
				{
					refrenshAdapter();
				}
		    }
		};
		registerReceiver(myBroadcastRecive, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(myBroadcastRecive != null) { //在结束时可取消广播
	        this.unregisterReceiver(myBroadcastRecive);
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityWattSeting.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：是否遥控选择按钮事件
	public void remoteAction(View v) { 
		final Breaker data = (Breaker)v.getTag();
		final int control = v.isSelected()?0:1;
		if(data != null) {
			String info = "是否设置此开关不能遥控？";
			if(control == 1) {
				info = "是否设置此开关能遥控？";
			}
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(info)
			.setTitle("提示")
			.setCancelable(false)
			.setPositiveButton("确认",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							
							DBswitchsetting.UpdateControl(data.addr, control);	
							data.control = control;
							refrenshAdapter();
						}
			        }
		    )
			.setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}
			)
			.show();
			
			final int message = this.getResources().getIdentifier("message","id","android") ;        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27);        
	        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
	        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：是否显示选择按钮事件
    public void displayAction(View v) { 
    	final Breaker data = (Breaker)v.getTag();
		final int visibility = v.isSelected()?0:1;
		if(data != null) {
			String info = "是否设置此开关不显示？";
			if(visibility == 1) {
				info = "是否设置此开关显示？";
			}
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(info)
			.setTitle("提示")
			.setCancelable(false)
			.setPositiveButton("确认",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							
							DBswitchsetting.UpdateVisibility(data.addr, visibility);
							data.visibility = visibility;
							refrenshAdapter();
						}
			        }
		    )
			.setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}
			)
			.show();
			
			final int message = this.getResources().getIdentifier("message","id","android") ;        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27);        
	        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
	        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}
	}
	
    private void refrenshAdapter() {
		try
		{
			adapter.notifyDataSetChanged();
		}
		catch(Exception e){}
	}
	
}
