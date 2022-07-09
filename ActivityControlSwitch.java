package snd.ui;

import snd.adapter.AdapterWattSeting;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBControlSwitchs;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

public class ActivityControlSwitch extends ActivityBase {

	private GridView listView;
	
	private AdapterWattSeting adapter;
	private BroadcastReceiver br;
	private Breaker curBreaker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.control);
		
		listView = (GridView)findViewById(R.id.list);
		
		adapter = new AdapterWattSeting(this);
		adapter.curchannel = -1;
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
	public void onStart() {
		super.onStart();
		
		this.registerReceiver(br, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
		refrenshAdapter();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.unregisterReceiver(br);
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
	//方法功能：保存按钮事件
	public void saveAction(View v) {
		if(check()) {
			DBControlSwitchs.insert(curBreaker);
			Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) {
		Breaker breaker = (Breaker)v.getTag();
		adapter.curchannel = breaker.addr;
		curBreaker = breaker;
		refrenshAdapter();
	}
	
	private void refrenshAdapter() {
		adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		if(curBreaker == null) {
			Toast.makeText(this, "请选择保护开关！", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(DBControlSwitchs.check(adapter.curchannel)) {
			Toast.makeText(this, "该保护开关已保存！", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
}
