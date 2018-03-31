package com.inspur.icity.web.cust.service;

import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.logic.cust.model.SmsHist;
import com.inspur.icity.logic.cust.service.SmsHistService;
import com.inspur.icity.web.utils.Config;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jinan-dg-20150525 on 2016/4/22.
 */
@Service("WFAlidayuSmsService")
public class AlidayuSmsService {
    Logger logger = LoggerFactory.getLogger(getClass());
    private static String URL = Config.getValue("URL");//正式环境http请求地址

    private static String APPKEY = Config.getValue("APPKEY");
    private static String SECRET = Config.getValue("SECRET");
    private static String SMSTYPE = Config.getValue("SMSTYPE");//短信类型
    private static String SMSFREESIGNNAME = "爱城市网";//短信签名
    private static String SMSTEMPLATECODE_LOGIN = Config.getValue("SMSTEMPLATECODE_LOGIN");//短信模板id
    private static String SMSTEMPLATECODE_REGISTER = Config.getValue("SMSTEMPLATECODE_REGISTER");//短信模板id
    private static String SMSTEMPLATECODE_UPDATE = Config.getValue("SMSTEMPLATECODE_UPDATE");//短信模板id
    private static final String PRODUCT = "爱城市网";

    @Autowired
    SmsHistService smsHistService;

    public String sendSmsToRegister(String cellPhoneNumber, String code){
        TaobaoClient client = new DefaultTaobaoClient(URL, APPKEY, SECRET);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        //req.setExtend("23351277");
        req.setSmsType(SMSTYPE);
        req.setSmsFreeSignName(SMSFREESIGNNAME);
        req.setSmsParamString("{\"code\":\"" + code + "\",\"product\":\"" + PRODUCT +"\"}");
        req.setRecNum(cellPhoneNumber);
        req.setSmsTemplateCode(SMSTEMPLATECODE_REGISTER);
        AlibabaAliqinFcSmsNumSendResponse rsp = null;
        try {
            rsp = client.execute(req);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        SmsHist smsHist = new SmsHist();
        smsHist.setMobilePhone(cellPhoneNumber);
        smsHist.setSmsType(SMSTYPE);
        smsHist.setSmsFreeSignName(SMSFREESIGNNAME);
        smsHist.setSmsTemplateCode(SMSTEMPLATECODE_REGISTER);
        smsHist.setSmsParam("code:" + code + "," + "product:" + PRODUCT);
        smsHist.setReturnCode(rsp.getBody());
        smsHistService.add(smsHist);
        return rsp.getBody();
    }

    public String sendSmsToLogin(String cellPhoneNumber, String code, String type){
    	logger.info("---------sendSmsToLogin|type:"+type);
    	try {
			TaobaoClient client = new DefaultTaobaoClient(URL, APPKEY, SECRET);
        	logger.info("--------------getCode(设置第三方接口sendSmsToLogin相关属性)-------------"+"|"+"URL:"+URL+"|"+"APPKEY:"+APPKEY+"|"+"SECRET:"+SECRET);
        	String smsTemplateCode = "";
        	if("register".equals(type)){
        		smsTemplateCode = SMSTEMPLATECODE_REGISTER;
        	}else if("updatePassword".equals(type) || "findPassword".equals(type)){
        		smsTemplateCode = SMSTEMPLATECODE_UPDATE;
        	}else if("login".equals(type)){
        		smsTemplateCode = SMSTEMPLATECODE_LOGIN;
        	}else{
        		smsTemplateCode = SMSTEMPLATECODE_LOGIN;
        	}
			AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
			//req.setExtend("23351277");
			req.setSmsType(SMSTYPE);
			req.setSmsFreeSignName(SMSFREESIGNNAME);
			req.setSmsParamString("{\"code\":\"" + code + "\",\"product\":\"" + PRODUCT +"\"}");
			req.setRecNum(cellPhoneNumber);
			req.setSmsTemplateCode(smsTemplateCode);

			AlibabaAliqinFcSmsNumSendResponse rsp = null;
			rsp = client.execute(req);
			SmsHist smsHist = new SmsHist();
			smsHist.setMobilePhone(cellPhoneNumber);
			smsHist.setSmsType(SMSTYPE);
			smsHist.setSmsFreeSignName(SMSFREESIGNNAME);
			smsHist.setSmsTemplateCode(smsTemplateCode);
			smsHist.setSmsParam("code:" + code + "," + "product:" + PRODUCT);
			smsHist.setReturnCode(rsp.getBody());
			smsHistService.add(smsHist);
			return rsp.getBody();
		} catch (ApiException e) {
			logger.info("--------------getCode(error)-------------"+"|"+"fromModule:AlidayuSmsService"+"|"+"interfaceName:sendSmsToLogin()"+"|"+"error:"+e.toString());

			throw new ApplicationException(900,"获取手机验证码失败");
		}
    }

}
