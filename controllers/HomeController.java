package com.inspur.icity.web.controllers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sf.ehcache.hibernate.management.impl.BeanUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.google.common.collect.Maps;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.logic.app.model.AppHome;
import com.inspur.icity.logic.app.model.AppLink;
import com.inspur.icity.logic.app.model.AppModule;
import com.inspur.icity.logic.app.model.Application;
import com.inspur.icity.logic.app.service.AppHomeService;
import com.inspur.icity.logic.app.service.AppLinkService;
import com.inspur.icity.logic.app.service.AppModuleService;
import com.inspur.icity.logic.app.service.AppRecommentService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.gov.service.GovRecommentService;
import com.inspur.icity.logic.news.service.HotNewsService;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.operating.model.Activity;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.web.app.builder.AppToMapBuilder;
import com.inspur.icity.web.utils.Constants;

/**
 * 爱城市网首页精华展示
 * @author gaoheng
 */

@Controller
@RequestMapping(value = "/home")
public class HomeController extends BaseAuthController{
	 Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
    GovRecommentService govRecommentService;
	@Autowired
	AppHomeService appHomeService;
	@Autowired
    AppToMapBuilder appToMapBuilder;
	@Autowired
	AppRecommentService appRecommentService;
	@Autowired
	ApplicationService applicationService;
	@Autowired
	MsgService msgService;
	@Autowired
    HotNewsService hotNewsService;
	@Autowired
	AppModuleService appModuleService;
	@Autowired
	AppLinkService appLinkService;
	@Autowired
	ActivityService activityService;
	@Autowired
	CommentService commentService;
	/**
	 * 获取首页精华应用信息
	 * @param cityCode
	 * @param type
	 * @return
	 */
	
