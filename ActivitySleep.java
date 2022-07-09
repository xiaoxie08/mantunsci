package snd.ui;

import seasnake.browser.BaiduWeather;
import seasnake.browser.BaiduWeatherData;
import seasnake.browser.BaiduWeatherZs;
import seasnake.browser.Browser;
import seasnake.loger.Logger;
import snd.broadcastreceiver.Hours1BroadcastReceiver;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.broadcastreceiver.SndBroadcastReceiver;
import snd.database.DBAdvertising;
import snd.database.DBLocalAdvert;
import snd.model.AdvertisingData;
import snd.util.LanguageHelper;
import snd.util.Lunar;
import snd.util.QueryTask;
import snd.util.Tooles;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitySleep extends ActivityBase
{
	private static Logger log = Logger.getLogger(ActivitySleep.class);
	
	private MyBroadcastReciver myBroadcastRecive;
	
	TextView textView1; //显示日期
	TextView textView2; //显示农历
	TextView textView3; //显示时间
	TextView textView4; //显示星期
	TextView textView5; //显示早天气
	TextView textView6; //显示晚天气
	TextView textView7; //显示当前天气
	TextView textView8; //显示当前天气类型
	TextView textView9; //显示明天天气类型
	TextView textView10; //显示明天天气
	TextView textView11; //显示当前天气的温馨提示
	ImageView imageView1; //显示当前的天气图标
	ImageView imageView2; //显示早的天气图标
	ImageView imageView3; //显示晚的天气图标
	ImageView imageView4; //显示明天的天气图标
	
	Browser browser = new Browser();
	BroadcastReceiver bcReceiver;
	ExecutorService executors = Executors.newFixedThreadPool(5);
	boolean isTouch = false;
	
	Handler handler = new Handler()
    {
    	@Override
    	public void handleMessage(Message msg) 
    	{
    		switch(msg.what)
    		{
    		    case 1:
    		    {
    		    	try
    		    	{
    		    		//获取明天是星期几
    		    		Calendar tomorrow = Calendar.getInstance();
        		    	tomorrow.add(Calendar.DAY_OF_MONTH, 1);   
        		    	SimpleDateFormat format = new SimpleDateFormat("E");
    				    String s = format.format(tomorrow.getTime());
    				    
        		    	BaiduWeather weather = (BaiduWeather)msg.obj;
        		    	
        		    	//天气数据信息
        		    	List<BaiduWeatherData> datas = weather.getResults().get(0).getWeather_data();
        		    	for(BaiduWeatherData data : datas)
        		    	{
        		    		//今天
        		    		if(data.getDate().indexOf("实时")!=-1)
        		    		{
        		    			//获取当前的小时
        		    			Calendar today = Calendar.getInstance();
        		    			int hour = today.get(Calendar.HOUR_OF_DAY);
        		    			
        		    			//早晚和当前天气
        		    			String[] temp = data.getTemperature().replace("℃", "").replace(" ", "").split("~");
        		    			String weather1 = temp[0]+"℃"; //早   		    				
    		    				String weather2 = temp[1]+"℃"; //晚
    		    				String weather3 = data.getDate().split("实时：")[1].replace("℃)", "℃"); //当前
    		    				
    		    				textView5.setText(weather1);    		    			
    		    				textView6.setText(weather2);
    		    				textView7.setText(weather3);
    		    				
    		    				//当前天气类型
    		    				String type = data.getWeather();
								type = LanguageHelper.changeLanguageWeather(type);
    		    				textView8.setText(type);
        		    			
        		    			//当前天气图标
        		    			APP.weathId = Tooles.getWeatherIcon(data, hour);
        		    			imageView1.setImageResource(APP.weathId);
        		    			
        		    			//早晚天气图标
        		    			int icon2 = Tooles.getWeatherIcon(data, 6);
        		    			int icon3 = Tooles.getWeatherIcon(data, 19);
        		    			imageView2.setImageResource(icon2);
        		    			imageView3.setImageResource(icon3);
        		    			
        		    			APP.weatherdata = data;
        		    		}
        		    		
        		    		//明天
        		    		if(data.getDate().startsWith(s))
        		    		{
        		    			String tomorrow_weather = data.getWeather();
        		    			String tomorrow_temperature = data.getTemperature();

        		    			tomorrow_temperature = tomorrow_temperature.replace(" ~ ", "/");
								tomorrow_weather = LanguageHelper.changeLanguageWeather(tomorrow_weather);
        		    			
        		    			textView9.setText(tomorrow_weather);
        		    			textView10.setText(tomorrow_temperature);
        		    			
        		    			int icon4 = Tooles.getWeatherIcon(data, 6);
        		    			imageView4.setImageResource(icon4);
        		    		}
        		    	}
        		    	
        		    	//判断明天天气是否获取成功，否的话就继续获取
        		    	String t_weather = textView10.getText().toString();
        		    	if(t_weather == null || t_weather.equals("N℃")) {
        		    		for(BaiduWeatherData data : datas)
            		    	{
        		    			if(data.getDate().indexOf("实时")==-1)
        		    			{
        		    				String tomorrow_weather = data.getWeather();
            		    			String tomorrow_temperature = data.getTemperature();

            		    			tomorrow_temperature = tomorrow_temperature.replace(" ~ ", "/");
									tomorrow_weather = LanguageHelper.changeLanguageWeather(tomorrow_weather);
            		    			
            		    			textView9.setText(tomorrow_weather);
            		    			textView10.setText(tomorrow_temperature);
            		    			
            		    			int icon4 = Tooles.getWeatherIcon(data, 6);
            		    			imageView4.setImageResource(icon4);
            		    			
            		    			break;
        		    			}
            		    	}
        		    	}

        		    	//天气温馨提示
        		    	List<BaiduWeatherZs> indexs = weather.getResults().get(0).getIndex();
        		    	for(BaiduWeatherZs data : indexs)
        		    	{
        		    		String title = data.getTitle();
        		    		if(title != null && title.endsWith("感冒")) {
        		    			String des = "【 温馨提示 】" + data.getDes();
								//des = des.replace("机率", "几率");
								des = LanguageHelper.changeLanguageWeather(des);
								textView11.setText(des);
        		    			break;
        		    		}
        		    	}
        		    	if (textView11.getText().toString().length() == 0) {
							String des = LanguageHelper.changeLanguageWeather("【 温馨提示 】无论天气如何，都记得让心情保持舒畅哦");
							textView11.setText(des);
						}
    		    	}
    		    	catch(Exception e){}
    		    }
    		    break;
    		}
    	}
    };
	   	   
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.sleepscreen);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //全屏显示
        
        textView1=(TextView)this.findViewById(R.id.textView1);
        textView2=(TextView)this.findViewById(R.id.textView2);
        textView3=(TextView)this.findViewById(R.id.textView3);
        textView4=(TextView)this.findViewById(R.id.textView4);
        textView5=(TextView)this.findViewById(R.id.textView5);
        textView6=(TextView)this.findViewById(R.id.textView6);
        textView7=(TextView)this.findViewById(R.id.textView7);
        textView8=(TextView)this.findViewById(R.id.textView8);
        textView9=(TextView)this.findViewById(R.id.textView9);
        textView10=(TextView)this.findViewById(R.id.textView10);
        textView11=(TextView)this.findViewById(R.id.textView11);
        imageView1=(ImageView)this.findViewById(R.id.imageview1);   
        imageView2=(ImageView)this.findViewById(R.id.imageview2);
        imageView3=(ImageView)this.findViewById(R.id.imageview3);
        imageView4=(ImageView)this.findViewById(R.id.imageview4);
        
        bcReceiver=new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				String action  = intent.getAction();
				if(action.equals(Second1BroadcastReceiver.Msg_1S))
				{
					showTime();
				}
				else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
						action.equals(Hours1BroadcastReceiver.Msg_1H))
				{
					initWeather();
				}
				else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
				{
					if(APP.connmanager != null) {
						NetworkInfo [] networkInfos=APP.connmanager.getAllNetworkInfo();  
		                for (int i = 0; i < networkInfos.length; i++) {  
		                    State state=networkInfos[i].getState();  
		                    if (NetworkInfo.State.CONNECTED == state) {
		                    	initWeather();
								if(APP.instance != null) APP.instance.initSDK();
		                        return;  
		                    }  
		                }  
					}
				}
			}
        };
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.show.advertising");
        myBroadcastRecive = new MyBroadcastReciver();
        registerReceiver(myBroadcastRecive, intentFilter);

		int systemBrightness = Tooles.getSystemBrightness(this);
		if(systemBrightness < 255) {
			Tooles.changeAppBrightness(this, 255);
		}
        
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  //屏幕宽度（像素）
        int height = metric.heightPixels;  //屏幕高度（像素）
        int densityDpi = metric.densityDpi; //屏幕密度DPI（120 / 160 / 240）
		int barHeight = Tooles.getNavigationBarHeight(this); //虚拟按键的高度（像素）
        APP.screenWidth = width+"";
        APP.screenHeight = (height+barHeight)+"";

        log.info("----屏幕宽度："+APP.screenWidth );
        log.info("----屏幕高度："+APP.screenHeight);
        log.info("----屏幕密度DPI："+densityDpi);
        log.info("----要连接的wifi名称："+APP.SSID);
        log.info("----要连接的wifi密码："+APP.PWD);
        log.info("----设备密码："+APP.LoginPwd);

		initDatas();
	}
	
	@Override
    public void onDestroy()
	{
    	super.onDestroy();

        APP.isAdvertising = false;
    	
        if(bcReceiver != null) {
        	this.unregisterReceiver(bcReceiver);
        	bcReceiver = null;
    	}
    	
    	if(myBroadcastRecive != null) {
	        this.unregisterReceiver(myBroadcastRecive);
	        myBroadcastRecive = null;
		}
    	
    	if(executors != null) {
    		executors.shutdown();
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!isTouch) {
            isTouch = true;

            if(APP.isGesturespassword) {
                SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
                String password = preferences.getString("app_password", "");
                if(password != null && password.length()>0) {
                    Intent intent = new Intent(this, ActivityGesturesPassword.class);
                    intent.putExtra("type", 1);
                    this.startActivity(intent);
                    this.finish();

                    return false;
                }
            }

            APP.isGesturespassword = false;
            this.startActivity(new Intent(this, ActivityMenu.class));
            this.finish();
        }

        //log.error("----触摸位置-X坐标："+event.getX()+"Y坐标："+event.getY());
        return false;
    }

	private void initDatas() {
		APP.isWifiConnection = true;

		showTime();

		this.registerReceiver(bcReceiver, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
		this.registerReceiver(bcReceiver, new IntentFilter(Hours1BroadcastReceiver.Msg_1H));
		this.registerReceiver(bcReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		this.registerReceiver(bcReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

		executors.execute(new Runnable()
		{
			@Override
			public void run()
			{
				getWeather();

				checkServices();
			}
		});

		//播放广告
		SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
		boolean mark = preferences.getBoolean("advertising", true);
		if(!APP.isGesturespassword && mark) {
			APP.isAdvertising = true;
			Intent intent = new Intent();
			intent.setAction("com.show.advertising");
			sendBroadcast(intent);
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：进入主菜单按钮事件
	public void goMenuAction(View v) {
		if(APP.isGesturespassword) {
			SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
			String password = preferences.getString("app_password", "");
			if(password != null && password.length()>0) {
				Intent intent = new Intent(this, ActivityGesturesPassword.class);
				intent.putExtra("type", 1);
				this.startActivity(intent);
				this.finish();

				return;
			}
		}

		APP.isGesturespassword = false;
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：检查服务是否在运行
	private void checkServices() {
		try{
			if(!isServiceRunning("snd.service.TaskBroadcastIp") && !APP.isBootCompleted){
				APP.isBootCompleted = false;
				
				Intent intent = new Intent();
				intent.setAction(SndBroadcastReceiver.Msg_STARTALARM);
				sendBroadcast(intent);
			}
		}catch(Exception e){}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：用来判断服务是否运行(className判断的服务名字,true在运行,false不在运行)
    private boolean isServiceRunning(String className) { 
        boolean isRunning = false; 
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);  
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (!(serviceList.size()>0)) { 
            return false; 
        } 
        for (int i=0; i<serviceList.size(); i++) { 
            if (serviceList.get(i).service.getClassName().equals(className) == true) { 
                isRunning = true; 
                break; 
            } 
        } 
        return isRunning;
    }

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：更新显示的日期时间
	private void showTime() {
		SimpleDateFormat format = null;
		String date = ""; //日期
		String time = ""; //时间
		String day = ""; //星期

		if (APP.language.equals("zh")) {
			format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss EEEE");

			String[] s = format.format(new Date()).split(" ");
			if(s.length > 0) date = s[0]; //日期
			if(s.length > 1) time = s[1]; //时间
			if(s.length > 2) day = s[2]; //星期
		}else {
			format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss EEEE", Locale.ENGLISH);

			String[] s = format.format(new Date()).split(" ");
			if(s.length > 2) date = s[0]+" "+s[1]+" "+s[2]; //日期
			if(s.length > 3) time = s[3]; //时间
			if(s.length > 4) day = s[4]; //星期
		}

		String[] times = time.split(":");
		if(times.length == 3) {
			time = times[0] + ":" + times[1];
		}

		//农历
		String oldDate = textView1.getText().toString();
		boolean check = (oldDate.length()==0 || (date != null && !date.equals(oldDate)));
		if(textView2.getText().toString().length()==0 || check) {
			Calendar calendar = Calendar.getInstance();
			Lunar lunar = new Lunar(calendar);
			String lunarStr = lunar.cyclical() + "年 " + lunar.toString();
			if (APP.language.equals("zh")) textView2.setText(lunarStr);
		}

		String oldTime = textView3.getText().toString();
		if(oldDate.length()>0 && oldTime.length()>0
			&& ((date != null && !date.equals(oldDate)) || (time != null && !time.equals(oldTime)))) {
            APP.isNewScreen = true;
		}

		textView1.setText(date);
		textView3.setText(time);
		textView4.setText(day);
	}
    
    private void initWeather()
    {
    	executors.execute(new Runnable()
		{
			@Override
			public void run() 
			{
				getWeather();
			}
		});
    }
    
	//方法类型：自定义方法
	//编   写：
	//方法功能：获取天气数据
    public synchronized void getWeather() 
	{
		boolean isNetwork = Tooles.isNetworkAvailable(this);
		if(!isNetwork) {
			return;
		}
		
		//开启异步任务请求天气数据
		new QueryTask(this, browser, handler).execute("");
	}
    
	private class MyBroadcastReciver extends BroadcastReceiver { 
		
		 @Override
		 public void onReceive(Context context, Intent intent) {
		     String action = intent.getAction();
		     if(action.equals("com.show.advertising") && APP.isAdvertising) {

		     	 //每次只播放一个广告
				 /*String advertisingURL = "";
				 AdvertisingData data = DBAdvertising.getAdvertising();
				 if (data != null && data.getUrl().length()>0) {
				 	 String advertisingId = data.getAdvertisingId();
				 	 String url = data.getUrl();
					 String path = data.getPath();

					 if (path.length()>0 && new File(path).exists()) {
						 advertisingURL = path;
					 }else if(Tooles.checkNetState(context) != 0) { //判断是否有网络
						 advertisingURL = url;

						 //Common.goToDownloadAdvertising(data, url);
					 }
				 }

			     if (advertisingURL.length()>0) {
				 	 final String url = advertisingURL;
			    	 handler.postDelayed(new Runnable(){
						 @Override
						 public void run(){
							 if(APP.isAdvertising) {
								 Message msg = new Message();
								 msg.what = 2;
								 Bundle b = new Bundle();
								 b.putString("url", url);
								 msg.setData(b);
								 handler.sendMessage(msg);
							 }
						 }
					 }, 5000);
			     }*/

				 //每次播放所有广告
                 int NetState = Tooles.checkNetState(context);
				 final List<AdvertisingData> datas1 = DBAdvertising.getAdvertisings(true);
                 final List<AdvertisingData> datas2 = DBAdvertising.getAdvertisings(false);
				 List<AdvertisingData> datas3 = DBLocalAdvert.getAdvertisings();
				 datas1.addAll(datas3);
				 datas2.addAll(datas3);
				 SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
				 final int mode = preferences.getInt("advertisingMode", 1);
                 if (datas1.size()>0 && NetState == 2) { //有连接网络
                     handler.postDelayed(new Runnable(){
                         @Override
                         public void run(){
                             if(APP.isAdvertising) {
								 APP.isAdvertising = false;

                                 //new DialogPopAdvertising1(ActivitySleep.this, datas1, 0).show();

								 /*Intent i = new Intent(ActivitySleep.this, ActivityAdvertising.class);
								 i.putExtra("datas", (Serializable)datas1);
								 ActivitySleep.this.startActivity(i);*/

								 Intent i = new Intent(ActivitySleep.this, ActivityAdvertising1.class);
								 i.putExtra("index", 0);
								 i.putExtra("datas", (Serializable)datas1);
								 i.putExtra("mode", mode);
								 ActivitySleep.this.startActivity(i);
                             }
                         }
                     }, 5000);
				 }else if (datas2.size()>0 && NetState == 0) { //没有连接网络，本地有缓存文件
                     handler.postDelayed(new Runnable(){
                         @Override
                         public void run(){
                             if(APP.isAdvertising) {
								 APP.isAdvertising = false;

                                 //new DialogPopAdvertising1(ActivitySleep.this, datas2, 0).show();

                                 /*Intent i = new Intent(ActivitySleep.this, ActivityAdvertising.class);
                                 i.putExtra("datas", (Serializable)datas2);
                                 ActivitySleep.this.startActivity(i);*/

								 Intent i = new Intent(ActivitySleep.this, ActivityAdvertising1.class);
								 i.putExtra("index", 0);
								 i.putExtra("datas", (Serializable)datas2);
								 i.putExtra("mode", mode);
								 ActivitySleep.this.startActivity(i);
                             }
                         }
                     }, 5000);
                 }

		      }
		 }
	    	  
	}
    
}
