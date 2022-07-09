package snd.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import snd.dialog.DialogPopAlarm;
import snd.util.Tooles;

public class ShowNewAlarmActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); //去掉标题栏
        Window window = getWindow();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; //保持屏幕亮着
        lp.alpha = 0.0f; //透明度：0.0全透明 1.0不透明
        window.setAttributes(lp);

        View view = this.getWindow().getDecorView();
        Tooles.hideBottomUIMenu(view);
        
        Intent intent = getIntent();
        long autoid = intent.getLongExtra("autoid", 0);
        String datetime = intent.getStringExtra("datetime");
        String node = intent.getStringExtra("node");
        String type = intent.getStringExtra("type");
        String info = intent.getStringExtra("info");

        if(APP.popdialog != null)
		{
			APP.popdialog.dismiss();
			APP.popdialog = null;
		}

		APP.popdialog = new DialogPopAlarm(this);
		APP.popdialog.setAlarm(autoid, datetime, node, type, info, this);
		APP.popdialog.show();
		
		//APP.activity = null;
	}
	
}
