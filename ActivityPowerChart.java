package snd.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import snd.database.DBpower;
import snd.database.Power;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.MyMarkerView;
import snd.view.SelectPopupWindow;
import snd.view.SelectPopupWindow.PopupListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;

public class ActivityPowerChart extends ActivityBase implements PopupListener {

	private int channel;
	private TextView titleView;

	private TextView hour_yearView;
	private TextView hour_monthView;
	private TextView hour_dayView;

	private TextView day_yearView;
	private TextView day_monthView;

	private LineChart hourChartView;
	private LineChart dayChartView;
	private RelativeLayout hour_loadBgView;
	private RelativeLayout day_loadBgView;

	private List<String> activities1 = new ArrayList<String>();
	private List<Entry> entries1 = new ArrayList<Entry>();

	private List<String> activities2 = new ArrayList<String>();
	private List<Entry> entries2 = new ArrayList<Entry>();

	private float maxHourNumber = 0;
	private float maxDayNumber = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent intent = getIntent();
	    channel = intent.getIntExtra("channel", 1);
	    String name = intent.getStringExtra("name");
	    
	    setContentView(R.layout.powerchart);
	    
	    titleView = (TextView)findViewById(R.id.title);
	    if(name != null && name.length()>0 && APP.language.equals("zh")) {
	    	titleView.setText(name+"用电情况");
	    }
	    
	    {
	    	hour_yearView = (TextView)findViewById(R.id.hour_selectyear);
	    	hour_monthView = (TextView)findViewById(R.id.hour_selectmonth);
	    	hour_dayView = (TextView)findViewById(R.id.hour_selectday);

			hourChartView = (LineChart) findViewById(R.id.chart1);
	    	hour_loadBgView = (RelativeLayout)findViewById(R.id.hour_loadingbg);

			hourChartView.getLegend().setEnabled(false);
			hourChartView.getDescription().setEnabled(false);
			hourChartView.getAxisRight().setEnabled(false);
			hourChartView.setDrawGridBackground(false);
			hourChartView.setDragEnabled(false);
			hourChartView.setPinchZoom(false);
			hourChartView.setScaleEnabled(false);
			hourChartView.setDoubleTapToZoomEnabled(false);
			hourChartView.setNoDataText(LanguageHelper.changeLanguageText("无数据"));
			hourChartView.setExtraBottomOffset(30);

			XAxis xAxis = hourChartView.getXAxis();
			xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
			xAxis.setTextSize(15f);
			xAxis.setTextColor(Color.BLACK);
			xAxis.setDrawGridLines(false);
			xAxis.setDrawAxisLine(true);
			xAxis.setValueFormatter(new HourXAxisValueFormatter());
			if (activities1.size() > 1) {
				float space = 0.17f;
				xAxis.setAxisMaximum(activities1.size() - 1 + space); //后面留空区域
				xAxis.setLabelCount(activities1.size() - 1);
			} else
				xAxis.setLabelCount(0);

			YAxis leftAxis = hourChartView.getAxisLeft();
			leftAxis.setTextSize(15f);
			leftAxis.setTextColor(Color.BLACK);
			leftAxis.setAxisMinimum(0f);
			leftAxis.resetAxisMaximum();
			leftAxis.setDrawZeroLine(false);
			leftAxis.setDrawLimitLinesBehindData(true);
			leftAxis.setGridDashedLine(new DashPathEffect(new float[]{5f, 5f}, 0));

			MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
			mv.setChartView(hourChartView);
			hourChartView.setMarker(mv);
	    	
	    	Time time = new Time();
	        time.setToNow();      
	        int year = time.year;      
	        int month = time.month+1;
	        int day = time.monthDay;

			String tig1 = month+"月";
			String tig2 = day+(APP.language.equals("zh")?"日":"");
			if(!APP.language.equals("zh")) tig1 = LanguageHelper.changeEnMonth(tig1);

	        hour_yearView.setText(year+"");
	        hour_monthView.setText(tig1);
	        hour_dayView.setText(tig2);
	    }
	    
