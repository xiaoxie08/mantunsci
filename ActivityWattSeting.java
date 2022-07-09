package snd.ui;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import snd.adapter.AdapterWattSeting;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.database.DBconfig;
import snd.database.DBswitchsetting;
import snd.serialservice.SerialThread;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.MaxPowerView;
import snd.view.MaxVoltageView;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWattSeting extends ActivityBase
{
	private static final String TAG=ActivityWattSeting.class.getName();

	private Resources resources;
	
	private GridView grid;
	private TextView nameView;
	private TextView maxPowerView;
	private TextView maxCurrentView;
	private TextView maxVoltageView;
	private TextView maxLeakageView;
	private TextView maxTemperatureView;
	private TextView setChannelView;

	private ImageButton check1View;
	private ImageButton check2View;
	private ImageButton check3View;
	private ImageButton check4View;

	private RelativeLayout nameBgView;
	private RelativeLayout powerBgView;
	private RelativeLayout currentBgView;
	private RelativeLayout voltageBgView;
	private RelativeLayout leakageBgView;
	private RelativeLayout temperatureView;
	private RelativeLayout channelSettingBgView;
	private RelativeLayout visibilityBgView;
	private RelativeLayout controlBgView;
	private RelativeLayout autoCloseBgView;
	private RelativeLayout remoteLockBgView;
	private RelativeLayout alarmEnableBgView;
	private RelativeLayout tripEnableBgView;

	private View voltageLineView;
	private View leakageLineView;
	private View autoCloseLineView;
	private View remoteLockLineView;
	private View alarmEnableLineView;
	private View tripEnableLineView;
	
	private LinearLayout bottomView;

	private int setingCount;
	
	private List<Breaker> lineDatas = new ArrayList<Breaker>();
	
	AdapterWattSeting adapter;
	
	BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		int curchannel = intent.getIntExtra("curchannel", 1);
		
        setContentView(R.layout.wattseting);
        
        grid = (GridView)findViewById(R.id.list);
		nameView = (TextView)findViewById(R.id.name);
        maxPowerView = (TextView)findViewById(R.id.maxpower);
		maxCurrentView = (TextView)findViewById(R.id.maxcurrent);
		maxVoltageView = (TextView)findViewById(R.id.maxvoltage);
		maxLeakageView = (TextView)findViewById(R.id.maxleakage);
		maxTemperatureView = (TextView)findViewById(R.id.maxtemperature);
		setChannelView = (TextView)findViewById(R.id.setchannel);
		check1View = (ImageButton)findViewById(R.id.visibility);
		check2View = (ImageButton)findViewById(R.id.control);
		check3View = (ImageButton)findViewById(R.id.autoClose);
		check4View = (ImageButton)findViewById(R.id.remoteLock);

		nameBgView = (RelativeLayout)findViewById(R.id.nameBg);
		powerBgView = (RelativeLayout)findViewById(R.id.powerBg);
		currentBgView = (RelativeLayout)findViewById(R.id.currentBg);
		voltageBgView = (RelativeLayout)findViewById(R.id.voltageBg);
		leakageBgView = (RelativeLayout)findViewById(R.id.leakageBg);
		temperatureView = (RelativeLayout)findViewById(R.id.temperatureBg);
		channelSettingBgView = (RelativeLayout)findViewById(R.id.channelSettingBg);
		visibilityBgView = (RelativeLayout)findViewById(R.id.visibilityBg);
		controlBgView = (RelativeLayout)findViewById(R.id.controlBg);
		autoCloseBgView = (RelativeLayout)findViewById(R.id.autoCloseBg);
		remoteLockBgView = (RelativeLayout)findViewById(R.id.remoteLockBg);
		alarmEnableBgView = (RelativeLayout)findViewById(R.id.alarmEnableBg);
		tripEnableBgView = (RelativeLayout)findViewById(R.id.tripEnableBg);

		voltageLineView = (View)findViewById(R.id.voltageLine);
		leakageLineView = (View)findViewById(R.id.leakageLine);
		autoCloseLineView = (View)findViewById(R.id.autoCloseLine);
		remoteLockLineView = (View)findViewById(R.id.remoteLockLine);
		alarmEnableLineView = (View)findViewById(R.id.alarmEnableLine);
		tripEnableLineView = (View)findViewById(R.id.tripEnableLine);
        
        bottomView = (LinearLayout)findViewById(R.id.limit_bottom);
        
        adapter = new AdapterWattSeting(this);
		adapter.curchannel = curchannel;
        grid.setAdapter(adapter);
       
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

		updateLayout(7);
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
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：设置按钮事件
	public void settingAction(View v) {
		this.startActivity(new Intent(this, ActivitySwitchSetting.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) {
		Breaker breaker = (Breaker)v.getTag();
		adapter.curchannel = breaker.addr;
		refrenshAdapter();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：重新命名按钮事件
	public void nameAction(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		String name = nameView.getText().toString();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(resources.getString(R.string.alert16));
		final EditText et = new EditText(this);
		et.setTextSize(25);
		et.setHint(resources.getString(R.string.hint1));
		et.setSingleLine(true);
		if(name != null && name.length()>0){
			et.setText(name);

			Editable etext = et.getText();
			et.setSelection(etext.length());
		}
		builder.setView(et);
		builder.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
			}

		});
		builder.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {

			@Override
            public void onClick(DialogInterface dialog, int which) {
				String name = et.getText().toString();
				nameView.setText(name);

				b.title = name;
				DBconfig.UpdateConfig("DBOX", "SWITCH"+b.addr, name);

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
			}

        });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：功率限额设定按钮事件
	public void maxPowerAction(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		final int maxPower = (int)(b.specification*220*(b.lineType.equals("380")?3:1)*1.5);
		String maxStr = maxPower+"";
		int row = maxStr.length();
		String power = (long)b.MXGG+"";
		String number = maxPowerView.getText().toString();
		if(number != null && number.length()>0) {
			power = number.replace(resources.getString(R.string.realtime_hint2), "");
		}
		if (row == 0) {
			row = 5;
		}

		final MaxPowerView view = new MaxPowerView(this, row, power);
		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert17))
				.setView(view)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String power = view.getMaxPower();
						if(power != null && power.length() == 0) {
							power = "0";
						}
						if(power != null && (!(Integer.valueOf(power)>maxPower) || maxPower==0)) {
							int maxPower = Integer.parseInt(power);
							if(maxPower > 0) {
								for (int i=0; i<3; i++) {
									SerialThread.CmdQueue(SerialThread.CFG_POWER, b.addr, maxPower);
									SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, b.addr, 0);
								}
							}else
								Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast14), Toast.LENGTH_LONG).show();
						}else {
							String message = "超过"+b.title+"的最大功率额定值"+maxPower+"瓦了";
							message = LanguageHelper.changeLanguageLimitValue(message, maxPower);
							Toast.makeText(ActivityWattSeting.this, message, Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：电流限额设定按钮事件
	public void maxCurrentAction(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		int num = (int) Math.round(b.specification*1.5);
		final int maxCurrent = (num < 1?135:num);
		String maxStr = maxCurrent+"";
		int row = maxStr.length();
		String current = (long)b.MXGL+"";
		String number = maxCurrentView.getText().toString();
		if(number != null && number.length()>0) {
			current = number.replace(resources.getString(R.string.realtime_hint5), "");
		}
		if (row == 0) {
			row = 3;
		}

		final MaxPowerView view = new MaxPowerView(this, row, current);
		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert18))
				.setView(view)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String current = view.getMaxPower();
						if(current != null && current.length() == 0) {
							current = "0";
						}
						if(current != null && (!(Integer.valueOf(current)>maxCurrent) || maxCurrent==0)) {
							int maxCurrent = Integer.parseInt(current)*100;
							if(maxCurrent > 0) {
								for (int i=0; i<3; i++) {
									SerialThread.CmdQueue(SerialThread.CFG_CURRENT, b.addr, maxCurrent);
									SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, b.addr, 0);
								}
							}else
								Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast15), Toast.LENGTH_LONG).show();
						}else {
							String message = "超过"+b.title+"的最大电流额定值"+maxCurrent+"安了";
							message = LanguageHelper.changeLanguageLimitValue(message, maxCurrent);
							Toast.makeText(ActivityWattSeting.this, message, Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：电压限额设定按钮事件
	public void maxVoltageAction(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		int row1 = 3;
		int row2 = 3;
		String lower = (long)b.MXQY+"";
		String upper = (long)b.MXGY+"";
		String number = maxVoltageView.getText().toString();
		if(number != null && number.length()>0) {
			number = number.replace(resources.getString(R.string.realtime_hint6), "");
			String numbers[] = number.split("-");
			if(numbers.length>0) lower = numbers[0];
			if(numbers.length>1) upper = numbers[1];
		}
		if (lower.length() > 3) {
			row1 = lower.length();
		}
		if (upper.length() > 3) {
			row2 = upper.length();
		}

		final MaxVoltageView view = new MaxVoltageView(this, row1, lower, row2, upper);
		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert19))
				.setView(view)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String number1 = view.getLower();
						String number2 = view.getUpper();

						if(number1 != null && number1.length() == 0) {
							number1 = "0";
						}

						if(number2 != null && number2.length() == 0) {
							number2 = "0";
						}

						if(number1 != null && number1.length()>0 && number2 != null && number2.length()>0) {
							int maxLower = Integer.parseInt(number1);
							int maxpper = Integer.parseInt(number2);
							if(maxLower > 0 && maxpper > 0) {
								if (maxLower > maxpper) {
									Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast17), Toast.LENGTH_LONG).show();
								}else {
									for (int i=0; i<3; i++) {
										SerialThread.CmdQueue(SerialThread.CFG_VOLTAGE_LOWER, b.addr, maxLower);
										SerialThread.CmdQueue(SerialThread.CFG_VOLTAGE_UPPER, b.addr, maxpper);
										SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, b.addr, 0);
									}
								}
							}else
								Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast16), Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：漏电电流限额设定按钮事件
	public void maxLeakageAction(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		int row = 3;
		String leakage = (long)b.MXLD+"";
		String number = maxLeakageView.getText().toString();
		if(number != null && number.length()>0) {
			leakage = number.replace(resources.getString(R.string.realtime_hint3), "");
		}
		if (leakage.length() > 3) {
			row = leakage.length();
		}

		final MaxPowerView view = new MaxPowerView(this, row, leakage);
		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert20))
				.setView(view)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String number = view.getMaxPower();
						if(number != null && number.length() == 0) {
							number = "0";
						}

						if(number != null && number.length()>0) {
							int maxLeakage = Integer.parseInt(number);
							if(maxLeakage > 0) {
								for (int i=0; i<3; i++) {
									SerialThread.CmdQueue(SerialThread.CFG_LEAKAGE, b.addr, maxLeakage);
									SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, b.addr, 0);
								}
							}else
								Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast18), Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：温度限额设定按钮事件
	public void maxTemperature(View v) {
		final Breaker b = APP.distributbox.Breakers.get(adapter.curchannel);
		if(b == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		int row = 3;
		String temperature = (long)b.MXGW+"";
		String number = maxTemperatureView.getText().toString();
		if(number != null && number.length()>0) {
			temperature = number.replace(resources.getString(R.string.realtime_hint4), "");
		}
		if (temperature.length() > 3) {
			row = temperature.length();
		}

		final MaxPowerView view = new MaxPowerView(this, row, temperature);
		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.alert21))
				.setView(view)
				.setPositiveButton(resources.getString(R.string.tig10), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String number = view.getMaxPower();
						if(number != null && number.length() == 0) {
							number = "0";
						}

						if(number != null && number.length()>0) {
							int maxTemperature = Integer.parseInt(number);
							if(maxTemperature > 0) {
								for (int i=0; i<3; i++) {
									SerialThread.CmdQueue(SerialThread.CFG_TEMPERATURE, b.addr, maxTemperature);
									SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, b.addr, 0);
								}
							}else
								Toast.makeText(ActivityWattSeting.this, resources.getString(R.string.toast19), Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				})
				.setNegativeButton(resources.getString(R.string.tig4), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：接线设置事件
	public void channelSettingAction(View v) {
		Breaker data = APP.distributbox.Breakers.get(adapter.curchannel);
		if(data == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}
		
		showSelectTotalChannel(data);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：是否遥控选择按钮事件
	public void remoteAction(View v) {
		final Breaker data = APP.distributbox.Breakers.get(adapter.curchannel);
		if(data == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		final int control = v.isSelected()?0:1;
		String info = resources.getString(R.string.alert22);
		if(control == 1) {
			info = resources.getString(R.string.alert23);
		}
		AlertDialog alert = new MyAlertDialog(this)
				.setMessage(info)
				.setTitle(resources.getString(R.string.tig7))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();

								DBswitchsetting.UpdateControl(data.addr, control);
								data.control = control;
								check2View.setSelected(control == 1?true:false);
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

		final int message = this.getResources().getIdentifier("message","id","android") ;
		TextView messageTextView = (TextView) alert.findViewById(message);
		messageTextView.setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：是否显示选择按钮事件
	public void displayAction(View v) {
		final Breaker data = APP.distributbox.Breakers.get(adapter.curchannel);
		if(data == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		final int visibility = v.isSelected()?0:1;
		String info = resources.getString(R.string.alert24);
		if(visibility == 1) {
			info = resources.getString(R.string.alert25);
		}
		AlertDialog alert = new MyAlertDialog(this)
				.setMessage(info)
				.setTitle(resources.getString(R.string.tig7))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();

								DBswitchsetting.UpdateVisibility(data.addr, visibility);
								data.visibility = visibility;
								check1View.setSelected(visibility == 1?true:false);
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

		final int message = this.getResources().getIdentifier("message","id","android") ;
		TextView messageTextView = (TextView) alert.findViewById(message);
		messageTextView.setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：断电恢复是否自动分闸保护选择按钮事件
	public void autoCloseAction(View v) {
		final Breaker data = APP.distributbox.Breakers.get(adapter.curchannel);
		if(data == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		final int autoClose = v.isSelected()?0:1;
		String info = resources.getString(R.string.alert26);
		if(autoClose == 1) {
			info = resources.getString(R.string.alert27);
		}
		AlertDialog alert = new MyAlertDialog(this)
				.setMessage(info)
				.setTitle(resources.getString(R.string.tig7))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();

								if (data.EnableAlarm >= 0) {
									String EnableAlarm = data.EnableAlarm+"";
									BigInteger number = new BigInteger(EnableAlarm);
									String binary = Tooles.getBinaryByDecimal(number, 32);
									int EnableAlarm0 = 0;
									if (binary.length() > 15) {
										String num = binary.substring(0, 16);
										EnableAlarm0 = Tooles.binaryToDecimal(num);
									}

									boolean mark = false;
									if ((EnableAlarm0&0x01) == 0x01 && autoClose == 0) {
										EnableAlarm0 = EnableAlarm0-1;
										mark = true;
									}else if ((EnableAlarm0&0x01) == 0 && autoClose == 1) {
										EnableAlarm0 = EnableAlarm0+autoClose;
										mark = true;
									}

									if (mark) {
										SerialThread.CmdQueue(SerialThread.CTR_UNLOCK, data.addr, 0);
										for(int i=0; i<3; i++) {
											SerialThread.CmdQueue(SerialThread.CFG_ENABLEAlARM0, data.addr, EnableAlarm0);
											SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, data.addr, 0);
										}
										SerialThread.CmdQueue(SerialThread.CTR_LOCK, data.addr, 0);
									}
								}
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

		final int message = this.getResources().getIdentifier("message","id","android") ;
		TextView messageTextView = (TextView) alert.findViewById(message);
		messageTextView.setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：是否分闸锁定选择按钮事件
	public void remoteLockAction(View v) {
		final Breaker data = APP.distributbox.Breakers.get(adapter.curchannel);
		if(data == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return;
		}

		final boolean remoteLock = v.isSelected()?false:true;
		String info = resources.getString(R.string.alert28);
		if(remoteLock) {
			info = resources.getString(R.string.alert29);
		}
		AlertDialog alert = new MyAlertDialog(this)
				.setMessage(info)
				.setTitle(resources.getString(R.string.tig7))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();

								if (data.EnableAlarm >= 0) {
									String EnableAlarm = data.EnableAlarm+"";
									BigInteger number = new BigInteger(EnableAlarm);
									String binary = Tooles.getBinaryByDecimal(number, 32);
									int EnableAlarm0 = 0;
									if (binary.length() > 15) {
										String num = binary.substring(0, 16);
										EnableAlarm0 = Tooles.binaryToDecimal(num);
									}

									if ((EnableAlarm0&0x08) == 0) {
										EnableAlarm0 = EnableAlarm0+8;

										SerialThread.CmdQueue(SerialThread.CTR_UNLOCK, data.addr, 0);
										SerialThread.CmdQueue(SerialThread.CFG_ENABLEAlARM0, data.addr, EnableAlarm0);
										SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, data.addr, 0);
										SerialThread.CmdQueue(SerialThread.CTR_LOCK, data.addr, 0);
									}
								}

								//下发远程锁定模式（欠费模式）命令
								for(int i=0; i<3; i++) {
									if (remoteLock) { //远程禁止合闸命令
										SerialThread.CmdQueue(SerialThread.CTR_REMOTELOCK_RELAY, data.addr, 0);
									}else //解除远程禁止合闸命令
										SerialThread.CmdQueue(SerialThread.CTR_UNREMOTELOCK_RELAY, data.addr, 0);
								}
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

		final int message = this.getResources().getIdentifier("message","id","android") ;
		TextView messageTextView = (TextView) alert.findViewById(message);
		messageTextView.setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：报警使能配置按钮事件
	public void alarmEnableAction(View v) {
		Intent intent = new Intent(this, ActivityEnable.class);
		intent.putExtra("curchannel", adapter.curchannel);
		intent.putExtra("type", 1);
		this.startActivity(intent);
		this.finish();
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：脱扣使能配置按钮事件
	public void tripEnableAction(View v) {
		Intent intent = new Intent(this, ActivityEnable.class);
		intent.putExtra("curchannel", adapter.curchannel);
		intent.putExtra("type", 2);
		this.startActivity(intent);
		this.finish();
	}

	private void refrenshAdapter() {
		if(adapter != null) adapter.notifyDataSetChanged();
		updateBottomView();
	}
	
	private void updateBottomView() {
		Breaker channelData = APP.distributbox.Breakers.get(adapter.curchannel);
		if(channelData == null) return;
		
		boolean oc = channelData.OpenClose;
		if(oc) { //打开状态
			bottomView.setBackgroundColor(0xffF77B55);
		}else { //关闭状态
			bottomView.setBackgroundColor(0xff7ac058);
		}

		nameView.setText(LanguageHelper.changeLanguageNode(channelData.title));
		maxPowerView.setText((long)channelData.MXGG+resources.getString(R.string.realtime_hint2));
		maxCurrentView.setText((long)channelData.MXGL+resources.getString(R.string.realtime_hint5));
		maxVoltageView.setText((long)channelData.MXQY+"-"+(long)channelData.MXGY+resources.getString(R.string.realtime_hint6));
		maxLeakageView.setText((long)channelData.MXLD+resources.getString(R.string.realtime_hint3));
		maxTemperatureView.setText((long)channelData.MXGW+resources.getString(R.string.realtime_hint4));

		String totalChannelId = channelData.totalChannelId;
		if (totalChannelId != null && Tooles.isNumber(totalChannelId)
				&& APP.distributbox.Breakers.containsKey(Integer.parseInt(totalChannelId))) {
			Breaker data = APP.distributbox.Breakers.get(Integer.parseInt(totalChannelId));
			String name = data.title;
			name = LanguageHelper.changeLanguageNode(name);
			setChannelView.setText(name);
		}else
			setChannelView.setText(LanguageHelper.changeLanguageText("进线直连"));

		int control = 1;
		int visibility = 1;
		DBswitchsetting setting = DBswitchsetting.getSwitchSetting(channelData.addr);
		if(setting != null) {
			control = setting.getControl();
			visibility = setting.getVisibility();
		}
		boolean isControl = (control == 1)?true:false;
		boolean isVisibility = (visibility == 1)?true:false;
		check1View.setSelected(isVisibility);
		check2View.setSelected(isControl);

		boolean isAutoClose = (channelData.autoClose == 1)?true:false;
		check3View.setSelected(isAutoClose);
		check4View.setSelected(channelData.remoteLock);

		int count = 7;
		if (channelData.model.equals(Breaker.BreakerJZK2L100_BL6523)) {
			voltageBgView.setVisibility(View.VISIBLE);
			voltageLineView.setVisibility(View.VISIBLE);
			leakageBgView.setVisibility(View.VISIBLE);
			leakageLineView.setVisibility(View.VISIBLE);
			count = 9;
		}else {
			voltageBgView.setVisibility(View.GONE);
			voltageLineView.setVisibility(View.GONE);
			leakageBgView.setVisibility(View.GONE);
			leakageLineView.setVisibility(View.GONE);
			count = 7;
		}

		if (Tooles.isLockFlag(channelData)) {
			autoCloseBgView.setVisibility(View.VISIBLE);
			autoCloseLineView.setVisibility(View.VISIBLE);
			remoteLockBgView.setVisibility(View.VISIBLE);
			remoteLockLineView.setVisibility(View.VISIBLE);
			count += 2;
		}else {
			autoCloseBgView.setVisibility(View.GONE);
			autoCloseLineView.setVisibility(View.GONE);
			remoteLockBgView.setVisibility(View.GONE);
			remoteLockLineView.setVisibility(View.GONE);
		}

		if(channelData.EnableAlarm >= 0) {
			alarmEnableBgView.setVisibility(View.VISIBLE);
			alarmEnableLineView.setVisibility(View.VISIBLE);
			count += 1;
		}else {
			alarmEnableBgView.setVisibility(View.GONE);
			alarmEnableLineView.setVisibility(View.GONE);
		}

		if(channelData.EnableTrip >= 0) {
			tripEnableBgView.setVisibility(View.VISIBLE);
			tripEnableLineView.setVisibility(View.VISIBLE);
			count += 1;
		}else {
			tripEnableBgView.setVisibility(View.GONE);
			tripEnableLineView.setVisibility(View.GONE);
		}

		updateLayout(count);
	}

	private void updateLayout(int count) {
		if (setingCount != count) {
			setingCount = count;

			if(count > 9) count = 9;
			int height = Integer.valueOf(APP.screenHeight)-(count-1)*1-90;
			int itemHeight = height/count;
			android.view.ViewGroup.LayoutParams pp = nameBgView.getLayoutParams();
			pp.height = itemHeight+(height-count*itemHeight);
			nameBgView.setLayoutParams(pp);

			pp = powerBgView.getLayoutParams();
			pp.height = itemHeight;
			powerBgView.setLayoutParams(pp);

			pp = currentBgView.getLayoutParams();
			pp.height = itemHeight;
			currentBgView.setLayoutParams(pp);

			pp = voltageBgView.getLayoutParams();
			pp.height = itemHeight;
			voltageBgView.setLayoutParams(pp);

			pp = leakageBgView.getLayoutParams();
			pp.height = itemHeight;
			leakageBgView.setLayoutParams(pp);

			pp = temperatureView.getLayoutParams();
			pp.height = itemHeight;
			temperatureView.setLayoutParams(pp);

			pp = channelSettingBgView.getLayoutParams();
			pp.height = itemHeight;
			channelSettingBgView.setLayoutParams(pp);

			pp = visibilityBgView.getLayoutParams();
			pp.height = itemHeight;
			visibilityBgView.setLayoutParams(pp);

			pp = controlBgView.getLayoutParams();
			pp.height = itemHeight;
			controlBgView.setLayoutParams(pp);

			pp = autoCloseBgView.getLayoutParams();
			pp.height = itemHeight;
			autoCloseBgView.setLayoutParams(pp);

			pp = remoteLockBgView.getLayoutParams();
			pp.height = itemHeight;
			remoteLockBgView.setLayoutParams(pp);

			pp = alarmEnableBgView.getLayoutParams();
			pp.height = itemHeight;
			alarmEnableBgView.setLayoutParams(pp);

			pp = tripEnableBgView.getLayoutParams();
			pp.height = itemHeight;
			tripEnableBgView.setLayoutParams(pp);
		}
	}
	
	private void showSelectTotalChannel(final Breaker channelData) {
		int position = -1;
        String totalChannelId = channelData.totalChannelId;
        
        List<String> list = new ArrayList<String>();
        int i = 0;
        lineDatas.clear();
		if(APP.distributbox != null) {
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
				String parentId = data.totalChannelId;
				if(parentId != null && parentId.equals("-1") && data.addr != channelData.addr) {
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

		ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
		new MyAlertDialog(ctw)
		.setTitle(resources.getString(R.string.alert11))
		.setSingleChoiceItems(array, position, new DialogInterface.OnClickListener() {
			 
		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		         showtotalChannelDialog(channelData, which);
		         
		         dialog.dismiss();
		     }
		}).show();
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
		        String name = "";
		        if (lineDatas != null && position < lineDatas.size()) {
		        	Breaker data = lineDatas.get(position);
		        	totalChannelId = data.addr+"";
					name = data.title;
					name = LanguageHelper.changeLanguageNode(name);
		        }else { //进线直连
		            totalChannelId = "-1";
					name = LanguageHelper.changeLanguageText("进线直连");
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

				setChannelView.setText(name);
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
