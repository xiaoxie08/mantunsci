package snd.ui;

import snd.adapter.AdapterWattSeting;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBdevices;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityRealtime extends ActivityBase
{
	private static final String TAG=ActivityRealtime.class.getName();

	private GridView listView;
	private RelativeLayout contentView;
	private TextView nameView;
	private TextView apView;
	private TextView aldView;
	private TextView atView;
	private TextView aaView;
	private TextView avView;
	private ImageView treeImageView;
	private LinearLayout treeItemView1;
	private TextView item1TextView;
	private TextView item1StateView;
	private TextView item1PowerView;
	
	private AdapterWattSeting adapter;
	private BroadcastReceiver br;
	
	private Breaker curBreaker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.realtime);
        
        listView = (GridView) findViewById(R.id.realtime_list);
		contentView = (RelativeLayout) findViewById(R.id.realtime_content_bg);
		nameView = (TextView) findViewById(R.id.realtime_name);
		aldView = (TextView) findViewById(R.id.realtime_ald);
		apView = (TextView) findViewById(R.id.realtime_ap);
		atView = (TextView) findViewById(R.id.realtime_at);
		aaView = (TextView) findViewById(R.id.realtime_aa);
		avView = (TextView) findViewById(R.id.realtime_av);
		treeImageView = (ImageView) findViewById(R.id.realtime_tree);
		treeItemView1 = (LinearLayout) findViewById(R.id.realtime_tree_item1);
		item1TextView = (TextView) findViewById(R.id.realtime_tree_text1);
		item1StateView = (TextView) findViewById(R.id.realtime_tree_state1);
		item1PowerView = (TextView) findViewById(R.id.realtime_tree_power1);
        
        adapter = new AdapterWattSeting(this);
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
	public void onDestroy() 
	{
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
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：查看电压情况按钮事件
	public void chartAction(View v) {
		if (curBreaker != null) {
			int channel = curBreaker.addr;
			Intent intent = new Intent (this, ActivityVoltageChart.class);
			intent.putExtra("channel", channel);
			intent.putExtra("name", curBreaker.title);
			startActivity(intent);
			this.finish();
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
	
	private void refrenshAdapter()
	{
		try
		{			
			adapter.notifyDataSetChanged();
			
			Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
			
			showChannelData(b);
			initElectricity(b);
		}
		catch(Exception e){}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：显示选择线路的具体数据
	private void showChannelData(Breaker breaker) {
		curBreaker = breaker;
		String name = breaker.title;
		String oldName = nameView.getText().toString();
		if(oldName == null || name == null || !oldName.equals(name)) {
			nameView.setText(name);
		}

		//漏电流
		float _ald = breaker.A_LD;
		int ald = (int) _ald;
		
		//功率
		float _ap = breaker.A_P;
		int ap = (int) _ap;
		
		//温度
		float _at = breaker.A_T;
		int at = (int) _at;
		
		//电流
		float _aa = breaker.A_A;
		int aa = (int) _aa;
		
		//电压
		float _av = breaker.A_V;
		int av = (int) _av;

        aldView.setText(ald + "");
		apView.setText(ap + "");
		atView.setText(at + "");
		aaView.setText(_aa + "");
		avView.setText(av + "");
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：显示线路上的电器情况
	private void initElectricity(Breaker breaker) 
	{
		boolean oc = breaker.OpenClose;
		boolean isRun = false;
		boolean isShow = false;
		
		if(oc) { //线路是开启
			nameView.setBackgroundResource(R.drawable.realtime_icon_on1);
			contentView.setBackgroundResource(R.drawable.realtime_icon_on2);
		}else { //线路是关闭
			nameView.setBackgroundResource(R.drawable.realtime_icon1);
			contentView.setBackgroundResource(R.drawable.realtime_icon2);
		}
		
		DBdevices devices = DBdevices.GetChannelDevice(breaker.addr);
		if(devices != null) { //有电器
			treeItemView1.setVisibility(View.VISIBLE);
			isShow = true;
			
			String name = devices.getName();
			String status = devices.getStatus();
			String sleeppower = devices.getSleeppower()+"";
			String power = devices.getPower()+"";
			String message = "已开启";
			if(status != null && status.length()>0) {
				if(status.contains("停") || status.contains("关")) {
					message = "已关闭";
				}else if(status.contains("待")) {
					message = "待机中";
				}
			}
			power = (message.equals("待机中"))?sleeppower:power;
			boolean state = !message.equals("已关闭");
			String s = ((oc && state) ? message : "已关闭");
			String p = ((oc && state) ? power : "0") + "瓦";
			
			item1TextView.setText(name);
			item1StateView.setText(s);
			item1PowerView.setText(p);
			
			isRun = (oc && state) ? true:false;
		}else { //没有电器
			treeItemView1.setVisibility(View.GONE);
		}
		
		//显示线路状态
		treeImageView.setSelected(isRun);
		treeImageView.setEnabled(isShow);
	}

}
