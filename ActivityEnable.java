package snd.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import snd.adapter.EnableAdapter;
import snd.broadcastreceiver.Second1BroadcastReceiver;
import snd.database.Breaker;
import snd.serialservice.SerialThread;
import snd.util.Tooles;

public class ActivityEnable extends ActivityBase {

    private int curchannel = 1;
    public int type = 1; //1:报警使能；2:脱扣使能

    private ListView listView;
    private EnableAdapter adapter;

    public ArrayList<String> datas;
    public int EnableAlarm0 = 0;
    public int EnableAlarm1 = 0;

    private BroadcastReceiver br;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        curchannel = intent.getIntExtra("curchannel", 1);
        type = intent.getIntExtra("type", 1);

        setContentView(R.layout.enable);

        TextView titleView = (TextView)findViewById(R.id.title);
        listView = (ListView)findViewById(R.id.list);

        titleView.setText((type == 1)?R.string.limit_text13:R.string.limit_text14);

        initData();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Second1BroadcastReceiver.Msg_1S)) {
                    Breaker breaker = APP.distributbox.Breakers.get(curchannel);
                    if (breaker != null && breaker.EnableAlarm >= 0) {
                        String EnableAlarm = ((type == 1)?breaker.EnableAlarm:breaker.EnableTrip)+"";
                        BigInteger number = new BigInteger(EnableAlarm);
                        String binary = Tooles.getBinaryByDecimal(number, 32);
                        if (binary.length() > 15) {
                            String num = binary.substring(0, 16);
                            EnableAlarm0 = Tooles.binaryToDecimal(num);
                        }
                        if (binary.length() > 31) {
                            String num = binary.substring(16, 32);
                            EnableAlarm1 = Tooles.binaryToDecimal(num);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        this.registerReceiver(br, new IntentFilter(Second1BroadcastReceiver.Msg_1S));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.unregisterReceiver(br);
    }

    private void initData() {
        Breaker breaker = APP.distributbox.Breakers.get(curchannel);
        if(breaker == null) return;

        if(breaker.TYPE == 1) { //1P开关
            String[] strs = new String[] {"短路报警", "浪涌报警", "打火报警", "温度预警", "温度报警", "电流预警", "过流报警", "过载报警", "欠压预警", "欠压报警"
                    , "过压预警", "过压报警"};
            datas = new ArrayList<String>(Arrays.asList(strs));
            if(breaker.VERSION >= 0x55) datas.add("恶性负载报警");
        }else if(breaker.TYPE == 2) { //2P开关
            String[] strs = new String[] {"短路报警", "浪涌报警", "打火报警", "温度预警", "温度报警", "漏电预警", "漏电报警", "电流预警", "过流报警", "过载报警"
                    , "欠压预警", "欠压报警", "过压预警", "过压报警"};
            datas = new ArrayList<String>(Arrays.asList(strs));
        }else { //3P/4P开关
            String[] strs = new String[] {"短路报警", "浪涌报警", "打火报警", "温度预警", "温度报警", "漏电预警", "漏电报警", "电流预警", "过流报警", "过载报警"
                    , "欠压预警", "欠压报警", "过压预警", "过压报警", "不平衡报警", "相序报警"};
            datas = new ArrayList<String>(Arrays.asList(strs));
            if (breaker.VERSION >= 0x59) {
                datas.add("电压缺相报警");
                datas.add("电流缺相报警");
            }else
                datas.add("缺相报警");
        }

        if (breaker.EnableAlarm >= 0) {
            String EnableAlarm = ((type == 1)?breaker.EnableAlarm:breaker.EnableTrip)+"";
            BigInteger number = new BigInteger(EnableAlarm);
            String binary = Tooles.getBinaryByDecimal(number, 32);
            if (binary.length() > 15) {
                String num = binary.substring(0, 16);
                EnableAlarm0 = Tooles.binaryToDecimal(num);
            }
            if (binary.length() > 31) {
                String num = binary.substring(16, 32);
                EnableAlarm1 = Tooles.binaryToDecimal(num);
            }
        }

        adapter = new EnableAdapter(this);
        listView.setAdapter(adapter);
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivityWattSeting.class);
        intent.putExtra("curchannel", curchannel);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选中事件
    public void itemClickAction(View v) {
        String name = (String) v.getTag();
        int enableType = getEnableType(name);
        int enable = getEnable(name);
        int value = getValue(name);
        if (enable >=0 && value > 0 && APP.distributbox.Breakers.containsKey(curchannel)) {
            if ((enable&value) == value) {
                enable = enable-value;
            }else
                enable = enable+value;

            if (enable >= 0) {
                int cmd_type = 0;
                if (type == 1) { //报警使能
                    cmd_type = (enableType == 0?SerialThread.CFG_ENABLEAlARM0:SerialThread.CFG_ENABLEAlARM1);
                }else { //脱扣使能
                    cmd_type = (enableType == 0?SerialThread.CFG_ENABLETRIP0:SerialThread.CFG_ENABLETRIP1);
                }

                SerialThread.CmdQueue(SerialThread.CTR_UNLOCK, curchannel, 0);
                for (int i=0; i<3; i++) {
                    SerialThread.CmdQueue(cmd_type, curchannel, enable);
                    SerialThread.CmdQueue(SerialThread.REQ_THRESHOLD, curchannel, 0);
                }
                SerialThread.CmdQueue(SerialThread.CTR_LOCK, curchannel, 0);
            }
        }
    }

    private int getEnableType(String name) {
        if (name.equals("恶性负载报警") || name.equals("温度报警") || name.equals("不平衡报警") || name.equals("相序报警") || name.equals("电流缺相报警")) {
            return 0;
        }else
            return 1;
    }

    private int getEnable(String name) {
        if (name.equals("恶性负载报警") || name.equals("温度报警") || name.equals("不平衡报警") || name.equals("相序报警") || name.equals("电流缺相报警")) {
            return EnableAlarm0;
        }else
            return EnableAlarm1;
    }

    private int getValue(String name) {
        if (name.equals("短路报警")) {
            return 0x01;
        }else if (name.equals("浪涌报警")) {
            return 0x02;
        }else if (name.equals("过载报警")) {
            return 0x04;
        }else if (name.equals("温度预警")) {
            return 0x08;
        }else if (name.equals("漏电报警")) {
            return 0x10;
        }else if (name.equals("过流报警")) {
            return 0x20;
        }else if (name.equals("过压报警")) {
            return 0x40;
        }else if (name.equals("缺相报警")) {
            return 0x200;
        }else if (name.equals("电压缺相报警")) {
            return 0x200;
        }else if (name.equals("打火报警")) {
            return 0x400;
        }else if (name.equals("欠压报警")) {
            return 0x800;
        }else if (name.equals("过压预警")) {
            return 0x1000;
        }else if (name.equals("欠压预警")) {
            return 0x2000;
        }else if (name.equals("漏电预警")) {
            return 0x4000;
        }else if (name.equals("电流预警")) {
            return 0x8000;
        }else if (name.equals("恶性负载报警")) {
            return 0x04;
        }else if (name.equals("温度报警")) {
            return 0x20;
        }else if (name.equals("不平衡报警")) {
            return 0x40;
        }else if (name.equals("相序报警")) {
            return 0x80;
        }else if (name.equals("电流缺相报警")) {
            return 0x200;
        }

        return 0;
    }

}
