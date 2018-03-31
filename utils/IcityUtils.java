package com.inspur.icity.web.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by wujiyue on 2016/4/29.
 */
public class IcityUtils {

    //获取距离
    public static String getDistance(Double distance) {
        String result = "";
        if (distance > 1.0) {
            int len = distance.toString().substring(distance.toString().lastIndexOf(".") + 1).length();
            if (len > 1) {
                result = String.format("%.1f", distance) + "km";
            }
        } else {
            result = Math.round(distance * 1000) + "m";
        }
        return result;
    }

    public static String getDistanceTwo(Double distance) {
        String result = "";
        if (distance > 1.0) {
            int len = distance.toString().substring(distance.toString().lastIndexOf(".") + 1).length();
            if (len > 1) {
                result = String.format("%.1f", distance) + "km";
            }
        } else {
            result = Math.round(distance) + "m";
        }
        return result;
    }
    /**
     *  Date 转  LocalDateTime 
     * @param date
     * @return LocalDateTime
     */
    public static LocalDateTime UDateToLocalDateTime(Date date){
    	  Instant instant = date.toInstant();
		    ZoneId zone = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(instant, zone);
    	
    }
    /**
     *  LocalDateTime 转  Date 
     * @param localDateTime
     * @return Date
     */
    public static Date ULocalDateTimeToDate(LocalDateTime localDateTime){
    	 ZoneId zone = ZoneId.systemDefault();
 	    Instant instant = localDateTime.atZone(zone).toInstant();
    	return Date.from(instant);
    	
    }

}
