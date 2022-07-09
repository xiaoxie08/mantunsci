package snd.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import snd.util.LanguageHelper;
import snd.util.TTS;
import snd.util.Tooles;
import snd.view.MyAlertDialog;

public class ActivityAlarmSetting extends ActivityBase {

    private Resources resources;

    private ImageButton checkButton;
    private RelativeLayout voiceBgView;
    private TextView voiceTitleView;
    private TextView voiceView;
    private TextView typeView;

    private HashMap<String,String> datas;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);
	    
	    setContentView(R.layout.alarmsetting);

        checkButton = (ImageButton)findViewById(R.id.check_but);
        voiceBgView = (RelativeLayout)findViewById(R.id.voice_bg);
        voiceTitleView = (TextView)findViewById(R.id.voice_text);
        voiceView = (TextView)findViewById(R.id.voice);
        typeView = (TextView)findViewById(R.id.type);

        datas = Tooles.getAlarVoiceSetting(this);
        String check = datas.get("check");
        String voice = datas.get("voice");

        boolean state = check.equals("true")?true:false;
        int color = state?0xff000000:0xffbdbfbf;
        checkButton.setSelected(state);
        voiceBgView.setEnabled(state);
        voiceTitleView.setTextColor(color);
        voiceView.setTextColor(color);

        String type = voice.equals("2")?"语音播报":"普通铃声";
        type = LanguageHelper.changeLanguageText(type);
        voiceView.setText(type);
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
    //方法功能：打开铃声按钮事件
    public void checkAction(View v) {
        boolean state = !v.isSelected();
        checkButton.setSelected(state);

        int color = state?0xff000000:0xffbdbfbf;
        voiceBgView.setEnabled(state);
        voiceTitleView.setTextColor(color);
        voiceView.setTextColor(color);

        String check = state?"true":"false";
        datas.put("check", check);
        Tooles.saveAlarmVoiceSetting(datas, this);
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选择铃声事件
    public void voiceAction(View v) {
        String[] items = {"普通铃声", "语音播报"};

        for (int i=0; i<items.length; i++) items[i] = LanguageHelper.changeLanguageText(items[i]);

        String voice = datas.get("voice");
        int select = voice.equals("2")?1:0;

        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
        new MyAlertDialog(ctw)
                .setTitle(resources.getString(R.string.alert36))
                .setSingleChoiceItems(items, select, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        if (which == 0) { //普通铃声
                            try{
                                //响铃
                                MediaPlayer mp = new MediaPlayer();
                                mp.setDataSource(ActivityAlarmSetting.this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                mp.setAudioStreamType(AudioManager.STREAM_RING);
                                mp.prepare();
                                mp.start();
                                mp.setLooping(false); //是否循环播放
                            }catch(Exception e){}
                        }else { //语音播报
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //TTS.getInstance(ActivityAlarmSetting.this).speak("漏电报警");

                                    String text = LanguageHelper.changeLanguageAlarm("漏电报警");
                                    TTS.getInstance(ActivityAlarmSetting.this).speak(text);
                                }
                            }).start();
                        }

                        String voice = (which+1)+"";
                        datas.put("voice", voice);
                        Tooles.saveAlarmVoiceSetting(datas, ActivityAlarmSetting.this);

                        String type = voice.equals("2")?"语音播报":"普通铃声";
                        type = LanguageHelper.changeLanguageText(type);
                        voiceView.setText(type);
                    }
                })
                .show();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选择报警类型事件
    public void typeAction(View v) {
        Intent intent = new Intent(this, ActivityAlarmType.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }
	
}
