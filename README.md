# SmartHome_Project
기존의 2D UI의 스마트홈 제어 앱은 집의 전체적인 상태를 한눈에 파악하기 어렵다.  
이러한 문제점을 해결하기 위해 3D 스마트홈 제어 앱을 제작하였다.

## APP 
![wlq](https://user-images.githubusercontent.com/81062639/140068495-4384d1ed-2fe8-4b1e-92de-25c93afce646.PNG)

## 전체적인 구조
Android Studio 와 Unity를 이용하여 제어 앱을 제작하여 아두이노와 서버를 통해 데이터를 교환하고 처리한다.     
제어 앱에서 집안 상태 변경 요청을 서버에 전송하면 서버가 아두이노에게 명령을 보내고 아두이노가 명령을 받아 처리, 수행한다.  
명령 처리 결과인 현재 상태를 서버에 전송하여 업데이트하면 제어 앱은 서버에서 최신 상태 정보를 받아 화면 UI를 업데이트 한다.  

![fdsafds](https://user-images.githubusercontent.com/81062639/140278085-a8a7f8b5-87b0-4d57-b037-c9d9b1c21da5.PNG)



## 아두이노  
### WIFI Module ESP-01  
ESP-01 의 AP 명령어를 사용하여 공유기에 접속하여 IP를할당 받고 서버에 접속을 시도한다.  
현재 집안의 상태를 읽어 상태가 변경되면 서버에 최신 정보를 업데이트 한다.

### Wifi 접속 코드
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
### Server 접속 코드
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
### Server Data 전송 코드
```c
void sendData(String message){   
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

## SERVER

### 서버 구조
Multi Threading을 사용한다. 
![서버구조](https://user-images.githubusercontent.com/81062639/140278490-f4a0ea3d-eff8-45e6-b6af-488835f89837.png)


