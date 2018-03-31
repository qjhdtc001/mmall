package com.inspur.icity.web.controllers;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.web.utils.Config;

public class BaseAuthController extends BaseController {

    public static final String IMAGE_BASE_DIR = "/MOBILE";
    public static  JsonResultModel jsonResultModel=null;
    public static final String VERIFYCODE = "Register.verificationCode";//手机验证码
    public static final String VERIFYMOBILEPHONE = "Register.verificationMobilePhone";
    public static final String VERIFYCODE_EXPIRESIN = "Register.verificationCodeExpiresIn";//验证码过期时间
    public static final String VERIFYCODE_RESENDTIME = "Register.verificationCodeReSendTime";//重新获取验证码的最短时间
    public static final String VERIFYCODESUCCESS = "Register.verificationCodeSuccess";//验证码登录成功标识

    public static final String OS_ANDROID =  "ANDROID";
    public static final String OS_IOS = "IOS";

    public static final String ANDROID_HDPI = "HDPI";
    public static final String ANDROID_XHDPI = "XHDPI";
    public static final String ANDROID_XXHDPI = "XXHDPI";
    public static final String IOS_2X = "2X";
    public static final String IOS_3X = "3X";

    public static final String RESOLUTION_IPHONE6_PLUS = "2208*1242";
  
    public JsonResultModel getJsonResultModel(){
    		JsonResultModel model = new JsonResultModel();
            Map<String,Object> serverMap = Maps.newHashMap();
            Map<String,Object> markInfo = Maps.newHashMap();
            serverMap.put("version", Config.getValue("version"));
            serverMap.put("build",Config.getValue("build"));
            markInfo.put("isPop", 0);
            markInfo.put("mark", 0);
            try {
    			serverMap.put("time", DateUtil.getNow("yyyy-MM-dd HH:mm:ss"));
    		} catch (Exception e) {
    			serverMap.put("time", "");
    		}
            model.setServer(serverMap);
            model.setMarkInfo(markInfo);
            
    	return model;
    }
    public Customer getLoginUser(){
        return (Customer)getSession().getAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
    }
    public Long getLoginUserId(){
        Customer customer = getLoginUser();
        if(customer == null){
            return  null;
        }
        return customer.getId();
    }
    public String  getPushToken(){
        return (String) getRequest().getSession().getAttribute("pushToken");
    }
    
    public Accesstoken getAccessToken(){
    	return (Accesstoken) getRequest().getSession().getAttribute("User.accessToken");
    }
    
    public String getCityCode(){
    	HttpSession session = getSession();
    	String cityCode = null;
		ClientInfo clientInfo = (ClientInfo) session.getAttribute("clientInfo");
		if(!BeanUtil.isNullString(clientInfo.getCityCode())){
			cityCode = clientInfo.getCityCode();
		}
    	return cityCode;
    }
    public Integer getVersion(){
    	HttpSession session = getSession();
    	Integer version = null;
		ClientInfo clientInfo = (ClientInfo) session.getAttribute("clientInfo");
		if(!BeanUtil.isNullString(clientInfo.getVersion())){
			 version = Integer.valueOf(clientInfo.getVersion().replace(".", "")).intValue();
		}
    	return version;
    }
	

    public Long getDeviceId(){
        Accesstoken accessToken = (Accesstoken)getSession().getAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
        if(accessToken == null){
            return null;
        }
        return accessToken.getDeviceId();
    }

    /**
     * 根据用户手机的操作系统及DPI或者分辨率信息，获取该机型对应的图片尺寸前缀。
     * 如 : iPhone6Plus - /MOBILE/IOS/3X
     * 如 : MeizuMX4Pro - /MOBILE/ANDROID/XXHDPI
     * @return
     */
    public String getImagePrefix(String imageType) {
        String imagePrefix = "";

        String os = getRequest().getHeader("os");
        if(StringUtils.isEmpty(os)) {
            return imagePrefix;
        }
        if(OS_ANDROID.toLowerCase().equals(os.toLowerCase())) {
            imagePrefix = IMAGE_BASE_DIR + "/" + OS_ANDROID + "/" + StringUtils.defaultString(imageType);
            int dpi = getRequest().getIntHeader("dpi");
            if (dpi <= 240) {
                imagePrefix += "/" + ANDROID_HDPI;
            } else if (dpi <= 320) {
                imagePrefix += "/" + ANDROID_XHDPI;
            } else if (dpi <= 480) {
                imagePrefix += "/" + ANDROID_XXHDPI;
            }
        } else if (OS_IOS.toLowerCase().equals(os.toLowerCase())) {
            imagePrefix = IMAGE_BASE_DIR + "/" + OS_IOS + "/" + StringUtils.defaultString(imageType);
            String resolution = getRequest().getHeader("resolution");
            if(RESOLUTION_IPHONE6_PLUS.equals(resolution)) {
                imagePrefix += "/" + IOS_3X;
            } else {
                imagePrefix += "/" + IOS_2X;
            }
        }

        return imagePrefix;
    }


}
