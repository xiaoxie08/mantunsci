package snd.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import snd.adapter.AdapterTimerEdit;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBtimer;
import snd.util.JudgeDate;
import snd.util.LanguageHelper;
import snd.util.WheelMain;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTimerEdit extends ActivityBase
{
	private static final String TAG=ActivityTimerEdit.class.getName();

	private Resources resources;
	private long autoid = -1;

	TextView title;
	TextView type;
	TextView time;
	TextView week;
	TextView status;
	RelativeLayout weekItem;
	GridView grid;
	
	AdapterTimerEdit adapter;
	
	String[] types = {"单次定时","循环定时"};
	int typecheck;
	String[] statuses = {"开","关"};
	int statuscheck;
	String[] weekes = {"周日","周一","周二","周三","周四","周五","周六"};
	boolean[] weekcheck = {false,false,false,false,false,false,false};
	boolean[] weektmp = {false,false,false,false,false,false,false};
	BroadcastReceiver br;

	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
		Intent intent = getIntent();
		autoid = intent.getLongExtra("autoid", -1);
		
        setContentView(R.layout.timeredit);
        
        grid = (GridView) findViewById(R.id.list);
        weekItem = (RelativeLayout) findViewById(R.id.timer_repeat_item);
        title = (TextView) findViewById(R.id.addtimer_title);
        type = (TextView) findViewById(R.id.timer_type);
        time = (TextView) findViewById(R.id.timer_time);
        week = (TextView) findViewById(R.id.timer_repeat);
        status = (TextView) findViewById(R.id.timer_status);
        
        adapter = new AdapterTimerEdit(this);
        grid.setAdapter(adapter);
        
        initDatas();
        
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
	public void onDestroy() 
	{
		super.onDestroy();
		
		this.unregisterReceiver(br);
	}
	
	private void initDatas() {
		if(autoid == -1) { //新添
			title.setText(LanguageHelper.changeLanguageText("新添定时"));
			weekItem.setVisibility(View.GONE);
	    	
	    	SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		String date = sDateFormat.format(new Date()); 
    		
    		type.setText(LanguageHelper.changeLanguageText("单次定时"));
    		time.setText(date);
    		status.setText(LanguageHelper.changeLanguageText("开"));
		}else { //修改
			title.setText(LanguageHelper.changeLanguageText("修改定时"));
			
			DBtimer data = DBtimer.GetTimer(autoid);
			if(data != null) {
				String state = "";
				String weekday = data.getWeekday();
				if(weekday != null) {
					weekday = weekday.replace(",","");
				}
				if (weekday == null || weekday.length()==0 ||
	    				weekday.equals("-1") || weekday.equals("单次")) { //单次定时
					state = LanguageHelper.changeLanguageText("单次定时");
					typecheck = 0;
	    			weekItem.setVisibility(View.GONE);
	    		}else { //循环定时
	    			state = LanguageHelper.changeLanguageText("循环定时");
	    			typecheck = 1;
	    			weekItem.setVisibility(View.VISIBLE);
	    			
	    			if (weekday.length() < 7) {
	    				String cycle = "";
	    				for (int i=0; i<weekday.length(); i++) {
	    					char string = weekday.charAt(i);
	    					int position = getCyclePosition("周"+string);
	    					weekcheck[position] = true;
	    					if(cycle.length()==0) {
	    						cycle = "周"+string;
								cycle = LanguageHelper.changeLanguageText(cycle);
	    					}else {
	    						String week = "周"+string;
								week = LanguageHelper.changeLanguageText(week);
								cycle = cycle + "," + week;
							}
	    				}
	    				week.setText(cycle);
	    			}else {
	    				for(int i=0; i<weekcheck.length; i++) {
	    					weekcheck[i] = true;
	    				}
	    				week.setText(LanguageHelper.changeLanguageText("每天"));
	    			}
	    		}
				
				String str = data.getChannel();
	    		if(str == null) {
	    			str = "";
	    		}
	    		String[] channels = str.split("\\,");
	    		for(int i=0;i<channels.length;i++) {
	    			String channel = channels[i];
	    			if (channel != null && channel.length() > 0) {
	    				int addr = Integer.parseInt(channel);
	    				adapter.selected.add(addr);
	    			}
	    		}
	    		
	    		String date = data.getTime();
	    		if(date != null && date.length()>0) {
	    			String[] timeArray = date.split(":");
		            if(timeArray.length==3) {
		            	date = timeArray[0]+":"+timeArray[1];
		            }
	    		}
	    		
	    		String _status = data.getStatus();
	    	
	    		if(_status != null) {
	    			if(_status.equals("true")) {
	    				_status = "开";
		            }else if(_status.equals("false")) {
		            	_status = "关";
		            }
	    			
	    			statuscheck = (_status.equals("关")?1:0);

					_status = LanguageHelper.changeLanguageText(_status);
	    		}
	    		
	    		type.setText(state);
	    		time.setText(date);
	    		status.setText(_status);
			}
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：返回按钮事件
	public void backAction(View v) { 
		this.startActivity(new Intent(this, ActivityTimer.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) { 
		Breaker breaker = (Breaker)v.getTag();
		int curchannel = breaker.addr;
		
		if(adapter.selected.contains((Integer)curchannel)) {
			adapter.selected.remove((Integer)curchannel);
		}else
			adapter.selected.add(curchannel);
		
		refrenshAdapter();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：类型选择按钮事件
	public void typeSelectAction(View v) {
		String numbers[] = new String[types.length];
		for (int i=0; i<types.length; i++) {
			numbers[i] = LanguageHelper.changeLanguageText(types[i]);
		}

		ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
		new MyAlertDialog(ctw)
    	.setTitle(resources.getString(R.string.alert5))
        .setSingleChoiceItems(numbers, typecheck, new DialogInterface.OnClickListener()
        {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if(typecheck != which) {
					time.setText("");
					
					if(which == 0) { //单次定时
						SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				    	String date = sDateFormat.format(new Date());
				    	time.setText(date);
					}else { //循环定时
						SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
				    	String date = sDateFormat.format(new Date());
				    	time.setText(date);
					}
				}
				
				typecheck = which;
				String text = types[typecheck];
				type.setText(LanguageHelper.changeLanguageText(text));
				if(text.equals("循环定时"))
				{
					weekItem.setVisibility(View.VISIBLE);
				}
				else
				{
					week.setText("");
					weekItem.setVisibility(View.GONE);
				}
				dialog.dismiss();
			}
        })
        .show();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：时间选择按钮事件
	public void timeSelectAction(View v) {
		String type = types[typecheck];
		String date = time.getText().toString();
		DateFormat dateFormat = null;
		String format = "";
		boolean hasSelectTime;
		Calendar calendar = Calendar.getInstance();
		if(type != null && type.equals("循环定时")) { //循环定时
			dateFormat = new SimpleDateFormat("HH:mm");
			format = "HH:mm";
			hasSelectTime = false;
		}else { //单次定时
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			format = "yyyy-MM-dd HH:mm";
			hasSelectTime = true;
		}
		
		if(JudgeDate.isDate(date, format)){
			try {
				calendar.setTime(dateFormat.parse(date));
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View timepickerview = inflater.inflate(R.layout.timepicker, null);
				final WheelMain wheelMain = new WheelMain(timepickerview, hasSelectTime);
				DisplayMetrics metric = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metric);
				wheelMain.screenheight = metric.heightPixels;
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int hour = calendar.get(Calendar.HOUR_OF_DAY); //小时
				int minute = calendar.get(Calendar.MINUTE); //分
				wheelMain.initDateTimePicker(year, month, day, hour, minute);
				
				AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert6))
				.setView(timepickerview)
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String date = wheelMain.getTime();
						time.setText(date);
						
						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.show();
				
				alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
			}catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：重复选择按钮事件
	public void repeatSelectAction(View v) {
		String numbers[] = new String[weekes.length];
		for (int i = 0; i < weekes.length; i++) {
			weektmp[i] = weekcheck[i];
			numbers[i] = LanguageHelper.changeLanguageWeek(weekes[i]);
		}

		ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
		AlertDialog alert = new MyAlertDialog(ctw)
    	.setTitle(resources.getString(R.string.alert7))
    	.setMultiChoiceItems(numbers, weektmp, new OnMultiChoiceClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) 
			{
			}
		})
    	.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				String str="";
				int count = 0;
				for (int i = 0; i < weekes.length; i++)
                {
					weekcheck[i] = weektmp[i];
					if(weekcheck[i])
					{
						if(str.equals("")) {
							str = weekes[i];
							str = LanguageHelper.changeLanguageText(str);
						}else {
							String week = weekes[i];
							week = LanguageHelper.changeLanguageText(week);
							str += ","+week;
						}
						count++;
					}
                }
				if(count == 7) {
					str = LanguageHelper.changeLanguageText("每天");
				}
				week.setText(str);
				
				dialog.cancel();
			}
    	})
    	.setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		})
    	.show();
		
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：状态选择按钮事件
	public void statusSelectAction(View v) {
		String numbers[] = new String[statuses.length];
		for (int i=0; i<statuses.length; i++) {
			numbers[i] = LanguageHelper.changeLanguageText(statuses[i]);
		}

		ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
		new MyAlertDialog(ctw)
    	.setTitle(resources.getString(R.string.alert8))
        .setSingleChoiceItems(numbers, statuscheck, new DialogInterface.OnClickListener()
        {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				statuscheck = which;
				String text = statuses[statuscheck];
				text = LanguageHelper.changeLanguageText(text);
				status.setText(text);
				dialog.dismiss();
			}
        })
        .show();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：保存按钮事件
	public void saveAction(View v) { 
		if(check()) {
			try
	    	{
				String _time = time.getText().toString();
				String _status = status.getText().toString();
				
				if(_time != null && _time.length()>0 && _time.split(":").length<3) {
					_time = _time + ":00";
				}
				
				String cycle = week.getText().toString();
				if(cycle != null && cycle.length()>0) {
					cycle = cycle.replace("周", "");
					cycle = cycle.replace("Sun", "日");
					cycle = cycle.replace("Mon", "一");
					cycle = cycle.replace("Tue", "二");
					cycle = cycle.replace("Wed", "三");
					cycle = cycle.replace("Thu", "四");
					cycle = cycle.replace("Fri", "五");
					cycle = cycle.replace("Sat", "六");
					if(cycle.equals("每天") || cycle.equals("Every day"))
						cycle = "日,一,二,三,四,五,六";
				}else
					cycle = "";
				
				String ch = "";
				Collections.sort(adapter.selected);
		    	for(int c : adapter.selected)
		    	{
		    		if(ch.equals("")) ch+=c;
		    		else ch+=","+c;
		    	}
		    	
		    	if(_status.equals("开") || _status.equals("ON")) {
		    		_status = "true";
	            }else if(_status.equals("关") || _status.equals("OFF")) {
	            	_status = "false";
	            }
				
		    	if(autoid == -1) { //新添
		    		DBtimer rec = new DBtimer();
			    	rec.setMac(APP.MAC);
		    		rec.setChannel(ch);
		    		rec.setTime(_time);
		    		rec.setWeekday(cycle);
		    		rec.setStatus(_status);
		    		DBtimer.Update(rec);
		    	}else { //修改
		    		DBtimer rec = DBtimer.GetTimer(autoid);
					if(rec != null) {
						rec.setChannel(ch);
			    		rec.setTime(_time);
			    		rec.setWeekday(cycle);
			    		rec.setStatus(_status);
			    		DBtimer.UpdateTimer(rec);
					}
		    	}
	    		
		    	Toast.makeText(this, resources.getString(R.string.toast8), Toast.LENGTH_SHORT).show();
		    	this.backAction(null);
	    	}
	    	catch(Exception e)
	    	{
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    	}
		}
	}
	
	private void refrenshAdapter() {
		try
		{
			adapter.notifyDataSetChanged();
		}
		catch(Exception e){}
	}
	
	private int getCyclePosition(String name) {
		int position = 0;
		if(name.equals("周日")) {
			position = 0;
		}else if(name.equals("周一")) {
			position = 1;
		}else if(name.equals("周二")) {
			position = 2;
		}else if(name.equals("周三")) {
			position = 3;
		}else if(name.equals("周四")) {
			position = 4;
		}else if(name.equals("周五")) {
			position = 5;
		}else if(name.equals("周六")) {
			position = 6;
		}
		return position;
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		if(adapter.selected.isEmpty())
    	{
    		Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
    		return false;
    	}
		
    	if(time.getText().toString().equals(""))
    	{
    		Toast.makeText(this, resources.getString(R.string.toast5), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if(status.getText().toString().equals("")) {
			Toast.makeText(this, resources.getString(R.string.toast6), Toast.LENGTH_SHORT).show();
			return false;
		}
    	
    	if(typecheck == 1 && week.getText().toString().equals("")) {
    		Toast.makeText(this, resources.getString(R.string.toast7), Toast.LENGTH_SHORT).show();
			return false;
    	}
		
		return true;
	}
	
}
