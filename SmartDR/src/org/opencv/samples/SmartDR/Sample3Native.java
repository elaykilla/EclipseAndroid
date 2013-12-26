package org.opencv.samples.SmartDR;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;



public class Sample3Native extends Activity implements SensorEventListener{
    private static final String TAG = "Sample::Activity";
    public static final int     MODE_All		= 0;
    public static final int     MODE_Part		= 1;

    private MenuItem            mItemAll;
    private MenuItem            mItemPart;

    public static int           Mode		= MODE_All;

    // �Z���T�p�ϐ�
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // setContentView�p�ϐ�
    private Sample3View view;

    public Sample3Native() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        view = new Sample3View(this);
  

        setContentView(view);

        //�Z���T�[�}�l�[�W���̎擾
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        //�Z���T�[�̎擾
        List<Sensor> list;
        list=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (list.size()>0) accelerometer=list.get(0);

    }

    //�A�v���̊J�n
    protected void onResume()
    {
       //�A�v���̊J�n
       super.onResume();
       //�Z���T�[�̏����̊J�n
       if (accelerometer!=null) sensorManager.registerListener(this,accelerometer,
    		   SensorManager.SENSOR_DELAY_FASTEST);
    }

    //�A�v���̒�~
    protected void onStop()
    {
       //�Z���T�[�̏����̒�~
       sensorManager.unregisterListener(this);
       //�A�v���̒�~
       super.onStop();
    }

    // ���x�ύX�C�x���g�̏���
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	// //�Z���T�[���X�i�[�̏���
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor == accelerometer){
			view.setSensorValue(event.values[0], event.values[1], event.values[2]);
		}
	}

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mItemAll = menu.add("All");
        mItemPart = menu.add("Part");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemAll)
            Mode = MODE_All;
        else if (item == mItemPart)
            Mode = MODE_Part;
        return true;
    }

	// �j��̍ۂɎ��
	public void onDestroy()
	{
		super.onDestroy();
	}
}



