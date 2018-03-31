package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.inspur.icity.logic.app.model.AppShareInfo;
import com.inspur.icity.logic.app.service.AppShareInfoService;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.InviteRecord;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.InviteRecordService;
import com.inspur.icity.logic.cust.service.MarkMissionService;
import com.inspur.icity.logic.cust.service.MyDeviceService;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;

@Controller
@RequestMapping(value = "/inviteCode")
public class InviteCodeController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	//邀请码
	public static final char[] chars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
	@Autowired
	CustomerService customerService;
	@Autowired
	InviteRecordService inviteRecordService;
	@Autowired
	MarkMissionService markMissionService;
	@Autowired
	MyDeviceService myDeviceService;
	@Autowired
	ActivityService activityService;
	@Autowired
	AppShareInfoService appShareInfoService;

	/**
	 * 
	 * @Title 邀请码接口
	 * @Description 获取邀请码，邀请记录，推荐人推荐码
	 * @return Object
	 * @author ZhangXingLiang
	 * @date 2017年6月5日下午8:39:37
	 */
	@ResponseBody
	@RequestMapping(value = "/inviteInfo", method = RequestMethod.GET)
	public Object getInviteCodeInfo() {
		logger.info("--------------getInviteCodeInfo(start)-------------" + "|" + "fromModule:InviteCodeController" + "|" + "interfaceInfo:获取邀请码信息" + "|" + "LoginUserId:" + getLoginUserId());

		JsonResultModel model = getJsonResultModel();
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> result = Maps.newHashMap();
			String inviteCode = null;
			Long custId = getLoginUserId();
			Customer customer = customerService.get(custId);
			if (customer != null) {
				inviteCode = customer.getInviteCode();
				if (inviteCode == null || "".equals(inviteCode)) {
					inviteCode = getInviteCodeString();
					customer.setInviteCode(inviteCode);
					customerService.update(customer);
				}
				result.put("inviteCode", customer.getInviteCode());
				List<Map<String, Object>> inviteRecords = inviteRecordService.getInviteRecord(customer.getInviteCode(), getPageBounds());
				result.put("inviteRecord", inviteRecords);
				InviteRecord inviteRecord = inviteRecordService.getInviteRecordByCustId(custId, null, "0");
				String referralCode = "";
				if (inviteRecord != null) {
					referralCode = inviteRecord.getInviteCode();
				}
				result.put("referralCode", referralCode);
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				model.setResult(list);
				logger.info("--------------getInviteCodeInfo(end)-------------|inviteCode:" + customer.getInviteCode() + "|inviteRecord:" + inviteRecord + "|referralCode:" + referralCode);

			} else {
				logger.info("--------------getInviteCodeInfo(未获取到用户信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
				model.setCode("0100");
				model.setError("系统未知异常！");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------getInviteCodeInfo(error)-------------" + "|" + "fromModule:InviteCodeController" + "|" + "interfaceName:获取邀请码信息" + "|" + "error:" + e.toString());
		}
		return model;

	}

	/**
	 * 
	 * @Title 添加推荐码
	 * @Description 添加邀请人
	 * @return Object
	 * @author ZhangXingLiang
	 * @date 2017年6月5日下午8:39:37
	 */
	@ResponseBody
	@RequestMapping(value = "/addReferralCode", params = { "referralCode" }, method = RequestMethod.POST)
	public Object addReferralCode(String referralCode) {
		logger.info("--------------addReferralCode(start添加推荐码)-------------" + "|" + "referralCode:" + referralCode);
		JsonResultModel model = getJsonResultModel();
		try {
			Long custId = getLoginUserId();
			Customer customer = customerService.getCustomerByInviteCode(referralCode);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> result = Maps.newHashMap();
			if (customer == null) {
				result.put("status", 0);//0：代表推荐码不存在；1：代表添加推荐人成功 ；2：推荐码已添加
				result.put("message", "推荐码不存在");
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------addReferralCode(推荐码不存在)-------------|Customer:" + customer + "|custId:" + custId);
			} else {
				InviteRecord inviteRecord = inviteRecordService.getInviteRecordByCustId(custId, null, "0");
				if (inviteRecord != null) {
					result.put("status", 2);//0：代表推荐码不存在；1：代表添加推荐人成功 ；2：推荐码已添加
					result.put("message", "推荐码已添加");
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				} else {
					inviteRecord = new InviteRecord(custId, referralCode, "0");
					inviteRecordService.add(inviteRecord);
					//给推荐人加积分
					Map<String, Object> markInfo = markMissionService.getMark("/inviteCode/inviteInfo", customer.getId(), "已完成");
					logger.info("--------------addReferralCode(给推荐人加积分)-------------" + "|" + "markInfo:" + markInfo);
					logger.info("--------------addReferralCode(添加推荐人推送活动开始)-------------");
					//注册后邀请人和推荐人可以参与活动
					String pushToken = getPushToken();
					if (BeanUtil.isNullString(pushToken)) {
						Map<String, Object> pushC = myDeviceService.getPushTokenByCustId(getLoginUserId());
						pushToken = (String) pushC.get("pushToken");
					}

					Map<String, Object> pushT = myDeviceService.getPushTokenByCustId(customer.getId());
					if (pushT.size() > 0) {
						pushToken = pushToken + "," + pushT.get("pushToken");
					}
					Accesstoken accessToken = getAccessToken();
					Long[] longs = new Long[] { getLoginUserId(), customer.getId() };
					String url = Config.getValue("sendMessageUrl");//应用中心路径
					activityService.inviteFriendsRegisterAfter(longs, pushToken, accessToken.getAccessToken(), getClientInfo(), url,Constants.ACTIVITY_TYPE_INVITE_CODE);
					logger.info("--------------addReferralCode(添加推荐人推送活动完成)-------------");

					result.put("status", 1);//0：代表推荐码不存在；1：代表添加推荐人成功 2：推荐码已添加
					result.put("message", "添加推荐人成功");
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------addReferralCode(end添加推荐人成功)-------------|Customer:" + customer + "|custId:" + custId);
				}
			}
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------addReferralCode(error)-------------" + "|" + "fromModule:InviteCodeController" + "|" + "interfaceName:添加推荐人" + "|" + "error:" + e.toString());
		}
		return model;

	}

	@ResponseBody
	@RequestMapping(value = "/shareApp", method = RequestMethod.GET)
	public Object getAppShareInfo() {
		JsonResultModel model = getJsonResultModel();
		try {
			Map<String, Object> appShareInfo = appShareInfoService.findByTitle(1L);
			logger.info("--------------getAppShareInfo(获取分享信息)-------------appShareInfo:" + appShareInfo);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			list.add(appShareInfo);
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------getAppShareInfo(error)-------------" + "|" + "fromModule:InviteCodeController" + "|" + "interfaceName:添加推荐人" + "|" + "error:" + e.toString());
		}
		return model;

	}

	/**
	 * 
	 * @Title 获取邀请码
	 * @Description 获取不和其他用户重复的邀请码
	 * @return String
	 * @author ZhangXingLiang
	 * @date 2017年6月5日下午6:05:37
	 */
	private String getInviteCodeString() {
		String invoteCode = getRandomString();
		boolean flag = false;
		List<String> list = customerService.getAllUserInviteCode();
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) != null && invoteCode.equals(list.get(i))) {
					flag = true;
					break;
				}
			}
		}
		return flag == true ? getInviteCodeString() : invoteCode;

	}

	/**
	 * 
	 * @Title 随机生成6位数字
	 * @Description 随机生成6位数字
	 * @return 6位数字的字符串
	 * @author ZhangXingLiang
	 * @date 2017年6月5日下午5:47:00
	 */
	private String getRandomString() {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i <= 5; i++) {
			sb.append(chars[random.nextInt(chars.length)]);
		}
		return sb.toString();
	}

}
