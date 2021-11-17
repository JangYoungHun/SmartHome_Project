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
	// 상태 정보 클래스
	
	// 동시 최대 4개 아두이노
	static List<ArduinoThread> arduinoList = new ArrayList<ArduinoThread>();
	// 동시 최대 4대 폰 제어가능
	static List<MobileThread> mobileList = new ArrayList<MobileThread>();
	
	// 서버 객체 생성 막기
	private Server() {
	}

	public static void main(String[] args) {
		System.out.println("서버 실행");
		// 아두이노 연결을 기다린다.
		Socket client = null;
		
		CommandThread commandThread;
		CheckConnectionListThread checkConnectionListThread;
		// param :  코어쓰레드 수, 최대 쓰레드 수, 놀고있는시간, 시간 단위, 작업 큐
		ExecutorService factoryThreadPool = new ThreadPoolExecutor(1,3,5L,TimeUnit.MINUTES,new SynchronousQueue<Runnable>());

		
		//연결된 소켓 연결상태 확인 후 업데이트 Thread
		checkConnectionListThread = CheckConnectionListThread.getInstance();
		checkConnectionListThread.start();
		//명령어 Thread
		commandThread = CommandThread.getInstance();
		commandThread.setDaemon(true);
		commandThread.start();

		try {
			serverSocket = new ServerSocket(port);

			while (!serverClose) {
				// 최대 연결 개수 4			
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
				System.out.println("서버 종료");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	


	static void closeServer() {
		System.out.println("서버 종료 . . . .");
		System.out.println("자원 정리 . . . .");
		CloseClass.closeServerSocket(serverSocket);		
		serverClose = true;
	}

}
