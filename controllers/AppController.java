package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.collect.Lists;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.app.model.ActivityPopupCount;
import com.inspur.icity.logic.app.model.Application;
import com.inspur.icity.logic.app.model.Version;
import com.inspur.icity.logic.app.service.ActivityPopupCountService;
import com.inspur.icity.logic.app.service.AdvertService;
import com.inspur.icity.logic.app.service.AppRecommentRepoService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.app.service.VersionService;
import com.inspur.icity.logic.cust.model.App;
import com.inspur.icity.logic.cust.service.AppService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.MarkMissionService;
import com.inspur.icity.logic.cust.service.ShareInfoService;
import com.inspur.icity.logic.operating.model.Activity;
import com.inspur.icity.logic.operating.model.Credits;
import com.inspur.icity.logic.operating.model.CustActivity;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.logic.operating.service.CreditsService;
import com.inspur.icity.logic.operating.service.CustActivityService;
import com.inspur.icity.logic.operating.service.NotificationService;
import com.inspur.icity.web.app.builder.AddAppToMapBuilder;
import com.inspur.icity.web.app.builder.AppToLifeBuild;
import com.inspur.icity.web.app.builder.AppToMapBuilder;
import com.inspur.icity.web.utils.Config;

import net.sf.json.JSONObject;

/**
 * 应用相关模块的接口
 */
