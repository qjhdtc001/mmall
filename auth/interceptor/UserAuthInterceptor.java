package com.inspur.icity.web.auth.interceptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.RedisUtil;
import com.inspur.icity.logic.app.model.CustomerAction;
import com.inspur.icity.logic.app.service.CustomerActionService;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.Device;
import com.inspur.icity.logic.cust.service.AccesstokenService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.DeviceService;
import com.inspur.icity.web.controllers.AuthController;
import com.inspur.icity.web.utils.Forbidener;

import redis.clients.jedis.Jedis;

/**
 * 开发用户拦截器，默认设置一个登陆用户用于开发
 */
public class UserAuthInterceptor extends HandlerInterceptorAdapter {
    Logger logger = LoggerFactory.getLogger(getClass());
    public static final String ACCESS_TOKEN = "access_token";
    public static final String VERSION = "version";
    public static final String BUILD = "build";
    public static final String PUSHTOKEN = "pushToken";
    public static final String CITYCODE = "cityCode";
    public static final String DEVICETOKEN = "deviceToken";
    public static final String INVITECODE = "inviteCode";
    private static final String KEY_LIMIT_PREFIX = "rateLimit_";
    private static final String KEY_BAN_PREFIX = "rateBan_";
    Map<String,Map<String,LocalDateTime>> accessControl = Maps.newConcurrentMap();
    List<String> accessPaths = Lists.newArrayList();
    @Autowired
    AccesstokenService accessTokenService;
    @Autowired
    CustomerService customerService;
    @Autowired
    CustomerActionService customerActionService;
    @Autowired
    DeviceService deviceService;
    
