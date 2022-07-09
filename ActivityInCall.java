package snd.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import seasnake.browser.Browser;
import seasnake.loger.Logger;
import snd.database.DBconfig;
import snd.serialservice.SerialDoorTalkThread;
import snd.util.AudioRecordThread;
import snd.util.AudioTrackThread;
import snd.util.OnAudioRecordListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ActivityInCall extends ActivityBase implements OnClickListener,Callback,PreviewCallback,OnAudioRecordListener
{
	private static Logger log = Logger.getLogger(ActivityInCall.class);
	
	Chronometer elapsedTime;
	SurfaceView surface_camera;
	ImageButton endButton;
	LinearLayout end_call_bar;
	
	SurfaceHolder surfaceHolder;
	Camera camera;
	MediaPlayer mediaPlayer; 
	AudioRecordThread recAudio = new AudioRecordThread();
	AudioTrackThread  playAudio = new AudioTrackThread();
	int talkStatus = 0;
	boolean isPreview = true;
	boolean isFirst = true;
	int takeCaptureTime = 0;
	
	ExecutorService executors = Executors.newFixedThreadPool(1);
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

        setContentView(R.layout.in_call_card);

        end_call_bar = (LinearLayout)this.findViewById(R.id.end_call_bar);
        elapsedTime = (Chronometer)this.findViewById(R.id.elapsedTime);
        surface_camera = (SurfaceView)this.findViewById(R.id.surface_camera);
        endButton = (ImageButton)this.findViewById(R.id.endButton);
        endButton.setOnClickListener(this);
        
        surfaceHolder = surface_camera.getHolder();   
        surfaceHolder.addCallback(this);   
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        elapsedTime.setTag(30);
        elapsedTime.setOnChronometerTickListener(tickListener);
        
		try 
		{
			String ring = DBconfig.GetConfig("DOORTALK", "RINGFILE", "");
			if(ring.equals(""))
			{
				mediaPlayer = MediaPlayer.create(this, R.raw.amour);
				mediaPlayer.setLooping(true);
			}
			else
			{
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setLooping(true);
				mediaPlayer.setDataSource(ring);
			}
			mediaPlayer.setVolume(1, 1);
			mediaPlayer.prepare();
		} 
		catch (Exception e) 
		{
			log.error( e);
		}
		
        recAudio.setOnAudioRecordListener(this);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		elapsedTime.start();
		this.recAudio.StartRecord();
		this.playAudio.StartPlay();
		mediaPlayer.start();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		elapsedTime.stop();
		recAudio.StopRecord();
		playAudio.StopPlay();

		if(surfaceHolder != null) {
			surfaceHolder.removeCallback(this);
			surfaceHolder.getSurface().release();
		}

		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;

			System.gc();
		}
	}
	
	@Override
	public void OnAudioRecord(byte[] data, int len) 
	{
		//即录即播
		playAudio.play(data);
	}
	
    /***************Callback***********************/
    @Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
    	int cameras = Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for(int i=0; i<cameras; i++)
		{
			Camera.getCameraInfo(i, info);
			if(info.facing == CameraInfo.CAMERA_FACING_FRONT)
			{
				try
				{
				    camera = Camera.open(i);
				}
				catch (Exception e) 
				{
					//Loger.Log(TAG, e);
					if(camera != null) camera.release();//释放资源
					camera = null;
				}
				break;
			}
		}
		
		try 
		{
			//没有前置摄像头
			if(camera == null) camera = Camera.open();
			camera.stopPreview();
        	//camera.setDisplayOrientation(-90);
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(this);
		} 
		catch (Exception e) 
		{
			if(camera != null) camera.release();//释放资源
			camera = null;
		}
	}

    @Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) 
	{
    	if(camera != null && isPreview)
    	{
    		Camera.Parameters parameters = camera.getParameters(); //得到相机设置参数
    		parameters.setPictureFormat(PixelFormat.JPEG); //设置图片格式

			//设置预览大小
			WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			Point screenResolution = new Point(display.getWidth(), display.getHeight());
			final Point cameraResolution = getCameraResolution(parameters, screenResolution);
			parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

			//surface_camera.getLayoutParams().width = cameraResolution.x;
			//surface_camera.getLayoutParams().height = cameraResolution.y;

			//setFlash(parameters);
			//setZoom(parameters);

			camera.setParameters(parameters);
    	    camera.setPreviewCallback(this);
    		camera.startPreview();//开始预览
			isPreview = false;

			ViewTreeObserver viewTreeObserver = surface_camera.getViewTreeObserver();
			viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
				@Override
				public boolean onPreDraw(){
					if (isFirst){
						isFirst = false;
						int height = surface_camera.getMeasuredHeight();
						int width = surface_camera.getMeasuredWidth();
						float r = (float)height/(float)width;
						float r2 = (float)cameraResolution.y/(float)cameraResolution.x;
						if (r > r2){
							surface_camera.getLayoutParams().height = (int) (width*r2);
						}else{
							surface_camera.getLayoutParams().width = (int) (height/r2);
						}
					}
					return true;
				}
			});
    	}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		if(camera != null)
		{
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	@Override
	public void onPreviewFrame(final byte[] data, final Camera camera) 
	{
		int x = Integer.parseInt(elapsedTime.getTag().toString());
		if(talkStatus==0 && (x==25 || x==20) && takeCaptureTime != x) //呼叫后5,10秒拍照
		{
			takeCaptureTime = x;
			Size size = camera.getParameters().getPreviewSize();
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
	    	ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
	        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, os);

	        FileOutputStream fos = null;
			try 
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String time = sdf.format(System.currentTimeMillis());

				String path = APP.path + "/photo";
				File file = new File(path);
				if(!file.exists()) file.mkdir();

				File f = new File(path+"/"+APP.MAC+"-"+x+"-"+time+".bmp");
				if(!f.exists()) f.createNewFile();

				fos = new FileOutputStream(f);
			    fos.write(os.toByteArray());
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if(fos != null) try{fos.close();}catch(Exception e){}
			}
		}

		/*if (data == null) {
			Camera.Parameters params = camera.getParameters();
			Size size = params.getPreviewSize();
			int bufferSize = (((size.width|0x1f)+1) * size.height * ImageFormat.getBitsPerPixel(params.getPreviewFormat())) / 8;
			camera.addCallbackBuffer(new byte[bufferSize]);
		}else {
			camera.addCallbackBuffer(data);
		}*/
	}
	/***************Callback***********************/
	
	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		    case R.id.endButton:
		    {
		    	if(talkStatus == 0)
		    	{
		    		this.mediaPlayer.stop();
		    		elapsedTime.setTag(60);
		    		talkStatus=1;

		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CALL, 0);
		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CALL, 0);
		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CALL, 0);

		    		end_call_bar.setBackgroundResource(R.drawable.end_call_background);
		    		endButton.setImageResource(R.drawable.ic_jog_dial_decline);
		    	}
		    	else
		    	{
		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CANCEL, 0);
		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CANCEL, 0);
		    		SerialDoorTalkThread.CmdQueue(SerialDoorTalkThread.CTR_CANCEL, 0);

		    		this.startActivity(new Intent(APP.activity, ActivitySleep.class));
					this.finish();
		    	}
		    }
		    break;
		}
	}

	/*根据相机的参数和手机屏幕参数来重新设置相机的*/
	private Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
		String previewSizeValueString = parameters.get("preview-size-values");
		// saw this on Xperia
		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}

		Point cameraResolution = null;

		if (previewSizeValueString != null) {
			cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
		}

		if (cameraResolution == null) {
			// Ensure that the camera resolution is a multiple of 8, as the screen may not be.
			cameraResolution = new Point(
					(screenResolution.x >> 3) << 3,
					(screenResolution.y >> 3) << 3);
		}

		return cameraResolution;
	}

	private Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;
		Pattern COMMA_PATTERN = Pattern.compile(",");
		for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

			previewSize = previewSize.trim();
			int dimPosition = previewSize.indexOf('x');
			if (dimPosition < 0) {
				continue;
			}

			int newX;
			int newY;
			try {
				newX = Integer.parseInt(previewSize.substring(0, dimPosition));
				newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
			} catch (NumberFormatException nfe) {
				continue;
			}

			int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
			if (newDiff == 0) {
				bestX = newX;
				bestY = newY;
				break;
			} else if (newDiff < diff) {
				bestX = newX;
				bestY = newY;
				diff = newDiff;
			}

		}

		if (bestX > 0 && bestY > 0) {
			return new Point(bestX, bestY);
		}
		return null;
	}

	private int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
		int tenBestValue = 0;
		Pattern COMMA_PATTERN = Pattern.compile(",");
		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			stringValue = stringValue.trim();
			double value;
			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}

	/*设置闪光灯，本来是不用设置的，应该是直接获取，但是根据三星建议说防止黑客攻击，最好设置一下*/
	private void setFlash(Camera.Parameters parameters) {
		// FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
		int sdkInt = 10000;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {}

		if (Build.MODEL.contains("Behold II") && sdkInt == 3) { // 3 = Cupcake
			parameters.set("flash-value", 1);
		} else {
			parameters.set("flash-value", 2);
		}
		// This is the standard setting to turn the flash off that all devices should honor.
		parameters.set("flash-mode", "off");
	}

	/*设置缩放*/
	private void setZoom(Camera.Parameters parameters) {
		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = 27;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) { }
		}

		String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null) {
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException nfe) {
				// continue
			}
		}

		// Set zoom. This helps encourage the user to pull back.
		// Some devices like the Behold have a zoom parameter
		if (maxZoomString != null || motZoomValuesString != null) {
			parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		}

		// Most devices, like the Hero, appear to expose this zoom parameter.
		// It takes on values like "27" which appears to mean 2.7x zoom
		if (takingPictureZoomMaxString != null) {
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}

	OnChronometerTickListener tickListener = new OnChronometerTickListener() {
		@Override
		public void onChronometerTick(Chronometer chronometer)
		{
			int x=Integer.parseInt(chronometer.getTag().toString())-1;
			chronometer.setTag(x);
			chronometer.setText( (x/60)+":"+(x%60) );

			if(x==0)
			{
				final File file1 = new File (APP.path+"/photo/"+APP.MAC+"20.bmp");
				final File file2 = new File (APP.path+"/photo/"+APP.MAC+"25.bmp");
				if(talkStatus==0 && file1.exists() && file2.exists())
				{
					executors.execute(new Runnable()
					{
						@Override
						public void run()
						{
							/*try
							{
								log.info("上传门口机呼叫照片");
								MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
								entity.addPart("mac",new StringBody(APP.MAC));
								entity.addPart("image", new FileBody(file1));
								entity.addPart("image", new FileBody(file2));

								HttpPost post = new HttpPost(APP.Server+"/upload.action");
								post.setEntity(entity);

								Browser browser = new Browser();
								browser.doPost(post);
							}
							catch (IOException e)
							{
								log.error(e);
							}*/
						}
					});
				}

				ActivityInCall.this.startActivity(new Intent(APP.activity, ActivitySleep.class));
				ActivityInCall.this.finish();
			}
		}
	};

}
