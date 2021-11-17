package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class FactoryThread extends Thread {
	final static int MAX_ARDUINO_NUM = 4;
	final static int MAX_MOBILE_NUM = 4;

	private Socket client;
	private DataOutputStream dataOutputStream ;
	private DataInputStream dataInputStream;

	public FactoryThread(Socket client) {
		try {
			this.client = client;
			dataOutputStream = new DataOutputStream(client.getOutputStream());
			dataInputStream = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			if (client == null)
				return;
			System.out.println("소켓 연결");
			InetAddress ia = client.getInetAddress();
			String clientIp = ia.getHostAddress(); // 접속 Client ip
			switch (whoIsClient()) {
			// 아두이노 접속
			case 0: {
				System.out.println("Arduino 접속");
				System.out.println("Arduino ip : " + clientIp);
				if (Server.arduinoList.size() < MAX_ARDUINO_NUM) {
					ArduinoThread arduino = new ArduinoThread(client);		
					arduino.start();
					Server.arduinoList.add(arduino);		

				} 
				else {
					System.out.println("최대 Arduino 기기 개수 초과 , 소켓 연결 해제");
					CloseClass.closeOutStream(	dataOutputStream);
					CloseClass.closeSocket(client);	
				}
			}
				break;
			// 스마트폰 접속
			case 1: {
				System.out.println("Smartphone 접속");
				System.out.println("Smartphone ip : " + clientIp);
				// 최대 접속개수 제한
				if (Server.mobileList.size() < MAX_MOBILE_NUM) {
					MobileThread mobile = new MobileThread(client);
					mobile.start();
					Server.mobileList.add(mobile);
				} else {
					System.out.println("최대 Smartphone 개수 초과 , 소켓 연결 해제");					
					CloseClass.closeOutStream(	dataOutputStream);
					CloseClass.closeSocket(client);	
				
				}
			}
				break;
			// 에러, 미허용된 접속자
			case -1: {
				System.out.println("허용되지 않은 접속, 소켓 연결 해제");		
				CloseClass.closeOutStream(	dataOutputStream);
				CloseClass.closeSocket(client);
			}
				break;
			}

		} catch (Exception e) {
			System.out.println("ThreadFactory 에러 발생");
		}
	}


	
	private int whoIsClient() {
		try {
			long time = System.currentTimeMillis();
			while (true) {
				if (dataInputStream.available() > 0) {
					byte[] buffer = new byte[20];
					dataInputStream.read(buffer);
					String recv = new String(buffer).trim();
					//System.out.println(recv);
					switch (recv) {
					case "Ar":
						return 0;
					case "Mb":
						return 1;
					default :
						return -1;
					}				
				} else {
					if (System.currentTimeMillis() - time > 6000)
						return -1;
				}
			}
		} catch (IOException e) {			
			return -1;
		}
	}
}
