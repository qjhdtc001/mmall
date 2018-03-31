package com.inspur.icity.web.utils;

import java.util.Date;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Forbidener {  
	Logger log = LoggerFactory.getLogger(getClass());
    private Forbidener(){  
        ipmap = new LRUMap(100000);  
    }  
    public static Forbidener forbidener;  
    public static Forbidener getInstance(){  
        if(forbidener == null){  
            forbidener = new Forbidener();  
        }  
        return forbidener;  
    }  
    LRUMap ipmap = null ;  
    /** 
     * 超过次数封禁 
     */  
    long maxTime = 60;  
    /** 
     * 超过访问时间重新计时 
     */  
    long minInteval = 60*1000*1;  
    /** 
     * 封禁的时间 
     */  
    long forbidenTime = 60*1000*10;  
      
  
    public int getMapSize() {  
        return ipmap.size();  
    }  
  
    public void setMaxTime(int i) {  
        maxTime = i;  
    }  
  
    public void setMinInteval(int i) {  
        minInteval = i;   
    }  
  
    public void setForbidenTime(int i) {  
        forbidenTime = i;  
    }  
  
    public void setMaxMapSize(int i) {  
        ipmap = new LRUMap(i);  
    }  
      
      
    public boolean check(String IP){  
        IPer iper = (IPer)ipmap.get(IP);  
        if(iper == null){  
            iper = new IPer();  
            iper.lastVisiterTime = new Date();  
            iper.visitTimes = 1;  
            ipmap.put(IP, iper);  
            return true;  
        }  
        Date now = new Date();  
        if(iper.isForbiden){  
            if(now.getTime() -  iper.lastVisiterTime.getTime() > forbidenTime){  
                //解封  
                iper.isForbiden = false;  
                iper.visitTimes = 1;  
                iper.lastVisiterTime = now;  
                log.info("unforbiden ip:" + IP);  
                return true;  
            }else{  
                return false;  
            }  
        }  
        if(now.getTime() - iper.lastVisiterTime.getTime() > minInteval){  
            //太久没有访问了,重新计时  
            iper.lastVisiterTime = now;  
            iper.visitTimes = 1;  
            iper.isForbiden = false;  
            log.info("reset ip:" + IP);  
            return true;  
        }else{  
            iper.lastVisiterTime = now;  
            iper.visitTimes = iper.visitTimes +1;  
            if(iper.visitTimes > maxTime){  
                //封禁  
                iper.isForbiden = true;  
                log.info("forbiden ip:" + IP);  
                return false;  
            }else{  
                return true;  
            }  
        }  
          
          
    }  
      
    public static void main(String[] args) throws InterruptedException {  
        Date d1 = new Date();  
        Thread.sleep(1000);  
        Date d2 = new Date();  
        System.out.println(d1.getTime() - d2.getTime());  
    }  
}  