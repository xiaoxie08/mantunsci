package snd.ui;

import snd.adapter.AdapterSysset;
import snd.database.DBconfig;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ActivitySysSet extends ActivityBase implements OnClickListener
{
    private static final String TAG=ActivitySysSet.class.getName();
	
	TextView textView2;
	GridView grid;
	AdapterSysset adapter;

	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.sysset);
        
        textView2=(TextView)this.findViewById(R.id.textView2);
        
        textView2.setOnClickListener(this);
        
        adapter=new AdapterSysset(this);
        grid=(GridView) findViewById(R.id.list);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				adapter.selected=arg2;
				adapter.notifyDataSetChanged();
				
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("audio/*");
				startActivityForResult(intent,444);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		//Loger.Log(TAG, requestCode+" "+resultCode+" "+data);
		if(requestCode==444 && data!=null)
		{
			Uri uri=data.getData();
			String file = ((APP)this.getApplication()).AudioUriToFile(uri); 
			//Loger.Log(TAG, file);
			DBconfig.UpdateConfig("DOORTALK", "RINGFILE", file);
		}
	}
	
	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		    case R.id.textView2:
		    {
		    	this.startActivity(new Intent(this,ActivityMenu.class));
				this.finish();
		    }
		    break;
		}
	}
}
