package snd.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import snd.database.DBconfig;

public class ActivityServerSetting extends ActivityBase {

	private Resources resources;
	private EditText inputBoxView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = getResources();

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);

		setContentView(R.layout.serversetting);

		inputBoxView = (EditText)findViewById(R.id.edit);

		inputBoxView.setHint(resources.getString(R.string.serversetting_hint));
		inputBoxView.setText(APP.Server);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		Intent intent = new Intent(this, ActivitySetting.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：保存按钮事件
	public void sureAction(View v) {
		String url = inputBoxView.getText().toString();
		if(url != null && url.length()>0) {
			if(!url.contains("http://") && !url.contains("https://")) {
				url = "http://"+url;
			}

			APP.Server = url;
			DBconfig.UpdateConfig("DBOX", "SERVER", APP.Server);

			Toast.makeText(this, resources.getString(R.string.toast8), Toast.LENGTH_SHORT).show();
			backAction(v);
		}else
			Toast.makeText(this, resources.getString(R.string.serversetting_toast), Toast.LENGTH_SHORT).show();
	}
	
}
