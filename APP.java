package snd.ui;

import seasnake.browser.BaiduWeatherData;
import seasnake.loger.Logger;
import seasnake.util.DES;
import seasnake.util.SuDo;
import snd.database.Breaker;
import snd.database.DB;
import snd.database.DBAlarmValue;
import snd.database.DBHistory;
import snd.database.DBLocalAdvert;
import snd.database.DBScene;
import snd.database.DBSceneTask;
import snd.database.DBalarminfo;
import snd.database.DBconfig;
import snd.database.DBcurrent;
import snd.database.DBcurrentsum;
import snd.database.DBdevices;
import snd.database.DBleakage;
import snd.database.DBleakagesum;
import snd.database.DBpower;
import snd.database.DBswitchsetting;
import snd.database.DBtemperature;
import snd.database.DBtemperaturesum;
import snd.database.DBtimer;
import snd.database.DBvoltage;
import snd.database.DBvoltagesum;
import snd.database.DistributBox;
import snd.dialog.DialogPopAlarm;
import snd.loger.JavaUtilLoggingConfig;
import snd.serialservice.SerialCmd;
import snd.serialservice.SerialThread;
import snd.util.Common;
import snd.util.IntefaceManager;
import snd.util.LanguageHelper;
import snd.util.ScreenListener;
import snd.util.ScreenListener.ScreenStateListener;
import snd.util.TTS;
import snd.util.Tooles;
import snd.view.MyAlertDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;

public class APP extends Application
{
    private static Logger log = Logger.getLogger(APP.class); //??????

	public static APP instance;

	/**
	 * L10(?????????)???a31ce072c5
	 * L10(?????????)???0761acc587
	 * L10S(?????????)???08f5dba1e4
	 * L10S(?????????)???00a239fbf0
	 **/
	public static final String APP_ID = "08f5dba1e4"; //bugly????????????appid
	
	public static String sdcard = Environment.getExternalStorageDirectory().getPath(); //sdcard??????
	public static String path = sdcard+"/snd"; //???????????????sdcard????????????

	public static String Default_Server = "http://pad.snd02.com:8080/data/carry"; //?????????????????????????????????
	//public static String Default_Server = "http://pad.sndtest.com:8080/data/carry"; //?????????????????????????????????
	//public static String Default_Server = "http://36.250.68.86:8081/ebx-bishop/data/carry";
	//public static String Default_Server = "http://interface.we-booming.com/zuul/data/carry"; //??????????????????????????????
	//public static String Default_Server = "http://stream.snd02.com:7760/data/carry"; //?????????????????????????????????????????????
	public static String Server = Default_Server; //???????????????

	public static String Version = "1.2"; //???????????????
	public static String HardWare = "L10S"; //????????????(803???804???805A???805C???L10???L10S)
	public static String PlatForm = "HARDWARE"; //HARDWARE
	public static String screenWidth = "1280"; //????????????????????????
	public static String screenHeight = "800"; //????????????????????????
	public static boolean isEngineering = false;
	public static boolean isBootCompleted = false; //?????????????????????
    public static boolean isGesturespassword = true; //????????????????????????
	public static boolean isInitSDK = false;

	public static String Default_SSID = ""; //?????????ssid???
	public static String Default_PWD = ""; //?????????ssid??????
	public static String Default_LoginPwd = "admin"; //?????????????????????
	public static String Default_ApPwd = "88888888"; //???????????????wifi????????????
	public static boolean isWifiAp = false; //??????wifi??????????????????
	public static boolean isWifiConnection = true; //??????????????????wifi??????
	
	public static String SSID = ""; //?????????ssid???
	public static String PWD = ""; //?????????ssid??????
	public static String MAC = ""; //?????????mac??????
	public static String LoginId = ""; //?????????
	public static String LoginPwd = ""; //????????????
	public static String LockScreenPwd = ""; //????????????
	public static String IP = ""; //??????IP
	public static String GATE = ""; 
	public static int PORT = 12345; //???????????????
	public static String language = "zh"; //?????????????????????

	public static int weathId; //??????????????????
	public static BaiduWeatherData weatherdata; //??????????????????
	public static boolean hasClient = false; //???????????????????????????????????????(???10???????????????2???)???
	public static boolean isUpdateReelData = false; //??????????????????????????????
	public static boolean execLeakCheck = false; //????????????
	public static String leakCheckDate = ""; //??????????????????
	public static String lastleakCheckDate = ""; //????????????????????????
	public static String timezoneId = ""; //??????Id

