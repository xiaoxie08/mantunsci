package snd.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import seasnake.loger.Logger;
import seasnake.util.SuDo;
import snd.adapter.LeftMenuAdapter;
import snd.adapter.MenuPagerAdapter;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBDelayedTask;
import snd.database.DBSceneTask;
import snd.database.DBconfig;
import snd.database.DBpower;
import snd.database.DBswitchsetting;
import snd.model.SceneData;
import snd.model.SceneTaskData;
import snd.serialservice.SerialThread;
import snd.util.DelayedPicker;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.MyAlertDialog;
import snd.view.SlideMenu;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMenu extends ActivityBase
{
	private static final String TAG = ActivityMenu.class.getName();
	private static Logger log = Logger.getLogger(ActivityMenu.class); //日志

	private Resources resources;

	private TextView weatherView;
	private ViewPager viewPager;
	private ViewGroup group;
	
	private SlideMenu menuView;
	private ListView listView;
	
	private MenuPagerAdapter pagerAdapter;
	private int currentPage = 0; //当前显示的页数

	public LeftMenuAdapter menuAdapter;
	
	private BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);
        
        setContentView(R.layout.menu);

        weatherView=(TextView)this.findViewById(R.id.weather);
        viewPager = (ViewPager)findViewById(R.id.pager);
		group = (ViewGroup) findViewById(R.id.viewGroup);
		
		viewPager.setOnPageChangeListener(pageChangeListener);
		
		menuView = (SlideMenu)findViewById(R.id.slideMenu);
		listView = (ListView)findViewById(R.id.leftmenu_list);
		
		menuAdapter = new LeftMenuAdapter(this);
		listView.setAdapter(menuAdapter);
        
        initDatas();
        
        br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals(Second1BroadcastReceiver.Msg_1S)) {
					//if(pagerAdapter != null) pagerAdapter.updateMenuAdapter();
				}else if(action.equals(Intent.ACTION_TIMEZONE_CHANGED)) { //时区改变
					TimeZone tz = TimeZone.getDefault();
					String zone = tz.getID();
					if(zone != null && !zone.equals(APP.timezoneId)) {
						APP.timezoneId = zone;
						DBconfig.UpdateConfig("DBOX", "TTIMEZONE", zone);
					}
				}else if(action.equals(Intent.ACTION_LOCALE_CHANGED)) { //语言改变
					String language = Locale.getDefault().getLanguage();
					if (!APP.language.equals(language)) {
						if (!language.equals("en")) {
							language = "zh";
						}

						APP.language = language;
						LanguageHelper.saveSelectLanguage(context, language);

						Intent it = new Intent(ActivityMenu.this, ActivitySleep.class);
						//it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ActivityMenu.this.startActivity(it);

						ActivityMenu.this.finish();

						//android.os.Process.killProcess(android.os.Process.myPid());
					}
				}
		    }
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Second1BroadcastReceiver.Msg_1S);
		intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
		intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
		this.registerReceiver(br, intentFilter);
    }
	
	@Override
    public void onDestroy()
	{
    	super.onDestroy();
    	
    	this.unregisterReceiver(br);
    }
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：初始化数据
	private void initDatas() {
		if(APP.weatherdata != null) {
        	//当前天气
			String weather = APP.weatherdata.getDate().split("实时：")[1];
			if (APP.language.equals("en")) {
				weather = weather.replace("℃)", "℃");
			}else
				weather = weather.replace("℃)", "度");

			//当前天气类型
			String type = APP.weatherdata.getWeather();
			if(type != null && type.length()>0) {
				type = LanguageHelper.changeLanguageWeather(type);
				weather = weather + ", "+ type;
			}
			
			weatherView.setText(weather);
        }
		
		//获取第1个线路的本月电量
		int number = 0;
		if(APP.distributbox.Breakers != null && APP.distributbox.Breakers.size()>0) {
			boolean isTotal = false;
			float totalMonthEle = 0; //本月总电量
			
			SimpleDateFormat dfym = new SimpleDateFormat("yyyyMM");
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -1); //上月
			String lastmonth = dfym.format(calendar.getTime());

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
			
			for(Breaker data : values) {
				String totalChannelId = data.totalChannelId;
				if((totalChannelId != null && totalChannelId.equals("-1"))) {
					isTotal = true;
					DBpower lm = DBpower.GetPowerrec(lastmonth, "MONTH", data.addr);
					if(lm != null) {
						//本月电量
						float ME = Math.abs(data.power-lm.getTotalpower());
						ME = (float)(Math.floor(ME*10))/10;
						totalMonthEle += ME;
					}else {
						totalMonthEle += data.power;
					}
				}
			}
			
			if(isTotal) {
				number = (int)totalMonthEle;
				number = Math.abs(number);
			}else {
				Breaker breaker = (Breaker)values.toArray()[0];
				int channel = breaker.addr;
				DBpower lm = DBpower.GetPowerrec(lastmonth, "MONTH", channel);
				
				//总路的本月电量
				if(lm==null) {
					number = (int)breaker.power;
				}else {
					float ele = Math.abs(breaker.power-lm.getTotalpower());
					number = (int) Math.floor(ele);
				}
				number = Math.abs(number);
			}
		}
		
		pagerAdapter = new MenuPagerAdapter(this, number);
		viewPager.setAdapter(pagerAdapter);

		if(currentPage > pagerAdapter.getCount()) {
			currentPage = 0;
		}
		viewPager.setCurrentItem(currentPage);
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：菜单按钮事件
	public void menuAction(View v) {
		menuView.toggleMenu();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：场景按钮事件
    public void sceneAction(View v) {
		Intent intent = new Intent(this, ActivityScene.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
    }
	
    // 方法类型：自定义方法
  	// 编 写：
  	// 方法功能：左边菜单选择事件
    public void menuClickAction(View v) {
    	int position = (Integer) v.getTag();
 		switch(position) {
 		   case 0: //主界面
		   {
		   	   menuView.closeMenu();
		   }
		   break;
 		   case 1: //修改电箱密码
		   {
			   Intent intent = new Intent(this, ActivityNetPassword.class);
			   intent.putExtra("currentPage", currentPage);
			   this.startActivity(intent);
			   this.finish();
		   }
		   break;
 		   case 2: //电箱安装指导
		   {
			   Intent intent = new Intent(this, ActivityInstallation.class);
			   intent.putExtra("currentPage", currentPage);
			   this.startActivity(intent);
			   this.finish();
		   }
		   break;
 		   case 3: //设置
		   {
			   Intent intent = new Intent(this, ActivitySetting.class);
			   intent.putExtra("currentPage", currentPage);
			   this.startActivity(intent);
			   this.finish();
		   }
		   break;
		   case 4: //通知
		   {
			   Intent intent = new Intent(this, ActivityNotices.class);
			   intent.putExtra("currentPage", currentPage);
			   this.startActivity(intent);
			   this.finish();
		   }
		   break;
 		   case 5: //电箱二维码
		   {
			   Intent intent = new Intent(this, ActivityQrCode.class);
			   intent.putExtra("currentPage", currentPage);
			   this.startActivity(intent);
			   this.finish();
		   }
		   break;
 		   case 6: //版本更新
		   {
			   /*String paramString = "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
		                "cat /sdcard/sndpad.apk > /system/app/sndpad.apk" +"\n"+
		                "mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
		                "rm /sdcard/MyWifi.apk" +"\n"+
		                "/system/bin/reboot";
			   SuDo.PutCmd(paramString);*/

			   /*String paramString = "mount -o rw,remount /system" +"\n"+
					   "cat /sdcard/sndpad.apk > /system/app/sndpad.apk" +"\n"+
					   "mount -o ro,remount /system" +"\n"+
					   "rm /sdcard/MyWifi.apk" +"\n"+
					   "/system/bin/reboot";
			   SuDo.PutCmd(paramString);*/

			   /*String paramString = "pm install -t -r /sdcard/sndpad.apk" +"\n"+
					   "rm /sdcard/sndpad.apk" +"\n"+
					   "/system/bin/reboot";
			   SuDo.PutCmd(paramString);*/


			   String appVersion = Tooles.getApkVersion(this);
			   if (appVersion != null && appVersion.length()>0
					&& Tooles.isNumber(appVersion)
					&& Double.parseDouble(appVersion) > Double.parseDouble(APP.Version)) {

				   String name = "sndpad"+appVersion+".apk";
				   final String path = APP.path + "/" + name;
				   File file = new File(path);
				   if (file.exists() && Tooles.getUninatllApkInfo(this, path)) {
					   AlertDialog alert = new MyAlertDialog(this)
							   .setTitle(LanguageHelper.changeLanguageText("提示"))
							   .setMessage(LanguageHelper.changeLanguageText("检测到有最新版本: ")+appVersion)
							   .setCancelable(false)
							   .setPositiveButton(LanguageHelper.changeLanguageText("立刻更新")
									   , new DialogInterface.OnClickListener()
									   {
										   public void onClick(DialogInterface dialog, int id)
										   {
											   dialog.cancel();

											   /*String appPath = "";
											   if (new File("/system/app/sndpad.apk").exists()) {
												   appPath = "/system/app/sndpad.apk";
											   }else if (new File("/system/priv-app/sndpad.apk").exists()) {
												   appPath = "/system/priv-app/sndpad.apk";
											   }

											   if(appPath.length()>0) {
												   String paramString = "mount -o rw,remount /system"+"\n"+
														   "cat "+path+" > "+appPath+"\n"+
														   "mount -o ro,remount /system"+"\n"+
														   "rm "+path+"\n"+
														   "/system/bin/reboot";
												   SuDo.PutCmd(paramString);
											   }*/

											   Tooles.installSilently(path);
										   }
									   })
							   .setNegativeButton(LanguageHelper.changeLanguageText("下次再说")
									   , new DialogInterface.OnClickListener()
									   {
										   public void onClick(DialogInterface dialog, int id)
										   {
											   dialog.cancel();
										   }
									   })
							   .show();

					   int message = this.getResources().getIdentifier("message","id","android");
					   TextView messageTextView = (TextView) alert.findViewById(message);
					   messageTextView.setTextSize(27);
					   alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
					   alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
				   }
			   }

		   }
		   break;
 		}
    }
	
    // 方法类型：自定义方法
 	// 编 写：
 	// 方法功能：按钮事件
    public void itemAction(View v) {
 		int position = (Integer) v.getTag();
 		switch(position) {
		   case 0: //智能空开
		   {
			   this.startActivity(new Intent(this, ActivityCtrlSwitch.class));
			   this.finish();
		   }
		   break;
		   case 1: //定时控制
		   {
			   this.startActivity(new Intent(this, ActivityTimer.class));
			   this.finish();
		   }
		   break;
		   case 2: //电量
		   {
			   this.startActivity(new Intent(this, ActivityPower.class));
			   this.finish();
		   }
		   break;
		   case 3: //一键开关
		   {
			   this.startActivity(new Intent(this, ActivityAKeySwitch.class));
			   this.finish();
		   }
		   break;
		   case 4: //漏保自检
		   {
			   this.startActivity(new Intent(this, ActivityLeak.class));
			   this.finish();
		   }
		   break;
		   case 5: //安全信息
		   {
			   this.startActivity(new Intent(this, ActivityAlarminfo.class));
			   this.finish();
		   }
		   break;
		   /*case 6: //视频
		   {
			   addControlView = new AddControlView(this, v);
			   addControlView.show();
		   }
		   break;
		   case 7: //可视门铃
		   {
			   this.startActivity(new Intent(this, ActivityInCall.class));
			   this.finish();
		   }
		   break;*/
		   case 6: //我的电器
		   {
			   this.startActivity(new Intent(this, ActivityEleAppState.class));
			   this.finish();
		   }
		   break;
		   case 7: //开关设置
		   {
			   this.startActivity(new Intent(this, ActivityWattSeting.class));
			   this.finish();
		   }
		   break;
		   case 8: //智慧面板
		   {
			   this.startActivity(new Intent(this, ActivityPanelControl.class));
			   this.finish();
		   }
		   break;
		   case 9: //Wi-Fi
		   {
		   	   Intent intent = new Intent();
		   	   //intent.setAction("android.net.wifi.PICK_WIFI_NETWORK"); //跳转到系统WIFI界面
			   intent.setAction(Settings.ACTION_SETTINGS); //跳转到系统设置界面
		   	   intent.putExtra("extra_prefs_show_button_bar", true);
		   	   intent.putExtra("extra_prefs_set_next_text", LanguageHelper.changeLanguageText("完成"));
		   	   intent.putExtra("extra_prefs_set_back_text", LanguageHelper.changeLanguageText("返回"));
		   	   //intent.putExtra("wifi_enable_next_on_connect", true);
			   //startActivity(intent);

			   APP.isWifiConnection = false;
			   startActivityForResult(intent, 888);
		   }
		   break;
 		}
    }
    
    // 方法类型：自定义方法
  	// 编 写：
  	// 方法功能：场景执行按钮事件
    public void controlAction(View v) {
		final SceneData data = (SceneData) v.getTag();
    	if(data != null) {
			List<SceneTaskData> taskDatas = DBSceneTask.getDatas(data.getAutoid());
			List<String> channels1 = new ArrayList<String>(); //合闸线路
			List<String> channels2 = new ArrayList<String>(); //分闸线路
			for (SceneTaskData taskata : taskDatas) {
				String channel = taskata.getChannel();
				int task = taskata.getTask();
				if (task == 1) { //合闸
					channels1.add(channel);
				}else //分闸
					channels2.add(channel);
			}

			Collections.sort(channels1, new Comparator<String>(){
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

			Collections.sort(channels2, new Comparator<String>(){
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

			final List<String> keys1 = channels1;
			final List<String> keys2 = channels2;

			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(resources.getString(R.string.alert30))
			.setTitle(resources.getString(R.string.tig7))
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

					//合闸
					for(String channel : keys1) {
						int channelId = Integer.parseInt(channel);

						int control = 1;
						DBswitchsetting setting = DBswitchsetting.getSwitchSetting(channelId);
						if(setting != null){
							control = setting.getControl();
						}

						if (control == 1) {
							for(int i = 0; i<3; i++) {
								SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, channelId, 0);
							}
						}

						Breaker breaker = APP.distributbox.Breakers.get(channelId);
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

					//分闸
					for(String channel : keys2) {
						int channelId = Integer.parseInt(channel);

						int control = 1;
						DBswitchsetting setting = DBswitchsetting.getSwitchSetting(channelId);
						if(setting != null){
							control = setting.getControl();
						}

						if (control == 1) {
							for(int i = 0; i<3; i++) {
								SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, channelId, 0);
							}
						}

						Breaker breaker = APP.distributbox.Breakers.get(channelId);
						if (breaker != null && breaker.title.length()>0) {
							String name = breaker.title;
							if(breaker.localLock) {
								if(name1s.length() == 0) {
									name1s = name;
								} else
									name1s = name1s+","+name;
							}else if(control == 0) {
								if(name3s.length() == 0) {
									name3s = name;
								} else
									name3s = name3s+","+name;
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
						Toast.makeText(ActivityMenu.this, message, Toast.LENGTH_LONG).show();
					}
				}
			})
			.setNeutralButton(resources.getString(R.string.tig17), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();

					int sceneId = (int)data.getAutoid();
					showDelayedPicker(3, sceneId);
				}
			})
			.setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			}).show();

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
  	//方法功能：显示页数的小圆点
    public void showPageCount(int count) {
    	if(group != null) {
    		group.removeAllViews();
    		
    		if(count == 1) {
    			return;
    		}
    		
    		if(currentPage > count-1) {
    			currentPage = count-1;
    		}
    		
    		for (int i = 0; i<count; i++) {
                LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                //设置每个小圆点距离左边的间距
                margin.setMargins(7, 0, 0, 0);
                margin.height = 13;
                margin.width = 13;
                ImageView imageView = new ImageView(this);
                if (i == currentPage) { // 默认选中第一张图片
                	imageView.setBackgroundResource(R.drawable.icon_dot_select);
                } else { //其他都设置未选中状态
                	imageView.setBackgroundResource(R.drawable.icon_dot_normal);
                }
                group.addView(imageView, margin);
            }
    	}
    }

	private void showDelayedPicker(final int type, final int sceneId) {
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
					Toast.makeText(ActivityMenu.this, resources.getString(R.string.toast31), Toast.LENGTH_LONG).show();
				}else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(new Date());
					calendar.add(Calendar.HOUR_OF_DAY, hour);
					calendar.add(Calendar.MINUTE, minute);
					calendar.add(Calendar.SECOND, second);

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = format.format(calendar.getTime());

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
    
    private OnPageChangeListener pageChangeListener = new OnPageChangeListener(){
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			
			currentPage = arg0;
			
			for (int i = 0; i < group.getChildCount(); i++) {
				ImageView imageView = (ImageView)group.getChildAt(i);
                if (arg0 == i) {
                    imageView.setBackgroundResource(R.drawable.icon_dot_select);
                }else
                	imageView.setBackgroundResource(R.drawable.icon_dot_normal);
            }
		}
	};

}
