package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.stream.Stream;

//Arduino client Thread
public class ArduinoThread extends Thread {

	private Socket client = null;
	// input port
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private InetAddress ia;
	private String clientIp;
	private long preTime;
	private long connectionCheckInterval = 5000;
	private DataClass dataClass = DataClass.getInstance();
	private boolean close = false;

	public ArduinoThread(Socket socket) {
		this.client = socket;
	}

	String getIp() {
		return clientIp;
	}

	// Thread ����
	void closeThread() {
		this.close = true;
	}
	// �ʱ�ȭ �۾�
	void init() {
		try {
			ia = client.getInetAddress();
			clientIp = ia.getHostAddress();
			dataInputStream = new DataInputStream(client.getInputStream());
			dataOutputStream = new DataOutputStream(client.getOutputStream());
			preTime = System.currentTimeMillis();
		} catch (Exception e) {
		}
	}

	// ������ ����
	String readData() {
		try {
			byte[] buffer = new byte[20];
			dataInputStream.read(buffer);
			String recv = new String(buffer);
			return recv;
		} catch (Exception e) {
			System.out.println(clientIp + "Data Read Exception");
			return "";
		}
	}

	// ������ �۽�
	void sendData(String data) throws IOException {
		dataOutputStream.write(data.getBytes());
		preTime = System.currentTimeMillis();
	}

	//���� ���� ���� Ȯ��
	void checkConnection() throws Exception {
		// System.out.println(clientIp + " : �Ƶ��̳� check ����");
		dataOutputStream.write("ck".getBytes());
		preTime = System.currentTimeMillis();
	}

//����� ��� �Ƶ��̳뿡�� ������ ����
	void writeToArduino(String data) throws Exception {
		for (int i = 0; i < Server.arduinoList.size(); i++) {
			ArduinoThread arduino = Server.arduinoList.get(i);
			try {
				arduino.sendData(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Arduino " + arduino.getIp() + " ������ ���� ����");
				throw e;
			}
		}
	}

	// ����� ��� ����Ʈ������ ������ ����
	void writeToMobile(String data) throws Exception {
		for (int i = 0; i < Server.mobileList.size(); i++) {
			MobileThread mobile = Server.mobileList.get(i);
			try {
				mobile.sendData(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Smartphone " + mobile.getIp() + " ������ ���� ����");
				throw e;
			}
		}
	}

	@Override
	public void run() {
		try {
			init();

			while (client != null && !close) {
				
				if (dataInputStream.available() > 1) {
					
					String recv = readData().trim();
					System.out.println(recv);
					DataClass dataClass = DataClass.getInstance();
					switch (recv.charAt(0)) {
					//LAMP ���� ���� ��ȭ ������ ����
					case 'L': {
						String data =recv.substring(1, recv.length());
						dataClass.setLampStatus(data.toCharArray()); 
						//writeToMobile(data);
					}
						break;
					case 'D': {								
						System.out.println("recv : "+recv);
						dataClass.setDoorAngle(recv);
						//writeToMobile(data);
					}
						break;
					case 'W': {								
						System.out.println("recv : "+recv);
						dataClass.setWindowAngle(recv);
						//writeToMobile(data);
					}
						break;

					default:
						break;
					}
					preTime = System.currentTimeMillis();
				} else {
					if ((System.currentTimeMillis() - preTime) > connectionCheckInterval) {
						checkConnection();
					}
				}
			}

		} catch (Exception e) {
			System.out.println(clientIp +"Arduino ���� ���� ����");
			close = true;
		} finally {
			System.out.println(clientIp + " : Arduino ���� ����");
			CloseClass.closeSocket(client);
			CloseClass.closeInputStream(dataInputStream);
			CloseClass.closeOutStream(dataOutputStream);
		}
	}
}
