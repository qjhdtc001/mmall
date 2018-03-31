package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.logic.app.model.AppLink;
import com.inspur.icity.logic.app.model.Application;
import com.inspur.icity.logic.app.service.AppLinkService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.news.model.Msg;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.operating.model.Activity;
import com.inspur.icity.logic.operating.model.CustNotification;
import com.inspur.icity.logic.operating.model.Notification;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.logic.operating.service.CustNotificationService;
import com.inspur.icity.logic.operating.service.NotificationService;

/**
 * 消息通知相关接口
 */
@Controller
@RequestMapping(value = "/msg")
public class MessageController extends BaseAuthController{
	Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	AppLinkService appLinkService;
	@Autowired
	ApplicationService applicationService;
	@Autowired
	MsgService msgService;
	@Autowired
	ActivityService activityService;
	@Autowired
	CustNotificationService custNotificationService;
	@Autowired
	NotificationService notificationService;
	/**
     * 通知消息总数
     * @param 
     * @return 消息总数
     */
	@ResponseBody
    @RequestMapping(value = "/getMsgCount", method = RequestMethod.POST)
    public Object getMsgCount(){
		logger.info("--------------getMsgCount(start)---------fromController:MessageController-------------");
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,String>> list = new ArrayList<Map<String,String>>();
    	List<Map<String,Object>> messageList = new ArrayList<Map<String,Object>>();//根据custId 和 deviceId搜寻的list
    	Integer messageNum = 0;//通知消息总数
    	Map<String, String> messageCount = new HashMap<String, String>();//记录通知消息数目的map
    	
    	Long custId = getLoginUserId();
    	Long deviceId = getDeviceId();
    	logger.info("获取登录用户的custId： " + custId + ",未登录用户设备的deviceId：" + deviceId);
    	if(custId != null || deviceId != null){
    		try {
    			messageList = custNotificationService.findByCustOrDeviceId(custId, deviceId);
        		logger.info("------------------------查询完成-------------------------");
        		if(messageList != null){
        			messageNum = messageList.size();
            		//过期检验
            		for(Map<String, Object> message : messageList){
            			Long msgId = Long.valueOf(message.get("msgId").toString());
            			if(expire(msgId)){
            				messageNum = messageNum - 1;
            				logger.info("-----------------------该消息过期，同时更新cust_notification为已读---------------");
            				try {
            					CustNotification custNotification = custNotificationService.get(Long.valueOf(message.get("id").toString()));
            					custNotification.setIsRead(1l);
                				custNotificationService.update(custNotification);
                				logger.info("----------------------更新完成-----------------------");
							} catch (Exception e) {
								// TODO: handle exception
								logger.error("-----------------------更新出现异常--------------------------");
								model.setCode("0100");
						        model.setError("系统未知异常");
						        model.setResult(list);
						        model.setMessage("调用失败");
						        model.setState("0");
						        return model;
							}
            			}else{
            				logger.info("-----------------------该消息未过期---------|msgId：" + msgId);
            			}
            		}
        		}
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("-----------------------查询出现异常--------------------------");
				model.setCode("0100");
		        model.setError("系统未知异常");
		        model.setResult(list);
		        model.setMessage("调用失败");
		        model.setState("0");
		        return model;
			}
    		
    		messageCount.put("messageNum",String.valueOf(messageNum));
    		list.add(messageCount);
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
    	}else{
    		model.setCode("0100");
	        model.setError("系统未知异常");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
    	}
        return model;
    }
	/**
     * 展示不同类型消息
     * @param 
     * @return 不同类型未读消息数量
     */
	@ResponseBody
    @RequestMapping(value = "/showVariousMsg", method = RequestMethod.POST)
	public Object showVariousMsg(){
		logger.info("--------------showVariousMsg(start)---------|fromController:MessageController-------------");
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//放进result中的list
		
		Long custId = getLoginUserId(); 
		Long deviceId = getDeviceId();
		logger.info("----------custId： " + custId + ",deviceId：" + deviceId);
		if(custId == null && deviceId == null){
			model.setCode("0100");
	        model.setError("系统未知异常");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
		
		List<Map<String,Object>> searchList = custNotificationService.searchVariousMsg(custId, deviceId);//搜寻结果
		logger.info("-----------搜寻结果 resultList： " + searchList.size());
		for(Map<String,Object> map : searchList){
			if(BeanUtil.nullValueOf(map.get("open")).equals("operation")){
				map.put("open", "web");
			}
			String[] strArray = new String[4];
			if(map.get("value") == null){
				for(int i = 0; i < 4; i++){
					strArray[i] = "";
				}
			}else{
				strArray = BeanUtil.nullValueOf(map.get("value")).split("\\|");//数组化value中的值
			}
			logger.info("-----------------------value:" + map.get("value"));
			String imgUrl = "/Image/Logo/msg/";
			Map<String, String> m = new HashMap<String, String>(); 
			for(int i = 0;i < strArray.length;i++ ){
				if(!BeanUtil.isNullString(strArray[0])  && strArray[0] != "null"){
					m.put("id", strArray[0]);
				}else{
					m.put("id", "");
				}
				if(!BeanUtil.isNullString(strArray[1]) && strArray[1] != "null"){
					m.put("title", strArray[1]);
				}else{
					m.put("title", "");
				}
				if(!BeanUtil.isNullString(strArray[2]) && strArray[2] != "null"){
					m.put("gotoUrl", strArray[2]);
				}else{
					m.put("gotoUrl", "");
				}
				if(!BeanUtil.isNullString(strArray[3]) && strArray[3] != "null"){
					m.put("code", strArray[3]);
				}else{
					m.put("code", "");
				}
				
				logger.info("---------------第" + (i + 1) + "次执行");
				
			}
			switch (map.get("type").toString()) {
			case "gov":{
				imgUrl = imgUrl + "govImg.png";
				map.put("imageUrl", imgUrl);
				break;
			}
			case "ops":{
				imgUrl = imgUrl + "opsImg.png";
				map.put("imageUrl", imgUrl);
				break;
			}
			case "sys":{
				imgUrl = imgUrl + "sysImg.png";
				map.put("imageUrl", imgUrl);
				break;
			}
			case "pers":{
				imgUrl = imgUrl + "persImg.png";
				map.put("imageUrl", imgUrl);
				break;
			}
			default:
				break;
			}
			map.put("value", m);
		}
		list = searchList;
		model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("调用成功");
		model.setState("1");
		return model;
	}
	/**
     * 展示不同类型消息
     * @param 
     * @return 不同类型未读及已读消息列表
     */
	@ResponseBody
	@RequestMapping(value = "/showMsg",params={"msgType"}, method = RequestMethod.POST)
	public Object showMsg(String msgType){
		logger.info("----------------showMsg(start)----------|fromController:MessageController---|msgType:" + msgType);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//放进result中的list
		Long custId = getLoginUserId();
		Long deviceId = getDeviceId();
		
		Map<String, Object> msgIdMap = new HashMap<String, Object>();//记录所有msgId
		List<Long> msgIdList = new ArrayList<Long>();//记录所有msgId
		logger.info("----------获取登录用户的custId： " + custId + ",未登录用户设备的deviceId：" + deviceId);
		if(custId == null && deviceId == null){
			model.setCode("0100");
	        model.setError("系统未知异常");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
		if(BeanUtil.isNullString(msgType)){
			model.setCode("0203");
	        model.setError("参数缺失");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		resultList = custNotificationService.showMsg(custId, deviceId, msgType, getPageBounds());
		logger.info("-------------------------共获得resultList数目：" + resultList.size());
		for(Map<String,Object> map : resultList){
			Map<String, String> m = new HashMap<String, String>();
			Long msgId = Long.valueOf(map.get("msgId").toString());
			msgIdList.add(msgId);
			logger.info("---------------------msgId:" + msgId + "---|map.get('open'):" + map.get("open"));
			if(expire(msgId)){
				map.put("expire", "1");
			}else {
				map.put("expire", "0");
			}
			/*try {
				custNotificationService.updateIsReadByMsgId(custId, deviceId,Long.valueOf(msgId));
				logger.info("----------------from : showMsg|设置消息已读更新完成---------------");
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("---------------------------更新数据库出现异常------------------------");
				model.setCode("0100");
				model.setError("系统未知异常");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
				return model;
			}*/
			switch (map.get("open").toString()) {
			case "web":{
				Long relatedId = Long.valueOf(map.get("relatedId").toString());
				logger.info("-----------------map.get('open') 对应着 ：" + map.get("open").toString());
				AppLink appLinks = new AppLink();
				if(appLinkService.get(relatedId) == null){//数据库中该条信息不存在
					m.put("id","");
					m.put("title", "");
					m.put("gotoUrl", "");
					m.put("code", "");
					break;
				}
				appLinks = appLinkService.get(relatedId);
				m.put("id",appLinks.getId().toString());
				m.put("title", BeanUtil.nullValueOf(appLinks.getTitle()));
				m.put("gotoUrl", BeanUtil.nullValueOf(appLinks.getGotoUrl()));
				m.put("code", BeanUtil.nullValueOf(appLinks.getCode()));
				logger.info("------------------Map m 的id ： " + m.get("id") + "--|title :" + m.get("title") + "--|gotoUrl :" + m.get("gotoUrl") );
				break;
			}
			case "app":{
				Long relatedId = Long.valueOf(map.get("relatedId").toString());
				logger.info("-----------------map.get('open') 对应着 ：" + map.get("open").toString());
				Application application = new Application();
				if(applicationService.get(relatedId) == null){//数据库中该条信息不存在
					m.put("id","");
					m.put("title", "");
					m.put("gotoUrl", "");
					m.put("code", "");
					//增加"应用是否可分享字段"
					m.put("isShare", "");
					m.put("description", "");
					break;
				}
				application = applicationService.get(relatedId);
				m.put("id",application.getId().toString());
				m.put("title", BeanUtil.nullValueOf(application.getName()));
				m.put("gotoUrl", BeanUtil.nullValueOf(application.getGotoUrl()));
				m.put("code", BeanUtil.nullValueOf(application.getCode()));
				//增加"应用是否可分享字段"
				m.put("isShare", BeanUtil.nullValueOf(application.getIsShare()));
				m.put("description", BeanUtil.nullValueOf(application.getDescription()));
				logger.info("------------------Map m 的id ： " + m.get("id") + "--|title :" + m.get("title") + "--|gotoUrl :" + m.get("url") + "--|code:" + m.get("code"));
				break;
			}
			case "news":{
				Long relatedId = Long.valueOf(map.get("relatedId").toString());
				logger.info("-----------------map.get('open') 对应着 ：" + map.get("open").toString());
				Msg msg = new Msg();
				if(msgService.get(relatedId) == null){//数据库中该条信息不存在
					m.put("id","");
					m.put("title", "");
					m.put("gotoUrl", "");
					m.put("code", "");
					break;
				}
				msg = msgService.get(relatedId);
				m.put("id",BeanUtil.nullValueOf(msg.getId()));
				m.put("title", BeanUtil.nullValueOf(msg.getTitle()));
				m.put("gotoUrl", BeanUtil.nullValueOf(msg.getGotoUrl()));
				m.put("code", "");
				logger.info("------------------Map m 的id ： " + m.get("id"));
				break;
			}
			case "operation":{
				Long relatedId = Long.valueOf(map.get("relatedId").toString());
				logger.info("-----------------map.get('open') 对应着 ：" + map.get("open").toString());
				Activity activity = new Activity();
				if(activityService.get(relatedId) == null){//数据库中该条信息不存在
					m.put("id","");
					m.put("title", "");
					m.put("gotoUrl", "");
					m.put("code", "");
					m.put("isShare", "");
					m.put("shareUrl", "");
					m.put("description", "");
					map.put("open", "web");
					break;
				}
				activity = activityService.get(relatedId);
				m.put("id",BeanUtil.nullValueOf(activity.getId()));
				m.put("title", BeanUtil.nullValueOf(activity.getName()));
				m.put("gotoUrl", BeanUtil.nullValueOf(activity.getGotoUrl()));
				m.put("isShare", BeanUtil.nullValueOf(activity.getIsShare()));
				m.put("shareUrl", BeanUtil.nullValueOf(activity.getShareUrl()));
				m.put("description", BeanUtil.nullValueOf(activity.getDescription()));
				m.put("code", "");
				map.put("open", "web");
				break;
			}
			case "launch":{
				logger.info("------------------map.get('open') 对应着 ：" + map.get("open").toString());
				m.put("id","");
				m.put("title", "");
				m.put("gotoUrl", "");
				m.put("code", "");
				logger.info("------------------launch的value为空");
				break;
			}
			default:
				logger.error("-----------------map.get('open') 对应着 ：" + map.get("open").toString());
				model.setCode("0100");
		        model.setError("系统未知异常");
		        model.setResult(list);
		        model.setMessage("调用失败");
		        model.setState("0");
		        return model;
			}
			map.put("value", m);
		}
		List<Map<String, Object>> unReadMsglist = custNotificationService.unReadMsgMsg(custId, deviceId, msgType);
		List<Long> msgIds = new ArrayList<Long>();
		for(Map<String,Object> map : unReadMsglist){
			Long msgId = Long.valueOf(map.get("msgId").toString());
			msgIds.add(msgId);
		}
		msgIdMap.put("msgIdList", msgIds);
		msgIdMap.put("custId", custId);
		msgIdMap.put("deviceId", deviceId);
		logger.info("-------------------msgIdMap:" + msgIdMap);
		try {
			custNotificationService.updateIsReadByMsgIdMap(msgIdMap);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("---------------updateIsReadByMsgIdMap更新数据库出现异常----------------");
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			return model;
		}
		list = resultList;
		model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("调用成功");
		model.setState("1");
		return model;
	}
	/**
     * 设置消息为已读
     * @param msgId
     * @return 已读
     */
	@ResponseBody
    @RequestMapping(value = "/setMsgSign",params={"msgId"}, method = RequestMethod.POST)
    public Object setMsgSign(String msgId){
		logger.info("-------------------setMsgSign(start)--------fromcontroller:MessageController---|msgId:" + msgId);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		JsonResultModel model = getJsonResultModel();
		
		if(BeanUtil.isNullString(msgId)){
			model.setCode("0203");
	        model.setError("代表参数缺失");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
		Long custId = getLoginUserId();
    	Long deviceId = getDeviceId();
    	logger.info("------------获取登录用户的custId： " + custId + ",未登录用户设备的deviceId：" + deviceId);	
		try {
				custNotificationService.updateIsReadByMsgId(custId, deviceId, Long.valueOf(msgId));
				logger.info("----------------更新完成---------------");
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				return model;
		} catch (Exception e) {
				// TODO: handle exception
			    logger.error("---------------------------更新数据库出现异常------------------------");
				model.setCode("0100");
		        model.setError("系统未知异常");
		        model.setResult(list);
		        model.setMessage("调用失败");
		        model.setState("0");
		        return model;
		}		
    }
	/**
     * 删除推送消息
     * @param msgId
     * @return 
     */
	@ResponseBody
    @RequestMapping(value = "/deleteMsg",params={"msgId"}, method = RequestMethod.POST)
	public Object deleteMsg(String msgId){
		logger.info("-------------------deleteMsg(start)--------fromcontroller:MessageController---|msgId:" + msgId);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		JsonResultModel model = getJsonResultModel();
		if(BeanUtil.isNullString(msgId)){
			model.setCode("0203");
	        model.setError("参数缺失");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
		Long custId = getLoginUserId();
    	Long deviceId = getDeviceId();
    	logger.info("------------获取登录用户的custId： " + custId + ",未登录用户设备的deviceId：" + deviceId);
    	try {
    		CustNotification custNotification = custNotificationService.getByMsgIdAndCDId(custId, deviceId, Long.valueOf(msgId));
    		custNotificationService.remove(custNotification.getId());
        	logger.info("------------------------从cust_notification表中删除完毕-------------------------");
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("---------------------------删除数据出现异常------------------------");
			model.setCode("0100");
	        model.setError("系统未知异常");
	        model.setResult(list);
	        model.setMessage("调用失败");
	        model.setState("0");
	        return model;
		}
    	model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("调用成功");
		model.setState("1");
		return model;
	}
	/**
	 * 分享后发送消息和短信通知
	 * @param json
	 * @return
	 *//*
	@ResponseBody
    @RequestMapping(value = "/sendShareMsg",params={"json"}, method = RequestMethod.POST)
	public Object sendSmsAndMessage(String json){
		if(!BeanUtil.isNullString(json)){
		   JSONObject jsonObject = JsonUtil.strToJson((Object)json);
		   String shareType = jsonObject.getString("shareType");
		   String shareType = jsonObject.getString("shareType");
		}
		return null;
	}*/
	//过期检测函数
	private boolean expire(Long msgId) {
		Notification notification = notificationService.get(msgId);
		Boolean expire = false;
		if (notification == null) {
			return expire = true;
		}
		Long relatedId = notification.getRelatedId(); 
		switch (notification.getOpen()) {
		case "operation":{
			Activity activity = activityService.get(relatedId);
			if(activity != null){
				if(activity.getType().equals("0")){
					expire = true;
				}
			}else{
				logger.info("-----------------过期检测函数中查询到该Activity不存在-------------------");
				expire = true;
			}
			break;
		}
		case "app":{
			Application application = applicationService.get(relatedId);
			if(application != null){
				if(application.getDisable() == 1l){
					expire = true;
				}
			}else{
				logger.info("------------------过期检测函数中查询到该app不存在------------------");
				expire = true;
			}
			break;
		}
		case "news":{
			Msg msg = msgService.findMsgById(relatedId);
			if(msg != null){
				if(msg.getDisabled() == 1l){
					expire = true;
				}
			}else{
				logger.info("------------------过期检测函数中查询到该news不存在------------------");
				expire = true;
			}
			break;
		}
		case "web":{
			AppLink appLink = appLinkService.get(relatedId);
			if(appLink != null){
				if(appLink.getDisable() == 0l){
					expire = true;
				}
			}else{
				logger.info("------------------过期检测函数中查询到该appLink不存在------------------");
				expire = true;
			}
			break;
		}
		
		default:
			break;
		}
		
		return expire;
	}
}
