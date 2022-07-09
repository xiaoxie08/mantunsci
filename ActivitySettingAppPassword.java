package snd.ui;

import snd.gesturespassword.ContentView;
import snd.gesturespassword.ScreenUtils;
import snd.gesturespassword.Drawl.GestureCallBack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ActivitySettingAppPassword extends ActivityBase {

    private TextView tigView;
    private Button resetButton;
    private FrameLayout body_layout;
    private ContentView content;
    private String pw = "";
    private String oldPassWord;
    private boolean isUpdate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.setting_gestpassword);

        tigView = (TextView)findViewById(R.id.settingapppassword_tig);
        resetButton = (Button)findViewById(R.id.settingapppassword_reset);
        body_layout = (FrameLayout)findViewById(R.id.body_layout);

        resetButton.setVisibility(View.INVISIBLE);

        body_layout.setPadding(0, 10, 0, 10);

        SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
        oldPassWord = preferences.getString("app_password", "");
        if(oldPassWord != null && oldPassWord.length()>0) {
            tigView.setText(R.string.settingapppassword_text3);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int height = body_layout.getHeight();
        ScreenUtils.height = height;
        addContentView();
    }

    private void addContentView() {
        if(content == null) {
            //初始化一个显示各个点的viewGroup
            content = new ContentView(this, "", new GestureCallBack() {

                @Override
                public void checkedSuccess() {

                }

                @Override
                public void checkedFail() {

                }

                @Override
                public void getPassword(String passWord) {
                    if(pw.length()>0) {
                        if(pw.equals(passWord)) {
                            tigView.setTextColor(android.graphics.Color.BLACK);
                            tigView.setText(R.string.settingapppassword_text4);
                            resetButton.setVisibility(View.INVISIBLE);

                            SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
                            Editor editor = preferences.edit();
                            editor.putString("app_password", pw);
                            editor.commit();

                            Intent intent = new Intent(ActivitySettingAppPassword.this, ActivityAppPassword.class);
                            intent.putExtra("currentPage", currentPage);
                            ActivitySettingAppPassword.this.startActivity(intent);
                            ActivitySettingAppPassword.this.finish();
                        }else {
                            tigView.setTextColor(android.graphics.Color.RED);
                            tigView.setText(R.string.settingapppassword_text5);
                            resetButton.setVisibility(View.VISIBLE);
                        }
                    }else {

                        if(oldPassWord != null && oldPassWord.length()>0 && !isUpdate) {
                            if(oldPassWord.equals(passWord)) {
                                isUpdate = true;
                                tigView.setTextColor(android.graphics.Color.BLACK);
                                tigView.setText(R.string.settingapppassword_text6);
                            }else {
                                tigView.setTextColor(android.graphics.Color.RED);
                                tigView.setText(R.string.settingapppassword_text7);
                            }
                        }else {
                            if(passWord.length() < 4) { //至少连接4个点
                                tigView.setTextColor(android.graphics.Color.RED);
                                tigView.setText(R.string.settingapppassword_text8);
                            }else {
                                tigView.setTextColor(android.graphics.Color.BLACK);
                                tigView.setText(R.string.settingapppassword_text9);
                                pw = passWord;
                            }
                        }

                    }
                }

            });

            //设置手势解锁显示到哪个布局里面
            content.setParentView(body_layout);
        }
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivityAppPassword.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：重新设置手势按钮事件
    public void resetAction(View v) {
        pw = "";

        tigView.setTextColor(android.graphics.Color.BLACK);
        if(isUpdate) {
            tigView.setText(R.string.settingapppassword_text6);
        }else
            tigView.setText(R.string.settingapppassword_text1);
        resetButton.setVisibility(View.INVISIBLE);
    }
}
