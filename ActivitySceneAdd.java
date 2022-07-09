package snd.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import snd.adapter.SceneAddAdapter;
import snd.database.Breaker;
import snd.database.DBScene;
import snd.database.DBSceneTask;
import snd.model.SceneData;
import snd.model.SceneTaskData;
import snd.util.LanguageHelper;
import snd.util.Tooles;
import snd.view.SceneSwitchPopupWindow;
import snd.view.SceneTypePopupWindow;

public class ActivitySceneAdd extends ActivityBase {

    private Resources resources;

    private SceneData sceneData;
    private TextView titleView;
    private ImageView iconView;
    private ImageView arrowView;
    private EditText nameView;
    private GridView grid;
    private SceneAddAdapter adapter;
    private int sceneType;
    public Hashtable<Integer,SceneTaskData> datas = new Hashtable<Integer,SceneTaskData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);
        sceneData = (SceneData)intent.getSerializableExtra("SceneData");

        setContentView(R.layout.scene_add);

        titleView = (TextView) findViewById(R.id.navbar_title);
        iconView = (ImageView) findViewById(R.id.icon);
        arrowView = (ImageView) findViewById(R.id.arrow);
        nameView = (EditText) findViewById(R.id.edit);
        grid = (GridView) findViewById(R.id.list);

        adapter = new SceneAddAdapter(this);

        titleView.setText(R.string.scene_tig1);
        if(sceneData != null) {
            titleView.setText(R.string.scene_tig2);

            setSceneType(sceneData.getType(), false);
            nameView.setText(sceneData.getName());

            List<SceneTaskData> taskDatas = DBSceneTask.getDatas(sceneData.getAutoid());
            for (SceneTaskData data : taskDatas) {
                String channel = data.getChannel();
                if(Tooles.isInteger(channel)) {
                    datas.put(Integer.parseInt(channel), data);
                }
            }
            adapter.datas = taskDatas;
        }

        grid.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        datas.clear();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivityScene.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：保存按钮事件
    public void saveAction(View v) {
        if(check()) {
            boolean mark = true;
            String name = nameView.getText().toString();

            if(sceneData != null) { //修改场景
                sceneData.setType(sceneType);
                sceneData.setName(name);
                DBScene.update(sceneData);

                List<SceneTaskData> taskDatas = DBSceneTask.getDatas(sceneData.getAutoid());
                for (SceneTaskData data : taskDatas) {
                    String channel = data.getChannel();
                    if(Tooles.isInteger(channel) && datas.containsKey(Integer.parseInt(channel))) { //更新
                        SceneTaskData newData = datas.get(Integer.parseInt(channel));
                        if (newData.getAutoid() != -1) DBSceneTask.update(newData);
                    }else { //删除
                        DBSceneTask.delete(data.getAutoid());
                    }
                }

                //新增
                for (SceneTaskData data : adapter.datas) {
                    if (data.getAutoid() == -1) {
                        data.setSceneId(sceneData.getAutoid());
                        DBSceneTask.insert(data);
                    }
                }
            }else { //添加场景
                SceneData data = new SceneData();
                data.setType(sceneType);
                data.setName(name);
                long sceneId = DBScene.insert(data);
                if(sceneId != -1) {
                    for (SceneTaskData taskData : adapter.datas) {
                        taskData.setSceneId(sceneId);
                        DBSceneTask.insert(taskData);
                    }
                }else
                    mark = false;
            }

            if (mark) {
                Toast.makeText(this, resources.getString(R.string.toast8), Toast.LENGTH_SHORT).show();
                this.backAction(null);
            }else
                Toast.makeText(this, resources.getString(R.string.toast22), Toast.LENGTH_SHORT).show();
        }
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：场景类型选择按钮事件
    public void typeClickAction(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        new SceneTypePopupWindow(this, v, arrowView).show();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：添加线路按钮事件
    public void addAction(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        new SceneSwitchPopupWindow(this, v).show();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：线路状态选择按钮事件
    public void checkAction(View v) {
        SceneTaskData data = (SceneTaskData)v.getTag();
        data.setTask(v.isSelected()?0:1);
        adapter.notifyDataSetChanged();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：删除按钮事件
    public void deleteAction(View v) {
        SceneTaskData data = (SceneTaskData)v.getTag();
        String channel = data.getChannel();
        if(Tooles.isInteger(channel) && datas.containsKey(Integer.parseInt(channel))) {
            datas.remove(Integer.parseInt(channel));
            adapter.datas.remove(data);
            adapter.notifyDataSetChanged();
        }
    }

    public void setSceneType(int type, boolean mark) {
        sceneType = type;

        String names[] = {"自定义", "在家", "离家", "起床", "睡觉", "上班", "下班"};
        int icons[] = {R.drawable.scene_11, R.drawable.scene_3, R.drawable.scene_5, R.drawable.scene_7, R.drawable.scene_9, R.drawable.scene_17, R.drawable.scene_19};
        String name = names[type];
        int icon = icons[type];

        name = LanguageHelper.changeLanguageSceneName(name);

        nameView.setText(name);
        iconView.setImageResource(icon);

        if (type == 0) {
            nameView.setText("");
            nameView.setEnabled(true);

            if(mark) {
                nameView.setFocusable(true);
                nameView.setFocusableInTouchMode(true);
                nameView.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }else {
            nameView.setEnabled(false);
            nameView.setFocusable(false);
            nameView.setFocusableInTouchMode(false);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public void updateSceneTask() {
        adapter.datas.clear();

        ArrayList<SceneTaskData> values = new ArrayList<SceneTaskData>(datas.values());
        if (values.size() > 1) {
            Collections.sort(values, new Comparator<SceneTaskData>() {
                @Override
                public int compare(SceneTaskData arg0, SceneTaskData arg1) {
                    String channel0 = arg0.getChannel();
                    String channel1 = arg1.getChannel();
                    if(Tooles.isInteger(channel0) && Tooles.isInteger(channel1)) {
                        int addr0 = Integer.parseInt(channel0);
                        int addr1 = Integer.parseInt(channel1);
                        if (addr0 < addr1) {
                            return -1;
                        }
                    }

                    return 1;
                }
            });
        }

        adapter.datas.addAll(values);
        adapter.notifyDataSetChanged();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：检测
    private boolean check() {
        if(nameView.getText().toString().equals(""))
        {
            Toast.makeText(this, resources.getString(R.string.toast20), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(datas.size() == 0) {
            Toast.makeText(this, resources.getString(R.string.toast21), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
