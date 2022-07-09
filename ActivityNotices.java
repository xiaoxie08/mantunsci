package snd.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import snd.adapter.NoticeAdapter;
import snd.database.DBNotice;
import snd.model.NoticeData;
import snd.view.MyAlertDialog;

public class ActivityNotices extends ActivityBase {

    private Resources resources;

    private LinearLayout bottomView;
    public Button editButton;
    private Button deleteButton;
    private Button selectButton;

    private ListView listView;
    private NoticeAdapter adapter;

    public HashMap<Long,NoticeData> curSelectDatas = new HashMap<Long,NoticeData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);

        setContentView(R.layout.notice_list);

        listView = (ListView) findViewById(R.id.list);

        editButton = (Button)findViewById(R.id.edit_button);
        bottomView = (LinearLayout)findViewById(R.id.bottom);
        deleteButton = (Button)findViewById(R.id.button_delete);
        selectButton = (Button)findViewById(R.id.button_select);

        String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
        String tig2 = resources.getString(R.string.tig2); //全选
        deleteButton.setText(tig1);
        selectButton.setText(tig2);

        adapter = new NoticeAdapter(this);
        listView.setAdapter(adapter);

        loadDatas();

        DBNotice.update();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        curSelectDatas.clear();
    }

    public void loadDatas() {
        List<NoticeData> datas = DBNotice.getDatas();
        adapter.datas = datas;
        adapter.notifyDataSetChanged();
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
    //方法功能：编辑按钮事件
    public void editAction(View v) {
        editButton.setSelected(!editButton.isSelected());
        if(editButton.isSelected()) {
            int color = 0x00000000;
            String tig = resources.getString(R.string.tig4); //取消
            editButton.setBackgroundColor(color);
            editButton.setText(tig);
            editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_height);

            bottomView.setVisibility(View.VISIBLE);
        }else {
            editButton.setBackgroundResource(R.drawable.icon_delete_normal);
            editButton.setText("");
            editButton.getLayoutParams().width = (int)resources.getDimension(R.dimen.navbar_button);

            curSelectDatas.clear();

            String tig1 = resources.getString(R.string.tig1)+"(0)"; //删除(0)
            String tig2 = resources.getString(R.string.tig2); //全选

            deleteButton.setText(tig1);
            selectButton.setText(tig2);
            selectButton.setSelected(false);
            bottomView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：全选或全不选按钮事件
    public void selectAction(View v) {
        if(adapter.datas == null) return;
        curSelectDatas.clear();

        selectButton.setSelected(!selectButton.isSelected());
        if(selectButton.isSelected()) {
            String tig = resources.getString(R.string.tig3); //全不选
            selectButton.setText(tig);

            for(NoticeData data : adapter.datas) {
                curSelectDatas.put(data.getAutoid(), data);
            }
        }else {
            String tig = resources.getString(R.string.tig2); //全选
            selectButton.setText(tig);
        }

        String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
        deleteButton.setText(tig);
        adapter.notifyDataSetChanged();
    }

    //方法类型：自定义方法
    //编   写：
    //方法功能：删除选择的通知按钮事件
    public void deleteAllAction(View v) {
        if(curSelectDatas.size()>0) {
            AlertDialog alert = new MyAlertDialog(this)
            .setMessage(resources.getString(R.string.alert32))
            .setTitle(resources.getString(R.string.tig7))
            .setPositiveButton(resources.getString(R.string.tig5),
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            for(NoticeData data : curSelectDatas.values()) {
                                DBNotice.delete(data.getAutoid());
                            }

                            curSelectDatas.clear();

                            String tig = resources.getString(R.string.tig1)+"(0)"; //删除(0)
                            deleteButton.setText(tig);

                            loadDatas();
                            Toast.makeText(ActivityNotices.this, resources.getString(R.string.toast1), Toast.LENGTH_SHORT).show();
                        }
                    }
            )
            .setNegativeButton(resources.getString(R.string.tig6),
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            ).show();

            int message = this.getResources().getIdentifier("message","id","android");
            TextView messageTextView = (TextView) alert.findViewById(message);
            messageTextView.setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(27);
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(27);
        }else {
            Toast.makeText(this, resources.getString(R.string.toast28), Toast.LENGTH_SHORT).show();
        }
    }

    // 方法类型：自定义方法
    // 编 写：
    // 方法功能：按钮事件
    public void itemClickAction(View v) {
        NoticeData data = (NoticeData) v.getTag();
        if (editButton.isSelected() && data != null) {
            if(!curSelectDatas.containsKey(data.getAutoid())) {
                curSelectDatas.put(data.getAutoid(), data);
            }else
                curSelectDatas.remove(data.getAutoid());

            String tig = resources.getString(R.string.tig1)+"("+curSelectDatas.size()+")"; //删除
            deleteButton.setText(tig);
            adapter.notifyDataSetChanged();
        }else if(data != null && data.getUrl().length()>0) {
            Intent intent = new Intent(this, ActivityNotice.class);
            intent.putExtra("currentPage", currentPage);
            intent.putExtra("title", data.getTitle());
            intent.putExtra("url", data.getUrl());
            this.startActivity(intent);
            this.finish();
        }
    }

}
