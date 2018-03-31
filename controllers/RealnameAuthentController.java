package com.inspur.icity.web.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
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
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.CustomerExp;
import com.inspur.icity.logic.cust.model.RealNameAuth;
import com.inspur.icity.logic.cust.service.CheckService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.IdcardInfoExtractor;
import com.inspur.icity.logic.cust.service.MarkMissionService;
import com.inspur.icity.logic.cust.service.RealNameAuthService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;
import com.inspur.icity.web.utils.Constants.UmsAnswerCode;
import com.inspur.icity.web.utils.RealNameCheck;

import net.sf.json.JSONObject;

/**
 * 实名制认证
 * 
 * @author gaoheng
 *
 */
@Controller
@RequestMapping(value = "/realNameAuthent")
public class RealnameAuthentController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	// 手机号认证
	private static final String PHONE_URL = Config.getValue("PHONE_URL");
	// 手机号认证KEY
	private static final String PHONE_KEY = Config.getValue("PHONE_KEY");
	@Autowired
	CheckService customerExpService;
	@Autowired
	CustomerService customerService;
	@Autowired
	RealNameAuthService realNameAuthService;
	@Autowired
	MarkMissionService markMissionService;

	/**
	 * 手机号实名认证
	 * 
	 * @param realName
	 * @param idCard
	 * @param mobile
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/phoneCheck", params = { "realName", "idCard", "mobile" }, method = { RequestMethod.POST })
	public Object phoneCheck(String realName, String idCard, String mobile) {
		logger.info("--------------phoneCheck(start)-------------" + "|" + "fromModule:RealnameAuthentController" + "|"
				+ "interfaceInfo:手机号实名认证" + "|" + "realName:" + realName + "idCard" + idCard + "mobile" + mobile);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		Long custId = getLoginUserId();
		Map<String, Object> checkResult = checkNameIdCardMobile(realName, idCard, mobile, custId);
		if (checkResult.containsKey("flag")) {
			boolean flag = (boolean) checkResult.get("flag");
			if (flag == false) {
				RealNameAuth realNameAuth = (RealNameAuth) checkResult.get("obj");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("res", "0");
				map.put("msg", realNameAuth.getResmsg() == null ? "" : realNameAuth.getResmsg());
				list.add(map);
				model.setError(realNameAuth.getResmsg() == null ? "" : realNameAuth.getResmsg());
				model.setCode("0400");
				model.setMessage(realNameAuth.getResmsg() == null ? "" : realNameAuth.getResmsg());
				model.setState("1");
				model.setResult(list);
				return model;
			}
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("realname", realName);
		params.put("idcard", idCard.toUpperCase());
		params.put("mobile", mobile);
		params.put("key", PHONE_KEY);
		if (getLoginUser() != null) {
			if (!BeanUtil.isNullString(realName) && !BeanUtil.isNullString(idCard) && !BeanUtil.isNullString(mobile)) {
				try {
					JsonResultModel umsModel = umsRealNameCheck(params, custId);
					if(umsModel != null){
						return umsModel;
					}
//					String response = HttpUtil.get(PHONE_URL, params);
//					logger.info("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard
//							+ "mobile" + mobile + "|手机认证第三方接口调用结果:" + response);
//					// 获取json对象
//					JSONObject jsonResult = JsonUtil.strToJson(response);
//					String errorCode = jsonResult.getString("error_code");
//					if (!BeanUtil.isNullString(errorCode) && errorCode.equalsIgnoreCase("0")) {// 接口调用成功
//						JSONObject Result = jsonResult.getJSONObject("result");
//						Map<String, Object> map = new HashMap<String, Object>();
//						if (Result.getString("res").equalsIgnoreCase("1")) {
//							map.put("res", "1");
//							map.put("msg", "认证通过");
//							// 将认证信息进行持久化
//							CustomerExp customerExp = new CustomerExp();
//							customerExp.setCustId(getLoginUserId());
//							customerExp.setCustName(realName);
//							customerExp.setIdCard(idCard);
//							customerExp.setState(map.get("res").toString());
//							customerExp.setMobilePhone(customerService.get(getLoginUserId()).getMobilePhone());
//							customerExp.setCheckPhone(mobile);
//							customerExp.setErrorCode(errorCode);
//							customerExp.setRegisterResult(map.get("msg").toString());
//							customerExp.setRegisterType("3");
//							saveCheckInfo(customerExp);// 进行数据持久化
//							// 更新账户表
//							Customer customer = getLoginUser();
//							customer.setIsRealName(customerExp.getState());
//							// 更新账户生日和性别信息
//							IdcardInfoExtractor idcardInfo = new IdcardInfoExtractor(idCard);
//							// String birthday =
//							// idcardInfo.getYear()+"-"+idcardInfo.getMonth()+"-"+idcardInfo.getDay();
//							// SimpleDateFormat sdf = new SimpleDateFormat( "
//							// yyyy-MM-dd " );
//							// Date birth = sdf.parse(birthday);
//							String birthday = idcardInfo.getYear() + "-" + idcardInfo.getMonth() + "-"
//									+ idcardInfo.getDay();
//							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//							Date birth = sdf.parse(birthday);
//							customer.setBirthday(birth);
//							String sex = idcardInfo.getGender();
//							customer.setSex(sex);
//							
//							// 实名认证后加积分，为一次性任务。
//							customerService.updateRealNameSign(customer);
//							Map<String, Object> markInfo = markMissionService.getMark("/realNameAuthent/phoneCheck", customer.getId(),
//									"已完成");
//							list.add(map);
//							model.setMarkInfo(markInfo);
//							
//							// 清除session，通过拦截器重新更新session信息
//							getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
//							// 保存到cust_realnameauth表
//							RealNameAuth realNameAuth = new RealNameAuth();
//							realNameAuth.setCustId(custId);
//							realNameAuth.setName(realName);
//							realNameAuth.setIdCard(idCard);
//							realNameAuth.setMobile(mobile);
//							realNameAuth.setErrorcode(errorCode);
//							realNameAuth.setRes(Result.getString("res"));
//							realNameAuth.setResmsg(Result.getString("resmsg"));
//							realNameAuthService.add(realNameAuth);
//						} else {
//							/***********************调用银联商务的实名认证接口（start）*******************/
//							JsonResultModel umsModel = umsRealNameCheck(params, custId);
//							if(umsModel != null){
//								return umsModel;
//							}
//							/***************** 调用银联商务的实名认证接口（end）***************/
//							
//							logger.error("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard
//									+ "mobile" + mobile + "|手机认证第三方接口调用失败Result:" + Result.getString("resmsg"));
//							
//							// errorcode=0 res=2 认证未通过,保存到数据库
//							RealNameAuth realNameAuth = new RealNameAuth();
//							realNameAuth.setCustId(custId);
//							realNameAuth.setName(realName);
//							realNameAuth.setIdCard(idCard);
//							realNameAuth.setMobile(mobile);
//							realNameAuth.setErrorcode(errorCode);
//							realNameAuth.setRes(Result.getString("res"));
//							realNameAuth.setResmsg(Result.getString("resmsg"));
//							realNameAuthService.add(realNameAuth);
//
//							map.put("res", "0");
//							map.put("msg", Result.getString("resmsg"));
//							list.add(map);
//							model.setError(Result.getString("resmsg"));
//							model.setCode("0400");
//							model.setMessage(Result.getString("resmsg"));
//							model.setState("1");
//							model.setResult(list);
//							return model;
//						}
//						
//						model.setResult(list);
//						model.setCode("0000");
//						model.setError("");
//						model.setMessage("调用成功");
//						model.setState("1");
//					} else {
//						
//						/***********************调用银联商务的实名认证接口（start）*******************/
//						JsonResultModel umsModel = umsRealNameCheck(params, custId);
//						if(umsModel != null){
//							return umsModel;
//						}
//						/***************** 调用银联商务的实名认证接口（end）***************/
//						
//						logger.error("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard
//								+ "mobile" + mobile + "|手机认证第三方接口调用失败errorCode:" + errorCode);
//						String message = "";
//						switch (errorCode) {
//						case "220801":
//							message = "服务商网络异常，请重试";
//							model.setError(message);
//							break;
//						case "220802":
//							message = "认证失败";
//							model.setError(message);
//							break;
//						case "220804":
//							message = "手机号码格式不正确";
//							model.setError(message);
//							break;
//						case "220805":
//							message = "姓名不合法";
//							model.setError(message);
//							break;
//						case "220806":
//							message = "身份证号码不合法";
//							model.setError(message);
//							break;
//						default:
//							message = "认证失败";
//							model.setError(message);
//						}
//
//						model.setCode("0400");
//						model.setMessage(message);
//						model.setState("0");
//						model.setResult(list);
//						// 保存到cust_realnameauth
//						RealNameAuth realNameAuth = new RealNameAuth();
//						realNameAuth.setCustId(custId);
//						realNameAuth.setName(realName);
//						realNameAuth.setIdCard(idCard);
//						realNameAuth.setMobile(mobile);
//						realNameAuth.setErrorcode(errorCode);
//						realNameAuth.setRes("");
//						realNameAuth.setResmsg(message);
//						realNameAuthService.add(realNameAuth);
//					}
				} catch (Exception e) {
					logger.error("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard
							+ "mobile" + mobile + "|手机认证第三方接口调用失败:" + e.toString());
					model.setCode("0100");
					model.setMessage("调用失败");
					model.setError("系统未知异常！");
					model.setState("0");
					model.setResult(list);
				}
			} else {
				logger.error("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard + "mobile"
						+ mobile + "|手机认证第三方接口调用失败:参数缺失");
				model.setCode("0203");
				model.setMessage("调用失败");
				model.setError("参数缺失");
				model.setState("0");
				model.setResult(list);
			}
		} else {
			logger.error("----------------phoneCheck(手机号实名认证)|realName:" + realName + "idCard" + idCard + "mobile"
					+ mobile + "|手机认证第三方接口调用失败:未登录");
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		logger.info("--------------phoneCheck(end)-------------" + "|" + "fromModule:RealnameAuthentController" + "|"
				+ "interfaceInfo:手机号实名认证" + "|" + "realName:" + realName + "idCard" + idCard + "mobile" + mobile);
		return model;
	}
	
	/**
	 * 
	 * @Title umsRealNameCheck
	 * @Description 调用银联实名认证接口
	 * @param params
	 * @param custId
	 * @return JsonResultModel
	 * @throws ParseException
	 * @author meng-ke
	 * @date 2017年6月9日下午8:16:02
	 */
	private JsonResultModel umsRealNameCheck(Map<String, String> params, Long custId) throws ParseException{
		logger.info("-----------------umsRealNameCheck----------参数|params:"+params+"|custId"+custId);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		String realName = params.get("realname");
		String idCard = params.get("idcard");
		String mobile = params.get("mobile");
		RealNameCheck realNameCheck = RealNameCheck.getInstance();
		String accessToken = realNameCheck.getAccessToken();
		logger.info("-----------------umsRealNameCheck----------获取的accessToken："+accessToken);
		if(!"".equals(accessToken)){
			String checkStr = realNameCheck.realNameCHeck(accessToken, params);
			//银联实名认证返回结果
			JSONObject checkJson = JSONObject.fromObject(checkStr);
			String errCode = checkJson.get("errCode")==null?"":checkJson.get("errCode").toString();
			if(Constants.UMS_NORMAL_CODE.equals(errCode)){//接口返回正常
				JSONObject data = checkJson.getJSONObject("data");
				String respCode = data.get("respCode")==null?"":data.get("respCode").toString();
				String respMsg = data.get("respMsg")==null?"":data.get("respMsg").toString();
//				EnumMap<UmsAnswerCode, String> currEnumMap = new EnumMap<UmsAnswerCode, String>(UmsAnswerCode.class);
				if(Constants.UmsAnswerCode.SUCCESS.getValue().equals(respCode)){//认证成功
					map.put("res", "1");
					map.put("msg", "认证通过");
					// 将认证信息进行持久化
					CustomerExp customerExp = new CustomerExp();
					customerExp.setCustId(getLoginUserId());
					customerExp.setCustName(realName);
					customerExp.setIdCard(idCard);
					customerExp.setState("1");//1是认证通过
					customerExp.setMobilePhone(customerService.get(getLoginUserId()).getMobilePhone());
					customerExp.setCheckPhone(mobile);
					customerExp.setErrorCode(errCode);
					customerExp.setRegisterResult(respMsg);
					customerExp.setRegisterType("3");
					saveCheckInfo(customerExp);// 进行数据持久化
					// 更新账户表
					Customer customer = getLoginUser();
					customer.setIsRealName(customerExp.getState());
					// 更新账户生日和性别信息
					IdcardInfoExtractor idcardInfo = new IdcardInfoExtractor(idCard);
					String birthday = idcardInfo.getYear() + "-" + idcardInfo.getMonth() + "-"
							+ idcardInfo.getDay();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date birth = sdf.parse(birthday);
					customer.setBirthday(birth);
					String sex = idcardInfo.getGender();
					customer.setSex(sex);
					
					// 实名认证后加积分，为一次性任务。
					customerService.updateRealNameSign(customer);
					Map<String, Object> markInfo = markMissionService.getMark("/realNameAuthent/phoneCheck", customer.getId(),
							"已完成");
					list.add(map);
					model.setMarkInfo(markInfo);
					
					// 清除session，通过拦截器重新更新session信息
					getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
					// 保存到cust_realnameauth表
					RealNameAuth realNameAuth = new RealNameAuth();
					realNameAuth.setCustId(custId);
					realNameAuth.setName(realName);
					realNameAuth.setIdCard(idCard);
					realNameAuth.setMobile(mobile);
					realNameAuth.setErrorcode(errCode);
					realNameAuth.setRes("1");//1匹配
					realNameAuth.setResmsg(respMsg);
					realNameAuthService.add(realNameAuth);
					
					list.add(map);
					model.setResult(list);
					model.setCode("0000");
					model.setError("");
					model.setMessage("调用成功");
					model.setState("1");
					return model;
				}else if(Constants.UmsAnswerCode.FAIL.getValue().equals(respCode)){//认证失败
					String detailRespCode = data.get("detailRespCode")==null?"":data.get("detailRespCode").toString();
					String message = "";
					switch (detailRespCode) {
					case "1001":
						message = "手机号、证件号、姓名不一致";
						model.setError(message);
						break;
					case "1002":
						message = "手机号一致，证件号和姓名不一致";
						model.setError(message);
						break;
					case "1003":
						message = "手机号和证件号一致，姓名不一致";
						model.setError(message);
						break;
					case "1004":
						message = "手机号和姓名一致，证件号不一致";
						model.setError(message);
						break;
					case "1005":
						message = "未查得";
						model.setError(message);
						break;
					case "1006":
						message = "非正常号码";
						model.setError(message);
						break;
					case "1007":
						message = "用户未实名";
						model.setError(message);
						break;
					case "1013":
						message = "请求超过总次数限制";
						model.setError(message);
						break;
					case "1019":
						message = "手机号非法或不存在";
						model.setError(message);
						break;
					default:
						message = "认证失败";
						model.setError(message);
					}

					model.setCode("0400");
					model.setMessage(message);
					model.setState("0");
					model.setResult(list);
					// 保存到cust_realnameauth
					RealNameAuth realNameAuth = new RealNameAuth();
					realNameAuth.setCustId(custId);
					realNameAuth.setName(realName);
					realNameAuth.setIdCard(idCard);
					realNameAuth.setMobile(mobile);
					realNameAuth.setErrorcode(respCode);
					realNameAuth.setRes("");
					realNameAuth.setResmsg(message);
					realNameAuthService.add(realNameAuth);
					return model;
				}else{//其他原因认证失败
					model.setCode("0400");
					model.setMessage(respMsg);
					model.setState("0");
					model.setResult(list);
					// 保存到cust_realnameauth
					RealNameAuth realNameAuth = new RealNameAuth();
					realNameAuth.setCustId(custId);
					realNameAuth.setName(realName);
					realNameAuth.setIdCard(idCard);
					realNameAuth.setMobile(mobile);
					realNameAuth.setErrorcode(respCode);
					realNameAuth.setRes("");
					realNameAuth.setResmsg(respMsg);
					realNameAuthService.add(realNameAuth);
					return model;
				}
			}
		}
		return null;
	}

	/**
	 * 保存验证信息
	 * 
	 * @param customerExp
	 * @return
	 */
	private boolean saveCheckInfo(CustomerExp customerExp) {
		try {
			// 进行持久化操作
			if (customerExpService.isExistCustomer(customerExp.getCustId(), customerExp.getRegisterType())) {
				// 存在已有数据进行更新
				customerExpService.updateCustomerExp(customerExp);
			} else {
				// 不存在数据进行添加
				customerExpService.addCustomerExp(customerExp);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Description:验证身份三要素之前是否存在未认证通过的情况，如果存在则直接返回提示， 不继续调用第三方接口；
	 *                                                 如果不存在则继续验证该用户当天是否已经认证超过3次
	 *                                                 ，如果超过则直接返回提示，不继续调用第三方接口
	 * @param name
	 *            姓名
	 * @param idCard
	 *            身份证号
	 * @param mobile
	 *            手机号
	 * @param custId
	 *            用户id
	 * @return Map<String,Object>
	 * @author: MengKe
	 * @time:2017年3月2日 上午9:13:09
	 *
	 */
	private Map<String, Object> checkNameIdCardMobile(String name, String idCard, String mobile, Long custId) {
		logger.info("-------------checkNameIdCardMobile参数:" + "|name:" + name + "|idCard" + idCard + "|mobile:" + mobile
				+ "|custId:" + custId);
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 根据三要素身份查询cust_realnameauth表
			List<RealNameAuth> list = realNameAuthService.getInfoByNameIdCardMobile(name, idCard, mobile);
			if (list != null && list.size() > 0) {// 当天表中已经存在数据，说明之前该三要素未认证通过
				map.put("flag", false);
				map.put("obj", list.get(0));
			} else {
				// 查询一天内同一用户未认证通过的记录
				List<RealNameAuth> list_cust = realNameAuthService.getInfoByCustIdWithinADay(custId);
				if (list_cust != null && list_cust.size() >= 3) {// 一天内同一用户最多认证3次
					map.put("flag", false);
					RealNameAuth realNameAuth = list_cust.get(0);
					realNameAuth.setResmsg("实名认证次数已达上限");
					map.put("obj", realNameAuth);
				} else {
					map.put("flag", true);
				}
			}
		} catch (Exception e) {
			logger.error("-----------checkNameIdCardMobile出现异常:" + "|error:" + e.toString());
			e.printStackTrace();
		}
		return map;
	}
	// public static void main(String[] args) throws ParseException{
	// String idCard = "37158119960101715X";
	// IdcardInfoExtractor idcardInfo=new IdcardInfoExtractor(idCard);
	// String birthday =
	// idcardInfo.getYear()+"-"+idcardInfo.getMonth()+"-"+idcardInfo.getDay();
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	// Date birth = sdf.parse(birthday);
	// System.out.print(birth);
	//
	// }
}
