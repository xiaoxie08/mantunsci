package snd.ui;

import snd.database.DBconfig;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityNetPassword extends ActivityBase {

	private Resources resources;
	
	private TextView deviceIdView;
	private EditText oldPawordInputBox;
	private EditText newPawordInputBox1;
	private EditText newPawordInputBox2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);
	    
	    setContentView(R.layout.netpassword);
	    
	    deviceIdView = (TextView)findViewById(R.id.changepassword_deviceId);
	    oldPawordInputBox = (EditText)findViewById(R.id.changepassword_pwd);
	    newPawordInputBox1 = (EditText)findViewById(R.id.changepassword_newpwd1);
	    newPawordInputBox2 = (EditText)findViewById(R.id.changepassword_newpwd2);
	    
	    deviceIdView.setText(APP.MAC);
	    
	    oldPawordInputBox.setFocusable(true);   
	    oldPawordInputBox.setFocusableInTouchMode(true);   
	    oldPawordInputBox.requestFocus();
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：返回按钮事件
	public void backAction(View v) {
		Intent intent = new Intent(this, ActivityMenu.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：保存按钮事件
	public void saveAction(View v) {
		if(check()) {
			APP.LoginPwd = newPawordInputBox1.getText().toString();
    		DBconfig.UpdateConfig("DBOX", "LOGINPWD", APP.LoginPwd);
    		Toast.makeText(this, resources.getString(R.string.toast8), Toast.LENGTH_SHORT).show();
			APP.isUpdateReelData = true;
		}
	}
	
	//方法类型：自定义方法
    //编   写：
	//方法功能：检测输入框
	private boolean check() {
		String passwork = APP.LoginPwd;
		String oldPassword = oldPawordInputBox.getText().toString();
		String newPassword1 = newPawordInputBox1.getText().toString();
		String newPassword2 = newPawordInputBox2.getText().toString();
		 
		if(oldPassword.length() == 0) { //提输入原密码
			Toast.makeText(this, resources.getString(R.string.toast23), Toast.LENGTH_SHORT).show();
			return false;
		}
		
	    if(passwork== null || 
				!passwork.equals(oldPassword)) {//判断原密码是否正确
			Toast.makeText(this, resources.getString(R.string.toast24), Toast.LENGTH_SHORT).show();
			return false;
		}
	    
	    if(newPassword1.length() == 0) { //提示输入新密码
			Toast.makeText(this, resources.getString(R.string.toast25), Toast.LENGTH_SHORT).show();
			return false;
		}
	    
	    if(newPassword2.length() == 0) { //提示输入确认密码
			Toast.makeText(this, resources.getString(R.string.toast26), Toast.LENGTH_SHORT).show();
			return false;
		}
	    
	    if(!newPassword1.equals(newPassword2)) { //提示确认密码输入不一致
			Toast.makeText(this, resources.getString(R.string.toast27), Toast.LENGTH_SHORT).show();
			return false;
		}
	    
	    return true;
	}

}