@Controller
@RequestMapping(value = "/app")
public class AppController extends BaseAuthController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AppRecommentRepoService appRecommentRepoService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    AppService appService;
    @Autowired
    AppToMapBuilder appToMapBuilder;
    @Autowired
    AdvertService advertService;
    @Autowired
    AppToLifeBuild appToLifeBuild;
    @Autowired
    AddAppToMapBuilder addAppToMapBuilder;
    @Autowired
    VersionService versionService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    ActivityService activityService;
    @Autowired
    CustomerService customerService;
    @Autowired
    ShareInfoService shareInfoService;
    @Autowired
    CustActivityService custActivityService;
    @Autowired
    CreditsService creditsService;
    @Autowired
    ActivityPopupCountService activityPopupCountService;
    @Autowired
    MarkMissionService markMissionService;

    /**
     * 获取指定城市的推荐应用
     * 
     * @param cityCode
     *            城市标识
     * @return 推荐应用一览
     */
    @ResponseBody
    @RequestMapping(value = "/recommendApps", params = { "cityCode" })
    public Object recommendApps(String cityCode) {
	logger.info("--------------recommendApps(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:获取指定城市的推荐应用" + "|"
		+ "cityCode:" + cityCode + "|" + "PageBounds:" + getPageBounds().toString());
	return appRecommentRepoService.recommendApps(cityCode, getPageBounds());
    }

    /**
     * 应用搜索
     * 
     * @param key
     * @param cityCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/search", params = { "key", "cityCode" })
    public Object search(String key, String cityCode) {
	logger.info("--------------search(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:应用搜索" + "|" + "key:" + key + "|"
		+ "cityCode:" + cityCode);
	List<Application> list = Lists.newArrayList();
	if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(cityCode)) {
	    logger.info("--------------search(从数据库查找相关应用的信息)-------------");
	    list = applicationService.search(key, cityCode, getPageBounds());
	}
	return list;
    }

    /**
     *
     * 添加应用列表
     * 
     * @param cityCode
     *            城市Code
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/addAppList", params = { "cityCode" })
    public Object addAppList(String cityCode) {
	logger.info("--------------addAppList(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:添加应用列表" + "|" + "cityCode:"
		+ cityCode + "|" + "LoginUserId:" + getLoginUserId() + "|" + "PageBounds:" + getPageBounds());
	List<Map<String, Object>> list = appService.addAppList(cityCode, getLoginUserId(), getPageBounds());
	return appToMapBuilder.build(list);
    }

    /**
     * 添加应用列表（新）
     * 
     * @param cityCode
     *            城市Code
     * @return
     */
    /*
     * @ResponseBody
     * 
     * @RequestMapping(value = "/addAppListNew", params = {"cityCode"}) public
     * Object addAppListNew(String cityCode) { List<Map<String, Object>> list =
     * appService.addAppList(cityCode, getLoginUserId(), getPageBounds());
     * return addAppToMapBuilder.build(list,cityCode); }
     */
    @ResponseBody
    @RequestMapping(value = "/addAppListNew", params = { "cityCode" })
    public Object addAppListNew(String cityCode) {
	logger.info("--------------addAppListNew(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:添加应用列表" + "|" + "cityCode:"
		+ cityCode + "|" + "LoginUserId:" + getLoginUserId() + "|" + "PageBounds:" + getPageBounds());
	List<Map<String, Object>> list = appService.addAppLists(cityCode, getLoginUserId(), getPageBounds());
	return addAppToMapBuilder.build(list, cityCode);
    }

    /**
     *
     * 添加应用
     * 
     * @param appList
     */
    @ResponseBody
    @RequestMapping(value = "/addApp", method = RequestMethod.POST)
    public void addApp(String appList) {
	logger.info("--------------addApp(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:添加应用" + "|" + "appList:" + appList);
	try {
	    if (appList != null && !"".equals(appList)) {
		String[] arr = appList.split(",");
		List<App> apps = new ArrayList<>();
		for (int i = 0; i < arr.length; i++) {
		    App app = new App();
		    Long appId = Long.parseLong(arr[i]);
		    app.setCustId(getLoginUserId());
		    app.setDeviceId(getDeviceId());
		    app.setAppId(appId);
		    apps.add(app);
		}
		appService.addList(apps, getLoginUserId(), getCityCode() == null ? "370100" : getCityCode());
		logger.info("--------------addApp(添加相关应用)-------------");
	    } else {
		appService.cancelApp(getLoginUserId(), getCityCode() == null ? "370100" : getCityCode());
	    }
	} catch (Exception e) {
	    logger.error("--------------addApp(添加相关应用)出现异常：" + e.toString());
	    throw new ApplicationException(900, "操作失败");
	}
    }

    /**
     * 生活列表(新的)
     * 
     * @param
     */
    @ResponseBody
    @RequestMapping(value = "/applist", params = { "cityCode" })
    public Object appList(String cityCode) {
	logger.info("--------------applist(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:生活列表(新的)" + "|" + "cityCode:"
		+ cityCode + "|" + "PageBounds:" + getPageBounds());
	List<Map<String, Object>> list = applicationService.applist(cityCode, getPageBounds());
	return appToLifeBuild.build(list, cityCode);
    }

    /**
     * 生活列表
     *
     * @param
     */
    @ResponseBody
    @RequestMapping(value = "/appListOld", params = { "cityCode", "type" })
    public Object appListOld(String cityCode, String type) {
	logger.info("--------------applist(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:生活列表" + "|" + "cityCode:" + cityCode
		+ "|" + "PageBounds:" + getPageBounds() + "|" + "type" + type);
	return applicationService.appListOld(cityCode, type, getPageBounds());
    }

    /**
     * 广告信息
     * 
     * @param cityCode
     */
    @ResponseBody
    @RequestMapping(value = "/advert", params = { "cityCode" }, method = RequestMethod.GET)
    public Object advert(String cityCode) {
	logger.info("--------------advert(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:广告信息" + "|" + "cityCode:" + cityCode);
	return advertService.getDetails(cityCode);
    }

    /**
     * 获取操作系统最新版本号
     * 
     * @param os
     */
    @ResponseBody
    @RequestMapping(value = "/version", params = { "os" })
    public Object getVersion(String os) {
    	Integer forceUpdateVersion=null;
    //获取当前版本号
    Integer nowVersion=getVersion();
    Version version=versionService.getLatestVersion(os);
    //获取强制更新版本号
    if(!BeanUtil.isNullString(version.getForceUpdateVersion())){
    	forceUpdateVersion = Integer.valueOf(version.getForceUpdateVersion().replace(".", "")).intValue();
	}
    if(forceUpdateVersion==null || forceUpdateVersion<=nowVersion){
    	//不更新
    	version.setForceUpdate("0");
    }else{
    	//强制更新
    	version.setForceUpdate("1");
    }
	logger.info("--------------version(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:获取操作系统最新版本号" + "|" + "os:" + os);
	return version;
    }

    /**
     * 获取操作系统最新版本号【PC端专用】
     */
    /*
     * @ResponseBody
     * 
     * @RequestMapping(value="/getVersionMobile",method = RequestMethod.GET)
     * public Object getVersionMobile(String os){ HttpServletResponse response =
     * ((ServletRequestAttributes)
     * RequestContextHolder.getRequestAttributes()).getResponse();
     * response.setContentType("application/x-javascript"); return new
     * JSONPObject(JsonpUtils.JSONP_CALLBACK, getVersion(os)); }
     */
    @ResponseBody
    @RequestMapping(value = "/getVersionMobile", method = RequestMethod.GET)
    public JSONPObject getVersionMobile(String callbackparam, String os) {
	logger.info("--------------getVersionMobile(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:获取操作系统最新版本号" + "|"
		+ "callbackparam:" + callbackparam + "|" + "os:" + os);
	Version version = new Version();
	version = versionService.getLatestVersion(os);
	return new JSONPObject(callbackparam, version);
    }

    /**
     * 获取当前系统时间，格式yyyy-MM-dd HH:mm:ss
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSystemTime", method = RequestMethod.POST)
    public Object getSystemTime() {
	logger.info("--------------getSystemTime(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:获取当前系统时间");
	List<Map<String, String>> listJson = new ArrayList<Map<String, String>>();
	JsonResultModel model = getJsonResultModel();
	model.setResult(listJson);
	try {
	    String nowTime = DateUtil.getNow("yyyy-MM-dd HH:mm:ss");
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("time", nowTime);
	    listJson.add(map);
	    model.setCode("0000");
	    model.setError("");
	    model.setResult(listJson);
	    model.setMessage("调用成功");
	    model.setState("1");
	} catch (Exception e) {
	    logger.info("--------------getSystemTime(获取当前系统时间出现异常：)" + e.toString());
	    model.setCode("0100");
	    model.setError(e.toString());
	    model.setResult(listJson);
	    model.setMessage("调用失败");
	    model.setState("0");
	}
	return model;
    }

    /**
     * 分享成功之后发短信
     * 
     * @param params
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/sendMsgAfterShare", params = { "params" }, method = { RequestMethod.POST })
    public Object sendMsgAfterShare(@RequestParam("params") String params, @RequestHeader("access_token") String access_token) throws Exception {
	logger.info("--------------sendMsgAfterShare(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:分享之后发送消息" + "|params:"
		+ params + "|access_token:" + access_token);
	JsonResultModel model = getJsonResultModel();
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	try {
	    JSONObject jsonObject = JsonUtil.strToJson((Object) params);
	    if (!BeanUtil.isNullString(params)) {
	    	// 用于分享加积分做判断
			List<Map<String, Object>> shareInfo = null;
			String shareContentId = jsonObject.getString("contentId");
		String cityCode = "";
		if (getClientInfo() != null) {
		    cityCode = getClientInfo().getCityCode();
		}
		logger.info("-------------sendMsgAfterShare获取的cityCode:" + cityCode);
		Long custId = getLoginUserId();
		logger.info("-------------sendMsgAfterShare获取的custId:" + custId);
		// 分享状态（提交0、成功1、失败2、取消3）
		String shareState = jsonObject.getString("shareState");
		Long deviceId = getDeviceId();
		// 可参与的活动类型
		String type = "";
		// 判断当前分享信息类型，app是应用，news是资讯，operation是运营活动
		if (jsonObject.getString("shareType").equalsIgnoreCase("app")) {
		    type = "4";
		    jsonObject.put("activityType", "4");
		    // 用于应用分享加积分做判断,一天内分享多次只加一次分
			shareInfo = shareInfoService.findIsShareByCustIdAndContentId(custId, shareContentId,new Date());
		} else if (jsonObject.getString("shareType").equalsIgnoreCase("news")) {
		    type = "5";
		    jsonObject.put("activityType", "5");
		    // 用于咨询分享加积分做判断，分享一条内容只加一次分
			shareInfo = shareInfoService.findIsShareByCustIdAndContentId(custId, shareContentId,null);
		} else if (jsonObject.getString("shareType").equalsIgnoreCase("operation")) {
			logger.info("sendMsgAfterShare获取的shareContentId: "+shareContentId);
			if("2".equals(activityService.get(Long.valueOf(shareContentId)).getType())){
				type = "2";
				jsonObject.put("activityType", "2");
			}else{
			    type = "6";
			    jsonObject.put("activityType", "6");
			}
		}
		String shareGoal = jsonObject.getString("shareGoal");
		// 电话号码
		String mobilePhone = jsonObject.getString("mobilePhone");
		String date = DateUtil.getNow("yyyy-MM-dd HH:mm:ss");
		// 查询活动表信息，确定有无或送消息可发
		if (jsonObject.getString("shareType").equalsIgnoreCase("operation")) {
		    logger.info("--------------分享类型是operation，活动开始--------------");
		    try {
			// Map<String, Object> m =
			// activityService.findActivityById(jsonObject.getString("contentId"),
			// date);// 根据前台传入contentId查询的custActivity
			List<Map<String, Object>> activityLists = activityService.findActivityByCondition(cityCode, type, date);
			

			if (activityLists.size() == 0) {
			    // logger.error("----------------contentId为" +
			    // jsonObject.getString("contentId") +
			    // "对应的活动不存在---------------");
			    logger.error("对应的活动不存在---------------");
			    // 保存分享信息,activityId为空
			    notificationService.saveShareInfo(custId, deviceId, "", jsonObject);
			    model.setCode("0000");
			    model.setError("");
			    model.setResult(list);
			    model.setMessage("调用成功");
			    model.setState("1");
			    return model;
			}
			logger.info("-----------sendMsgAfterShare根据活动id:" + jsonObject.getString("contentId") + "查询到的活动信息为:" + activityLists.get(0).get("id"));
			// 活动的id
			Map<String, Object> m = activityLists.get(0);
			String activityId = m.get("id").toString();
			logger.info("----------------sendMsgAfterShare(判断该用户是否已经推送过该条活动信息)------------------");
			List<Map<String, Object>> lss = shareInfoService.getShareInfoByCustIdAndShareGoal(custId, activityId);
			if (lss != null && lss.size() > 0) {
			    logger.info("----------------sendMsgAfterShare(该用户已经推送过该活动)------------------");
			    notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
			} else {
			    logger.info("----------------sendMsgAfterShare(该用户未推送过该活动,开始推送消息或者短信)------------------");
			    // 发送方式1：通知推送，2：短信发送,0:无
			    String sendType = m.get("sendType").toString();
			    // pushToken
			    String pushToken = getPushToken();
			    if("120c83f7601532fd1d5".equals(pushToken)||"1517bfd3f7f796f2f1d".equals(pushToken)){
			    	logger.error("--------------pushToken为120c83f7601532fd1d5和1517bfd3f7f796f2f1d的用户为刷单用户,不发送通知--------------");
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("分享成功，不发送通知");
					model.setState("1");
					return model;
			    }
			    logger.info("-------------sendMsgAfterShare获取的pushToken为:" + pushToken);
			    m.put("pushToken", pushToken);
			    String jsonStr = (String) notificationService.getMsgAndSmsInfo(m, sendType, mobilePhone, "ops", "operation");
			    logger.info("-------------getMsgAndSmsInfo返回的jsonStr为:" + jsonStr);
			    if (BeanUtil.isNullString(jsonStr)) {
				logger.error("--------------getMsgAndSmsInfo的sendType为0,表示不发送通知,所以jsonStr为空--------------");
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("分享成功，不发送通知");
				model.setState("1");
				return model;
			    }
			    logger.info("-----------------sendMsgAfterShare---|sendType:" + sendType + "|shareState:" + shareState);
			    if (!BeanUtil.isNullString(sendType) && sendType.equals("1")) {
				if (!BeanUtil.isNullString(shareState) && shareState.equalsIgnoreCase("1")) {
				    // 分享活动加积分
				    if (type.equalsIgnoreCase("6") && (shareGoal.equalsIgnoreCase("1"))) {// 即时生效活动
					logger.info("---------------分享活动加积分开始----------------");
					CustActivity custActivity = custActivityService.getCustActivity(custId, Long.valueOf(activityId));
					if (custActivity == null) {
					    custActivity = new CustActivity();
					    custActivity.setActivityId(Long.valueOf(activityId));
					    custActivity.setCustId(custId);
					    custActivity.setActualPrice(0l);
					    custActivityService.add(custActivity);
					    logger.info("---------------添加custActivity信息完成--------------");
					}
					Credits credits = creditsService.getCustCredits(custId);
					if (credits == null) {
					    logger.info("--------------cust_credits表中未查询到该用户的信息--------------");
					    model.setCode("0100");
					    model.setError("未知异常");
					    model.setResult(list);
					    model.setMessage("调用失败");
					    model.setState("0");
					    return model;
					} else {
					    creditsService.updateCreditsByShare(custId, Long.valueOf(m.get("actualPrice").toString()));
					    logger.info("--------------更新cust_credits表完成--------------");
					}
					logger.info("--------------sendMsgAfterShare(开始保存活动数据)------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
					logger.info("----------------sendMsgAfterShare(开始推送消息)------------------");
					String url = Config.getValue("sendMessageUrl");
					String state = notificationService.sendMessage(jsonStr, "notification", access_token, url);
					if (state.equalsIgnoreCase("1")) {
						// 分享活动加积分新版本
						logger.info("-----------------sendMsgAfterShare(新版本应用分享活动加积分开始)----------|shareType:" + jsonObject.getString("shareType")+"|sendType:"+sendType);
						Map<String, Object> markInfo = markMissionService.getMark("/app/sendMsgAfterShareApp", custId,
								"今日已完成");
						model.setMarkInfo(markInfo);
						logger.info("-----------------sendMsgAfterShare(新版本应用分享活动加积分成功)----------|markInfo:" + markInfo);
					    logger.info("--------------sendMsgAfterShare(消息推送成功)-------------accessToken" + access_token);
					} else {
					    logger.info("--------------sendMsgAfterShare(推送消息不成功)------------------");
					}
				    } else {
					logger.info("--------------sendMsgAfterShare(不是朋友圈和空间分享)------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				    }
				} else {
				    logger.info("-----------------sendMsgAfterShare(分享消息不成功)--------------------");
				    notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				}
			    } else if (!BeanUtil.isNullString(sendType) && sendType.equals("2")) {
				if (!BeanUtil.isNullString(shareState) && shareState.equalsIgnoreCase("1")) {
				    // 分享活动加积分
				    if (type.equalsIgnoreCase("6") && (shareGoal.equalsIgnoreCase("1"))) {// 即时生效活动
					logger.info("---------------分享活动加积分开始----------------");
					CustActivity custActivity = custActivityService.getCustActivity(custId, Long.valueOf(activityId));
					if (custActivity == null) {
					    custActivity = new CustActivity();
					    custActivity.setActivityId(Long.valueOf(activityId));
					    custActivity.setCustId(custId);
					    custActivity.setActualPrice(0l);
					    custActivityService.add(custActivity);
					    logger.info("---------------添加custActivity信息完成--------------");
					}
					Credits credits = creditsService.getCustCredits(custId);
					if (credits == null) {
					    logger.info("--------------cust_credits表中未查询到该用户的信息--------------");
					    model.setCode("0100");
					    model.setError("未知异常");
					    model.setResult(list);
					    model.setMessage("调用失败");
					    model.setState("0");
					    return model;
					} else {
					    creditsService.updateCreditsByShare(custId, Long.valueOf(m.get("actualPrice").toString()));
					    logger.info("--------------更新cust_credits表完成--------------");
					}
					logger.info("--------------sendMsgAfterShare(开始保存活动数据)------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
					logger.info("----------------sendMsgAfterShare(开始发送短信)------------------");
					String url = Config.getValue("sendMessageMsgUrl");
					String state = notificationService.sendMessage(jsonStr, "message", access_token, url);
					if (state.equalsIgnoreCase("1")) {
						// 分享活动加积分新版本
						logger.info("-----------------sendMsgAfterShare(新版本应用分享活动加积分开始)----------|shareType:" + jsonObject.getString("shareType")+"|sendType:"+sendType);
						Map<String, Object> markInfo = markMissionService.getMark("/app/sendMsgAfterShareApp", custId,
								"今日已完成");
						model.setMarkInfo(markInfo);
						logger.info("-----------------sendMsgAfterShare(新版本应用分享活动加积分成功)----------|markInfo:" + markInfo);
					    logger.info("--------------sendMsgAfterShare(短信推送成功)-------------mobile" + mobilePhone);

					} else {
					    logger.info("--------------sendMsgAfterShare(发送短信不成功)------------------");
					}
				    } else {
					logger.info("--------------sendMsgAfterShare(不是朋友圈和空间分享)------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				    }
				} else {
				    logger.info("-----------------sendMsgAfterShare(分享短信不成功)--------------------");
				    notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				}
			    }
			}
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
			return model;
		    } catch (Exception e) {
			logger.info("----------sendMsgAfterShare出现异常--------e:" + e.toString());
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			return model;
		    }
		}
		List<Map<String, Object>> ls = activityService.findActivityByCondition(cityCode, type, date);
		if (ls != null && ls.size() > 0) {
		    for (Map<String, Object> m : ls) {
			// 活动的id
			String activityId = m.get("id").toString();
			if (activityId != null && !BeanUtil.isNullString(activityId) && custId != null && !BeanUtil.isNullString(custId.toString())) {
			    List<Map<String, Object>> lss = shareInfoService.getShareInfoByCustId(custId, activityId);
			    logger.info("----------------sendMsgAfterShare(判断该用户是否已经推送过该条活动信息)------------------");
			    //
			    if (lss != null && lss.size() > 0) {
				logger.info("----------------sendMsgAfterShare(该用户已经推送过该活动)------------------");
				notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
			    } else {
				logger.info("----------------sendMsgAfterShare(开始推送消息或者短信)------------------");
				// 发送方式1：通知推送，2：短信发送,0:无
				String sendType = (String) m.get("sendType");
				// pushToken
				String pushToken = getPushToken();
				m.put("pushToken", pushToken);
				String jsonStr = (String) notificationService.getMsgAndSmsInfo(m, sendType, mobilePhone, "ops", "operation");

				if (!BeanUtil.isNullString(sendType) && sendType.equals("1") && !BeanUtil.isNullString(jsonStr)) {
				    if (!BeanUtil.isNullString(shareState) && shareState.equalsIgnoreCase("1")) {
					logger.info("----------------sendMsgAfterShare(开始推送消息)------------------");
					String url = Config.getValue("sendMessageUrl");
					String state = notificationService.sendMessage(jsonStr, "notification", access_token, url);
					if (state.equalsIgnoreCase("1")) {
					    logger.info("--------------sendMsgAfterShare(消息推送成功)-------------accessToken" + access_token);
					    logger.info("--------------sendMsgAfterShare(开始保存活动数据)------------------");
					    notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
					} else {
					    logger.info("--------------sendMsgAfterShare(推送消息不成功)------------------");
					}
				    } else {
					logger.info("-----------------sendMsgAfterShare(分享消息不成功)--------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				    }
				} else if (!BeanUtil.isNullString(sendType) && sendType.equals("2") && !BeanUtil.isNullString(jsonStr)) {
				    if (!BeanUtil.isNullString(shareState) && shareState.equalsIgnoreCase("1")) {
					logger.info("----------------sendMsgAfterShare(开始发送短信)------------------");
					String url = Config.getValue("sendMessageMsgUrl");
					String state = notificationService.sendMessage(jsonStr, "message", access_token, url);
					if (state.equalsIgnoreCase("1")) {
					    logger.info("--------------sendMsgAfterShare(短信推送成功)-------------mobile" + mobilePhone);
					    logger.info("--------------sendMsgAfterShare(开始保存活动数据)------------------");
					    notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
					} else {
					    logger.info("--------------sendMsgAfterShare(发送短信不成功)------------------");
					}
				    } else {
					logger.info("-----------------sendMsgAfterShare(分享短信不成功)--------------------");
					notificationService.saveShareInfo(custId, deviceId, activityId, jsonObject);
				    }
				}
			    }
			}
		    }
		} else {
		    // 保存分享信息,activityId为空
		    notificationService.saveShareInfo(custId, deviceId, "", jsonObject);
		}
		
		if(!BeanUtil.isNullString(shareState) && shareState.equalsIgnoreCase("1")){
			// 应用分享给用户添加积分
			if (jsonObject.getString("shareType").equalsIgnoreCase("app")) {
				if (shareInfo == null || shareInfo.size() <= 0) {
					Map<String, Object> markInfo = markMissionService.getMark("/app/sendMsgAfterShareApp", custId,
							"今日已完成");
					model.setMarkInfo(markInfo);
					logger.info("-----------------sendMsgAfterShare(应用分享加积分成功)----------|markInfo:" + markInfo);
				} else {
					logger.info("-----------------sendMsgAfterShare(应用已分享)----------|shareInfo:" + shareInfo);
				}
	
			}
			// 资讯分享给用户添加积分
			if (jsonObject.getString("shareType").equalsIgnoreCase("news")) {
				if (shareInfo == null || shareInfo.size() <= 0) {
					Map<String, Object> markInfo = markMissionService.getMark("/app/sendMsgAfterShareNews", custId,
							"今日已完成");
					model.setMarkInfo(markInfo);
					logger.info("-----------------sendMsgAfterShare(资讯分享加积分成功)----------|markInfo:" + markInfo);
				} else {
					logger.info("-----------------sendMsgAfterShare(资讯已分享)----------|shareInfo:" + shareInfo);
				}
			}
		}

	    }
	    model.setCode("0000");
	    model.setError("");
	    model.setResult(list);
	    model.setMessage("调用成功");
	    model.setState("1");
	    return model;
	} catch (Exception e) {
	    logger.error("-----------------sendMsgAfterShare(error)--------------------" + e.toString());
	    model.setCode("0100");
	    model.setError(e.toString());
	    model.setResult(list);
	    model.setMessage("调用失败");
	    model.setState("0");
	    return model;
	}
    }

    /**
     * 活动弹窗，返回sendType是弹窗通知的上线活动
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/activityPopup", method = { RequestMethod.POST })
    public Object activityPopup() {
	logger.info("--------------activityPopup(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:活动弹窗");
	JsonResultModel model = getJsonResultModel();
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	try {
	    String cityCode = getCityCode();
	    list = activityService.getActivityByPopup(cityCode);
	    model.setCode("0000");
	    model.setError("");
	    model.setResult(list);
	    model.setMessage("调用成功");
	    model.setState("1");
	} catch (Exception e) {
	    logger.error("-----------------activityPopup(error)--------------------" + e.toString());
	    model.setCode("0100");
	    model.setError(e.toString());
	    model.setResult(list);
	    model.setMessage("调用失败");
	    model.setState("0");
	}
	return model;
    }

    /**
     * 活动弹窗统计
     * 
     * @param type
     *            打开：open， 关闭：shutDown
     * @param activityId
     *            活动ID
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getActivityPopupCount", method = { RequestMethod.POST })
    public Object getActivityPopupCount(String type, String activityId) {
	logger.info("--------------getActivityPopupCount(start)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:前台传过来的参数" + "|type:"
		+ type + "|activityId:" + activityId);
	JsonResultModel model = getJsonResultModel();
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	try {
	    if (!BeanUtil.isNullString(type) && !BeanUtil.isNullString(activityId)) {
		List<Map<String, Object>> activityType = activityPopupCountService.getActivityById(activityId);
		if (activityType.size() > 0) {
		    ActivityPopupCount activityPopupCo = activityPopupCountService.getActivityPoCoByActivityId(activityId);
		    if (activityPopupCo == null) {
			ActivityPopupCount ac = new ActivityPopupCount();
			ac.setActivityId(Long.parseLong(activityId));
			ac.setClickAmount(0l);
			ac.setCloseAmount(0l);
			ac.setPopupAmount(0l);
			activityPopupCountService.add(ac);
		    }

		    List<Map<String, Object>> activityPopupC = activityPopupCountService.findActivityPopupCountById(activityId);

		    if (activityPopupC != null) {
			if ("open".equals(type)) {
			    ActivityPopupCount activityPopupCount = activityPopupCountService.get(Long.parseLong(activityId));
			    if (activityPopupCount != null) {
				activityPopupCount.setClickAmount(activityPopupCount.getClickAmount() + 1l);
				activityPopupCount.setPopupAmount(activityPopupCount.getClickAmount() + activityPopupCount.getCloseAmount());
				activityPopupCountService.update(activityPopupCount);
				Map<String, Object> map = activityPopupC.get(0);
				logger.info("--------------getActivityPopupCount(弹窗点击之前统计信息)-------------" + "|" + "fromModule:AppController" + "|"
					+ "interfaceInfo:活动弹窗统计" + "|map:" + map);
				if (((Date) map.get("endTime")).getTime() > new Date().getTime()) {
				    map.put("status", "上线");
				} else {
				    map.put("status", "下线");
				}

				map.put("clickAmount", activityPopupCount.getClickAmount());
				map.put("popupAmount", activityPopupCount.getPopupAmount());
				list.add(map);
				logger.info("--------------getActivityPopupCount(弹窗点击之后统计信息)-------------" + "|" + "fromModule:AppController" + "|"
					+ "interfaceInfo:活动弹窗统计" + "|map:" + map);
				logger.info("--------------getActivityPopupCount(end)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:");
			    }
			}
			if ("shutDown".equals(type)) {
			    ActivityPopupCount activityPopupCount = activityPopupCountService.get(Long.parseLong(activityId));
			    if (activityPopupCount != null) {
				activityPopupCount.setCloseAmount(activityPopupCount.getCloseAmount() + 1l);
				activityPopupCount.setPopupAmount(activityPopupCount.getClickAmount() + activityPopupCount.getCloseAmount());
				activityPopupCountService.update(activityPopupCount);
				Map<String, Object> map = activityPopupC.get(0);
				logger.info("--------------getActivityPopupCount(弹窗关闭之前统计信息)-------------" + "|" + "fromModule:AppController" + "|"
					+ "interfaceInfo:活动弹窗统计" + "|map:" + map);
				if (((Date) map.get("endTime")).getTime() > new Date().getTime()) {
				    map.put("status", "上线");
				} else {
				    map.put("status", "下线");
				}
				map.put("closeAmount", activityPopupCount.getCloseAmount());
				map.put("popupAmount", activityPopupCount.getPopupAmount());
				list.add(map);
				logger.info("--------------getActivityPopupCount(弹窗关闭之后统计信息)-------------" + "|" + "fromModule:AppController" + "|"
					+ "interfaceInfo:活动弹窗统计" + "|map:" + map);
				logger.info("--------------getActivityPopupCount(end)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:");

			    }
			}
		    }

		}

		model.setResult(list);
		model.setCode("0000");
		model.setError("");
		model.setMessage("调用成功");
		model.setState("1");

	    } else {
		model.setResult(list);
		model.setCode("0203");
		model.setError("接口参数缺失");
		model.setMessage("调用失败");
		model.setState("0");
		logger.info("--------------getActivityPopupCount(接口参数缺失)-------------");
		logger.info("--------------getActivityPopupCount(end)-------------" + "|" + "fromModule:AppController" + "|" + "interfaceInfo:");

	    }
	} catch (Exception e) {
	    logger.error("-----------------getActivityPopupCount(error)--------------------" + e.toString());
	    model.setCode("0100");
	    model.setError(e.toString());
	    model.setResult(list);
	    model.setMessage("调用失败");
	    model.setState("0");
	}
	return model;

    }

}
