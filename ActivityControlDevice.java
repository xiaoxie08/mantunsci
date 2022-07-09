package snd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityControlDevice extends ActivityBase {

private int type; //-1为空调遥控，-2为开关，-3为插座
	
	private TextView tig1View;
	private TextView tig2View;
	private ImageView logoView;
	private ImageView iconView;
	private LinearLayout icon2BgView;
	private EditText nameView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		type = intent.getIntExtra("type", -1);

		setContentView(R.layout.control_device);
		
		tig1View = (TextView)findViewById(R.id.tig1);
		tig2View = (TextView)findViewById(R.id.tig2);
		logoView = (ImageView)findViewById(R.id.logo);
		iconView = (ImageView)findViewById(R.id.icon);
		icon2BgView = (LinearLayout)findViewById(R.id.icon2_bg);
		nameView = (EditText)findViewById(R.id.name);
		
		if(type == -1) { //空调遥控
			tig1View.setText(R.string.addcontrol_tig1);
			tig2View.setText(R.string.addcontrol_tig2);
			logoView.setImageResource(R.drawable.addcontrol_logo1);
			iconView.setImageResource(R.drawable.addcontrol_icon1);
		}else if(type == -2) { //开关
			tig1View.setText(R.string.addcontrol_tig5);
			tig2View.setText(R.string.addcontrol_tig6);
			logoView.setImageResource(R.drawable.addcontrol_logo3);
			iconView.setImageResource(R.drawable.addcontrol_icon4);
			icon2BgView.setVisibility(View.GONE);
		}else if(type == -3) { //插座
			tig1View.setText(R.string.addcontrol_tig3);
			tig2View.setText(R.string.addcontrol_tig4);
			logoView.setImageResource(R.drawable.addcontrol_logo2);
			iconView.setImageResource(R.drawable.addcontrol_icon3);
			icon2BgView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：保存按钮事件
	public void saveAction(View v) { 
		if(check()) {
			String name = nameView.getText().toString();
			String channelId = type+"";
			
			
		}
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测
	private boolean check() {
		String name = nameView.getText().toString();
		if(name.length() == 0) {
			Toast.makeText(this, "请取名！", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
}
