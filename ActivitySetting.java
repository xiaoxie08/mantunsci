package snd.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import snd.adapter.SettingAdapter;
import snd.util.LanguageHelper;
import snd.view.MyAlertDialog;

public class ActivitySetting extends ActivityBase {

    private Resources resources;
	private ListView listView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);
	    
	    setContentView(R.layout.setting);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new SettingAdapter(this));
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

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：按钮事件
    public void itemClickAction(View v) {
        int position = (Integer) v.getTag();
        switch(position) {
            case 0: //新报警提醒
            {
                Intent intent = new Intent(this, ActivityAlarmSetting.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 1: //线路地址设置
            {
                Intent intent = new Intent(this, ActivityAutoAddress1.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 2: //云平台指向
            {
                Intent intent = new Intent(this, ActivityServerSetting.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 3: //手势密码
            {
                Intent intent = new Intent(this, ActivityAppPassword.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 4: //当前城市
            {
                Intent intent = new Intent(this, ActivitySelectCity.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 5: //语言
            {
                String[] items = {"简体中文", "English"};

                final int select = APP.language.equals("en")?1:0;

                ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
                new MyAlertDialog(ctw)
                        .setTitle(resources.getString(R.string.alert34))
                        .setSingleChoiceItems(items, select, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();

                                if(select == which) return;

                                String language = "";
                                if (which == 0) { //简体中文
                                    language = "zh";
                                }else { //English
                                    language = "en";
                                }

                                APP.language = LanguageHelper.changeAppLanguage(ActivitySetting.this, language);

                                Intent it = new Intent(ActivitySetting.this, ActivitySleep.class);
                                ActivitySetting.this.startActivity(it);

                                ActivitySetting.this.finish();
                            }
                        })
                        .show();
            }
            break;
            case 6: //广告设置
            {
                Intent intent = new Intent(this, ActivityAdvertSetting.class);
                intent.putExtra("currentPage", currentPage);
                this.startActivity(intent);
                this.finish();
            }
            break;
            case 7: //恢复出厂设置
            {
                AlertDialog alert = new MyAlertDialog(this)
                        .setTitle(resources.getString(R.string.tig7))
                        .setMessage(resources.getString(R.string.alert33))
                        .setCancelable(false)
                        .setPositiveButton(resources.getString(R.string.tig5)
                                , new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                        APP.restory();
                                        Toast.makeText(ActivitySetting.this, resources.getString(R.string.toast29), Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton(resources.getString(R.string.tig6)
                                , new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                    }
                                })
                        .show();

                final int message = this.getResources().getIdentifier("message","id","android");
                TextView messageTextView = (TextView) alert.findViewById(message);
                messageTextView.setTextSize(27);
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
            }
            break;
        }
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：是否播放广告选择事件
    public void selectAction(View v) {
	    /*boolean mark = !v.isSelected();
        v.setSelected(mark);

        SharedPreferences preferences = getSharedPreferences("Setting", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("advertising", mark);
        editor.commit();*/
    }
	
}
