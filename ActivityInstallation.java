package snd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActivityInstallation extends ActivityBase {

	private ViewPager mViewPager;
	private ViewGroup group;
	
	private int currentPage1;
	
	private int[] datas = { R.drawable.installation1, R.drawable.installation2 };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		currentPage = intent.getIntExtra("currentPage", 0);

		setContentView(R.layout.help);

		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		group = (ViewGroup) findViewById(R.id.viewGroup);
		
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(pageChangeListener);
		
		showPageCount(datas.length);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// 方法类型：自定义方法
	// 编 写：
	// 方法功能：返回按钮事件
	public void backAction(View v) {
		Intent intent = new Intent(this, ActivityMenu.class);
		intent.putExtra("currentPage", currentPage);
		this.startActivity(intent);
		this.finish();
	}
	
    //方法类型：自定义方法
  	//编   写：
  	//方法功能：显示页数的小圆点
    private void showPageCount(int count) {
    	if(group != null) {
    		group.removeAllViews();
    		
    		if(count == 1) {
    			return;
    		}
    		
    		if(currentPage1 > count-1) {
				currentPage1 = count-1;
    		}
    		
    		for (int i = 0; i<count; i++) {
                LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                //设置每个小圆点距离左边的间距
                margin.setMargins(10, 0, 0, 0);
                //设置每个小圆点的宽高
                DisplayMetrics dm = new DisplayMetrics();
    			getWindowManager().getDefaultDisplay().getMetrics(dm);
                margin.height = 10*dm.widthPixels/720;
                margin.width = 10*dm.widthPixels/720;
                ImageView imageView = new ImageView(this);
                if (i == currentPage1) { // 默认选中第一张图片
                	imageView.setBackgroundResource(R.drawable.icon_dot_select1);
                } else { //其他都设置未选中状态
                	imageView.setBackgroundResource(R.drawable.icon_dot_normal);
                }
                group.addView(imageView, margin);
            }
    	}
    }
	
	private PagerAdapter mPagerAdapter = new PagerAdapter() {

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return datas.length;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View)object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "";
		}

		@Override
		public Object instantiateItem(View container, int position) {
			ImageView view = new ImageView(ActivityInstallation.this);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			view.setScaleType(ImageView.ScaleType.FIT_CENTER);
			view.setImageResource(datas[position]);
			
			((ViewPager) container).addView(view);
			return view;
		}
	};
	
    private OnPageChangeListener pageChangeListener = new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub		

			currentPage1 = arg0;
			
			for (int i = 0; i < group.getChildCount(); i++) {
				ImageView imageView = (ImageView)group.getChildAt(i);
                if (arg0 == i) {
                    imageView.setBackgroundResource(R.drawable.icon_dot_select1);
                }else
                	imageView.setBackgroundResource(R.drawable.icon_dot_normal);
            }
		}
		
	};
	
}