	public static DistributBox distributbox = new DistributBox();
	public static Queue<SerialCmd> queue = new LinkedList<SerialCmd>();
	public static Queue<SerialCmd> queue433 = new LinkedList<SerialCmd>();
	public static Queue<SerialCmd> queuedoor = new LinkedList<SerialCmd>();
	public static Queue<SerialCmd> queuepanel = new LinkedList<SerialCmd>();
	public static Queue<DBalarminfo> pushinfo = new LinkedList<DBalarminfo>();
	public static Queue<String> queueshell = new LinkedList<String>();
	public static Resources res;
	public static WifiManager wifi;
	public static ActivityManager activityManager;
	public static ConnectivityManager connmanager;
	public static PowerManager powermanage;
	public static AlarmManager alarmmanage;
	KeyguardManager keyguardManager;
	public static WakeLock wakelock;
	WifiLock wifilock;
	MulticastLock multicastlock;
	KeyguardLock keyguardLock;
	public static Activity activity;
	public static DialogPopAlarm popdialog;
	public static boolean isShowNewAlarm = false; //???????????????????????????????????????
	public static boolean isShowAlertDialog = false; //??????????????????????????????
	public static boolean isAdvertising = true; //??????????????????

	public static int panel_state = 1; //?????????????????????0???????????????1?????????2?????????3???????????????

    public static boolean isLog = false; //??????????????????

	public static boolean isNewScreen = true; //????????????????????????

    private ScreenListener screenListener;
	
	//??????????????????wifi??????
	public static List<Hashtable<String,String>> wifiDatas = new ArrayList<Hashtable<String,String>>();

	public static int ReelTimeOut = 0;

	private static String curProcessName = null;

	public static Map<String,Object> realDatas1 = new HashMap<String,Object>();
	public static Map<String,Object> realDatas2 = new HashMap<String,Object>();

	//??????????????????
	private boolean isInitLocation = true; //???????????????????????????
	private LocationClient mLocationClient = null;
	private MyLocationListener myListener = new MyLocationListener();
	public static String LATITUDE = ""; //??????
	public static String LONGITUDE = ""; //??????
	public static String ADDRSTR = ""; //??????????????????
	public static String CITY = ""; //??????????????????
	public static String SElE_CITY = ""; //??????????????????
	public static String DISTRICTID = ""; //???????????????????????????
	public static boolean INCN = true; //????????????????????????

