#include <SoftwareSerial.h>
#include <Servo.h> 

String ssid = ""; // network SSID (name)

String pass = "";    // network password

String domain = ""; // server domain

String port = "8888";  // server port
String typeTU = "TCP";  // TCP or UDP

// 일정 주기로 연결 상태를 확인하기 위해 check 데이터를 서버에 보냄. 이를 위한 시간 변수
long preTime;
// 서버 연결 상태 확인 시간 간격
long connectionCheckInterval = 5000;

//LED ON, OFF 조작 핀
const int LIGHT_POWER_PIN[] = {4,5,6,7};
//LED 상태 정보 입력 핀
const int LIGHT_READ_PIN[] = {8,9,10,11};
// 현대 집안의 전등 상태 정보
int lightStatus[ sizeof( LIGHT_READ_PIN)/sizeof(int)]; 

// 문 서보모터 제어 핀
const int SERVO_DOOR_PINS[] = {30,31,32,33}; 
// 문 서보모터 
Servo SERVO_DOOR1; Servo SERVO_DOOR2; Servo SERVO_DOOR3; Servo SERVO_DOOR4;
Servo DOOR_SERVO[] = {SERVO_DOOR1,SERVO_DOOR2,SERVO_DOOR3,SERVO_DOOR4};
//문 서보모터 상태 정보
int doorStatus[ sizeof(SERVO_DOOR_PINS)/sizeof(int)]; 

// 창문 조작 핀
const int SERVO_WINDOW_PINS[3][2] = {
{40,41},
{42,43},
{44,45},
}; 
// 창문 서보모터
Servo SERVO_WINDOW1_L; Servo SERVO_WINDOW1_R;
Servo SERVO_WINDOW2_L; Servo SERVO_WINDOW2_R;
Servo SERVO_WINDOW3_L; Servo SERVO_WINDOW3_R;
const Servo WINDOW_SERVO[3][2] = {
{SERVO_WINDOW1_L, SERVO_WINDOW1_R},
{SERVO_WINDOW2_L, SERVO_WINDOW2_R},
{SERVO_WINDOW3_L, SERVO_WINDOW3_R},
}; 

// 창문 서보모터 상태 정보
int windowStatus[3][2]; 

//통신 관련 wifi, server
/**********************************************************************************************************
**********************************************************************************************************/

// 서버 접속 시도 함수
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

// 서버 접속 시도 
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

// wifi 연결 해제
void disconnectWifi(){
   Serial1.println("AT+CWQAP");
}

// 서버에 데이터 전송
void sendData(String message){   
   Serial1.println("AT+CIPSEND=" + String(message.length()));
   delay(100);
   Serial1.println(message);
   delay(100);
   if(Serial1.find("ERROR")){
    Serial.println("데이터 전송 실패");
      connectServer();
    }
     preTime=millis();
}

/**********************************************************************************************************
**********************************************************************************************************/


//전등 관련 LAMP
/**********************************************************************************************************
**********************************************************************************************************/

// 집의 전등 상태를 업데이트 한다.
void readLightStatus(){
    bool light_changed = false;
    // 기존의 전등 값과 현재 값의 동등 여부를 확인한다.
      for(int i =0; i < sizeof(LIGHT_POWER_PIN)/sizeof(int); i++){
        int val = digitalRead(LIGHT_READ_PIN[i]);
        if(lightStatus[i] == val ) continue;

        lightStatus[i] = val;
        if(light_changed == false)
        light_changed = true;
      }
      // 상태값이 변했다면 데이터를 서버에 전송한다.
        if(light_changed == true){
            String str = "";
            for(int i =0; i < sizeof(lightStatus)/sizeof(int); i++){
            str += String(lightStatus[i]);      
        }
        sendData("L" + str);
  }
}
/**********************************************************************************************************
**********************************************************************************************************/
// 현재 창문의 서보모터 상태를 업데이트 한다.
void readWindowStatus(){
     for(int i =0; i < 3; i++){  
      for(int j=0; j < 2; j++){    
       int angle = WINDOW_SERVO[i][j].read();
       // 상태가 변했다면 서버에 데이터를 전송하여 상태를 업데이트 한다.
        if(windowStatus[i][j] != angle){
            windowStatus[i][j] = angle;           
          if(angle<10)
            sendData("W"+String(i)+String(j)+ "0" + String(angle));   
          else
            sendData("W"+String(i)+String(j)+ String(angle));   
            }
    }
   }
  }