	    {
	    	day_yearView = (TextView)findViewById(R.id.day_selectyear);
	    	day_monthView = (TextView)findViewById(R.id.day_selectmonth);

			dayChartView = (LineChart) findViewById(R.id.chart2);
	    	day_loadBgView = (RelativeLayout)findViewById(R.id.day_loadingbg);

			dayChartView.getLegend().setEnabled(false);
			dayChartView.getDescription().setEnabled(false);
			dayChartView.getAxisRight().setEnabled(false);
			dayChartView.setDrawGridBackground(false);
			dayChartView.setDragEnabled(false);
			dayChartView.setPinchZoom(false);
			dayChartView.setScaleEnabled(false);
			dayChartView.setDoubleTapToZoomEnabled(false);
			dayChartView.setNoDataText(LanguageHelper.changeLanguageText("无数据"));
			dayChartView.setExtraBottomOffset(30);

			XAxis xAxis = dayChartView.getXAxis();
			xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
			xAxis.setTextSize(15f);
			xAxis.setTextColor(Color.BLACK);
			xAxis.setDrawGridLines(false);
			xAxis.setDrawAxisLine(true);
			xAxis.setValueFormatter(new DayXAxisValueFormatter());
			if (activities2.size() > 1) {
				float space = 0.17f;
				xAxis.setAxisMaximum(activities2.size() - 1 + space); //后面留空区域
				xAxis.setLabelCount(activities2.size() - 1);
			} else
				xAxis.setLabelCount(0);

			YAxis leftAxis = dayChartView.getAxisLeft();
			leftAxis.setTextSize(15f);
			leftAxis.setTextColor(Color.BLACK);
			leftAxis.setAxisMinimum(0f);
			leftAxis.resetAxisMaximum();
			leftAxis.setDrawZeroLine(false);
			leftAxis.setDrawLimitLinesBehindData(true);
			leftAxis.setGridDashedLine(new DashPathEffect(new float[]{5f, 5f}, 0));

			MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
			mv.setChartView(dayChartView);
			dayChartView.setMarker(mv);
	    	
	    	Time time = new Time();
	        time.setToNow();      
	        int year = time.year;      
	        int month = time.month+1;

			String tig = month+"月";
			if(!APP.language.equals("zh")) tig = LanguageHelper.changeEnMonth(tig);

	        day_yearView.setText(year+"");
	        day_monthView.setText(tig);
	    }
	    
