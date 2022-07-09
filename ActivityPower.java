package snd.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import seasnake.loger.Logger;
import snd.adapter.AdapterPower;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBconfig;
import snd.database.DBpower;
import snd.util.LanguageHelper;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityPower extends ActivityBase
{
	private static Logger log=Logger.getLogger(ActivityPower.class);

	private Resources resources;
	
	private RelativeLayout topView;
	private TextView tipView;
	private LinearLayout eleView;
	private ListView listView;
	
	private AdapterPower adapter;
	private BroadcastReceiver br;
	
	private String lastmonth;
	private String lastyear;
	
	public float totalLastMonthEle = 0; //上月总电量
	public float totalMonthEle = 0; //本月总电量
	public float totalYearEle = 0; //本年总电量
	
	private List<Breaker> lineDatas = new ArrayList<Breaker>();
	private List<Breaker> datas = new ArrayList<Breaker>();
	private List<String> wiringDatas = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
        setContentView(R.layout.power);
        
        topView = (RelativeLayout)findViewById(R.id.electricity_top);
	    tipView = (TextView)findViewById(R.id.electricity_tip);
	    eleView = (LinearLayout)findViewById(R.id.electricity_ele);
        listView = (ListView)findViewById(R.id.electricity_list);

        adapter = new AdapterPower(this);
        setWiring();
        listView.setAdapter(adapter); 
        
        Calendar calendar = Calendar.getInstance();
        
        //去年
        lastyear = (calendar.get(Calendar.YEAR)-1)+"12";
        
        //上月
      	calendar.add(Calendar.MONTH, -1);
      	SimpleDateFormat dfym = new SimpleDateFormat("yyyyMM");
      	lastmonth = dfym.format(calendar.getTime());
        
        br=new BroadcastReceiver()
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
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    
	    int height = topView.getHeight();
	    int width = topView.getWidth();
	    
	    android.view.ViewGroup.LayoutParams pp = tipView.getLayoutParams();
	    pp.height = height/2-2;
	    tipView.setLayoutParams(pp);
	    
	    pp = eleView.getLayoutParams();
		int eleBgWidth = 2*width/3+20;
		int iconWidth = eleBgWidth/13;
		pp.width = iconWidth*13;
		pp.height = 138*iconWidth/82;
	    eleView.setLayoutParams(pp);
	    
	    int bgWidth = 0;
	    for(int i=0; i<eleView.getChildCount(); i++) {
	    	View iconView = eleView.getChildAt(i);
			LayoutParams iconPara = iconView.getLayoutParams();
			iconPara.width = iconWidth;
			iconPara.height = 138*iconWidth/82;
	        iconView.setLayoutParams(iconPara);
	        
	        bgWidth += iconPara.width;
	    }
	    
	    if(bgWidth > 0) {
	    	pp.width = bgWidth;
	    	eleView.setLayoutParams(pp);
	    }
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

		lineDatas.clear();
		datas.clear();
		wiringDatas.clear();
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
	// 方法功能：接线设置事件
	public void channelSettingAction(View v) {
		Breaker data = (Breaker) v.getTag();
		if(data != null && data.addr != 0) {
			showSelectTotalChannel(data);
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：选中事件
	public void itemClickAction(View v) { 
		Breaker data = (Breaker) v.getTag();
		if(data != null) {
			int channel = data.addr;
			Intent intent = new Intent (this, ActivityPowerChart.class);
			intent.putExtra("channel", channel);
			intent.putExtra("name", data.title);
			startActivity(intent);
			this.finish();
		}
	}
	
	private void refrenshAdapter() {
		try
		{
			setWiring();
			adapter.notifyDataSetChanged();
			
			if(adapter.getCount() > 0 && adapter.getItem(0) != null) {
				Breaker breaker = (Breaker)adapter.getItem(0);
				if(breaker.addr == 0) {
					tipView.setText(LanguageHelper.changeLanguageText("本年总电量"));
					
					String yearEle = totalYearEle+"";
					if(yearEle.equals("0.0")) {
						yearEle = "0";
					}
					setNumber(yearEle);
				}else {
					tipView.setText(breaker.title+LanguageHelper.changeLanguageText("本年度数"));
					
					//线路上累计的总电量
					float p = Math.abs((float)(breaker.power));
					p = (float)(Math.floor(p*10))/10;
					String power = p+"";
					if(power.equals("0.0")) {
						power = "0";
					}
					
					//年度电量
					String yearEle = power;	
					DBpower ly = DBpower.GetPowerrec(lastyear, "MONTH", breaker.addr);
					if(ly != null) {
						float YE = Math.abs((float)(breaker.power-ly.getTotalpower()));
						YE = (float)(Math.floor(YE*10))/10;
						yearEle = YE+"";
						if(yearEle.equals("0.0")) {
							yearEle = "0";
						}
					}
					setNumber(yearEle);
				}
				 
			}
		}
		catch(Exception e){}
	}
	
	private void setNumber(String number) {
		eleView.removeAllViews();
		
		if(number.length() < 13) {
			if(number.length() == 0) {
				number = "0.0";
			}else if (number.length() == 1)
	            number = number+".0";
			
			int count = 13-number.length();
	        for (int i=0; i<count; i++) {
	            number = "0"+number;
	        }
		}
		
		if(number.length() > 13) {
			number = number.substring(number.length()-13, number.length());
		}
		
		int bgWidth = 0;
		for(int i=0; i<number.length(); i++) {
			String n = String.valueOf(number.charAt(i));
			ImageView iconView = new ImageView(this);
			iconView.setImageResource(getNumberIcon(n));
			eleView.addView(iconView);
				
			LayoutParams para = eleView.getLayoutParams();
			LayoutParams iconPara = iconView.getLayoutParams();
			iconPara.width = para.width/13;
			iconPara.height = 138*iconPara.width/82;
	        iconView.setLayoutParams(iconPara);
	        
	        bgWidth += iconPara.width;
		}
		
		if(bgWidth > 0) {
			android.view.ViewGroup.LayoutParams pp = eleView.getLayoutParams();
			pp.width = bgWidth;
			eleView.setLayoutParams(pp);
		}
	}
	
	private int getNumberIcon(String number) {
		if(number.endsWith(".")) {
			return R.drawable.ele_number;
		}else if(number.endsWith("0")) {
			return R.drawable.ele_0;
		}else if(number.endsWith("1")) {
			return R.drawable.ele_1;
		}else if(number.endsWith("2")) {
			return R.drawable.ele_2;
		}else if(number.endsWith("3")) {
			return R.drawable.ele_3;
		}else if(number.endsWith("4")) {
			return R.drawable.ele_4;
		}else if(number.endsWith("5")) {
			return R.drawable.ele_5;
		}else if(number.endsWith("6")) {
			return R.drawable.ele_6;
		}else if(number.endsWith("7")) {
			return R.drawable.ele_7;
		}else if(number.endsWith("8")) {
			return R.drawable.ele_8;
		}else {
			return R.drawable.ele_9;
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：计算总电量
	private void setTotalPower(Breaker breaker) {
		float power = Math.abs((float)(breaker.power));
		power = (float)(Math.floor(power*10))/10;
		
		DBpower lm = DBpower.GetPowerrec(lastmonth, "MONTH", breaker.addr);
		DBpower ly = DBpower.GetPowerrec(lastyear, "MONTH", breaker.addr);
		
		if(lm != null) {
			//上月电量
			float LME = Math.abs((float)(lm.getPower()));
			LME = (float)(Math.floor(LME*10))/10;
			totalLastMonthEle += LME;
			
			//本月电量
			float ME = Math.abs((float)(breaker.power-lm.getTotalpower()));
			ME = (float)(Math.floor(ME*10))/10;
			totalMonthEle += ME;
		}else {
			//本月电量
			totalMonthEle += power;
		}
		
		if(ly != null) {
			//年度电量
			float YE = Math.abs((float)(breaker.power-ly.getTotalpower()));
			YE = (float)(Math.floor(YE*10))/10;
			totalYearEle += YE;
		}else {
			//年度电量
			totalYearEle += power;
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：根据接线设置更新排序线路数据
	private void setWiring() {
		totalMonthEle = 0;
		totalYearEle = 0;
		totalLastMonthEle = 0;
		datas.clear();
		
		if(APP.distributbox != null && APP.distributbox.Breakers != null
			&& APP.distributbox.Breakers.values().size()>0) {

			ArrayList<Breaker> values = new ArrayList<Breaker>(APP.distributbox.Breakers.values());
			if(values.size() > 1) {
				Collections.sort(values, new Comparator<Breaker>(){
					@Override
					public int compare(Breaker arg0, Breaker arg1) {
						int addr0 = arg0.addr;
						int addr1 = arg1.addr;
						if(addr0 < addr1) {
							return -1;
						}
						return 1;
					}
				});
			}

			List<Breaker> array = new ArrayList<Breaker>();
			HashMap<String, List<Breaker>> dictionary = new HashMap<String, List<Breaker>>();
			
			float totalPower = 0; //当前累加的总电量
			for(Breaker data : values) {
				String channelId = data.addr+"";
				String totalChannelId = data.totalChannelId;
				
				if(totalChannelId != null && totalChannelId.equals("-1")) {
					totalPower += data.power;
					setTotalPower(data);
					datas.add(data);
				}
				
				data.wiringType = 1;
				if(totalChannelId != null && totalChannelId.length()>0) {
					wiringDatas.clear();
					String parentId = getParentChannelId(channelId);
					if (parentId == null || parentId.length() == 0) {
						array.add(data);
					}else {
						List<Breaker> value = dictionary.get(parentId);
    	                if(value == null) {
    	                	value = new ArrayList<Breaker>();
    	                	dictionary.put(parentId, value);
    	                }
    	                
    	                if(parentId.equals(channelId)) {
    	                	value.add(0, data);
    	                }else {
    	                	if(APP.distributbox.Breakers.containsKey(Integer.parseInt(parentId))) {
    	                		data.wiringType = 2;
    	                	}else
    	                		data.wiringType = 1;
    	                	value.add(data);
    	                }
					}
					
				}else {
					array.add(data);
				}
			}
			
			adapter.channelDatas.clear();
			
			if(datas.size()>0) {
				Breaker data = new Breaker(0, LanguageHelper.changeLanguageText("总电量"));
				data.power = totalPower;
				adapter.channelDatas.add(data);
			}
			
			adapter.channelDatas.addAll(array);	
			
			Set<String> keySet = dictionary.keySet();
			List<String> keys = new ArrayList<String>(keySet);
			Collections.sort(keys, new Comparator<Object>(){ 
    			@Override
    	        public int compare(Object arg0, Object arg1) {  
    				String address0 = (String) arg0;
    				String address1 = (String) arg1;
    				if(address0 != null && address1 != null
    				   && address0.length()>0 && address1.length()>0
    				   && Integer.parseInt(address0) < Integer.parseInt(address1)) {
    					return -1;
    				}
    				return 1;  
    	        }
    	    });
			for(String key : keys) {
				List<Breaker> value = dictionary.get(key);
				adapter.channelDatas.addAll(value);
			}
	        
	        array.clear();
	        dictionary.clear();
		}else
			adapter.channelDatas.clear();
	}
	
	private String getParentChannelId(String channelId) {
        String parentId = null;
		
		if(channelId != null && channelId.length()>0
			&& APP.distributbox.Breakers.containsKey(Integer.parseInt(channelId))) {
			Integer addr = Integer.parseInt(channelId);
			Breaker data = APP.distributbox.Breakers.get(addr);
			parentId = data.totalChannelId;

			wiringDatas.add(channelId);
		}

		if(parentId != null && parentId.length()>0 && parentId.equals(channelId)) {
			return parentId;
		}

		if(parentId != null && parentId.length()>0 && wiringDatas.contains(parentId)) {
			return parentId;
		}
		
		if(parentId != null && parentId.length()>0 && !parentId.equals("-1")) {
			parentId = getParentChannelId(parentId);
		}
		
		if(parentId != null && parentId.length()>0 && parentId.equals("-1")) {
			parentId = channelId;
		}
		
		return parentId;
	}
	
	private void showSelectTotalChannel(final Breaker channelData) {
		int position = -1;
        String totalChannelId = channelData.totalChannelId;

        List<String> list = new ArrayList<String>();
        int i = 0;
        lineDatas.clear();
        if(datas != null) {
			for(Breaker data : datas) {
				if(data.addr != channelData.addr) {
					String channelId = data.addr+"";
					String name = data.title;
				    if(name == null) name = "";
					name = LanguageHelper.changeLanguageNode(name);
				    list.add(name);
				    lineDatas.add(data);
				    
				    if(totalChannelId != null && totalChannelId.length()>0
				       && channelId != null && channelId.equals(totalChannelId)) {
				    	position = i;
			        }
				    i++;
				}
			}
		}
        list.add(LanguageHelper.changeLanguageText("进线直连"));
        final int count =  list.size();
        String[] array = (String[])list.toArray(new String[count]);
		
		if(totalChannelId != null && totalChannelId.length()>0
		   && totalChannelId.equals("-1")) {
			position = i;
		}
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(resources.getString(R.string.alert11));
		builder.setSingleChoiceItems(array, position, new DialogInterface.OnClickListener() {
			 
		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		         showtotalChannelDialog(channelData, which);
		         
		         dialog.dismiss();
		     }
		});
		Dialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void showtotalChannelDialog(final Breaker channelData, final int position) {
		String name1 = channelData.title;
		String message = "您是否确定"+name1+"是进线直连？";
		message = LanguageHelper.changeLanguageWiring(message, name1, null);

		if (lineDatas != null && position < lineDatas.size()) {
			Breaker data = lineDatas.get(position);
			String name2 = data.title;
			message = "您是否确定"+name1+"接入"+name2+"？";
			message = LanguageHelper.changeLanguageWiring(message, name1, name2);
		}
		
		AlertDialog alert = new MyAlertDialog(this)
		.setMessage(message)
		.setTitle(resources.getString(R.string.tig7))
		.setPositiveButton(resources.getString(R.string.tig5), new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
				String channelId = channelData.addr+"";
		        String totalChannelId = "-1";
		        if (lineDatas != null && position < lineDatas.size()) {
		        	Breaker data = lineDatas.get(position);
		        	totalChannelId = data.addr+"";
		        }else { //进线直连
		            totalChannelId = "-1";
		        }
		        
		        if(channelId == null || totalChannelId == null) return;
		        
				DBconfig.UpdateConfig("WIRING", "SWITCH"+channelId, totalChannelId);
				if(APP.distributbox != null) {
					int addr = Integer.parseInt(channelId);
					Breaker breaker1 = APP.distributbox.Breakers.get(addr);
					Breaker breaker2 = APP.distributbox.totalBreakers.get(addr);
					if(breaker1 != null) breaker1.totalChannelId = totalChannelId;
					if(breaker2 != null) breaker2.totalChannelId = totalChannelId;
				}
		        
		        refrenshAdapter();
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
		
		final int tig = this.getResources().getIdentifier("message","id","android") ;        
    	TextView messageTextView = (TextView) alert.findViewById(tig);        
    	messageTextView.setTextSize(27);        
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
}
