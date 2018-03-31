package com.inspur.icity.web.utils;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inspur.icity.core.utils.Base64Util;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @Description 极光定时推送
 * @author MengKe
 * @time 2017年2月20日 下午3:20:20
 *
 */
public class JPushSchedule {

	static Logger logger = LoggerFactory.getLogger(JPushSchedule.class);
	
	//极光appkey
	private static final String appKey = Config.getValue("jpush_appKey");
	//极光masterSecret
	private static final String masterSecret = Config.getValue("jpush_masterSecret");
	
	public static String pushBySchedule(String name, String enabled, String trigger, String push){
		
		//返回结果
		String resultStr = "";
		// 极光认证信息
		String base64_auth_string = "";
		if (!"".equals(appKey) && !"".equals(masterSecret)) {
		    base64_auth_string = "Basic " + Base64Util.encodeBase64(appKey + ":" + masterSecret);
		}
		try {
		    if (!(BeanUtil.isNullString(name)) && !(BeanUtil.isNullString(enabled)) && !(BeanUtil.isNullString(trigger))
			    && !(BeanUtil.isNullString(push))) {
				String response = null;
				Boolean Bl = new Boolean(enabled);
				boolean bl_enabled = Bl.booleanValue();
				// 重置参数
				JSONObject jsonparam = new JSONObject();
				jsonparam.accumulate("name", name);
				jsonparam.accumulate("enabled", bl_enabled);
				jsonparam.accumulate("trigger", trigger);
				jsonparam.accumulate("push", JSONObject.fromObject(push));
				logger.info("--------------scheduleNotification(重组第三方接口参数)-------------" + "|" + "paramMap:" + jsonparam.toString());
				response = HttpUtil.post("https://api.jpush.cn/v3/schedules", jsonparam.toString(), "Authorization", base64_auth_string);
				logger.info("--------------scheduleNotification(调用第三方接口)-------------" + "|" + "response:" + response);
				JSONObject jsonRet = JsonUtil.strToJson(response);
				// 调用成功
				if (jsonRet.has("schedule_id")) {   
					resultStr = response;
				}else {
					logger.info("--------------scheduleNotification(无法连接服务)-------------" + "|" + "name:" + name + "|" + "enabled:" + enabled + "|" + "trigger"
							+ "|" + "push" + push);
				}
		    } else {
				logger.info("--------------scheduleNotification(前端传入参数为空)-------------" + "|" + "name:" + name + "|" + "enabled:" + enabled + "|" + "trigger"
					+ "|" + "push" + push);
		    }

		} catch (Exception e) {
		    logger.info("--------------scheduleNotification(error)-------------" + "|" + "fromModule:NotificationController" + "|"
			    + "interfaceName:scheduleNotification" + "|" + "error:" + e.toString());
		    e.printStackTrace();
		}
		return resultStr;
	}
}
