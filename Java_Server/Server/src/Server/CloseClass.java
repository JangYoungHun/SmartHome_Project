package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CloseClass {


	static void closeServerSocket(ServerSocket socket) {	
		if(socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ServerSocket 해제 중 문제 발생");
			}
		}
	}
	
	static void closeSocket(Socket socket) {	
		if(socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("socket 해제 중 문제 발생");
			}
		}
	}

	static void closeOutStream(DataOutputStream stream) {	
		if(stream != null) {
			try {
				stream.close();

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Outstream 해제 중 문제 발생");
			}
		}
	}
	
	static void closeInputStream(DataInputStream stream) {	
		if(stream != null) {
			try {
				stream.close();

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Inputstream 해제 중 문제 발생");
			}
		}
	}
	
	static void closeThreadPool(ExecutorService threadPool) {	
		if(threadPool != null) {
			try {
				threadPool.shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ThreadPool 해제 중 문제 발생");
			}
		}
	}
	
	static void closeBufferedreader(BufferedReader reader) {	
		if(reader != null) {
			try {
				reader.close();

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Bufferedreader 해제 중 문제 발생");
			}
		}
	}
	
	static void closeArduinoList(List<ArduinoThread> list) {	
		System.out.println("ArduinoList 해제 중 . . . .");
		if(list != null) {
			try {
				for(int i =0; i<list.size(); i++) {				
					list.get(i).closeThread();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ArduinoList 해제 중 문제 발생");
			}
		}
	}
	static void closeMobileList(List<MobileThread> list) {	
		System.out.println("MobileList 해제 중 . . . .");
		if(list != null) {
			try {
				for(int i =0; i<list.size(); i++) {
					list.get(i).closeThread();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("MobileThread 해제 중 문제 발생");
			}
		}
	}
}
