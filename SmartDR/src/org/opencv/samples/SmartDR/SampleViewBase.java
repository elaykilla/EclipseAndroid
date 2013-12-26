package org.opencv.samples.SmartDR;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public abstract class SampleViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private static final String TAG = "SurfaceView";

	private Camera              mCamera;
	private SurfaceHolder       mHolder;
	private int                 mFrameWidth;	//Resolution of the sending/receved image
	private int                 mFrameHeight;
	private int                 mWindowWidth;	//Resolution of the smartphone display
	private int                 mWindowHeight;
	private byte[]              mFrame;
	private boolean             mThreadRun;
	private Connect 			c;
	AndroidTimer 				timer;
	private byte[] 				result;
	private Rect 				rect1, rect2;
	private int 				ratio;
	private int					mChannel;
	protected double[] 			sensorData;

	private Paint 				paint = new Paint();
	private Paint 				paint2 = new Paint();
	private float 				posx, posy;
	private float				minx, miny, maxx, maxy;
	private Path 				path;
	private  boolean			fingerUp = false;
	private  boolean			changeable = true;
	private  boolean			changeableChange = true;
	private Bitmap 				disp;

	private FpsMeter            mFps;				//Measure FPS

	public SampleViewBase(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		timer = new AndroidTimer();
		ratio = 90;
		mChannel = 3;
		sensorData = new double[3];

		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.RED);
		paint2.setStyle(Paint.Style.STROKE);
		paint2.setStrokeWidth(2);
		paint2.setStrokeCap(Paint.Cap.ROUND);
		paint2.setStrokeJoin(Paint.Join.ROUND);


		mFps = new FpsMeter();


		try {
			//IPv4 address
			c = new Connect("192.168.11.2", 50001);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Connect", "Port or IP Addless is not correct.");
		}

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	public int getFrameWidth() {
		return mFrameWidth;
	}
	public int getFrameHeight() {
		return mFrameHeight;
	}
	public int getWindowWidth() {
		return mWindowWidth;
	}
	public int getWindowHeight() {
		return mWindowHeight;
	}

	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
		Log.i(TAG, "surfaceCreated");
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();

			//Choose from available resolution
