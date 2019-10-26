package com.ddf.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	public static long getMillis() {
		return System.currentTimeMillis();
	}
	
	public static Date getNow() {
		return new Timestamp(getMillis());
	}
	
	public static String getNowTime() {
		DateFormat df = new SimpleDateFormat("HH:mm");
		return df.format(new Date());
	}
	
	public static Date getTodayStartTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}
	
	public static Date getTodayEndTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}

	public static String addDate(int n) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, n);
		return StringUtil.date2String(cal.getTime());
	}

	public static Date addDate(Date d, int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DAY_OF_MONTH, n);
		return cal.getTime();
	}
	
	public static long getDiffDays(Date from, Date to) {
		return (to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000);
	}

	public static int getYearNum() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	public static String getYear() {
		return "" + getYearNum();
	}

	public static int getMonthNum() {
		return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}

	public static String getMonth() {
		int m = getMonthNum();
		return m < 10 ? "0" + m : "" + m;
	}

	public static int getDayNum(){
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}
	
	public static String getDay(){
		return "" + getDayNum();
	}
	
	public static int getWeekNum(){
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}
	
	public static String getWeek(){
		int week=getWeekNum();		
		return getWeek(week);
	}
	
	public static String getWeek(int week){		
		String strWeek="";
		switch(week){
		case 0:
			strWeek="周日";
			break;
		case 1:
			strWeek="周一";
			break;
		case 2:
			strWeek="周二";
			break;
		case 3:
			strWeek="周三";
			break;
		case 4:
			strWeek="周四";
			break;
		case 5:
			strWeek="周五";
			break;
		case 6:
			strWeek="周六";
			break;
		}
		return strWeek;
	}
	
	public static int getWeekNum(Date date){
		Calendar cal = Calendar.getInstance();		
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);		
	}
	
	public static String getWeek(Date date){
		int week=getWeekNum(date);		
		return getWeek(week);
	}
	
	public static String getWeek(String dateString){
		Date date=StringUtil.string2Date(dateString);
		if(date!=null){
			return getWeek(date);
		}
		else{
			return "";
		}
	}
	
	
	public static String getChineseDate(Calendar calendar) {  
        int i = calendar.get(1);  
        int j = calendar.get(2);  
        int k = calendar.get(5);  
        StringBuffer localStringBuffer = new StringBuffer();  
        localStringBuffer.append(i);  
        localStringBuffer.append("年");  
        localStringBuffer.append(j + 1);  
        localStringBuffer.append("月");  
        localStringBuffer.append(k);  
        localStringBuffer.append("日");  
        return localStringBuffer.toString();  
    }  
}
