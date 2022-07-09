package snd.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import snd.adapter.AKeySwitchAdapter;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBDelayedTask;
import snd.database.DBswitchsetting;
import snd.serialservice.SerialThread;
import snd.util.DelayedPicker;
import snd.util.Tooles;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityAKeySwitch extends ActivityBase {

	private Resources resources;

	private BroadcastReceiver br;
	public HashMap<String,String> curSelectDatas = new HashMap<String,String>();
	public HashMap<String,Breaker> channelDatas = new HashMap<String,Breaker>();
	
	private GridView listView;
	
	private AKeySwitchAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		resources = getResources();
	    
	    setContentView(R.layout.akeyswitch);
	    
	    listView = (GridView)findViewById(R.id.list);
	    
	    String channels = Tooles.getAKeySwitch(this);
	    if(channels != null && channels.length()>0) {
	    	String[] array = channels.split(",");
	    	for(int i=0; i<array.length; i++) {
    			String channel = array[i];
    			if (channel != null && channel.length() > 0) {
    				curSelectDatas.put(channel, channel);
    			}
	    	}
	    }
	    
	    adapter = new AKeySwitchAdapter(this);
    	listView.setAdapter(adapter);
	    
    	br = new BroadcastReceiver()
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
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		this.registerReceiver(br, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
		refrenshAdapter();
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
		
		this.unregisterReceiver(br);
		
		curSelectDatas.clear();
		channelDatas.clear();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：返回按钮事件
	public void backAction(View v) { 
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：一键开按钮事件
	public void switchOnAction(View v) { 
		if(check()) {
			AlertDialog alert = new MyAlertDialog(this)
       		.setTitle(resources.getString(R.string.tig7))
            .setMessage(resources.getString(R.string.alert9))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.tig16), new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                	 dialog.cancel();

					 String name1s = "";
					 String name2s = "";
					 String name3s = "";
					 String name4s = "";
                	 
                	 Set<String> keySet = curSelectDatas.keySet();
	        		 List<String> keys = new ArrayList<String>(keySet);
	        		 Collections.sort(keys, new Comparator<String>(){
						 @Override
						 public int compare(String address0, String address1) {
							 if(address0 != null && address1 != null
									 && Tooles.isNumber(address0) && Tooles.isNumber(address1)
									 && Integer.parseInt(address0) < Integer.parseInt(address1)) {
								 return -1;
							 }
							 return 1;
						 }
					 });
	        		 for(String channel : keys) {
	        			 int curchannel = Integer.parseInt(channel);
	        			 int visibility = 1;
						 int control = 1;
	        			 DBswitchsetting setting = DBswitchsetting.getSwitchSetting(curchannel);
	        			 if(setting != null){
	        				 visibility = setting.getVisibility();
							 control = setting.getControl();
	        			 }
	        			 
	        			 if(visibility == 1) {
	        			     if (control == 1) {
								 for(int i = 0; i<3; i++) {
									 SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, curchannel, 0);
								 }
							 }

							 Breaker breaker = channelDatas.get(channel);
	        				 if (breaker != null && breaker.title.length()>0) {
								 String name = breaker.title;
								 if(breaker.localLock) {
									 if(name1s.length() == 0) {
										 name1s = name;
									 } else
										 name1s = name1s+","+name;
								 }else if(!breaker.EnableNetCtrl) {
									 if(name2s.length() == 0) {
										 name2s = name;
									 } else
										 name2s = name2s+","+name;
								 }else if(control == 0) {
									 if(name3s.length() == 0) {
										 name3s = name;
									 } else
										 name3s = name3s+","+name;
								 }else if(breaker.remoteLock) {
									 if(name4s.length() == 0) {
										 name4s = name;
									 } else
										 name4s = name4s+","+name;
								 }
							 }

	        			 }	        			 
	        		 }

					 String message = "";
					 if(name1s.length()>0) {
						 message = name1s+"已被硬件锁定，请现场手动解除硬件锁定后再操作！";
					 }
					 if(name2s.length()>0) {
						 message = message+(message.length()>0?"\n":"")+name2s+"已因用电报警断电或现场关断，遥控功能关闭。请现场手动送电或远程解锁后恢复遥控功能！";
					 }
					 if(name3s.length()>0) {
						 message = message+(message.length()>0?"\n":"")+name3s+"已设置为不能遥控，请设置为能遥控再操作！";
					 }
					 if(name4s.length()>0) {
						 message = message+(message.length()>0?"\n":"")+name4s+"已被分闸锁定，请解除分闸锁定后再操作！";
					 }
					 if(message.length()>0 && APP.language.equals("zh")) {
						 Toast.makeText(ActivityAKeySwitch.this, message, Toast.LENGTH_LONG).show();
					 }
                 }
            })
            .setNeutralButton(resources.getString(R.string.tig17), new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                     dialog.cancel();

					 showDelayedPicker(1);
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
            alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(27);
			alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：一键关按钮事件
	public void switchOffAction(View v) { 
        if(check()) {
			AlertDialog alert = new MyAlertDialog(this)
       		.setTitle(resources.getString(R.string.tig7))
            .setMessage(resources.getString(R.string.alert10))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.tig16), new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                	 dialog.cancel();

					 String name1s = "";
					 String name2s = "";
                	 
                	 Set<String> keySet = curSelectDatas.keySet();
	        		 List<String> keys = new ArrayList<String>(keySet);
	        		 Collections.sort(keys, new Comparator<String>(){
						 @Override
						 public int compare(String address0, String address1) {
							 if(address0 != null && address1 != null
									 && Tooles.isNumber(address0) && Tooles.isNumber(address1)
									 && Integer.parseInt(address0) < Integer.parseInt(address1)) {
								 return 1;
							 }
							 return -1;
						 }
					 });
	        		 for(String channel : keys) {
	        			 int curchannel = Integer.parseInt(channel);
	        			 int visibility = 1;
						 int control = 1;

	        			 DBswitchsetting setting = DBswitchsetting.getSwitchSetting(curchannel);
	        			 if(setting != null){
	        				 visibility = setting.getVisibility();
							 control = setting.getControl();
	        			 }
	        			 
	        			 if(visibility == 1) {
							 if (control == 1) {
								 for(int i = 0; i<3; i++) {
									 SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, curchannel, 0);
								 }
							 }

							 Breaker breaker = channelDatas.get(channel);
							 if (breaker != null && breaker.title.length()>0) {
								 String name = breaker.title;
								 if(breaker.localLock) {
									 if(name1s.length() == 0) {
										 name1s = name;
									 } else
										 name1s = name1s+","+name;
								 }else if(control == 0) {
									 if(name2s.length() == 0) {
										 name2s = name;
									 } else
										 name2s = name2s+","+name;
								 }
							 }

	        			 }
	        		 }

					 String message = "";
					 if(name1s.length()>0) {
						 message = name1s+"已被硬件锁定，请现场手动解除硬件锁定后再操作！";
					 }
					 if(name2s.length()>0) {
						 message = message+(message.length()>0?"\n":"")+name2s+"已设置为不能遥控，请设置为能遥控再操作！";
					 }
					 if (message.length()>0 && APP.language.equals("zh")) {
						 Toast.makeText(ActivityAKeySwitch.this, message, Toast.LENGTH_LONG).show();
					 }
                 }
            })
			.setNeutralButton(resources.getString(R.string.tig17), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();

					showDelayedPicker(2);
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
			alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) { 
		Breaker breaker = (Breaker) v.getTag();
		if(breaker != null) {
			String channelId = breaker.addr+"";
			if(v.isSelected()) {
				curSelectDatas.remove(channelId);
			}else
				curSelectDatas.put(channelId, channelId);
			
			adapter.notifyDataSetChanged();
			
			Set<String> keySet = curSelectDatas.keySet();
			List<String> keys = new ArrayList<String>(keySet);
			Collections.sort(keys);
			String channels = "";
			for(String channel : keys) {
				if(channels.length()==0)
					channels = channel;
				else
					channels = channels+","+channel;
			}
			Tooles.saveAKeySwitch(channels, this);
		}
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		if(curSelectDatas == null || curSelectDatas.size() == 0) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private void refrenshAdapter() {
		try
		{
			adapter.notifyDataSetChanged();
		}
		catch(Exception e){}
	}

	private void showDelayedPicker(final int type) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View pickerview = inflater.inflate(R.layout.delayedpicker, null);
		final DelayedPicker picker = new DelayedPicker(pickerview);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		picker.screenheight = metric.heightPixels;
		picker.initPicker(0, 1, 0);

		AlertDialog alert = new MyAlertDialog(this)
		.setTitle(resources.getString(R.string.alert39))
		.setView(pickerview)
		.setCancelable(false)
		.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				int hour = picker.getHour();
				int minute = picker.getMin();
				int second = picker.getSecond();

				if (hour == 0 && minute == 0 && second == 0) {
					Toast.makeText(ActivityAKeySwitch.this, resources.getString(R.string.toast31), Toast.LENGTH_LONG).show();
				}else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(new Date());
					calendar.add(Calendar.HOUR_OF_DAY, hour);
					calendar.add(Calendar.MINUTE, minute);
					calendar.add(Calendar.SECOND, second);

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = format.format(calendar.getTime());
					int sceneId = (type == 1)?-1:-2;

					DBDelayedTask.insert(type, sceneId, time);
				}
			}
		})
		.setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
}
