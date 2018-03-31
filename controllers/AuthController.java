package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.Device;
import com.inspur.icity.logic.cust.service.AccesstokenService;
import com.inspur.icity.logic.cust.service.DeviceService;
import com.inspur.icity.web.utils.Config;


@Controller
public class AuthController extends BaseAuthController {

    Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SESSION_ATTRIBUTE_ACCESS_TOKEN = "User.accessToken";
    public static final String SESSION_ATTRIBUTE_LOGIN_USER = "User.loginUser";

    public static final String TOKEN_SCOPE_GUEST = "guest";
    public static final String TOKEN_SCOPE_LOGINUSER = "login";
    
    public static final String appVersion = Config.getValue("version");

    @Autowired
    DeviceService deviceService;

    @Autowired
    AccesstokenService accessTokenService;

    @ResponseBody
    @RequestMapping(value = "/makeAccessToken", params = {"deviceToken","pushToken","os","model","osVersion","appVersion",}, method = {RequestMethod.POST})
    public Object makeAccessToken(String deviceToken,String pushToken, String os, String model, String osVersion, String appVersion){
    	logger.info("--------------makeAccessToken(start)-------------|"+"deviceToken:"+deviceToken+"pushToken:"+pushToken+"os:"+os+"model:"+model+"osVersion:"+osVersion+"appVersion:"+appVersion);
    	if("140fe1da9e9ad1c362d".equals(pushToken)){
			logger.error("--------------checkToken(恶意攻击)-------------|pushToken:"+pushToken);
			return null;
		}
    	getSession().removeAttribute(SESSION_ATTRIBUTE_ACCESS_TOKEN);
        getSession().removeAttribute(SESSION_ATTRIBUTE_LOGIN_USER);
        Device device = deviceService.getByDeviceToken(deviceToken);
        if(device == null) {
        	device = new Device();
            device.setDeviceToken(deviceToken);
            device.setPushToken(pushToken);
            device.setOs(os);
            device.setModel(model);
            device.setOsVersion(osVersion);
            device.setAppVersion(appVersion);
            device.setDeviceCityCode("");
            deviceService.add(device);
            logger.info("--------------makeAccessToken(当前deviceToken:"+deviceToken+",数据库中无存在，重新将其增加至数据库");
        } else {
            if(!StringUtils.defaultString(model).equals(StringUtils.defaultString(device.getModel()))
                    || !StringUtils.defaultString(osVersion).equals(StringUtils.defaultString(device.getOsVersion()))
                    || !StringUtils.defaultString(appVersion).equals(StringUtils.defaultString(device.getAppVersion()))
                    || !StringUtils.defaultString(pushToken).equals(StringUtils.defaultString(device.getPushToken()))) {
                device.setModel(model);
                device.setOsVersion(osVersion);
                device.setAppVersion(appVersion);
                device.setPushToken(pushToken);
                deviceService.update(device);
            logger.info("--------------makeAccessToken(当前deviceToken:"+deviceToken+",开始更新deviceToken信息");
            }
        }
        logger.debug("device:"+device.getId());
        accessTokenService.removeByDeviceId(device.getId());
        Accesstoken accessToken = new Accesstoken();
        accessToken.setAccessToken(accessTokenService.generateToken());
        accessToken.setDeviceId(device.getId());
        accessToken.setScope(TOKEN_SCOPE_GUEST);
        accessTokenService.add(accessToken);
        accessToken = accessTokenService.get(accessToken.getId());
        logger.debug("accessToken.getId():"+accessToken.getId());
        Map<String,Object> result = Maps.newHashMap();
        result.put("access_token", accessToken.getAccessToken());
        result.put("expires_in", accessToken.getExpiresTime());
        result.put("scope", accessToken.getScope());
        logger.debug("result:"+result.toString());
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/checkAccessToken", params = {"token"}, method = {RequestMethod.POST})
    public Object checkAccessToken(String token){
    	logger.info("--------------checkAccessToken(start)-------------|fromModule:AuthController|interfaceInfo:校验access_token|"+"token:"+token);
        Accesstoken accessToken  = accessTokenService.getByAccessToken(token);
        Map<String,Object> result = Maps.newHashMap();
        if(accessToken != null && accessToken.getExpiresTime().getTime() > new Date().getTime()){
            result.put("checkResult", "ok");
        }else{
            result.put("checkResult", "ng");
        }
        logger.info("--------------checkAccessToken(end)-------------|fromModule:AuthController|interfaceInfo:校验access_token|"+"token:"+token+"|return:"+result);
        return result;
    }
    /**
     * 检查安全认证信息完整性
     * @param jsonTokens
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkToken", method = {RequestMethod.POST})
    public Object checkToken(String jsonTokens){
    	//logger.info("--------------checkToken(start)-------------"+"|"+"fromModule:AuthController"+"|"+"interfaceInfo:检查安全认证信息完整性"+"|"+"jsonTokens:"+jsonTokens);
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		JsonResultModel model = getJsonResultModel();
		JSONObject jsonObject = JsonUtil.strToJson((Object)jsonTokens);
		if(!BeanUtil.isNullString(jsonTokens)){
    		if(jsonObject.has("accessToken")&&jsonObject.has("deviceToken")&&jsonObject.has("pushToken")
    		   &&jsonObject.has("os")&&jsonObject.has("model")&&jsonObject.has("osVersion")&&jsonObject.has("appVersion")
    		   &&jsonObject.has("type")){
    			//logger.info("--------------checkToken(开始解析参数值)-------------|"+"accessToken:"+jsonObject.getString("accessToken")+
    			//		"|"+"deviceToken:"+jsonObject.getString("deviceToken")+"|pushToken:"+jsonObject.getString("pushToken"));
    			String pushToken = jsonObject.getString("pushToken");
    			if("140fe1da9e9ad1c362d".equals(pushToken)){
    				//logger.error("--------------checkToken(恶意攻击)-------------|jsonTokens:"+jsonTokens);
    				model.setCode("0200");
    				model.setError("参数未知异常");
    				model.setResult(list);
    				model.setMessage("服务报错了！");
    				model.setState("0");
    				return model;
    			}
    			Accesstoken aT = accessTokenService.getByAccessToken(jsonObject.getString("accessToken"));
    			Device dT = deviceService.getByDeviceToken(jsonObject.getString("deviceToken"));
    			logger.info("--------------checkToken(开始将token在数据库中进行校验)-------------");
    			if(aT == null || aT.getExpiresTime().getTime() <= new Date().getTime()||dT==null||jsonObject.getString("type").equalsIgnoreCase("off")){
    				logger.info("--------------checkToken(token信息在数据库中不存在，开始调用makeAccessToken重新生成)-------------");
    				try {
						@SuppressWarnings("unchecked")
						Map<String,Object> result = (Map<String, Object>) makeAccessToken(jsonObject.getString("deviceToken")
								,jsonObject.getString("pushToken"),jsonObject.getString("os"),jsonObject.getString("model")
								,jsonObject.getString("osVersion"),jsonObject.getString("appVersion"));
						result.put("device_token", jsonObject.getString("deviceToken"));
						list.add(result);
						logger.info("--------------checkToken(重新生成token信息)-------------|"+"access_token:"+result.get("access_token")
								+"|expiresTime:"+result.get("expires_in")+"|scope:"+result.get("scope")+"|device_token:"+result.get("device_token"));
						model.setCode("0000");
						model.setError("");
						model.setResult(list);
						model.setMessage("安全认证信息校验完成");
						model.setState("1");
						return model;
					} catch (Exception e) {
						logger.error("--------------checkToken(调用makeAccessToken报错)-------------|Except:"+e.toString());
						model.setCode("0100");
						model.setError(e.toString());
						model.setResult(list);
						model.setMessage("服务报错了！");
						model.setState("0");
						return model;
					}
    			}else{
    				logger.info("--------------checkToken(token信息在数据库中均存在，直接返回信息)-------------");
    				Map<String,Object> result = Maps.newHashMap();
    				result.put("access_token", aT.getAccessToken());
    		        result.put("expires_in", aT.getExpiresTime());
    		        result.put("scope", aT.getScope());
    		        result.put("device_token", jsonObject.getString("deviceToken"));
    		        logger.info("--------------checkToken(重新生成token信息)-------------|"+"access_token:"+result.get("access_token")
							+"|expiresTime:"+result.get("expires_in")+"|scope:"+result.get("scope")+"|device_token:"+result.get("device_token"));
    		        list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("安全信息校验完成");
					model.setState("1");
					return model;
    			}
    		}else{
    			logger.error("--------------checkToken(参数缺失)-------------|jsonTokens:"+jsonTokens);
    			model.setCode("0203");
				model.setError("参数缺失");
				model.setResult(list);
				model.setMessage("服务报错了！");
				model.setState("0");
				return model;
    		}
	    	
    	}else{
    		logger.error("--------------checkToken(参数未知异常)-------------|jsonTokens:"+jsonTokens);
    		model.setCode("0200");
			model.setError("参数未知异常");
			model.setResult(list);
			model.setMessage("服务报错了！");
			model.setState("0");
			return model;
    	}
	}
    
    @ResponseBody
    @RequestMapping(value = "/registerPushToken", params = {"push_token","os"}, method = {RequestMethod.POST})
    public Object registerPushToken(String push_token,
                             String os){
    	logger.info("--------------registerPushToken(start)-------------"+"|"+"fromModule:AuthController"+"|"+"interfaceInfo:注册pushToken"+"|"+"push_token:"+push_token+"|os:"+os);
        Device device = deviceService.get(getDeviceId());
        device.setPushToken(push_token);
        device.setOs(os);
        deviceService.update(device);

        Map<String,Object> result = Maps.newHashMap();
        result.put("registerPushToken", "success");
        return result;
    }
    /**
     * 服务中心对外发布权限认证信息
     * @param access_token
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getServiceCenterInfo",method = {RequestMethod.POST})
    public Object getServiceCenterInfo(){
    	logger.info("--------------getServiceCenterInfo(start)-------------"+"|"+"fromModule:AuthController"+"|"+"interfaceInfo:服务中心对外发布权限认证信息"+"|");
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = new JsonResultModel();
		if(!BeanUtil.isNullString(getRequest().getHeader("access_token"))){
			Customer  customer =null;
			HttpSession session = getSession();
			logger.info("-------------getServiceCenterInfo获取的session为:"+session);
			Accesstoken accessToken = (Accesstoken)session.getAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
			//将应用中心传递的token与服务中心的token进行比较
			if(accessToken!=null&&accessToken.getAccessToken().equalsIgnoreCase(getRequest().getHeader("access_token"))){
				Map<String,Object> map = Maps.newHashMap();
				//判断该用户是否为登录用户 
				if(AuthController.TOKEN_SCOPE_LOGINUSER.equals(accessToken.getScope())){
					  customer = getLoginUser();
				}
				map.put("customer", customer);
				map.put("accessToken", accessToken);
				logger.info("----------------map:" + map.toString());
				list.add(map);
				model.setCode("0000");
		        model.setError("");
		        model.setResult(list);
		        model.setMessage("调用成功");
		        model.setState("1");
			}else{
				logger.error("------------服务中心权限认证失败--------------");
				model.setCode("0500");
		        model.setError("");
		        model.setResult(list);
		        model.setMessage("服务中心权限认证失败");
		        model.setState("0");
			}
		}else{
			logger.error("------------access_token 参数缺失--------------");
			model.setCode("0203");
	        model.setError("");
	        model.setResult(list);
	        model.setMessage("access_token 参数缺失");
	        model.setState("0");
		}
        return model;
    }
    /**
     * 更新cust_device表中的pushToken
     * @param deviceToken,pushToken
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updatePushToken", params = {"deviceToken","pushToken"},method = {RequestMethod.POST})
    public Object updatePushToken(String deviceToken, String pushToken){
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
    	JsonResultModel model = getJsonResultModel();
    	if(StringUtils.defaultString(deviceToken) == null || StringUtils.defaultString(pushToken) == null){
    		logger.info("--------------参数输入不完整-------------|");
    		model.setCode("0203");
	        model.setError("");
	        model.setResult(list);
	        model.setMessage("参数缺失");
	        model.setState("0");
	        return model;
    	}
    	logger.info("--------------updatePushToken(start)-------------|" + "deviceToken:" + deviceToken );
    	Device device = deviceService.getByDeviceToken(deviceToken);
    	device.setPushToken(pushToken);
    	deviceService.update(device);
    	logger.info("--------------updatePushToken(当前pushToken:"+pushToken+",开始更新pushToken信息");
    	model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("pushToken信息更新完成");
		model.setState("1");
    	return model;
    }
    
    
}
