package com.example.bodyalarm.utils;
public class Const {

	public static String TAG="CASE";
	
	//人体
	public static String BODY_CHK= "01 03 00 3c 00 01 44 06";
	public static int BODY_NUM=1;
	public static int BODY_LEN=7;
	public static Boolean BODY=null;
	
	//蜂鸣器
	public static String BUZZER_ON= "01 10 00 5a 00 02 04 01 00 00 00 77 10";
	public static String BUZZER_OFF= "01 10 00 5a 00 02 04 00 00 00 00 76 ec";
	public static boolean isBuzzerOn=false;
	
	//IP端口
	public static String BODY_IP= "192.168.0.104";
	public static int BODY_PORT=4001;
	public static String BUZZER_IP= "192.168.0.107";
	public static int BUZZER_PORT=4001;
	
	//配置
	public static Integer time=500;
	public static Boolean linkage=true;
	public static boolean SMS=true;
}
