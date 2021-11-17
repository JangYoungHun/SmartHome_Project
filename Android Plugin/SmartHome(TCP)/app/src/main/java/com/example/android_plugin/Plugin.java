package com.example.android_plugin;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Plugin {

    String STT_Str;
    Intent intent;
    RecognitionListener recognitionListener;
    SpeechRecognizer speechRecognizer;
    TextToSpeech TTS;
    Activity UnityActivity;
    List<String> list;
    String readMessage;
    Socket socket;
    connectServerThread thread;
    final static String ERROR_TAG = "ERROR 발생 ";
    final static String ADDRESS = "도메인 주소";
    final static int PORT = 8888; //  포트번호
    private Context context;

    private static Plugin instance;

    Handler handler = new Handler();

    public static Plugin getInstance() {
        if (instance == null)
            instance = new Plugin();
        return instance;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }


    private void setActivity(Activity activity) {
        UnityActivity = activity;
    }

    private String getReadMessage() {
        return readMessage;
    }


    private void makeAlertDialog(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
                .setTitle("확인")
                .setMessage(str)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void deleteTTS() {
        TTS.stop();
        TTS.shutdown();
        TTS = null;
    }

    void makeTTS(String str) {
        if (TTS == null) {
            TTS = new TextToSpeech(context, status -> {
                if (status != -1)
                    TTS.setLanguage(Locale.KOREA);
            });
        }
        TTS.setPitch(1f); // 톤설정
        TTS.setSpeechRate(1f);   // 말 속도 설정;
        TTS.speak(str, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    void makeSTT() {

        if (recognitionListener == null) {
            recognitionListener = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 400);
                    Toast.makeText(context, "음성인식 시작.", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

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


                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            };
        }


        if (intent == null) {
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(recognitionListener);
        }

        speechRecognizer.startListening(intent);


    }



    void sendData(String data){
       if(thread != null && thread.isAlive()){
           thread.sendData(data);
       }
    }

    // 서버 접속 시도 Socket 생성, Thread 실행
    boolean connectServer() {

        try {
            socket = new Socket(ADDRESS, PORT);

            if (socket != null) {
                thread = new connectServerThread(socket);
                thread.start();

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(ERROR_TAG, "Socket 생성 중 에러발생");
            return false;
        }
        return socket.isConnected();
    }

    void disconnectServer() {
        if (thread != null) {
            thread.setStopThread(true);
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(ERROR_TAG, "Socket.close() 중 에러발생");

            }
        }
    }

    class connectServerThread extends Thread {
        Socket socket;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        boolean stopThread = false;
        long preTime;
        long connectionCheckInterval = 5000;
        connectServerThread(Socket socket) {
            this.socket = socket;
        }

        void sendData(String data){
            if(dataOutputStream!=null){
                try {
                    dataOutputStream.write(data.getBytes());
                }catch (Exception e){
                    showToast("data 전송 실패");
                }
            }
        }

        void init() {
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write("Mb".getBytes());
                preTime =  System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setStopThread(boolean stopThread) {
            this.stopThread = stopThread;
        }

        @Override
        public void run() {

            init();

            try {
                while (socket != null && !stopThread) {
                    if (dataInputStream.available() > 0) {
                        Thread.sleep(200);
                        byte[] buffer = new byte[dataInputStream.available()];
                        dataInputStream.read(buffer);
                        String recv = new String(buffer);

                        //받은 데이터 처리
                        if(recv != "")
                           UnityPlayer.UnitySendMessage("GameObject", "ReadBlutoothData", recv);

                        preTime =  System.currentTimeMillis();
                    }
                    else {
                        if((System.currentTimeMillis() - preTime) > connectionCheckInterval) {
                            try {
                                dataOutputStream.write("ck".getBytes());
                                preTime = System.currentTimeMillis() ;
                            }catch (Exception e) {
                                showToast("서버 종료");
                                break;
                            }
                        }
                    }
                }
                //서버 닫히거나 종료
                UnityPlayer.UnitySendMessage("GameObject", "ReadServerMessage", "0");


            } catch (Exception e) {
                e.printStackTrace();
                UnityPlayer.UnitySendMessage("GameObject", "ReadServerMessage", "0");
            } finally {
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

}