//			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
//			Camera.Size size = sizes.get(3);
//			mFrameWidth = (int) size.width;
//			mFrameHeight = (int) size.height;
//			Log.d("TEST2", "width = " + mFrameWidth);
//			Log.d("TEST2", "height = " + mFrameHeight);

			//Set resolution directly
			mFrameWidth = 320;
			mFrameHeight = 240;
			mWindowWidth = 854;
			mWindowHeight = 480;
			disp = Bitmap.createBitmap(getWindowWidth(), getWindowHeight(), Bitmap.Config.ARGB_8888);

			//Set focus mode
			//params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);	//hyperfocal distance

			//Set white balance
			//List<String> white = params.getSupportedWhiteBalance();
			//params.setWhiteBalance("fluorescent");
			//params.setWhiteBalance("cloudy-daylight");
			//params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
			//params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

			params.setPreviewSize(getFrameWidth(), getFrameHeight());
			rect1 = new Rect(0, 0, getFrameWidth(), getFrameHeight());
			rect2 = new Rect(0, 0, getWindowWidth(), getWindowHeight());
			result = new byte[getFrameWidth() * getFrameHeight() * mChannel];
			sendImageProp(mChannel);
			mCamera.setParameters(params);
			mCamera.startPreview();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "skurfaceCreated");
		mCamera = Camera.open();
		mCamera.setPreviewCallback(new PreviewCallback() {
			public void onPreviewFrame(byte[] data, Camera camera) {
				synchronized (SampleViewBase.this) {
					mFrame = data;
					//Log.i("Time", "time= " + System.currentTimeMillis());
					SampleViewBase.this.notify();
				}
			}
		});
		(new Thread(this)).start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		c.close();
		mThreadRun = false;
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}

	protected abstract Bitmap processFrame(byte[] data);
	protected abstract Bitmap processFrame2(byte[] data);
	protected abstract Bitmap processFrame3(byte[] data, int minx, int miny, int maxx, int maxy, Bitmap bmp);
	protected abstract byte[] YUV2JpgArray(byte[] data, int ratio);

	public void setSensorValue(float arg0, float arg1, float arg2){
		sensorData[0] = arg0;
		sensorData[1] = arg1;
		sensorData[2] = arg2;
	}


	public void run() {
		mThreadRun = true;
		byte[] rgba = null;
		boolean flg = false;
		byte[] tmpByte = null;

		mFps.init();

		Log.i(TAG, "Starting processing thread");
		while (mThreadRun) {
			Bitmap bmp = null;

			mFps.measure();

			synchronized (this) {
				try {
					this.wait();
					if (flg == false){
						flg = true;
						Log.i("A", "Starting processing thread");
						rgba = YUV2JpgArray(mFrame, ratio);
					}

					tmpByte = mFrame;
					rgba = YUV2JpgArray(tmpByte, ratio);

					byte[] packet = int2byte(rgba.length);
					sendDoubleData(sensorData);
					c.sendMessage(packet);
					c.sendMessage(rgba);

					//Send value
//					int num = 30;
//					byte[] bytenum = int2byte(num);
//					packet = int2byte(bytenum.length);
//					c.sendMessage(packet);
//					c.sendMessage(bytenum);

					timer.PushTask("Timer", "time:");
					long start = System.currentTimeMillis();

					c.receive(packet, 4);

					int resultLength = byte2int(packet);

					c.receive(result, resultLength);
					long end = System.currentTimeMillis();
					long pass = end-start;
					Log.d("TEST2", "Time: " + pass);

					timer.PopTask();

					ByteArrayInputStream input = new ByteArrayInputStream(result);
					bmp = BitmapFactory.decodeStream(input);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			if (bmp != null) {
				Canvas canvas = mHolder.lockCanvas();
				if (canvas != null) {
					if (Sample3Native.Mode == Sample3Native.MODE_All){	//Draw received image as it is
						canvas.drawBitmap(bmp, rect1, rect2, null);

						mFps.draw(canvas, (canvas.getWidth() - bmp.getWidth()) / 2, 0);

						if (fingerUp == true) {
							fingerUp = false;
							path = new Path();
						}
					} else {											//Draw received image only the area specified by finger
						changeable = false;
						canvas.drawBitmap(processFrame2(tmpByte), rect1, rect2, null);

						if (fingerUp == true) {
							//1msec
							canvas.drawRect(minx, miny, maxx-1, maxy-1, paint2);

							int w = (int)maxx - (int)minx;
							int h = (int)maxy - (int)miny;

							Bitmap newImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							Canvas canvas2 = new Canvas(newImage);

							Canvas canvas3 = new Canvas(disp);

							canvas3.drawBitmap(bmp, rect1, rect2, null);
							canvas2.drawBitmap(disp, new Rect((int)minx, (int)miny, (int)maxx, (int)maxy), new Rect(0, 0, w, h), null);
							canvas.drawBitmap(newImage,(int)minx,(int)miny, null);
							newImage.recycle();
						} else if(path != null){
							canvas.drawPath(path, paint);
						}
						changeable = true;
					}
					mHolder.unlockCanvasAndPost(canvas);
				}
				bmp.recycle();
			}
		}
	}

	public boolean onTouchEvent(MotionEvent e){
		if (Sample3Native.Mode == Sample3Native.MODE_Part){
			posx = e.getX();
			posy = e.getY();
			switch(e.getAction()){
			case MotionEvent.ACTION_DOWN:
				if (changeable == false){
					changeableChange = false;
					break;
				}
				path = new Path();
				path.moveTo(posx , posy );
				minx = posx;
				miny = posy;
				maxx = posx;
				maxy = posy;
				fingerUp = false;
				break;
			case MotionEvent.ACTION_MOVE:
				if (changeableChange == false && changeable == true){
					path = new Path();
					path.moveTo(posx , posy );
					minx = posx;
					miny = posy;
					maxx = posx;
					maxy = posy;
					fingerUp = false;
					changeableChange = true;
					break;
				}
				path.lineTo(posx, posy);
				invalidate();
				if (posx > maxx) 		maxx = posx;
				else if (posx < minx) 	minx = posx;
				if (posy > maxy) 		maxy = posy;
				else if (posy < miny) 	miny = posy;
				break;
			case MotionEvent.ACTION_UP:
				path.lineTo(posx, posy);
				invalidate();
				if (posx > maxx) 		maxx = posx;
				else if (posx < minx) 	minx = posx;
				if (posy > maxy) 		maxy = posy;
				else if (posy < miny) 	miny = posy;
				if (maxx - minx < 10 || maxy - miny < 10){
					path = new Path();
					break;
				}
				fingerUp = true;
				break;
			default:
				break;
			}
		}

		return true;
	}

	private byte[] int2byte(int length) {
		byte[] packet = new byte[4];
		packet[0] = (byte) ((length >> 24) & 0xFF);
		packet[1] = (byte) ((length >> 16) & 0xFF);
		packet[2] = (byte) ((length >> 8) & 0xFF);
		packet[3] = (byte) (length & 0xFF);
		return packet;
	}

	private byte[] long2byte(long length) {
		byte[] packet = new byte[8];
		packet[0] = (byte) ((length >> 56) & 0xFF);
		packet[1] = (byte) ((length >> 48) & 0xFF);
		packet[2] = (byte) ((length >> 40) & 0xFF);
		packet[3] = (byte) ((length >> 32) & 0xFF);
		packet[4] = (byte) ((length >> 24) & 0xFF);
		packet[5] = (byte) ((length >> 16) & 0xFF);
		packet[6] = (byte) ((length >> 8) & 0xFF);
		packet[7] = (byte) (length & 0xFF);
		return packet;
	}

	private int byte2int(byte[] packet) {
		int result = 0;
		result += (packet[0] & 0xff) * 256 * 256 * 256;
		result += (packet[1] & 0xff) * 256 * 256;
		result += (packet[2] & 0xff) * 256;
		result += (packet[3] & 0xff);
		return result;
	}

	//	private long byte2long(byte[] packet) {
	//		long result = 0;
	//		result += (packet[0] & 0xff) * 72057594037927936L;
	//		result += (packet[1] & 0xff) * 281474976710656L;
	//		result += (packet[2] & 0xff) * 1099511627776L;
	//		result += (packet[3] & 0xff) * 4294967296L;
	//		result += (packet[4] & 0xff) * 16777216L;
	//		result += (packet[5] & 0xff) * 65536L;
	//		result += (packet[6] & 0xff) * 256L;
	//		result += (packet[7] & 0xff);
	//		return result;
	//	}

	private void sendDoubleData(double[] data){
		byte[] sendData = new byte[24];
		byte[] pan = long2byte(Double.doubleToLongBits(data[0]));
		byte[] tilt = long2byte(Double.doubleToLongBits(data[1]));
		byte[] roll = long2byte(Double.doubleToLongBits(data[2]));
		for (int i = 0; i < 8; i++){
			sendData[i] = pan[i];
			sendData[i + 8] = tilt[i];
			sendData[i + 16] = roll[i];
		}
		c.sendMessage(sendData);
	}

	private void sendImageProp(int channel){
		Log.i("test", "---------------- " + getFrameWidth());
		byte[] _width = int2byte(getFrameWidth());
		byte[] _height = int2byte(getFrameHeight());
		byte[] _ratio = int2byte(ratio);
		byte[] _channel = int2byte(channel);
		byte[] sendData = new byte[16];
		for (int i = 0; i < 4; i++){
			sendData[i] = _width[i];
			sendData[i+4] = _height[i];
			sendData[i+8] = _ratio[i];
			sendData[i+12] = _channel[i];
		}
		c.sendMessage(sendData);
	}
}