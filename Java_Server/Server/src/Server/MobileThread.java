package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

////Smartphone client Thread
class MobileThread extends Thread {
	

	private Socket client = null;
	// input port
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private InetAddress ia;
	private String clientIp;
	private long preTime ;
	private long connectionCheckInterval = 5000;
	private long DataSendInterval = 1000;
	private boolean close = false;
	private DataClass dataClass = DataClass.getInstance();
	public MobileThread (Socket socket) {
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
			preTime =  System.currentTimeMillis();
		} catch (Exception e) {
		}
	}
	// 데이터 수신
	String readData() {
		try {
		byte[] buffer = new byte[20];
		dataInputStream.read(buffer);
		String recv = new String(buffer);
		 preTime = System.currentTimeMillis() ;
		 return recv;
		}
		catch (Exception e) {
			System.out.println(clientIp + "Data Read Exception");
			return "";
		}
	}
	// 데이터 송신
	void  sendData(String data) throws Exception {
		dataOutputStream.write(data.getBytes());
		preTime = System.currentTimeMillis();
	}
	
	//소켓 연결 상태 확인
	void checkConnection() throws IOException {
			dataOutputStream.write("ck".getBytes());
			preTime = System.currentTimeMillis();
			}
	
	//연결된 모든 아두이노에게 데이터 전송
		void writeToArduino(String data) throws Exception{
			for(int i =0; i<Server.arduinoList.size(); i++) {
				ArduinoThread arduino = Server.arduinoList.get(i);
				try {				
					arduino.sendData(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Arduino " +arduino.getIp() + " 데이터 전송 에러");
					throw e;
				}
			}
		}
		//연결된 모든 스마트폰에게 데이터 전송
		void writeToMobile(String data) throws Exception{
			for(int i =0; i<Server.mobileList.size(); i++) {
				MobileThread mobile = Server.mobileList.get(i);
				try {				
					mobile.sendData(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Smartphone " +mobile.getIp() + " 데이터 전송 에러");
					throw e;
				}
			}
		}
	@Override
	public void run() {
		try {
			init();
			
			while (client != null && !close) {			
		
				//스마트폰 에서 전달된 요청 처리
				
				if (dataInputStream.available() > 0) {	
					String recv = readData();
					System.out.println(recv);
					writeToArduino(recv);
				}
			
				//매초 상태 정보 전송
				if((System.currentTimeMillis() - preTime) >DataSendInterval ) {
					try {
						//현재 장치의 상태를 읽어 연결된 스마트폰으로 송신
						sendData(dataClass.getStatus());								
						preTime = System.currentTimeMillis();
					}catch (Exception e) {
						System.out.println(clientIp +" Smartphone 연결 상태 오류");
						break;
					}
				
				} 
			}
		} catch (Exception e) {
			 	
		}
		finally {
				System.out.println(clientIp +" Smartphone 소켓 종료");
				CloseClass.closeSocket(client);
				CloseClass.closeInputStream(dataInputStream);
				CloseClass.closeOutStream(dataOutputStream);
		}
	}

}