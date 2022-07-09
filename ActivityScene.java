package snd.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import snd.adapter.SceneAdapter;
import snd.database.Breaker;
import snd.database.DBDelayedTask;
import snd.database.DBScene;
import snd.database.DBSceneTask;
import snd.database.DBswitchsetting;
import snd.model.SceneData;
import snd.model.SceneTaskData;
import snd.serialservice.SerialThread;
import snd.util.DelayedPicker;
import snd.util.Tooles;
import snd.view.MyAlertDialog;

public class ActivityScene extends ActivityBase {

    private Resources resources;

    private RelativeLayout noSceneView;
    private GridView grid;
    private SceneAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.scene);

        noSceneView = (RelativeLayout) findViewById(R.id.no_scene);
        grid = (GridView) findViewById(R.id.list);

        adapter = new SceneAdapter(this);
        grid.setAdapter(adapter);

        loadDatas();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：加载数据
    private void loadDatas() {
        adapter.datas = DBScene.getDatas();
        adapter.notifyDataSetChanged();

        if(adapter.datas.size() > 0) {
            noSceneView.setVisibility(View.GONE);
        }else
            noSceneView.setVisibility(View.VISIBLE);
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

    //方法类型：自定义方法
    //编   写：
    //方法功能：添加按钮事件
    public void addAction(View v) {
        Intent intent = new Intent (this, ActivitySceneAdd.class);
        intent.putExtra("currentPage", currentPage);
        startActivity(intent);
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选中事件
    public void itemClickAction(View v) {
        SceneData data = (SceneData) v.getTag();

        Intent intent = new Intent (this, ActivitySceneAdd.class);
        intent.putExtra("currentPage", currentPage);
        Bundle bundle = new Bundle();
        bundle.putSerializable("SceneData", data);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：执行按钮事件
    public void controlAction(View v) {
        final SceneData data = (SceneData) v.getTag();
        List<SceneTaskData> taskDatas = DBSceneTask.getDatas(data.getAutoid());
        List<String> channels1 = new ArrayList<String>(); //合闸线路
        List<String> channels2 = new ArrayList<String>(); //分闸线路
        for (SceneTaskData taskata : taskDatas) {
            String channel = taskata.getChannel();
            int task = taskata.getTask();
            if (task == 1) { //合闸
                channels1.add(channel);
            }else //分闸
                channels2.add(channel);
        }

        Collections.sort(channels1, new Comparator<String>(){
            @Override
            public int compare(String address0, String address1) {
                if(address0 != null && address1 != null
                        && Tooles.isNumber(address0) && Tooles.isNumber(address1)
                        && Integer.parseInt(address0) < Integer.parseInt(address1)) {
                    return -1;
                }
                return 1;
            }
        });

        Collections.sort(channels2, new Comparator<String>(){
            @Override
            public int compare(String address0, String address1) {
                if(address0 != null && address1 != null
                        && Tooles.isNumber(address0) && Tooles.isNumber(address1)
                        && Integer.parseInt(address0) < Integer.parseInt(address1)) {
                    return 1;
                }
                return -1;
            }
        });

        final List<String> keys1 = channels1;
        final List<String> keys2 = channels2;

        AlertDialog alert = new MyAlertDialog(this)
        .setMessage(resources.getString(R.string.alert30))
        .setTitle(resources.getString(R.string.tig7))
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.tig16), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();

                String name1s = "";
                String name2s = "";
                String name3s = "";
                String name4s = "";

                //合闸
                for(String channel : keys1) {
                    int channelId = Integer.parseInt(channel);

                    int control = 1;
                    DBswitchsetting setting = DBswitchsetting.getSwitchSetting(channelId);
                    if(setting != null){
                        control = setting.getControl();
                    }

                    if (control == 1) {
                        for(int i = 0; i<3; i++) {
                            SerialThread.CmdQueue(SerialThread.CTR_ON_RELAY, channelId, 0);
                        }
                    }

                    Breaker breaker = APP.distributbox.Breakers.get(channelId);
                    if (breaker != null && breaker.title.length()>0) {
                        String name = breaker.title;
                        if(breaker.localLock) {
                            if(name1s.length() == 0) {
                                name1s = name;
                            } else
                                name1s = name1s+","+name;
                        }else if(!breaker.EnableNetCtrl) {
                            if(name2s.length() == 0) {
                                name2s = name;
                            } else
                                name2s = name2s+","+name;
                        }else if(control == 0) {
                            if(name3s.length() == 0) {
                                name3s = name;
                            } else
                                name3s = name3s+","+name;
                        }else if(breaker.remoteLock) {
                            if(name4s.length() == 0) {
                                name4s = name;
                            } else
                                name4s = name4s+","+name;
                        }
                    }
                }

                //分闸
                for(String channel : keys2) {
                    int channelId = Integer.parseInt(channel);

                    int control = 1;
                    DBswitchsetting setting = DBswitchsetting.getSwitchSetting(channelId);
                    if(setting != null){
                        control = setting.getControl();
                    }

                    if (control == 1) {
                        for(int i = 0; i<3; i++) {
                            SerialThread.CmdQueue(SerialThread.CTR_OFF_RELAY, channelId, 0);
                        }
                    }

                    Breaker breaker = APP.distributbox.Breakers.get(channelId);
                    if (breaker != null && breaker.title.length()>0) {
                        String name = breaker.title;
                        if(breaker.localLock) {
                            if(name1s.length() == 0) {
                                name1s = name;
                            } else
                                name1s = name1s+","+name;
                        }else if(control == 0) {
                            if(name3s.length() == 0) {
                                name3s = name;
                            } else
                                name3s = name3s+","+name;
                        }
                    }
                }

                String message = "";
                if(name1s.length()>0) {
                    message = name1s+"已被硬件锁定，请现场手动解除硬件锁定后再操作！";
                }
                if(name2s.length()>0) {
                    message = message+(message.length()>0?"\n":"")+name2s+"已因用电报警断电或现场关断，遥控功能关闭。请现场手动送电或远程解锁后恢复遥控功能！";
                }
                if(name3s.length()>0) {
                    message = message+(message.length()>0?"\n":"")+name3s+"已设置为不能遥控，请设置为能遥控再操作！";
                }
                if(name4s.length()>0) {
                    message = message+(message.length()>0?"\n":"")+name4s+"已被分闸锁定，请解除分闸锁定后再操作！";
                }
                if(message.length()>0 && APP.language.equals("zh")) {
                    Toast.makeText(ActivityScene.this, message, Toast.LENGTH_LONG).show();
                }
            }
        })
        .setNeutralButton(resources.getString(R.string.tig17), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();

                int sceneId = (int)data.getAutoid();
                showDelayedPicker(3, sceneId);
            }
        })
                .setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener()
        {
             public void onClick(DialogInterface dialog, int id)
             {
                 dialog.cancel();
             }
        }).show();

        final int message = this.getResources().getIdentifier("message","id","android") ;
        TextView messageTextView = (TextView) alert.findViewById(message);
        messageTextView.setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：删除按钮事件
    public void deleteAction(View v) {
        final SceneData data = (SceneData) v.getTag();

        AlertDialog alert = new MyAlertDialog(this)
        .setMessage(resources.getString(R.string.alert31))
        .setTitle(resources.getString(R.string.tig7))
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                DBScene.delete(data.getAutoid());
                DBSceneTask.deletes(data.getAutoid());
                DBDelayedTask.deleteWhereSceneId((int)data.getAutoid());

                adapter.datas.remove(data);
                adapter.notifyDataSetChanged();

                if (adapter.datas.size() == 0) noSceneView.setVisibility(View.VISIBLE);

                dialog.cancel();
            }
        })
        .setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        }).show();

        final int message = this.getResources().getIdentifier("message","id","android") ;
        TextView messageTextView = (TextView) alert.findViewById(message);
        messageTextView.setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
    }

    private void showDelayedPicker(final int type, final int sceneId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pickerview = inflater.inflate(R.layout.delayedpicker, null);
        final DelayedPicker picker = new DelayedPicker(pickerview);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        picker.screenheight = metric.heightPixels;
        picker.initPicker(0, 1, 0);

        AlertDialog alert = new MyAlertDialog(this)
        .setTitle(resources.getString(R.string.alert39))
        .setView(pickerview)
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.tig5), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                int hour = picker.getHour();
                int minute = picker.getMin();
                int second = picker.getSecond();

                if (hour == 0 && minute == 0 && second == 0) {
                    Toast.makeText(ActivityScene.this, resources.getString(R.string.toast31), Toast.LENGTH_LONG).show();
                }else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.HOUR_OF_DAY, hour);
                    calendar.add(Calendar.MINUTE, minute);
                    calendar.add(Calendar.SECOND, second);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = format.format(calendar.getTime());

                    DBDelayedTask.insert(type, sceneId, time);
                }
            }
        })
        .setNegativeButton(resources.getString(R.string.tig6), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
    }

}
