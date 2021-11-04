package Server;


public class DataClass {

	private static DataClass instance;
	
	private static int LAMP_NUM = 4;
	private static int DOOR_NUM = 4;
	private static int WINDOW_NUM = 3;
	private char[] lampStatus = new char[LAMP_NUM];
	private int[] doorAngles= new int[DOOR_NUM];
	private int[][] windowAngles= new int[DOOR_NUM][2];
	
	private DataClass() {
		for(int i =0; i<LAMP_NUM; i++) {
			lampStatus[i] = '0';
		}
	}
	
	public static DataClass getInstance() {
		if(instance == null)
			instance = new DataClass();
		
		return instance;
	}

	void setLampStatus(char[] newStatus) {
		for(int i =0; i<LAMP_NUM; i++) {
			lampStatus[i] = newStatus[i];
		}
	}
	
	void setDoorAngle(String data) {
		int index = data.charAt(1) - '0';
		int angle = Integer.parseInt(data.substring(2,data.length()));
			doorAngles[index] = angle;
	}
	
	public void setWindowAngle(String data) {
		int index = data.charAt(1) - '0';
		int lr =  data.charAt(2) - '0';
		windowAngles[index][lr] = Integer.parseInt(data.substring(3,data.length()));		
	}
	
	
	String getStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append(getLampStatus());
		sb.append(getDoorStatus()); 
		//sb.append("W909090909090");
		sb.append(getWindowStatus());
		return  sb.toString();
		//return getLampStatus() + "D90909090";
	}
	String getLampStatus() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("L");
		for(char i : lampStatus) {
			sb.append(String.valueOf(i));
		}
		//System.out.println("sb" +sb.toString() );
		return sb.toString();
	}
	String getDoorStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append("D");
		for(int i :  doorAngles) {
			if(i<10)
				sb.append("0");
			
			sb.append(String.valueOf(i));
		}
		return sb.toString();
	}

	String getWindowStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append("W");
		for(int i =0; i<WINDOW_NUM; i++) {
			for(int j =0; j<2;  j++) {
				if(windowAngles[i][j]<10)
					sb.append("0");
				sb.append(windowAngles[i][j]);
			}
		}
		return sb.toString();
	}
	
}
