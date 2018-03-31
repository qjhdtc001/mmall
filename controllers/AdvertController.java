package com.inspur.icity.web.controllers;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.web.service.AdvertCtrService;
/**
 * 应用启动画面的广告获取接口
 */
@Controller
@RequestMapping(value = "/Advert")
public class AdvertController extends BaseAuthController {
	
	 Logger logger = LoggerFactory.getLogger(getClass());
	 @Autowired
	 AdvertCtrService advertCtrService;
	 
	 /*
	  * 上线的广告
	  * @return
	  */
	 @ResponseBody
	 @RequestMapping(value = "/findAdvert",method = RequestMethod.GET)
	 public Object findAdvert(){
		 logger.info("--------------findAdvert(start)-------------"+"|"+"fromModule:AdvertController"+"|"+"interfaceInfo:上线的广告");
		 
		 List<Object> list = new ArrayList<Object>();
		 JsonResultModel model = getJsonResultModel();		 
		 try{
			 	ClientInfo clientInfo = getClientInfo();
			 	HttpSession session = getSession();
			 	list = (List<Object>) advertCtrService.findAdvert(clientInfo, session);
			 	logger.info("--------------findAdvert(从数据库获取相关信息)-------------");
			    model.setCode("0000");
				model.setResult(list);
		     	model.setError("");
		        model.setMessage("成功");
		     	model.setState("1"); 
		     	logger.info("--------------findAdvert(获取上线广告的相关信息)-------------");
		     	logger.info("--------------findAdvert(end)-------------"+"|"+"fromModule:AdvertController"+"|"+"interfaceInfo:上线的广告");
		 }catch(Exception e){
			    model.setCode("0100");
				model.setResult(list);
		     	model.setError(e.toString());
		        model.setMessage("服务出错了！");
		     	model.setState("0"); 
		     	logger.info("--------------findAdvert(error)-------------"+"|"+"fromModule:AdvertController"+"|"+"interfaceName:findAdvert"+"|"+"error:"+e.toString());
		 }
		 		 
		 return model;
	 }
}