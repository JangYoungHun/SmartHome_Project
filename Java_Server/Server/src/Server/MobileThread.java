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
			preTime =  System.currentTimeMillis();
		} catch (Exception e) {
		}
	}
	// ������ ����
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
	// ������ �۽�
	void  sendData(String data) throws Exception {
		dataOutputStream.write(data.getBytes());
		preTime = System.currentTimeMillis();
	}
	
	//���� ���� ���� Ȯ��
	void checkConnection() throws IOException {
			dataOutputStream.write("ck".getBytes());
			preTime = System.currentTimeMillis();
			}
	
	//����� ��� �Ƶ��̳뿡�� ������ ����
		void writeToArduino(String data) throws Exception{
			for(int i =0; i<Server.arduinoList.size(); i++) {
				ArduinoThread arduino = Server.arduinoList.get(i);
				try {				
					arduino.sendData(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Arduino " +arduino.getIp() + " ������ ���� ����");
					throw e;
				}
			}
		}
		//����� ��� ����Ʈ������ ������ ����
		void writeToMobile(String data) throws Exception{
			for(int i =0; i<Server.mobileList.size(); i++) {
				MobileThread mobile = Server.mobileList.get(i);
				try {				
					mobile.sendData(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Smartphone " +mobile.getIp() + " ������ ���� ����");
					throw e;
				}
			}
		}
	@Override
	public void run() {
		try {
			init();
			
			while (client != null && !close) {			
		
				//����Ʈ�� ���� ���޵� ��û ó��
				
				if (dataInputStream.available() > 0) {	
					String recv = readData();
					System.out.println(recv);
					writeToArduino(recv);
				}
			
				//���� ���� ���� ����
				if((System.currentTimeMillis() - preTime) >DataSendInterval ) {
					try {
						//���� ��ġ�� ���¸� �о� ����� ����Ʈ������ �۽�
						sendData(dataClass.getStatus());								
						preTime = System.currentTimeMillis();
					}catch (Exception e) {
						System.out.println(clientIp +" Smartphone ���� ���� ����");
						break;
					}
				
				} 
			}
		} catch (Exception e) {
			 	
		}
		finally {
				System.out.println(clientIp +" Smartphone ���� ����");
				CloseClass.closeSocket(client);
				CloseClass.closeInputStream(dataInputStream);
				CloseClass.closeOutStream(dataOutputStream);
		}
	}

}