package snd.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import snd.dialog.DialogPopAdvertising1;
import snd.model.AdvertisingData;
import snd.util.Tooles;

public class ActivityAdvertising extends Activity {

    public DialogPopAdvertising1 dialogPop;
    private MyBroadcastReciver myBroadcastRecive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        Window window = getWindow();
        window.setBackgroundDrawable(getResources().getDrawable(R.color.advertising_bg));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;//保持屏幕亮着
        lp.alpha = 1.0f;//透明度  0.0全透明 1.0不透明
        window.setAttributes(lp);

        View view = this.getWindow().getDecorView();
        Tooles.hideBottomUIMenu(view);

        Intent intent = getIntent();
        List<AdvertisingData> datas = (List<AdvertisingData>)intent.getSerializableExtra("datas");
        new DialogPopAdvertising1(this, datas, 0).show();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.dismiss.advertising");
        myBroadcastRecive = new MyBroadcastReciver();
        registerReceiver(myBroadcastRecive, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myBroadcastRecive);

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    private class MyBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("cn.dismiss.advertising")
                && dialogPop != null && dialogPop.isShowing()) {
                dialogPop.dismiss();
                dialogPop = null;
            }
        }
    }

}