	@ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.POST)
	public Object test(){
		return appLinkService.findLink("1");
	}
	
	
	@ResponseBody
    @RequestMapping(value = "/getHomeApp", method = RequestMethod.POST)
	public Object getHomeApp(String cityCode,String type){
		logger.info("--------------getHomeApp(start)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:获取首页精华应用信息"+"|"+"cityCode:"+cityCode+"|"+"type:"+type);
		JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> listResult = new ArrayList<Map<String,Object>>();
		List<AppHome> list = new ArrayList<AppHome>();
		try {
			list = appHomeService.getAppList(cityCode, type);
			List<Map<String,Object>> listApp= new ArrayList<Map<String,Object>>(); 
			logger.info("--------------getHomeApp(获取各个Module的详细信息)-------------"+"|"+"cityCode:"+cityCode+"|"+"type:"+type);
			for (AppHome appHome : list)  
			{  
			  if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("banner")){
				  listApp = govRecommentService.findBannersByCityCodeHome(appHome.getCityCode(), appHome.getFkId());
				  listResult.add(appToMapBuilder.buildHome(listApp, appHome.getType(),appHome.getImageUrl()));
				  listApp.clear();
			  }
			  if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("gov")){
				  listApp = appRecommentService.findGovAppHome(appHome.getCityCode(), appHome.getFkId());
				  listResult.add(appToMapBuilder.buildHome(listApp, appHome.getType(),appHome.getImageUrl()));
				  listApp.clear();
			  }
			  if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("life")){
				  listApp = applicationService.appHomelist(appHome.getCityCode(), appHome.getFkId());
				  listResult.add(appToMapBuilder.buildHome(listApp, appHome.getType(),appHome.getImageUrl()));
				  listApp.clear();
			  }
			  
			  if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("news")){
				  listApp = msgService.findNewsHomeList(appHome.getCityCode(), appHome.getFkId());
				  listResult.add(appToMapBuilder.buildHome(listApp, appHome.getType(),appHome.getImageUrl()));
				  listApp.clear();
			  }
			}
			model.setCode("0000");
    		model.setError("");
    		model.setMessage("调用成功");
    		model.setState("1");
    		model.setResult(listResult);
			logger.info("--------------getHomeApp(获取成功)-------------");
		} catch (Exception e) {
			model.setCode("0100");
    		model.setError(e.toString());
    		model.setMessage("调用失败");
    		model.setState("0");
    		logger.error("--------------getHomeApp(error)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceName:获取首页精华应用信息"+"|"+"error:"+e.toString());
		} 
		logger.info("--------------getHomeApp(end)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:获取首页精华应用信息");
		return model;
	}
    /**
     * 获取首页信息
     * @param cityCode
     * @param type
     * @param moduleIndex
     * @return
     */
	@ResponseBody
    @RequestMapping(value = "/getHotSpotList", method = RequestMethod.POST)
	public Object getHotSpotList(String cityCode,String moduleIndex){
		logger.info("--------------getHotSpotList(start)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo: 获取首页信息"+"|"+"cityCode:"+cityCode+"|"+"moduleIndex:"+moduleIndex);
		JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			//获取首页各个模块信息
			logger.info("--------------getHotSpotList(获取首页各个模块信息)-------------"+"|"+"cityCode:"+cityCode+"|"+"moduleIndex:"+moduleIndex);
			List<AppModule> moduleList = appModuleService.getAppModuleList(cityCode,moduleIndex);
			if(moduleList!=null&&moduleList.size()>0){
				for(AppModule appModule:moduleList){
					List<Map<String,Object>> listApp= new ArrayList<Map<String,Object>>();
					List<AppHome> homeList = new ArrayList<AppHome>();
					//获取各个模块下配置的应用类型
					homeList = appHomeService.getAppHotSpotList(cityCode, null, appModule.getModuleIndex());
					if(homeList!=null&&homeList.size()>0){
						for(AppHome appHome:homeList){//根据应用类型获取具体的应用信息
							//获取生活应用详细信息
							if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("life")){
								result = applicationService.appHomelist(appHome.getCityCode(), appHome.getFkId());
								listApp.add(appToMapBuilder.buildHome(BeanUtil.getList(result, "name", appHome.getName()), appHome.getType(),appHome.getImageUrl(),appHome.getComment()));
							}
							//获取咨讯信息
							if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("hotNews")){
								PageBounds page = getPageBounds();
							    page.setLimit(1);
							    result = (List<Map<String, Object>>) hotNewsService.hotNews(cityCode, page);
							    if(result!=null&&result.size()>0){
							    	 for(Map<String, Object> mapApp :result){
							    		  for (Object k : mapApp.keySet())  
								   	      {   if(k.toString().equalsIgnoreCase("newsId")){
								   				    mapApp.put("id", mapApp.get(k));
								   			  }
										   	  if(k.toString().equalsIgnoreCase("source")){
										   		  if(!BeanUtil.isNullString(mapApp.get(k).toString())){
									   				mapApp.put("source", "来源："+mapApp.get(k));
										   		  }else{
										   			mapApp.put("source", "");  
										   		  }
									   		  }
								   		  }
							    		  if(mapApp.get("id").toString().equalsIgnoreCase(mapApp.get("newsId").toString())){
							    			  mapApp.remove("newsId");
							    		  }
							   	     }
						      	}
							    //appHome.setComment("来源："+BeanUtil.getListData(result, "source"));
								listApp.add(appToMapBuilder.buildHome(result, appHome.getType(),appHome.getImageUrl(),appHome.getComment()));
							}
							//获取办事指南和办事大厅信息
							if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("gov")){
								result = appRecommentService.findGovAppHome(appHome.getCityCode(), appHome.getFkId());
								listApp.add(appToMapBuilder.buildHome(BeanUtil.getList(result, "name", appHome.getName()), appHome.getType(),appHome.getImageUrl(),appHome.getComment()));
							}
							//获取一般咨询信息
							if(!BeanUtil.isNullString(appHome.getType().toString())&&appHome.getType().equalsIgnoreCase("news")){
								result = msgService.findNewsHomeList(appHome.getCityCode(), appHome.getFkId());
								listApp.add(appToMapBuilder.buildHome(BeanUtil.getList(result, "name", appHome.getName()), appHome.getType(),appHome.getImageUrl(),appHome.getComment()));
							}
						}
					}
					appModule.setApps(listApp);//将应用信息添加进首页应用模块中
				}
			}
			model.setCode("0000");
    		model.setError("");
    		model.setMessage("调用成功");
    		model.setState("1");
    		model.setResult(moduleList);
		} catch (Exception e) {
			model.setCode("0100");
    		model.setError(e.toString());
    		model.setMessage("调用失败");
    		model.setState("0");
    		logger.error("--------------getHotSpotList(error)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceName:获取首页信息"+"|"+"error:"+e.toString());
		} 
		logger.info("--------------getHotSpotList(end)-------------"+"|"+"fromModule:HomeControlle"+"|"+"interfaceInfo:获取首页信息");
		return model;
	}
	
	//-----------------------------------------------动态首页接口---------------------------------------------
	/**
     * 动态首页接口
     * @param cityCode
     * @return
     */
	@ResponseBody
    @RequestMapping(value = "/getDynamicHomeApp", method = RequestMethod.POST)
	public Object getDynamicHomeApp(String cityCode){
		logger.info("--------------getDynamicHomeApp(start)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:动态首页接口"+"|"+"cityCode:"+cityCode);
		/*
		 * 首先从app_home_module和app_homespot两表联查并按module的Id升序 查询并返回到变量list
		 * 然后将与List的第一条记录的moduleId相同的记录 的信息一一取出
		 * 之后将与当前moduleId不同的记录相继循环取出，取出每一个moduleId对应的记录后，更新变量moduleId的值，之后每一次循环都与moduleId比较并根据不同结果执行不同处理过程
		 */	
		HttpSession session = getSession();
		ClientInfo clientInfo = (ClientInfo) session.getAttribute("clientInfo");
		String versionStr = clientInfo.getVersion();
		logger.info("--------------getDynamicHomeApp(从session中获取版本信息)-------------versionStr:"+versionStr);
		String versionStr2 =versionStr.replace(".", "");
		int version = Integer.parseInt(versionStr2);
		JsonResultModel model = getJsonResultModel();
		JSONArray ja = new JSONArray();
		try{	
			List<Map<String,Object>> list;
			if(version < 230){
		         list = (List<Map<String, Object>>) appHomeService.getOldDynamicHomeApp(cityCode);
		         logger.info("--------------getOldDynamicHomeApp(从数据库获取首页相关信息)-------------"+"|"+"cityCode:"+cityCode+"|"+"list.size:"+list.size());
			}else if(version < 240){
				 list = (List<Map<String, Object>>) appHomeService.getNewDynamicHomeApp(cityCode);
				 logger.info("--------------getNewDynamicHomeApp(从数据库获取首页相关信息)-------------"+"|"+"cityCode:"+cityCode+"|"+"list.size:"+list.size());
			}else{
				list = (List<Map<String, Object>>) appHomeService.getNewDynamicHomeApp240(cityCode);
				 logger.info("--------------getNewDynamicHomeApp240(从数据库获取首页相关信息)-------------"+"|"+"cityCode:"+cityCode+"|"+"list.size:"+list.size());
			}
		
		//判断list是否为空，即是否有符合条件的信息返回
		if(list.size() > 0){
		//初始化moduleId变量，之后新建相应的JSONObject时都要与之比较
		int moduleId =  Integer.parseInt(list.get(0).get("moduleId").toString());
		//初始化第一个module的信息取出
		JSONObject jo_module1 = new JSONObject();
		//jo_module1.put("moduleId",list.get(0).get("moduleId"));
		jo_module1.put("moduleType",list.get(0).get("moduleType"));
		jo_module1.put("moduleImgUrl",list.get(0).get("moduleImageurl") );
		jo_module1.put("moduleTitle",list.get(0).get("moduleTitle") );
		jo_module1.put("moduleTitleColor",list.get(0).get("moduleTitleColor") );
		logger.info("--------------getDynamicHomeApp(获取到第一个module相关信息)-------------");
		JSONArray valueArray = new JSONArray();
		for(int i = 0;i < list.size();i++){
			if(Integer.parseInt(list.get(i).get("moduleId").toString()) == Integer.parseInt(list.get(0).get("moduleId").toString())){
				switch(list.get(i).get("spotType").toString()){
				case "web":
					JSONObject tempjo_web = new JSONObject();
					tempjo_web.put("type","web" );
					AppLink appLink =  appLinkService.findLink(list.get(i).get("fkId").toString());
					tempjo_web.put("name", list.get(i).get("spotName"));
					tempjo_web.put("nameColor", list.get(i).get("spotNameColor"));
					tempjo_web.put("comment", list.get(i).get("spotComment"));
					tempjo_web.put("imageUrl",list.get(i).get("spotImageurl"));
					JSONObject webDetail = new JSONObject();
					webDetail.put("title", appLink.getTitle());
					webDetail.put("gotoUrl",appLink.getGotoUrl());
					webDetail.put("code",appLink.getCode());
					tempjo_web.put("detail",webDetail);
					valueArray.add(tempjo_web);
					break;
				case "operation":
					JSONObject tempjo_operation = new JSONObject();
					tempjo_operation.put("type","web" );
					Activity activity =  activityService.get( Long.valueOf(list.get(i).get("fkId").toString()));
					tempjo_operation.put("name", list.get(i).get("spotName"));
					tempjo_operation.put("nameColor", list.get(i).get("spotNameColor"));
					tempjo_operation.put("comment", list.get(i).get("spotComment"));
					tempjo_operation.put("imageUrl",list.get(i).get("spotImageurl"));
					JSONObject operationDetail = new JSONObject();
					operationDetail.put("title", activity.getName());
					operationDetail.put("gotoUrl",activity.getGotoUrl());
					operationDetail.put("code",activity.getCityCode());
					operationDetail.put("isShare", activity.getIsShare());
					operationDetail.put("shareUrl", activity.getShareUrl());
					operationDetail.put("description", activity.getDescription());
					operationDetail.put("level", activity.getLevel());
					operationDetail.put("id", activity.getId());
					tempjo_operation.put("detail",operationDetail);
					valueArray.add(tempjo_operation);
					break;
				case "app":
					JSONObject tempjo_app = new JSONObject();
					Application application= applicationService.getAppById((Integer)list.get(i).get("fkId"));
					tempjo_app.put("type", "app");
					tempjo_app.put("name", list.get(i).get("spotName"));
					tempjo_app.put("nameColor", list.get(i).get("spotNameColor"));
					tempjo_app.put("comment", list.get(i).get("spotComment"));
					tempjo_app.put("imageUrl",list.get(i).get("spotImageurl"));
					JSONObject appDetail = new JSONObject();
					appDetail.put("title", application.getName());
					appDetail.put("gotoUrl",application.getGotoUrl());
					appDetail.put("code",application.getCode());
					//增加“应用是否可分享字段”
					appDetail.put("isShare", application.getIsShare());
					//分享描述
					appDetail.put("description", application.getDescription());
					//返回数据增加应用id
					appDetail.put("id", application.getId());
					appDetail.put("level", application.getLevel());
					tempjo_app.put("detail",appDetail);
					valueArray.add(tempjo_app);
					break;
				case "news":
					Map<String,Object> tempMap_news = msgService.findNewsDetails(Long.parseLong(list.get(i).get("fkId").toString()));
					JSONObject tempjo_news = new JSONObject();
					tempjo_news.put("type","news" );
					//tempjo_news.put("name", list.get(i).get("spotName"));
					//新闻标题改为原新闻的原标题
					tempjo_news.put("name", tempMap_news.get("title"));
					tempjo_news.put("nameColor", list.get(i).get("spotNameColor"));
					tempjo_news.put("comment", list.get(i).get("spotComment"));
					tempjo_news.put("imageUrl",list.get(i).get("spotImageurl"));
					JSONObject newsDetail = new JSONObject();
					newsDetail.put("newsId", tempMap_news.get("id"));
					newsDetail.put("source",tempMap_news.get("source"));
					//newsDetail.put("imageUrl",tempMap_news.get("imgUrl"));
					//newsDetail.put("gotoUrl",tempMap_news.get("gotoUrl"));
					newsDetail.put("date",tempMap_news.get("createTime").toString().substring(0, 10));
					tempjo_news.put("detail",newsDetail);
					valueArray.add(tempjo_news);
					break;					
				}
			}
		}
		jo_module1.put("value", valueArray);
		ja.add(jo_module1);
		logger.info("--------------getDynamicHomeApp(获取到第一个module详细信息)-------------");
		//将其他module的信息取出
		for(int i = 1;i < list.size();i++){
			if(Integer.parseInt(list.get(i).get("moduleId").toString()) != moduleId){
				JSONObject jo = new JSONObject();
				//jo.put("moduleId",list.get(i).get("moduleId"));
				jo.put("moduleType",list.get(i).get("moduleType"));
				jo.put("moduleImgUrl",list.get(i).get("moduleImageurl") );
				jo.put("moduleTitle",list.get(i).get("moduleTitle") );
				jo.put("moduleTitleColor",list.get(i).get("moduleTitleColor") );
				JSONArray ja_other = new JSONArray();
				
				for(int j = 0;j < list.size();j++){
					if(Integer.parseInt(list.get(i).get("moduleId").toString()) == Integer.parseInt(list.get(j).get("moduleId").toString())){
						switch(list.get(j).get("spotType").toString()){
						case "web":
							JSONObject tempjo_web = new JSONObject();
							AppLink appLink =  appLinkService.findLink(list.get(j).get("fkId").toString());
							tempjo_web.put("type","web" );
							tempjo_web.put("name", list.get(j).get("spotName"));
							tempjo_web.put("nameColor", list.get(j).get("spotNameColor"));
							tempjo_web.put("comment", list.get(j).get("spotComment"));
							tempjo_web.put("imageUrl",list.get(j).get("spotImageurl"));
							JSONObject webDetail = new JSONObject();
							webDetail.put("title", appLink.getTitle());
							webDetail.put("gotoUrl",appLink.getGotoUrl());
							webDetail.put("code",appLink.getCode());
							tempjo_web.put("detail",webDetail);
							ja_other.add(tempjo_web);
							break;
						case "operation":
							JSONObject tempjo_operation = new JSONObject();
							tempjo_operation.put("type","web" );
							Activity activity =  activityService.get(Long.valueOf(list.get(j).get("fkId").toString()));
							tempjo_operation.put("name", list.get(j).get("spotName"));
							tempjo_operation.put("nameColor", list.get(j).get("spotNameColor"));
							tempjo_operation.put("comment", list.get(j).get("spotComment"));
							tempjo_operation.put("imageUrl",list.get(j).get("spotImageurl"));
							JSONObject operationDetail = new JSONObject();
							operationDetail.put("title", activity.getName());
							operationDetail.put("gotoUrl",activity.getGotoUrl());
							operationDetail.put("code",activity.getCityCode());
							operationDetail.put("isShare", activity.getIsShare());
							operationDetail.put("shareUrl", activity.getShareUrl());
							operationDetail.put("description", activity.getDescription());
							operationDetail.put("level", activity.getLevel());
							operationDetail.put("id", activity.getId());
							tempjo_operation.put("detail",operationDetail);
							ja_other.add(tempjo_operation);
							break;
						case "app":
							JSONObject tempjo_app = new JSONObject();
							Application application= applicationService.getAppById((Integer)list.get(j).get("fkId"));
							tempjo_app.put("type", "app");
							tempjo_app.put("name", list.get(j).get("spotName"));
							tempjo_app.put("nameColor", list.get(j).get("spotNameColor"));
							tempjo_app.put("comment", list.get(j).get("spotComment"));
							tempjo_app.put("imageUrl",list.get(j).get("spotImageurl"));
							JSONObject detail = new JSONObject();
							detail.put("title", application.getName());
							detail.put("gotoUrl",application.getGotoUrl());
							detail.put("code",application.getCode());
							//增加“应用是否可分享字段”
							detail.put("isShare", application.getIsShare());
							detail.put("description", application.getDescription());
							//返回数据增加应用id
							detail.put("id", application.getId());
							detail.put("level", application.getLevel());
							tempjo_app.put("detail",detail);
							ja_other.add(tempjo_app);
							break;
						case "news":
							Map<String,Object> tempMap = msgService.findNewsDetails(Long.parseLong(list.get(j).get("fkId").toString()));
							JSONObject tempjo_news = new JSONObject();
							tempjo_news.put("type","news" );
							//tempjo_news.put("name", list.get(j).get("spotName"));
							tempjo_news.put("name", tempMap.get("title"));
							tempjo_news.put("nameColor", list.get(j).get("spotNameColor"));
							tempjo_news.put("comment", list.get(j).get("spotComment"));
							tempjo_news.put("imageUrl",list.get(j).get("spotImageurl"));
							JSONObject newsDetail = new JSONObject();
							newsDetail.put("newsId", tempMap.get("id"));
							newsDetail.put("source",tempMap.get("source"));
							//newsDetail.put("imageUrl",tempMap.get("imgUrl"));
							//newsDetail.put("gotoUrl",tempMap.get("gotoUrl"));
							newsDetail.put("date",tempMap.get("createTime").toString().substring(0, 10));
							tempjo_news.put("detail",newsDetail);
							ja_other.add(tempjo_news);
							break;					
						}
					}
				}
				
				jo.put("value",ja_other);
				ja.add(jo);
				moduleId = Integer.parseInt(list.get(i).get("moduleId").toString());		
			}
		}
		}
		
		model.setCode("0000");
		model.setError("");
		model.setMessage("成功");
		model.setResult(ja);
		model.setState("1");
		logger.info("--------------getDynamicHomeApp(获取到全部module详细信息)-------------");
		}catch(Exception e){
			model.setCode("0100");
			model.setResult(ja);
	     	model.setError(e.toString());
	        model.setMessage("服务出错了！");
	     	model.setState("0");
	     	logger.error("--------------getDynamicHomeApp(error)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceName:动态首页接口"+"|"+"error:"+e.toString());
	     	e.printStackTrace();
		}
		logger.info("--------------getDynamicHomeApp(end)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:动态首页接口");
		return model;
	}
	
	//---------------------------------------------搜索接口--------------------------------------------
	@ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST)
	public Object search(String cityCode,String type,String condition,int newsCount){
		logger.info("--------------search(start)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:搜索接口"+"|"+"cityCode:"+cityCode+"|"+"condition:"+condition+"|"+"newsCount:"+newsCount);
		JsonResultModel model = getJsonResultModel();
		condition = condition.replaceAll("\\%", "\\\\%");
		condition = condition.replaceAll("\\_", "\\\\_");
		logger.info("------------------修改后的condition：" + condition);
		List<Object> resultList = new ArrayList<>();
		try{
			Map<String,Object> appResultMap = Maps.newHashMap();
			Map<String,Object> newsResultMap = Maps.newHashMap();
			List<Map<String,Object>> appList = new ArrayList<>();
			List<Map<String,Object>> newsList = new ArrayList<>();
			List<Map<String,Object>> tempNewsList = new ArrayList<>();
			logger.info("--------------search(根据查询条件到数据库搜索相关应用和咨询)-------------");
	        appList = applicationService.searchList(cityCode,condition);
	        if(newsCount == 0){
	        	newsList = msgService.findNews(type, cityCode, condition,Integer.MAX_VALUE);
	        	for(Map<String, Object> map : newsList){
	        		map.put("commentCount",commentService.findCommentCount(Constants.TYPE_NEWS,Long.valueOf(BeanUtil.nullValueOf(map.get("id")))));
	        	}	
	        }else{
	        	newsList = msgService.findNews(type, cityCode, condition,newsCount);
	        	for(Map<String, Object> map : newsList){
	        		map.put("commentCount",commentService.findCommentCount(Constants.TYPE_NEWS,Long.valueOf(BeanUtil.nullValueOf(map.get("id")))));
	        	}
	        }
			tempNewsList = msgService.findNews(type, cityCode, condition,Integer.MAX_VALUE);
			appResultMap.put("count", appList.size());
			appResultMap.put("value", appList);
			appResultMap.put("type", "app");
			newsResultMap.put("count", tempNewsList.size());
			newsResultMap.put("value", newsList);
			newsResultMap.put("type", "news");
			if(BeanUtil.isNullString(type)){
				resultList.add(appResultMap);
				resultList.add(newsResultMap);
			}else if(type.equalsIgnoreCase("news")){
				resultList.add(newsResultMap);
			}else if(type.equalsIgnoreCase("app")){
				resultList.add(appResultMap);
			}
			model.setCode("0000");
			model.setError("");
			model.setMessage("成功");
			model.setResult(resultList);
			model.setState("1");
			logger.info("--------------search(根据查询条件到数据库搜索相关应用和咨询)-------------");
           }  
		    catch(Exception e){
		    	model.setCode("0100");
				model.setResult(resultList);
		     	model.setError(e.toString());
		        model.setMessage(e.toString());
		     	model.setState("0");	
		     	logger.error("--------------search(error)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceName:搜索接口"+"|"+"error:"+e.toString());
	     	}
		logger.info("--------------search(end)-------------"+"|"+"fromModule:HomeController"+"|"+"interfaceInfo:搜索接口");
        return model;
	}
}
