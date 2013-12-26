#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <android/log.h>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
	JNIEXPORT void JNICALL Java_org_opencv_samples_SmartDR_Sample3View_FindFeatures(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray rgba)
	{
		jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
		jint*  _rgba = env->GetIntArrayElements(rgba, 0);

		Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
		Mat mrgba(height, width, CV_8UC4, (unsigned char *)_rgba);
		Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

		cvtColor(myuv, mrgba, CV_YUV2BGR_NV12, 4);

		vector<KeyPoint> v;

		FastFeatureDetector detector(50);
		detector.detect(mgray, v);
		for( size_t i = 0; i < v.size(); i++ )
			circle(mrgba, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));

		env->ReleaseIntArrayElements(rgba, _rgba, 0);
		env->ReleaseByteArrayElements(yuv, _yuv, 0);
	}

}

extern "C" {
	JNIEXPORT jbyteArray JNICALL Java_org_opencv_samples_SmartDR_Sample3View_Yuv2Jpg(JNIEnv* env, jobject thiz, jint ratio, jint width, jint height, jbyteArray yuv)
	{
		//__android_log_print(ANDROID_LOG_DEBUG, "tag", "format");
		jbyte* _yuv = env->GetByteArrayElements(yuv, 0);

		Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
		Mat mrgb(height, width, CV_8UC3);

		// yuv420����rgb�ɕϊ�
		cvtColor(myuv, mrgb, CV_YUV2BGR_NV12, 3);

		// jpg�ϊ�
		static vector<uchar> uc;
		vector<int> param = vector<int>(2);
		param[0]=CV_IMWRITE_JPEG_QUALITY;
		param[1]=ratio;//default(95) 0-100
		imencode(".jpg", mrgb, uc, param);
		int size = uc.size();
		jbyteArray result = env->NewByteArray(size);
		jbyte* _result = env->GetByteArrayElements(result, NULL);

		int i;
		for (i = 0; i < size; i++){
			_result[i] = uc[i];
		}

		// java�ɖ߂�
		env->ReleaseByteArrayElements(yuv, _yuv, 0);
		env->ReleaseByteArrayElements(result, _result, 0);

		return result;
	}

}

Mat test(const Mat& img1)
{
	//Mat a;
	Mat a(Size(img1.cols, img1.rows), CV_8UC4);	//ブレンドした画像
	a = Scalar(255,0,0,255);
	//a = Mat::zeros(img1.rows, img1.cols, CV_32FC3);
	//a = Mat::eye(img1.rows, img1.cols, CV_32FC3);
	//a.setTo(Scalar(1,0,0));
	return a;
}

extern "C" {
	JNIEXPORT void JNICALL Java_org_opencv_samples_SmartDR_Sample3View_Byte2Mat(JNIEnv* env, jobject thiz, jbyteArray byt, jint width, jint height, jint exwidth, jint exheight, jint minx, jint miny, jint maxx, jint maxy, jlong addrimg, jlong addrres)
	{
		//__android_log_print(ANDROID_LOG_DEBUG, "tag", "format");
		jbyte* data = env->GetByteArrayElements(byt, 0);
		Mat* img=(Mat*)addrimg;
		Mat* res=(Mat*)addrres;

        int frameSize = width * height;
        Mat rgba(Size(width, height), CV_8UC4);
        Mat originalExp(Size(exwidth, exheight), CV_8UC4);
        Mat DRExp(Size(exwidth, exheight), CV_8UC4);

        uchar* rgbap;
        int ch = 4;
        for (int i = 0; i < height; i++){
        	rgbap = rgba.ptr(i);
        	for (int j = 0; j < width; j++) {
        		int y = (0xff & ((int) data[i * width + j]));
        		int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
        		int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
        		y = y < 16 ? 16 : y;

        		int r = (int)(1.164f * (y - 16) + 1.596f * (v - 128)+0.5);
        		int g = (int)(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128)+0.5);
        		int b = (int)(1.164f * (y - 16) + 2.018f * (u - 128)+0.5);

        		r = r < 0 ? 0 : (r > 255 ? 255 : r);
        		g = g < 0 ? 0 : (g > 255 ? 255 : g);
        		b = b < 0 ? 0 : (b > 255 ? 255 : b);

        		rgbap[j*ch] = b;
        		rgbap[j*ch+1] = g;
        		rgbap[j*ch+2] = r;
        		rgbap[j*ch+3] = 255;
        	}
        }

        resize(rgba, originalExp, originalExp.size());
        resize(*img, DRExp, DRExp.size());

        uchar* originalExpp;
        uchar* DRExpp;
        for (int i = miny; i < maxy; i++){
        	originalExpp = originalExp.ptr(i);
        	DRExpp = DRExp.ptr(i);
        	for (int j = minx; j < maxx; j++){
        		for (int k = 0; k < 4; k++){
        			originalExpp[j*ch+k] = (unsigned char)DRExpp[j*ch+k];
        		}
        	}
        }
        originalExp.copyTo(*res);
	}
}

extern "C" {
	JNIEXPORT jintArray JNICALL Java_org_opencv_samples_SmartDR_Sample3View_Byte2Int(JNIEnv* env, jobject thiz, jbyteArray byt, jint width, jint height)
	{
		//__android_log_print(ANDROID_LOG_DEBUG, "tag", "format");
		jbyte* data = env->GetByteArrayElements(byt, 0);

        int frameSize = width * height;
		jintArray rgba = env->NewIntArray(frameSize);
		jint* _rgba= env->GetIntArrayElements(rgba, NULL);


            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    int y = (0xff & ((int) data[i * width + j]));
                    int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                    int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                    y = y < 16 ? 16 : y;

                    int r = (int)(1.164f * (y - 16) + 1.596f * (v - 128)+0.5);
                    int g = (int)(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128)+0.5);
                    int b = (int)(1.164f * (y - 16) + 2.018f * (u - 128)+0.5);

                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
                    b = b < 0 ? 0 : (b > 255 ? 255 : b);

                    _rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
                }

		// java�ɖ߂�
		env->ReleaseByteArrayElements(byt, data, 0);
		env->ReleaseIntArrayElements(rgba, _rgba, 0);

		return rgba;
	}

}

