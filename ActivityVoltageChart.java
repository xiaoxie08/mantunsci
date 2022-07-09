package snd.ui;

import java.util.List;

import snd.database.ChartData;
import snd.database.DBvoltage;
import snd.database.Voltage;
import snd.util.ScrollViewListener;
import snd.util.Tooles;
import snd.view.MyHorizontalScrollView;
import snd.view.MyScrollView;
import snd.view.SelectPopupWindow;
import snd.view.SelectPopupWindow.PopupListener;
import snd.view.Voltage1ChartView;
import snd.view.Voltage1XView;
import snd.view.Voltage1YView;
import snd.view.Voltage2ChartView;
import snd.view.Voltage2XView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityVoltageChart extends ActivityBase implements ScrollViewListener,PopupListener {

	private int channel;
	private TextView titleView;
	
	private TextView hour_yearView;
	private TextView hour_monthView;
	private TextView hour_dayView;
	private RelativeLayout hour_scrollBgView;
	private MyScrollView hour_scrollView1;
	private MyHorizontalScrollView hour_scrollView2;
	private MyScrollView hour_scrollView3;
	private MyHorizontalScrollView hour_scrollView4;
	private Voltage1ChartView hour_chartView;
	private Voltage1XView hour_xView;
	private Voltage1YView hour_yView;
	private RelativeLayout hour_loadBgView;
	
	private TextView day_yearView;
	private TextView day_monthView;
	private RelativeLayout day_scrollBgView;
	private MyScrollView day_scrollView1;
	private MyHorizontalScrollView day_scrollView2;
	private MyScrollView day_scrollView3;
	private MyHorizontalScrollView day_scrollView4;
	private Voltage2ChartView day_chartView;
	private Voltage2XView day_xView;
	private Voltage1YView day_yView;
	private RelativeLayout day_loadBgView;
	
	private Handler handler = new Handler(){  
        @Override  
        public void handleMessage(Message msg) {  
        	switch(msg.what) {
        	    case 1: { //获取到每小时的电压
        	    	hour_loadBgView.setVisibility(View.GONE);
        	    	
        	    	ChartData chartData = (ChartData) msg.getData().getSerializable("Data");
        	    	setHourData(chartData);
        	    }
        	    break;
                case 2: { //获取到每天的电压
                	day_loadBgView.setVisibility(View.GONE);
                	
                	ChartData chartData = (ChartData) msg.getData().getSerializable("Data");
                	setDayData(chartData);
        	    }
        	    break;
        	}
        }
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent intent = getIntent();
	    channel = intent.getIntExtra("channel", 1);
	    String name = intent.getStringExtra("name");
	    
	    setContentView(R.layout.voltagechart);
	    
	    titleView = (TextView)findViewById(R.id.title);
	    if(name != null && name.length()>0) {
	    	titleView.setText(name+"电压情况");
	    }
	    
	    {
	    	hour_yearView = (TextView)findViewById(R.id.hour_selectyear);
	    	hour_monthView = (TextView)findViewById(R.id.hour_selectmonth);
	    	hour_dayView = (TextView)findViewById(R.id.hour_selectday);
	    	
	    	hour_scrollBgView = (RelativeLayout)findViewById(R.id.hour_scroll_bg);
	    	hour_loadBgView = (RelativeLayout)findViewById(R.id.hour_loadingbg);
			
	    	hour_scrollView1 = (MyScrollView)findViewById(R.id.hour_scroll1);
	    	hour_scrollView2 = (MyHorizontalScrollView)findViewById(R.id.hour_scroll2);
	    	hour_chartView = (Voltage1ChartView)findViewById(R.id.hour_chart);
			
	    	hour_scrollView3 = (MyScrollView)findViewById(R.id.hour_scroll3);
	    	hour_yView = (Voltage1YView)findViewById(R.id.hour_y);
			
	    	hour_scrollView4 = (MyHorizontalScrollView)findViewById(R.id.hour_scroll4);
	    	hour_xView = (Voltage1XView)findViewById(R.id.hour_x);
	    	
	    	hour_scrollView1.setScrollViewListener(this);
	    	hour_scrollView2.setScrollViewListener(this);
	    	hour_scrollView3.setScrollViewListener(this);
	    	hour_scrollView4.setScrollViewListener(this);
	    	
	    	Time time = new Time();
	        time.setToNow();      
	        int year = time.year;      
	        int month = time.month+1;
	        int day = time.monthDay;
	        hour_yearView.setText(year+"");
	        hour_monthView.setText(month+"月");
	        hour_dayView.setText(day+"日");
	    }
	    
	    {
	    	day_yearView = (TextView)findViewById(R.id.day_selectyear);
	    	day_monthView = (TextView)findViewById(R.id.day_selectmonth);
	    	
	    	day_scrollBgView = (RelativeLayout)findViewById(R.id.day_scroll_bg);
	    	day_loadBgView = (RelativeLayout)findViewById(R.id.day_loadingbg);
			
	    	day_scrollView1 = (MyScrollView)findViewById(R.id.day_scroll1);
	    	day_scrollView2 = (MyHorizontalScrollView)findViewById(R.id.day_scroll2);
	    	day_chartView = (Voltage2ChartView)findViewById(R.id.day_chart);
			
	    	day_scrollView3 = (MyScrollView)findViewById(R.id.day_scroll3);
	    	day_yView = (Voltage1YView)findViewById(R.id.day_y);
			
	    	day_scrollView4 = (MyHorizontalScrollView)findViewById(R.id.day_scroll4);
	    	day_xView = (Voltage2XView)findViewById(R.id.day_x);
	    	
	    	day_scrollView1.setScrollViewListener(this);
	    	day_scrollView2.setScrollViewListener(this);
	    	day_scrollView3.setScrollViewListener(this);
	    	day_scrollView4.setScrollViewListener(this);
	    	
	    	Time time = new Time();
	        time.setToNow();      
	        int year = time.year;      
	        int month = time.month+1;
	        day_yearView.setText(year+"");
	        day_monthView.setText(month+"月");
	    }
	    
	    DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels/2-60;
		int height = 6*3*((width - 87)/7)/4+35;
		
		LayoutParams para = hour_loadBgView.getLayoutParams();
		para.height = height+50;
		hour_loadBgView.setLayoutParams(para);
		
		para = day_loadBgView.getLayoutParams();
		para.height = height+50;
		day_loadBgView.setLayoutParams(para);
	    
	    getChartData(1);
	    getChartData(2);
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
	}
	
	@Override
	public void onScrollChanged(View scrollView, int x, int y, int oldx,
			int oldy) {
		if (scrollView == hour_scrollView1) {  
			hour_scrollView3.scrollTo(x, y);
        }else if(scrollView == hour_scrollView2) {
        	hour_scrollView4.scrollTo(x, y);
        }else if(scrollView == hour_scrollView3) {
        	hour_scrollView1.scrollTo(x, y);
        }else if(scrollView == hour_scrollView4) {
        	hour_scrollView2.scrollTo(x, y);
        }else if (scrollView == day_scrollView1) {  
			day_scrollView3.scrollTo(x, y);
        }else if(scrollView == day_scrollView2) {
        	day_scrollView4.scrollTo(x, y);
        }else if(scrollView == day_scrollView3) {
        	day_scrollView1.scrollTo(x, y);
        }else if(scrollView == day_scrollView4) {
        	day_scrollView2.scrollTo(x, y);
        }
	}
	
	@Override
	public void onPopupClick(TextView textView) {
		if(textView == day_yearView || textView == day_monthView) { //获取每天电压
			getChartData(2);
		}else { //获取每小时电压
			getChartData(1);
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityRealtime.class));
		this.finish();
	}
	
	//方法类型：自定义方法
  	//编   写：
  	//方法功能：选择年份事件
	public void selectYearAction(View v) {
		String tag = (String) v.getTag();
		TextView textView = null;
		if(tag.equals("hour")) { //每小时电压
			textView = hour_yearView;
		}else { //每天电压
			textView = day_yearView;
		}
		
		if(textView != null) {
			SelectPopupWindow popup = new SelectPopupWindow(this, v, textView, 2);
			popup.setPopupListener(this);
			popup.show();
		}
	}
	
    //方法类型：自定义方法
  	//编   写：
  	//方法功能：选择月份事件
	public void selectMonthAction(View v) {
		String tag = (String) v.getTag();
		TextView textView = null;
		if(tag.equals("hour")) { //每小时电压
			textView = hour_monthView;
		}else { //每天电压
			textView = day_monthView;
		}
		
		if(textView != null) {
			SelectPopupWindow popup = new SelectPopupWindow(this, v, textView, 3);
			popup.setPopupListener(this);
			popup.show();
		}
	}
	
    //方法类型：自定义方法
  	//编   写：
  	//方法功能：选择日期事件
	public void selectDayAction(View v) {
		int maxDay = 31;
		String year = hour_yearView.getText().toString();
		String month = hour_monthView.getText().toString().replace("月", "");
		if(year.length()>0 && month.length()>0) {
		    try {
			   int y = Integer.parseInt(year);
			   int m = Integer.parseInt(month);
			   maxDay = Tooles.getMaxDayByYearMonth(y, m);
		    } catch(Exception e) {}
		}
		
		SelectPopupWindow popup = new SelectPopupWindow(this, v, hour_dayView, 4, maxDay);
		popup.setPopupListener(this);
		popup.show();
	}
	
	public void setHourData(ChartData chartData) {
		String[] AllData = null;
		int vNumber = 20;
		int min = 160;
		int max = 260;
		
		if(chartData != null) {
			AllData = chartData.getDatas();
			if (chartData.getMin() != 0 && chartData.getMax() != 0) {
				min = chartData.getMin();
				max = chartData.getMax();
	        }
		}
		if(min == 0 && max == 0) {
			min = 160;
			max = 260;
		}
		
		if (min > 0) {
            min = (min/vNumber)*vNumber;
        }
        if (max > 0) {
            max = ((max/vNumber)+1)*vNumber;
        }
        if ((max-min) < 120) {
        	int number = (120-(max-min))/2;
            if (min > number) {
                max += number;
                min -= number;
            }else {
                max += (2*number-min);
                min = 0;
            }
            max += 120-max+min;
        }
        if (min > 0 && max%20 != 0) {
            max -= 10;
            min -= 10;
        }
        String[] YLabels = new String[(max-min)/vNumber+1];
        for(int i=0; i<YLabels.length; i+=1) {
        	YLabels[i] = (min+vNumber*i)+"";
        }
		
		int number = YLabels.length-1;
		String[] XLabels = new String[]{"0:00","1:00","2:00","3:00","4:00","5:00","6:00","7:00"
				,"8:00","9:00","10:00","11:00","12:00","13:00","14:00","15:00"
				,"16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"}; //X轴刻度
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels/2-60;
		int height = 6*3*((width - 87)/7)/4+35;
		
		LayoutParams para = hour_scrollBgView.getLayoutParams();
		para.height = height;
		hour_scrollBgView.setLayoutParams(para);
		
		para = hour_loadBgView.getLayoutParams();
		para.height = height+50;
		hour_loadBgView.setLayoutParams(para);
		
		//折线图
		para = hour_scrollView1.getLayoutParams();
		para.width = width;
		para.height = height;
		hour_scrollView1.setLayoutParams(para);
		
		para = hour_scrollView2.getLayoutParams();
		para.width = width;
		para.height = height;
		hour_scrollView2.setLayoutParams(para);
		
		para = hour_chartView.getLayoutParams();
		para.width = 23*(width - 87)/7+87;
		para.height = number*3*((width - 87)/7)/4+35;
		hour_chartView.number = number;
		hour_chartView.setLayoutParams(para);
		
		//Y轴
		para = hour_scrollView3.getLayoutParams();
		para.width = 67;
		para.height = height;
		hour_scrollView3.setLayoutParams(para);
		
		para = hour_yView.getLayoutParams();
		para.width = 67;
		para.height = number*3*((width - 87)/7)/4+35;
		hour_yView.number = number;
		hour_yView.setLayoutParams(para);
		
		//X轴
		para = hour_scrollView4.getLayoutParams();
		para.width = width;
		para.height = 33;
		hour_scrollView4.setLayoutParams(para);
		
		para = hour_xView.getLayoutParams();
		para.width = 23*(width - 87)/7+87;
		para.height = 33;
		hour_xView.setLayoutParams(para);
		
		hour_chartView.setInfo(YLabels, AllData, 5);
		hour_yView.setInfo(YLabels);
		hour_xView.setInfo(XLabels);
		
		new Handler().post(new Runnable() {
		    @Override
		    public void run() {
		    	hour_scrollView3.fullScroll(ScrollView.FOCUS_DOWN);
		    	hour_scrollView4.fullScroll(ScrollView.FOCUS_LEFT);
		    }
		});
	}
	
	public void setDayData(ChartData chartData) {	
		String[] AllData = null;
		int vNumber = 20;
		int min = 160;
		int max = 260;
		
		if(chartData != null) {
			AllData = chartData.getDatas();
			if (chartData.getMin() != 0 && chartData.getMax() != 0) {
				min = chartData.getMin();
				max = chartData.getMax();
	        }
		}
		if(min == 0 && max == 0) {
			min = 160;
			max = 260;
		}
		
		if (min > 0) {
            min = (min/vNumber)*vNumber;
        }
        if (max > 0) {
            max = ((max/vNumber)+1)*vNumber;
        }
        if ((max-min) < 120) {
        	int number = (120-(max-min))/2;
            if (min > number) {
                max += number;
                min -= number;
            }else {
                max += (2*number-min);
                min = 0;
            }
            max += 120-max+min;
        }
        if (min > 0 && max%20 != 0) {
            max -= 10;
            min -= 10;
        }
        String[] YLabels = new String[(max-min)/vNumber+1];
        for(int i=0; i<YLabels.length; i+=1) {
        	YLabels[i] = (min+vNumber*i)+"";
        }
		
		int number = YLabels.length-1;
		String[] XLabels = new String[]{"1","2","3","4","5","6","7","8","9","10"
				,"11","12","13","14","15","16","17","18","19","20"
				,"21","22","23","24","25","26","27","28","29","30","31"}; //X轴刻度
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels/2-60;
		int height = 6*3*((width - 87)/7)/4+35;
		
		LayoutParams para = day_scrollBgView.getLayoutParams();
		para.height = height;
		day_scrollBgView.setLayoutParams(para);
		
		para = day_loadBgView.getLayoutParams();
		para.height = height+50;
		day_loadBgView.setLayoutParams(para);
		
		//折线图
		para = day_scrollView1.getLayoutParams();
		para.width = width;
		para.height = height;
		day_scrollView1.setLayoutParams(para);
		
		para = day_scrollView2.getLayoutParams();
		para.width = width;
		para.height = height;
		day_scrollView2.setLayoutParams(para);
		
		para = day_chartView.getLayoutParams();
		para.width = 30*(width - 87)/14+87;
		para.height = number*3*((width - 87)/7)/4+35;
		day_chartView.number = number;
		day_chartView.setLayoutParams(para);
		
		//Y轴
		para = day_scrollView3.getLayoutParams();
		para.width = 67;
		para.height = height;
		day_scrollView3.setLayoutParams(para);
		
		para = day_yView.getLayoutParams();
		para.width = 67;
		para.height = number*3*((width - 87)/7)/4+35;
		day_yView.number = number;
		day_yView.setLayoutParams(para);
		
		//X轴
		para = day_scrollView4.getLayoutParams();
		para.width = width;
		para.height = 33;
		day_scrollView4.setLayoutParams(para);
		
		para = day_xView.getLayoutParams();
		para.width = 30*(width - 87)/14+87;
		para.height = 33;
		day_xView.setLayoutParams(para);
		
		day_chartView.setInfo(YLabels, AllData, 5);
		day_yView.setInfo(YLabels);
		day_xView.setInfo(XLabels);
		
		new Handler().post(new Runnable() {
		    @Override
		    public void run() {
		    	day_scrollView3.fullScroll(ScrollView.FOCUS_DOWN);
		    	day_scrollView4.fullScroll(ScrollView.FOCUS_LEFT);
		    }
		});
	}	
	
	private void getChartData(int type) {
		if(type == 1) { //获取每小时电压
			hour_loadBgView.setVisibility(View.VISIBLE);
			
			String year = hour_yearView.getText().toString();
			String month = hour_monthView.getText().toString().replace("月", "");
			String day = hour_dayView.getText().toString().replace("日", "");
			if(month != null && month.length() == 1) {
				month = "0"+month;
			}
			if(day != null && day.length() == 1) {
				day = "0"+day;
			}
			
			String time = year+month+day;
			List<Voltage> voltage = DBvoltage.GetVoltageHoursList(channel, time);
			
			ChartData chartData = analyticalVoltageHour(voltage);
			Message msg = new Message();  
            msg.what = 1;
            Bundle b = new Bundle(); 
            b.putSerializable("Data", chartData);
            msg.setData(b); 
        	handler.sendMessage(msg);
        	
        	voltage.clear();
        	voltage = null;
		}else { //获取每天电压
			day_loadBgView.setVisibility(View.VISIBLE);
			
			String year = day_yearView.getText().toString();
			String month = day_monthView.getText().toString().replace("月", "");
			if(month != null && month.length() == 1) {
				month = "0"+month;
			}
			
			String time = year+month;
			List<Voltage> voltage = DBvoltage.GetVoltageDayList(channel, time);
			
			ChartData chartData = analyticalVoltageDay(voltage);
			Message msg = new Message();  
            msg.what = 2;
            Bundle b = new Bundle(); 
            b.putSerializable("Data", chartData);
            msg.setData(b); 
        	handler.sendMessage(msg);
			
        	voltage.clear();
        	voltage = null;
		}
	}
	
	private ChartData analyticalVoltageHour(List<Voltage> datas) {
		ChartData chartData = new ChartData();
		
		if(datas != null && datas.size()>0) {
			int min = 0;
        	int max = 0;
        	for(int i = 0; i < datas.size() ; i++){
        		Voltage data = datas.get(i);
        		int hour = data.getHours();
        		float number = data.getVoltage();
        		String voltage = number+"";
        		
        		int number1 = (int)number;
    			int number2 = (int)Math.round(number);
    			if(i == 0 || min > number1) {
    				min = number1;
    			}	            			
    			if(i == 0 || max < number2) {
    				max = number2;
    			}
    			
    			chartData.setData(hour, voltage);
        	}
        	chartData.setMin(min);
        	chartData.setMax(max);
		}
		
		return chartData;
	}
	
	private ChartData analyticalVoltageDay(List<Voltage> datas) {
		ChartData chartData = new ChartData();
		
		if(datas != null && datas.size()>0) {
			int min = 0;
        	int max = 0;
        	for(int i = 0; i < datas.size() ; i++){
        		Voltage data = datas.get(i);
        		int day = data.getDay()-1;
        		float number = data.getVoltage();
        		String voltage = number+"";
        		
        		int number1 = (int)number;
    			int number2 = (int)Math.round(number);
    			if(i == 0 || min > number1) {
    				min = number1;
    			}	            			
    			if(i == 0 || max < number2) {
    				max = number2;
    			}
    			
    			chartData.setData(day, voltage);
        	}
        	chartData.setMin(min);
        	chartData.setMax(max);
		}
		
		return chartData;
	}
	
}
