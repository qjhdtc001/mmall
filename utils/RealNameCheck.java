package com.inspur.icity.web.utils;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.HttpUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName RealNameCheck
 * @Description 银联实名认证工具类
 * @author meng-ke
 * @date 2017年6月8日 下午2:43:17
 */
public class RealNameCheck {
	
	Logger log = LoggerFactory.getLogger(getClass());

    public static RealNameCheck realNameCheck;  
    public static RealNameCheck getInstance(){  
        if(realNameCheck == null){  
        	realNameCheck = new RealNameCheck();  
        }  
        return realNameCheck;  
    }  

    /**
     * 
     * @Title getAccessToken
     * @Description 银联获取接入令牌
     * @return String
     * @author meng-ke
     * @date 2017年6月8日下午3:12:08
     */
	public String getAccessToken(){
		try{
			JSONObject params = new JSONObject();
			params.accumulate("appId", Constants.UMS_APPID);
			//时间戳
			String timestamp = DateUtil.formatDate(new Date(), DateUtil.YMDHMS);
			params.accumulate("timestamp", timestamp);
			//随机数
			String nonce = UUID.randomUUID().toString();
			params.accumulate("nonce", nonce);
			//签名
			String signature = DigestUtils.shaHex(Constants.UMS_APPID+timestamp+nonce+Constants.UMS_APPKEY);
			params.accumulate("signature", signature);
			log.info("---------------银联获取access_token接口参数：" + params.toString());
			String result = HttpUtil.post(Constants.UMS_ACCESSTOKEN_URL, params.toString(), null, null);
			log.info("---------------银联获取access_token接口返回结果：" + result);
			return result;
		}catch(Exception e){
			log.info("---------------银联获取access_token接口出现异常(error)：" + e.toString());
			return "";
		}
	}
	
	/**
	 * 
	 * @Title realNameCHeck
	 * @Description 银联实名认证
	 * @param accessToken
	 * @param map
	 * @return String
	 * @author meng-ke
	 * @date 2017年6月8日下午3:34:58
	 */
	public String realNameCHeck(String accessToken, Map<String, String> map){
		try{
			JSONObject json = JSONObject.fromObject(accessToken);
			String accessTokenStr = json==null?"":json.get("accessToken").toString();
			//证件类型（身份证）
			String certType = Constants.UMS_IDCARD_TYPE;
			//手机号
			String phoneNo = "";
			//证件号码
			String certNo = "";
			//姓名
			String name = "";
			if(map != null){
				phoneNo = map.get("mobile");
				certNo = map.get("idcard");
				name = map.get("realname");
			}
			JSONObject params = new JSONObject();
			params.accumulate("phoneNo", phoneNo);
			params.accumulate("certType", certType);
			params.accumulate("certNo", certNo);
			params.accumulate("name", name);
			JSONObject data = new JSONObject();
			data.accumulate("data", params.toString());
			log.info("---------------银联实名认证接口参数：" + data.toString());
			String result = HttpUtil.post(Constants.UMS_CHECK_URL, data.toString(), "Authorization", accessTokenStr);
			log.info("---------------银联实名认证接口返回结果：" + result);
			return result;
		}catch(Exception e){
			log.info("---------------银联实名认证接口异常(error)：" + e.toString());
			return "";
		}
	}
}
