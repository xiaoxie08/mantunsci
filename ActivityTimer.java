package snd.ui;

import java.util.HashMap;
import java.util.List;

import snd.adapter.AdapterTimer;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.DBtimer;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTimer extends ActivityBase
{
	private static final String TAG=ActivityTimer.class.getName();

	private Resources resources;

	private GridView grid;
	
	private LinearLayout bottomView;
	public Button editButton;
	private Button deleteButton;
	private Button selectButton;

	public HashMap<Long,DBtimer> curSelectDatas = new HashMap<Long,DBtimer>();
	
	AdapterTimer adapter;
	
	LoadDataAsyncTask loaddata;
	BroadcastReceiver br;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		resources = getResources();
		
        setContentView(R.layout.timer);
        
        grid = (GridView) findViewById(R.id.list);    
        
        editButton = (Button)findViewById(R.id.edit_button);
        bottomView = (LinearLayout)findViewById(R.id.bottom);
	    deleteButton = (Button)findViewById(R.id.button_delete);
	    selectButton = (Button)findViewById(R.id.button_select);

		String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
		String tig2 = resources.getString(R.string.tig2); //全选
		deleteButton.setText(tig1);
		selectButton.setText(tig2);
        
        adapter = new AdapterTimer(this);
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
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：返回按钮事件
	public void backAction(View v) { 
		this.startActivity(new Intent(this, ActivityMenu.class));
		this.finish();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：编辑按钮事件
	public void editAction(View v) { 
		editButton.setSelected(!editButton.isSelected());
    	if(editButton.isSelected()) {
    		int color = 0x00000000;
			String tig = resources.getString(R.string.tig4); //取消
    		editButton.setBackgroundColor(color);
    		editButton.setText(tig);
			editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_height);
    		
    		bottomView.setVisibility(View.VISIBLE);
    	}else {
    		editButton.setBackgroundResource(R.drawable.icon_delete_normal);
    		editButton.setText("");
			editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_button);
    		
    		curSelectDatas.clear();

			String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
			String tig2 = resources.getString(R.string.tig2); //全选

			deleteButton.setText(tig1);
			selectButton.setText(tig2);
    		selectButton.setSelected(false);
    		bottomView.setVisibility(View.GONE);
    	}
    	
    	if(adapter != null) adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：全选或全不选按钮事件
	public void selectAction(View v) { 
		if(adapter.list == null) return;
		curSelectDatas.clear();
		
		selectButton.setSelected(!selectButton.isSelected());
		if(selectButton.isSelected()) {
			String tig = resources.getString(R.string.tig3); //全不选
			selectButton.setText(tig);
			
			for(DBtimer data : adapter.list) {
				curSelectDatas.put(data.getAutoid(), data);
			}
		}else {
			String tig = resources.getString(R.string.tig2); //全选
			selectButton.setText(tig);
		}

		String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
		deleteButton.setText(tig);
		adapter.notifyDataSetChanged();
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除选择的信息按钮事件
	public void deleteAllAction(View v) { 
		if(curSelectDatas.size()>0) {
			AlertDialog alert = new MyAlertDialog(this)
			.setMessage(resources.getString(R.string.alert3))
			.setTitle(resources.getString(R.string.tig7))
			.setPositiveButton(resources.getString(R.string.tig5),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
							
							for(DBtimer data : curSelectDatas.values()) {
								DBtimer.VirtualDelete(data.getAutoid());				      
							}
							
							curSelectDatas.clear();

							String tig = resources.getString(R.string.tig1)+"(0)"; //删除(0)
							deleteButton.setText(tig);
							
							loadDatas();
							Toast.makeText(ActivityTimer.this, resources.getString(R.string.toast1), Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, resources.getString(R.string.toast3), Toast.LENGTH_SHORT).show();
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：选中事件
	public void itemClickAction(View v) { 
		int position = (Integer) v.getTag();
		long autoid = -1;
		DBtimer rec = null;
		if(position != -1) {
			rec = (DBtimer)adapter.getItem(position);
			autoid = rec.getAutoid();
		}
		
		if(editButton.isSelected() && rec != null) {
			if(!curSelectDatas.containsKey(autoid)) {
				curSelectDatas.put(autoid, rec);
			}else
				curSelectDatas.remove(autoid);

			String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
			deleteButton.setText(tig);
			adapter.notifyDataSetChanged();
		}else {
			Intent intent = new Intent (this, ActivityTimerEdit.class);
			intent.putExtra("autoid", autoid);
			startActivity(intent);
			this.finish();
		}
	}
	
	//方法类型：自定义方法
	//编   写：
	//方法功能：删除按钮事件
	public void deleteAction(View v) { 
		int position = (Integer) v.getTag();
		final DBtimer rec = (DBtimer)adapter.getItem(position);
		String info = resources.getString(R.string.alert4);
		
		AlertDialog alert = new MyAlertDialog(this)
        .setMessage(info)
        .setTitle(resources.getString(R.string.tig7))
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
        {
             public void onClick(DialogInterface dialog, int id) 
             {
            	 DBtimer.VirtualDelete(rec.getAutoid());
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
		
		final int message = ActivityTimer.this.getResources().getIdentifier("message","id","android") ;        
    	TextView messageTextView = (TextView) alert.findViewById(message);        
    	messageTextView.setTextSize(27);        
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
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
	
	private class LoadDataAsyncTask extends AsyncTask<Void, Void, List<DBtimer>>
	{
		@Override
		protected List<DBtimer> doInBackground(Void... params) 
		{
			return DBtimer.GetTimerList(null);
		}
		
		@Override  
		protected void onPostExecute(List<DBtimer> result)
		{
			adapter.setData(result);
			adapter.notifyDataSetChanged();
		}
	}
	
}
