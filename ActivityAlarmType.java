package snd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import snd.adapter.AlarmTypeAdapter;
import snd.util.Tooles;

public class ActivityAlarmType extends ActivityBase {

    private ListView listView;
    private AlarmTypeAdapter adapter;

    public String[] datas = {"短路报警", "漏电报警", "过载报警", "过流报警", "过压报警", "欠压报警", "温度报警", "浪涌报警"
            , "漏电保护功能正常", "漏电保护自检未完成", "打火报警", "漏电预警", "电流预警", "过压预警", "欠压预警", "温度预警"
            , "恶性负载报警", "通讯报警", "缺相报警", "三相负载不平衡报警", "三相相序报警", "分闸警示", "合闸警示", "异常分闸"};
    public HashMap<String,String> selectDatas = new HashMap<String,String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.alarmtype);

        listView = (ListView)findViewById(R.id.list);

        String numbers = Tooles.getAlarTypeSetting(this);
        if(numbers != null && numbers.length()>0) {
            String[] array = numbers.split(",");
            for (int i = 0; i < array.length; i++) {
                String number = array[i];
                if (number != null && number.length() > 0)
                    selectDatas.put(number, number);
            }
        }

        adapter = new AlarmTypeAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        selectDatas.clear();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：返回按钮事件
    public void backAction(View v) {
        Intent intent = new Intent(this, ActivityAlarmSetting.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：选中事件
    public void itemClickAction(View v) {
        int position = (Integer) v.getTag();
        if(position > (datas.length-1)) return;

        String alarm = datas[position];
        String number = Tooles.getAlarmNumber(alarm)+"";

        boolean state = !v.isSelected();
        v.setSelected(state);

        if (state) { //选择
            selectDatas.remove(number);
        }else { //取消选择
            selectDatas.put(number, number);
        }

        Set<String> keySet = selectDatas.keySet();
        List<String> keys = new ArrayList<String>(keySet);
        Collections.sort(keys, new Comparator<String>(){
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
        String numbers = "";
        for(String key : keys) {
            if(numbers.length()==0)
                numbers = key;
            else
                numbers = numbers+","+key;
        }

        Tooles.saveAlarmTypeSetting(numbers, this);
    }

}
