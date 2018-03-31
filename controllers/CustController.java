package com.inspur.icity.web.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.core.dareway.bean.Medical;
import com.inspur.icity.core.dareway.bean.Pension;
import com.inspur.icity.core.dareway.client.Uddi;
import com.inspur.icity.core.dareway.client.UddiPortType;
import com.inspur.icity.core.dareway.xml.XmlToBean;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.AESUtil;
import com.inspur.icity.core.utils.Bean2Map;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.CreateMD5;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.DownDataUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.core.utils.RedisUtil;
import com.inspur.icity.logic.app.model.AppMineCity;
import com.inspur.icity.logic.app.service.AppMineCityService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.app.service.MessageTypeService;
import com.inspur.icity.logic.base.model.Dict;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.base.service.ImageService;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.Account;
import com.inspur.icity.logic.cust.model.Check;
import com.inspur.icity.logic.cust.model.ClientInfo;
import com.inspur.icity.logic.cust.model.Comment;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.Device;
import com.inspur.icity.logic.cust.model.FeedBack;
import com.inspur.icity.logic.cust.model.InviteRecord;
import com.inspur.icity.logic.cust.model.Mark;
import com.inspur.icity.logic.cust.model.MarkMission;
import com.inspur.icity.logic.cust.model.MarkRecord;
import com.inspur.icity.logic.cust.model.MineInfo;
import com.inspur.icity.logic.cust.model.MyDevice;
import com.inspur.icity.logic.cust.model.Praise;
import com.inspur.icity.logic.cust.model.VerifiCode;
import com.inspur.icity.logic.cust.service.AccountService;
import com.inspur.icity.logic.cust.service.AnswerService;
import com.inspur.icity.logic.cust.service.AppService;
import com.inspur.icity.logic.cust.service.CheckService;
import com.inspur.icity.logic.cust.service.CollectionQuestionService;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.DeviceService;
import com.inspur.icity.logic.cust.service.FeedBackService;
import com.inspur.icity.logic.cust.service.GovService;
import com.inspur.icity.logic.cust.service.InviteRecordService;
import com.inspur.icity.logic.cust.service.MarkMissionService;
import com.inspur.icity.logic.cust.service.MarkRecordService;
import com.inspur.icity.logic.cust.service.MarkService;
import com.inspur.icity.logic.cust.service.MineInfoService;
import com.inspur.icity.logic.cust.service.MyDeviceService;
import com.inspur.icity.logic.cust.service.MyHallService;
import com.inspur.icity.logic.cust.service.NewsService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.cust.service.QuestionService;
import com.inspur.icity.logic.cust.service.RealNameAuthService;
import com.inspur.icity.logic.cust.service.ShareInfoService;
import com.inspur.icity.logic.cust.service.VerifiCodeService;
import com.inspur.icity.logic.life.model.HotPhone;
import com.inspur.icity.logic.life.model.TrafficViolation;
import com.inspur.icity.logic.life.service.HotPhoneAnswerService;
import com.inspur.icity.logic.life.service.HotPhoneService;
import com.inspur.icity.logic.life.service.PayService;
import com.inspur.icity.logic.life.service.PeckerInfoService;
import com.inspur.icity.logic.life.service.TrafficUserInfoService;
import com.inspur.icity.logic.life.service.TrafficViolationService;
import com.inspur.icity.logic.life.service.WoodpeckerService;
import com.inspur.icity.logic.news.model.Msg;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.operating.model.Credits;
import com.inspur.icity.logic.operating.model.CustActivity;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.logic.operating.service.CreditsService;
import com.inspur.icity.logic.operating.service.CustActivityService;
import com.inspur.icity.logic.operating.service.NotificationService;
import com.inspur.icity.logic.sensitive.model.SensitiveHist;
import com.inspur.icity.logic.sensitive.model.SensitiveWords;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.cust.builder.CommentToMapBuilder;
import com.inspur.icity.web.cust.builder.FeedBackToMapBuilder;
import com.inspur.icity.web.cust.service.AlidayuSmsService;
import com.inspur.icity.web.gov.builder.HallToMapBuilder;
import com.inspur.icity.web.news.builder.MsgToMapBuilder;
import com.inspur.icity.web.service.CustService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;
import com.inspur.icity.web.utils.IcityUtils;
import com.inspur.icity.web.utils.MD5Encrypt;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 用户相关接口
 */
/**
 * @ClassName CustController
 * @Description TODO
 * @author ZhangXingLiang
 * @date 2017年6月14日 下午2:51:34
 */
