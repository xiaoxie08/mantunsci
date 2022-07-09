package snd.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import snd.gesturespassword.ContentView;
import snd.gesturespassword.Drawl;
import snd.gesturespassword.ScreenUtils;
import snd.passlibrary.PayPassDialog;
import snd.passlibrary.PayPassView;
import snd.util.LanguageHelper;
import snd.view.MyAlertDialog;

public class ActivityGesturesPassword extends ActivityBase {

    private Resources resources;

    private int type = 1; //1:主界面；2:电量；3:漏电自检
    private FrameLayout body_layout;
    private int number = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 1);

        setContentView(R.layout.gesturespassword);

        body_layout = (FrameLayout) findViewById(R.id.body_layout);

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        ScreenUtils.height = height;

        SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
        String password = preferences.getString("app_password", "");

        //初始化一个显示各个点的viewGroup
        ContentView content = new ContentView(this, password, new Drawl.GestureCallBack() {

            @Override
            public void checkedSuccess() {
                APP.isGesturespassword = false;

                if(type == 1) { //主界面
                    Intent intent = new Intent (ActivityGesturesPassword.this, ActivityMenu.class);
                    startActivity(intent);
                }else if(type == 2) { //电量
                    Intent intent = new Intent (ActivityGesturesPassword.this, ActivityPower.class);
                    startActivity(intent);
                }else { //漏电自检
                    Intent intent = new Intent (ActivityGesturesPassword.this, ActivityLeak.class);
                    startActivity(intent);
                }

                ActivityGesturesPassword.this.finish();
            }

            @Override
            public void checkedFail() {
                number--;
                if(number < 1) {
                    AlertDialog alert = new MyAlertDialog(ActivityGesturesPassword.this)
                    .setMessage(resources.getString(R.string.alert37))
                    .setTitle(resources.getString(R.string.tig7))
                    .setCancelable(false)
                    .setPositiveButton(resources.getString(R.string.tig11),
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    payDialog();

                                    SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("app_password", "-");
                                    editor.commit();
                                }
                            })
                    .show();

                    final int message = ActivityGesturesPassword.this.getResources().getIdentifier("message","id","android") ;
                    TextView messageTextView = (TextView) alert.findViewById(message);
                    messageTextView.setTextSize(27);
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
                }else {
                    String message = "密码错误，还可以再输入"+number+"次！";
                    message = LanguageHelper.changeLanguageLimitValue(message, number);
                    Toast.makeText(ActivityGesturesPassword.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void getPassword(String passWord) {

            }

        });

        //设置手势解锁显示到哪个布局里面
        content.setParentView(body_layout);

        if(password.equals("-")) {
            payDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：输入出厂密码窗口
    private void payDialog() {
        final PayPassDialog dialog = new PayPassDialog(this);
        dialog.getPayViewPass()
                .setPayClickListener(new PayPassView.OnPayClickListener() {
                    @Override
                    public void onPassFinish(String passContent) {
                        //6位输入完成回调
                        if (passContent.equals("888888")) {
                            dialog.dismiss();

                            APP.isGesturespassword = false;

                            SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("app_password");
                            editor.commit();

                            if(type == 1) { //主界面
                                Intent intent = new Intent (ActivityGesturesPassword.this, ActivityMenu.class);
                                startActivity(intent);
                            }else if(type == 2) { //电量
                                Intent intent = new Intent (ActivityGesturesPassword.this, ActivityPower.class);
                                startActivity(intent);
                            }else { //漏电自检
                                Intent intent = new Intent (ActivityGesturesPassword.this, ActivityLeak.class);
                                startActivity(intent);
                            }

                            ActivityGesturesPassword.this.finish();
                        }else {
                            Toast.makeText(ActivityGesturesPassword.this, resources.getString(R.string.toast30), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPayClose() {
                        dialog.dismiss();

                        //关闭回调
                        Intent intent = new Intent (ActivityGesturesPassword.this, ActivitySleep.class);
                        startActivity(intent);
                        ActivityGesturesPassword.this.finish();
                    }

                    @Override
                    public void onPayForget() {
                        //dialog.dismiss();
                        //点击忘记密码回调
                    }
                });
    }

}
