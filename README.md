# SmartHome_Project
기존의 2D UI의 스마트홈 제어 앱은 집의 전체적인 상태를 한눈에 파악하기 어렵다.    
이러한 문제점을 해결하기 위해 3D 스마트홈 제어 앱을 제작하였다.  

# 사용한 기술
## Android  
> + Socket(Client)
> + TTS(Text to Speech)
> + STT(Speech-to-Text)  
## Java
> + Multi Threading
>> - Thread
>> - ThreadPool
> + Socket(Server)

## Unity  
 

# APP 
![wlq](https://user-images.githubusercontent.com/81062639/140068495-4384d1ed-2fe8-4b1e-92de-25c93afce646.PNG)

## 전체적인 구조
Android Studio 와 Unity를 이용하여 제어 앱을 제작하여 아두이노와 서버를 통해 데이터를 교환하고 처리한다.    
Android Native 의 기능을 사용하기위해 Android Plugin을 제작 하고 Unity에서 해당 Plugin을 사용한다.  

제어 앱에서 집안 상태 변경 요청을 서버에 전송하면 서버가 아두이노에게 명령을 보내고 아두이노가 명령을 받아 처리, 수행한다.  
명령 처리 결과인 현재 상태를 서버에 전송하여 업데이트하면 제어 앱은 서버에서 최신 상태 정보를 받아 화면 UI를 업데이트 한다.  

![fdsafds](https://user-images.githubusercontent.com/81062639/140278085-a8a7f8b5-87b0-4d57-b037-c9d9b1c21da5.PNG)

# Android Studio
Native 기능을 사용하기 위해 Plugin을 제작한다.
## 사용한 기능  
> + 서버 접속
> + TTS(Text to Speech)
> + STT(Speech-to-Text)


### 서버 접속
서버(TCP Socket)에 접속하는 Thread를 작성.
서버에 데이터를 송신하고 서버로 부터 데이터를 수신받아 Unity에서 사용할 수 있도록 한다.
서버와의 접속 상태를 주기 적으로 확인한다.
```Java
class connectServerThread extends Thread {

        // 접속 소켓
        Socket socket;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        boolean stopThread = false;

        // 마지막으로 서버와 상호작용한 시간
        long preTime;
        // 서버와의 연결 상태를 확인하는 시간 간격
        long connectionCheckInterval = 5000;

        connectServerThread(Socket socket) {
            this.socket = socket;
        }

        //초기화 함수
        // 데이터 통신을 위한 소켓의 입출력 Stream 을 생성한다.
        void init() {
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write("Mb".getBytes());
                //마지막 상호작용 시간을 업데이트 한다.
                preTime =  System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	
	
	        // 서버에 데이터를 전송하는 함수
        void sendData(String data){
            if(dataOutputStream!=null){
                try {
                    dataOutputStream.write(data.getBytes());
                }catch (Exception e){
                    showToast("data 전송 실패");
                }
            }
        }

	
        // Thread를 종료 시키기 위한 변수를 성정하는 함수.
        public void setStopThread(boolean stopThread) {
            this.stopThread = stopThread;
        }

        @Override
        public void run() {

            // Thread 초기화
            init();

            try {
                while (socket != null && !stopThread) {

                    //서버로부터 데이터를 수신한다.

                    if (dataInputStream.available() > 0) {
                        Thread.sleep(200);
                        byte[] buffer = new byte[dataInputStream.available()];
                        dataInputStream.read(buffer);
                        String recv = new String(buffer);

                        //받은 데이터를 처리하여 Unity의 함수에 전달한다.
                        if(recv != "")
                           UnityPlayer.UnitySendMessage("GameObject", "ReadBlutoothData", recv);

                        preTime =  System.currentTimeMillis();
                    }
                    else {
                        // 마지막으로 서버와 상호작용한 시간이 설정 시간을 넘을 경우 서버에 연결상태 확인 데이터를 보낸다.
                        if((System.currentTimeMillis() - preTime) > connectionCheckInterval) {
                            try {
                                dataOutputStream.write("ck".getBytes());
                                preTime = System.currentTimeMillis() ;
                            }catch (Exception e) {
                                //연결 상태가 불량일 시 연결을 종료한다.
                                showToast("서버 종료");
                                break;
                            }
                        }
                    }
                }
                //서버와의 연결 종료를 Unity에게 알린다.
                UnityPlayer.UnitySendMessage("GameObject", "ReadServerMessage", "0");

            //에러 발생시
            } catch (Exception e) {
                e.printStackTrace();
                //서버와의 연결 종료를 Unity에게 알린다.
                UnityPlayer.UnitySendMessage("GameObject", "ReadServerMessage", "0");
            } finally {
                // 자원 정리
                try {
                    if (dataInputStream != null)
                        dataInputStream.close();
                    if (dataOutputStream != null)
                        dataOutputStream.close();
                    if (socket != null)
                        socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
```

### TTS(Text to Speech)
사용자에게 특정한 알람을 소리로 알려주기 위해 사용한 기능이다.    
문자열을 입력받아 소리내어 읽어 준다.  


```java
  //문자열을 받아 소리로 변환해주는 함수
    void makeTTS(String str) {
        if (TTS == null) {
            // TTS 언어 설정
            TTS = new TextToSpeech(context, status -> {
                if (status != -1)
                    TTS.setLanguage(Locale.KOREA);
            });
        }
        TTS.setPitch(1f); // 톤설정
        TTS.setSpeechRate(1f);   // 말 속도 설정;
        TTS.speak(str, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }
```


### STT(Speech-to-Text)
음성인식 기능  
사용자의 음성 입력을 인식하고 문자열로 변환해주는 함수   
 

```java
 // 음성인식 기능 
    // 사용자의 음성인식을 문자열로 변환해주는 함수
    void makeSTT() {

        if (recognitionListener == null) {
            // 음성인식 Listener를 등록한다.
            recognitionListener = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 400);
                    Toast.makeText(context, "음성인식 시작.", Toast.LENGTH_SHORT).show();

                }

 ---------------------------------------------------중략 ------------------------------------------------------	

                //에러발생시 종류에 따른 에러 처리
                @Override
                public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "오디오 에러";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "클라이언트 에러";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "퍼미션 없음";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "네트워크 에러";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "네트웍 타임아웃";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "찾을 수 없음";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RECOGNIZER 가 바쁨";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "서버가 이상함";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "말하는 시간초과";
                            break;
                        default:
                            message = "알 수 없는 오류임";
                            break;
                    }
                    Toast.makeText(context, "에러 : " + message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {

                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    for (int i = 0; i < matches.size(); i++) {
                        STT_Str = (matches.get(i));
                    }
                    UnityPlayer.UnitySendMessage("GameObject", "GetSTT_Msg", STT_Str);
                }


      ---------------------------------------------------중략 ------------------------------------------------------	
            };
```


# 아두이노  
## WIFI Module ESP-01  
ESP-01 의 AP 명령어를 사용하여 공유기에 접속하여 IP를할당 받고 서버에 접속을 시도한다.    
현재 집안의 상태를 읽어 상태가 변경되면 서버에 최신 정보를 업데이트 한다.  


## Wifi 접속 코드
네트워크와 서버 접속과 관련된 코드.

```c
String ssid = " network SSID (name) "; 
String pass = " network password ";     
String domain = " server domain "; 
String port = "";  // server port
String typeTU = "";  // TCP or UDP

bool connectWifi(){

 Serial1.println("AT");
 
 String cmd = "AT+CWJAP="+ssid+","+pass;
  Serial1.println(cmd);
  Serial.println("WiFi 접속 시도 ....");
  delay(8000);
  // wifi 접속 성공 시
  if(Serial1.find("OK")){
    Serial.println("Wifi 연결 성공");  
    
    //할당된 ip 주소 출력
    Serial1.println("AT+CIFSR");
    delay(1000);
    Serial.println("할당된 ip 주소 : ");
    int i =0;
    char data[100]= {}; 
    while(Serial1.available()>0){
      delay(10);
      data[i++]= Serial1.read();
      }
    Serial.println(data); 
    return true;    
  }
   // wifi 접속 실패 시
  else{
    Serial.println("Wifi 연결 실패");
       return false;
  }
}
```
## Server 접속 코드
```c
void connectServer(){
   bool result = false;
   
   // 서버 접속 실패시 다시 접속 시도
   while(!result){
     Serial.println("서버 접속 시도 ....");
      delay(4000);      
      Serial1.println("AT+CIPSTART=\""+ typeTU + "\",\"" + domain + "\"," + port);
      delay(4000);
      result = !Serial1.find("ERROR");
      if(result == false)
        Serial.println("서버 접속 실패");
   } 
   // 서버 접속 성공
   Serial.println("서버 접속 성공");
   delay(500);
   sendData("Ar");
   preTime = millis();  
  }
```
## Server Data 전송 코드
```c
void sendData(String message){   
   //ESP-01 AT DATA 전송 명령어 
   Serial1.println("AT+CIPSEND=" + String(message.length()));
   delay(100);
   Serial1.println(message);
   delay(100);
   // 서버 연결 문제로 실패시 서버 재접속 시도
   if(Serial1.find("ERROR")){
    Serial.println("데이터 전송 실패");
      connectServer();
    }
     preTime=millis();
}
```

# SERVER

## 서버 구조
Multi Threading을 사용한다. 
![서버구조](https://user-images.githubusercontent.com/81062639/140278490-f4a0ea3d-eff8-45e6-b6af-488835f89837.png)   

  
  
## main()
커스텀 명령어기능의 CommandThread와 접속된 Client들을 관리하는 checkConnectionListThread를 생성하고 실행시킨다.  
Client의 접속 요청을 처리할 ThreadPool을 생성한다.  
포트를 개방하고 Client의 접속을 처리한다.  



```java
public static void main(String[] args) {
System.out.println("서버 실행");

Socket client = null;
		
// 커스텀 명령어 Thread
CommandThread commandThread;
CheckConnectionListThread checkConnectionListThread;
  
// ThreadPool 생성
// param :  코어쓰레드 수, 최대 쓰레드 수, 놀고있는시간, 시간 단위, 작업 큐  
ExecutorService factoryThreadPool = new ThreadPoolExecutor(1,3,5L,TimeUnit.MINUTES,new SynchronousQueue<Runnable>());

		
//연결된 소켓 연결상태 확인, 관리 Thread 시작
checkConnectionListThread = CheckConnectionListThread.getInstance();
checkConnectionListThread.start();
  
//명령어 Thread 시작
commandThread = CommandThread.getInstance();
commandThread.setDaemon(true);
commandThread.start();

try {
// 포트 개방
serverSocket = new ServerSocket(port);

while (!serverClose) {
// 최대 연결 개수 4			
// Client 연결 대기
client = serverSocket.accept();
 
//Client 접속 시 factoryThreadPoold에 넣는다.
 factoryThreadPool.submit(new FactoryThread(client));
}
}catch (Exception e) {
 }finally {
try {
// 자원 정리  
 checkConnectionListThread.closeThread();
 CloseClass.closeSocket(client);
 CloseClass.closeServerSocket(serverSocket);				
 CloseClass.closeArduinoList(arduinoList);
 CloseClass.closeMobileList(mobileList);
 CloseClass.closeThreadPool(factoryThreadPool);				
 commandThread.closeThread();			
 Thread.sleep(2000);
 System.out.println("서버 종료");
}catch (Exception e) {
 e.printStackTrace();
  }
 }
}
```

## FactoryThread
작업 큐에서 요청을 가져와 처리한다.   
FactoryThread 안의 whoIsClient() 함수를 이용하여 Client가 스마트폰인지 아두이노 인지 판단한다.  
판단하는 작업은 간단한 문자열 키를 사용한다.  


```java
//Client의 종류를 판단하는 함수
	private int whoIsClient() {
		try {
			long time = System.currentTimeMillis();
			while (true) {
			// Client로 부터 요청 인증 KEY를 읽는다.
				if (dataInputStream.available() > 0) {
					byte[] buffer = new byte[20];
					dataInputStream.read(buffer);
					String recv = new String(buffer).trim();
					//System.out.println(recv);
					switch (recv) {
					case "아두이노 인증 KEY":
						return 0;
					case "스마트폰 인증 KEY":
						return 1;
					//요청 거절
					default :
						return -1;
					}				
				} else {
					// TimeOut
					if (System.currentTimeMillis() - time > 6000)
						return -1;
				}
			}
		} catch (IOException e) {			
			return -1;
		}
	}
```

## FactoryThread run()
판단된 클라이언트 종류에 따라 최대 동시 접속 가능 수를 확인하고 해당하는 (아두이노 or 스마트폰)Thread를 만들어 실행 시킨다.  


```java
	@Override
	public void run() {
		try {
			if (client == null)
				return;
			System.out.println("소켓 연결");
			InetAddress ia = client.getInetAddress();
			String clientIp = ia.getHostAddress(); // 접속 Client ip
			
			//  접속한 Client 구분
			switch (whoIsClient()) {
			// 아두이노 접속
			case 0: {
				System.out.println("Arduino 접속");
				System.out.println("Arduino ip : " + clientIp);
				
				// 최대 동시 접속 확인 
				if (Server.arduinoList.size() < MAX_ARDUINO_NUM) {
					ArduinoThread arduino = new ArduinoThread(client);		
					arduino.start();
					Server.arduinoList.add(arduino);		

				} 
				// 최대 동시 접속 초과
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
				// 최대 동시 접속 확인 
				if (Server.mobileList.size() < MAX_MOBILE_NUM) {
					MobileThread mobile = new MobileThread(client);
					mobile.start();
					Server.mobileList.add(mobile);					
				}
				// 최대 동시 접속 초과
				else {
					System.out.println("최대 Smartphone 개수 초과 , 소켓 연결 해제");					
					CloseClass.closeOutStream(	dataOutputStream);
					CloseClass.closeSocket(client);	
				
				}
			}
				break;
			// 에러, 미허용된 접속자
			case -1: {
				System.out.println("허용되지 않은 접속, 소켓 연결 해제");
				
				//소켓 종료
				CloseClass.closeOutStream(dataOutputStream);
				CloseClass.closeSocket(client);
			}
				break;
			}

		} catch (Exception e) {
			System.out.println("ThreadFactory 에러 발생");
		}
```

