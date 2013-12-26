package org.opencv.samples.SmartDR;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.util.Log;

public class Connect {

	/* �����o�ϐ� */
	private String ipAddr;
	private int port;
	private Socket connection;
	private BufferedOutputStream out;
	private InputStream is;

	/* �R���X�g���N�^ */
	Connect(String ipAddr, int port) throws IOException {
		Log.d("Connect", "Constractor");
		this.ipAddr = ipAddr;
		this.port = port;
		init();
	}

	/* �T�[�o�֐ڑ� */
	private void init() throws IOException{
		Log.d("Connect", "init");
		try {
			connection = new Socket(ipAddr, port);
			out = new BufferedOutputStream(connection.getOutputStream());
			is = connection.getInputStream();
			//connection.setSoTimeout(5);
		} catch (UnknownHostException e) {
			Log.e("Connect", "init() : unknownHostException");
			e.printStackTrace();
		}
		Log.d("Connect", "init_finish");
	}

	/* �T�[�o�Ƀ��b�Z�[�W�𑗐M */
	public int sendMessage(byte[] data) {
		Log.d("Connect", "sendMessage");
		try {
			out.write(data);
			out.flush();
		} catch (IOException e) {
			Log.e("Connect", "sendMessage() : ioException");
			e.printStackTrace();
		}
		return data.length;
	}

	/* �T�[�o����̃��b�Z�[�W����M */
	public int receive(byte[] line, int byteSize) {
		Log.d("Connect", "receive");
		int recvSize = 0;
		int numrcv = 0;

		try {
			byte[] cline = new byte[byteSize];
			while (is.available() == 0) {
				continue;
			}
			for (; recvSize < byteSize;) {
				numrcv = is.read(cline, 0, byteSize - recvSize);
				System.arraycopy(cline, 0, line, recvSize, numrcv);
				recvSize += numrcv;

				/* �t�@�C���̏I���ɓ��B */
				if (numrcv == -1) {
					Log.e("Connect", "receive() : numrcv = -1");
					break;
				}
			}
			return 0;
		} catch (IOException e) {
			Log.e("Connect", "receive() : ioException");
			Arrays.fill(line, (byte) 0);
			e.printStackTrace();
			return -1;
		}
	}

	/* �T�[�o�Ƃ̐ڑ���؂� */
	public void close() {
		Log.d("Connect", "Close");
		try {
			is.close();
			out.close();
			connection.close();
		} catch (IOException e) {
			Log.e("Connect", "close() : ioException");
			e.printStackTrace();
		}
	}
}
