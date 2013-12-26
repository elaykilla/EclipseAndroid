package org.opencv.samples.SmartDR;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

class Sample3View extends SampleViewBase {

    AndroidTimer timer;

    public Sample3View(Context context) {
        super(context);
        timer = new AndroidTimer();
        sensorData = new double[3];
    }


    @Override
    protected Bitmap processFrame(byte[] data) {
        int frameSize = getFrameWidth() * getFrameHeight();
        int[] rgba = new int[frameSize];

        FindFeatures(getFrameWidth(), getFrameHeight(), data, rgba);

        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        return bmp;
    }

    protected Bitmap processFrame2(byte[] data) {
//        int frameSize = getFrameWidth() * getFrameHeight();
//        int[] rgba = new int[frameSize];
//
//            for (int i = 0; i < getFrameHeight(); i++)
//                for (int j = 0; j < getFrameWidth(); j++) {
//                    int y = (0xff & ((int) data[i * getFrameWidth() + j]));
//                    int u = (0xff & ((int) data[frameSize + (i >> 1) * getFrameWidth() + (j & ~1) + 0]));
//                    int v = (0xff & ((int) data[frameSize + (i >> 1) * getFrameWidth() + (j & ~1) + 1]));
//                    y = y < 16 ? 16 : y;
//
//                    int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
//                    int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
//                    int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
//
//                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
//                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
//                    b = b < 0 ? 0 : (b > 255 ? 255 : b);
//
//                    rgba[i * getFrameWidth() + j] = 0xff000000 + (b << 16) + (g << 8) + r;
//                }

    	int[] rgba = Byte2Int(data, getFrameWidth(), getFrameHeight());

        Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(), Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        return bmp;
    }

    protected Bitmap processFrame3(byte[] data, int minx, int miny, int maxx, int maxy, Bitmap bmp) {
    Bitmap bmp2 = bmp.copy(Bitmap.Config.ARGB_8888, false);
    Mat img = new Mat();;
    Utils.bitmapToMat(bmp2, img);			//bitmapToMat縺ｯARGB_8888縺倥ｃ縺ｪ縺�→縺�ａ��
    //Mat img = Utils.bitmapToMat(bmp2);
    Mat res = new Mat();

  	Byte2Mat(data, getFrameWidth(), getFrameHeight(), getWindowWidth(), getWindowHeight(), minx, miny, maxx, maxy, img.getNativeObjAddr(), res.getNativeObjAddr());

  	Bitmap dst = Bitmap.createBitmap(getWindowWidth(), getWindowHeight(), Bitmap.Config.ARGB_8888);
  	Utils.matToBitmap(res, dst);
     return dst;
  }

    protected byte[] YUV2JpgArray(byte[] data, int ratio){
        return Yuv2Jpg(ratio, getFrameWidth(), getFrameHeight(), data);
    }

    public native void FindFeatures(int width, int height, byte yuv[], int[] rgba);
    public native byte[] Yuv2Jpg(int ratio, int width, int height, byte yuv[]);
    public native int[] Byte2Int(byte byt[], int width, int height);
    public native int[] Byte2Mat(byte byt[], int width, int height, int exwidth, int exheight, int minx, int miny, int maxx, int maxy, long addrimg, long addrres);

    public void setSensorValue(float arg0, float arg1, float arg2){
    	sensorData[0] = arg0;
    	sensorData[1] = arg1;
    	sensorData[2] = arg2;
    }

    static {
        System.loadLibrary("mixed_sample");
    }
}
