package snd.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityQrCode extends ActivityBase {

	private ImageView imageView;
    private TextView numberView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentPage = intent.getIntExtra("currentPage", 0);
	    
	    setContentView(R.layout.code);
	    
	    imageView = (ImageView) findViewById(R.id.code);
        numberView = (TextView) findViewById(R.id.number);

        numberView.setText(APP.LoginId);
	    
	    final String filePath = APP.sdcard+"/QrCode.jpg";
	    File file = new File(filePath);
	    if (file.exists()) {
	    	imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
	    }else {
	    	
	    	new Thread(new Runnable() {
                @Override
                public void run() {
                	DisplayMetrics metric = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metric);
                    int width = metric.heightPixels/2;
                    boolean success = createQRImage(APP.MAC, width, width, filePath);

                    if (success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                            }
                        });
                    }
                }
            }).start();
	    	
	    }
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
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
	//方法功能：生成二维码及保存文件是否成功
	private boolean createQRImage(String content, int widthPix, int heightPix, String filePath) {
        try {
            if (content == null || "".equals(content)) {
                return false;
            }

            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); //容错级别

            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
	
}
