package com.inspur.icity.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.HotFixDevice;
import com.inspur.icity.logic.cust.service.CheckService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.HotFixDeviceService;

import net.sf.json.JSONObject;

/**
 * @ClassName HotFixController
 * @Description 热修复相关接口 
 * @author ZhangXingLiang
 * @date 2017年5月31日 下午3:54:29
 */
@Controller
@RequestMapping(value = "/hotFix")
public class HotFixController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	CheckService checkService;
	@Autowired
	CustomerService customerService;
	@Autowired
	HotFixDeviceService hotFixDeviceService;

	/**
	 * 
	 * @Title 热修复
	 * @Description 添加热修复设备信息
	 * @param jsonStr void
	 * @author ZhangXingLiang
	 * @date 2017年5月31日下午3:57:31
	 */
	@ResponseBody
	@RequestMapping(value = "/add", params = { "jsonStr" }, method = RequestMethod.POST)
	public void addHotFixDeviceInfo(String jsonStr) {
		logger.info("--------------addHotFixDeviceInfo(start)-------------|fromModule:HotFixController|interfaceInfo:(参数)|jsonStr:" + jsonStr);
		try {
			// 解析参数
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			String appVersion = jsonObject.getString("appVersion");
			String code = jsonObject.getString("code");
			String deviceToken = jsonObject.getString("deviceToken");
			String info = jsonObject.getString("info");
			String model = jsonObject.getString("model");
			String phoneNum = jsonObject.getString("phoneNum");
			String os = jsonObject.getString("os");
			String osVersion = jsonObject.getString("osVersion");
			//获取用户名字从实名认证表中获取
			String userName = checkService.getRealUserByPhone(phoneNum);
			if (userName == null) {
				//从用户表中获取
				Customer customer = customerService.getByMobilePhone(phoneNum, 0L);
				userName = customer.getNickName();
			}
			logger.info("--------------addHotFixDeviceInfo(用户姓名)----------|userName:" + userName);
			HotFixDevice hotFixDevice = hotFixDeviceService.getHotFixByCondition(phoneNum, code, deviceToken);
			logger.info("--------------addHotFixDeviceInfo(是否已存在热修复设备信息)----------|hotFixDevice:" + hotFixDevice);
			if (hotFixDevice != null) {
				hotFixDevice.setAppVersion(appVersion);
				hotFixDevice.setCode(code);
				hotFixDevice.setDeviceToken(deviceToken);
				hotFixDevice.setInfo(info);
				hotFixDevice.setModel(model);
				hotFixDevice.setName(userName);
				hotFixDevice.setOs(os);
				hotFixDevice.setOsVersion(osVersion);
				hotFixDevice.setPhoneNum(phoneNum);
				hotFixDeviceService.updateByCondition(hotFixDevice);
				logger.info("--------------addHotFixDeviceInfo(更新热修复设备信息)----------|hotFixDevice:" + hotFixDevice);
			} else {
				hotFixDevice = new HotFixDevice();
				hotFixDevice.setAppVersion(appVersion);
				hotFixDevice.setCode(code);
				hotFixDevice.setDeviceToken(deviceToken);
				hotFixDevice.setInfo(info);
				hotFixDevice.setModel(model);
				hotFixDevice.setName(userName);
				hotFixDevice.setOs(os);
				hotFixDevice.setOsVersion(osVersion);
				hotFixDevice.setPhoneNum(phoneNum);
				hotFixDeviceService.add(hotFixDevice);//添加热修复设备信息
			}

			logger.info("--------------addHotFixDeviceInfo(end)-------------|fromModule:HotFixController|hotFixDevice:" + hotFixDevice);
		} catch (Exception e) {
			logger.error("--------------addHotFixDeviceInfo(error)----------|fromModule:HotFixController|interfaceName:添加热修复设备信息|error:" + e.toString());
		}
	}
}
