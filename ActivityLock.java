package snd.ui;

import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.view.LockPatternView;
import snd.view.LockPatternView.Cell;
import snd.view.LockPatternView.DisplayMode;
import snd.view.LockPatternView.OnPatternListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityLock extends ActivityBase implements OnPatternListener
{
	private static final String TAG=ActivityLock.class.getName();
	TextView textView1;
	TextView textView2;
	TextView textView3;
	ImageView imageView1;
	LockPatternView lockPatternView1;
	
	BroadcastReceiver bcReceiver;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
        
        textView1=(TextView)this.findViewById(R.id.textView1);
        textView2=(TextView)this.findViewById(R.id.textView2);
        textView3=(TextView)this.findViewById(R.id.textView3);
        imageView1=(ImageView)this.findViewById(R.id.imageview1);
        lockPatternView1=(LockPatternView)this.findViewById(R.id.lockPatternView1);
        
        imageView1.setImageResource(APP.weathId);
        lockPatternView1.setOnPatternListener(this);
        lockPatternView1.clearPattern();
        
        bcReceiver=new BroadcastReceiver()
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				String action  = intent.getAction();
				if(action.equals(Second1BroadcastReceiver.Msg_1S))
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE");
				    String[] s=format.format(new Date()).split(" ");
				    imageView1.setImageResource(APP.weathId);
    		    	textView1.setText(s[0]);
                    textView2.setText(s[1]);
                    textView3.setText(s[2]);
				}
			}
        };
        
    }
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.registerReceiver(bcReceiver, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
	}
	
	@Override
    public void onDestroy()
	{
    	super.onDestroy();
    	this.unregisterReceiver(bcReceiver);
    }

	@Override
	public void onPatternStart() {}

	@Override
	public void onPatternCleared() {}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {}

	@Override
	public void onPatternDetected(List<Cell> pattern) 
	{
		String pwd=lockPatternView1.getPatternValue();
		if(APP.LockScreenPwd.equals(""))
		{
			this.startActivity(new Intent(this,ActivityMenu.class));
			this.finish();
		}
		else if(APP.LockScreenPwd.equals(pwd))
		{
			this.startActivity(new Intent(this,ActivityMenu.class));
			this.finish();
		}
		else
		{
			lockPatternView1.setDisplayMode(DisplayMode.Wrong);
		}
	}

}
