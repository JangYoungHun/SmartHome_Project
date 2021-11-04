package Server;

import java.util.ArrayList;
import java.util.List;

public class CheckConnectionListThread extends Thread {
	
	private static CheckConnectionListThread instance;
	
	private final static int CHECK_INTERVAL = 2000;
	

	
	boolean close = false;


	static CheckConnectionListThread getInstance() {
		if(instance == null) 
			instance = new CheckConnectionListThread();
		
		return instance;
	}
	

	void closeThread() {
		this.close = true;
	}
	
	//������ �ð����� ����� client �ִ��� Ȯ���Ͽ� list ������Ʈ
	@Override
	public void run() {
		
		while(!close) {
		List<ArduinoThread> aliveArduino = new ArrayList<ArduinoThread>();
		for(int i =0; i<Server.arduinoList.size(); i++) {
			if(Server.arduinoList.get(i).isAlive()) {
				aliveArduino.add(Server.arduinoList.get(i));
			}
		}
		Server.arduinoList = aliveArduino;
		
		
		List<MobileThread> aliveMobile = new ArrayList<MobileThread>();
		for(int i =0; i<Server.mobileList.size(); i++) {
			if(Server.mobileList.get(i).isAlive()) {
				aliveMobile.add(Server.mobileList.get(i));
			}
		}
		Server.mobileList = aliveMobile;
		
		
		try {
			Thread.sleep(CHECK_INTERVAL);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	System.out.println("���� ���� List ������Ʈ");
	}
		
		System.out.println("CheckConnectionListThread ���� ");
		
	}
	
	void showConnections(){
		System.out.println("Arduino ���� ����");
		
		System.out.println("����� Arduino ���� : " + Server.arduinoList.size());
		for(int i =0; i<Server.arduinoList.size(); i++) {
			System.out.println(Server.arduinoList.get(i).getIp());
		}
		
		System.out.println("Mobile ���� ����");
		System.out.println("����� Mobile ���� : " + Server.mobileList.size());
		for(int i =0; i<Server.mobileList.size(); i++) {
			System.out.println(Server.mobileList.get(i).getIp());
		}	
	}

	
}
