package snd.ui;

import snd.adapter.AdapterWattSeting;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBdevices;
import snd.util.Common;
import snd.util.LanguageHelper;
import snd.util.Tooles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDeviceEdit extends ActivityBase
{
	private static final String TAG=ActivityDeviceEdit.class.getName();

	private Resources resources;
	
	private long autoid = -1;
	private int type = 1;

	private GridView listView;
	private TextView titleView;
	private EditText nameView;
	private EditText powerView;
	private EditText sleepPowerView;
	
	private AdapterWattSeting adapter;
	private BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
		Intent intent = getIntent();
		autoid = intent.getLongExtra("autoid", -1);
		type = intent.getIntExtra("type", 1);
		
        setContentView(R.layout.deviceedit);
        
        listView = (GridView)findViewById(R.id.list);
	    titleView = (TextView)findViewById(R.id.title);
	    nameView = (EditText)findViewById(R.id.name);
	    powerView = (EditText)findViewById(R.id.power);
	    sleepPowerView = (EditText)findViewById(R.id.sleeppower);
        
        adapter = new AdapterWattSeting(this);
        listView.setAdapter(adapter);
        
        powerView.addTextChangedListener(textWatcher1);
	    sleepPowerView.addTextChangedListener(textWatcher2);
	    
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
	    	titleView.setText(resources.getString(R.string.eleAppl_add_title));
	    	powerView.setText("100");
	    	sleepPowerView.setText("0");
		}else { //修改
			titleView.setText(resources.getString(R.string.eleAppl_edit_title));
			
			DBdevices data = DBdevices.GetDevice(autoid);
			if(data != null) {
				String name = data.getName();
	    		String power = data.getPower()+"";
	    		String sleepPower = data.getSleeppower()+"";
	    		int channel = data.getChannel();
	    		
	    		if(power.length() == 0) {
	    			power = "100";
	    		}else
					power = Common.strs(power);

	    		if(sleepPower.length() == 0) {
	    			sleepPower = "0";
	    		}else
					sleepPower = Common.strs(sleepPower);
	    		
	    		adapter.curchannel = channel;
	    		nameView.setText(name);
	    		powerView.setText(power);
	    		sleepPowerView.setText(sleepPower);
	    		
	    		Editable etext = nameView.getText();
	    		nameView.setSelection(etext.length());
			}
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		if(type == 1) {
			this.startActivity(new Intent(this, ActivityEleAppState.class));
		}else {
			this.startActivity(new Intent(this, ActivityDevice.class));
		}
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：保存按钮事件
	public void saveAction(View v) { 
		if(check()) {
			try
	    	{
				Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
				String name = nameView.getText().toString();
				String power = powerView.getText().toString().trim();
				String sleeppower = sleepPowerView.getText().toString().trim();
				power = power.replace(LanguageHelper.changeLanguageText("瓦"), "");
				sleeppower = sleeppower.replace(LanguageHelper.changeLanguageText("瓦"), "");
				
				if(autoid == -1) { //新添
					DBdevices rec = new DBdevices();
			    	rec.setMac(APP.MAC);
	        		rec.setChannel(b.addr);
	        		rec.setName(name);
	        		rec.setSleeppower(Float.parseFloat(sleeppower));
	        		rec.setPower(Float.parseFloat(power));
	        		rec.setStatus("关");
	        		
	        		DBdevices.Update(rec);
				}else { //修改
					DBdevices rec = DBdevices.GetDevice(autoid);
					if(rec != null) {
						rec.setChannel(b.addr);
		        		rec.setName(name);
		        		rec.setSleeppower(Float.parseFloat(sleeppower));
		        		rec.setPower(Float.parseFloat(power));
		        		
		        		DBdevices.UpdateBdevice(rec);
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
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) {
		Breaker breaker = (Breaker)v.getTag();
		adapter.curchannel = breaker.addr;
		refrenshAdapter();
	}
	
	private void refrenshAdapter() {
		try {
			adapter.notifyDataSetChanged();
		}catch(Exception e){}
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		String name = nameView.getText().toString();
		String power = powerView.getText().toString();
		String sleeppower = sleepPowerView.getText().toString();
		
		if(APP.distributbox.Breakers.size() == 0 || 
		   !APP.distributbox.Breakers.containsKey(adapter.curchannel)) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(name.length() == 0) {
			Toast.makeText(this, resources.getString(R.string.toast9), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(power.length() == 0) {
			Toast.makeText(this, resources.getString(R.string.toast10), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(sleeppower.length() == 0) {
			Toast.makeText(this, resources.getString(R.string.toast11), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(autoid == -1 && DBdevices.GetChannelDevice(adapter.curchannel) != null) {
			Toast.makeText(this, resources.getString(R.string.toast12), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private TextWatcher textWatcher1 = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			String str = s.toString();
			if(str.length()>0 && !str.contains(LanguageHelper.changeLanguageText("瓦"))) {
				str = str + LanguageHelper.changeLanguageText("瓦");
				powerView.setText(str);
			}else if(str.equals(LanguageHelper.changeLanguageText("瓦"))) {
				powerView.setText("");
			}
		}
	};
	
	private TextWatcher textWatcher2 = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			String str = s.toString();
			if(str.length()>0 && !str.contains(LanguageHelper.changeLanguageText("瓦"))) {
				str = str + LanguageHelper.changeLanguageText("瓦");
				sleepPowerView.setText(str);
			}else if(str.equals(LanguageHelper.changeLanguageText("瓦"))) {
				sleepPowerView.setText("");
			}
		}
	};
	
}
