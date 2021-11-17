package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	private static boolean serverClose = false;
	private static int port = 8888;
	private static ServerSocket serverSocket = null;
	// ���� ���� Ŭ����
	
	// ���� �ִ� 4�� �Ƶ��̳�
	static List<ArduinoThread> arduinoList = new ArrayList<ArduinoThread>();
	// ���� �ִ� 4�� �� �����
	static List<MobileThread> mobileList = new ArrayList<MobileThread>();
	
	// ���� ��ü ���� ����
	private Server() {
	}

	public static void main(String[] args) {
		System.out.println("���� ����");
		// �Ƶ��̳� ������ ��ٸ���.
		Socket client = null;
		
		CommandThread commandThread;
		CheckConnectionListThread checkConnectionListThread;
		// param :  �ھ���� ��, �ִ� ������ ��, ����ִ½ð�, �ð� ����, �۾� ť
		ExecutorService factoryThreadPool = new ThreadPoolExecutor(1,3,5L,TimeUnit.MINUTES,new SynchronousQueue<Runnable>());

		
		//����� ���� ������� Ȯ�� �� ������Ʈ Thread
		checkConnectionListThread = CheckConnectionListThread.getInstance();
		checkConnectionListThread.start();
		//��ɾ� Thread
		commandThread = CommandThread.getInstance();
		commandThread.setDaemon(true);
		commandThread.start();

		try {
			serverSocket = new ServerSocket(port);

			while (!serverClose) {
				// �ִ� ���� ���� 4			
				client = serverSocket.accept();
				factoryThreadPool.submit(new FactoryThread(client));
			//	new FactoryThread(client).start();
			}

		} catch (Exception e) {			
			
		} finally {
			try {
				checkConnectionListThread.closeThread();
				CloseClass.closeSocket(client);
				CloseClass.closeServerSocket(serverSocket);				
				CloseClass.closeArduinoList(arduinoList);
				CloseClass.closeMobileList(mobileList);
				CloseClass.closeThreadPool(factoryThreadPool);				
				commandThread.closeThread();			
				Thread.sleep(2000);
				System.out.println("���� ����");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	


	static void closeServer() {
		System.out.println("���� ���� . . . .");
		System.out.println("�ڿ� ���� . . . .");
		CloseClass.closeServerSocket(serverSocket);		
		serverClose = true;
	}

}
