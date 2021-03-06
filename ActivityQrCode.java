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
	
	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????
	public void backAction(View v) {
        Intent intent = new Intent(this, ActivityMenu.class);
        intent.putExtra("currentPage", currentPage);
        this.startActivity(intent);
        this.finish();
	}
	
	//??????????????????????????????
	//???   ??????
	//?????????????????????????????????????????????????????????
	private boolean createQRImage(String content, int widthPix, int heightPix, String filePath) {
        try {
            if (content == null || "".equals(content)) {
                return false;
            }

            //????????????
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); //????????????

            //??????????????????????????????????????????
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // ????????????????????????????????????????????????????????????????????????
            // ??????for????????????????????????????????????
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            // ???????????????????????????????????????ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            //????????????compress?????????bitmap???????????????????????????????????????????????????bitmap????????????????????????????????????????????????
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
	
}
