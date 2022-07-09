package snd.ui;

import java.util.HashMap;
import java.util.List;

import snd.adapter.EleAppStateAdapter;
import snd.database.DBalarminfo;
import snd.view.MyAlertDialog;
import snd.view.OptionsPopupWindow;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityEleAppState extends ActivityBase {

	private Resources resources;
	
	private TextView screenTextView;
	private ImageView screenIconView;
	private ListView listView;
	
	public HashMap<Long,DBalarminfo> curSelectDatas = new HashMap<Long,DBalarminfo>();
	private EleAppStateAdapter adapter;
	private LoadDataAsyncTask loaddata;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = getResources();

		setContentView(R.layout.eleappstate);
		
	    screenTextView = (TextView)findViewById(R.id.screentext);
	    screenIconView = (ImageView)findViewById(R.id.screenicon);
	    listView = (ListView)findViewById(R.id.info_list);
	    
	    adapter = new EleAppStateAdapter(this);
  	    listView.setAdapter(adapter);
  	    
  	    loadDatas();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
		
		curSelectDatas.clear();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：加载数据
	public void loadDatas() {
		String where = "where type='信息'";
		String name = screenTextView.getText().toString();
		if(name != null && name.length() > 0 && !name.equals("全部") && !name.equals("All")) {
			where = where + "and info like '%"+name+"%'";
		}
		
		if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
		loaddata=new LoadDataAsyncTask();
	    loaddata.execute(new String[]{where});
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this,ActivityMenu.class));
		this.finish();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：筛选按钮事件
	public void screenAction(View v) {
		new OptionsPopupWindow(this, v, screenTextView, screenIconView).show();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：删除按钮事件
	public void deleteAction(View v) {
		if(curSelectDatas.size()>0) {
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(resources.getString(R.string.alert1))
			.setTitle(resources.getString(R.string.tig7))
			.setPositiveButton(resources.getString(R.string.tig5),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
							deleteInfo();
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
			).show();
			
			int message = this.getResources().getIdentifier("message","id","android");        
	    	TextView messageTextView = (TextView) alert.findViewById(message);        
	    	messageTextView.setTextSize(27); 
			alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
		}else {
			Toast.makeText(this, resources.getString(R.string.toast2), Toast.LENGTH_SHORT).show();
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：我的电器按钮事件
	public void eleappAction(View v) {
		Intent intent = new Intent(this, ActivityDevice.class);			
		startActivity(intent);
		this.finish();
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：添加按钮事件
	public void addAction(View v) {
		Intent intent = new Intent (this, ActivityDeviceEdit.class);
		intent.putExtra("autoid", -1);
		intent.putExtra("type", 1);
		startActivity(intent);
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：选中事件
	public void itemClickAction(View v) { 
		DBalarminfo info = (DBalarminfo) v.getTag();
		if(!curSelectDatas.containsKey(info.getAutoid())) {
			curSelectDatas.put(info.getAutoid(), info);
		}else
			curSelectDatas.remove(info.getAutoid());
		
		adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除选中的信息
	private void deleteInfo() {
		for(DBalarminfo info : curSelectDatas.values()) {
			DBalarminfo.VirtualDelete(info.getAutoid());
		}
		
		loadDatas();
		Toast.makeText(this, resources.getString(R.string.toast1), Toast.LENGTH_SHORT).show();
	}
	
	private class LoadDataAsyncTask extends AsyncTask<String, Void, List<DBalarminfo>>
	{
		@Override
		protected List<DBalarminfo> doInBackground(String... params) 
		{
			return DBalarminfo.GetAlarmsList(params[0], "30");
		}
		
		@Override  
		protected void onPostExecute(List<DBalarminfo> result)
		{
			adapter.info=result;
			adapter.notifyDataSetChanged();
		}
	}
	
}
