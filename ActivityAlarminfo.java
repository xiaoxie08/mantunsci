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

import snd.adapter.AdapterAlarminfo;
import snd.broadcastreceiver.SndBroadcastReceiver;
import snd.database.AlarmHeaderData;
import snd.database.DBalarminfo;
import snd.util.LanguageHelper;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityAlarminfo extends ActivityBase
{
	private static final String TAG=ActivityAlarminfo.class.getName();

	private Resources resources;
	
	private ListView list;
	
	private LinearLayout bottomView;
	public Button editButton;
	private Button deleteButton;
	private Button selectButton;
	private ProgressBar progressBarView;
	
	AdapterAlarminfo adapter;
	LoadDataAsyncTask loaddata;
	
	List<DBalarminfo> info = new ArrayList<DBalarminfo>();
	public HashMap<Long,DBalarminfo> curSelectDatas = new HashMap<Long,DBalarminfo>();
	
    BroadcastReceiver bcReceiver;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
        setContentView(R.layout.alarminfolist);
        
        list=(ListView)findViewById(R.id.list);
        
        editButton = (Button)findViewById(R.id.edit_button);
	    bottomView = (LinearLayout)findViewById(R.id.bottom);
	    deleteButton = (Button)findViewById(R.id.button_delete);
	    selectButton = (Button)findViewById(R.id.button_select);
		progressBarView = (ProgressBar)findViewById(R.id.progressbar);

		String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
		String tig2 = resources.getString(R.string.tig2); //全选
	    deleteButton.setText(tig1);
	    selectButton.setText(tig2);
        
        adapter = new AdapterAlarminfo(this);
        list.setAdapter(adapter);
        
        bcReceiver=new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				String action  = intent.getAction();
				if(action.equals(SndBroadcastReceiver.Msg_POPINFO))
				{
					loadDatas();
				}
			}
        };
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		this.registerReceiver(bcReceiver, new IntentFilter(SndBroadcastReceiver.Msg_POPINFO));
		
		loadDatas();
	}
	
	@Override
    public void onDestroy()
	{
    	super.onDestroy();
    	
    	this.unregisterReceiver(bcReceiver);
    	
    	if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
    	
    	curSelectDatas.clear();
    	info.clear();
    	adapter.datas.clear();
    }
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：加载数据
	private void loadDatas() {
		if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
		loaddata=new LoadDataAsyncTask();
	    loaddata.execute(new String[]{"where type='警告'"});
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：编辑按钮事件
	public void editAction(View v) { 
		editButton.setSelected(!editButton.isSelected());
    	if(editButton.isSelected()) {
    		int color = 0x00000000;
			String tig = resources.getString(R.string.tig4); //取消
    		editButton.setBackgroundColor(color);
    		editButton.setText(tig);
			editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_height);
    		
    		bottomView.setVisibility(View.VISIBLE);
    	}else {
    		editButton.setBackgroundResource(R.drawable.icon_delete_normal);
    		editButton.setText("");
			editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_button);
    		
    		curSelectDatas.clear();

			String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
			String tig2 = resources.getString(R.string.tig2); //全选
    		
    		deleteButton.setText(tig1);
    		selectButton.setText(tig2);
    		selectButton.setSelected(false);
    		bottomView.setVisibility(View.GONE);
    	}
    	
    	if(adapter != null) adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：全选或全不选按钮事件
	public void selectAction(View v) { 
		if(info == null) return;
		curSelectDatas.clear();
		
		selectButton.setSelected(!selectButton.isSelected());
		if(selectButton.isSelected()) {
			String tig = resources.getString(R.string.tig3); //全不选
			selectButton.setText(tig);
			
			for(DBalarminfo data : info) {
				curSelectDatas.put(data.getAutoid(), data);
			}
		}else {
			String tig = resources.getString(R.string.tig2); //全选
			selectButton.setText(tig);
		}

		String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
		deleteButton.setText(tig);
		adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除选择的信息按钮事件
	public void deleteAllAction(View v) { 
		if(curSelectDatas.size()>0) {
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(resources.getString(R.string.alert1))
			.setTitle(resources.getString(R.string.tig7))
			.setPositiveButton(resources.getString(R.string.tig5),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
							deleteInfo();
						}
			        }
		    )
			.setNegativeButton(resources.getString(R.string.tig6),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}
			).show();
			
			int message = this.getResources().getIdentifier("message","id","android");        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27); 
			alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}else {
			Toast.makeText(this, resources.getString(R.string.toast2), Toast.LENGTH_SHORT).show();
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：删除按钮事件
	public void deleteAction(View v) {
		final DBalarminfo data = (DBalarminfo) v.getTag();
		if(data != null) {
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(resources.getString(R.string.alert2))
			.setTitle(resources.getString(R.string.tig7))
			.setPositiveButton(resources.getString(R.string.tig5),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
							
							DBalarminfo.VirtualDelete(data.getAutoid());
							loadDatas();
							Toast.makeText(ActivityAlarminfo.this, resources.getString(R.string.toast1), Toast.LENGTH_SHORT).show();
						}
			        }
		    )
			.setNegativeButton(resources.getString(R.string.tig6),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}
			).show();
			
			int message = this.getResources().getIdentifier("message","id","android");        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27); 
			alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：选中事件
	public void itemClickAction(View v) { 
		DBalarminfo info = (DBalarminfo) v.getTag();
		if(editButton.isSelected() && info != null) {
			if(!curSelectDatas.containsKey(info.getAutoid())) {
				curSelectDatas.put(info.getAutoid(), info);
			}else
				curSelectDatas.remove(info.getAutoid());

			String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
			deleteButton.setText(tig);
			adapter.notifyDataSetChanged();
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除选中的信息
	private void deleteInfo() {
		for(DBalarminfo info : curSelectDatas.values()) {
			DBalarminfo.VirtualDelete(info.getAutoid());
		}
		
		curSelectDatas.clear();

		String tig = resources.getString(R.string.tig1)+"(0)"; //删除(0)
		deleteButton.setText(tig);
		
		loadDatas();
		Toast.makeText(this, resources.getString(R.string.toast1), Toast.LENGTH_SHORT).show();
	}
	
	private class LoadDataAsyncTask extends AsyncTask<String, Void, List<DBalarminfo>>
	{
		@Override
		protected List<DBalarminfo> doInBackground(String... params) 
		{
			return DBalarminfo.GetAlarmsList(params[0], "100");
		}
		
		@Override  
		protected void onPostExecute(List<DBalarminfo> result)
		{
			info = result;
			updateAlarmDatas();
			adapter.notifyDataSetChanged();

			progressBarView.setVisibility(View.GONE);
		}
	}
	
	private void updateAlarmDatas() {
		if(info != null) {
			HashMap<String, List<DBalarminfo>> alarmDatas = new HashMap<String, List<DBalarminfo>>();
			
			for(DBalarminfo data : info) {
				String dateTime = data.getDatetime();
				if(dateTime != null && dateTime.length()>0) {
					String[] array = dateTime.split(" ");
					if (array.length > 0 && array[0] != null) {
						dateTime = array[0];
					}
					
					List<DBalarminfo> alarms = alarmDatas.get(dateTime);
					if(alarms == null) {
						alarms = new ArrayList<DBalarminfo>();
						alarmDatas.put(dateTime, alarms);
					}
					alarms.add(data);
				}
			}
			
			if(alarmDatas.size()>0) {
				Set<String> keySet = alarmDatas.keySet();
				List<String> keys = new ArrayList<String>(keySet);
				if(keys.size()>1) {
					Collections.sort(keys, new Comparator<Object>(){
						@Override
						public int compare(Object arg0, Object arg1) {
							try {
								String time1 = (String) arg0;
								String time2 = (String) arg1;								
								SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
								Date date1 = dateFormat.parse(time1);
								Date date2 = dateFormat.parse(time2);								
								return date2.compareTo(date1);
							}catch (Exception e) {}
							return 0;
						}
					});
				}
				
				adapter.datas.clear();
				for(String date : keys) {
					List<DBalarminfo> alarms = alarmDatas.get(date);
					date = getTime(date);
					
					AlarmHeaderData headerData = new AlarmHeaderData();
					headerData.setDate(date);
					headerData.setCount(alarms.size()+"");
					adapter.datas.add(headerData);
					
					adapter.datas.addAll(alarms);
				}
			}else
				adapter.datas.clear();
		}
	}
	
	private String getTime(String time) {
		if (time == null || time.length() == 0) {
	        return "";
	    }
		
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(time);

			if (APP.language.equals("zh")) {
				dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
				time = dateFormat.format(date);

				String week = getWeek(date);
				week = LanguageHelper.changeLanguageText(week);

				time = time + " " + week;
			}else {
				dateFormat = new SimpleDateFormat("MMMM d, yyyy");
				time = dateFormat.format(date);

				String week = getWeek(date);
				week = LanguageHelper.changeLanguageText(week);

				time = week + ", " + time;
			}

		}catch (Exception e) {}
		
		return time;
	}
	
	private String getWeek(Date date) {
		String Week = "星期";
		
		try {
			Calendar c = Calendar.getInstance();
		    c.setTime(date);
		    
		    if (c.get(Calendar.DAY_OF_WEEK) == 1) {
		        Week += "日";
			}
		    if (c.get(Calendar.DAY_OF_WEEK) == 2) {
				Week += "一";
			}
			if (c.get(Calendar.DAY_OF_WEEK) == 3) {
				Week += "二";
			}
			if (c.get(Calendar.DAY_OF_WEEK) == 4) {
				Week += "三";
			}
		    if (c.get(Calendar.DAY_OF_WEEK) == 5) {
				Week += "四";
			}
			if (c.get(Calendar.DAY_OF_WEEK) == 6) {
				Week += "五";
			}
			if (c.get(Calendar.DAY_OF_WEEK) == 7) {
				Week += "六";
			}
	    } catch (Exception e) {}
			  
		return Week;
	}
	
}
