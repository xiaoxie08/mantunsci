package snd.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import snd.view.MyAlertDialog;

public class ActivityAdvertSetting extends ActivityBase {

    private Resources resources;

    private ImageButton checkButton;
    private RelativeLayout advertBgView;
    private TextView advertTitleView;
    private TextView advertView;

    private int mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.advertsetting);

        checkButton = (ImageButton)findViewById(R.id.check_but);
        advertBgView = (RelativeLayout)findViewById(R.id.advertsetting_bg);
        advertTitleView = (TextView)findViewById(R.id.advertsetting_text);
        advertView = (TextView)findViewById(R.id.advertsetting);

        SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
        boolean mark = preferences.getBoolean("advertising", true);
        mode = preferences.getInt("advertisingMode", 1);

        int color = mark?0xff000000:0xffbdbfbf;
        checkButton.setSelected(mark);
        advertBgView.setEnabled(mark);
        advertTitleView.setTextColor(color);
        advertView.setTextColor(color);

        int text = (mode == 2)?R.string.advertsetting_tig4:R.string.advertsetting_tig3;
        advertView.setText(text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivitySetting.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：是否播放广告按钮事件
    public void checkAction(View v) {
        boolean mark = !v.isSelected();
        checkButton.setSelected(mark);

        int color = mark ? 0xff000000 : 0xffbdbfbf;
        advertBgView.setEnabled(mark);
        advertTitleView.setTextColor(color);
        advertView.setTextColor(color);

        SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("advertising", mark);
        editor.commit();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选择播放模式事件
    public void advertAction(View v) {
        final String[] items = {resources.getString(R.string.advertsetting_tig3), resources.getString(R.string.advertsetting_tig4)};
        int select = (mode == 2)?1:0;

        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
        new MyAlertDialog(ctw)
                .setTitle(resources.getString(R.string.alert38))
                .setSingleChoiceItems(items, select, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        mode = which+1;
                        SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("advertisingMode", mode);
                        editor.commit();

                        String text = items[which];
                        advertView.setText(text);
                    }
                })
                .show();
    }

}
