package com.loic.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	/**
     * 得到当前的周数（一年中的第几周）
     * 
     */
	public static int getNumWeek() 
	{
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int xq = c.get(Calendar.DAY_OF_WEEK);
        if (xq == 1) {
                c.set(Calendar.DATE, c.get(Calendar.DATE) - 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("w");
        return Integer.parseInt(sdf.format(c.getTime()));
	}
	
	/**
     * 得到日期（月/日）
     * 
     * @param numweek
     *          得到当前的周数
     * @param dayOfWeek
     * 			星期几
     */
	public static String getDateViaNumWeek(int numweek, int dayOfWeek) 
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.WEEK_OF_YEAR, numweek);
		int num = cal.get(Calendar.MONTH) + 1;

		String date = (num < 10) ? "0" + num : "" + num;
		date += "/";
		num = cal.get(Calendar.DAY_OF_MONTH);
		date += (num < 10) ? "0" + num : "" + num;
		return date;
	}
	
	/**
     * 得到公元多少年
     * 
     */
	public static int getYear() 
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
	
	/**
     * 星期几
     * 
     */
	public static int getDayOfWeek() 
	{
		Calendar c = Calendar.getInstance();
		int xq = c.get(Calendar.DAY_OF_WEEK);
		return xq;
	}
}
