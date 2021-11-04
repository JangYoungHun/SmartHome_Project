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

	// Thread 종료
	void closeThread() {
		this.close = true;
	}
	// 초기화 작업
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

	// 데이터 수신
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

	// 데이터 송신
	void sendData(String data) throws IOException {
		dataOutputStream.write(data.getBytes());
		preTime = System.currentTimeMillis();
	}

	//소켓 연결 상태 확인
	void checkConnection() throws Exception {
		// System.out.println(clientIp + " : 아두이노 check 전송");
		dataOutputStream.write("ck".getBytes());
		preTime = System.currentTimeMillis();
	}

//연결된 모든 아두이노에게 데이터 전송
	void writeToArduino(String data) throws Exception {
		for (int i = 0; i < Server.arduinoList.size(); i++) {
			ArduinoThread arduino = Server.arduinoList.get(i);
			try {
				arduino.sendData(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Arduino " + arduino.getIp() + " 데이터 전송 에러");
				throw e;
			}
		}
	}

	// 연결된 모든 스마트폰에게 데이터 전송
	void writeToMobile(String data) throws Exception {
		for (int i = 0; i < Server.mobileList.size(); i++) {
			MobileThread mobile = Server.mobileList.get(i);
			try {
				mobile.sendData(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Smartphone " + mobile.getIp() + " 데이터 전송 에러");
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
					//LAMP 전등 상태 변화 데이터 수신
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
			System.out.println(clientIp +"Arduino 연결 상태 오류");
			close = true;
		} finally {
			System.out.println(clientIp + " : Arduino 소켓 종료");
			CloseClass.closeSocket(client);
			CloseClass.closeInputStream(dataInputStream);
			CloseClass.closeOutStream(dataOutputStream);
		}
	}
}