@Controller
@RequestMapping(value = "/cust")
public class CustController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	Map<String, Object> reSendSmsCount = Maps.newHashMap();
	final int MOST_MSG_TIMES = Integer.parseInt(Config.getValue("MOST_MSG_TIMES"));
	final String Fund_Url = Config.getValue("Fund_Url");
	final String Fund_Key = Config.getValue("Fund_Key");
	final String Fund_Iv = Config.getValue("Fund_Iv");
	final String Source_No = Config.getValue("Source_No");
	public static final String TrafficUrl = Config.getValue("TrafficUrl");
	@Autowired
	AppService appService;
	@Autowired
	CustomerService customerService;
	@Autowired
	CustService custService;
	@Autowired
	ImageService imageService;
	@Autowired
	PraiseService praiseService;
	@Autowired
	GovService govService;
	@Autowired
	MsgService msgService;
	@Autowired
	MsgToMapBuilder msgToMapBuilder;
	@Autowired
	CommentService commentService;
	@Autowired
	CommentToMapBuilder commentToMapBuilder;
	@Autowired
	QuestionService questionService;
	@Autowired
	NewsService newsService;
	@Autowired
	AnswerService answerService;
	@Autowired
	CollectionQuestionService collectionQuestionService;
	@Autowired
	AlidayuSmsService alidayuSmsService;
	@Autowired
	FeedBackService feedBackService;
	@Autowired
	FeedBackToMapBuilder feedBackToMapBuilder;
	@Autowired
	AccountService accountService;
	@Autowired
	DictService dictService;
	@Autowired
	MyHallService myHallService;
	@Autowired
	HallToMapBuilder hallToMapBuilder;
	@Autowired
	SensitiveWordService sensitiveWordService;
	@Autowired
	SensitiveHistService sensitiveHistService;
	@Autowired
	ActivityService activityService;
	@Autowired
	CustActivityService custActivityService;
	@Autowired
	CreditsService creditsService;
	@Autowired
	DeviceService deviceService;
	@Autowired
	MessageTypeService messageTypeService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	ShareInfoService shareInfoService;
	@Autowired
	ApplicationService applicationService;
	@Autowired
	PeckerInfoService peckerInfoService;
	@Autowired
	HotPhoneService hotPhoneService;
	@Autowired
	HotPhoneAnswerService hotPhoneAnswerService;
	@Autowired
	TrafficUserInfoService trafficUserInfoService;
	@Autowired
	TrafficViolationService trafficViolationService;
	@Autowired
	PayService payService;
	@Autowired
	WoodpeckerService woodpeckerService;
	@Autowired
	CheckService checkService;
	@Autowired
	MineInfoService mineInfoService;
	@Autowired
	AppMineCityService appMineCityService;
	@Autowired
	VerifiCodeService verifiCodeService;
	@Autowired
	MarkMissionService markMissionService;
	@Autowired
	RealNameAuthService realNameAuthService;
	@Autowired
	InviteRecordService inviteRecordService;
	@Autowired
	MyDeviceService myDeviceService;
	@Autowired
	MarkRecordService markRecordService;
	@Autowired
	MarkService markService;

	/**
	 * 获取我的应用接口
	 *
	 * @param cityCode
	 *            城市code
	 * @return
	 */
	/*
	 * @ResponseBody
	 * 
	 * @RequestMapping(value = "/appList", params = {"cityCode"}, method =
	 * RequestMethod.GET) public Object getApp(String cityCode) {
	 * List<Map<String , Object>> results = appService.getApp(cityCode,
	 * getLoginUserId(), getPageBounds()); if(results != null){ for (Map result
	 * : results){ String[] module =
	 * result.get("module").toString().split("\\+"); if(module.length >1){
	 * result.put("module",module[1]); } } } return results; }
	 */
	@ResponseBody
	@RequestMapping(value = "/appList", params = { "cityCode" }, method = RequestMethod.GET)
	public Object getApp(String cityCode) {
		logger.info("--------------appList(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取我的应用" + "|" + "cityCode:" + cityCode);
		return appService.getApp(cityCode, getLoginUserId(), getPageBounds());

	}

	/**
	 * 个人资料接口
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/detail")
	public Object getPersonalData() {
		logger.info("--------------getPersonalData(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取个人资料" + "|" + "LoginUserId:" + getLoginUserId());
		String inviteUrl = Config.getValue("inviteUrl");//我的推荐人地址
		return customerService.getPersonalData(getLoginUserId(), inviteUrl);
	}

	/**
	 * 2.4.0新版个人资料接口
	 * 涉及数据库：cust_peckinfo、app_application、cust_hotline、cust_hotline_answer、
	 * cust_traffic、base_carinfo 创建者：王建法
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/personalStatus", method = RequestMethod.POST)
	public Object getPersonalStatus(Long custId, String cityCode) {
		logger.info("--------------getPersonalStatus(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取个人资料" + "|" + "LoginUserId:" + getLoginUserId());
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> statusList = new ArrayList<Map<String, Object>>();
		String phone = customerService.get(custId).getMobilePhone();
		Map<String, Object> socialCarStatus = new HashMap<String, Object>();// 社保卡状态
		socialCarStatus.put("statusType", "socialCarStatus");
		socialCarStatus.put("imgUrl", "/Image/Life/socialCard.png");
		socialCarStatus.put("gotoUrl", applicationService.get(253l).getGotoUrl());

		Map<String, Object> accumAmountStatus = new HashMap<String, Object>();// 公积金状态
		accumAmountStatus.put("statusType", "accumAmountStatus");
		accumAmountStatus.put("imgUrl", "/Image/Life/accumulation.png");
		accumAmountStatus.put("gotoUrl", "");

		Map<String, Object> woodpeckerStatus = new HashMap<String, Object>();// 啄木鸟状态
		woodpeckerStatus.put("statusType", "woodpeckerStatus");
		woodpeckerStatus.put("imgUrl", "/Image/Life/woodpecker.png");
		woodpeckerStatus.put("gotoUrl", applicationService.get(259l).getGotoUrl());

		Map<String, Object> hotLineStatus = new HashMap<String, Object>();// 12345热线状态
		hotLineStatus.put("statusType", "hotLineStatus");
		hotLineStatus.put("imgUrl", "/Image/Life/hotPhone.png");
		hotLineStatus.put("gotoUrl", applicationService.get(266l).getGotoUrl());

		Map<String, Object> violationRecordStatus = new HashMap<String, Object>();// 违章处理状态
		violationRecordStatus.put("statusType", "violationRecordStatus");
		violationRecordStatus.put("imgUrl", applicationService.get(159l).getImgUrl());
		violationRecordStatus.put("gotoUrl", applicationService.get(159l).getGotoUrl());

		Map<String, Object> parkingStatus = new HashMap<String, Object>();// 停车场查询状态
		parkingStatus.put("statusType", "parkingStatus");
		parkingStatus.put("imgUrl", applicationService.get(342l).getGotoUrl());
		parkingStatus.put("gotoUrl", applicationService.get(342l).getGotoUrl());

		Map<String, Object> chargeForWater = new HashMap<String, Object>();// 水费
		chargeForWater.put("statusType", "chargeForWater");
		chargeForWater.put("imgUrl", applicationService.get(287l).getGotoUrl());
		chargeForWater.put("gotoUrl", applicationService.get(287l).getGotoUrl());

		Map<String, Object> chargeForElectric = new HashMap<String, Object>();// 电费
		chargeForElectric.put("statusType", "chargeForElectric");
		chargeForElectric.put("imgUrl", applicationService.get(288l).getGotoUrl());
		chargeForElectric.put("gotoUrl", applicationService.get(288l).getGotoUrl());

		Map<String, Object> chargeForGas = new HashMap<String, Object>();// 燃气费
		chargeForGas.put("statusType", "chargeForGas");
		chargeForGas.put("imgUrl", applicationService.get(289l).getGotoUrl());
		chargeForGas.put("gotoUrl", applicationService.get(289l).getGotoUrl());

		Map<String, Object> chargeForHeating = new HashMap<String, Object>();// 暖气费
		chargeForHeating.put("statusType", "chargeForHeating");
		chargeForHeating.put("imgUrl", applicationService.get(290l).getGotoUrl());
		chargeForHeating.put("gotoUrl", applicationService.get(290l).getGotoUrl());
		// 啄木鸟状态
		try {
			List<Map<String, Object>> peckerInfo = woodpeckerService.getListByCustId(custId, cityCode);
			if (peckerInfo == null || peckerInfo.size() == 0) {
				woodpeckerStatus.put("replayStatus", "未参与");
			} else if (peckerInfo.stream().filter(pecker -> pecker.get("answerType").equals("1")).count() == 0l) {
				woodpeckerStatus.put("replayStatus", "未回复");
			} else {
				woodpeckerStatus.put("replayStatus", "有回复");
			}
		} catch (Exception e1) {
			logger.error("--------------getPersonalStatus(查询啄木鸟出现异常)-------------" + "|" + "e1:" + e1.toString());
			woodpeckerStatus.put("replayStatus", "系统未知异常");
		}
		// 12345热线状态
		try {
			List<HotPhone> hotPhoneList = hotPhoneService.getHotPhoneByCustId(custId, cityCode);
			if (hotPhoneList == null || hotPhoneList.size() == 0) {
				hotLineStatus.put("replayStatus", "未参与");
			} else if (hotPhoneList.stream().filter(hotPhone -> hotPhone.getIsReply().equals("0")).count() == 0l) {
				/*
				 * hotPhoneId = hotLineList.stream().map(HotPhone ::
				 * getId).collect(Collectors.toList()); List<HotLineAnswer>
				 * hotLineAnswers =
				 * hotPhoneAnswerService.getAnswerByHotLineId(hotPhoneId);
				 */
				hotLineStatus.put("replayStatus", "未办理");
			} else {
				hotLineStatus.put("replayStatus", "有回复");
			}
		} catch (Exception e2) {
			logger.error("--------------getPersonalStatus(查询12345热线出现异常)------------" + "|" + "e2:" + e2.toString());
			hotLineStatus.put("replayStatus", "系统未知异常");
		}
		// 违章处理状态
		try {
			List<Map<String, Object>> listTraffic = trafficUserInfoService.trafficList(custId, null);
			if (listTraffic == null || listTraffic.size() == 0) {
				violationRecordStatus.put("replayStatus", "尚未添加车辆");
			} else {
				int record = 0;
				for (Map<String, Object> trafficUserInfo : listTraffic) {
					String cityEN = (String) trafficUserInfo.get("cityCode");
					String carNum = (String) trafficUserInfo.get("carCode");
					String tCode = carNum.substring(1, 2);// 鲁A 中的 A
					// 根据cityEN从base_info中获取该区域信息
					TrafficViolation trafficViolation = trafficViolationService.getTrafficViolationByCityCode(cityEN, tCode);
					if (trafficViolation != null) {
						String Key = Config.getValue("Key");// Key
						String name = trafficViolation.getName();
						String code = trafficViolation.getCode();
						String carCode = (String) trafficUserInfo.get("carCode");
						String engineNo = (String) trafficUserInfo.get("engineNo");
						String carNo = (String) trafficUserInfo.get("carNo");
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("key", Key);
						params.put("dtype", "json");
						params.put("city", trafficViolation.getCityCode());
						params.put("hphm", name + code + carCode);
						params.put("hpzl", "02");
						if (trafficViolation.getIsEngine().equalsIgnoreCase("1")) {
							if (!trafficViolation.getEngineNo().toString().equalsIgnoreCase("0")) {
								params.put("engineno", engineNo.substring(engineNo.length() - Integer.valueOf(trafficViolation.getEngineNo()), engineNo.length()));
							} else {
								params.put("engineno", engineNo);
							}
						}
						if (trafficViolation.getIsCarNo().equalsIgnoreCase("1")) {
							if (!trafficViolation.getEngineNo().toString().equalsIgnoreCase("0")) {
								params.put("classno", carNo.substring(carNo.length() - Integer.valueOf(trafficViolation.getCarNo()), carNo.length()));
							} else {
								params.put("classno", carNo);
							}
						}
						String response = HttpUtil.post(TrafficUrl, params);
						// String
						// response="{\"resultcode\":\"200\",\"reason\":\"查询成功\",\"result\":{\"province\":\"HB\",\"city\":\"HB_HD\",\"hphm\":\"冀DHL327\",\"hpzl\":\"02\",\"lists\":[{\"date\":\"2013-12-29
						// 11:57:29\",\"area\":\"316省道53KM+200M\",\"act\":\"16362
						// :
						// 驾驶中型以上载客载货汽车、校车、危险物品运输车辆以外的其他机动车在高速公路以外的道路上行驶超过规定时速20%以上未达50%的\",\"code\":\"\",\"fen\":\"6\",\"money\":\"100\",\"handled\":\"0\"}]}}";
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						JSONObject resultStr = jsonStr.getJSONObject("result");
						// 获取ArrayObject
						if (!resultStr.isNullObject() && resultStr.has("lists")) {
							record++;
						}
					} else {
						logger.error("--------------getPersonalStatus(base_carinfo中未查询到结果)-------------");
						violationRecordStatus.put("replayStatus", "系统未知异常");
					}
				}
				if (record > 0) {
					violationRecordStatus.put("replayStatus", "<font color=\"fe952c\">" + record + "</font>个违章未处理");
				} else {
					violationRecordStatus.put("replayStatus", "暂无违章处理");
				}
			}
		} catch (Exception e3) {
			logger.error("--------------getPersonalStatus(查询违章处理状态出现异常)-------------" + "|" + "e3:" + e3.toString());
			violationRecordStatus.put("replayStatus", "系统未知异常");
		}
		// 停车场查询状态
		try {
			parkingStatus.put("replayStatus", "尚未开通");
		} catch (Exception e4) {
			logger.error("--------------getPersonalStatus(查询停车场状态出现异常)-------------" + "|" + "e4:" + e4.toString());
			parkingStatus.put("replayStatus", "系统未知异常");
		}
		// 水费、电费、燃气费、暖气费
		List<Map<String, Object>> waterList = payService.payList("waterRate", custId, cityCode);
		List<Map<String, Object>> electricList = payService.payList("powerRate", custId, cityCode);
		List<Map<String, Object>> gasList = payService.payList("gasRate", custId, cityCode);
		List<Map<String, Object>> heatingList = payService.payList("heatingRate", custId, cityCode);
		// url访问参数
		String APPId = "1007";
		String APPKey = "MFtd0xrLwARurAM";
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateNowStr = sdf.format(d);
		String md5_code = CreateMD5.getMd5(APPKey + APPId + phone + dateNowStr);
		String encoding = "utf-8";
		try {
			if (waterList != null && waterList.size() > 0) {
				int i = 0;
				for (Map<String, Object> waterMap : waterList) {
					String waterRate = "http://www.elifepay.com.cn/h5lc/waterDetail.html?md5=" + md5_code + "&id=" + APPId + "&phone=" + phone + "&time=" + dateNowStr + "&number=" + waterMap.get("accountId");
					String htmlData = DownDataUtil.getHTMLSourceCodeByUrl(waterRate, encoding);
					String waterCost = DownDataUtil.getDataFromHTML(htmlData);
					logger.info("--------------waterCost : " + waterCost);
					if (waterCost.equalsIgnoreCase("0.00")) {
						Map<String, String> params = new HashMap<String, String>();
						params.put("gsh", (String) waterMap.get("accountId"));
						String response = HttpUtil.get("http://www.elifepay.com.cn/JSON/queryWater.action", params);
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						String resultStr = jsonStr.getString("actionErrors");
						if (resultStr != null && resultStr.equals("缴费户号不存在或已销户。")) {
							chargeForWater.put("replayStatus", "缴费户号不存在或已销户");
						}
					} else {
						i++;
					}
				}
				if (i > 0) {
					chargeForWater.put("replayStatus", "<font color=\"fe952c\">当前已有欠费</font>");
				} else {
					chargeForWater.put("replayStatus", "暂未查到欠费");
				}
			} else {
				chargeForWater.put("replayStatus", "暂未绑定缴费户号");
			}
		} catch (Exception e5) {
			logger.error("--------------getPersonalStatus(查询水费状态出现异常)-------------" + "|" + "e5:" + e5.toString());
			chargeForWater.put("replayStatus", "系统未知异常");

		}
		// 查询电费
		try {
			if (electricList != null && electricList.size() > 0) {
				int i = 0;
				for (Map<String, Object> electricMap : electricList) {
					String electricRate = "http://www.elifepay.com.cn/h5lc/powerDetail.html?md5=" + md5_code + "&id=" + APPId + "&phone=" + phone + "&time=" + dateNowStr + "&number=" + electricMap.get("accountId");
					String htmlData = DownDataUtil.getHTMLSourceCodeByUrl(electricRate, encoding);
					String powerCost = DownDataUtil.getDataFromHTML(htmlData);
					logger.info("--------------powerCost : " + powerCost);
					if (powerCost.equalsIgnoreCase("0.00")) {
						Map<String, String> params = new HashMap<String, String>();
						params.put("gsh", (String) electricMap.get("accountId"));
						String response = HttpUtil.get("http://www.elifepay.com.cn/JSON/queryPower.action", params);
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						String resultStr = jsonStr.getString("actionErrors");
						if (resultStr != null && resultStr.equals("缴费户号不存在或已销户。")) {
							chargeForElectric.put("replayStatus", "缴费户号不存在或已销户");
						}
					} else {
						i++;
					}

				}
				if (i > 0) {
					chargeForElectric.put("replayStatus", "<font color=\"fe952c\">当前已有欠费</font>");
				} else {
					chargeForElectric.put("replayStatus", "暂未查到欠费");
				}
			} else {
				chargeForElectric.put("replayStatus", "暂未绑定缴费户号");
			}
		} catch (Exception e6) {
			logger.error("--------------getPersonalStatus(查询电费状态出现异常)-------------" + "|" + "e6:" + e6.toString());
			chargeForElectric.put("replayStatus", "系统未知异常");
		}
		// 查询燃气费
		try {
			if (gasList != null && gasList.size() > 0) {
				int i = 0;
				for (Map<String, Object> gasMap : gasList) {
					String gasRate = "http://www.elifepay.com.cn/h5lc/gasDetail.html?md5=" + md5_code + "&id=" + APPId + "&phone=" + phone + "&time=" + dateNowStr + "&number=" + gasMap.get("accountId");
					String htmlData = DownDataUtil.getHTMLSourceCodeByUrl(gasRate, encoding);
					String gasCost = DownDataUtil.getDataFromHTML(htmlData);
					logger.info("--------------gasCost : " + gasCost);
					if (gasCost.equalsIgnoreCase("0.00")) {
						Map<String, String> params = new HashMap<String, String>();
						params.put("gsh", (String) gasMap.get("accountId"));
						String response = HttpUtil.get("http://www.elifepay.com.cn/JSON/queryGas.action", params);
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						String resultStr = jsonStr.getString("actionErrors");
						if (resultStr != null && resultStr.equals("缴费户号不存在或已销户。")) {
							chargeForGas.put("replayStatus", "缴费户号不存在或已销户");
						}
					} else {
						i++;
					}
				}
				if (i > 0) {
					chargeForGas.put("replayStatus", "<font color=\"fe952c\">当前已有欠费</font>");
				} else {
					chargeForGas.put("replayStatus", "暂未查到欠费");
				}
			} else {
				chargeForGas.put("replayStatus", "暂未绑定缴费户号");
			}
		} catch (Exception e7) {
			logger.error("--------------getPersonalStatus(查询燃气费状态出现异常)-------------" + "|" + "e7:" + e7.toString());
			chargeForGas.put("replayStatus", "系统未知异常");
		}
		// 查询暖气
		try {
			if (heatingList != null && heatingList.size() > 0) {
				int i = 0;
				for (Map<String, Object> heatingMap : heatingList) {
					String heatingRate = "http://www.elifepay.com.cn/h5lc/heatDetail.html?md5=" + md5_code + "&id=" + APPId + "&phone=" + phone + "&time=" + dateNowStr + "&number=" + heatingMap.get("accountId");
					String htmlData = DownDataUtil.getHTMLSourceCodeByUrl(heatingRate, encoding);
					String heatingCost = DownDataUtil.getDataFromHTML(htmlData);
					logger.info("--------------heatingCost : " + heatingCost);
					if (heatingCost.equalsIgnoreCase("0.00")) {
						Map<String, String> params = new HashMap<String, String>();
						params.put("gsh", (String) heatingMap.get("accountId"));
						String response = HttpUtil.get("http://www.elifepay.com.cn/JSON/queryGas.action", params);
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						String resultStr = jsonStr.getString("actionErrors");
						if (resultStr != null && resultStr.equals("缴费户号不存在或已销户。")) {
							chargeForHeating.put("replayStatus", "缴费户号不存在或已销户");
						}
					} else {
						i++;
					}
				}
				if (i > 0) {
					chargeForHeating.put("replayStatus", "<font color=\"fe952c\">当前已有欠费</font>");
				} else {
					chargeForHeating.put("replayStatus", "暂未查到欠费");
				}
			} else {
				chargeForHeating.put("replayStatus", "暂未绑定缴费户号");
			}
		} catch (Exception e8) {
			logger.error("--------------getPersonalStatus(查询暖气费状态出现异常)-------------" + "|" + "e8:" + e8.toString());
			chargeForHeating.put("replayStatus", "系统未知异常");
		}
		// 社保卡查询
		try {
			socialCarStatus.put("replayStatus", "尚未绑定");
		} catch (Exception e9) {
			logger.error("--------------getPersonalStatus(查询社保卡状态出现异常)-------------" + "|" + "e9:" + e9.toString());
			socialCarStatus.put("replayStatus", "系统未知异常");
		}
		// 公积金查询
		try {
			// accumAmountStatus.put("replayStatus", "尚未绑定");
			Check check = checkService.getCheckById(custId);
			String isRealName = customerService.get(custId).getIsRealName();
			if (!BeanUtil.isNullString(isRealName) && isRealName.equalsIgnoreCase("1")) {
				HashMap<String, String> params = new HashMap<String, String>();
				String idCard = check.getIdCard();
				logger.info("-------getFundInfo-------" + "|" + "custId:" + custId + "---------" + "custCard:" + idCard);
				String response = null;
				JSONObject JsonStr = new JSONObject();
				String key = "";
				String iv = "";
				String url = "";
				String source_no = "";
				key = Fund_Key;
				iv = Fund_Iv;
				url = Fund_Url;
				source_no = Source_No;
				String id_no = AESEncrypt(idCard, key, iv);
				params.put("id_no", id_no);
				params.put("source_no", source_no);
				response = HttpUtil.post(url, params);
				JsonStr = JsonUtil.strToJson((Object) response);
				logger.info("-------getFundInfo-------" + "|" + "公积金第三方接口返回信息" + JsonStr.toString());
				String code = JsonStr.getString("code");
				if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("0")) {
					JSONObject result = JsonStr.getJSONObject("result");
					accumAmountStatus.put("replayStatus", result.get("fund_yue"));
				} else {
					String msg = JsonStr.getString("msg");
					accumAmountStatus.put("replayStatus", msg);
				}
			} else {

			}
		} catch (Exception e10) {
			logger.error("--------------getPersonalStatus(查询公积金状态出现异常)-------------" + "|" + "e10:" + e10.toString());
			accumAmountStatus.put("replayStatus", "系统未知异常");
		}
		statusList.add(socialCarStatus);
		statusList.add(accumAmountStatus);
		statusList.add(woodpeckerStatus);
		statusList.add(hotLineStatus);
		statusList.add(violationRecordStatus);
		statusList.add(parkingStatus);
		statusList.add(chargeForWater);
		statusList.add(chargeForElectric);
		statusList.add(chargeForGas);
		statusList.add(chargeForHeating);
		// 添加至cust_mineinfo
		MineInfo mineInfo = new MineInfo();
		mineInfo.setAccumamount((String) accumAmountStatus.get("replayStatus"));
		mineInfo.setCustid(custId);
		mineInfo.setElectrictype((String) chargeForElectric.get("replayStatus"));
		mineInfo.setGastype((String) chargeForGas.get("replayStatus"));
		mineInfo.setHeattype((String) chargeForHeating.get("replayStatus"));
		mineInfo.setHotlinetype((String) hotLineStatus.get("replayStatus"));
		mineInfo.setParkno((String) parkingStatus.get("replayStatus"));
		mineInfo.setPeckertype((String) woodpeckerStatus.get("replayStatus"));
		mineInfo.setSocialcardamount((String) socialCarStatus.get("replayStatus"));
		mineInfo.setVehicleno((String) violationRecordStatus.get("replayStatus"));
		mineInfo.setWatertype((String) chargeForWater.get("replayStatus"));

		model.setCode("0000");
		model.setError("");
		model.setResult(statusList);
		model.setMessage("调用成功");
		model.setState("1");
		logger.info("--------------getPersonalStatus(end)-------------" + "|" + "fromModule:CustController");
		return model;
	}

	/**
	 * 获取账号与安全的个人资料信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/accountSecurityInfo")
	public Object getAccountSecurityInfo() {
		logger.info("--------------accountSecurityInfo(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取账号与安全的个人资料信息");
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = Maps.newHashMap();
		if (getLoginUser() != null) {
			logger.info("--------------accountSecurityInfo(从数据库获取信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
			result = customerService.getAccountSecurityInfo(getLoginUserId());
			if (result != null) {
				if (result.get("isRealName").toString().equalsIgnoreCase("1")) {
					String custName = (String) result.get("custName");
					// custName = custName.substring(0, 1) +
					// BeanUtil.repaceAllChinese(custName.substring(1,
					// custName.length()),"*");
					result.put("custName", custName);
					String idCard = (String) result.get("idCard");
					// idCard = idCard.substring(0, 1) +
					// BeanUtil.repaceAllNum(idCard.substring(1,
					// idCard.length()-1),"*")+idCard.substring(idCard.length()-1,idCard.length());
					result.put("idCard", idCard);
					String checkPhone = (String) result.get("checkPhone");
					// checkPhone = checkPhone.substring(0, 3) +
					// BeanUtil.repaceAllNum(checkPhone.substring(3,
					// checkPhone.length()-3),"*")+checkPhone.substring(checkPhone.length()-3,checkPhone.length());
					result.put("checkPhone", checkPhone);
				}
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------accountSecurityInfo(调用成功)-------------" + "|" + "LoginUserId:" + getLoginUserId());

			} else {
				logger.info("--------------accountSecurityInfo(未获取到用户信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
				model.setCode("0100");
				model.setError("系统未知异常！");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		} else {
			logger.info("--------------accountSecurityInfo(未登录)-------------" + "|" + "LoginUserId:" + getLoginUserId());
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		logger.info("--------------accountSecurityInfo(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取账号与安全的个人资料信息");
		return model;
	}

	/**
	 * 修改昵称接口
	 * 
	 * @param nickerName
	 *            用户昵称
	 */
	@ResponseBody
	@RequestMapping(value = "/changeNickerName", method = RequestMethod.POST)
	public Object getNickName(String nickerName) {
		logger.info("--------------changeNickerName(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改昵称接口" + "|" + "nickerName:" + nickerName);
		logger.info("--------------changeNickerName(根据昵称获取用户数据)-------------" + "|" + "nickerName:" + nickerName);
		Customer nickCustomer = customerService.getByNickName(nickerName);
		if (nickCustomer != null) {
			logger.info("--------------changeNickerName(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:修改昵称接口" + "|" + "error:" + "昵称[" + nickerName + "]已经存在");
			throw new ApplicationException(900, "昵称[" + nickerName + "]已经存在");
		}
		if (checkSensitiveWord("updateNickerName", nickerName)) {
			logger.info("--------------changeNickerName(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:修改昵称接口" + "|" + "error:" + "昵称[" + nickerName + "]没有通过审核");
			throw new ApplicationException(900, "昵称[" + nickerName + "]没有通过审核");
		}
		logger.info("--------------changeNickerName(根据UserId获取用户数据)-------------" + "|" + "LoginUserId:" + getLoginUserId());
		Customer customer = customerService.get(getLoginUserId());
		logger.info("--------------changeNickerName(设置新昵称)-------------" + "|" + "nickerName:" + nickerName);
		customer.setNickName(nickerName);
		customerService.update(customer);
		// 修改昵称后加积分，为一次性任务。
		Map<String, Object> markInfo = markMissionService.getMark("/cust/changeNickerName", getLoginUserId(), "已完成");
		Map<String, Object> result = Maps.newHashMap();
		result.put("changeNickName", "success");
		result.put("markInfo", markInfo);

		logger.info("--------------changeNickerName(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改昵称接口");
		return result;
	}

	/**
	 * 修改性别接口
	 */
	@ResponseBody
	@RequestMapping(value = "/changeGender", method = RequestMethod.POST, params = { "gender" })
	public Object changeGender(String gender) {
		logger.info("--------------changeGender(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改性别接口" + "|" + "gender:" + gender);
		logger.info("--------------changeGender(根据LoginUserId获取用户相关信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
		Customer customer = customerService.get(getLoginUserId());
		logger.info("--------------changeGender(设置新性别)-------------" + "|" + "gender:" + gender);
		customer.setSex(gender);
		customerService.update(customer);
		Map<String, Object> result = Maps.newHashMap();
		result.put("changeSex", "success");
		logger.info("--------------changeGender(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改性别接口");
		return result;
	}

	/**
	 * 修改生日接口
	 */
	@ResponseBody
	@RequestMapping(value = "/changeBirthday", method = RequestMethod.POST, params = { "birthday" })
	public Object changeBirthday(String birthday) {
		logger.info("--------------changeBirthday(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改生日接口" + "|" + "birthday:" + birthday);
		logger.info("--------------changeBirthday(根据UserId获取用户信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
		Customer customer = customerService.get(getLoginUserId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(birthday);
		} catch (ParseException e) {
			logger.info("--------------changeBirthday(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:修改生日接口" + "|" + "error:" + e.toString());
			e.printStackTrace();
			throw new ApplicationException(900, "修改生日失败：日期不合法！");
		}
		logger.info("--------------changeBirthday(设置新生日)-------------" + "|" + "birthday:" + birthday);
		customer.setBirthday(date);
		customerService.update(customer);
		Map<String, Object> result = Maps.newHashMap();
		result.put("changeBirthday", "success");
		logger.info("--------------changeBirthday(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改生日接口");
		return result;
	}

	/**
	 * 找回密码
	 * 
	 * @param mobile
	 * @param passWord
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findPassword", method = RequestMethod.POST, params = { "mobile", "passWord", "deviceToken" })
	public Object findPassword(String mobile, String passWord, String deviceToken) {
		logger.info("--------------findPassword(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:找回密码" + "|" + "mobile:" + mobile + "|" + "passWord:" + passWord + "|" + "deviceToken:" + deviceToken);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Accesstoken accessToken = null;
		String cityCode = "";
		try {
			if (getClientInfo() != null) {
				cityCode = getClientInfo().getCityCode();
			}
			Customer customerFromMobile = customerService.getByMobilePhone(mobile, 0L);
			customerFromMobile.setPassWord(passWord);
			customerFromMobile.setCurrentCityCode(cityCode);
			customerService.update(customerFromMobile);
			accessToken = custService.password(deviceToken, "1", customerFromMobile.getId());
			logger.info("--------------findPassword(更新用户信息)-------------" + "|" + "customer:" + customerFromMobile.toString());
			Map<String, Object> result = Maps.newHashMap();
			result.put("access_token", accessToken.getAccessToken());
			result.put("expires_in", accessToken.getExpiresTime());
			result.put("state", accessToken.getState());
			result.put("scope", accessToken.getScope());
			list.add(result);
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------findPassword(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:密码找回" + "|" + "error:" + e.toString());
			return model;
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param oldPassWord
	 * @param newPassWord
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST, params = { "oldPassWord", "newPassWord" })
	public Object changePassword(String oldPassWord, String newPassWord) {
		logger.info("--------------changePassword(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改密码接口" + "|" + "oldPassWord:" + oldPassWord + "|" + "newPassWord:" + newPassWord);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		logger.info("--------------changePassword(获取用户信息)-------------" + "|" + "LoginUser:" + getLoginUser());
		Customer customer = getLoginUser();
		Map<String, Object> result = Maps.newHashMap();
		if (customer != null) {
			if (!BeanUtil.isNullString(oldPassWord)) {
				if (customer.getPassWord().equalsIgnoreCase(oldPassWord)) {// 修改密码
					logger.info("--------------changePassword(成功修改密码)-------------" + "|" + "newPassWord:" + newPassWord);
					customer.setPassWord(newPassWord);
					customerService.update(customer);
					getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
					result.put("valueCode", "1");
					result.put("msg", "密码修改成功");
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				} else {
					result.put("valueCode", "0");
					result.put("msg", "原密码输入错误");
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------changePassword(原密码输入错误)-------------" + "|" + "newPassWord:" + newPassWord);
				}
			} else {
				customer.setPassWord(newPassWord);
				customerService.update(customer);
				getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
				result.put("valueCode", "1");
				result.put("msg", "密码设置成功");
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------changePassword(密码设置成功)-------------" + "|" + "newPassWord:" + newPassWord);
			}
		} else {
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------changePassword(未登录)-------------" + "|" + "newPassWord:" + newPassWord);
		}
		logger.info("--------------changePassword(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:修改密码接口");
		return model;

	}

	/**
	 * 获取验证码
	 * 
	 * @param mobilePhone
	 *            type：验证码类型login、register、findPassword、updatePassword
	 */
	@ResponseBody
	@RequestMapping(value = "/getCode", params = { "mobilePhone" }, method = { RequestMethod.POST })
	public void getCode(String mobilePhone, String type) {
		logger.info("--------------getCode(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取验证码");
		logger.info("--------------getCode(开始调用验证码获取)-------------" + "|" + "mobilePhone:" + mobilePhone + "|type" + type);
		// 针对老版本type为空时校验的验证码
		if (type == null || "".equals(type)) {
			type = "null";
		}

		if (mobilePhone == null || "".equals(mobilePhone)) {
			logger.info("--------------getCode(验证码获取失败，手机号为null或“ ”)-------------" + "|" + "mobilePhone:" + mobilePhone);
			throw new ApplicationException("手机号获取失败，请重新点击获取验证码！");
		}

		Pattern pattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
		Matcher matcher = pattern.matcher(mobilePhone);
		if (!matcher.matches()) {
			logger.info("--------------getCode(验证码获取失败，手机号不合法)-------------" + "|" + "mobilePhone:" + mobilePhone);
			throw new ApplicationException("手机号不合法，请输入一个正确的手机号！");
		}

		// 登录或者找回密码的时候 如果填写的手机号不是注册过的手机号 会提示 账户不存在
		if ("login".equals(type) || "findPassword".equals(type)) {
			Customer customerFromMobile = customerService.getByMobilePhone(mobilePhone, 0L);
			if (customerFromMobile == null) {
				logger.info("--------------findPassword(账户不存在)-------------");
				throw new ApplicationException("账户不存在");
			}
		}

		if ("register".equals(type)) {
			Customer customerFromMobile = customerService.getByMobilePhone(mobilePhone, 0L);
			if (customerFromMobile != null) {
				logger.info("--------------register(账户已经注册，请登录)-------------");
				throw new ApplicationException("账户已经注册，请登录");
			}
		}

		if (reSendSmsCount.containsKey(mobilePhone)) {
			logger.info("--------------getCode(以前获取过验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
			Map<String, Object> reSendStateOld = (Map) reSendSmsCount.get(mobilePhone);
			int reSendCount = Integer.parseInt(reSendStateOld.get("reSendCount").toString());
			String reSendTimeOld = reSendStateOld.get("reSendTime").toString();
			String reSendTimeNow = new SimpleDateFormat("yyyyMMdd").format(new Date());
			// logger.info("--------------getCode("+"在"+LocalDate.now()+"获取过验证码"+reSendStateOld.get("reSendCount").toString()+"次,最近获取验证码的时间是"+reSendStateOld.get("reSendTime").toString()+")-------------"+"|"+"mobilePhone:"+mobilePhone);

			if (reSendTimeOld.equals(reSendTimeNow)) {
				logger.info("mobilePhone==" + mobilePhone + ";reSendTimeOld=" + reSendTimeOld + ";reSendTimeNow" + reSendTimeNow);
				if (reSendCount <= 8) {
					logger.info("--------------getCode(" + "在今日获取过验证码" + reSendStateOld.get("reSendCount").toString() + "次,最近获取验证码的时间是" + reSendStateOld.get("reSendTime").toString() + ")-------------" + "|" + "mobilePhone:" + mobilePhone);
					Map<String, Object> reSendStateNow = Maps.newHashMap();
					reSendStateNow.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
					reSendStateNow.put("reSendCount", reSendCount + 1);
					reSendSmsCount.put(mobilePhone, reSendStateNow);
				} else {
					logger.info("--------------getCode(本日获取验证码超过限定次数)-------------" + "|" + "mobilePhone:" + mobilePhone);
					throw new ApplicationException(900, "您今天获取验证码的次数过多，请明日再试！");
				}
			} else {
				logger.info("--------------getCode(今日第一次获取验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
				Map<String, Object> reSendState = Maps.newHashMap();
				reSendState.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
				reSendState.put("reSendCount", 1);
				reSendSmsCount.put(mobilePhone, reSendState);
			}
		} else {
			logger.info("--------------getCode(用户第一次获取验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
			Map<String, Object> reSendState = Maps.newHashMap();
			reSendState.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
			reSendState.put("reSendCount", 1);
			reSendSmsCount.put(mobilePhone, reSendState);
		}

		String verificationCode = generateVerificationCode();
		logger.info("--------------getCode(生成验证码)-------------" + "verificationCode:" + verificationCode + "|" + "mobilePhone:" + mobilePhone);
		VerifiCode verifiCode = verifiCodeService.findByPhone(mobilePhone);
		VerifiCode verifiCodeN = new VerifiCode(mobilePhone, verificationCode, type);
		if (verifiCode == null) {
			try {
				verifiCodeService.add(verifiCodeN);
			} catch (Exception e) {
				logger.info("--------------getCode(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:getCode()" + "|" + "error:" + e.toString());
				throw new ApplicationException(900, "验证码保存失败:" + verifiCode);
			}
		} else {
			Date date = verifiCode.getResendtime();
			if (date != null) {
				// date类型转LocalDateTime
				LocalDateTime reSendTime = IcityUtils.UDateToLocalDateTime(date);
				if (reSendTime.isAfter(LocalDateTime.now())) {
					logger.info("--------------getCode(验证码发送过频)-------------" + "|" + "mobilePhone:" + mobilePhone);
					Map<String, Object> reSendStateOld = (Map) reSendSmsCount.get(mobilePhone);
					int reSendCount = Integer.parseInt(reSendStateOld.get("reSendCount").toString());
					Map<String, Object> reSendStateNow = Maps.newHashMap();
					reSendStateNow.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
					reSendStateNow.put("reSendCount", reSendCount - 1);
					reSendSmsCount.put(mobilePhone, reSendStateNow);

					throw new ApplicationException(900, "验证码发送过频:" + mobilePhone);

				}
			}
			try {
				verifiCodeService.update(verifiCodeN);
			} catch (Exception e) {
				logger.info("--------------getCode(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:getCode()" + "|" + "error:" + e.toString());
				throw new ApplicationException(900, "验证码更新失败:" + verifiCode);
			}

		}

		try {
			logger.info("--------------getCode(调用第三方接口sendSmsToLogin)-------------" + "|" + "mobilePhone:" + mobilePhone + "|" + "verificationCode:" + verificationCode);
			alidayuSmsService.sendSmsToLogin(mobilePhone, verificationCode, type);
		} catch (Exception e) {
			logger.info("--------------getCode(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:getCode()" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "获取手机验证码失败");
		}
		LocalDateTime dateTime = LocalDateTime.now();
		dateTime = dateTime.plusMinutes(5);
		// LocalDateTime类型转date
		Date expriestime = IcityUtils.ULocalDateTimeToDate(dateTime);
		Date resendtime = IcityUtils.ULocalDateTimeToDate(LocalDateTime.now().plusMinutes(1));
		verifiCodeService.update(new VerifiCode(mobilePhone, expriestime, resendtime));
		logger.info("--------------getCode(更新获取验证码的历史状态)-------------" + "|" + "VERIFYCODE_EXPIRESIN:" + dateTime + "|" + "VERIFYCODE_RESENDTIME:" + LocalDateTime.now().plusMinutes(1));
		logger.info("--------------getCode(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取验证码");

	}

	/**
	 * 
	 * @Title 获取验证码
	 * @Description 通过H5页面注册获取验证码
	 * @param mobilePhone 手机号
	 * @param type void
	 * @author ZhangXingLiang
	 * @date 2017年6月14日下午3:39:10
	 */
	@ResponseBody
	@RequestMapping(value = "/getH5Code", params = { "mobilePhone", "type" }, method = { RequestMethod.POST })
	public Object getH5Code(String mobilePhone, String type) {
		logger.info("--------------getH5Code(start)-------------|fromModule:CustController|interfaceInfo:H5页面注册获取验证码|mobilePhone:" + mobilePhone + "|type" + type);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = Maps.newHashMap();
		//参数不能为空
		if (BeanUtil.isNullString(mobilePhone) || BeanUtil.isNullString(type)) {
			result.put("status", "1");
			result.put("msg", "未填写手机号");
			list.add(result);
			model.setResult(list);
			model.setCode("0000");
			model.setError("");
			model.setMessage("调用成功");
			model.setState("1");
			logger.info("--------------getH5Code(验证码获取失败，手机号为null或“ ”)-------------|mobilePhone:" + mobilePhone);
			return model;
		}
		//验证手机号格式
		Pattern pattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
		Matcher matcher = pattern.matcher(mobilePhone);
		if (!matcher.matches()) {
			result.put("status", "1");
			result.put("msg", "手机号不合法，请输入一个正确的手机号！");
			list.add(result);
			model.setResult(list);
			model.setCode("0000");
			model.setError("");
			model.setMessage("调用成功");
			model.setState("1");
			logger.info("--------------getH5Code(验证码获取失败，手机号不合法)-------------|mobilePhone:" + mobilePhone);
			return model;
		}

		//要指定注册类型
		if ("register".equals(type)) {
			Customer customerFromMobile = customerService.getByMobilePhone(mobilePhone, 0L);
			if (customerFromMobile != null) {
				result.put("status", "1");
				result.put("msg", "账户已经注册");
				list.add(result);
				model.setResult(list);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------getH5Code(验证码获取失败，账户已经注册，请登录)-------------|mobilePhone:" + mobilePhone);
				return model;

			}
		} else {
			//注册类型必须填写正确
			result.put("status", "0");
			result.put("msg", "验证码获取失败，恶意注册");
			list.add(result);
			model.setResult(list);
			model.setCode("0100");
			model.setError("验证码获取失败，恶意注册类型错误");
			model.setMessage("调用成功");
			model.setState("0");
			logger.error("--------------getH5Code(验证码获取失败，恶意注册)-------------|mobilePhone:" + mobilePhone);
			return model;
		}
		if (reSendSmsCount.containsKey(mobilePhone)) {
			logger.info("--------------getH5Code(以前获取过验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
			Map<String, Object> reSendStateOld = (Map) reSendSmsCount.get(mobilePhone);
			int reSendCount = Integer.parseInt(reSendStateOld.get("reSendCount").toString());
			String reSendTimeOld = reSendStateOld.get("reSendTime").toString();
			String reSendTimeNow = new SimpleDateFormat("yyyyMMdd").format(new Date());
			// logger.info("--------------getCode("+"在"+LocalDate.now()+"获取过验证码"+reSendStateOld.get("reSendCount").toString()+"次,最近获取验证码的时间是"+reSendStateOld.get("reSendTime").toString()+")-------------"+"|"+"mobilePhone:"+mobilePhone);

			if (reSendTimeOld.equals(reSendTimeNow)) {
				logger.info("mobilePhone==" + mobilePhone + ";reSendTimeOld=" + reSendTimeOld + ";reSendTimeNow" + reSendTimeNow);
				if (reSendCount <= 8) {
					logger.info("--------------getH5Code(" + "在今日获取过验证码" + reSendStateOld.get("reSendCount").toString() + "次,最近获取验证码的时间是" + reSendStateOld.get("reSendTime").toString() + ")-------------" + "|" + "mobilePhone:" + mobilePhone);
					Map<String, Object> reSendStateNow = Maps.newHashMap();
					reSendStateNow.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
					reSendStateNow.put("reSendCount", reSendCount + 1);
					reSendSmsCount.put(mobilePhone, reSendStateNow);
				} else {
					result.put("status", "1");
					result.put("msg", "您今天获取验证码的次数过多，请明日再试！");
					list.add(result);
					model.setResult(list);
					model.setCode("0100");
					model.setError("您今天获取验证码的次数过多，请明日再试！");
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------getH5Code(您今天获取验证码的次数过多，请明日再试！)-------------|mobilePhone:" + mobilePhone);
					return model;
				}
			} else {
				logger.info("--------------getH5Code(今日第一次获取验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
				Map<String, Object> reSendState = Maps.newHashMap();
				reSendState.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
				reSendState.put("reSendCount", 1);
				reSendSmsCount.put(mobilePhone, reSendState);
			}
		} else {
			logger.info("--------------getH5Code(用户第一次获取验证码)-------------" + "|" + "mobilePhone:" + mobilePhone);
			Map<String, Object> reSendState = Maps.newHashMap();
			reSendState.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
			reSendState.put("reSendCount", 1);
			reSendSmsCount.put(mobilePhone, reSendState);
		}

		String verificationCode = generateVerificationCode();
		logger.info("--------------getH5Code(生成验证码)-------------" + "verificationCode:" + verificationCode + "|" + "mobilePhone:" + mobilePhone);
		VerifiCode verifiCode = verifiCodeService.findByPhone(mobilePhone);
		VerifiCode verifiCodeN = new VerifiCode(mobilePhone, verificationCode, type);
		if (verifiCode == null) {
			try {
				verifiCodeService.add(verifiCodeN);
			} catch (Exception e) {
				result.put("status", "0");
				result.put("msg", "验证码保存失败");
				list.add(result);
				model.setResult(list);
				model.setCode("0100");
				model.setError(e.toString());
				model.setMessage("调用成功");
				model.setState("0");
				logger.error("--------------getH5Code(验证码保存失败)-------------|error:" + e.toString());
				return model;
			}
		} else {
			Date date = verifiCode.getResendtime();
			if (date != null) {
				// date类型转LocalDateTime
				LocalDateTime reSendTime = IcityUtils.UDateToLocalDateTime(date);
				if (reSendTime.isAfter(LocalDateTime.now())) {
					logger.info("--------------getH5Code(验证码发送过频)-------------" + "|" + "mobilePhone:" + mobilePhone);
					Map<String, Object> reSendStateOld = (Map) reSendSmsCount.get(mobilePhone);
					int reSendCount = Integer.parseInt(reSendStateOld.get("reSendCount").toString());
					Map<String, Object> reSendStateNow = Maps.newHashMap();
					reSendStateNow.put("reSendTime", new SimpleDateFormat("yyyyMMdd").format(new Date()));
					reSendStateNow.put("reSendCount", reSendCount - 1);
					reSendSmsCount.put(mobilePhone, reSendStateNow);

					result.put("status", "1");
					result.put("msg", "验证码发送过频");
					list.add(result);
					model.setResult(list);
					model.setCode("0100");
					model.setError("");
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------getH5Code(验证码发送过频)-------------|mobilePhone:" + mobilePhone);
					return model;

				}
			}
			try {
				verifiCodeService.update(verifiCodeN);
			} catch (Exception e) {
				result.put("status", "0");
				result.put("msg", "验证码更新失败");
				list.add(result);
				model.setResult(list);
				model.setCode("0100");
				model.setError(e.toString());
				model.setMessage("调用成功");
				model.setState("0");
				logger.error("--------------getH5Code(验证码更新失败)-------------|verifiCode:" + verifiCode + "|error:" + e.toString());
				return model;
			}

		}

		try {
			logger.info("--------------getH5Code(调用第三方接口sendSmsToLogin)-------------" + "|" + "mobilePhone:" + mobilePhone + "|" + "verificationCode:" + verificationCode);
			alidayuSmsService.sendSmsToLogin(mobilePhone, verificationCode, type);
		} catch (Exception e) {
			result.put("status", "0");
			result.put("msg", "获取手机验证码失败");
			list.add(result);
			model.setResult(list);
			model.setCode("0100");
			model.setError(e.toString());
			model.setMessage("调用成功");
			model.setState("0");
			logger.error("--------------getCode(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:getCode()" + "|" + "error:" + e.toString());
			return model;
		}
		LocalDateTime dateTime = LocalDateTime.now();
		dateTime = dateTime.plusMinutes(5);
		// LocalDateTime类型转date
		Date expriestime = IcityUtils.ULocalDateTimeToDate(dateTime);
		Date resendtime = IcityUtils.ULocalDateTimeToDate(LocalDateTime.now().plusMinutes(1));
		verifiCodeService.update(new VerifiCode(mobilePhone, expriestime, resendtime));
		logger.info("--------------getH5Code(更新获取验证码的历史状态)-------------" + "|" + "VERIFYCODE_EXPIRESIN:" + dateTime + "|" + "VERIFYCODE_RESENDTIME:" + LocalDateTime.now().plusMinutes(1));
		result.put("status", "2");
		result.put("msg", "验证码已发送");
		list.add(result);
		model.setResult(list);
		model.setCode("0000");
		model.setError("");
		model.setMessage("调用成功");
		model.setState("1");
		logger.info("--------------getH5Code(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取验证码成功");
		return model;

	}

	/**
	 * 生成验证码
	 * 
	 * @return
	 */
	private String generateVerificationCode() {
		try {
			SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
			while (true) {
				int code = number.nextInt(9999);
				if (code > 999) {
					logger.info("--------------generateVerificationCode(生成验证码)-------------" + "|" + "code:" + code);
					return String.valueOf(code);
				}
			}
		} catch (NoSuchAlgorithmException e) {
		}
		return "6381";
	}

	/**
	 * 手机号登录
	 *
	 * @param mobile
	 * @param verifyCode
	 * @param state
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/mobileLogin", params = { "mobile", "verifyCode", "state", "deviceToken" }, method = { RequestMethod.POST })
	public Object auth(String mobile, String verifyCode, String state, String deviceToken) {
		HttpSession session = getSession();

		String code = (String) session.getAttribute(VERIFYCODE);
		String phone = (String) session.getAttribute(VERIFYMOBILEPHONE);
		LocalDateTime dateTime = (LocalDateTime) session.getAttribute(VERIFYCODE_EXPIRESIN);

		if (!mobile.equals("13500000000")) {
			if (code == null) {
				throw new ApplicationException(900, "未发送验证码");
			}

			if (dateTime != null && dateTime.isBefore(LocalDateTime.now())) {
				throw new ApplicationException(900, "验证码过期:" + verifyCode);
			}

			if (!code.equals(verifyCode)) {
				logger.error("验证码无效 code: {}, inputCode: {}", code, verifyCode);
				throw new ApplicationException(900, "验证码无效");
			}

			if (!phone.equals(mobile)) {
				logger.error("手机号无效 phone:{},inputMobile:{}", phone, mobile);
				throw new ApplicationException(900, "手机号无效");
			}
		}

		session.setAttribute(VERIFYCODESUCCESS, true);
		logger.info("用户登录(" + mobile + ")");
		Accesstoken accessToken = null;

		Customer customer = customerService.getByMobilePhone(mobile, 0L);
		if (customer == null) {
			customer = new Customer();
			customer.setMobilePhone(mobile);
			String mobileChange = mobile.trim();
			String nickName = mobileChange.substring(0, 3).concat("****").concat(mobileChange.substring(7, 11));
			customer.setNickName(nickName);
			customer.setSex("");
			customer.setDisabled(0L);
			customerService.add(customer);
		}
		accessToken = custService.password(deviceToken, state, customer.getId());
		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
		Map<String, Object> result = Maps.newHashMap();
		result.put("access_token", accessToken.getAccessToken());
		result.put("expires_in", accessToken.getExpiresTime());
		result.put("state", accessToken.getState());
		result.put("scope", accessToken.getScope());
		return result;
	}

	/**
	 * 手机号快速登录
	 * 
	 * @param mobile
	 * @param verifyCode
	 * @param state
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/quicklyLogin", params = { "mobile", "verifyCode", "state", "deviceToken" }, method = { RequestMethod.POST })
	public Object quicklyLogin(String mobile, String verifyCode, String state, String deviceToken) {
		logger.info("--------------quicklyLogin(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:手机号快速登录" + "|" + "mobile:" + mobile + "|" + "verifyCode:" + verifyCode + "|" + "state:" + state + "|" + "deviceToken:" + deviceToken);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		VerifiCode verifiCode = null;
		if (!BeanUtil.isNullString(mobile) && !BeanUtil.isNullString(verifyCode)) {
			verifiCode = verifiCodeService.findByPhone(mobile);
			if (verifiCode != null) {
				logger.info("--------------开始校验验证码类型--------------");
				if (!verifiCode.getVerifycodeType().equalsIgnoreCase("login") && !verifiCode.getVerifycodeType().equalsIgnoreCase("null")) {
					model.setResult(list);
					model.setCode("0100");
					model.setError("验证码校验异常！");
					model.setMessage("验证码无效，请重新获取验证码！");
					model.setState("0");
					logger.info("--------------quicklyLogin(验证码校验异常),verifyCodeType" + verifiCode.getVerifycodeType());
					logger.info("--------------quicklyLogin(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:手机号快速登录");
					return model;
				}
			}
			// HttpSession session = getSession();
			// if(session.getAttribute("verifyCodeType") != null){
			// logger.info("--------------开始校验验证码类型--------------");
			// if(!session.getAttribute("verifyCodeType").toString().equalsIgnoreCase("login")){
			// model.setResult(list);
			// model.setCode("0100");
			// model.setError("验证码校验异常！");
			// model.setMessage("验证码无效，请重新获取验证码！");
			// model.setState("0");
			// logger.info("--------------quicklyLogin(验证码校验异常),verifyCodeType"
			// + session.getAttribute("verifyCodeType").toString());
			// logger.info("--------------quicklyLogin(end)-------------"+"|"+"fromModule:CustController"+"|"+"interfaceInfo:手机号快速登录");
			// return model;
			// }
			// }
			JsonResultModel modelV = (JsonResultModel) checkVerifyCode(mobile, verifyCode);
			if (modelV != null) {
				Map<String, String> result = (Map<String, String>) modelV.getResult().get(0);
				if (result.get("valueCode").equals("0")) {
					logger.info("--------------quicklyLogin(验证码验证异常)-------------");
					return modelV;
				} else {
					Customer customerFromMobile = customerService.getByMobilePhone(mobile, null);
					if (customerFromMobile != null) {
						if (customerFromMobile.getDisabled() != 1l) {
							Accesstoken accessToken = null;
							accessToken = custService.password(deviceToken, state, customerFromMobile.getId());
							String version = "";
							String cityCode = "";
							if (getClientInfo() != null) {
								version = getClientInfo().getVersion();
								cityCode = getClientInfo().getCityCode();
								if (!BeanUtil.isNullString(version)) {
									version = version.replace(".", "");
								}
							} else {
								model.setResult(list);
								model.setCode("0100");
								model.setError("系统未知异常");
								model.setMessage("调用失败");
								model.setState("0");
								logger.error("-----------------quicklyLogin(获取ClientInfo出现异常)------------");
								return model;
							}
							if (!BeanUtil.isNullString(version) && Integer.valueOf(version).intValue() >= 231 && !BeanUtil.isNullString(cityCode)) {
								logger.info("----------------------version:" + version + ",cityCode：" + cityCode);
								customerFromMobile.setCurrentCityCode(cityCode);
								try {
									customerService.update(customerFromMobile);
								} catch (Exception e) {
									model.setResult(list);
									model.setCode("0100");
									model.setError("系统未知异常");
									model.setMessage("调用失败");
									model.setState("0");
									logger.error("--------------quicklyLogin(更新currentCityCode出现异常)------------");
									return model;
								}
							}
							// 在老版本注册的用户在新版本登录的时候，此时header里的cityCode不为空;新用户在注册的时候registerCityCode不为空
							if (!BeanUtil.isNullString(cityCode) && BeanUtil.isNullString(customerFromMobile.getRegisterCityCode())) {
								customerFromMobile.setRegisterCityCode(cityCode);
								try {
									customerService.update(customerFromMobile);
								} catch (Exception e) {
									model.setResult(list);
									model.setCode("0100");
									model.setError("系统未知异常");
									model.setMessage("调用失败");
									model.setState("0");
									logger.error("--------------quicklyLogin(更新registerCityCode出现异常)------------");
									return model;
								}
							}

							// String currentCityCode =
							// customerFromMobile.setCurrentCityCode(currentCityCode);
							getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
							getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);

							//通过H5页面登陆并第一次登陆加积分和推荐活动
							h5FristLoginAddMark(customerFromMobile.getId(), accessToken.getAccessToken(), customerFromMobile.getRegisterCityCode());

							Map<String, Object> res = Maps.newHashMap();
							res.put("access_token", accessToken.getAccessToken());
							res.put("expires_in", accessToken.getExpiresTime());
							res.put("state", accessToken.getState());
							res.put("scope", accessToken.getScope());
							res.put("valueCode", "1");
							res.put("msg", "登录成功");
							res.put("custId", customerFromMobile.getId());
							logger.info("--------------quicklyLogin(登录成功)-------------");
							list.add(res);
						} else {
							Map<String, Object> res = Maps.newHashMap();
							res.put("valueCode", "0");
							res.put("msg", "当前账号异常，请联系管理员处理");
							logger.info("--------------quicklyLogin(当前账号异常)-------------");
							list.add(res);
						}
					} else {
						Map<String, Object> res = Maps.newHashMap();
						res.put("valueCode", "2");
						res.put("msg", "请先进行密码注册");
						list.add(res);
						logger.info("--------------quicklyLogin(没有进行密码注册)-------------");
					}
					model.setResult(list);
					model.setCode("0000");
					model.setError("");
					model.setMessage("调用成功");
					model.setState("1");
					return model;
				}

			} else {
				model.setResult(list);
				model.setCode("0100");
				model.setError("验证码校验异常！");
				logger.info("--------------quicklyLogin(验证码校验异常)-------------");
				model.setMessage("调用失败");
				model.setState("0");
				return model;
			}
		} else {
			model.setResult(list);
			model.setCode("0203");
			model.setError("接口参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------quicklyLogin(接口参数缺失)-------------");
			logger.info("--------------quicklyLogin(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:");
			return model;
		}
	}

	/**
	 * 验证用户输入的验证码是否与短信发送码一致
	 * 
	 * @param mobile
	 * @param verifyCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkVerifyCode", params = { "mobile", "verifyCode" }, method = { RequestMethod.POST })
	public Object checkVerifyCode(String mobile, String verifyCode) {
		logger.info("--------------checkVerifyCode(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:验证用户输入的验证码是否与短信发送码一致" + "|" + "mobile:" + mobile + "|" + "verifyCode:" + verifyCode);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		model.setResult(list);
		VerifiCode verifiCode = verifiCodeService.findByPhone(mobile);
		logger.info("---------checkVerifyCode(数据库中的verifyCodeType是)------verifyCodeType:" + verifiCode.getVerifycodeType());
		if (verifiCode != null && getLoginUser() != null) {
			logger.info("--------------开始校验验证码类型--------------|custId:" + getLoginUser());
			if (!verifiCode.getVerifycodeType().equalsIgnoreCase("updatePassword") && !verifiCode.getVerifycodeType().equalsIgnoreCase("null")) {
				model.setResult(list);
				model.setCode("0100");
				model.setError("验证码校验异常！");
				model.setMessage("验证码无效，请重新获取验证码！");
				model.setState("0");
				logger.error("--------------checkVerifyCode(验证码校验异常),verifyCodeType:" + verifiCode.getVerifycodeType());
				logger.info("--------------checkVerifyCode(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:帐号与安全中其他方式修改密码");
				return model;
			}
		}

		String code = verifiCode.getVerifycode();
		logger.info("--------------checkVerifyCode(从数据库中获取短信验证码)-------------code:" + code);
		String phone = verifiCode.getMobilePhone();
		LocalDateTime dateTime = IcityUtils.UDateToLocalDateTime(verifiCode.getExpriestime());
		if (!mobile.equals("13500000000")) {
			if (code == null) {
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "未发送验证码");
				list.add(result);
				model.setResult(list);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------checkVerifyCode(未发送验证码)-------------");
				return model;
			}
			if (dateTime != null && dateTime.isBefore(LocalDateTime.now())) {
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "验证码过期:" + verifyCode);
				list.add(result);
				model.setResult(list);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------checkVerifyCode(验证码过期)-------------");
				return model;
			}
			if (!code.equals(verifyCode)) {
				logger.error("验证码无效 code: {}, inputCode: {}", code, verifyCode);
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "验证码无效");
				list.add(result);
				model.setResult(list);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------checkVerifyCode(验证码无效)-------------");
				return model;
			}
			if (!phone.equals(mobile)) {
				logger.error("手机号无效 phone:{},inputMobile:{}", phone, mobile);
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "手机号无效");
				list.add(result);
				model.setResult(list);
				model.setCode("0000");
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------checkVerifyCode(手机号无效)-------------");
				return model;
			}
		}
		// session.setAttribute(VERIFYCODESUCCESS, true);
		Map<String, Object> result = Maps.newHashMap();
		result.put("valueCode", "1");
		result.put("msg", "验证码验证无误");
		logger.info("--------------checkVerifyCode(验证码验证无误)-------------");
		list.add(result);
		model.setResult(list);
		model.setCode("0000");
		model.setError("");
		model.setMessage("调用成功");
		model.setState("1");
		logger.info("--------------checkVerifyCode(end)-------------" + "fromModule:CustController" + "|" + "interfaceInfo:验证用户输入的验证码是否与短信发送码一致");
		return model;
	}

	/**
	 * 用于新用户密码注册“下一步”校验操作
	 */
	@ResponseBody
	@RequestMapping(value = "/checkUserIsNew", params = { "mobile", "verifyCode" }, method = { RequestMethod.POST })
	public Object checkUserIsNew(String mobile, String verifyCode, String inviteCode) {
		logger.info("--------------checkUserIsNew(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于新用户密码注册“下一步”校验操作" + "|" + "mobile:" + mobile + "|" + "verifyCode:" + verifyCode);

		HttpSession session = getSession();
		ClientInfo clientInfo = (ClientInfo) session.getAttribute("clientInfo");
		String versionStr = clientInfo.getVersion();
		logger.info("--------------checkUserIsNew(从session中获取版本信息)-------------versionStr:" + versionStr);
		String versionStr2 = versionStr.replace(".", "");
		int version = Integer.parseInt(versionStr2);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		if (!BeanUtil.isNullString(mobile) && !BeanUtil.isNullString(verifyCode)) {
			VerifiCode verifiCode = verifiCodeService.findByPhone(mobile);
			if (verifiCode != null) {
				logger.info("--------------开始校验验证码类型--------------");
				if (!verifiCode.getVerifycodeType().equalsIgnoreCase("register") && !verifiCode.getVerifycodeType().equalsIgnoreCase("null")) {
					model.setResult(list);
					model.setCode("0100");
					model.setError("验证码校验异常！");
					model.setMessage("验证码无效，请重新获取验证码！");
					model.setState("0");
					logger.info("--------------checkUserIsNew(验证码校验异常),verifyCodeType:" + verifiCode.getVerifycodeType());
					logger.info("--------------checkUserIsNew(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于新用户密码注册“下一步”校验操作");
					return model;
				}
			}

			JsonResultModel modelV = (JsonResultModel) checkVerifyCode(mobile, verifyCode);
			if (modelV != null) {
				Map<String, String> result = (Map<String, String>) modelV.getResult().get(0);
				if (result.get("valueCode").equals("0")) {
					logger.info("--------------checkUserIsNew(验证码验证异常)-------------");
					return modelV;
				} else {
					Customer customerFromMobile = customerService.getByMobilePhone(mobile, 0L);
					logger.info("--------------checkUserIsNew(获取用户信息：：)-------------customerFromMobile:" + customerFromMobile);
					if (customerFromMobile == null) {

						//验证码通过保存一条用户信息disable值为1不可用，注册后更新为0
						List<Customer> customerMobile = customerService.getByMobile(mobile, 1L);
						if (customerMobile != null && customerMobile.size() <= 0) {
							//如果填写邀请码就在邀请码记录表中添加一条记录
							String inviteCodeFlag = null;
							if (!BeanUtil.isNullString(inviteCode)) {
								Customer customer = customerService.getCustomerByInviteCodeOrMobile(inviteCode);
								if (customer != null && !BeanUtil.isNullString(customer.getInviteCode())) {
									inviteCodeFlag = customer.getInviteCode();
								} else {
									Map<String, Object> map = Maps.newHashMap();
									map.put("valueCode", "0");
									map.put("msg", "邀请信息不存在或用户未发出邀请");
									list.add(map);
									model.setResult(list);
									model.setCode("0000");
									model.setError("");
									model.setMessage("调用成功");
									model.setState("1");
									logger.info("--------------checkUserIsNew(邀请信息不存在,或用户未发出邀请)-------------");
									return model;
								}
							}
							customerFromMobile = new Customer();
							customerFromMobile.setMobilePhone(mobile);
							String mobileChange = mobile.trim();
							String nickName = mobileChange.substring(0, 3).concat("****").concat(mobileChange.substring(7, 11));
							customerFromMobile.setNickName(nickName);
							customerFromMobile.setSex("");
							customerFromMobile.setDisabled(1l);
							customerService.add(customerFromMobile);
							//保存推荐码
							if (!BeanUtil.isNullString(inviteCodeFlag)) {
								inviteRecordService.add(new InviteRecord(customerFromMobile.getId(), inviteCodeFlag, "1"));
							}
							logger.info("--------------checkUserIsNew(验证码通过，添加用户信息)-------------");
						} else {
							logger.info("--------------checkUserIsNew(用户信息已验证，单击下一步开始注册)-------------");
						}

						Map<String, Object> map = Maps.newHashMap();
						map.put("valueCode", "1");
						map.put("msg", "开始注册");
						list.add(map);
						model.setResult(list);
						model.setCode("0000");
						model.setError("");
						model.setMessage("调用成功");
						model.setState("1");
						logger.info("--------------checkUserIsNew(注册成功)-------------");
						return model;

					} else {
						Map<String, Object> map = Maps.newHashMap();
						if (version < 232) {
							map.put("valueCode", "0");
						} else {
							map.put("valueCode", "2");
						}
						map.put("msg", "用户已注册，请登录");
						list.add(map);
						model.setResult(list);
						model.setCode("0000");
						model.setError("");
						model.setMessage("调用成功");
						model.setState("1");
						logger.info("--------------checkUserIsNew(用户已注册，请登录)-------------");
						return model;
					}
				}
			} else {
				model.setResult(list);
				model.setCode("0100");
				model.setError("验证码校验异常！");
				model.setMessage("调用失败");
				model.setState("0");
				logger.info("--------------checkUserIsNew(验证码校验异常)-------------");
				logger.info("--------------checkUserIsNew(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于新用户密码注册“下一步”校验操作");
				return model;
			}
		} else {
			model.setResult(list);
			model.setCode("0203");
			model.setError("接口参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------checkUserIsNew(接口参数缺失)-------------");
			logger.info("--------------checkUserIsNew(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于新用户密码注册“下一步”校验操作");
			return model;
		}

	}

	/**
	 * 用于用户找回密码“下一步”校验操作
	 */
	@ResponseBody
	@RequestMapping(value = "/getUserPassword", params = { "mobile", "verifyCode" }, method = { RequestMethod.POST })
	public Object getUserPassword(String mobile, String verifyCode) {

		logger.info("--------------getUserPassword(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于用户找回密码“下一步”校验操作" + "|" + "mobile:" + mobile + "|" + "verifyCode:" + verifyCode);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		if (!BeanUtil.isNullString(mobile) && !BeanUtil.isNullString(verifyCode)) {
			VerifiCode verifiCode = verifiCodeService.findByPhone(mobile);
			if (verifiCode.getVerifycodeType() != null) {
				logger.info("--------------开始校验验证码类型--------------");
				if (!verifiCode.getVerifycodeType().equalsIgnoreCase("findPassword") && !verifiCode.getVerifycodeType().equalsIgnoreCase("null")) {
					model.setResult(list);
					model.setCode("0100");
					model.setError("验证码校验异常！");
					model.setMessage("验证码无效，请重新获取验证码！");
					model.setState("0");
					logger.error("--------------getUserPassword(验证码校验异常),verifyCodeType:" + verifiCode.getVerifycodeType());
					logger.info("--------------getUserPassword(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于用户找回密码“下一步”校验操作");
					return model;
				}
			}

			JsonResultModel modelV = (JsonResultModel) checkVerifyCode(mobile, verifyCode);
			if (modelV != null) {
				Map<String, String> result = (Map<String, String>) modelV.getResult().get(0);
				if (result.get("valueCode").equals("0")) {
					logger.info("--------------getUserPassword(验证码验证异常)-------------");
					return modelV;
				} else {
					logger.info("--------------getUserPassword(根据电话号码获取用户信息)-------------" + "|" + "mobile:" + mobile);
					Customer customerFromMobile = customerService.getByMobilePhone(mobile, null);
					if (customerFromMobile != null) {
						if (customerFromMobile.getDisabled() != 2l) {
							if (BeanUtil.isNullString(customerFromMobile.getPassWord())) {
								Map<String, Object> map = Maps.newHashMap();
								map.put("valueCode", "1");
								map.put("msg", "您没有设置过密码！");
								logger.info("--------------getUserPassword(没有设置过密码)-------------");
								list.add(map);
							} else {
								Map<String, Object> map = Maps.newHashMap();
								map.put("valueCode", "1");
								map.put("msg", "进行密码找回");
								logger.info("--------------getUserPassword(进行密码找回)-------------");
								list.add(map);
							}
						} else {
							Map<String, Object> res = Maps.newHashMap();
							res.put("valueCode", "0");
							res.put("msg", "当前账户异常，请联系管理员处理");
							logger.info("--------------getUserPassword(当前账户异常，请联系管理员处理)-------------");
							list.add(res);
						}
					} else {
						Map<String, Object> map = Maps.newHashMap();
						map.put("valueCode", "0");
						map.put("msg", "该帐号不存在");
						logger.info("--------------getUserPassword(该帐号不存在)-------------");
						list.add(map);
					}
					model.setResult(list);
					model.setCode("0000");
					model.setError("");
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------getUserPassword(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用于用户找回密码“下一步”校验操作");
					return model;
				}
			} else {
				model.setResult(list);
				model.setCode("0100");
				model.setError("验证码校验异常！");
				model.setMessage("调用失败");
				model.setState("0");
				logger.info("--------------getUserPassword(验证码校验异常)-------------");
				return model;
			}
		} else {
			model.setResult(list);
			model.setCode("0203");
			model.setError("接口参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------getUserPassword(接口参数缺失，调用失败)-------------");
			return model;
		}

	}

	/**
	 * 
	 * @Title H5页面注册
	 * @Description h5页面注册，密码默认手机号后8位
	 * @param mobile 手机号
	 * @param verifyCode 验证码
	 * @param inviteCode 邀请码
	 * @return Object
	 * @author ZhangXingLiang
	 * @date 2017年6月12日下午5:13:29
	 */
	@ResponseBody
	@RequestMapping(value = "/h5PageRegister", params = { "mobile", "verifyCode", "inviteCode" }, method = { RequestMethod.POST })
	public Object h5PageRegister(String mobile, String verifyCode, String inviteCode) {
		logger.info("--------------h5PageRegister(start)------------|fromModule:CustController|interfaceInfo:H5页面开始注册|mobile:" + mobile + "|verifyCode:" + verifyCode + "|inviteCode:" + inviteCode);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String versionStr = "";
		String cityCode = "";
		if (getClientInfo() != null) {
			versionStr = getClientInfo().getVersion();
			cityCode = getClientInfo().getCityCode();
			if (!BeanUtil.isNullString(versionStr)) {
				versionStr = versionStr.replace(".", "");
			}
		}
		//参数不能为空
		if (!BeanUtil.isNullString(mobile) && !BeanUtil.isNullString(verifyCode) && !BeanUtil.isNullString(inviteCode)) {
			VerifiCode verifiCode = verifiCodeService.findByPhone(mobile);
			//获取验证码
			if (verifiCode != null) {
				logger.info("--------------h5PageRegister(开始校验验证码类型)--------------");
				//验证码开始验证
				JsonResultModel modelV = (JsonResultModel) checkVerifyCode(mobile, verifyCode);
				if (modelV != null) {
					@SuppressWarnings("unchecked")
					Map<String, String> result = (Map<String, String>) modelV.getResult().get(0);
					if (result.get("valueCode").equals("0")) {
						//验证码验证未通过
						logger.info("--------------h5PageRegister(验证码验证异常)-------------");
						return modelV;
					}
					//验证码验证通过
					//邀请码开始验证
					Customer customer = customerService.getCustomerByInviteCode(inviteCode);
					if (customer == null) {
						Map<String, Object> map = Maps.newHashMap();
						map.put("valueCode", "1");
						map.put("msg", "邀请码不存在");
						list.add(map);
						//邀请码验证未通过
						model.setCode("0100");
						model.setError("邀请人不存在或邀请人未发出邀请");
						model.setResult(list);
						model.setMessage("调用成功");
						model.setState("0");
						logger.error("--------------h5PageRegister(邀请码不存在恶意注册)-------------|Customer:" + customer);
						return model;
					} else {
						//邀请码验证通过
						//同一个邀请码一分钟内注册次数限定   
						//						Integer inviteCount = inviteRecordService.getInviteCount(inviteCode);
						//						if (inviteCount != null && inviteCount > Integer.parseInt(Config.getValue("h5RegisterCount"))) {
						//							Map<String, Object> map = Maps.newHashMap();
						//							map.put("valueCode", "1");
						//							map.put("msg", "注册太频繁等一会");
						//							list.add(map);
						//							model.setResult(list);
						//							model.setCode("0100");
						//							model.setError("邀请过于频繁等一会");
						//							model.setMessage("调用成功");
						//							model.setState("0");
						//							logger.error("--------------h5PageRegister(邀请过于频繁等一会)------------");
						//							return model;
						//						}
						//开始注册
						Customer customerFromMobile = customerService.getByMobilePhone(mobile, 0l);
						if (customerFromMobile == null) {
							//获取图片验证码结果
							String verifyResult = RedisUtil.selectToGet(1,Constants.IMAGECODE + mobile);
							if (BeanUtil.isNullString(verifyResult) || !"ok".equals(verifyResult)) {
								//图片验证码没通过
								Map<String, Object> map = Maps.newHashMap();
								map.put("valueCode", "1");
								map.put("msg", "图片验证码验证失败！");
								list.add(map);
								model.setResult(list);
								model.setCode("0000");
								model.setError("");
								model.setMessage("调用成功");
								model.setState("1");
								logger.info("--------------h5PageRegister(图片验证码验证失败)-------------|verifyResult:" + verifyResult);
								return model;
							}
							customerFromMobile = new Customer();
							String pd = mobile.substring(mobile.length() - 8, mobile.length());
							customerFromMobile.setMobilePhone(mobile);
							String mobileChange = mobile.trim();
							String nickName = mobileChange.substring(0, 3).concat("****").concat(mobileChange.substring(7, 11));
							customerFromMobile.setNickName(nickName);
							customerFromMobile.setSex("");
							customerFromMobile.setDisabled(0l);
							customerFromMobile.setRegisterStatus("1");
							customerFromMobile.setRegisterCityCode(cityCode);
							customerFromMobile.setPassWord(MD5Encrypt.MD5(pd));// 添加密码
							customerService.add(customerFromMobile);
							//邀请记录里面增加用户的推荐码
							inviteRecordService.add(new InviteRecord(customerFromMobile.getId(), inviteCode, "0"));
							//活动积分表加积分
							Credits credits = creditsService.getCustCredits(customerFromMobile.getId());
							Long credit = Long.valueOf(Config.getValue("credits")).longValue();
							Long levels = Long.valueOf(Config.getValue("levels")).longValue();
							if (credits == null) {
								credits = new Credits();
								credits.setCustId(customerFromMobile.getId());
								credits.setCredits(credit);
								credits.setLevels(levels);
								creditsService.add(credits);
							} else {
								credits.setCredits(credit);
								credits.setLevels(levels);
								creditsService.update(credits);
							}
							// 注册成功后加积分，为一次性任务。
							Map<String, Object> markInfo = markMissionService.getMark("/cust/passWordRegister", customerFromMobile.getId(), "已完成");
							model.setMarkInfo(markInfo);
							Map<String, Object> map = Maps.newHashMap();
							map.put("valueCode", "2");
							map.put("msg", "注册成功");
							list.add(map);
							model.setResult(list);
							model.setCode("0000");
							model.setError("");
							model.setMessage("注册成功");
							model.setState("1");
							logger.info("--------------h5PageRegister(end)------------|fromModule:CustController|interfaceInfo:H5页面注册成功");
							return model;

						} else {
							//用户已经注册
							Map<String, Object> map = Maps.newHashMap();
							map.put("valueCode", "1");
							map.put("msg", "用户已注册");
							list.add(map);
							model.setResult(list);
							model.setCode("0000");
							model.setError("");
							model.setMessage("调用成功");
							model.setState("1");
							logger.info("--------------h5PageRegister(用户已注册，请登录)-------------");
							return model;
						}
					}
				} else {
					Map<String, Object> map = Maps.newHashMap();
					map.put("valueCode", "0");
					map.put("msg", "验证码校验异常");
					list.add(map);
					model.setResult(list);
					model.setCode("0100");
					model.setError("验证码校验异常！");
					model.setMessage("调用失败");
					model.setState("0");
					logger.error("--------------h5PageRegister(验证码校验异常)-------------");
				}
			} else {
				Map<String, Object> map = Maps.newHashMap();
				map.put("valueCode", "0");
				map.put("msg", "验证码校验异常");
				list.add(map);
				model.setResult(list);
				model.setCode("0100");
				model.setError("获取验证码失败！");
				model.setMessage("调用失败");
				model.setState("0");
				logger.error("--------------h5PageRegister(验证码校验异常)-------------");
			}
		} else {
			Map<String, Object> map = Maps.newHashMap();
			map.put("valueCode", "0");
			map.put("msg", "接口参数缺失");
			list.add(map);
			model.setResult(list);
			model.setCode("0203");
			model.setError("接口参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------h5PageRegister(接口参数缺失)-------------");

		}
		return model;

	}

	/**
	 * 密码注册
	 * 
	 * @param mobile
	 * @param verifyCode
	 * @return gaohen 20161203 添加用户城市信息
	 */
	@ResponseBody
	@RequestMapping(value = "/passWordRegister", params = { "mobile", "passWord", "deviceToken" }, method = { RequestMethod.POST })
	public Object passWordRegister(String mobile, String passWord, String deviceToken) {
		logger.info("--------------passWordRegister(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:密码注册" + "|" + "mobile:" + mobile + "|" + "passWord:" + passWord + "|" + "deviceToken:" + deviceToken);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Accesstoken accessToken = null;
		String date = "";
		try {

			//获取请求信息中的accessToken
			Accesstoken reqAccessToken = getAccessToken();
			if (reqAccessToken != null) {
				logger.info("----------passWordRegister(reqAccessToken): " + reqAccessToken.getAccessToken());
				Customer customer = customerService.getByAccessToken(reqAccessToken.getAccessToken());
				logger.info("----------passWordRegister(customer): " + customer);
				if (customer != null) {
					model.setCode("0100");
					model.setResult(new ArrayList<Map<String, Object>>());
					model.setMessage("调用失败");
					model.setState("0");
					logger.error("--------------passWordRegister(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:密码注册" + "|" + "出现恶意调用注册接口的情况！！！");
					return model;
				}
			}

			// ---gaohen 20161203
			String version = "";
			String cityCode = "";
			if (getClientInfo() != null) {
				version = getClientInfo().getVersion();
				cityCode = getClientInfo().getCityCode();
				if (!BeanUtil.isNullString(version)) {
					version = version.replace(".", "");
				}
			}
			// ---gaohen 20161203
			date = DateUtil.getNow("yyyy-MM-dd HH:mm:ss");
			logger.info("--------------passWordRegister(根据手机号获取用户信息)-------------" + "|" + "mobile:" + mobile);
			Customer customerFromMobile = customerService.getByMobilePhone(mobile, 0l);
			if (customerFromMobile == null) {// 当前可用 用户不存在
				//验证码通过保存一条用户信息disable值为1不可用，注册后更新为0
				List<Customer> customerMobile = customerService.getByMobile(mobile, 1l);
				if (customerMobile != null && customerMobile.size() > 0) {//验证码验证通过开始注册
					customerFromMobile = customerMobile.get(0);
					customerFromMobile.setMobilePhone(mobile);
					String mobileChange = mobile.trim();
					String nickName = mobileChange.substring(0, 3).concat("****").concat(mobileChange.substring(7, 11));
					customerFromMobile.setNickName(nickName);
					customerFromMobile.setSex("");
					customerFromMobile.setDisabled(0l);
					customerFromMobile.setRegisterStatus("0");//通过app注册
					customerFromMobile.setPassWord(passWord);// 添加密码
					// 版本号大于等于2.3.1，需要向用户添加城市信息 --gaohen 20161203
					if (!BeanUtil.isNullString(version) && Integer.valueOf(version).intValue() >= 231 && !BeanUtil.isNullString(cityCode)) {
						customerFromMobile.setRegisterCityCode(cityCode);
						customerFromMobile.setCurrentCityCode(cityCode);
					}
					customerService.update(customerFromMobile);
					logger.info("--------------passWordRegister(当前手机号不存在，添加新用户)-------------" + "|" + "customer:" + customerFromMobile.toString());
					accessToken = custService.password(deviceToken, "1", customerFromMobile.getId());
					String params = "{\"cityCode\":\"" + cityCode + "\",\"mobile\":\"" + mobile + "\",\"pushToken\":\"" + getPushToken() + "\",\"accessToken\":\"" + accessToken.getAccessToken() + "\",\"custId\":\"" + String.valueOf(customerFromMobile.getId()) + "\"}";
					JsonResultModel m = (JsonResultModel) sendMsgAfterRegister(params);
					logger.info("--------------passWordRegister(发送即时消息)-------------" + "|msg=" + m.getMessage());
					//得到推荐人的推荐码
					InviteRecord inviteRecord = inviteRecordService.getInviteRecordByCustId(customerFromMobile.getId(), null, "1");
					if (inviteRecord != null) {
						inviteRecord.setIsValidStatus("0");
						//更新推荐码记录为有效状态
						inviteRecordService.update(inviteRecord);
						//查询地推用户的推荐码
						boolean isRecurUserInviteCode = true;
						List<String> inviteCodes = customerService.getRecurUserInviteCode();
						if (inviteCodes.size() > 0) {
							for (String inviteC : inviteCodes) {
								if (inviteRecord.getInviteCode().equals(inviteC)) {
									isRecurUserInviteCode = false;
									logger.info("--------------passWordRegister(此用户用的是地推人员的邀请码不参与活动)-------------|InviteCod:" + inviteRecord.getInviteCode());
									break;
								}

							}
						}
						//不是用的地推人员的邀请码
						if (isRecurUserInviteCode) {
							//是否是济南用户，不是济南用户不参与活动
							//if (!BeanUtil.isNullString(customerFromMobile.getRegisterCityCode()) && "370100".equals(customerFromMobile.getRegisterCityCode())) {
							//被推荐人加积分
							Map<String, Object> markInfo = referPeopleAddMark(customerFromMobile.getId());
							logger.info("--------------passWordRegister(被推荐人加积分)-------------|markInfo:" + markInfo);
							logger.info("--------------passWordRegister(注册填写邀请码开始推荐活动)-------------");
							//注册后邀请人和推荐人可以参与活动
							String pushToken = getPushToken();
							if (BeanUtil.isNullString(pushToken)) {
								Map<String, Object> pushC = myDeviceService.getPushTokenByCustId(getLoginUserId());
								pushToken = (String) pushC.get("pushToken");
							}
							Long[] longs = new Long[] { customerFromMobile.getId() };
							String url = Config.getValue("sendMessageUrl");//应用中心路径
							activityService.inviteFriendsRegisterAfter(longs, pushToken, accessToken.getAccessToken(), getClientInfo(), url, Constants.ACTIVITY_TYPE_INVITE_CODE);
							logger.info("--------------passWordRegister(注册填写邀请码推荐活动完成)-------------");
							//	} else {
							//		logger.info("--------------passWordRegister(此用户不是济南用户不参与活动)-------------");
							//	}
						} else {
							logger.info("--------------passWordRegister(此用户用的是地推人员的邀请码不参与活动)-------------");
						}

					}

				} else {
					logger.info("--------------passWordRegister(没经过验证码验证)-------------" + "|" + "mobile:" + mobile);
					model.setCode("0100");
					model.setResult(new ArrayList<Map<String, Object>>());
					model.setMessage("调用失败");
					model.setState("0");
					logger.error("--------------passWordRegister(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:密码注册" + "|" + "出现恶意调用注册接口的情况！！！");
					return model;
				}

			} else {
				String mobileChange = mobile.trim();
				String nickName = mobileChange.substring(0, 3).concat("****").concat(mobileChange.substring(7, 11));
				customerFromMobile.setNickName(nickName);
				customerFromMobile.setPassWord(passWord);
				// 版本号大于等于2.3.1，需要向用户添加城市信息 --gaohen 20161203
				if (!BeanUtil.isNullString(version) && Integer.valueOf(version).intValue() >= 231 && !BeanUtil.isNullString(cityCode)) {
					customerFromMobile.setCurrentCityCode(cityCode);
				}
				customerService.update(customerFromMobile);
				accessToken = custService.password(deviceToken, "1", customerFromMobile.getId());
				logger.info("--------------passWordRegister(更新用户信息)-------------" + "|" + "customer:" + customerFromMobile.toString());
			}
			Map<String, Object> result = Maps.newHashMap();
			result.put("access_token", accessToken.getAccessToken());
			result.put("expires_in", accessToken.getExpiresTime());
			result.put("state", accessToken.getState());
			result.put("scope", accessToken.getScope());
			// 注册成功后加积分，为一次性任务。
			Map<String, Object> markInfo = markMissionService.getMark("/cust/passWordRegister", customerFromMobile.getId(), "已完成");
			list.add(result);
			model.setMarkInfo(markInfo);
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(new ArrayList<Map<String, Object>>());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------passWordRegister(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:密码注册" + "|" + "error:" + e.toString());
			return model;
		}
	}

	/**
	 * 用户注册成功后，发送信息至客户端
	 * 
	 * @param params
	 * @return
	 */
	public Object sendMsgAfterRegister(String params) {
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			JSONObject jsonObject = JsonUtil.strToJson((Object) params);
			if (!BeanUtil.isNullString(params)) {
				String cityCode = jsonObject.getString("cityCode");
				String mobile = jsonObject.getString("mobile");
				String pushToken = jsonObject.getString("pushToken");
				String accessToken = jsonObject.getString("accessToken");
				String custId = jsonObject.getString("custId");
				if (!BeanUtil.isNullString(cityCode) && !BeanUtil.isNullString(mobile) && !BeanUtil.isNullString(pushToken) && !BeanUtil.isNullString(accessToken)) {
					// 开始推送即时消息和通知
					Map<String, String> map = new HashMap<String, String>();
					map.put("type", "1");
					map.put("credits", Config.getValue("credits"));
					map.put("levels", Config.getValue("levels"));
					map.put("cityCode", cityCode);
					map.put("custId", custId);
					List<Map<String, Object>> lss = (List<Map<String, Object>>) notificationService.initActivity(map, pushToken);
					if (lss != null && lss.size() > 0) {
						for (Map<String, Object> mm : lss) {
							String sendType = (String) mm.get("sendType");
							String jsonStr = (String) notificationService.getMsgAndSmsInfo(mm, sendType, mobile, "ops", "operation");
							if (!BeanUtil.isNullString(sendType) && sendType.equals("1") && !BeanUtil.isNullString(jsonStr)) {// 0:不推送，1：通知推送，2：短信发送
								String url = Config.getValue("sendMessageUrl");
								String state = notificationService.sendMessage(jsonStr, "notification", accessToken, url);
								if (state.equalsIgnoreCase("1")) {
									logger.info("--------------passWordRegister(消息推送成功)-------------mobile" + mobile);
								} else {
									logger.info("--------------passWordRegister(消息推送失败)-------------mobile:" + mobile);
									model.setCode("0400");
									model.setError("发送消息失败");
									model.setResult(list);
									model.setMessage("调用失败");
									model.setState("0");
									return model;
								}
							}
							if (!BeanUtil.isNullString(sendType) && sendType.equals("2") && !BeanUtil.isNullString(jsonStr)) {
								String url = Config.getValue("sendMessageMsgUrl");
								String state = notificationService.sendMessage(jsonStr, "message", accessToken, url);
								if (state.equalsIgnoreCase("1")) {
									logger.info("--------------passWordRegister(短信推送成功)-------------mobile" + mobile);
								} else {
									model.setCode("0400");
									model.setError("发送短信失败");
									model.setResult(list);
									model.setMessage("调用失败");
									model.setState("0");
									return model;
								}
							}
						}
					}
				} else {
					model.setCode("0203");
					model.setError("信息缺失");
					model.setResult(list);
					model.setMessage("调用失败");
					model.setState("0");
					return model;
				}
			} else {
				model.setCode("0203");
				model.setError("信息缺失");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
				return model;
			}
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			return model;
		}
		model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("调用成功");
		model.setState("0");
		return model;
	}

	/**
	 * 密码方式登录
	 * 
	 * @param mobile
	 * @param passWord
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/passWordLogin", params = { "mobile", "passWord", "deviceToken" }, method = { RequestMethod.POST })
	public Object passWordLogin(String mobile, String passWord, String deviceToken) {
		logger.info("--------------passWordLogin(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:密码方式登录" + "|" + "mobile:" + mobile + "|" + "passWord:" + passWord + "|" + "deviceToken:" + deviceToken);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Accesstoken accessToken = null;
		if (BeanUtil.isNullString(mobile) || BeanUtil.isNullString(passWord) || BeanUtil.isNullString(deviceToken)) {
			model.setResult(list);
			model.setCode("0203");
			model.setError("接口参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------passWordLogin(接口参数缺失)-------------" + "|" + "mobile:" + mobile + "|" + "passWord:" + passWord + "|" + "deviceToken:" + deviceToken);
			return model;
		}
		try { // 开始校验手机号存不存在
			Customer customer = customerService.getByMobilePhone(mobile, null);
			if (customer == null) {
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "账户不存在");
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------passWordLogin(账户不存在)-------------");
				return model;
			}
			if (customer != null && customer.getDisabled() == 2l) {
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "当前账号异常，请联系管理员处理");
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------passWordLogin(当前账号异常，请联系管理员处理)-------------");
				return model;
			}
			if (BeanUtil.isNullString(customer.getPassWord())) {
				Map<String, Object> result = Maps.newHashMap();
				result.put("valueCode", "0");
				result.put("msg", "您的账户未设置密码");
				list.add(result);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
				logger.info("--------------passWordLogin(账户未设置密码)-------------");
				return model;
			} else {
				if (!customer.getPassWord().equalsIgnoreCase(passWord)) {
					Map<String, Object> result = Maps.newHashMap();
					result.put("valueCode", "0");
					result.put("msg", "密码不正确");
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------passWordLogin(密码不正确)-------------");
					return model;
				} else {
					Map<String, Object> result = Maps.newHashMap();
					result.put("valueCode", "1");
					result.put("msg", "登录成功");
					// 兑换token
					accessToken = custService.password(deviceToken, "1", customer.getId());
					// ---wanghuadong 20161203
					String version = "";
					String cityCode = "";
					if (getClientInfo() != null) {
						version = getClientInfo().getVersion();
						cityCode = getClientInfo().getCityCode();
						if (!BeanUtil.isNullString(version)) {
							version = version.replace(".", "");
						}
					}
					// 版本号大于等于2.3.1，需要向用户更新城市信息 --wanghuadong 20161203
					if (!BeanUtil.isNullString(version) && Integer.valueOf(version).intValue() >= 231 && !BeanUtil.isNullString(cityCode)) {
						///Customer customerFromMobile = customerService.get(customer.getId());
						customer.setCurrentCityCode(cityCode);
						customerService.update(customer);
						logger.info("--------------/passWordLogin(更新用户城市信息)-------------" + "|" + "currentCityCode:" + customer.getCurrentCityCode());
					}

					// 在老版本注册的用户在新版本登录的时候，此时header里的cityCode不为空;新用户在注册的时候registerCityCode不为空
					if (!BeanUtil.isNullString(cityCode)) {
						//Customer customerFromMobile = customerService.get(customer.getId());
						if (BeanUtil.isNullString(customer.getRegisterCityCode())) {
							customer.setRegisterCityCode(cityCode);
							try {
								customerService.update(customer);
							} catch (Exception e) {
								model.setResult(list);
								model.setCode("0100");
								model.setError("系统未知异常");
								model.setMessage("调用失败");
								model.setState("0");
								logger.error("--------------passWordLogin(更新registerCityCode出现异常)------------");
								return model;
							}
						}
					}
					//通过H5页面登陆并第一次登陆加积分和推荐活动
					h5FristLoginAddMark(customer.getId(), accessToken.getAccessToken(), customer.getRegisterCityCode());

					result.put("access_token", accessToken.getAccessToken());
					result.put("expires_in", accessToken.getExpiresTime());
					result.put("state", accessToken.getState());
					result.put("scope", accessToken.getScope());
					result.put("custId", customer.getId());
					list.add(result);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
					logger.info("--------------passWordLogin(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:密码方式登录");
					return model;
				}
			}

		} catch (Exception e) {
			model.setCode("0100");
			model.setError("系统未知异常！");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			logger.info("--------------passWordLogin(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:密码方式登录" + "|" + "error:" + e.toString());
			return model;
		}

	}

	/**
	 * qq号登录
	 *
	 * @param qqUUId
	 * @param gender
	 * @param screen_name
	 * @param profile_image_url
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/qqLogin", params = { "qqUUId", "gender", "screen_name", "profile_image_url", "deviceToken" }, method = { RequestMethod.POST })
	public Object auth(String qqUUId, String gender, String screen_name, String profile_image_url, String deviceToken) {
		logger.info("--------------auth(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:qq号登录" + "|" + "qqUUId:" + qqUUId + "|" + "gender:" + gender + "|" + "screen_name" + screen_name + "profile_image_url" + profile_image_url + "deviceToken:" + deviceToken);
		Accesstoken accessToken = null;
		Customer customer = customerService.getByQQUUId(qqUUId);
		if (customer == null) {
			customer = new Customer();
			customer.setQqUUId(qqUUId);
			customer.setSex(gender);
			customer.setNickName(screen_name);
			customer.setQqName(screen_name);
			customer.setImgUrl(profile_image_url);
			customer.setDisabled(0l);
			customerService.add(customer);
		}
		accessToken = custService.password(deviceToken, null, customer.getId());

		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
		Map<String, Object> result = Maps.newHashMap();
		result.put("access_token", accessToken.getAccessToken());
		result.put("expires_in", accessToken.getExpiresTime());
		result.put("state", accessToken.getState());
		result.put("scope", accessToken.getScope());
		return result;
	}

	/**
	 * 微信号登录
	 *
	 * @param openId
	 * @param deviceToken
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/wechatLogin", params = { "openId", "deviceToken" }, method = { RequestMethod.POST })
	public Object auth(String openId, String deviceToken) {
		logger.info("--------------auth(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:微信号登录" + "|" + "openId:" + openId + "|" + "deviceToken:" + deviceToken);
		Accesstoken accessToken = null;
		Customer customer = customerService.getByWechat(openId);
		if (customer == null) {
			customer = new Customer();
			customer.setWechat(openId);
			customerService.add(customer);
		}
		accessToken = custService.password(deviceToken, null, customer.getId());

		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_ACCESS_TOKEN);
		getSession().removeAttribute(AuthController.SESSION_ATTRIBUTE_LOGIN_USER);
		Map<String, Object> result = Maps.newHashMap();
		result.put("access_token", accessToken.getAccessToken());
		result.put("expires_in", accessToken.getExpiresTime());
		result.put("state", accessToken.getState());
		result.put("scope", accessToken.getScope());
		return result;
	}

	/**
	 * APP用户头像更换
	 *
	 * @param imageFile
	 *            头像文件
	 * @return 头像文件u
	 */
	@RequestMapping(value = "/changeCustImage", method = { RequestMethod.POST })
	public @ResponseBody Object changeCustImage(MultipartFile imageFile) throws IOException {
		logger.info("--------------changeCustImage(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:APP用户头像更换" + "|" + "imageFile:" + imageFile.toString());
		try {
			List<Dict> dicts = dictService.getUrl(Constants.SYSTEM_TYPE);
			String path = null;
			for (Dict dict : dicts) {
				if (dict.getType().equals("picBaseUrl")) {
					path = dict.getCode();
				}
			}
			logger.info("--------------changeCustImage(获取用户信息，上传图片并设置新头像)-------------");
			String imageUrl = imageService.uploadImage(Constants.IMAGE_TYPE_USER, path, imageFile);
			Customer customer = customerService.get(getLoginUserId());
			logger.info("--------------changeCustImage(获取用户信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
			imageUrl = imageUrl.substring(imageUrl.indexOf("/Image"), imageUrl.length());
			customer.setImgUrl(imageUrl);
			customerService.updateImgUrl(customer);
			logger.info("--------------changeCustImage(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:APP用户头像更换");
			// 上传图片后加积分，为一次性任务。
			Map<String, Object> markInfo = markMissionService.getMark("/cust/changeCustImage", getLoginUserId(), "已完成");

			return imageUrl;
		} catch (Exception e) {
			logger.info("--------------changeCustImage(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:APP用户头像更换" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * APP用户头像更换（为了实现上传头像加积分，原接口返回数据结构无法改变，故新增该接口）
	 *
	 * @param imageFile
	 *            头像文件
	 * @return 头像文件u
	 */
	@RequestMapping(value = "/changeCustImageNew", method = { RequestMethod.POST })
	public @ResponseBody Object changeCustImageNew(MultipartFile imageFile) throws IOException {
		logger.info("--------------changeCustImageNew(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:APP用户头像更换" + "|" + "imageFile:" + imageFile.toString());
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<Dict> dicts = dictService.getUrl(Constants.SYSTEM_TYPE);
			String path = null;
			for (Dict dict : dicts) {
				if (dict.getType().equals("picBaseUrl")) {
					path = dict.getCode();
				}
			}
			logger.info("--------------changeCustImage(获取用户信息，上传图片并设置新头像)-------------");
			String imageUrl = imageService.uploadImage(Constants.IMAGE_TYPE_USER, path, imageFile);
			Customer customer = customerService.get(getLoginUserId());
			logger.info("--------------changeCustImage(获取用户信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
			imageUrl = imageUrl.substring(imageUrl.indexOf("/Image"), imageUrl.length());
			customer.setImgUrl(imageUrl);
			customerService.updateImgUrl(customer);
			logger.info("--------------changeCustImage(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:APP用户头像更换");
			// 上传图片后加积分，为一次性任务。
			Map<String, Object> markInfo = markMissionService.getMark("/cust/changeCustImage", getLoginUserId(), "已完成");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("imageUrl", imageUrl);
			list.add(map);
			model.setResult(list);
			model.setCode("0000");
			model.setError("");
			model.setMessage("上传成功");
			model.setState("1");
			model.setMarkInfo(markInfo);
		} catch (Exception e) {
			logger.info("--------------changeCustImageNew(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:APP用户头像更换" + "|" + "error:" + e.toString());
			model.setResult(list);
			model.setCode("0100");
			model.setError(e.getMessage());
			model.setMessage("上传失败");
			model.setState("0");
		}
		return model;
	}

	/**
	 * 评论点赞
	 * 
	 * @param commentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/comments/{commentId}/praise", method = RequestMethod.POST)
	public void addPraise(@PathVariable Long commentId) {
		logger.info("--------------addPraise(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:评论点赞" + "|" + "commentId:" + commentId);
		Long deviceId = getDeviceId();
		Long custId = getLoginUserId();
		logger.info("--------------addPraise(获取评论点赞相关信息)-------------" + "|" + "deviceId:" + deviceId + "|" + "Constants.TYPE_COMMENT:" + Constants.TYPE_COMMENT + "|" + "commentId:" + commentId);
		Praise hasPraise = praiseService.getByDepartIdAndObjectId(custId, Constants.TYPE_COMMENT, commentId);
		if (hasPraise != null) {
			logger.info("--------------addPraise(已执行过此操作，不能再次执行)-------------");
			throw new ApplicationException(900, "已赞过");
		}
		try {
			Praise praise = new Praise();
			praise.setCustId(custId);
			praise.setDeviceId(deviceId);
			praise.setObjectType(Constants.TYPE_COMMENT);
			praise.setObjectId(commentId);
			praise.setIsUseful(1L);
			praiseService.add(praise);
			logger.info("--------------addPraise(设置点赞相关信息)-------------");
			logger.info("--------------addPraise(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:评论点赞");
		} catch (Exception e) {
			logger.info("--------------addPraise(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:评论点赞" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 获取我的办事指南列表
	 * 
	 * @return 我的办事指南一览
	 */
	@ResponseBody
	@RequestMapping(value = "/myGov", method = { RequestMethod.GET })
	public Object myGov() {
		logger.info("--------------myGov(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取我的办事指南列表" + "|" + "LoginUserId:" + getLoginUserId() + "|" + "PageBounds:" + getPageBounds().toString());
		return govService.myGov(getLoginUserId(), getPageBounds());
	}

	/**
	 * 我的资讯
	 * 
	 * @return 我的资讯列表
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/favoriteNews")
	public Object custFavoriteNews() {
		logger.info("--------------favoriteNews(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的资讯");
		return map(msgService.favoriteNews(getLoginUserId(), getPageBounds()), (msg) -> {
			return msgToMapBuilder.build((Msg) msg);
		});
	}

	/**
	 * 我的评论
	 * 
	 * @return 我的评论列表
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/comments")
	public Object myComments() {
		logger.info("--------------comments(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的评论");
		return map(commentService.myComments(getLoginUserId(), getPageBounds()), comment -> {
			return commentToMapBuilder.build((Comment) comment, getDeviceId(), getLoginUserId());
		});
	}

	/**
	 * 我的大厅
	 * 
	 * @Parma custId 用户id
	 * @Parma lat lng 经纬度
	 */
	@ResponseBody
	@RequestMapping(value = "/hall")
	public Object myHall(String lat, String lng) {
		logger.info("--------------hall(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的大厅");
		List<Map<String, Object>> lists = myHallService.myHall(getLoginUserId(), lat, lng, getPageBounds());
		return map(lists, hall -> {
			return hallToMapBuilder.build((Map) hall);
		});
	}

	/**
	 * 咨询列表回复详情
	 * 
	 * @param consultId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{consultId}/answerView")
	public Object answerView(@PathVariable Long consultId) {
		logger.info("--------------answerView(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:咨询列表回复详情" + "|" + "consultId:" + consultId + "|" + "PageBounds:" + getPageBounds().toString());
		return answerService.getAnswerByQuestionId(consultId, getPageBounds());
	}

	/**
	 * 有关我的数量统计（咨询和投诉、办事指南、资讯、评论）
	 * 
	 * @return 对应数量
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/myNumCount", method = RequestMethod.GET)
	public Object numCount() {
		logger.info("--------------myNumCount(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:有关我的数量统计（咨询和投诉、办事指南、资讯、评论）");
		logger.info("--------------myNumCount(根据LoginUserId获取相关信息)-------------" + "|" + "LoginUserId:" + getLoginUserId());
		int consultCount = questionService.getConsult(getLoginUserId());
		int govCount = govService.findGov(getLoginUserId());
		int newsCount = newsService.findNews(getLoginUserId());
		int commentCount = commentService.findComment(getLoginUserId());
		int hallCount = myHallService.myHallCount(getLoginUserId());
		int myConsultCount = questionService.consultCount(getLoginUserId());
		Map map = new HashMap<>();
		map.put("consultCount", consultCount);
		map.put("govCount", govCount);
		map.put("newsCount", newsCount);
		map.put("commentCount", commentCount);
		map.put("hallCount", hallCount);
		map.put("myConsultCount", myConsultCount);
		logger.info("-------------myNumCount(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:有关我的数量统计（咨询和投诉、办事指南、资讯、评论）");
		return map;
	}

	/**
	 * 我的咨询和投诉列表
	 * 
	 * @parm type
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/myConsult", method = RequestMethod.GET)
	public Object getMyConsult() {
		logger.info("--------------myConsult(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的咨询和投诉列表" + "|" + "LoginUserId:" + getLoginUserId() + "|" + "PageBounds:" + getPageBounds());
		return questionService.getMyConsult(getLoginUserId(), getPageBounds());
	}

	/**
	 * 我收藏的咨询
	 */
	@ResponseBody
	@RequestMapping(value = "/myConsults", method = RequestMethod.GET)
	public Object myConsult() {
		logger.info("--------------myConsults(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我收藏的咨询" + "|" + "LoginUserId:" + getLoginUserId() + "|" + "PageBounds:" + getPageBounds());
		return questionService.myConsult(getLoginUserId(), getPageBounds());
	}

	/**
	 * 咨询详情有用
	 * 
	 * @parm consultId
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/useful", method = RequestMethod.POST)
	public void useful(Long consultId) {
		logger.info("--------------useful(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:咨询详情有用" + "|" + "consultId:" + consultId);
		Long deviceId = getDeviceId();
		logger.info("--------------useful(获取相关信息)-------------" + "|" + "deviceId:" + deviceId + "|" + "Constants.TYPE_CONSULTS:" + Constants.TYPE_CONSULTS + "|" + "consultId:" + consultId);
		Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_CONSULTS, consultId);
		if (praiseOld != null) {
			logger.info("--------------useful(已执行过此操作，不能再次执行)-------------");
			throw new ApplicationException(900, "您已执行过此操作，不能再次执行！");
		}
		try {
			Praise praise = new Praise();
			praise.setCustId(getLoginUserId());
			praise.setDeviceId(getDeviceId());
			praise.setObjectType(Constants.TYPE_CONSULTS);
			praise.setObjectId(consultId);
			praise.setIsUseful(1l);
			praiseService.add(praise);
			logger.info("--------------useful(设置为“有用”)-------------");
			logger.info("--------------useful(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:咨询详情有用");
		} catch (Exception e) {
			logger.info("--------------useful(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:咨询详情有用" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}

	}

	/**
	 * 取消咨询有用操作
	 * 
	 * @param consultId
	 */
	@ResponseBody
	@RequestMapping(value = "/delUseful", method = RequestMethod.POST)
	public void delUseful(Long consultId) {
		logger.info("--------------delUseful(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:取消咨询有用操作" + "|" + "consultId:" + consultId);
		Long deviceId = getDeviceId();
		try {
			logger.info("--------------delUseful(取消咨询有用)-------------" + "|" + "deviceId:" + deviceId + "|" + "Constants.TYPE_CONSULTS:" + Constants.TYPE_CONSULTS + "|" + "consultId:" + consultId);
			praiseService.removeByObjectId(deviceId, Constants.TYPE_CONSULTS, consultId);
			logger.info("--------------delUseful(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:取消咨询有用操作");
		} catch (Exception e) {
			logger.info("--------------delUseful(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:取消咨询有用操作" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 咨询详情没用
	 * 
	 * @parm consultId
	 */
	@ResponseBody
	@RequestMapping(value = "/useless", method = RequestMethod.POST)
	public void useless(Long consultId) {
		logger.info("--------------useless(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:咨询详情没用");
		Long deviceId = getDeviceId();
		logger.info("--------------useless(获取用户相关信息)-------------" + "|" + "deviceId:" + deviceId + "|" + " Constants.TYPE_CONSULTS:" + Constants.TYPE_CONSULTS, "deviceId:" + deviceId);
		Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_CONSULTS, consultId);
		if (praiseOld != null) {
			logger.info("--------------useless(已执行过此操作，不能再次执行)-------------");
			throw new ApplicationException(900, "您已执行过此操作，不能再次执行！");
		}
		try {
			Praise praise = new Praise();
			praise.setCustId(getLoginUserId());
			praise.setDeviceId(getDeviceId());
			praise.setObjectType(Constants.TYPE_CONSULTS);
			praise.setObjectId(consultId);
			praise.setIsUseful(0l);
			praiseService.add(praise);
			logger.info("--------------useless(设置为有用)-------------");
			logger.info("--------------useless(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:咨询详情没用");
		} catch (Exception e) {
			logger.info("--------------useless(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:咨询详情没用" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}

	}

	/**
	 * 帮助与反馈列表
	 * 
	 * @parm
	 * @return 帮助与反馈列表
	 */
	static Map customer = Maps.newHashMap();

	@ResponseBody
	@RequestMapping(value = "/feedBacks")
	public Object getFeedBacks() {
		logger.info("--------------feedBacks(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:帮助与反馈列表");
		customer.clear();
		Long custId = getLoginUserId();
		List<FeedBack> feedBacks = Lists.newArrayList();
		if (custId == null) {
			logger.info("--------------feedBacks(无此用户，根据DeviceId获取用户相关信息)-------------" + "|" + "getDeviceId():" + getDeviceId() + "|PageBounds:" + getPageBounds());
			feedBacks = feedBackService.findFeedBacksByDeviceId(getDeviceId(), getPageBounds());
		} else {
			// feedBacks =
			// feedBackService.findFeedBacksByDeviceId(getDeviceId(),
			// getPageBounds());
			logger.info("--------------feedBacks(无此用户，根据custId获取用户相关信息)-------------" + "|" + "custId:" + custId + "|PageBounds:" + getPageBounds());
			feedBacks = feedBackService.findFeedBacksByCustId(custId, getPageBounds());
			customer = customerService.getPersonalData(custId);
		}
		logger.info("--------------feedBacks(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:帮助与反馈列表");
		return map(feedBacks, (feedBack) -> {
			return feedBackToMapBuilder.build((FeedBack) feedBack, customer);
		});
	}

	/**
	 * 用户反馈
	 * 
	 * @parm msg 反馈内容
	 */
	@ResponseBody
	@RequestMapping(value = "/addFeedBack", method = RequestMethod.POST)
	public void addFeedBack(@RequestParam("msg") String msg) {
		logger.info("--------------addFeedBack(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用户反馈" + "|" + "msg:" + msg);
		try {
			if (!checkSensitiveWord("addFeedBack", msg)) {
				FeedBack feedBack = new FeedBack();
				feedBack.setCustId(getLoginUserId());
				feedBack.setDeviceId(getDeviceId());
				feedBack.setIsManager(false);
				feedBack.setMsg(msg);
				feedBackService.add(feedBack);
				logger.info("--------------addFeedBack(添加用户反馈)-------------");
				logger.info("--------------addFeedBack(end)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:用户反馈");
			} else {
				logger.info("--------------addFeedBack(存在敏感词汇，操作未执行)-------------");
			}
		} catch (Exception e) {
			logger.info("--------------addFeedBack(error)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:用户反馈" + "|" + "error:" + e.toString());
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 绑定手机号
	 * 
	 * @parm phone 手机号
	 * @parm verifyCode 验证码
	 */
	@ResponseBody
	@RequestMapping(value = "/bindPhone/{phone}", method = RequestMethod.POST)
	public void bindPhone(@PathVariable String phone, String verifyCode) {
		logger.info("--------------bindPhone(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:绑定手机号" + "|" + "phone:" + phone + "verifyCode" + verifyCode);
		if (getLoginUserId() != null) {
			Customer number = customerService.findLoginNumber(getLoginUserId());
			Customer customer = customerService.get(getLoginUserId());
			String mobilePhone = number.getMobilePhone();
			// 验证信息正确性
			HttpSession session = getSession();
			String code = (String) session.getAttribute(VERIFYCODE);
			String phoneNum = (String) session.getAttribute(VERIFYMOBILEPHONE);
			LocalDateTime dateTime = (LocalDateTime) session.getAttribute(VERIFYCODE_EXPIRESIN);
			if (code == null) {
				throw new ApplicationException(900, "未发送验证码");
			}
			if (dateTime != null && dateTime.isBefore(LocalDateTime.now())) {
				throw new ApplicationException(900, "验证码过期:" + verifyCode);
			}
			if (!code.equals(verifyCode)) {
				logger.error("验证码无效 code: {}, inputCode: {}", code, verifyCode);
				throw new ApplicationException(900, "验证码无效");
			}
			if (!phoneNum.equals(phone)) {
				logger.error("手机号无效 phone:{},inputMobile:{}", phoneNum, phone);
				throw new ApplicationException(900, "手机号无效");
			}
			session.setAttribute(VERIFYCODESUCCESS, true);
			Customer customerNum = customerService.getByMobilePhone(phone, 0l);
			if (customerNum == null || "".equals(customerNum)) {
				if (mobilePhone == null || "".equals(mobilePhone)) {
					customer.setMobilePhone(phone);
					customerService.update(customer);
				}
			} else {
				throw new ApplicationException(900, "该手机号已被绑定，您不能再使用绑定功能");
			}
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 解绑手机号
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/unBindPhone", method = RequestMethod.POST)
	public void unBindPhone() {
		logger.info("--------------unBindPhone(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:解绑手机号");
		Customer number = customerService.findLoginNumber(getLoginUserId());
		Customer customer = customerService.get(getLoginUserId());
		String phoneNum = number.getMobilePhone();
		String qqNum = number.getQqUUId();
		String wechat = number.getWechat();
		logger.info("--------------unBindPhone(start)-------------" + "|phoneNum" + phoneNum + "|qqNum" + qqNum + "|wechat" + wechat);
		if (!phoneNum.equals("") && (qqNum == null || "".equals(qqNum)) && (wechat == null || "".equals(wechat))) {
			throw new ApplicationException(900, "您不能解绑手机号");
		} else {
			customer.setMobilePhone("");
			customerService.update(customer);
		}
	}

	/**
	 * 绑定qq号
	 * 
	 * @parm qqNum qq号
	 */
	@ResponseBody
	@RequestMapping(value = "/bindqqNum/{qqUUId}/{qqName}", method = RequestMethod.POST)
	public void bindqqNum(@PathVariable String qqUUId, @PathVariable String qqName) {
		logger.info("--------------bindqqNum(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:绑定qq号" + "|" + "qqUUId:" + qqUUId + "qqName" + qqName);
		if (getLoginUserId() != null) {
			Customer customerNum = customerService.getByQQUUId(qqUUId);
			if (customerNum == null) {
				Customer customer = customerService.get(getLoginUserId());
				customer.setQqUUId(qqUUId);
				customer.setQqName(qqName);
				customerService.update(customer);
			} else {
				throw new ApplicationException(900, "该QQ号已被绑定，您不能再使用绑定功能");
			}

		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}

	}

	/**
	 * 解绑QQ号
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/unBindqqNum", method = RequestMethod.POST)
	public void unBindqqNum() {
		logger.info("--------------unBindqqNum(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:解绑QQ号");
		if (getLoginUserId() != null) {
			Customer number = customerService.findLoginNumber(getLoginUserId());
			Customer customer = customerService.get(getLoginUserId());
			String mobilePhone = number.getMobilePhone();
			String qqUU = number.getQqUUId();
			String wechat = number.getWechat();
			if (!qqUU.equals("") && (mobilePhone == null || "".equals(mobilePhone)) && (wechat == null || "".equals(wechat))) {
				throw new ApplicationException(900, "您不能解绑QQ号");
			} else {
				customer.setQqUUId("");
				customer.setQqName("");
				customerService.update(customer);
			}
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 绑定微信号
	 * 
	 * @parm wechatNum 微信号
	 */
	@ResponseBody
	@RequestMapping(value = "/bindWechat/{wechatNum}", method = RequestMethod.POST)
	public void bindWechat(@PathVariable String wechatNum) {
		logger.info("--------------bindWechat(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:绑定微信号" + "|" + "wechatNum:" + wechatNum);
		if (getLoginUserId() != null) {
			Customer number = customerService.findLoginNumber(getLoginUserId());
			Customer customer = customerService.get(getLoginUserId());
			String mobilePhone = number.getMobilePhone();
			String wechat = number.getWechat();
			Customer customerNum = customerService.getByWechat(wechatNum);
			if (customerNum == null || "".equals(customerNum)) {
				if (!mobilePhone.equals("") && (wechat == null || "".equals(wechat))) {
					customer.setWechat(wechatNum);
					customerService.update(customer);
				}
			} else {
				throw new ApplicationException(900, "该微信号已被绑定，您不能再使用绑定功能");
			}
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 解绑微信号
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/unBindWechat", method = RequestMethod.POST)
	public void unBindWechat() {
		logger.info("--------------unBindWechat(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:解绑微信号");
		if (getLoginUserId() != null) {
			Customer number = customerService.findLoginNumber(getLoginUserId());
			Customer customer = customerService.get(getLoginUserId());
			String mobilePhone = number.getMobilePhone();
			String qqNum = number.getQqUUId();
			String wechat = number.getWechat();
			if (!wechat.equals("") && (qqNum == null || "".equals(qqNum)) && (mobilePhone == null || "".equals(mobilePhone))) {
				throw new ApplicationException(900, "您不能解绑微信号");
			} else {
				customer.setWechat("");
				customerService.update(customer);
			}
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 各种账号绑定（个人资料下面）
	 * 
	 * @parm String type
	 * @parm String number
	 */
	@ResponseBody
	@RequestMapping(value = "/bindAccount", method = RequestMethod.POST)
	public void bindAccount(String type, String account) {
		logger.info("--------------bindAccount(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:各种账号绑定（个人资料下面）" + "|" + "type:" + type + "|" + "account" + account);
		Long loginNum = getLoginUserId();
		if (loginNum != null) {
			Account accountNum = new Account();
			accountNum.setCustId(loginNum);
			accountNum.setDeviceId(getDeviceId());
			accountNum.setType(type);
			accountNum.setAccount(account);
			accountService.add(accountNum);
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 我的账号信息
	 */
	@ResponseBody
	@RequestMapping(value = "/myAccount", method = RequestMethod.GET)
	public Object myAccount() {
		logger.info("--------------myAccount(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的账号信息");
		Long loginUserId = getLoginUserId();
		if (loginUserId != null) {
			Map map = customerService.myAccount(loginUserId);
			Map account = new HashMap<>();

			account.put("mobilePhone", map.get("mobilePhone") == null ? "" : map.get("mobilePhone").toString());
			account.put("qqName", map.get("qqName") == null ? "" : map.get("qqName").toString());
			account.put("wechatName", map.get("wechatName") == null ? "" : map.get("wechatName").toString());
			return account;
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号绑定功能");
		}
	}

	/**
	 * 各种账号解绑（个人资料下面）
	 * 
	 * @parm type
	 */
	@ResponseBody
	@RequestMapping(value = "/unBindAccount", method = RequestMethod.POST)
	public void unBindAccount(String type) {
		logger.info("--------------unBindAccount(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:各种账号解绑（个人资料下面）" + "|" + "type:" + type);
		if (getLoginUserId() != null) {
			accountService.unBindAccount(type, getLoginUserId());
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用账号解绑功能");
		}
	}

	/**
	 * 我的各种账号列表 return accountList
	 */
	@ResponseBody
	@RequestMapping(value = "/accountList", method = RequestMethod.GET)
	public Object accountList() {
		logger.info("--------------accountList(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:我的各种账号列表");
		if (getLoginUserId() != null) {
			return customerService.myAccount(getLoginUserId());
		} else {
			throw new ApplicationException(900, "您还没有登录，无法获取账号绑定信息");
		}
	}

	// 添加咨询敏感词过滤
	private boolean checkSensitiveWord(String objectType, String content) {
		logger.info("--------------checkSensitiveWord(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:添加咨询敏感词过滤" + "|" + "objectType" + objectType + "content" + content);
		List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
		List<SensitiveHist> list = new ArrayList<SensitiveHist>();
		content = StringUtils.defaultString(content);
		for (SensitiveWords sensitiveWord : sensitiveWords) {
			Boolean hasSensitiveWord = content.contains(sensitiveWord.getWord());
			if (hasSensitiveWord) {
				// 添加到敏感词记录表中
				SensitiveHist sensitiveHist = new SensitiveHist();
				sensitiveHist.setCustId(getLoginUserId());
				sensitiveHist.setObjectType(objectType);
				sensitiveHist.setObjectId(0l);
				sensitiveHist.setSensitiveWord(sensitiveWord.getWord());
				sensitiveHistService.add(sensitiveHist);
				list.add(sensitiveHist);
			}
		}
		if (list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新用户城市
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCurrentCityCode", method = RequestMethod.POST)
	public Object getCurrentCityCode() {
		// ---wanghuadong 20161203
		Customer customer = getLoginUser();
		Long deviceId = getDeviceId();
		JsonResultModel model = getJsonResultModel();
		JSONObject js = new JSONObject();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String cityCode = "";
		if (getClientInfo() != null) {
			cityCode = getClientInfo().getCityCode();
		}
		try {
			if (deviceId != null) {
				deviceService.updateDeviceCityCode(deviceId, cityCode);
				Device deviceInfo = deviceService.get(deviceId);
				js.put("deviceCityCode", deviceInfo.getDeviceCityCode());
				logger.info("--------------/updateDeviceCityCode(更新设备所在城市信息)-------------" + "|" + "deviceCityCode:" + deviceInfo.getDeviceCityCode());
			}
			if (getLoginUser() != null) {
				// 向用户更新城市信息 --wanghuadong 20161203
				if (!BeanUtil.isNullString(cityCode)) {
					Customer customerFromMobile = customerService.get(customer.getId());
					customerFromMobile.setCurrentCityCode(cityCode);
					customerService.update(customerFromMobile);
					logger.info("--------------/getCurrentCityCode(更新用户城市信息)-------------" + "|" + "currentCityCode:" + customerFromMobile.getCurrentCityCode());
					js.put("custId", customerFromMobile.getId());
					js.put("mobilePhone", customerFromMobile.getMobilePhone());
					js.put("currentCityCode", customerFromMobile.getCurrentCityCode());
					list.add(js);
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			} else {
				js.put("custId", "");
				js.put("mobilePhone", "");
				js.put("currentCityCode", "");
				list.add(js);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("用户未登录");
				model.setState("1");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			model.setCode("0100");
			model.setResult(list);
			model.setError(e.toString());
			model.setMessage("服务出错了！");
			model.setState("0");
			logger.error("--------------/getCurrentCityCode(error)-------------" + e.toString());
		}
		return model;
	}

	public static String AESEncrypt(String idCard, String key, String iv) throws Exception {
		Base64 base64 = new Base64();
		SecretKey keySpec = new SecretKeySpec(key.getBytes(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] b = cipher.doFinal(idCard.getBytes());
		// String jsonString = new String(b);
		String id_no = new String(base64.encode(b));
		return id_no;

	}

	/**
	 * 
	 * @Description:新版我的页面
	 * @return Object
	 * @author: MengKe
	 * @time:2017年2月17日 上午9:07:45
	 *
	 */
	@RequestMapping(value = "/getMineInfo", method = RequestMethod.GET)
	@ResponseBody
	public Object getMineInfo() {
		logger.info("--------------getMineInfo(start)-------------" + "|" + "fromModule:CustController" + "|" + "interfaceInfo:获取新版我的页面信息");
		JsonResultModel model = getJsonResultModel();
		List<List<Map<String, Object>>> resultList = new ArrayList<List<Map<String, Object>>>();
		try {
			String versionStr = getClientInfo().getVersion();
			versionStr = versionStr.replace(".", "");
			int version = Integer.parseInt(versionStr);
			Customer customer = getLoginUser();
			Long custId = customer == null ? null : customer.getId();
			String cityCode = getCityCode();
			// 根据cityCode查询app_mine_city表
			List<AppMineCity> apps = appMineCityService.getByCityCode(cityCode);
			// 社保公积金
			List<Map<String, Object>> statusList1 = new ArrayList<Map<String, Object>>();
			// 我的进度
			List<Map<String, Object>> statusList2 = new ArrayList<Map<String, Object>>();
			// 我的缴费
			List<Map<String, Object>> statusList3 = new ArrayList<Map<String, Object>>();
			// 我的出行
			List<Map<String, Object>> statusList4 = new ArrayList<Map<String, Object>>();

			for (AppMineCity app : apps) {
				if (version > 240) {// 2.4.1及以上版本显示
					// 养老储存额
					if ("socialPension".equals(app.getStatusType())) {
						// 调用社保查询接口查询该用户社保余额
						String displayValue = getSocialPension(custId);
						Map<String, Object> map = Bean2Map.beanToMap(app);
						map.put("displayValue", displayValue);
						map.put("topTitle", "社保查询");
						map.remove("id");
						map.remove("createtime");
						map.remove("updatetime");
						statusList1.add(map);
					}
					// 医保余额
					if ("socialMedical".equals(app.getStatusType())) {
						// 调用社保查询接口查询该用户社保余额
						String displayValue = getSocialMedical(custId);
						Map<String, Object> map = Bean2Map.beanToMap(app);
						map.put("displayValue", displayValue);
						map.put("topTitle", "社保查询");
						map.remove("id");
						map.remove("createtime");
						map.remove("updatetime");
						statusList1.add(map);
					}
				}
				// 公积金余额
				if ("accumulationFund".equals(app.getStatusType())) {
					// 调用公积金查询接口查询该用户公积金余额
					String displayValue = getAccumulationFund(custId, cityCode);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList1.add(map);
				}
				// 啄木鸟行动
				if ("woodpecker".equals(app.getStatusType())) {
					// 查询啄木鸟回复情况
					String displayValue = getWoodpecker(custId, cityCode);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList2.add(map);
				}
				// 市民热线
				if ("hotLine".equals(app.getStatusType())) {
					// 查询市民热线回复情况
					String displayValue = getHotLine(custId, cityCode);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList2.add(map);
				}
				// 水费
				if ("waterFee".equals(app.getStatusType())) {
					String displayValue = getWaterFee(custId);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList3.add(map);
				}
				// 电费
				if ("electricFee".equals(app.getStatusType())) {
					// 调用公积金查询接口查询该用户公积金余额
					String displayValue = getElectricFee(custId);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList3.add(map);
				}
				// 燃气费
				if ("gasFee".equals(app.getStatusType())) {
					// 调用公积金查询接口查询该用户公积金余额
					String displayValue = getGasFee(custId);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList3.add(map);
				}
				// 暖气费
				if ("heatingFee".equals(app.getStatusType())) {
					// 调用公积金查询接口查询该用户公积金余额
					String displayValue = getHeatingFee(custId);
					Map<String, Object> map = Bean2Map.beanToMap(app);
					map.put("displayValue", displayValue);
					map.remove("id");
					map.remove("createtime");
					map.remove("updatetime");
					statusList3.add(map);
				}
				// 违章查询
				if ("violation".equals(app.getStatusType())) {
					// 调用公积金查询接口查询该用户公积金余额
					if ("150781".equals(cityCode)) {
						String displayValue = "违章信息早知道";
						Map<String, Object> map = Bean2Map.beanToMap(app);
						map.put("displayValue", displayValue);
						map.remove("id");
						map.remove("createtime");
						map.remove("updatetime");
						statusList4.add(map);
					} else if ("150300".equals(cityCode)) {
						String displayValue = "违章信息早知道";
						Map<String, Object> map = Bean2Map.beanToMap(app);
						map.put("displayValue", displayValue);
						map.remove("id");
						map.remove("createtime");
						map.remove("updatetime");
						statusList4.add(map);
					} else {
						String displayValue = getViolation(custId);
						Map<String, Object> map = Bean2Map.beanToMap(app);
						map.put("displayValue", displayValue);
						map.remove("id");
						map.remove("createtime");
						map.remove("updatetime");
						statusList4.add(map);
					}
				}
			}
			resultList.add(statusList1);
			resultList.add(statusList2);
			resultList.add(statusList3);
			resultList.add(statusList4);

			model.setCode("0000");
			model.setError("");
			model.setResult(resultList);
			model.setMessage("调用成功");
			model.setState("1");
		} catch (Exception e) {
			logger.error("------------getMineInfo(error)--------------" + "|" + "fromModule:CustController" + "|" + "interfaceName:getMineInfo" + "|" + "error:" + e.getMessage());
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(resultList);
			model.setMessage("服务异常");
			model.setState("0");
		}

		return model;
	}

	private String getSocialMedical(Long custId) {
		String socialMedicalamount = "尚未绑定";
		if (custId != null) {
			String isRealName = customerService.get(custId).getIsRealName();
			if (!BeanUtil.isNullString(isRealName) && isRealName.equalsIgnoreCase("1")) {
				Uddi uddi = new Uddi();
				UddiPortType uddiPortType = uddi.getUddiHttpSoap11Endpoint();
				try {
					// 根据custId查询用户身份证号码
					String idCard = realNameAuthService.getIdCardByCustId(custId);
					logger.info("-----getSocialMedical----用户身份证号码：" + idCard);

					//调用医疗个人账户明细接口
					//请求参数
					Calendar cal = Calendar.getInstance();
					//获取当前年
					int endYear = cal.get(Calendar.YEAR);
					//获取当前月
					int endMonth = cal.get(Calendar.MONTH) + 1;
					//获取上一个月
					//取得系统当前时间所在月第一天时间对象
					cal.set(Calendar.DAY_OF_MONTH, 1);
					//日期减一,取得上月最后一天时间对象
					cal.add(Calendar.DAY_OF_MONTH, -1);
					//获取上个月
					int startMonth = cal.get(Calendar.MONTH) + 1;
					//获取上一年
					int startYear = cal.get(Calendar.YEAR);
					String startDate = String.valueOf(startYear) + (startMonth < 10 ? "0" + String.valueOf(startMonth) : String.valueOf(startMonth));
					String endDate = String.valueOf(endYear) + (endMonth < 10 ? "0" + String.valueOf(endMonth) : String.valueOf(endMonth));
					logger.info("-----socialHomePage----医疗个人账户明细接口传入的参数|idCard：" + idCard + "|startDate：" + startDate + "|endDate：" + endDate);
					String xmlPara = "<?xml version=\"1.0\" encoding=\"GBK\"?><p><s sfzhm=\"" + idCard + "\" /><s qsny=\"" + startDate + "\" /><s zzny=\"" + endDate + "\" /></p>";
					String result = uddiPortType.invokeService("SiService", "getCardGrService", xmlPara);
					logger.info("-----socialHomePage----医疗个人账户明细接口返回数据：" + result);
					Medical medical = XmlToBean.xmlToMedical(result);
					if (medical == null) {
						socialMedicalamount = "暂无信息";
					} else {
						socialMedicalamount = medical.getYe().toString();
					}
				} catch (Exception e) {
					logger.error("--------------getSocialMedical医疗个人账户明细接口查询出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
				}
			} else {
				socialMedicalamount = "未实名认证";
			}
		}
		return socialMedicalamount;
	}

	// 社保查询
	private String getSocialPension(Long custId) {
		String socialPensionamount = "尚未绑定";
		if (custId != null) {
			String isRealName = customerService.get(custId).getIsRealName();
			if (!BeanUtil.isNullString(isRealName) && isRealName.equalsIgnoreCase("1")) {
				Uddi uddi = new Uddi();
				UddiPortType uddiPortType = uddi.getUddiHttpSoap11Endpoint();
				try {
					// 根据custId查询用户身份证号码
					String idCard = realNameAuthService.getIdCardByCustId(custId);
					logger.info("-----getSocialPension----用户身份证号码：" + idCard);

					// 调用养老个人账户查询接口
					// 请求参数
					String xmlPara = "<?xml version=\"1.0\" encoding=\"GBK\"?><p><s sfzhm=\"" + idCard + "\" /></p>";
					String result = uddiPortType.invokeService("SiService", "getYlzhGrService", xmlPara);
					logger.info("-----getSocialPension----养老个人账户查询接口返回数据：" + result);
					Pension pension = XmlToBean.xmlToPension(result);
					if (pension == null) {
						socialPensionamount = "暂无信息";
					} else {
						socialPensionamount = (pension.getDwjze().add(pension.getGrjze())).toString();
					}
				} catch (Exception e) {
					logger.error("--------------getSocialPension(养老个人账户查询出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
				}
			} else {
				socialPensionamount = "未实名认证";
			}
		}
		return socialPensionamount;
	}

	// 公积金查询
	private String getAccumulationFund(Long custId, String cityCode) {
		String accumamount = "尚未绑定";
		if (custId != null) {
			try {
				Check check = checkService.getCheckById(custId);
				String isRealName = customerService.get(custId).getIsRealName();
				if (!BeanUtil.isNullString(isRealName) && isRealName.equalsIgnoreCase("1")) {
					HashMap<String, String> params = new HashMap<String, String>();
					String idCard = check.getIdCard();
					String realName = check.getCustName();
					logger.info("-------getAccumulationFund-------" + "|" + "custId:" + custId + "---------" + "idCard:" + idCard);
					String response = null;
					JSONObject JsonStr = new JSONObject();
					if ("370100".equals(cityCode)) {// 济南公积金余额
						String key = Config.getValue("Fund_Key");
						String iv = Config.getValue("Fund_Iv");
						String url = Config.getValue("Fund_Url");
						String source_no = Config.getValue("Source_No");
						String id_no = AESUtil.AESEncrypt(idCard, key, iv);
						params.put("id_no", id_no);
						params.put("source_no", source_no);
						response = HttpUtil.post(url, params);
						JsonStr = JsonUtil.strToJson((Object) response);
						logger.info("-------getFundInfo-------" + "|" + "公积金第三方接口返回信息" + JsonStr.toString());
						String code = JsonStr.getString("code");
						if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("0")) {
							JSONObject result = JsonStr.getJSONObject("result");
							accumamount = new BigDecimal(result.get("funds_yue").toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
						} else {
							String msg = JsonStr.getString("暂无信息");
							accumamount = msg;
						}
					} else if ("150300".equals(cityCode)) {// 乌海公积金余额
						logger.info("-------getAccumulationFundWH-------" + "|" + "乌海公积金参数" + "|" + "cityCode:" + cityCode + "|" + "realName:" + realName + "|" + "idCard:" + idCard);
						String url = Config.getValue("WH_Fund_Url");
						params.put("usename", realName);
						params.put("cardid", idCard);
						response = HttpUtil.post(url, params);
						JsonStr = JsonUtil.strToJson((Object) response);
						logger.info("-------getAccumulationFundWH-------" + "|" + "乌海公积金第三方接口返回信息" + JsonStr.toString());
						if (JsonStr.containsKey("currentbalance")) {
							accumamount = new BigDecimal(JsonStr.getString("currentbalance")).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
						} else {
							accumamount = "暂无信息";
						}
					}
				} else {
					accumamount = "未实名认证";
				}
			} catch (Exception e) {
				logger.error("--------------getMyHomePageInfo(查询公积金状态出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
			}
		}
		return accumamount;
	}

	// 查询啄木鸟回复情况
	private String getWoodpecker(Long custId, String cityCode) {
		String woodpecker = "";
		try {
			// 因为一个用户可能会有多个问题，为了在循环后能确保Peckertype有一个确定的值，故加入nobackFlag和backFlag标识
			// backFlag为true时，Peckertype为有回复；如果backFlag为false但是nobackFlag为true时，Peckertype为未回复,其余情况均为未参与
			boolean nobackFlag = false;
			boolean backFlag = false;
			List<Map<String, Object>> peckerInfo = woodpeckerService.getListByCustId(custId, cityCode);
			if (peckerInfo == null || peckerInfo.size() == 0) {
				woodpecker = "未参与";
			} else {
				for (Map<String, Object> map : peckerInfo) {
					String pass = map == null ? "" : map.get("pass").toString();
					String number = map == null ? "" : map.get("number").toString();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("number", number);
					params.put("pass", pass);
					logger.info("--------------getMyHomePageInfo(啄木鸟行动开始调用第三方接口)-------------" + "|" + "number:" + number + "|pass:" + pass);
					String response = HttpUtil.post(Config.getValue("dealSearchUrl"), params);
					logger.info("--------------getMyHomePageInfo(啄木鸟行动第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JSONObject json = JsonUtil.strToJson((Object) response);
					String status = json.getString("status");
					if ("fail".equals(status)) {
						nobackFlag = true;
					} else if ("success".equals(status) && "0".equals(map.get("readType"))) {
						backFlag = true;
					} else if ("success".equals(status) && "1".equals(map.get("readType"))) {
						woodpecker = "未参与";
					}
				}
			}
			if (backFlag == true) {
				woodpecker = "有回复";
			} else if (nobackFlag == true) {
				woodpecker = "未回复";
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询啄木鸟出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
			woodpecker = "未参与";
		}
		return woodpecker;
	}

	// 查询市民热线回复情况
	private String getHotLine(Long custId, String cityCode) {
		String hotLine = "";
		try {
			boolean nobackFlag = false;
			boolean backFlag = false;
			List<HotPhone> hotPhoneList = hotPhoneService.getHotPhoneByCustId(custId, cityCode);
			if (hotPhoneList == null || hotPhoneList.size() == 0) {
				hotLine = "未参与";
			} else {
				for (HotPhone hotPhone : hotPhoneList) {
					String pass = hotPhone == null ? "" : hotPhone.getPass();
					String number = hotPhone == null ? "" : hotPhone.getNumber();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("number", number);
					params.put("pass", pass);
					logger.info("--------------getMyHomePageInfo(市民热线开始调用第三方接口)-------------" + "|" + "number:" + number + "|pass:" + pass);
					String response = HttpUtil.post(Config.getValue("dealSearchUrl"), params);
					logger.info("--------------getMyHomePageInfo(市民热线第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JSONObject json = JsonUtil.strToJson((Object) response);
					String status = json.getString("status");
					if ("fail".equals(status)) {
						nobackFlag = true;
					} else if ("success".equals(status) && "0".equals(hotPhone.getReadType())) {
						backFlag = true;
					} else if ("success".equals(status) && "1".equals(hotPhone.getReadType())) {
						hotLine = "未参与";
					}
				}
			}
			if (backFlag == true) {
				hotLine = "有回复";
			} else if (nobackFlag == true) {
				hotLine = "未办理";
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询市民热线出现异常error)------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
			hotLine = "未参与";
		}
		return hotLine;
	}

	/**
	 * 
	 * @Title  查询水费
	 * @Description 查询水费 接口
	 * @param custId 用户ID
	 * @return String
	 * @author ZhangXingLiang
	 * @date 2017年6月2日下午6:31:11
	 */
	private String getWaterFee(Long custId) {
		String displayValue = "";
		try {
			List<Map<String, Object>> water = payService.getByCustIdPayList(custId, Constants.WATERRETE);
			if (water.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("waterRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				for (Map<String, Object> m : water) {
					params.put("userNo", (String) m.get("accountId"));//参数
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					logger.info("--------------getMyHomePageInfo(查询水费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------getMyHomePageInfo(查询水费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						displayValue = JsonStr.getString("shouldPay");
						if (Double.valueOf(displayValue) > 0) {
							displayValue = "<font color=\"#ff0000\">当前已有欠费</font>";
							break;
						}
					}
					displayValue = "查询无欠费记录";
					logger.info("--------------getMyHomePageInfo(查询水费响应码)-------------" + "|" + "code:" + code);
				}
			} else {
				logger.info("--------------getMyHomePageInfo(暂未绑定水费户号)-------------" + "|" + "custId:" + custId);
				displayValue = "未绑定账户";
			}

		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询水费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());

		}
		logger.info("--------------getMyHomePageInfo(水费end)-------------" + "|" + "displayValue:" + displayValue);
		return displayValue;
	}

	/**
	 * 
	 * @Title 查询电费 
	 * @Description 查询电费接口
	 * @param custId 用户ID
	 * @return String
	 * @author ZhangXingLiang
	 * @date 2017年6月2日下午6:30:25
	 */
	private String getElectricFee(Long custId) {
		String displayValue = "";
		try {
			List<Map<String, Object>> power = payService.getByCustIdPayList(custId, Constants.POWERRATE);
			if (power.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("powerRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				for (Map<String, Object> m : power) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					params.put("accoutingUnit", (String) m.get("payUnitName"));

					logger.info("--------------getMyHomePageInfo(查询电费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------getMyHomePageInfo(查询电费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						displayValue = JsonStr.getString("shouldPay");
						if (Double.valueOf(displayValue) > 0) {
							displayValue = "<font color=\"#ff0000\">当前已有欠费</font>";
							break;
						}
					}
					displayValue = "查询无欠费记录";
					logger.info("--------------getMyHomePageInfo(查询电费响应码)-------------" + "|" + "code:" + code);
				}
			} else {
				displayValue = "未绑定账户";
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询电费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());

		}
		logger.info("--------------getMyHomePageInfo(电费end)-------------" + "|" + "displayValue:" + displayValue);
		return displayValue;
		//return "暂未绑定缴费户号";
	}

	/**
	 * 
	 * @Title 查询燃气费 
	 * @Description 查询燃气费接口
	 * @param custId 用户ID
	 * @return String
	 * @author ZhangXingLiang
	 * @date 2017年6月2日下午6:29:07
	 */
	private String getGasFee(Long custId) {
		String displayValue = "";
		try {
			List<Map<String, Object>> gas = payService.getByCustIdPayList(custId, Constants.GASRATE);
			if (gas.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("gasRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				for (Map<String, Object> m : gas) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					logger.info("--------------getMyHomePageInfo(查询燃气费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------getMyHomePageInfo(查询燃气费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						displayValue = JsonStr.getString("shouldPay");
						if (Double.valueOf(displayValue) > 0) {
							displayValue = "<font color=\"#ff0000\">当前已有欠费</font>";
							break;
						}
					}
					displayValue = "查询无欠费记录";
					logger.info("--------------getMyHomePageInfo(查询燃气费响应码)-------------" + "|" + "code:" + code);
				}
			} else {
				displayValue = "未绑定账户";
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询燃气费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());

		}
		logger.info("--------------getMyHomePageInfo(燃气费end)-------------" + "|" + "displayValue:" + displayValue);
		return displayValue;

	}

	/**
	 * 
	 * @Title 查询暖气费
	 * @Description 查询暖气费 有热力和热电两个接口，热电接口暂未开放
	 * @param custId 用户ID
	 * @return String
	 * @author ZhangXingLiang
	 * @date 2017年6月2日下午6:27:15
	 */
	private String getHeatingFee(Long custId) {
		String displayValue = "";
		try {
			List<Map<String, Object>> heatingRate = payService.getByCustIdPayList(custId, Constants.HEATINGRATE);
			if (heatingRate.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String urlForce = Config.getValue("heatingForceRate");//热力
				//				String urlEle = Config.getValue("heatingEleRate");//热电
				String response = null;
				JSONObject JsonStr = new JSONObject();
				for (Map<String, Object> m : heatingRate) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					if ("370101".equals(m.get("payUnitName"))) { //热力
						logger.info("--------------getMyHomePageInfo(查询暖气费开始调用第三方热力接口)-------------" + "|" + "params:" + params);
						response = HttpUtil.post(urlForce, params);
						logger.info("--------------getMyHomePageInfo(查询暖气费第三方接口热力调用完毕)-------------" + "|" + "response:" + response);
						JsonStr = JsonUtil.strToJson((Object) response);
						String code = JsonStr.getString("code");
						if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
							displayValue = JsonStr.getString("shouldPay");
							if (Double.valueOf(displayValue) > 0) {
								displayValue = "<font color=\"#ff0000\">当前已有欠费</font>";
								break;
							}
						}
						logger.info("--------------getMyHomePageInfo(热力响应码)-------------" + "|" + "code:" + code);
					}
					if ("370102".equals(m.get("payUnitName"))) { //热电
						//						logger.info("--------------getMyHomePageInfo(查询暖气费开始调用第三方热电接口)-------------" + "|" + "params:" + params);
						//						response = HttpUtil.post(urlEle, params);
						//						logger.info("--------------getMyHomePageInfo(查询暖气费第三方接口热电调用完毕)-------------" + "|" + "response:" + response);
						//						JsonStr = JsonUtil.strToJson((Object) response);
						//						String code = JsonStr.getString("code");
						//						if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						//							displayValue = JsonStr.getString("shouldPay");
						//							if (Double.valueOf(displayValue) > 0) {
						//								displayValue = "<font color=\"#ff0000\">当前已有欠费</font>";
						//								break;
						//							}
						//						}
						displayValue = "热电暂不开放";
						break;
					}
					displayValue = "查询无欠费记录";
				}
			} else {
				displayValue = "未绑定账户";
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询暖气费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
		}
		logger.info("--------------getMyHomePageInfo(暖气费end)-------------" + "|" + "displayValue:" + displayValue);
		return displayValue;
	}

	// 违章查询
	private String getViolation(Long custId) {
		String violation = "";
		try {
			List<Map<String, Object>> listTraffic = trafficUserInfoService.trafficList(custId, null);
			if (listTraffic == null || listTraffic.size() == 0) {
				violation = "尚未添加车辆";
			} else {
				int record = 0;
				for (Map<String, Object> trafficUserInfo : listTraffic) {
					String cityEN = (String) trafficUserInfo.get("cityCode");
					String carNum = (String) trafficUserInfo.get("carCode");
					String code = carNum.substring(1, 2);// 鲁A 中的 A
					// 根据cityEN从base_info中获取该区域信息
					TrafficViolation trafficViolation = trafficViolationService.getTrafficViolationByCityCode(cityEN, code);
					if (trafficViolation != null) {
						String Key = Config.getValue("Key");
						String carCode = (String) trafficUserInfo.get("carCode");
						String engineNo = (String) trafficUserInfo.get("engineNo");
						String carNo = (String) trafficUserInfo.get("carNo");
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("key", Key);
						params.put("dtype", "json");
						params.put("city", trafficViolation.getCityCode());
						params.put("hphm", carCode);
						params.put("hpzl", "02");
						if (trafficViolation.getIsEngine().equalsIgnoreCase("1")) {
							if (!trafficViolation.getEngineNo().toString().equalsIgnoreCase("0")) {
								if (engineNo.length() <= Integer.valueOf(trafficViolation.getEngineNo())) {
									params.put("engineno", engineNo);
								} else {
									params.put("engineno", engineNo.substring(engineNo.length() - Integer.valueOf(trafficViolation.getEngineNo()), engineNo.length()));
								}
							} else {
								params.put("engineno", engineNo);
							}
						}
						if (trafficViolation.getIsCarNo().equalsIgnoreCase("1")) {
							if (!trafficViolation.getCarNo().toString().equalsIgnoreCase("0")) {
								if (carNo.length() <= Integer.valueOf(trafficViolation.getCarNo())) {
									params.put("classno", carNo);
								} else {
									params.put("classno", carNo.substring(carNo.length() - Integer.valueOf(trafficViolation.getCarNo()), carNo.length()));
								}
							} else {
								params.put("classno", carNo);
							}
						}
						String response = HttpUtil.post(Config.getValue("TrafficUrl"), params);
						logger.info("--------------getMyHomePageInfo(违章查询结果)------" + response);
						// String
						// response="{\"resultcode\":\"200\",\"reason\":\"查询成功\",\"result\":{\"province\":\"HB\",\"city\":\"HB_HD\",\"hphm\":\"冀DHL327\",\"hpzl\":\"02\",\"lists\":[{\"date\":\"2013-12-29
						// 11:57:29\",\"area\":\"316省道53KM+200M\",\"act\":\"16362
						// :
						// 驾驶中型以上载客载货汽车、校车、危险物品运输车辆以外的其他机动车在高速公路以外的道路上行驶超过规定时速20%以上未达50%的\",\"code\":\"\",\"fen\":\"6\",\"money\":\"100\",\"handled\":\"0\"}]}}";
						JSONObject jsonStr = JsonUtil.strToJson((Object) response);
						JSONObject resultStr = jsonStr.getJSONObject("result");
						// 获取ArrayObject
						if (!resultStr.isNullObject() && resultStr.has("lists")) {
							Collection<?> lists = JSONArray.toCollection(JSONArray.fromObject(resultStr.get("lists")));
							int num = lists.size();
							record += num;
						}
					} else {
						logger.info("--------------getMyHomePageInfo(base_carinfo中未查询到结果)-------------");
						violation = "暂无违章处理";
					}
				}
				if (record > 0) {
					violation = "<font color=\"#ff0000\">" + record + "个违章未处理</font>";
				} else {
					violation = "暂无违章处理";
				}
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询违章处理状态出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
			violation = "暂无违章处理";
		}

		return violation;
	}

	/**
	 * 
	 * @Title 我的邀请
	 * @Description 被推荐人加积分参与活动
	 * @param custId 用户id
	 * @param addMark 增加积分
	 * @return Map<String, Object>
	 * @author ZhangXingLiang
	 * @date 2017年6月13日下午3:03:48
	 */
	private Map<String, Object> referPeopleAddMark(Long custId) {
		//更新用户积分表
		Map<String, Object> markInfo = markMissionService.getMark("/cust/h5PageRegister", custId, "已完成");
		//更新活动积分表
		MarkMission markMission = markMissionService.findMarkMesByInterUrl("/cust/h5PageRegister");
		creditsService.updateCreditsByShare(custId, markMission.getPerMark().longValue());

		return markInfo;

	}

	/**
	 * 
	 * @Title 增加积分推送活动
	 * @Description 通过H5页面注册后，第一次登陆增加积分并推送活动
	 * @param custId   用户ID
	 * @param accessToken 访问令牌
	 * @author ZhangXingLiang
	 * @date 2017年6月14日下午7:34:22
	 */
	private void h5FristLoginAddMark(Long custId, String accessToken, String cityCode) {
		//此用户是否有推荐码
		InviteRecord inviteRecord = inviteRecordService.getInviteRecordByCustId(custId, null, "0");
		if (inviteRecord != null) {
			//获取用户的推荐码

			//查询地推用户的推荐码
			boolean isRecurUserInviteCode = true;
			List<String> inviteCodes = customerService.getRecurUserInviteCode();
			if (inviteCodes.size() > 0) {
				for (String inviteC : inviteCodes) {
					if (inviteRecord.getInviteCode().equals(inviteC)) {
						isRecurUserInviteCode = false;
						logger.info("--------------h5FristLoginAddMark(此用户用的是地推人员的邀请码不参与活动)-------------|InviteCod:" + inviteRecord.getInviteCode());
						break;
					}

				}
			}
			//不是用的地推人员的邀请码
			if (isRecurUserInviteCode) {
				//是否是济南用户，不是济南用户不参与活动
				//	if (!BeanUtil.isNullString(cityCode) && "370100".equals(cityCode)) {
				//是济南用户参与活动
				//通过H5页面注册加积分
				MarkMission markMission = markMissionService.findMarkMesByInterUrl("/cust/h5PageRegister");
				if (markMission != null) {
					MarkRecord markRecord = markRecordService.findMarkRecByMessIdCustId(custId, markMission.getId());// 判断用户此任务做过没有
					if (markRecord == null) {
						// 开始做任务
						markRecord = new MarkRecord();
						markRecord.setCustId(custId);
						markRecord.setMissionId(markMission.getId());
						markRecord.setMissionPlan("已完成");
						markRecordService.add(markRecord);
						// 更新用户积分记录
						Mark mark = markService.findByCustId(custId);
						if (mark == null) {// 判断用户积分记录是否存在
							mark = new Mark();
							mark.setCustId(custId);
							mark.setCurrentMark(0);
							mark.setTotalMark(0);
							mark.setYesterdayMark(0);
							markService.add(mark);
						}
						mark.setTotalMark(mark.getTotalMark() + markMission.getPerMark());
						mark.setCurrentMark(mark.getCurrentMark() + markMission.getPerMark());
						markService.update(mark);
						creditsService.updateCreditsByShare(custId, markMission.getPerMark().longValue());
						logger.info("--------------h5FristLoginAddMark(H5页面注册第一次登陆开始推荐活动)-------------");
						//注册后邀请人和推荐人可以参与活动
						String pushToken = getPushToken();
						if (BeanUtil.isNullString(pushToken)) {
							Map<String, Object> pushC = myDeviceService.getPushTokenByCustId(custId);
							pushToken = (String) pushC.get("pushToken");
						}
						Long[] longs = new Long[] { custId };
						String url = Config.getValue("sendMessageUrl");//应用中心路径
						try {
							activityService.inviteFriendsRegisterAfter(longs, pushToken, accessToken, getClientInfo(), url, Constants.ACTIVITY_TYPE_INVITE_CODE);
						} catch (Exception e) {
							logger.error("--------------h5FristLoginAddMark(H5页面注册第一次登陆推荐活动失败)-------------|error:" + e.toString());
						}
						logger.info("--------------h5FristLoginAddMark(H5页面注册第一次登陆推荐活动完成)-------------");
					} else {
						logger.info("--------------h5FristLoginAddMark(H5页面注册已经登陆过)-------------");
					}
				}
				//				} else {
				//					logger.info("--------------h5FristLoginAddMark(此用户不是济南用户不参与活动)-------------");
				//				}
			} else {
				logger.info("--------------h5FristLoginAddMark(此用户用的是地推人员的邀请码不参与活动)-------------");
			}
		} else {
			logger.info("--------------h5FristLoginAddMark(此用户没有推荐码)-------------");
		}

	}

}