/**********************************************************************************************************
**********************************************************************************************************/
// 현재 문 서보모터의 상태를 업데이트 한다.
void readDoorStatus(){
  // 문의 정보 데이터 태그
  String angleData = "D";
   for(int i =0; i < sizeof(SERVO_DOOR_PINS)/sizeof(int); i++){   
       int angle = DOOR_SERVO[i].read();
        // 상태가 변했다면 서버에 데이터를 전송하여 상태를 업데이트 한다.
        if(doorStatus[i] != angle){
            doorStatus[i] = angle;           
          if(angle<10)
            sendData("D"+String(i) + "0" + String(angle));   
          else
            sendData("D"+String(i)+ String(angle));    
          }
    }
  }

  
//초기화 , setup, loop 
/**********************************************************************************************************
**********************************************************************************************************/
// 핀의 입출력 모드를 설정.
void setPinMode(){
   for(int i =0; i < sizeof(LIGHT_READ_PIN)/sizeof(int); i++){
      pinMode(LIGHT_POWER_PIN[i], OUTPUT);     
      digitalWrite(LIGHT_POWER_PIN[i], 0);
   }

}

void setup() {
 
   Serial.begin(9600);
   Serial1.begin(9600);
   Serial1.write("AT+CWQAP");
   Serial1.write("AT+CIPCLOSE");

   //문 서보핀 초기화
   for(int i =0; i < sizeof(SERVO_DOOR_PINS)/sizeof(int); i++){   
      DOOR_SERVO[i].attach(SERVO_DOOR_PINS[i]);
      DOOR_SERVO[i].write(0);
      doorStatus[i] = DOOR_SERVO[i].read();
    }

   // 창문 서보핀 초기화
   for(int i =0; i < 3; i++){  
      for(int j=0; j < 2; j++){    
      WINDOW_SERVO[i][j].attach(SERVO_WINDOW_PINS[i][j]);
      WINDOW_SERVO[i][j].write(0);
      windowStatus[i][j] = WINDOW_SERVO[i][j].read();
    }
   }

    
  //접속 중이 아니라면 무한루프
  while(true){
    // 접속 시도 루프    
    if(connectWifi()){
      break;
     }
      delay(1000);
  }

    setPinMode();
    Serial.println("WiFi에 접속 상태 입니다.");
    
    //server 접속시도
    connectServer();
}


void loop() {

// wifi 수신 값 처리
if(Serial1.available()) {
    int i =0;
    char data[20]= {}; 
    while(Serial1.available()>0){
      delay(10);
      data[i++]= Serial1.read();
      }
    Serial.println(data); 

   //+IPD,20: 에 해당하는 부분 제거
   int offset = 10;
 
  Serial.println(data[0+offset]); 
    String dataStr = data;
    if(dataStr.startsWith("CLOSED")){
      Serial.println("서버 종료");
      connectServer();
      }
    //램프 조작 신호
    else if(data[0+offset] == 'L'){
      // Serial.println("Lamp 조작"); 
      int index = data[1+offset]-'0';
      int value =  data[2+offset]-'0';
      if(value == 1)
        digitalWrite(LIGHT_POWER_PIN[index],HIGH);
      else if(value == 0)
        digitalWrite(LIGHT_POWER_PIN[index],LOW);
     }

 // 문 조작 신호
   //+IPD,20:D010
    else if(data[0+offset] == 'D'){
  // Serial.println("문 조작"); 
    int index = data[1+offset]-'0';
    int value1 = data[2+offset]-'0';
    int value2 = data[3+offset]-'0';
    int angle = value1*10+value2;

    DOOR_SERVO[index].write(angle);
     }
 // 창문 조작 신호
   //+IPD,20:W0010
    else if(data[0+offset] == 'W'){
  // Serial.println("창문 조작"); 
    int index = data[1+offset]-'0';
    int lr = data[2+offset]-'0';
    int value1 = data[3+offset]-'0';
    int value2 = data[4+offset]-'0';
    int angle = value1*10+value2;

     WINDOW_SERVO[index][lr].write(angle);
     }
     
    preTime=millis();
  }
  else{
    if(millis()-preTime > connectionCheckInterval){
     preTime=millis();
      sendData("ck"); 
    }    
    }
  
//  // sendData("Arduino");
//  if(Serial.available()) {
//    Serial1.write(Serial.read());
//  } 

//  if(Serial1.available()) {
//    Serial.write(Serial1.read());
//  } 
   readLightStatus();  
   delay(100);
   readDoorStatus();
   readWindowStatus();
}