    public UserAuthInterceptor(){
        accessPaths.add("makeAccessToken");
        accessPaths.add("checkAccessToken");
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        //获取调用方IP
//        String ip = request.getRemoteAddr();
//        String ip = request.getHeader("X-Real-IP");
//        logger.info("调用者IP："+ ip);
//        //限制访问次数
//        Forbidener fb = Forbidener.getInstance();
//        if(!fb.check(ip)){
//        	throw new ApplicationException(500,"当前账号异常，请联系管理员处理");
//        }
        //用户通过爱城市网app注册、登录时，每分钟每台设备请求注册（Native）或登录接口不得超过2次，否则限制10分钟后才能访问。
        if(uri.endsWith("passWordRegister") || uri.endsWith("quicklyLogin") || uri.endsWith("deviceToken")){
        	//获取请求参数deviceToken
        	String deviceToken = request.getParameter(DEVICETOKEN);
        	logger.info("-------------passWordRegister|quicklyLogin|deviceToken------------"+deviceToken);
        	
        	Forbidener forbidener = Forbidener.getInstance();
        	forbidener.setMaxTime(2);
        	boolean ch = forbidener.check(KEY_LIMIT_PREFIX+deviceToken);
        	if(ch == false){
        		throw new ApplicationException(704,"操作过频，请10分钟后再试。");
        	}
//        	//先判断该deviceToken对应的请求有没有被禁止
//        	if(RedisUtil.exists(KEY_BAN_PREFIX+deviceToken)){//key存在，说明请求被禁止
//        		throw new ApplicationException(704,"系统繁忙，请稍后再试。");
//        	}
//        	//限制访问频率key
//        	Long times = RedisUtil.incr(KEY_LIMIT_PREFIX+deviceToken);
//        	logger.info("-------------passWordRegister|quicklyLogin|deviceToken------------"+KEY_LIMIT_PREFIX+deviceToken+" 访问次数："+times);
//        	System.out.println(KEY_LIMIT_PREFIX+deviceToken+" 访问次数："+times);
//            if(times == 1L){
//            	RedisUtil.expire(KEY_LIMIT_PREFIX+deviceToken, 60);
//            }
//            if(times > 2){//超过访问频率（1分钟超过2次）
////            	Long count = RedisUtil.incr(KEY_BAN_PREFIX+deviceToken);
////        		if(count == 1L){
//            	RedisUtil.set(KEY_BAN_PREFIX+deviceToken, "success");
//            	RedisUtil.expire(KEY_BAN_PREFIX+deviceToken, 600);//禁止访问10分钟
////        		}
//        		throw new ApplicationException(704,"系统繁忙，请稍后再试。");
//            }
        }
        //老用户分享出的h5界面的注册接口（h5）每分钟被调用不得超过5次，否则限制10分钟后才能访问。
//        if(uri.endsWith("h5PageRegister")){
//        	//获取请求参数deviceToken
//        	String inviteCode = request.getParameter(INVITECODE);
//        	logger.info("-------------h5PageRegister------------"+inviteCode);
////        	//先判断该deviceToken对应的请求有没有被禁止
////        	if(RedisUtil.exists(KEY_BAN_PREFIX+inviteCode)){//key存在，说明请求被禁止
////        		throw new ApplicationException(704,"系统繁忙，请稍后再试。");
////        	}
////        	//限制访问频率key
////        	Long times = RedisUtil.incr(KEY_LIMIT_PREFIX+inviteCode);
////        	logger.info("-------------h5PageRegister------------"+KEY_LIMIT_PREFIX+inviteCode+" 访问次数："+times);
////        	System.out.println(KEY_LIMIT_PREFIX+inviteCode+" 访问次数："+times);
////            if(times == 1L){
////            	RedisUtil.expire(KEY_LIMIT_PREFIX+inviteCode, 60);
////            }
////            if(times > 5){//超过访问频率（1分钟超过5次）
//////            	Long count = RedisUtil.incr(KEY_BAN_PREFIX+inviteCode);
////            	RedisUtil.set(KEY_BAN_PREFIX+inviteCode, "success");
////            	RedisUtil.expire(KEY_BAN_PREFIX+inviteCode, 60);//禁止访问10分钟
////        		throw new ApplicationException(704,"系统繁忙，请稍后再试。");
////            }
//        	Forbidener forbidener = Forbidener.getInstance();
//        	forbidener.setMaxTime(5);
//        	boolean ch = forbidener.check(KEY_LIMIT_PREFIX+inviteCode);
//        	if(ch == false){
//				logger.error("--------------h5PageRegister(邀请码存在恶意注册)-------------|inviteCode:" + inviteCode);
//				throw new ApplicationException(704,"操作过频，请10分钟后再试。");
//        	}
//        }
        //获取请求access_token
        String access_tokenReq = request.getHeader(ACCESS_TOKEN);
        boolean limit = limitReq(access_tokenReq);
        if(limit){
        	return false;
        }
        ClientInfo clientInfo = new ClientInfo();
        logger.info("调用URL:"+uri);
        //访问控制
        if(uri.endsWith("makeAccessToken") || uri.endsWith("checkAccessToken") || uri.endsWith("activate")|| uri.endsWith(".html")|| uri.endsWith(".css")|| uri.endsWith(".js")
                || uri.endsWith(".png") || uri.endsWith(".map") || uri.endsWith(".ico") || uri.endsWith("govDetails.html") || uri.endsWith("govDetail")|| uri.endsWith("newsDetails.html") || uri.endsWith("newsDetails")
                || uri.endsWith("govDetails-hall.html") || uri.endsWith("govDetails-hall") || uri.endsWith("govDetails-share.html") || uri.endsWith("govDetails-share")
                || uri.equalsIgnoreCase("/")||uri.endsWith("checkToken")||uri.endsWith("getSystemTime")||uri.endsWith("updatePushToken") ||uri.endsWith("getH5Code")||uri.endsWith("h5PageRegister")){
            return true;
        }
        if(uri.endsWith("findAdvert")){
        	String token = request.getHeader(ACCESS_TOKEN);
        	HttpSession session = request.getSession();
            Accesstoken accessToken = (Accesstoken)session.getAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
            if(accessToken == null || !accessToken.getAccessToken().equals(token)){
                accessToken = accessTokenService.getByAccessToken(token);
                session.setAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN, accessToken);
            }
            logger.info("拦截器从数据库及session获取的token:"+accessToken);
        	//获取cityCode信息并放到session里面
    	    if((!Strings.isNullOrEmpty(request.getHeader(CITYCODE)))){
    	    	 clientInfo.setCityCode(request.getHeader(CITYCODE));
    	    	 session.setAttribute("clientInfo", clientInfo);
    	    	 logger.info("从header中获取cityCode信息:"+request.getHeader(CITYCODE));
    	    }
    	    return true;
        }
    	if((!Strings.isNullOrEmpty(request.getHeader(PUSHTOKEN)))){
   	    	 logger.info("从header中获取pushToken信息:"+request.getHeader(PUSHTOKEN));
   	    	 if("140fe1da9e9ad1c362d".equals(request.getHeader(PUSHTOKEN))){
   	    		 return false;
   	    	 }
   	    }
        //拦截news/id  以数字结尾的uri
        Pattern pattern = Pattern.compile("/shared/\\d+$");
        Matcher matcher = pattern.matcher(uri);
        if(matcher.find()){
            return true;
        }
        //拦截news/id  以数字结尾的uri
        pattern = Pattern.compile("/hall/\\d+$");
        matcher = pattern.matcher(uri);
        if(matcher.find()){
            return true;
        }
        String token = request.getHeader(ACCESS_TOKEN);
        logger.info("前端传输的accessToken:"+token);
        if(Strings.isNullOrEmpty(token)){
            throw new ApplicationException(701,"安全认证信息缺失");//No Token
        }
        HttpSession session = request.getSession();
        Accesstoken accessToken = (Accesstoken)session.getAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
        if(accessToken == null || !accessToken.getAccessToken().equals(token)){
            accessToken = accessTokenService.getByAccessToken(token);
            session.setAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN, accessToken);
        }
        logger.info("拦截器从数据库及session获取的token:"+accessToken);
        if(accessToken == null){
            throw new ApplicationException(702,"安全信息未认证通过");//"Invalid token"
        }
        //获取版本信息并放到session里面
	    if((!Strings.isNullOrEmpty(request.getHeader(VERSION))) && (!Strings.isNullOrEmpty(request.getHeader(BUILD)))){
	    	 clientInfo.setVersion(request.getHeader(VERSION));
	         clientInfo.setBuild(request.getHeader(BUILD));
	         session.setAttribute("clientInfo", clientInfo);
	         logger.info("从header中获取版本信息:"+clientInfo.getVersion());
	    }
	    //获取pushToken信息并放到session里面
	    if((!Strings.isNullOrEmpty(request.getHeader(PUSHTOKEN)))){
	    	 session.setAttribute(PUSHTOKEN, request.getHeader(PUSHTOKEN).toString());
	    	 logger.info("从header中获取pushToken信息:"+session.getAttribute(PUSHTOKEN));
	    }
	    //获取cityCode信息并放到session里面
	    if((!Strings.isNullOrEmpty(request.getHeader(CITYCODE)))){
	    	 clientInfo.setCityCode(request.getHeader(CITYCODE));
	    	 session.setAttribute("clientInfo", clientInfo);
	    	 logger.info("从header中获取cityCode信息:"+request.getHeader(CITYCODE));
	    }
	    Date expiresTime = accessToken.getExpiresTime();
        logger.info("accessToken:"+accessToken.getAccessToken()+"token失效日期:"+expiresTime+"当前系统日期:"+new Date().getTime());
        if(expiresTime.getTime() < new Date().getTime()){
            throw new ApplicationException(703,"安全认证信息已失效");//"Expired token"
        }
        logger.info("开始判断当前accessToken是否为:"+accessToken.getAccessToken()+"token类别:"+AuthController.TOKEN_SCOPE_LOGINUSER);
        if(AuthController.TOKEN_SCOPE_LOGINUSER.equals(accessToken.getScope())){
            //Customer customer = (Customer)session.getAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
            //if(customer == null){
        	Customer  customer = customerService.getByAccessToken(accessToken.getAccessToken());
                session.setAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER, customer);
            //}
            if(customer == null){
                throw new ApplicationException(704,"账户信息异常，请联系管理员处理");//"Invalid token"
            }
            if(customer != null&&customer.getDisabled()==2l){
            	throw new ApplicationException(500,"当前账号异常，请联系管理员处理");
            } 
            //记录用户使用记录
            //创建者：王建法
            //涉及数据库表：cust_customer_action
            try{
            	logger.info("---------------开始记录登录用户行为----------------");
            	CustomerAction customerAction = new CustomerAction();
            	customerAction.setCustId(customer.getId());
            	customerAction.setPhone(customer.getMobilePhone());
            	customerAction.setUri(uri);
            	customerActionService.add(customerAction);
            }catch(Exception e){
            	logger.error("-----------添加登录用户信息至数据库出现异常:" + e.toString());
            	throw new ApplicationException(0100,"系统未知异常---from:UserAuthInterceptor");
            }
        }
        return true;
    }
	private boolean limitReq(String access_tokenReq) {
		//根据access_token获取deviceId
		Long deviceId = 0L;
		Accesstoken accesstoken = accessTokenService.getByAccessToken(access_tokenReq);
		if(accesstoken != null){
			deviceId = accesstoken.getDeviceId();
			//根据deviceId查询push_token
			Device device = deviceService.get(deviceId);
			String pushToken = device.getPushToken();
			logger.info("-------------limitReq-----------当前pushToken:"+pushToken);
			//获取当前时间前1分钟该pushToken的数据个数
			int count = deviceService.getByCurrentOneMin(pushToken);
			logger.info("-------------limitReq-----------当前时间前1分钟该pushToken的数据个数:"+count);
			if(count > 3){
				return true;
			}
		}
		return false;
	}
   
}