	    getChartData(1);
	    getChartData(2);
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
	}
	
	@Override
	public void onPopupClick(TextView textView) {
		if(textView == day_yearView || textView == day_monthView) { //获取每天电量
			getChartData(2);
		}else { //获取每小时电量
			getChartData(1);
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityPower.class));
		this.finish();
	}
	
	//方法类型：自定义方法
  	//编   写：
  	//方法功能：选择年份事件
	public void selectYearAction(View v) {
		String tag = (String) v.getTag();
		TextView textView = null;
		if(tag.equals("hour")) { //每小时电量
			textView = hour_yearView;
		}else { //每天电量
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
		if(tag.equals("hour")) { //每小时电量
			textView = hour_monthView;
		}else { //每天电量
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
		String month = hour_monthView.getText().toString();

		month = LanguageHelper.changeZhMonth(month);
		month = month.replace("月", "");

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

	private void setHourChartData() {
		LineDataSet dataSet = null;
		if (hourChartView.getData() != null && hourChartView.getData().getDataSetCount() > 0) {

			dataSet = (LineDataSet) hourChartView.getData().getDataSetByIndex(0);
			dataSet.setValues(entries1);
			dataSet.setDrawFilled((maxHourNumber>0)?true:false);

			hourChartView.getData().notifyDataChanged();
			hourChartView.notifyDataSetChanged();
		} else {
			dataSet = new LineDataSet(entries1, "小时电量");
			dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			dataSet.setHighLightColor(Color.rgb(244, 117, 117));
			dataSet.setLineWidth(2f);
			dataSet.setCircleRadius(3f);
			dataSet.setDrawCircleHole(false);
			dataSet.setDrawFilled((maxHourNumber>0)?true:false);

			//设置曲线展示为圆滑曲线（如果不设置则默认折线）
			//dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

			int color = Color.rgb(234, 147, 150);
			dataSet.setColor(color);
			dataSet.setCircleColor(color);

			if (Utils.getSDKInt() >= 18) {
				Drawable drawable = getResources().getDrawable(R.drawable.color_shape);
				dataSet.setFillDrawable(drawable);
			} else {
				dataSet.setFillAlpha(70);
				dataSet.setFillColor(color);
			}

			LineData data = new LineData(dataSet);
			data.setValueTextColor(Color.BLACK);
			data.setValueTextSize(15f);
			data.setDrawValues(false);

			hourChartView.setData(data);
		}

		hourChartView.highlightValue(null);
		hourChartView.animateX(1500);
	}

	private void setDayChartData() {
		LineDataSet dataSet = null;
		if (dayChartView.getData() != null && dayChartView.getData().getDataSetCount() > 0) {

			dataSet = (LineDataSet) dayChartView.getData().getDataSetByIndex(0);
			dataSet.setValues(entries2);
			dataSet.setDrawFilled((maxDayNumber>0)?true:false);

			dayChartView.getData().notifyDataChanged();
			dayChartView.notifyDataSetChanged();
		} else {
			dataSet = new LineDataSet(entries2, "天电量");
			dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			dataSet.setHighLightColor(Color.rgb(244, 117, 117));
			dataSet.setLineWidth(2f);
			dataSet.setCircleRadius(3f);
			dataSet.setDrawCircleHole(false);
			dataSet.setDrawFilled((maxDayNumber>0)?true:false);

			//设置曲线展示为圆滑曲线（如果不设置则默认折线）
			//dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

			int color = Color.rgb(234, 147, 150);
			dataSet.setColor(color);
			dataSet.setCircleColor(color);

			if (Utils.getSDKInt() >= 18) {
				Drawable drawable = getResources().getDrawable(R.drawable.color_shape);
				dataSet.setFillDrawable(drawable);
			} else {
				dataSet.setFillAlpha(70);
				dataSet.setFillColor(color);
			}

			LineData data = new LineData(dataSet);
			data.setValueTextColor(Color.BLACK);
			data.setValueTextSize(15f);
			data.setDrawValues(false);

			dayChartView.setData(data);
		}

		dayChartView.highlightValue(null);
		dayChartView.animateX(1500);
	}
	
	private void getChartData(int type) {
		if(type == 1) { //获取每小时电量
			hour_loadBgView.setVisibility(View.VISIBLE);
			
			String year = hour_yearView.getText().toString();
			String month = hour_monthView.getText().toString();
			String day = hour_dayView.getText().toString();

			month = LanguageHelper.changeZhMonth(month);
			month = month.replace("月", "");
			day = day.replace("日", "");

			if(month != null && month.length() == 1) {
				month = "0"+month;
			}
			if(day != null && day.length() == 1) {
				day = "0"+day;
			}
			
			String time = year+month+day;
			List<Power> power = DBpower.GetPowerHoursList(channel, time);
			
			analyticalPowerHour(power);
			power.clear();
		}else { //获取每天电量
			day_loadBgView.setVisibility(View.VISIBLE);
			
			String year = day_yearView.getText().toString();
			String month = day_monthView.getText().toString();

			month = LanguageHelper.changeZhMonth(month);
			month = month.replace("月", "");

			if(month != null && month.length() == 1) {
				month = "0"+month;
			}
			
			String time = year+month;
			List<Power> power = DBpower.GetPowerDayList(channel, time);
			
			analyticalPowerDay(power);
			power.clear();
		}
	}
	
	private void analyticalPowerHour(List<Power> power) {
		activities1.clear();
		entries1.clear();

		String[] XLabels = new String[]{"0:00","1:00","2:00","3:00","4:00","5:00","6:00","7:00"
				,"8:00","9:00","10:00","11:00","12:00","13:00","14:00","15:00"
				,"16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"}; //X轴刻度
		for (int i=0; i<XLabels.length; i++) {
			activities1.add(XLabels[i]);
		}

		maxHourNumber = 0;
		HashMap<Integer, Float> datas = new HashMap<Integer, Float>();

		if(power != null && power.size()>0) {
			for(int i = 0; i < power.size() ; i++){
				Power data = power.get(i);
				int hour = data.getHours();
				float number = data.getElectricity();
				datas.put(hour, number);
			}
		}

		for (int i=0; i<activities1.size(); i++) {
			float number = -100;

			if (datas.containsKey(i)) {
				number = datas.get(i);

				if (maxHourNumber < number) {
					maxHourNumber = number;
				}
			}else {
				if(datas.containsKey(i-1)) {
					number = -(datas.get(i-1)*1000);
				}else if(datas.containsKey(i+1)) {
					number = -(datas.get(i+1)*1000);
				}
			}

			String year = hour_yearView.getText().toString();
			String month = hour_monthView.getText().toString();
			String day = hour_dayView.getText().toString();

			month = LanguageHelper.changeZhMonth(month);

			String tig = "";
			if (APP.language.equals("zh")) {
				tig = year + "年" + month + day + " " + activities1.get(i);
			}else {
				month = month.replace("月", "");
				day = day.replace("日", "");
				tig = year + "-" + month + "-" + day + " " + activities1.get(i);
			}

			Entry entry = new Entry(i, number);
			entry.setData(tig);
			entries1.add(entry);
		}

		if (activities1.size() > 1) {
			float space = 0.17f;
			hourChartView.getXAxis().setAxisMaximum(activities1.size() - 1 + space); //后面留空区域
			hourChartView.getXAxis().setLabelCount(activities1.size() - 1);
		} else
			hourChartView.getXAxis().setLabelCount(0);

		if (maxHourNumber < 6) {
			hourChartView.getAxisLeft().setAxisMaximum(6f);
		} else
			hourChartView.getAxisLeft().resetAxisMaximum();

		hour_loadBgView.setVisibility(View.GONE);
		setHourChartData();
	}
	
	private void analyticalPowerDay(List<Power> power) {
		activities2.clear();
		entries2.clear();

		int maxDay = 31;
		String year = day_yearView.getText().toString();
		String month = day_monthView.getText().toString();

		month = LanguageHelper.changeZhMonth(month);
		month = month.replace("月", "");

		if(year.length()>0 && month.length()>0) {
			try {
				int y = Integer.parseInt(year);
				int m = Integer.parseInt(month);
				maxDay = Tooles.getMaxDayByYearMonth(y, m);
			} catch(Exception e) {}
		}
		for (int i=0; i<maxDay; i++) {
			String day = (i+1)+"";
			activities2.add(day);
		}

		maxDayNumber = 0;
		HashMap<Integer, Float> datas = new HashMap<Integer, Float>();

		if(power != null && power.size()>0) {
			for(int i = 0; i < power.size() ; i++){
				Power data = power.get(i);
				int day = data.getDay()-1;
				float number = data.getElectricity();
				datas.put(day, number);
			}
		}

		for (int i=0; i<activities2.size(); i++) {
			float number = -100;

			if (datas.containsKey(i)) {
				number = datas.get(i);

				if (maxDayNumber < number) {
					maxDayNumber = number;
				}
			}else {
				if(datas.containsKey(i-1)) {
					number = -(datas.get(i-1)*1000);
				}else if(datas.containsKey(i+1)) {
					number = -(datas.get(i+1)*1000);
				}
			}

			month = day_monthView.getText().toString();
			month = LanguageHelper.changeZhMonth(month);

			String tig = "";
			if (APP.language.equals("zh")) {
				tig = year + "年" + month + activities2.get(i) + "日";
			}else {
				month = month.replace("月", "");
				tig = year + "-" + month + "-" + activities2.get(i);
			}

			Entry entry = new Entry(i, number);
			entry.setData(tig);
			entries2.add(entry);
		}

		if (activities2.size() > 1) {
			float space = 0.17f;
			dayChartView.getXAxis().setAxisMaximum(activities2.size() - 1 + space); //后面留空区域
			dayChartView.getXAxis().setLabelCount(activities2.size() - 1);
		} else
			dayChartView.getXAxis().setLabelCount(0);

		if (maxDayNumber < 6) {
			dayChartView.getAxisLeft().setAxisMaximum(6f);
		} else
			dayChartView.getAxisLeft().resetAxisMaximum();

		day_loadBgView.setVisibility(View.GONE);
		setDayChartData();
	}

	private class HourXAxisValueFormatter implements IAxisValueFormatter {
		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			// TODO Auto-generated method stub
			if (value < 0) return "";

			if (activities1.size() > 0) {
				String name = activities1.get((int) value % activities1.size());
				return name;
			}
			return "";
		}
	}

	private class DayXAxisValueFormatter implements IAxisValueFormatter {
		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			// TODO Auto-generated method stub
			if (value < 0) return "";

			if (activities2.size() > 0) {
				String name = activities2.get((int) value % activities2.size());
				return name;
			}
			return "";
		}
	}
	
}