	public Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case 1: {
					final String path = msg.getData().getString("path");
					String appVersion = msg.getData().getString("appVersion");

					if(APP.activity != null && path.length()>0) {
						AlertDialog alert = new MyAlertDialog(APP.activity)
								.setTitle(LanguageHelper.changeLanguageText("??????"))
								.setMessage(LanguageHelper.changeLanguageText("????????????????????????: ")+appVersion)
								.setCancelable(false)
								.setPositiveButton(LanguageHelper.changeLanguageText("????????????")
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
								.setNegativeButton(LanguageHelper.changeLanguageText("????????????")
										, new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int id)
											{
												dialog.cancel();
											}
										})
								.show();

						int message = APP.activity.getResources().getIdentifier("message","id","android");
						TextView messageTextView = (TextView) alert.findViewById(message);
						messageTextView.setTextSize(27);
						alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
						alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
					}
				}
				break;
			}
		}
	};
	
	@Override
	public void onCreate() 
	{
	    super.onCreate();

		String processName = getCurProcessName(this);
		String packageName = getPackageName();
		boolean isMainProcess = !TextUtils.isEmpty(packageName) && TextUtils.equals(packageName, processName);
		if(!isMainProcess && !TextUtils.isEmpty(processName) && !TextUtils.isEmpty(packageName)) {
			if(processName.equals("snd.ui:advertising") || processName.equals("snd.play.advertising")
				|| processName.equals("snd.play.advertising1") || processName.equals("snd.play.advertising2")) {
				DB.getInstance(this).getReadableDatabase();
			}
			return;
		}

		instance = this;
		ReelTimeOut = 0;

		distributbox.Breakers.clear();
		distributbox.totalBreakers.clear();
		distributbox.leakBreakers.clear();
		queue.clear();
		queue433.clear();
		queuedoor.clear();
		queuepanel.clear();
		pushinfo.clear();
		queueshell.clear();

		realDatas1.clear();
		realDatas2.clear();

		SerialThread.START_ADDRESS = false;
		SerialThread.END_ADDRESS = false;
	    
	    try {
	    	new JavaUtilLoggingConfig(this);
		}catch (IOException e) {e.printStackTrace();}
	    
	    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) 
			{
				log.error(arg1);
			}
		}); 
	    
	    DB.getInstance(this).getReadableDatabase();
	    this.res = this.getResources();

		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    alarmmanage = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	    keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        connmanager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		powermanage = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakelock = powermanage.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SND");
		wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
	    wifilock = wifi.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "SND");
	    multicastlock = wifi.createMulticastLock("SND");
	    keyguardLock = keyguardManager.newKeyguardLock("SND");
	    keyguardLock.disableKeyguard(); //????????????

	    multicastlock.acquire();
        wifilock.acquire();
        wakelock.acquire();
		if(wifi.getWifiState()==wifi.WIFI_STATE_DISABLED) wifi.setWifiEnabled(true);
		Settings.System.putInt(this.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);

		language = LanguageHelper.getSetLanguageLocale(this);

		new Thread(new Runnable()
        {
			@Override
			public void run() 
			{
				File file1 = new File(path);
				if(!file1.exists()) file1.mkdir();	
				
				//????????????????????????????????? (??????)
				timezoneId = DBconfig.GetConfig("DBOX", "TTIMEZONE", "Asia/Shanghai");
				if(timezoneId == null || timezoneId.length() == 0) {
					timezoneId = "Asia/Shanghai";
				}
				TimeZone tz = TimeZone.getDefault();  
		        String zone = tz.getID(); 
		        if(zone != null && !zone.equals(timezoneId)) {
		        	AlarmManager timeZone = (AlarmManager)getSystemService(ALARM_SERVICE);
		        	timeZone.setTimeZone(timezoneId);
		        }	  
			    
		        //????????????????????????????????????
				/*String now = DBconfig.GetConfig("DBOX", "DATETIME", "");
			    if(now != null && now.length()>0)
			    {
			    	try {
			    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    		String curDate = dateFormat.format(new Date());
			    		Date date1 = dateFormat.parse(curDate);
						Date date2 = dateFormat.parse(now);
						long seconds = (date2.getTime() - date1.getTime())/1000;
						if(seconds > 60 && !Tooles.isNetworkAvailable(APP.this)) {
							DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd.HHmmss");
							now = dateFormat1.format(date2);
							//SuDo.PutCmd("busybox date -s '"+now+"'");
							SuDo.PutCmd("/system/bin/date -s "+now);
							log.error("------????????????:"+now);
						}
			    	}catch (Exception e) {}
			    }*/

				//???????????????????????????
			    if(getMac() != null) MAC = getMac().replace(":", "").toUpperCase();
			    SSID = DBconfig.GetConfig("DBOX", "SSID", Default_SSID);
		        PWD = DBconfig.GetConfig("DBOX", "PWD", Default_PWD);
		        LoginId = DBconfig.GetConfig("DBOX", "LOGINID", MAC);
		        LoginPwd = DBconfig.GetConfig("DBOX", "LOGINPWD", Default_LoginPwd);
		        LockScreenPwd = DBconfig.GetConfig("DBOX", "LOCKSCREENPWD", "");
				Server = DBconfig.GetConfig("DBOX", "SERVER", Default_Server);
		        
		        if(LoginId == null || LoginId.length() == 0) LoginId = MAC;
		        if(LoginPwd == null || LoginPwd.length() == 0) LoginPwd = Default_LoginPwd;
				if(Server == null || Server.length() == 0) Server = Default_Server;

		        execLeakCheck = Boolean.parseBoolean(DBconfig.GetConfig("DBOX", "LEAKCHECK", "false"));
		        leakCheckDate = DBconfig.GetConfig("DBOX", "LEAKCHECKDATE", "");
		        lastleakCheckDate = DBconfig.GetConfig("DBOX", "LASTLEAKCHECKDATE", "");

				LATITUDE = DBconfig.GetConfig("DBOX", "LATITUDE", "");
				LONGITUDE = DBconfig.GetConfig("DBOX", "LONGITUDE", "");
				ADDRSTR = DBconfig.GetConfig("DBOX", "ADDRSTR", "");
				CITY = DBconfig.GetConfig("DBOX", "CITY", "?????????");
				SElE_CITY = DBconfig.GetConfig("DBOX", "SElECITY", "");
				DISTRICTID = DBconfig.GetConfig("DBOX", "DISTRICTID", "");
		        
		        //???????????????????????????????????????????????????
		        List<DBconfig> onlyctrl = DBconfig.GetConfigList(" WHERE type='"+Breaker.Breaker433ToInfrared+"' ");
		        for(DBconfig c : onlyctrl)
		        {
		        	int addr = Byte.parseByte(c.getName().replace("SWITCH", ""));
		        	String name = c.getValue();
		        	if(name==null || name=="") name = "???????????????"+addr;
		        	if(!APP.distributbox.Breakers.containsKey(addr))
		    		{
		    			Breaker breaker = new Breaker(addr,name);
		    			breaker.model = Breaker.Breaker433ToInfrared;
		    			APP.distributbox.Breakers.put(addr, breaker);
		    		}
		        }
			}
        }).start();
		
        screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenStateListener() {

			@Override
			public void onScreenOn() {
				// TODO Auto-generated method stub
				if(APP.activity != null && APP.activity.toString().startsWith("snd.ui.ActivitySleep")) {
					Intent intent = new Intent();
			        intent.setAction("com.show.advertising");
			        sendBroadcast(intent);
				}
			}

			@Override
			public void onScreenOff() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onUserPresent() {
				// TODO Auto-generated method stub
			}
        	
        });
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		if(screenListener != null) {
			screenListener.unregisterListener();
			screenListener = null;
		}

		TTS.getInstance(this).stop();

		instance = null;
		curProcessName = null;

		distributbox.Breakers.clear();
		distributbox.totalBreakers.clear();
		distributbox.leakBreakers.clear();
		queue.clear();
		queue433.clear();
		queuedoor.clear();
		queuepanel.clear();
		pushinfo.clear();
		queueshell.clear();

		realDatas1.clear();
		realDatas2.clear();
		realDatas1 = null;
		realDatas2 = null;
	}

	//??????????????????????????????
	//???   ??????
	//????????????????????????????????????????????????SDK
	public synchronized void initSDK() {
		if(Tooles.checkNetState(APP.this) != 0 && isInitLocation) { //?????????????????????
			isInitLocation = false;

			//???????????????????????????
			TTS.getInstance(this);

			//????????????????????????
			initLocation();

			//?????????Bugly??????????????????
			initBuglyUpgrade();

			//??????????????????????????????
			initCloudChannel(this);

			isInitSDK = true;

			//??????????????????
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String curDate = dateFormat.format(new Date());
			if (curDate.equals("1970-01-02")) {
				IntefaceManager.sendSysTime();
			}

            //Tooles.getSimSerialNumber(this);
		}
	}

	//??????????????????????????????
	//???   ??????
	//???????????????????????????????????????
	private void initLocation() {
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setIsNeedAddress(true);
		option.setNeedNewVersionRgc(true);
		option.setIgnoreKillProcess(false);
		option.SetIgnoreCacheException(false);
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	//??????????????????????????????
	//???   ??????
	//????????????????????????Bugly??????????????????
	private void initBuglyUpgrade() {
		Beta.autoInit = true; //????????????????????????
		Beta.autoCheckUpgrade = true; //????????????????????????
		Beta.upgradeCheckPeriod = 60 * 1000; //????????????????????????
		Beta.initDelay = 10 * 1000; //???????????????
		//Beta.storageDir = new File(path); //??????sd??????snd???????????????????????????
		Beta.canShowApkInfo = false; //??????????????????????????????apk??????
		Beta.enableHotfix = false; //?????????????????????

		//??????????????????
        Beta.upgradeListener = new UpgradeListener() {
            @Override
            public void onUpgrade(int ret, UpgradeInfo strategy, boolean isManual, boolean isSilence) {
                if (strategy != null) {
					long appVersionCode = Tooles.getAppVersionCode(APP.this.getApplicationContext());
                	int versionCode = strategy.versionCode;
					String versionName = strategy.versionName;
					String apkUrl = strategy.apkUrl;
					long appSize = strategy.fileSize;

					/*System.out.println("-------appVersionCode: "+appVersionCode);
					System.out.println("-------versionCode: "+versionCode);
					System.out.println("-------versionName: "+versionName);
					System.out.println("-------?????????????????????: "+apkUrl);
					System.out.println("-------???????????????: "+appSize);*/

					if (appSize>0 && versionCode>appVersionCode && apkUrl.length()>0) {
						Common.goToDownloadApk(apkUrl, versionName, APP.this.getApplicationContext());
					}
                }
            }
        };

		Bugly.init(getApplicationContext(), APP_ID, false);
	}

	//??????????????????????????????
	//???   ??????
	//???????????????????????????????????????
	private void initCloudChannel(Context applicationContext) {
		PushServiceFactory.init(applicationContext);

		CloudPushService pushService = PushServiceFactory.getCloudPushService();
		pushService.register(applicationContext, new CommonCallback() {
			@Override
			public void onSuccess(String response) {

			}

			@Override
			public void onFailed(String errorCode, String errorMessage) {

			}
		});

		//????????????
		if (MAC != null && MAC.length()>0) {
			pushService.bindAccount(MAC, new CommonCallback() {
				@Override
				public void onSuccess(String response) {
					
				}

				@Override
				public void onFailed(String errorCode, String errorMessage) {

				}
			});
		}

		//????????????
		pushService.addAlias(PlatForm, new CommonCallback() {
			@Override
			public void onSuccess(String response) {

			}

			@Override
			public void onFailed(String errorCode, String errorMessage) {

			}
		});
	}

	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????
	public static void restory()
	{
		log.debug("??????????????????");
		 
   		DBconfig.Clear();
   		DBalarminfo.VirtualClear();
   		DBdevices.VirtualClear();
   		DBtimer.VirtualClear();
   		DBswitchsetting.Clear();

   		DBvoltagesum.Clear();
   		DBvoltage.VirtualClear();

		DBcurrentsum.Clear();
		DBcurrent.VirtualClear();

		DBtemperaturesum.Clear();
		DBtemperature.VirtualClear();

		DBleakagesum.Clear();
		DBleakage.VirtualClear();
		
		/*for(Breaker breaker : distributbox.Breakers.values()) {
			int addr = breaker.addr;
			DBconfig.UpdateConfig("POWER", String.valueOf(addr), String.valueOf(0));
			DBconfig.UpdateConfig("LASTPOWER", String.valueOf(addr), String.valueOf(0));
		}*/
		DBpower.VirtualClear();

		DBScene.Clear();
		DBSceneTask.Clear();

		DBAlarmValue.Clear();

		DBHistory.Clear();

		DBLocalAdvert.Clear();
 
   		//SuDo.PutCmd("/system/bin/reboot -p");
   		SuDo.PutCmd("/system/bin/reboot");
	}

	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????
	public static String getRealData(boolean isAlarm, Context context)
	{
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String datetime = dateformat.format(new Date());
		/*SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH");
		Date date = new Date();
		String datetime = dateformat.format(date);
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    int minute = calendar.get(Calendar.MINUTE);
	    minute = (minute/10)*10;
	    String min = ":"+minute;
	    if(minute < 10) min = ":0"+minute;
	    datetime = datetime + min + ":00";*/
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("version", APP.Version);
		map.put("hardware", APP.HardWare);
		map.put("server", APP.Server);
		map.put("ssid", APP.SSID);
		map.put("ssidpwd", DES.MD5_16(PWD));
		map.put("ip", APP.IP);
		map.put("gate", APP.GATE);
		map.put("port", String.valueOf(APP.PORT));
		map.put("mac", APP.MAC);
		map.put("loginid", APP.LoginId);
		map.put("loginpwd", DES.MD5_16(LoginPwd));
		map.put("execleakcheck", String.valueOf(APP.execLeakCheck));
		map.put("leakcheckdate", APP.leakCheckDate);
		map.put("lastleakcheckdate", APP.lastleakCheckDate);
		map.put("datetime", datetime);
		map.put("timezoneId", timezoneId);
		map.put("panelState", panel_state+"");

		if (CITY.length()>0) map.put("city", CITY); //??????
		if (LATITUDE.length()>0) map.put("latitude", LATITUDE); //??????
		if (LONGITUDE.length()>0) map.put("longitude", LONGITUDE); //??????

		//?????????????????????????????????????????????????????????????????????????????????
		if(!isAlarm) {
			Calendar cal = Calendar.getInstance();
			int timeZone = cal.getTimeZone().getOffset(System.currentTimeMillis())/3600000;
			if (timeZone != 8) {
				String zone = "";
				if (timeZone >= 0) {
					zone = String.valueOf("+" + timeZone);
				}else
					zone = String.valueOf(timeZone);

				Date date = new Date();
				TimeZone oldZone = TimeZone.getTimeZone("GMT" + zone);
				TimeZone newZone = TimeZone.getTimeZone("GMT+8");
				int timeOffset = oldZone.getRawOffset()-newZone.getRawOffset();
				Date dateTmp = new Date(date.getTime() - timeOffset);
				String newDatetime = dateformat.format(dateTmp);
				map.put("datetime", newDatetime);
			}
		}

		
		Map<String,Object> realdata = new HashMap<String,Object>();
		realdata.put("serverinfo", map);
		//realdata.put("distributbox", distributbox);

		{
			Map<String,Object> datas = new HashMap<String,Object>();

			if (distributbox.Breakers != null) {
				if(!isAlarm) realDatas2.clear();

				ArrayList<Breaker> values = new ArrayList<Breaker>(distributbox.Breakers.values());
				Map<String,Object> Breakers = getBreakers(values, isAlarm, datetime, context);
				datas.put("Breakers", Breakers);
			}

			/*if (distributbox.totalBreakers != null) {
				ArrayList<Breaker> values = new ArrayList<Breaker>(distributbox.totalBreakers.values());
				Map<String,Object> totalBreakers = getBreakers(values);
				datas.put("totalBreakers", totalBreakers);
			}*/

			realdata.put("distributbox", datas);
		}
		
		if(isAlarm) { //??????????????????????????????
			DBalarminfo alarmData = DBalarminfo.GetNewAlarm();
			realdata.put("alarm", alarmData);
		}

		Gson gson = new Gson();
		return gson.toJson(realdata);
	}

	private static Map<String,Object> getBreakers(ArrayList<Breaker> values, boolean isLan, String datetime, Context context) {
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

		Map<String,Object> datas = new HashMap<String,Object>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for(Breaker breaker : values) {
			String key = breaker.addr+"";
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("addr", breaker.addr);
			data.put("title", breaker.title);
			data.put("version", breaker.version);
			if(breaker.version2 > 0) data.put("version2", breaker.version2);
			data.put("model", breaker.model);
			if(breaker.model2 > 0) data.put("model2", breaker.model2);
			data.put("lineType", breaker.lineType);
			data.put("Configuration", breaker.Configuration);
			data.put("specification", breaker.specification);
			data.put("control", breaker.control);
			data.put("visibility", breaker.visibility);
			data.put("autoClose", breaker.autoClose);
			data.put("totalChannelId", breaker.totalChannelId);

			if(breaker.EnableAlarm >= 0) data.put("AlarmEnable", breaker.EnableAlarm);
			if(breaker.EnableTrip >= 0) data.put("CloseForAlarmEn", breaker.EnableTrip);
			if(breaker.EnableOpnen >= 0) data.put("RmtOpenAfterAlertEn", breaker.EnableOpnen);
			if(breaker.switchID.length() > 0) data.put("switchID", breaker.switchID);

			if(breaker.inverseCurve >= 0) data.put("inverseCurve", breaker.inverseCurve);
			if(breaker.leakageGear >= 0) data.put("leakageGear", breaker.leakageGear);
			if(breaker.firAlrSens >= 0) data.put("firAlrSens", breaker.firAlrSens);
			if(breaker.firAlrSens2 >= 0) data.put("firAlrSens2", breaker.firAlrSens2);

			data.put("EnableNetCtrl", breaker.EnableNetCtrl);
			data.put("OpenClose", breaker.OpenClose);
			data.put("power", breaker.power);
			data.put("Alarm", breaker.Alarm);

			if(breaker.frequency >= 0) data.put("frequency", breaker.frequency);
			data.put("localLock", breaker.localLock);
			data.put("remoteLock", breaker.remoteLock);

			data.put("MXDW", breaker.MXDW);
			data.put("MXGG", breaker.MXGG);
			data.put("MXGL", breaker.MXGL);
			data.put("MXGW", breaker.MXGW);
			data.put("MXGY", breaker.MXGY);
			data.put("MXLD", breaker.MXLD);
			data.put("MXQY", breaker.MXQY);

			data.put("A_V", breaker.A_V);
			data.put("A_A", breaker.A_A);
			data.put("A_T", breaker.A_T);
			data.put("A_LD", breaker.A_LD);
			data.put("A_P", breaker.A_P);
			data.put("A_WP", breaker.A_WP);
			data.put("A_PF", breaker.A_PF);
			//data.put("A_Alarm", breaker.A_Alarm);

			if (breaker.lineType.equals("380")) {
				data.put("B_V", breaker.B_V);
				data.put("B_A", breaker.B_A);
				data.put("B_T", breaker.B_T);
				data.put("B_P", breaker.B_P);
				data.put("B_WP", breaker.B_WP);
				data.put("B_PF", breaker.B_PF);
				//data.put("B_Alarm", breaker.B_Alarm);

				data.put("C_V", breaker.C_V);
				data.put("C_A", breaker.C_A);
				data.put("C_T", breaker.C_T);
				data.put("C_P", breaker.C_P);
				data.put("C_WP", breaker.C_WP);
				data.put("C_PF", breaker.C_PF);
				//data.put("C_Alarm", breaker.C_Alarm);

				data.put("G_V", breaker.G_V);
				data.put("G_A", breaker.G_A);
				data.put("G_T", breaker.G_T);
				data.put("G_LD", breaker.G_LD);
				data.put("G_P", breaker.G_P);
				data.put("G_WP", breaker.G_WP);
				data.put("G_PF", breaker.G_PF);

				data.put("N_A", breaker.N_A);
				data.put("N_T", breaker.N_T);

				if(breaker.currentWarnA >= 0) data.put("currentWarnA", breaker.currentWarnA);
				if(breaker.currentWarnB >= 0) data.put("currentWarnB", breaker.currentWarnB);
				if(breaker.currentWarnC >= 0) data.put("currentWarnC", breaker.currentWarnC);

				if(breaker.currentAlarmA >= 0) data.put("currentAlarmA", breaker.currentAlarmA);
				if(breaker.currentAlarmB >= 0) data.put("currentAlarmB", breaker.currentAlarmB);
				if(breaker.currentAlarmC >= 0) data.put("currentAlarmC", breaker.currentAlarmC);

				if(breaker.powerAlarmA >= 0) data.put("PowerAlarmA", breaker.powerAlarmA);
				if(breaker.powerAlarmB >= 0) data.put("PowerAlarmB", breaker.powerAlarmB);
				if(breaker.powerAlarmC >= 0) data.put("PowerAlarmC", breaker.powerAlarmC);
			}

			if(breaker.VoltWarnUp >= 0) data.put("VoltWarnUp", breaker.VoltWarnUp);
			if(breaker.VoltWarnDn >= 0) data.put("VoltWarnDn", breaker.VoltWarnDn);
			if(breaker.leakWarnUp >= 0) data.put("leakWarnUp", breaker.leakWarnUp);
			if(breaker.TemprWarn >= 0) data.put("TemprWarn", breaker.TemprWarn);
			if(breaker.crrentWarn >= 0) data.put("crrentWarn", breaker.crrentWarn);

			if(breaker.MalLoadAdjPowerDn >= 0) data.put("MalLoadAdjPowerDn", breaker.MalLoadAdjPowerDn);
			if(breaker.MalLoadResPowerDn >= 0) data.put("MalLoadResPowerDn", breaker.MalLoadResPowerDn);

			boolean isAdd = false;
			int NetState = 2;
			if(context != null) NetState = Tooles.checkNetState(context);
			if (isLan || !realDatas1.containsKey(key) || NetState == 2) {
				isAdd = true;
			}else if(realDatas1.containsKey(key)) {
				Map<String,Object> data1 = (Map<String, Object>) realDatas1.get(key);
				String updateTime = (String) data1.get("updateTime");
				data1.remove("updateTime");

				if (!data1.equals(data)) {
					isAdd = true;
				}else {
					try {
						Date date1 = dateFormat.parse(datetime);
						Date date2 = dateFormat.parse(updateTime);
						long seconds = (date1.getTime() - date2.getTime())/1000;
						if(seconds >= 1200) isAdd = true;
					}catch (Exception e) {}
				}

				data1.put("updateTime", updateTime);
			}

			if(isAdd) {
				datas.put(key, data);

				if(!isLan) {
					Map<String,Object> data1 = new HashMap<String,Object>();
					data1.putAll(data);
					data1.put("updateTime", datetime);
					realDatas2.put(key, data1);
				}
			}
		}

		return datas;
	}

	public String AudioUriToFile(Uri uri)
	{
		Cursor cursor = null;
		String[] proj = {MediaStore.Audio.Media.DATA};  
		try
		{
			cursor = this.getContentResolver().query(uri,proj,null,null,null);
			if (cursor != null && cursor.moveToFirst()) 
			{  
				int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);  
				return cursor.getString(index);  
			}  
		}
		finally
		{
			if(cursor!=null) cursor.close();
		}
		
		return null;
	}

	//??????????????????????????????
	//???   ??????
	//?????????????????????MAC??????
	public String getMac() 
	{
        String macSerial = null;
        try 
        {
        	Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            String str="";
            for (; null != str;) 
            {
            	str = input.readLine();
                if (str != null) 
                {
                	macSerial = str.trim();// ?????????
                    break;
                }
            }
        } 
        catch (Exception ex){}

		checkEth0(macSerial);
        
        return macSerial;
    }

	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????MAC????????????????????????MAC???????????????????????????????????????????????????MAC??????????????????????????????MAC????????????
	public static void checkEth0(String macSerial) {
		/*if (macSerial != null && macSerial.length()>0) {
			macSerial = macSerial.toUpperCase();
			String mac = getMacAddress();
			if (mac != null && mac.length()>0 && !mac.equals(macSerial)) {
				String paramString = "netcfg eth0 down"+"\n"+
						"netcfg eth0 hwaddr "+ macSerial +"\n"+
						"netcfg eth0 up";
				SuDo.PutCmd(paramString);
			}
		}*/
	}

	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????MAC??????
	public static String getMacAddress(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}

	public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
	}
	
	//??????????????????????????????
    //???   ??????
    //???????????????app?????????????????????
	public static boolean isAppForground(Context mContext) {
		try {
			if (activityManager == null) {
				activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			}

			List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
			if (!tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
					return false;
				}
			}
		}catch(Exception e){
			return true;
		}

	    return true;
	}

	//??????????????????????????????
	//???   ??????
	//??????????????????????????????????????????????????????
	public static boolean isAdvertising(Context mContext) {
		try {
			if (activityManager == null) {
				activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			}

			List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
			if (!tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				if (topActivity.getClassName().equals("snd.ui.ActivityAdvertising")
					|| topActivity.getClassName().equals("snd.ui.ActivityAdvertising1")
					|| topActivity.getClassName().equals("snd.ui.ActivityAdvertising2")) {
					return true;
				}
			}
		}catch(Exception e){
			return false;
		}

		return false;
	}

	//??????????????????????????????
	//???   ??????
	//??????????????????????????????????????????????????????
	public static boolean isSystemSetting(Context mContext) {
		try {
			if (activityManager == null) {
				activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			}

			List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
			if (!tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				if (topActivity.getPackageName().equals("com.android.settings")) {
					return true;
				}
			}
		}catch(Exception e){
			return false;
		}

		return false;
	}

	private static String getCurProcessName(Context context) {
		if (!TextUtils.isEmpty(curProcessName)) {
			return curProcessName;
		}

		curProcessName = getProcessName(android.os.Process.myPid());
		if (!TextUtils.isEmpty(curProcessName)) {
			return curProcessName;
		}

		try {
			int pid = android.os.Process.myPid();

			curProcessName = getProcessName(pid);
			if (!TextUtils.isEmpty(curProcessName)) {
				return curProcessName;
			}

			//???????????????ActivityManager??????
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			if (am == null) {
				return curProcessName;
			}

			for (ActivityManager.RunningAppProcessInfo appProcess : am.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
					curProcessName = appProcess.processName;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return curProcessName;
	}

	private static String getProcessName(int pid) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
			String processName = reader.readLine();
			if (!TextUtils.isEmpty(processName)) {
				processName = processName.trim();
			}

			return processName;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private class MyLocationListener extends BDAbstractLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location){
			double latitude = location.getLatitude();   //??????????????????
			double longitude = location.getLongitude(); //??????????????????

			String address = location.getAddrStr();   //????????????????????????
			String country = location.getCountry();   //????????????
			String province = location.getProvince(); //????????????
			String city = location.getCity();         //????????????
			String district = location.getDistrict(); //????????????
			String town = location.getTown();         //??????????????????
			String street = location.getStreet();     //??????????????????
			String adcode = location.getAdCode();     //????????????????????????
			int where = location.getLocationWhere();  //????????????????????????????????????

			//??????????????????????????????????????????????????????????????????????????????BDLocation???????????????
			int errorCode = location.getLocType();

			if (errorCode == 61 || errorCode == 66 || errorCode == 161) {
				LATITUDE = latitude+"";
				LONGITUDE = longitude+"";
				DBconfig.UpdateConfig("DBOX","LATITUDE", LATITUDE);
				DBconfig.UpdateConfig("DBOX","LONGITUDE", LONGITUDE);

				if(address != null && address.length()>0) {
					ADDRSTR = address;
					DBconfig.UpdateConfig("DBOX","ADDRSTR", ADDRSTR);
				}

				if(city != null && city.length()>0) {
					CITY = city;
					DBconfig.UpdateConfig("DBOX","CITY", CITY);
				}

				if(adcode != null && adcode.length()>0) {
					DISTRICTID = adcode;
					DBconfig.UpdateConfig("DBOX","DISTRICTID", DISTRICTID);
				}

				INCN = (where == BDLocation.LOCATION_WHERE_OUT_CN)?false:true;

				log.info("---------?????????"+LATITUDE);
				log.info("---------?????????"+LONGITUDE);
				log.info("---------?????????????????????"+ADDRSTR);
				log.info("---------?????????"+country);
				log.info("---------?????????"+province);
				log.info("---------?????????"+CITY);
				log.info("---------?????????"+district);
				log.info("---------???????????????"+town);
				log.info("---------???????????????"+street);
				log.info("---------?????????????????????"+DISTRICTID);
				log.info("---------?????????????????????"+(INCN?"??????":"??????"));
			}

			if(mLocationClient != null && mLocationClient.isStarted()) mLocationClient.stop();
		}
	}

}
