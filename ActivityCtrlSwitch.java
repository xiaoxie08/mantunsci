package snd.ui;

import snd.adapter.AdapterCtrlSwitch;
import snd.broadcastreceiver.Millisecond500BroadcastReceiver;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.serialservice.Serial433Thread;
import snd.serialservice.SerialThread;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.MyAlertDialog;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityCtrlSwitch extends ActivityBase implements OnClickListener
{
	private static final String TAG=ActivityCtrlSwitch.class.getName();

	private Resources resources;

	private GridView grid;
	
	private LinearLayout bottomView;
	private View lineView;
	private TextView nameView;
	private TextView apView;
	private TextView aldView;
	private TextView atView;
	private TextView aaView;
	private TextView avView;
	private Button switchButton;
	
	AdapterCtrlSwitch adapter;
	int curchannel;
	
	BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		int address = intent.getIntExtra("address", 0);
		
        setContentView(R.layout.ctrlswitch);
        
        grid = (GridView) findViewById(R.id.list);
        
        bottomView = (LinearLayout) findViewById(R.id.switchview_bottom);
        nameView = (TextView) findViewById(R.id.realtime_name);
	    lineView = (View) findViewById(R.id.line);
	    aldView = (TextView) findViewById(R.id.realtime_ald);
		apView = (TextView) findViewById(R.id.realtime_ap);
		atView = (TextView) findViewById(R.id.realtime_at);
		aaView = (TextView) findViewById(R.id.realtime_aa);
		avView = (TextView) findViewById(R.id.realtime_av);
		switchButton = (Button) findViewById(R.id.switch_button);

        adapter = new AdapterCtrlSwitch(this);
        if(address > 0 && APP.distributbox.Breakers.containsKey(address)) {
			adapter.curBreaker = APP.distributbox.Breakers.get(address);
		}
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

	@Override
	public void onClick(View v) 
	{
		/*switch(v.getId())
		{
		    case R.id.textView1:
		    {
		    	LayoutInflater layoutInflater = LayoutInflater.from(this.getBaseContext());
		    	View wirelessswitch = layoutInflater.inflate(R.layout.addwirelessswitch, null);
		    	final Spinner spinner1=(Spinner)wirelessswitch.findViewById(R.id.spinner1);
		    	final EditText name=(EditText)wirelessswitch.findViewById(R.id.name);
		    	final EditText addr=(EditText)wirelessswitch.findViewById(R.id.addr);
		        
		    	AlertDialog alert=new MyAlertDialog(ActivityCtrlSwitch.this)
				.setIcon(android.R.drawable.ic_menu_compass)
	            .setMessage(ActivityCtrlSwitch.this.getResources().getString(R.string.addwirelessswitch))
	            .setCancelable(false)
	            .setView(wirelessswitch)
	            .setPositiveButton(ActivityCtrlSwitch.this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
	            {
	                 public void onClick(DialogInterface dialog, int id) 
	                 {
	                	 if(spinner1.getSelectedItem()!=null &&
	                		addr.getText().toString()!="" && 
	                		name.getText().toString()!="")
	                	 {
	                		 int x=Integer.parseInt(addr.getText().toString());
	                		 DBconfig.UpdateConfig(spinner1.getSelectedItem().toString(), "SWITCH"+x, name.getText().toString());
	                		 if(!APP.distributbox.Breakers.containsKey(addr))
	     		    		 {
	     		    			Breaker breaker=new Breaker(x,name.getText().toString());
	     		    			breaker.model=spinner1.getSelectedItem().toString().trim();
	     		    			APP.distributbox.Breakers.put(x, breaker);
	     		    		 }
	                		 dialog.cancel();
	                	 }
	                	 else
	                	 {
	                		 Toast.makeText(ActivityCtrlSwitch.this, "请完成输入", Toast.LENGTH_SHORT).show();
	                	 }
	                 }
	            })
	            .setNeutralButton(ActivityCtrlSwitch.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
	            {
	                 public void onClick(DialogInterface dialog, int id) 
	                 {
	                	 dialog.cancel();
	                 }
	            })
	            .show();
		    	
		    	final int message = this.getResources().getIdentifier("message","id","android") ;        
		    	TextView messageTextView = (TextView) alert.findViewById(message);        
		    	messageTextView.setTextSize(32);        
	            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(32);
	            alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(32);
	            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(32);
		    }
		    break;
			
		    case R.id.imageview3:
		    {
		    	AlertDialog alert=new MyAlertDialog(ActivityCtrlSwitch.this)
				.setIcon(android.R.drawable.ic_menu_compass)
	            .setMessage("您要全关还是要全开呢?")
	            .setCancelable(false)
	            .setPositiveButton(this.getResources().getString(R.string.openall), new DialogInterface.OnClickListener() 
	            {
	                 public void onClick(DialogInterface dialog, int id) 
	                 {
	                	 for(Breaker b:APP.distributbox.Breakers.values())
	                	 {
	                		 if(b.model.equals(Breaker.Breaker433ToInfrared))
	                		 {
	                			 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
	                		 }
	                		 else
	                		 {
	                			 SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, b.addr, 0);
		    					 SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, b.addr, 0);
		    					 SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, b.addr, 0);
	                		 }
	                	 }
	                	 dialog.cancel();
	                 }
	            })
	            .setNeutralButton(this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
	            {
	                 public void onClick(DialogInterface dialog, int id) 
	                 {
	                     dialog.cancel();
	                 }
	            })
	            .setNegativeButton(this.getResources().getString(R.string.closeall), new DialogInterface.OnClickListener()
	            {
	                 public void onClick(DialogInterface dialog, int id) 
	                 {
	                	 for(Breaker b:APP.distributbox.Breakers.values())
	                	 {
	                		 if(b.model.equals(Breaker.Breaker433ToInfrared))
	                		 {
	                			 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
			                	 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
	                		 }
	                		 else
	                		 {
	                			 SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, b.addr, 0);
		    					 SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, b.addr, 0);
		    					 SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, b.addr, 0);
	                		 }
	                	 }
	                     dialog.cancel();
	                 }
	            })
		    	.show();
		    	
		    	final int message = this.getResources().getIdentifier("message","id","android") ;        
		    	TextView messageTextView = (TextView) alert.findViewById(message);        
		    	messageTextView.setTextSize(32);        
	            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(32);
	            alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(32);
	            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(32);
		    }
			break;
		}*/
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
	// 方法功能：线路取名和功率限定按钮事件
	public void limitAction(View v) {
		this.startActivity(new Intent(this, ActivityWattSeting.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：线路选择按钮事件
	public void itemClickAction(View v) {
		adapter.curBreaker = (Breaker)v.getTag();
		refrenshAdapter();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：查看开关详情事件
	public void detailClickAction(View v) {
		if(adapter.curBreaker != null) {
			Intent intent = new Intent(this, ActivitySwitchDetail.class);
			intent.putExtra("address", adapter.curBreaker.addr);
			startActivity(intent);
			this.finish();
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：开关事件
	public void switchAction(View v) { 
		if(check()) {
			if(adapter.curBreaker.EnableNetCtrl) { //分合闸
				openOrclose(adapter.curBreaker);
			}else { //异常分闸解锁
				unLock(adapter.curBreaker);
			}
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：开关操作
	private void openOrclose(Breaker breaker) {
		curchannel = breaker.addr;
		String model = breaker.model;
		String name = breaker.title;
		final boolean oc = breaker.OpenClose;
		if(name == null || name.length() == 0) {
			name = "此开关";
		}
		String info = "您确定"+(!oc?"合闸":"分闸")+name+"？";
		info = LanguageHelper.changeLanguageText(info);
		
		if(model.equals(Breaker.Breaker433ToInfrared))
       	{
			Button matchcode = new Button(this);
			matchcode.setBackgroundColor(0xffeeeeee);
	        matchcode.setText(R.string.matchcode);
	        matchcode.setTextSize(27);
	        matchcode.setOnClickListener(new OnClickListener()
	        {
				@Override
				public void onClick(View v) 
				{
					Millisecond500BroadcastReceiver.channel = curchannel;
				}
	        });
	        
			AlertDialog alert = new MyAlertDialog(this)
			.setTitle(resources.getString(R.string.tig7))
            .setMessage(info)
            .setCancelable(false)
            .setView(matchcode)
            .setPositiveButton(resources.getString(R.string.tig5)
            		, new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                	 Millisecond500BroadcastReceiver.channel=-1;
                	 for(int i = 0; i<10; i++) {
            			 if(!oc) { //开
            				 Serial433Thread.CmdQueue(Serial433Thread.CTR_ON_RELAY, curchannel);
            			 }else { //关
            				 Serial433Thread.CmdQueue(Serial433Thread.CTR_OFF_RELAY, curchannel);
            			 }
                	 }
                	 dialog.cancel();
                 }
            })
            .setNegativeButton(resources.getString(R.string.tig6)
            		, new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                	 Millisecond500BroadcastReceiver.channel=-1;
                     dialog.cancel();
                 }
            })
            .show();
			
			final int message = this.getResources().getIdentifier("message","id","android") ;        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27);        
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
       	}
       	else
       	{
			AlertDialog alert = new MyAlertDialog(this)
			.setTitle(resources.getString(R.string.tig7))
			.setMessage(info)
			.setCancelable(false)
			.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
            {
                 public void onClick(DialogInterface dialog, int id) 
                 {
                	 for(int i = 0; i<3; i++) {
            			 if(!oc) { //开
            				 SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, curchannel, 0);
            			 }else { //关
            				 SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, curchannel, 0);
            			 }
                	 }
                	 dialog.cancel();
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
       		
       		final int message = this.getResources().getIdentifier("message","id","android") ;        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27);        
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
       	}
	}

	//方法类型：自定义方法
	//编   写：
	//方法功能：开关异常分闸解锁
	private void unLock(Breaker breaker) {
		curchannel = breaker.addr;
		String name = breaker.title;

		if(name == null || name.length() == 0) {
			name = "此开关";
		}
		String info = name+"可能由于现场人为手动分闸或短路、漏电保护等故障锁定，需要确认现场可以安全合闸后才能解锁。此解锁操作后远程合闸不受限制，解锁、合闸造成的风险自行承担？";
		info = LanguageHelper.changeLanguageText(info);

		AlertDialog alert = new MyAlertDialog(this)
				.setTitle(resources.getString(R.string.tig7))
				.setMessage(info)
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						for(int i = 0; i<3; i++) {
							SerialThread.CmdQueue(SerialThread.CTR_UNLOCK_RELAY, curchannel, 0);
						}
						dialog.cancel();
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

		final int message = this.getResources().getIdentifier("message","id","android") ;
		TextView messageTextView = (TextView) alert.findViewById(message);
		messageTextView.setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
		alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	private void refrenshAdapter()
	{
		if(adapter != null) adapter.notifyDataSetChanged();
		showChannelData();
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		if(adapter.curBreaker == null) {
			Toast.makeText(this, resources.getString(R.string.toast4), Toast.LENGTH_SHORT).show();
			return false;
		}

		if(adapter.curBreaker.localLock) {
			String name =  adapter.curBreaker.title;
			if(name == null || name.length() == 0) {
				name = "此开关";
			}
			String message = LanguageHelper.changeLanguageText(name+"已被硬件锁定，请现场手动解除硬件锁定后再操作！");
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(!adapter.curBreaker.EnableNetCtrl && !Tooles.isLockFlag(adapter.curBreaker)) {
			String name =  adapter.curBreaker.title;
			if(name == null || name.length() == 0) {
				name = "此开关";
			}
			String message = LanguageHelper.changeLanguageText(name+"已因用电报警断电或现场关断，遥控功能关闭。请现场手动送电后恢复遥控功能！");
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(adapter.curBreaker.control == 0) {
			String name =  adapter.curBreaker.title;
			if(name == null || name.length() == 0) {
				name = "此开关";
			}
			String message = LanguageHelper.changeLanguageText(name+"已设置为不能遥控，请设置为能遥控再操作！");
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return false;
		}

		if(adapter.curBreaker.EnableNetCtrl && !adapter.curBreaker.OpenClose && adapter.curBreaker.remoteLock) {
			String name =  adapter.curBreaker.title;
			if(name == null || name.length() == 0) {
				name = "此开关";
			}
			String message = LanguageHelper.changeLanguageText(name+"已被分闸锁定，请解除分闸锁定后再操作！");
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：显示选择线路的具体数据
	private void showChannelData() {
		if(adapter.curBreaker == null) return;
		
		boolean oc = adapter.curBreaker.OpenClose;
		String switchStatus = " · "+ LanguageHelper.changeLanguageText("已通");
		if(oc) { //打开状态
			switchStatus = " · "+ LanguageHelper.changeLanguageText("已通");
			bottomView.setBackgroundColor(0xfff77c55);
			lineView.setBackgroundColor(0xfffbbda9);
			int icon = (!adapter.curBreaker.EnableNetCtrl && Tooles.isLockFlag(adapter.curBreaker))?R.drawable.switch_but_lock:R.drawable.switch_but_off;
			switchButton.setBackgroundResource(icon);
		}else { //关闭状态
			switchStatus = " · "+ LanguageHelper.changeLanguageText("已断");
			bottomView.setBackgroundColor(0xff7ac058);
			lineView.setBackgroundColor(0xffbce0ab);
			int icon = (!adapter.curBreaker.EnableNetCtrl && Tooles.isLockFlag(adapter.curBreaker))?R.drawable.switch_but_lock:R.drawable.switch_but_on;
			switchButton.setBackgroundResource(icon);
		}
		
		String name = adapter.curBreaker.title;
		if(name != null && name.length()>0) {
            name = LanguageHelper.changeLanguageNode(name);
			nameView.setText(name+switchStatus);
		}
		
		//漏电流
		float _ald = adapter.curBreaker.A_LD;
		
		//功率
		float _ap = adapter.curBreaker.A_WP;
		
		//温度
		float _at = adapter.curBreaker.A_T;
		
		//电流
		float _aa = adapter.curBreaker.A_A;
		
		//电压
		float _av = adapter.curBreaker.A_V;

		if(adapter.curBreaker.lineType.equals("380")) { //380V 3相总路
			_ald = adapter.curBreaker.G_LD;
			_ap = adapter.curBreaker.G_WP;
			_at = adapter.curBreaker.G_T;
			_aa = adapter.curBreaker.G_A;
			_av = adapter.curBreaker.G_V;
		}

		int ald = (int) _ald;
		int ap = (int) _ap;
		int at = (int) _at;
		int av = (int) _av;

		String aa = _aa + "";
		if(aa.equals("0.0")) aa = "0";

        aldView.setText(ald + "");
		apView.setText(ap + "");
		atView.setText(at + "");
		aaView.setText(aa);
		avView.setText(av + "");
	}
	
}
