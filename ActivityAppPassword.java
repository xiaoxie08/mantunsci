package snd.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityAppPassword extends ActivityBase {
    private RelativeLayout settingLayout;
    private TextView tigView;
    private ImageButton selectButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.app_password);

        settingLayout = (RelativeLayout)findViewById(R.id.app_password_setting);
        tigView = (TextView)findViewById(R.id.app_password_tig);
        selectButton = (ImageButton)findViewById(R.id.app_password_selectbutton);

        SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
        String password = preferences.getString("app_password", "");
        if(password != null && password.length()>0) {
            tigView.setVisibility(View.GONE);
            selectButton.setSelected(true);
        }else {
            settingLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivitySetting.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：是否选择手势密码按钮事件
    public void selectAction(View v) {

        if(v.isSelected()) {
            settingLayout.setVisibility(View.GONE);
            tigView.setVisibility(View.VISIBLE);
            v.setSelected(!v.isSelected());

            SharedPreferences preferences = getSharedPreferences("appPassWord", MODE_WORLD_READABLE);
            Editor editor = preferences.edit();
            editor.remove("app_password");
            editor.commit();
        }else {
            Intent intent = new Intent (this, ActivitySettingAppPassword.class);
            intent.putExtra("currentPage", currentPage);
            startActivity(intent);
        }

    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：修改手势密码按钮事件
    public void settingAction(View v) {
        Intent intent = new Intent (this, ActivitySettingAppPassword.class);
        intent.putExtra("currentPage", currentPage);
        startActivity(intent);
    }
}
