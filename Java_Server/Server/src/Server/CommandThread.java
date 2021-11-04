package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandThread extends Thread {

	private static CommandThread instance;
	private boolean close = false;
	private DataClass dataClass =DataClass.getInstance() ;
	private CommandThread() {
	}

	void closeThread() {
		close = true;
	}

	static CommandThread getInstance() {
		if (instance == null)
			return instance = new CommandThread();
		return instance;
	}

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	@Override
	public void run() {

		try {
			while (!close) {
				String command = br.readLine();
				if (command != null && !command.equals("")) {
					switch (command) {
					// 서버 종료
					case "closeServer":
					case "exit":
					case "close": {
						close = true;
						Server.closeServer();
					}
						break;
					
					case "show connections" :{
						CheckConnectionListThread thread = CheckConnectionListThread.getInstance();
						if(thread !=null) {
							thread.showConnections();
						}
						else {
							System.out.println("CheckConnectionListThread 상태를 확인해주세요");
						}
					} 
						break;
					case "show windowstatus" : {
						System.out.println(dataClass.getWindowStatus());
					} 
						break;
					case "show doorstatus" : {
						System.out.println(dataClass.getDoorStatus());
					} 
						break;
					case "show lampstatus" : {
						System.out.println(dataClass.getLampStatus());
					} 
						break;
					default:
						System.out.println("잘못된 명령어 입니다.");
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Command Thread ERROR 발생");
		} finally {
			CloseClass.closeBufferedreader(br);
			System.out.println("command Thread 종료");
		}

	}
}
