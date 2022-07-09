package snd.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cityselect.CharacterParser;
import com.cityselect.MyLetterAlistView;
import com.cityselect.PinyinComparator;
import com.cityselect.SortAdapter;
import com.cityselect.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import snd.database.DBconfig;

public class ActivitySelectCity extends ActivityBase {

    // 搜索内容
    private EditText editText;
    // 城市列表
    private ListView sortListView;
    // 右侧A-Z字母列表
    private MyLetterAlistView letterListView;
    // dialog text
    private TextView overlay;
    // 估计是弹出dialog线程
    private OverlayThread overlayThread;
    // 城市Adapter
    private SortAdapter adapter;
    private Handler handler;

    // 汉字转换成拼音的类
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    // 根据拼音来排列ListView里面的数据类
    private PinyinComparator pinyinComparator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.cityselectview);

        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        handler = new Handler();
        overlayThread = new OverlayThread();
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        //根据拼音排序
        pinyinComparator = new PinyinComparator();

        editText = (EditText) findViewById(R.id.editText);
        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        letterListView = (MyLetterAlistView) findViewById(R.id.cityLetterListView);
        SourceDateList = filledData(getResources().getStringArray(R.array.province));

        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
        initOverlay();

        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        sortListView.setOnItemClickListener(itemClickListener);

        // 根据输入框输入值的改变来过滤搜索
        editText.addTextChangedListener(textWatcher);
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：返回按钮事件
    public void backAction(View v) {
        try {
            WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(overlay);
        }catch(Exception e){}

        Intent intent = new Intent(this, ActivitySetting.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String city = ((SortModel) adapter.getItem(position)).getName();
            APP.SElE_CITY = city;
            DBconfig.UpdateConfig("DBOX","SElECITY", city);

            try {
                WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                windowManager.removeView(overlay);
            }catch(Exception e){}

            Intent intent = new Intent(ActivitySelectCity.this, ActivitySleep.class);
            ActivitySelectCity.this.startActivity(intent);
            ActivitySelectCity.this.finish();
        }

    };

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
            filterData(s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：右侧A-Z字母监听
    private class LetterListViewListener implements MyLetterAlistView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            // 该字母首次出现的位置
            int position = adapter.getPositionForSection(s.charAt(0));
            if (position != -1) {
                sortListView.setSelection(position);
                overlay.setText(SourceDateList.get(position).getSortLetters());
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1500);
            }
        }
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：初始化汉语拼音首字母弹出提示框
    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.city_overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：设置overlay不可见
    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }

    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledData(String[] date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        if (APP.CITY != null && APP.CITY.length() > 0) {
            String city = APP.CITY;
            city = city.replace("市", "");
            SortModel localModel = new SortModel();
            localModel.setName(city);
            localModel.setSortLetters("@");
            mSortList.add(localModel);
        }

        String[] hots = { "北京", "上海", "广州", "深圳", "武汉", "天津", "西安", "南京", "杭州",
                "成都", "重庆" };
        for (int i = 0; i < hots.length; i++) {
            SortModel hotModel = new SortModel();
            hotModel.setName(hots[i]);
            hotModel.setSortLetters("#");
            mSortList.add(hotModel);
        }

        for (int i = 0; i < date.length; i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(date[i]);
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }

        return mSortList;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1
                        || characterParser.getSelling(name).startsWith(
                        filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

}
