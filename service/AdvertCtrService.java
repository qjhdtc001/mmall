package com.inspur.icity.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.logic.app.model.AppLink;
import com.inspur.icity.logic.app.service.AppLinkService;
import com.inspur.icity.logic.app.service.AppRecommentService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.operating.model.Activity;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.web.controllers.AuthController;
import com.inspur.icity.logic.app.service.AdvertService;

@Service
public class AdvertCtrService {
	 @Autowired
	 AdvertService advertService;
	 @Autowired
	 ApplicationService applicationService;
	 @Autowired
	 AppRecommentService appRecommentService;
	 @Autowired
	 MsgService msgService;
	 @Autowired
	 AppLinkService appLinkService;
	 @Autowired
	 ActivityService activityService;
	 @Autowired
	 CustomerService customerService;
	//获取广告页
	public Object findAdvert(ClientInfo clientInfo, HttpSession session) throws Exception{
		String cityCode = "";//header中的cityCode
		String token = "";//header中的access_token
		try{
			if(clientInfo !=null){
				cityCode = clientInfo.getCityCode();
				Accesstoken accessToken =  (Accesstoken) session.getAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
				if(accessToken != null){
					token = accessToken.getAccessToken();
				}
			}
			//在老版本注册的用户在新版本登录的时候，此时header里的cityCode不为空;新用户在注册的时候registerCityCode不为空
			if(!BeanUtil.isNullString(cityCode) && "".equals(token)){
				Customer customerFromMobile = customerService.getByAccessToken(token);
				if(BeanUtil.isNullString(customerFromMobile.getRegisterCityCode())){
					customerFromMobile.setRegisterCityCode(cityCode);
					customerService.update(customerFromMobile);
				}
			}
		}catch(Exception e){
		}
		List<Object> list = new ArrayList<Object>();
		List<Map<String,Object>> tempList = new ArrayList<>();
		 //获取advert信息
		 tempList = advertService.findAdvert();			
		 for(int i = 0;i < tempList.size();i++){
			 Map<String,Object> map = new HashMap<>();
			 map.put("id",tempList.get(i).get("id").toString());
			 map.put("advert_name", tempList.get(i).get("advert_name"));
			 if(!BeanUtil.isNullString(tempList.get(i).get("imgUrl").toString())){
			      map.put("imgUrl",tempList.get(i).get("imgUrl"));	
			 }else{
				 map.put("imgUrl","");
			 }	
			 map.put("online", tempList.get(i).get("online"));
			 map.put("createTime", tempList.get(i).get("createTime").toString().substring(0, 19));
			 map.put("cityCode", tempList.get(i).get("cityCode"));
			 map.put("type", tempList.get(i).get("type"));
			 
			 //根据不同类型，获取不同内容
			 switch(tempList.get(i).get("type").toString()){
			 case "none":
				 break;
			 case "web":
				 AppLink applink = new AppLink();
				 applink =  appLinkService.findLink(tempList.get(i).get("relevanceid").toString());
				 map.put("gotoUrl", applink.getGotoUrl());
				 map.put("relTitle", applink.getTitle());
				 break;
			 case "operation":
				 Activity activity = new Activity();
				 activity =  activityService.get(Long.valueOf(tempList.get(i).get("relevanceid").toString()).longValue());
				 map.put("gotoUrl", activity.getGotoUrl());
				 map.put("relTitle", activity.getName());
				 map.put("isShare", activity.getIsShare());
				 map.put("shareUrl", activity.getShareUrl());
				 map.put("description", activity.getDescription());
				 map.put("id", activity.getId());
				 map.put("type", "web");
				 break;
			 case "app":
				 List<Map<String,Object>> appResult = new ArrayList<Map<String,Object>>();
				 appResult = applicationService.appHomelist(tempList.get(i).get("cityCode").toString(), Long.parseLong(tempList.get(i).get("relevanceid").toString()));
				 if(appResult.size() == 0){
				 appResult = appRecommentService.findGovAppHome(tempList.get(i).get("cityCode").toString(), Long.parseLong(tempList.get(i).get("relevanceid").toString()));					
				 }
				 map.put("value", appResult.get(0));
				 break;
			 case "news":
				 List<Map<String,Object>> newsResult = new ArrayList<Map<String,Object>>();
				 newsResult = msgService.findNewsHomeList(tempList.get(i).get("cityCode").toString(), Long.parseLong(tempList.get(i).get("relevanceid").toString()));
				 map.put("value", newsResult.get(0));
				 break;
			 }
			 list.add(map);		 
	}
		 return list;
}
	
}