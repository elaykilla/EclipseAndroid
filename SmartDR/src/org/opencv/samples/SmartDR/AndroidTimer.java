package org.opencv.samples.SmartDR;

import android.util.Log;

public class AndroidTimer {
	private long start;
	private long stop;
	private String message;
	private String m_tag;

	/* �R���X�g���N�^ */
	AndroidTimer() {
	};

	/* �v���J�n�n�_ */
	public void PushTask(String tag, String task) {
		message = task;
		m_tag = tag;
		start = System.currentTimeMillis();
	}

	/* �v���I���n�_ */
	public void PopTask() {
		stop = System.currentTimeMillis();
		Log.d(m_tag, message + " : " + (stop - start) + " msec");
	}

}
