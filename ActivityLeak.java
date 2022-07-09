package snd.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import snd.database.Breaker;
import snd.database.DBconfig;
import snd.serialservice.SerialThread;
import snd.util.JudgeDate;
import snd.util.LanguageHelper;
import snd.util.WheelMain;
import snd.view.DateSelectView;
import snd.view.MyAlertDialog;
import snd.view.TotalSwitchView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityLeak extends ActivityBase {

	private Resources resources;
	
	private ImageButton autoCheckButton;
	private ImageButton checkButton;
	
	private RelativeLayout dateBgView;
	private TextView dateTitleView;
	private TextView dateView;
	
	private RelativeLayout timeBgView;
	private TextView timeTitleView;
	private TextView timeView;
	
	private Button leakButton;
	private TextView leakDateView;
	
	private String selectDate = "3";
	private String selectHour = "13";
	private String selectMins = "33";
	private String selectTime = "";

	private Handler handler = new Handler(){  
        @Override  
        public void handleMessage(Message msg) {  
        	switch(msg.what) {
                case 1: { //开始漏电自检
                	boolean check = (leakButton.getBackground() instanceof AnimationDrawable);
                	if(!check) {
                		leakButton.setBackgroundResource(R.drawable.animation_leak);
                    	AnimationDrawable animaition = (AnimationDrawable) leakButton.getBackground();
        				animaition.start();
                	}
                	
                	leakButton.setText(R.string.leak_tig8);
    				leakButton.setEnabled(false);
        	    }
        	    break;
                case 2: { //漏电自检成功
        	    	updateLeakState(true);
        	    }
        	    break;
                case 3: { //漏电自检失败
                	updateLeakState(false);
        	    }
        	    break;
        	}
        }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		resources = getResources();
	    
	    setContentView(R.layout.leak);
	    
	    autoCheckButton = (ImageButton)findViewById(R.id.leak_autoCheck);
	    checkButton = (ImageButton)findViewById(R.id.leak_Check);
	    dateBgView = (RelativeLayout)findViewById(R.id.date_bg);
	    dateTitleView = (TextView)findViewById(R.id.date_text);
	    dateView = (TextView)findViewById(R.id.date);
	    timeBgView = (RelativeLayout)findViewById(R.id.time_bg);
	    timeTitleView = (TextView)findViewById(R.id.time_text);
	    timeView = (TextView)findViewById(R.id.time);
	    leakButton = (Button)findViewById(R.id.leakButton);
	    leakDateView = (TextView)findViewById(R.id.leak_date);
	    
	    initDatas();
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：初始化数据
	private void initDatas() {
		String date = "";
		String time = "";
		String leakDate = "";
		boolean state = APP.execLeakCheck;
		String day = "3";
		String hour = "13";
		String minutes = "33";
		String dateTime = APP.lastleakCheckDate;
		boolean manualState = Boolean.parseBoolean(DBconfig.GetConfig("DBOX", "MANUALLEAKCHECK", "false"));
		
		if(APP.leakCheckDate != null && APP.leakCheckDate.contains(",") && APP.leakCheckDate.split(",").length == 3) {
			String strs[] = APP.leakCheckDate.split(","); 
			day = strs[0];
			hour = strs[1];
			minutes = strs[2];
			
			selectDate = day;
			selectHour = hour;
			selectMins = minutes;
		}
		
		if(day != null && day.length()>0) {
			date = "每月"+day+"号";
			date = LanguageHelper.changeLanguageLeakDay(date, day);
		}
		
		if(hour != null && hour.length() == 1) {
			hour = "0"+hour;
		}
		if(minutes != null && minutes.length() == 1) {
			minutes = "0"+minutes;
		}
		if(hour != null && hour.length() > 0
				&& minutes != null && minutes.length() > 0) {
			time = hour + ":" + minutes;
		}
		
		if(dateTime != null && dateTime.length()>0 && dateTime.contains(" ")) {
			String[] array = dateTime.split(" ");
	        dateTime = array[0];
	    }
		if(dateTime != null && dateTime.length()>0) {
			leakDate = LanguageHelper.changeLanguageText("上次自检时间: ")+dateTime;
		}
		
		autoCheckButton.setSelected(state);
		dateView.setText(date);
		timeView.setText(time);   		
		checkButton.setSelected(manualState);
		leakDateView.setText(leakDate);
		
		selectTime = time;
		
		updateAutoCheck();
    	updateCheck();
	}
	
	public void sendLeak(final Breaker breaker) {
		if(breaker == null) return;
		if(!breaker.OpenClose) {
			String name = breaker.title;
			String message = name+"已断路，请送电后再执行漏电检测！";
			message = LanguageHelper.changeLanguageText(message);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return;
		}
		
		AlertDialog alert = new MyAlertDialog(this)
		.setMessage(resources.getString(R.string.alert12))
		.setTitle(resources.getString(R.string.tig7))
		.setCancelable(false)
		.setPositiveButton(resources.getString(R.string.tig8),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
						handler.sendEmptyMessage(1);
						SerialThread.CmdQueue(SerialThread.CM_LEAK_TEST, breaker.addr, 0);
						SerialThread.CmdQueue(SerialThread.CM_LEAK_TEST, breaker.addr, 0);
						SerialThread.CmdQueue(SerialThread.CM_LEAK_TEST, breaker.addr, 0);
						handler.sendEmptyMessage(2);
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
		)
		.show();
		
		int message = this.getResources().getIdentifier("message","id","android");        
    	TextView messageTextView = (TextView) alert.findViewById(message);        
    	messageTextView.setTextSize(27); 
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	private void sendAuToLeak(boolean state) {
		autoCheckButton.setSelected(state);
		updateAutoCheck();
		
		DBconfig.UpdateConfig("DBOX", "LEAKCHECK", (state?"true":"false"));
		APP.execLeakCheck = state;
		
		if(state && (APP.leakCheckDate == null || APP.leakCheckDate.length()==0)) {
			if(selectDate == null) {
				selectDate = "3";
			}
			if(selectHour == null) {
				selectHour = "13";
			}
			if(selectMins == null) {
				selectMins = "33";
			}
			APP.leakCheckDate = selectDate+","+selectHour+","+selectMins;
		}
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
	//方法功能：打开自动漏电自检按钮事件
	public void autoCheckAction(View v) {
		if(check()) {
			final boolean state = !v.isSelected();
			if(state) {
				AlertDialog alert = new MyAlertDialog(this)
				.setMessage(resources.getString(R.string.alert13))
				.setTitle(resources.getString(R.string.tig7))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig9),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								
								sendAuToLeak(state);
							}
				        }
				)
				.setNegativeButton(resources.getString(R.string.tig4),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}
				)
				.show();
				
				int message = this.getResources().getIdentifier("message","id","android");        
		    	TextView messageTextView = (TextView) alert.findViewById(message);        
		    	messageTextView.setTextSize(27); 
				alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
			}else {
				sendAuToLeak(state);
			}
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：日期选择按钮事件
	public void dateAction(View v) {
		final DateSelectView view = new DateSelectView(this, selectDate);
		AlertDialog alert = new MyAlertDialog(this)
		.setTitle(resources.getString(R.string.alert14))
		.setView(view)
		.setCancelable(false)
		.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(!selectDate.equals(view.getDate())) {
					selectDate = view.getDate();
					String hour = selectHour;
					String min = selectMins;
					
					if(selectDate == null) {
						selectDate = "3";
					}
					if(hour == null) {
						hour = "13";
					}
					if(min == null) {
						min = "33";
					}
					
					String time = selectDate+","+hour+","+min;
					DBconfig.UpdateConfig("DBOX", "LEAKCHECKDATE", time);
					APP.leakCheckDate = time;

					String date = "每月"+ selectDate +"号";
					date = LanguageHelper.changeLanguageLeakDay(date, selectDate);
					dateView.setText(date);
					
					dialog.cancel();
				}
			}
		})
		.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
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
	//方法功能：时间选择按钮事件
    public void timeAction(View v) {
		String time = timeView.getText().toString();
		if(time.length() == 0) {
			time = "13:33";
		}
		selectTime = time;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		String format = "HH:mm";
		boolean hasSelectTime = false;
		Calendar calendar = Calendar.getInstance();

		if(!JudgeDate.isDate(time, format)){
			time = "00:00";
		}
		
		if(JudgeDate.isDate(time, format)){
			try {
				calendar.setTime(dateFormat.parse(time));
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View timepickerview = inflater.inflate(R.layout.timepicker, null);
				final WheelMain wheelMain = new WheelMain(timepickerview, hasSelectTime);
				DisplayMetrics metric = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metric);
				wheelMain.screenheight = metric.heightPixels;
				int year = 0;
				int month = 0;
				int day = 0;
				int hour = calendar.get(Calendar.HOUR_OF_DAY);//小时
				int minute = calendar.get(Calendar.MINUTE);//分
				wheelMain.initDateTimePicker(year, month, day, hour, minute);
				
				AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert15))
				.setView(timepickerview)
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(!selectTime.equals(wheelMain.getTime())) {
							selectTime = wheelMain.getTime();
							selectHour = wheelMain.getHour();
							selectMins = wheelMain.getMins();
							String day = selectDate;
							
							if(day == null) {
								day = "3";
							}
							if(selectHour == null) {
								selectHour = "13";
							}
							if(selectMins == null) {
								selectMins = "33";
							}
							
							String time = day+","+selectHour+","+selectMins;
							DBconfig.UpdateConfig("DBOX", "LEAKCHECKDATE", time);
							APP.leakCheckDate = time;
							
							timeView.setText(selectTime);
							
							dialog.cancel();
						}					
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
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
	//方法功能：打开手动漏电自检按钮事件
	public void checkAction(View v) {
		boolean state = !v.isSelected();
		v.setSelected(state);
		
		updateCheck();
		
		DBconfig.UpdateConfig("DBOX", "MANUALLEAKCHECK", (state?"true":"false"));
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：漏电自检按钮事件
	public void leakAction(View v) {
		if(check()) {
			if(APP.distributbox != null && APP.distributbox.leakBreakers.size()>0) {
				if(APP.distributbox.leakBreakers.size() == 1) {
					Breaker breaker = (Breaker) APP.distributbox.leakBreakers.values().toArray()[0];
					sendLeak(breaker);
				}else
					new TotalSwitchView.Builder(ActivityLeak.this).create().show();
			}
		}
	}
	
	private void updateAutoCheck() {
		boolean state = autoCheckButton.isSelected();
		int color = state?0xff77d0dd:0xffbdbfbf;
		dateBgView.setEnabled(state);
		timeBgView.setEnabled(state);
		dateTitleView.setTextColor(color);
		timeTitleView.setTextColor(color);
		dateView.setTextColor(color);
		timeView.setTextColor(color);
	}
	
	private void updateCheck() {
		boolean state = checkButton.isSelected();
		if(state) {
			leakButton.setVisibility(View.VISIBLE);
		}else
			leakButton.setVisibility(View.GONE);
	}
	
	private void updateLeakState(boolean state) {
    	if(state) {
    		leakButton.setText(R.string.leak_tig9);
    	}else
    		leakButton.setText(R.string.leak_tig10);
    	
    	handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				boolean check = (leakButton.getBackground() instanceof AnimationDrawable);
		    	if(check) {
		    		AnimationDrawable _animaition = (AnimationDrawable) leakButton.getBackground();
					if (_animaition.isRunning()) {
						_animaition.stop();// 停止
					}
		    	}
		    	leakButton.setBackgroundResource(R.drawable.leak);
				
				leakButton.setText(R.string.leak_tig7);
			    leakButton.setEnabled(true);
			}
		}, 7000);
    	
    	if(state) { //更新最新的手动漏电自检日期
    		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    		String date = sDateFormat.format(new Date());
        	
        	leakDateView.setText(LanguageHelper.changeLanguageText("上次自检时间: ")+date);
    	}
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		if(APP.distributbox == null || APP.distributbox.leakBreakers.size()==0) {
			Toast.makeText(this, resources.getString(R.string.toast13), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
}
