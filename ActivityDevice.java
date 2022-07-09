package snd.ui;

import java.util.List;

import snd.adapter.AdapterDevices;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.DBdevices;
import snd.util.LanguageHelper;
import snd.view.MyAlertDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

public class ActivityDevice extends ActivityBase
{
	private static final String TAG=ActivityDevice.class.getName();

	private Resources resources;

	GridView grid;
	
	AdapterDevices adapter;
	LoadDataAsyncTask loaddata;
	BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
        setContentView(R.layout.device);
        
        grid = (GridView) findViewById(R.id.list);
        
        adapter = new AdapterDevices(this);
        grid.setAdapter(adapter);
        
        br = new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				if(intent.getAction().equals(Second1BroadcastReceiver.Msg_1S))
				{
					loadDatas();
				}
		    }
		};
	}
	
	@Override
	public void onStart()
	{
		super.onStart();

		loadDatas();
		this.registerReceiver(br, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		
		this.unregisterReceiver(br);
		if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：加载数据
	private void loadDatas() {
		if (loaddata != null && loaddata.getStatus() != AsyncTask.Status.FINISHED)
		{
			loaddata.cancel(true);
		}
		loaddata=new LoadDataAsyncTask();
	    loaddata.execute(new Void[]{});
	}
	
	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		this.startActivity(new Intent(this, ActivityEleAppState.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：选中事件
	public void itemClickAction(View v) { 
		int position = (Integer) v.getTag();
		long autoid = -1;
		if(position != -1) {
			DBdevices rec = (DBdevices)adapter.getItem(position);
			autoid = rec.getAutoid();
		}
		
		Intent intent = new Intent (this, ActivityDeviceEdit.class);
		intent.putExtra("autoid", autoid);
		intent.putExtra("type", 2);
		startActivity(intent);
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除按钮事件
	public void deleteAction(View v) { 
		int position = (Integer) v.getTag();
		final DBdevices rec = (DBdevices)adapter.getItem(position);
		String info = "您确定要删除"+rec.getName()+"这个电器吗？";
		info = LanguageHelper.changeLanguageText(info);
		
		AlertDialog alert = new MyAlertDialog(this)
        .setMessage(info)
        .setTitle(resources.getString(R.string.tig7))
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
        {
             public void onClick(DialogInterface dialog, int id) 
             {
            	 DBdevices.VirtualDelete(rec.getAutoid());
            	 loadDatas();
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
		
		int message = this.getResources().getIdentifier("message","id","android");        
    	TextView messageTextView = (TextView) alert.findViewById(message);        
    	messageTextView.setTextSize(27); 
		alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
	}
	
	private class LoadDataAsyncTask extends AsyncTask<Void, Void, List<DBdevices>>
	{
		@Override
		protected List<DBdevices> doInBackground(Void... params) 
		{
			return DBdevices.GetDevicesList(null);
		}
		
		@Override  
		protected void onPostExecute(List<DBdevices> result)
		{
			adapter.setData(result);
			adapter.notifyDataSetChanged();
		}
	}
	
}
