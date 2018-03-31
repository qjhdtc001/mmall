package com.inspur.icity.web.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.inspur.icity.core.dareway.bean.Medical;
import com.inspur.icity.core.dareway.bean.MedicalDetail;
import com.inspur.icity.core.dareway.client.Uddi;
import com.inspur.icity.core.dareway.client.UddiPortType;
import com.inspur.icity.core.dareway.xml.XmlToBean;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.app.model.Application;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.cust.model.Check;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.Mark;
import com.inspur.icity.logic.cust.model.MineInfo;
import com.inspur.icity.logic.cust.model.MineNotification;
import com.inspur.icity.logic.cust.service.CheckService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.MarkService;
import com.inspur.icity.logic.cust.service.MineInfoService;
import com.inspur.icity.logic.cust.service.MineNotificationService;
import com.inspur.icity.logic.cust.service.MyDeviceService;
import com.inspur.icity.logic.life.model.HotPhone;
import com.inspur.icity.logic.life.model.TrafficViolation;
import com.inspur.icity.logic.life.service.HotPhoneService;
import com.inspur.icity.logic.life.service.PayService;
import com.inspur.icity.logic.life.service.TrafficCityService;
import com.inspur.icity.logic.life.service.TrafficUserInfoService;
import com.inspur.icity.logic.life.service.TrafficViolationService;
import com.inspur.icity.logic.life.service.WoodpeckerService;
import com.inspur.icity.logic.operating.model.Credits;
import com.inspur.icity.logic.operating.model.CustActivity;
import com.inspur.icity.logic.operating.model.CustNotification;
import com.inspur.icity.logic.operating.model.Notification;
import com.inspur.icity.logic.operating.service.ActivityService;
import com.inspur.icity.logic.operating.service.CreditsService;
import com.inspur.icity.logic.operating.service.CustActivityService;
import com.inspur.icity.logic.operating.service.CustNotificationService;
import com.inspur.icity.logic.operating.service.NotificationService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;
import com.inspur.icity.web.utils.JPushSchedule;
import com.inspur.icity.web.utils.MD5Encrypt;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 定时任务
 * 
 * @author gaoheng
 *
 */
@Service
public class Task {
	static final String OPENLAUNCH = "launch";//打开爱城市网
	static final String OPENAPP = "app";//打开应用
	static final String WEBAPP = "web";//打开网页
	Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	TrafficCityService trafficCityService;

	@Autowired
	CustomerService customerService;

	@Autowired
	WoodpeckerService woodpeckerService;

	@Autowired
	HotPhoneService hotPhoneService;

	@Autowired
	TrafficUserInfoService trafficUserInfoService;

	@Autowired
	TrafficViolationService trafficViolationService;

	@Autowired
	PayService payService;

	@Autowired
	CheckService checkService;

	@Autowired
	MineInfoService mineInfoService;

	@Autowired
	NotificationService notificationService;

	@Autowired
	CustNotificationService custNotificationService;

	@Autowired
	MineNotificationService mineNotificationService;

	@Autowired
	ApplicationService applicationService;

	@Autowired
	MarkService markService;

	@Autowired
	ActivityService activityService;

	@Autowired
	MyDeviceService myDeviceService;

	@Autowired
	CustActivityService custActivityService;

	@Autowired
	CreditsService creditsService;
	//	@Autowired
	//	AccesstokenService accesstokenService;

	private final static String currentDeployment = Config.getValue("currentDeployment");

	@Scheduled(cron = "0 15 10 15 * ?")
	public void updateCarCode() {
		trafficCityService.importCarCode();// 更新违章信息所属城市信息配置表
	}

	/*
	 * 1 Seconds (0-59) 2 Minutes (0-59) 3 Hours (0-23) 4 Day of month (1-31) 5
	 * Month (1-12 or JAN-DEC) 6 Day of week (1-7 or SUN-SAT) 7 Year (1970-2099)
	 * 取值：可以是单个值，如6； 也可以是个范围，如9-12 也可以是个列表，如9,11,13 也可以是任意取值，使用*
	 */
	// 每天23点整执行
	@Scheduled(cron = "0 0 23 * * ?")
	public void myHomePages() {
		logger.info("--------------myHomePages(start)-------------" + "|" + "fromModule:Task" + "|" + "taskInfo:定时获取我的页面中的各项数据" + "|执行时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		// 查询注册的用户
		List<Map<String, Object>> users = customerService.disUser(0L);
		users.stream().forEach(map -> {
			Long custId = Long.parseLong(map.get("custId").toString());
			String cityCode = map.get("currentCityCode") == null ? "370100" : map.get("currentCityCode").toString();
			logger.info("-------开始获取用户id为：" + custId + "的用户的我的页面相关数据" + "|执行时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			getMyHomePageInfo(custId, cityCode);
			logger.info("用户id为：" + custId + "的用户的我的页面相关数据采集结束" + "|执行时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		});

		// getMyHomePageInfo(659L, "370100");
	}

	/**
	 * 违章查询调用的聚合数据接口 定时任务每天消费次数过多 故将违章查询定时任务剥离出来 单独按每星期执行一次
	 */
	@Scheduled(cron = "0 30 23 ? * MON")
	public void myHomePagesViolation() {
		logger.info("--------------myHomePagesViolation(start)-------------" + "|" + "fromModule:Task" + "|" + "taskInfo:定时获取我的页面中的违章数据" + "|执行时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		// 查询注册的用户
		List<Map<String, Object>> users = customerService.disUser(0L);
		users.stream().forEach(map -> {
			Long custId = Long.parseLong(map.get("custId").toString());
			String cityCode = map.get("currentCityCode") == null ? "370100" : map.get("currentCityCode").toString();
			logger.info("-------开始获取用户id为：" + custId + "的用户的我的页面相关数据" + "|执行时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			getMyHomePageViolationInfo(custId, cityCode);
			logger.info("用户id为：" + custId + "的用户的我的页面相关数据采集结束" + "|执行时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		});

	}

	private void getMyHomePageViolationInfo(Long custId, String cityCode) {
		// 推迟推送时间
		String pushtime = DateUtil.nextNDay(new Date(), 1, "yyyy-MM-dd") + " 09:00:00";
		// 存放给用户欠费、违章、啄木鸟12345热线等通知内容
		StringBuilder builder = new StringBuilder();
		// 存放违章通知内容
		StringBuilder volation = new StringBuilder();

		MineInfo mineInfo = new MineInfo();
		mineInfo.setCustid(custId);

		String phone = customerService.get(custId) == null ? "" : customerService.get(custId).getMobilePhone();
		// 违章处理查询
		try {
			List<Map<String, Object>> listTraffic = trafficUserInfoService.trafficList(custId, null);
			if (listTraffic == null || listTraffic.size() == 0) {
				mineInfo.setVehicleno("尚未添加车辆");
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
						// String name = trafficViolation.getName();
						// String code = trafficViolation.getCode();
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
							JSONArray jarr = JSONArray.fromObject(resultStr.get("lists"));
							Iterator<Object> it = jarr.iterator();
							int count = 0;
							while (it.hasNext()) {
								count++;
								JSONObject json = (JSONObject) it.next();
								Map<String, String> map = json;
								// 先查询在cust_minenotification表中是否有该违章记录
								MineNotification m = new MineNotification();
								m.setCustId(custId);
								m.setNotification("【违章提醒】您登记的车辆收到违章信息提醒，请点击查看详情。文明行车，让我们的城市更加温馨；安全驾驶，让您的家人更加放心。");
								m.setRelatedId(DateUtil.smartFormat(map.get("date")).getTime());
								m.setRelatedType("violation");
								// 此时要将之前处理过的违章记录删除掉，只需按返回的最新的违章记录的第一条违章时间之前的记录删除即可
								if (count == 1) {// 只根据返回的第一条数据的date作为条件
									mineNotificationService.removeOldData(custId, DateUtil.smartFormat(map.get("date")), "volation");
								}
								// 根据违章发生的时间来唯一确定违章
								List<MineNotification> mn_list = mineNotificationService.getInfoByCondition(m);
								if (mn_list == null || mn_list.size() <= 0) {
									// 新增记录
									mineNotificationService.add(m);
								}
							}
						}
					} else {
						logger.info("--------------getMyHomePageViolationInfo(base_carinfo中未查询到结果)-------------");
						mineInfo.setVehicleno("暂无违章处理");
					}
				}
				if (record > 0) {
					mineInfo.setVehicleno("<font color=\"#ff0000\">" + record + "个违章未处理</font>");

					// 记录用户存在的通知
					builder.append("有违章未处理");

					volation.append("【违章提醒】您登记的车辆收到违章信息提醒，请点击查看详情。文明行车，让我们的城市更加温馨；安全驾驶，让您的家人更加放心。");
				} else {
					mineInfo.setVehicleno("暂无违章处理");
				}
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageViolationInfo(查询违章处理状态出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setVehicleno("暂无违章处理");
		}

		try {
			// 如果volation为空说明用户没有添加车辆信息或者没有违章信息，此时要将cust_mineNotification表中的关联记录删除掉
			if (volation.length() == 0) {
				mineNotificationService.removeOldData(custId, null, "volation");
			} else {// 消息为空时不推送
						// 推送通知
					// 根据custId和relatedType查询
				MineNotification m = new MineNotification();
				// 违章查询发送通知
				m = new MineNotification();
				m.setCustId(custId);
				m.setRelatedType("violation");
				List<MineNotification> mineNotification_violation = mineNotificationService.getInfoByCondition(m);
				for (MineNotification mineNotification : mineNotification_violation) {
					String createtime = DateUtil.formatDate(mineNotification.getCreatetime(), DateUtil.Y_M_D);
					String today = DateUtil.formatDate(new Date(), DateUtil.Y_M_D);
					if (createtime.equals(today)) {
						scheduleNotification(phone, volation.toString(), custId, pushtime, OPENLAUNCH, null);
					} else {
						Date pushTime = mineNotification.getUpdatetime();
						// 计算当前时间与表中该用户的数据更新时间之间相差的天数
						int i = DateUtil.daysBetween(pushTime, new Date());
						if (i >= 7) {// 上一次推送通知的时间距离当前时间已经超过7天并且该用户仍然有需要推送的通知
							// 推送通知
							scheduleNotification(phone, volation.toString(), custId, pushtime, OPENLAUNCH, null);
							// 修改推送日期
							mineNotification.setUpdatetime(DateUtil.smartFormat(DateUtil.getNow(DateUtil.Y_M_D_HMS)));
							mineNotificationService.updateByPrimaryKeySelective(mineNotification);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("-----------------getMyHomePageViolationInfo(定时推送消息出现异常error)--------------" + "|error:" + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description:获取我的页面的信息，保存到cust_mineinfo表中
	 * @param custId
	 * @param cityCode
	 *            void
	 * @author: MengKe
	 * @time:2017年2月17日 下午3:13:22
	 *
	 */
	private void getMyHomePageInfo(Long custId, String cityCode) {
		// 推迟推送时间
		String pushtime = DateUtil.nextNDay(new Date(), 1, "yyyy-MM-dd") + " 09:00:00";
		// 存放给用户欠费、违章、啄木鸟12345热线等通知内容
		StringBuilder builder = new StringBuilder();
		// 存放生活缴费的通知内容
		StringBuilder lifePay = new StringBuilder();
		// 存放啄木鸟通知内容
		StringBuilder woodpecker = new StringBuilder();
		// 存放市民热线通知内容
		StringBuilder hotLine = new StringBuilder();

		MineInfo mineInfo = new MineInfo();
		mineInfo.setCustid(custId);

		String phone = customerService.get(custId) == null ? "" : customerService.get(custId).getMobilePhone();

		// 啄木鸟行动
		try {
			// 因为一个用户可能会有多个问题，为了在循环后能确保Peckertype有一个确定的值，故加入nobackFlag和backFlag标识
			// backFlag为true时，Peckertype为有回复；如果backFlag为false但是nobackFlag为true时，Peckertype为未回复,其余情况均为未参与
			boolean nobackFlag = false;
			boolean backFlag = false;
			List<Map<String, Object>> peckerInfo = woodpeckerService.getListByCustId(custId, cityCode);
			if (peckerInfo == null || peckerInfo.size() == 0) {
				mineInfo.setPeckertype("未参与");
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
						// 记录用户存在的通知
						builder.append("啄木鸟有回复");
						if (woodpecker.length() == 0) {
							woodpecker.append("【信息提醒】您在“啄木鸟行动”提交的问题已收到回复信息，请及时查看。");
						}
						// 当啄木鸟有回复的时候，将该问题的id与用户id对应保存到表里作为后面发送通知的条件
						MineNotification notif = new MineNotification();
						notif.setRelatedId(Long.parseLong(number));
						List<MineNotification> list = mineNotificationService.getInfoByCondition(notif);
						if (list == null || list.size() == 0) {
							MineNotification mn = new MineNotification();
							mn.setCustId(custId);
							mn.setNotification(woodpecker.toString());
							mn.setRelatedId(Long.parseLong(number));
							mn.setRelatedType("woodpecker");
							mineNotificationService.add(mn);
						}
					} else if ("success".equals(status) && "1".equals(map.get("readType"))) {
						mineInfo.setPeckertype("未参与");
						// 当发现此啄木鸟问题有回复并且已读，则需要到表里删除此记录，不作为后面发送通知的条件
						MineNotification m = new MineNotification();
						m.setCustId(custId);
						m.setRelatedId(Long.parseLong(number));
						m.setRelatedType("woodpecker");
						mineNotificationService.removeByCondition(m);
					}
				}
			}
			if (backFlag == true) {
				mineInfo.setPeckertype("有回复");
			} else if (nobackFlag == true) {
				mineInfo.setPeckertype("未回复");
			}

		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询啄木鸟出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setPeckertype("");
		}

		// 市民热线
		try {
			boolean nobackFlag = false;
			boolean backFlag = false;
			List<HotPhone> hotPhoneList = hotPhoneService.getHotPhoneByCustId(custId, cityCode);
			if (hotPhoneList == null || hotPhoneList.size() == 0) {
				mineInfo.setHotlinetype("未参与");
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
						// 记录用户存在的通知
						builder.append("市民热线有回复");
						if (hotLine.length() == 0) {
							hotLine.append("【信息提醒】您在“市民热线”提交的问题已收到回复信息，请及时查看。");
						}
						// 当市民热线有回复的时候，将该问题的id与用户id对应保存到表里作为后面发送通知的条件
						MineNotification notif = new MineNotification();
						notif.setRelatedId(Long.parseLong(number));
						List<MineNotification> list = mineNotificationService.getInfoByCondition(notif);
						if (list == null || list.size() == 0) {
							MineNotification mn = new MineNotification();
							mn.setCustId(custId);
							mn.setNotification(hotLine.toString());
							mn.setRelatedId(Long.parseLong(number));
							mn.setRelatedType("hotLine");
							mineNotificationService.add(mn);
						}
					} else if ("success".equals(status) && "1".equals(hotPhone.getReadType())) {
						mineInfo.setPeckertype("未参与");
						// 当发现此市民热线问题有回复并且已读，则需要到表里删除此记录，不作为后面发送通知的条件
						MineNotification m = new MineNotification();
						m.setCustId(custId);
						m.setRelatedId(Long.parseLong(number));
						m.setRelatedType("hotLine");
						mineNotificationService.removeByCondition(m);
					}
				}
			}
			if (backFlag == true) {
				mineInfo.setHotlinetype("有回复");
			} else if (nobackFlag == true) {
				mineInfo.setHotlinetype("未回复");
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询市民热线出现异常error)------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setHotlinetype("");
		}

		// 停车场查询 接口暂未开通
		try {
			mineInfo.setParkno("");
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询停车场信息出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setParkno("");
		}

		String encoding = "utf-8";

		// 水费(后期换查询第三方接口的方式)
		try {
			mineInfo.setWatertype("暂未查到欠费");
			// List<Map<String,Object>> waterList =
			// payService.payList("waterRate", custId, cityCode);
			// if(waterList != null && waterList.size() > 0){
			// int i = 0;
			// for(Map<String, Object> waterMap : waterList){
			// URL url = new
			// URL("http://www.elifepay.com.cn/JSON/queryWater.action?gsh="+waterMap.get("accountId"));
			// HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			// conn.setRequestProperty("Cookie",
			// "JSESSIONID=4YTQYvsMLnCXL0G8gM0j38lPYhp7llLV1tXTzLJ2pTr56vvkT2qC!-1234736833");
			// conn.connect();
			// InputStreamReader isr = new
			// InputStreamReader(conn.getInputStream(), encoding);
			// //建立文件缓存流
			// BufferedReader br = new BufferedReader(isr);
			// String temp = null;
			// StringBuffer buffer = new StringBuffer();
			// while((temp = br.readLine()) != null){
			// buffer.append(temp+"\n");
			// }
			// System.out.println(buffer);
			// JSONObject json = JSONObject.fromObject(buffer.toString());
			// if(json.containsKey("actionErrors")){
			// mineInfo.setWatertype("暂未查到欠费");
			// }else if(json.containsKey("zje")){
			// i++;
			// }
			// }
			// if(i > 0){
			// mineInfo.setWatertype("<font color=\"#fe952c\">当前已有欠费</font>");
			//
			// //记录用户存在的通知
			// builder.append("水费有欠费");
			//
			// if(lifePay.length() == 0){
			// lifePay.append("【水费/电费/燃气费/供暖费预警】您的用水账户/供电账户/燃气账户/供暖账户显示已欠费，为不影响您的日常生活，请记得及时充值缴费哦。");
			// }
			// }else{
			// mineInfo.setWatertype("暂未查到欠费");
			// }
			// }else{
			// mineInfo.setWatertype("暂未绑定缴费户号");
			// }
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询水费状态出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setWatertype("暂未查到欠费");
		}

		// 电费(后期换查询第三方接口的方式)
		try {
			mineInfo.setElectrictype("暂未查到欠费");
			// List<Map<String,Object>> electricList =
			// payService.payList("powerRate", custId, cityCode);
			// if(electricList != null && electricList.size() > 0){
			// int i = 0;
			// for(Map<String, Object> electricMap : electricList){
			// URL url = new
			// URL("http://www.elifepay.com.cn/JSON/queryPower.action?customerNum="+
			// electricMap.get("accountId")+"&accoutingUnit="+electricMap.get("payUnitName"));
			// HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			// conn.setRequestProperty("Cookie",
			// "JSESSIONID=4YTQYvsMLnCXL0G8gM0j38lPYhp7llLV1tXTzLJ2pTr56vvkT2qC!-1234736833");
			// conn.connect();
			// InputStreamReader isr = new
			// InputStreamReader(conn.getInputStream(), encoding);
			// //建立文件缓存流
			// BufferedReader br = new BufferedReader(isr);
			// String temp = null;
			// StringBuffer buffer = new StringBuffer();
			// while((temp = br.readLine()) != null){
			// buffer.append(temp+"\n");
			// }
			// JSONObject json = JSONObject.fromObject(buffer.toString());
			// if(json.containsKey("actionErrors")){
			// mineInfo.setWatertype("暂未查到欠费");
			// }else if(json.containsKey("zje")){
			// i++;
			// }
			// }
			// if(i > 0){
			// mineInfo.setElectrictype("<font
			// color=\"#fe952c\">当前已有欠费</font>");
			//
			// //记录用户存在的通知
			// builder.append("电费有欠费");
			//
			// if(lifePay.length() == 0){
			// lifePay.append("【水费/电费/燃气费/供暖费预警】您的用水账户/供电账户/燃气账户/供暖账户显示已欠费，为不影响您的日常生活，请记得及时充值缴费哦。");
			// }
			// }else{
			// mineInfo.setElectrictype("暂未查到欠费");
			// }
			// }else{
			// mineInfo.setElectrictype("暂未绑定缴费户号");
			// }
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询电费状态出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "e:" + e.toString());
			mineInfo.setElectrictype("暂未查到欠费");
		}

		// 查询燃气费(后期换查询第三方接口的方式)
		try {
			mineInfo.setGastype("暂未查到欠费");
			// List<Map<String,Object>> gasList = payService.payList("gasRate",
			// custId, cityCode);
			// if(gasList != null && gasList.size() > 0){
			// int i = 0;
			// for(Map<String, Object> gasMap : gasList){
			// URL url = new
			// URL("http://www.elifepay.com.cn/JSON/queryGas.action?userNo="+gasMap.get("accountId"));
			// HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			// conn.setRequestProperty("Cookie",
			// "JSESSIONID=4YTQYvsMLnCXL0G8gM0j38lPYhp7llLV1tXTzLJ2pTr56vvkT2qC!-1234736833");
			// conn.connect();
			// InputStreamReader isr = new
			// InputStreamReader(conn.getInputStream(), encoding);
			// //建立文件缓存流
			// BufferedReader br = new BufferedReader(isr);
			// String temp = null;
			// StringBuffer buffer = new StringBuffer();
			// while((temp = br.readLine()) != null){
			// buffer.append(temp+"\n");
			// }
			// JSONObject json = JSONObject.fromObject(buffer.toString());
			// if(json.containsKey("actionErrors")){
			// mineInfo.setWatertype("暂未查到欠费");
			// }else if(json.containsKey("zje")){
			// i++;
			// }
			// }
			// if(i > 0){
			// mineInfo.setGastype("<font color=\"#fe952c\">当前已有欠费</font>");
			// //记录用户存在的通知
			// builder.append("燃气费有欠费");
			//
			// if(lifePay.length() == 0){
			// lifePay.append("【水费/电费/燃气费/供暖费预警】您的用水账户/供电账户/燃气账户/供暖账户显示已欠费，为不影响您的日常生活，请记得及时充值缴费哦。");
			// }
			// }else{
			// mineInfo.setGastype("暂未查到欠费");
			// }
			// }else{
			// mineInfo.setGastype("暂未绑定缴费户号");
			// }
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询燃气费状态出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setGastype("暂未查到欠费");
		}
		// 查询暖气(后期换查询第三方接口的方式)
		try {
			mineInfo.setHeattype("暂未查到欠费");
			// List<Map<String,Object>> heatingList =
			// payService.payList("heatingRate", custId, cityCode);
			// if(heatingList != null && heatingList.size() > 0){
			// int i = 0;
			// for(Map<String, Object> heatingMap : heatingList){
			// URL url = new
			// URL("http://www.elifepay.com.cn/JSON/queryHeating.action?userCard="+heatingMap.get("accountId"));
			// HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			// conn.setRequestProperty("Cookie",
			// "JSESSIONID=4YTQYvsMLnCXL0G8gM0j38lPYhp7llLV1tXTzLJ2pTr56vvkT2qC!-1234736833");
			// conn.connect();
			// InputStreamReader isr = new
			// InputStreamReader(conn.getInputStream(), encoding);
			// //建立文件缓存流
			// BufferedReader br = new BufferedReader(isr);
			// String temp = null;
			// StringBuffer buffer = new StringBuffer();
			// while((temp = br.readLine()) != null){
			// buffer.append(temp+"\n");
			// }
			// JSONObject json = JSONObject.fromObject(buffer.toString());
			// if(json.containsKey("actionErrors")){
			// mineInfo.setWatertype("暂未查到欠费");
			// }else if(json.containsKey("zje")){
			// i++;
			// }
			// }
			// if(i > 0){
			// mineInfo.setHeattype("<font color=\"#fe952c\">当前已有欠费</font>");
			//
			// //记录用户存在的通知
			// builder.append("燃气费有欠费");
			//
			// if(lifePay.length() == 0){
			// lifePay.append("【水费/电费/燃气费/供暖费预警】您的用水账户/供电账户/燃气账户/供暖账户显示已欠费，为不影响您的日常生活，请记得及时充值缴费哦。");
			// }
			// }else{
			// mineInfo.setHeattype("暂未查到欠费");
			// }
			// }else{
			// mineInfo.setHeattype("暂未绑定缴费户号");
			// }
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询暖气费状态出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setHeattype("暂未查到欠费");
		}

		// 社保查询 接口暂未开通
		try {
			mineInfo.setSocialcardamount("");
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(社保查询出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());
			mineInfo.setSocialcardamount("");
		}

		// 公积金查询
		// try {
		// Check check = checkService.getCheckById(custId);
		// String isRealName = customerService.get(custId).getIsRealName();
		// if(!BeanUtil.isNullString(isRealName)&&isRealName.equalsIgnoreCase("1")){
		// HashMap<String, String> params = new HashMap<String, String>();
		// String idCard = check.getIdCard();
		// logger.info("-------getFundInfo-------"+"|"+"custId:"+custId+"---------"+"custCard:"+idCard);
		// String response =null;
		// JSONObject JsonStr = new JSONObject();
		// String key = Config.getValue("Fund_Key");
		// String iv = Config.getValue("Fund_Iv");
		// String url = Config.getValue("Fund_Url");
		// String source_no = Config.getValue("Source_No");
		// String id_no = AESUtil.AESEncrypt(idCard,key,iv);
		// params.put("id_no", id_no);
		// params.put("source_no", source_no);
		// response = HttpUtil.post(url,params);
		// JsonStr = JsonUtil.strToJson((Object)response);
		// logger.info("-------getFundInfo-------"+"|"+"公积金第三方接口返回信息"+JsonStr.toString());
		// String code = JsonStr.getString("code");
		// if(!BeanUtil.isNullString(code)&&code.equalsIgnoreCase("0")){
		// JSONObject result = JsonStr.getJSONObject("result");
		// mineInfo.setAccumamount(result.get("funds_yue").toString()+"元");
		// }else{
		// String msg = JsonStr.getString("msg");
		// mineInfo.setAccumamount(msg);
		// }
		// }else{
		// mineInfo.setAccumamount("未实名认证");
		// }
		// } catch (Exception e) {
		// logger.error("--------------getMyHomePageInfo(查询公积金状态出现异常error)-------------"+
		// "|"+"fromModule:Task"+"|"+"error:"+e.toString());
		// mineInfo.setAccumamount("");
		// }

		try {
			// 保存到cust_mineinfo表
			// 先查询是否存在该用户的数据，如果存在则覆盖
			MineInfo mInfo = mineInfoService.getMineInfoByCustId(custId);
			if (mInfo != null) {
				mineInfoService.removeMineInfoByCustId(custId);
				mineInfoService.add(mineInfo);
			} else {
				mineInfoService.add(mineInfo);
			}

			// 推送通知
			// 根据custId和relatedType查询
			MineNotification m = new MineNotification();
			// 啄木鸟发送通知
			m.setCustId(custId);
			m.setRelatedType("woodpecker");
			List<MineNotification> mineNotification_woodpecker = mineNotificationService.getInfoByCondition(m);
			if (woodpecker.length() == 0 && mineNotification_woodpecker != null) {
				mineNotificationService.removeByCondition(m);
				mineNotification_woodpecker = mineNotificationService.getInfoByCondition(m);
			} else if (woodpecker.length() > 0) {
				int notifCount = 0;
				for (MineNotification mineNotification : mineNotification_woodpecker) {
					String createtime = DateUtil.formatDate(mineNotification.getCreatetime(), DateUtil.Y_M_D);
					String today = DateUtil.formatDate(new Date(), DateUtil.Y_M_D);
					if (createtime.equals(today)) {
						notifCount++;
						if (notifCount == 1) {
							scheduleNotification(phone, woodpecker.toString(), custId, pushtime, OPENLAUNCH, null);
						}
					} else {
						Date pushTime = mineNotification.getUpdatetime();
						// 计算当前时间与表中该用户的数据更新时间之间相差的天数
						int i = DateUtil.daysBetween(pushTime, new Date());
						if (i > 7) {// 上一次推送通知的时间距离当前时间已经超过7天并且该用户仍然有需要推送的通知
							// 推送通知
							scheduleNotification(phone, woodpecker.toString(), custId, pushtime, OPENLAUNCH, null);
							// 修改推送日期
							mineNotification.setUpdatetime(DateUtil.smartFormat(DateUtil.getNow(DateUtil.Y_M_D_HMS)));
							mineNotificationService.updateByPrimaryKeySelective(mineNotification);
						}
					}
				}
			}
			// 市民热线发送通知
			// 根据custId和relatedType查询
			m = new MineNotification();
			m.setCustId(custId);
			m.setRelatedType("hotLine");
			List<MineNotification> mineNotification_hotLine = mineNotificationService.getInfoByCondition(m);
			if (hotLine.length() == 0 && mineNotification_hotLine != null) {
				mineNotificationService.removeByCondition(m);
				mineNotification_hotLine = mineNotificationService.getInfoByCondition(m);
			} else if (hotLine.length() > 0) {
				int lineCount = 0;
				for (MineNotification mineNotification : mineNotification_hotLine) {
					String createtime = DateUtil.formatDate(mineNotification.getCreatetime(), DateUtil.Y_M_D);
					String today = DateUtil.formatDate(new Date(), DateUtil.Y_M_D);
					if (createtime.equals(today)) {
						lineCount++;
						if (lineCount == 1) {
							scheduleNotification(phone, hotLine.toString(), custId, pushtime, OPENLAUNCH, null);
						}
					} else {
						Date pushTime = mineNotification.getUpdatetime();
						// 计算当前时间与表中该用户的数据更新时间之间相差的天数
						int i = DateUtil.daysBetween(pushTime, new Date());
						if (i > 7) {// 上一次推送通知的时间距离当前时间已经超过7天并且该用户仍然有需要推送的通知
							// 推送通知
							scheduleNotification(phone, hotLine.toString(), custId, pushtime, OPENLAUNCH, null);
							// 修改推送日期
							mineNotification.setUpdatetime(DateUtil.smartFormat(DateUtil.getNow(DateUtil.Y_M_D_HMS)));
							mineNotificationService.updateByPrimaryKeySelective(mineNotification);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("-----------------getMyHomePageInfo(定时推送消息出现异常error)--------------" + "|error:" + e.toString());
			e.printStackTrace();
		}
		// scheduleNotification(phone, "来啊", custId);
	}

	/**
	 * 
	 * @Description:极光定时推送消息
	 * @param phone
	 * @param content  通知内容
	 * @param pushtime 推迟发送时间
	 * @param open 打开的应用
	 * @param relatedId 打开应用ID
	 * @param custId
	 *            void
	 * @author: MengKe
	 * @time:2017年2月20日 下午8:26:00
	 *
	 */
	private void scheduleNotification(String phone, String content, Long custId, String pushtime, String open, Long relatedId) {
		logger.info("----Task|getMyHomePageInfo|scheduleNotification(start)---" + "phone:" + phone + "|content:" + content + "|custId:" + custId + "|pushtime:" + pushtime);
		logger.info("----Task|getMyHomePageInfo|scheduleNotification(pushtime)---" + pushtime);
		// 封装trigger参数
		JSONObject json_time = new JSONObject();
		json_time.put("time", pushtime);
		JSONObject json_single = new JSONObject();
		json_single.put("single", json_time);
		String trigger = json_single.toString();
		// 封装push参数
		JSONObject json = new JSONObject();
		// platform
		JSONArray jarr = new JSONArray();
		jarr.add("android");
		jarr.add("ios");
		json.accumulate("platform", jarr);
		// audience
		JSONObject json_alias = new JSONObject();
		JSONArray jarr_phone = new JSONArray();
		jarr_phone.add(phone);
		json_alias.put("alias", jarr_phone);
		json.accumulate("audience", json_alias);
		// notification
		JSONObject json_notification = new JSONObject();
		// android
		JSONObject json_android_extras = new JSONObject();
		String resultStr = "";

		if ("launch".equals(open)) {
			resultStr = "{\"type\":\"pers\",\"open\":\"launch\",\"value\":{},\"pushtime\":\"" + pushtime + "\"}";
		} else if ("app".equals(open)) {
			Application application = applicationService.get(relatedId);
			if (application != null) {
				resultStr = "{\"type\":\"pers\",\"open\":\"app\",\"value\":{\"title\":\"" + application.getName() + "\",\"url\":\"" + application.getGotoUrl() + "\",\"code\":\"" + application.getCode() + "\",\"isShare\":\"" + application.getIsShare() + "\",\"description\":\"" + application.getDescription() + "\"},\"pushtime\":\"" + pushtime + "\"}";
			} else {
				logger.info("----Task|getMyHomePageInfo|scheduleNotification(找不到指定的应用)---|application:" + application);
			}

		}

		json_android_extras.put("result", JsonUtil.toJson(resultStr));
		JSONObject json_android = new JSONObject();
		json_android.accumulate("alert", content);
		json_android.accumulate("extras", json_android_extras);
		json_notification.accumulate("android", json_android);
		// ios
		JSONObject json_ios_extras = new JSONObject();
		json_ios_extras.put("result", JsonUtil.toJson(resultStr));
		JSONObject json_ios = new JSONObject();
		json_ios.accumulate("alert", content);
		json_ios.accumulate("sound", "default");
		json_ios.accumulate("badge", "0");
		json_ios.accumulate("content-available", true);
		json_ios.accumulate("extras", json_ios_extras);
		json_notification.accumulate("ios", json_ios);
		json.accumulate("notification", json_notification);
		// options
		JSONObject json_apns_production = new JSONObject();
		if (currentDeployment.equals("test")) {
			json_apns_production.put("apns_production", false);
		} else if (currentDeployment.equals("official")) {
			json_apns_production.put("apns_production", true);
		}
		json.accumulate("options", json_apns_production);
		String push = json.toString();

		String name = "schedule_name";
		String enabled = "true";

		String result = JPushSchedule.pushBySchedule(name, enabled, trigger, push);
		logger.info("----Task|getMyHomePageInfo|scheduleNotification(start)---" + "推送结果：" + result);
		if (!"".equals(result)) {
			// 通知发送成功，保存数据库
			JSONObject obj = JSONObject.fromObject(result);
			Notification notification = new Notification();
			notification.setMsg_id(obj.getString("schedule_id"));
			notification.setContent(content);
			notification.setType("sys");
			if ("launch".equals(open)) {
				notification.setOpen(open);//启动爱城市网
			} else {
				notification.setOpen(open);
				notification.setRelatedId(relatedId);
			}

			notification.setIsImmediately(0L);
			notification.setPushType("person");
			notification.setPushTime(DateUtil.smartFormat(pushtime));
			notification.setCreateTime(DateUtil.smartFormat(pushtime));
			notification.setUpdateTime(DateUtil.smartFormat(pushtime));
			notificationService.add(notification);
			// 存储通知用户关联关系
			Notification notification1 = new Notification();
			notification1 = notificationService.getByMsgId(notification.getMsg_id());
			Long notif_id = notification1.getId();

			CustNotification custNotification = new CustNotification();
			custNotification.setMsgId(notif_id);
			custNotification.setDeviceId(null);
			custNotification.setCustId(custId);
			custNotification.setIsRead(0L);
			custNotification.setPushTime(DateUtil.smartFormat(pushtime));
			custNotificationService.add(custNotification);
		}
	}

	/**
	 * 
	 * @Title scheduleInviteNotification
	 * @Description 邀请好友，定时给老用户推荐活动
	 * @param phone 手机号
	 * @param content 活动内容
	 * @param custId 用户ID
	 * @param pushtime 推送时间
	 * @param open 打开方式
	 * @param cityCode 城市代码
	 * @throws Exception void
	 * @author ZhangXingLiang
	 * @date 2017年6月17日下午4:16:56
	 */
	private void scheduleInviteNotification(String phone, String content, Long custId, String pushtime, String open, String cityCode) throws Exception {
		logger.info("----Task|scheduleInviteNotification(start)---" + "phone:" + phone + "|content:" + content + "|custId:" + custId + "|pushtime:" + pushtime);
		logger.info("----Task|scheduleInviteNotification(pushtime)---" + pushtime);
		String date = DateUtil.getNow("yyyy-MM-dd HH:mm:ss");
		List<Map<String, Object>> activityLists = activityService.findActivityByCondition(cityCode, Constants.ACTIVITY_TYPE_INVITE_CODE, date);
		if (activityLists.size() == 0) {
			logger.error("scheduleInviteNotification(对应的活动不存在)---------------");
			return;
		}
		Map<String, Object> m = activityLists.get(0);
		// 封装trigger参数
		JSONObject json_time = new JSONObject();
		json_time.put("time", pushtime);
		JSONObject json_single = new JSONObject();
		json_single.put("single", json_time);
		String trigger = json_single.toString();
		// 封装push参数
		JSONObject json = new JSONObject();
		// platform
		JSONArray jarr = new JSONArray();
		jarr.add("android");
		jarr.add("ios");
		json.accumulate("platform", jarr);
		// audience
		JSONObject json_alias = new JSONObject();
		JSONArray jarr_phone = new JSONArray();
		jarr_phone.add(phone);
		json_alias.put("alias", jarr_phone);
		json.accumulate("audience", json_alias);
		// notification
		JSONObject json_notification = new JSONObject();
		// android
		JSONObject json_android_extras = new JSONObject();
		String resultStr = "";
		logger.info("---------------scheduleInviteNotification(邀请好友参与活动)----------------");
		CustActivity custActivity = custActivityService.getCustActivity(custId, Long.valueOf(m.get("id").toString()));
		if (custActivity == null) {
			custActivity = new CustActivity();
			custActivity.setActivityId(Long.valueOf(m.get("id").toString()));
			custActivity.setCustId(custId);
			custActivity.setActualPrice(0l);
			custActivityService.add(custActivity);
			logger.info("---------------scheduleInviteNotification(添加custActivity信息完成)--------------");
		}
		Credits credits = creditsService.getCustCredits(custId);
		if (credits == null) {
			logger.error("--------------scheduleInviteNotification(cust_credits表中未查询到该用户的信息)---------|custId:" + custId);
		}
		if ("web".equals(open)) {
			resultStr = "{\"type\":\"pers\",\"open\":\"web\",\"value\":{\"title\":\"" + m.get("name") + "\",\"url\":\"" + m.get("gotoUrl") + "\",\"code\":\"" + cityCode + "\",\"isShare\":\"" + m.get("isShare") + "\",\"description\":\"" + m.get("description") + "\"},\"pushtime\":\"" + pushtime + "\"}";
			logger.info("----scheduleInviteNotification---|resultStr:" + resultStr);
		} else {
			logger.info("----Task|scheduleInviteNotification(找不到指定活动)---|activityLists:" + activityLists);
		}

		json_android_extras.put("result", JsonUtil.toJson(resultStr));
		JSONObject json_android = new JSONObject();
		json_android.accumulate("alert", content);
		json_android.accumulate("extras", json_android_extras);
		json_notification.accumulate("android", json_android);
		// ios
		JSONObject json_ios_extras = new JSONObject();
		json_ios_extras.put("result", JsonUtil.toJson(resultStr));
		JSONObject json_ios = new JSONObject();
		json_ios.accumulate("alert", content);
		json_ios.accumulate("sound", "default");
		json_ios.accumulate("badge", "0");
		json_ios.accumulate("content-available", true);
		json_ios.accumulate("extras", json_ios_extras);
		json_notification.accumulate("ios", json_ios);
		json.accumulate("notification", json_notification);
		// options
		JSONObject json_apns_production = new JSONObject();
		if (currentDeployment.equals("test")) {
			json_apns_production.put("apns_production", false);
		} else if (currentDeployment.equals("official")) {
			json_apns_production.put("apns_production", true);
		}
		json.accumulate("options", json_apns_production);
		String push = json.toString();

		String name = "schedule_name";
		String enabled = "true";

		String result = JPushSchedule.pushBySchedule(name, enabled, trigger, push);
		logger.info("----Task|scheduleInviteNotification(end)---" + "推送结果：" + result);
		if (!"".equals(result)) {
			// 通知发送成功，保存数据库
			JSONObject obj = JSONObject.fromObject(result);
			Notification notification = new Notification();
			notification.setMsg_id(obj.getString("schedule_id"));
			notification.setContent(content);
			notification.setType("ops");
			notification.setRelatedId(Long.valueOf(m.get("id").toString()));
			;
			if ("web".equals(open)) {
				notification.setOpen("operation");//启动爱城市网
			}

			notification.setIsImmediately(0L);
			notification.setPushType("registration_id");
			notification.setPushTime(DateUtil.smartFormat(pushtime));
			notification.setCreateTime(DateUtil.smartFormat(pushtime));
			notification.setUpdateTime(DateUtil.smartFormat(pushtime));
			notificationService.add(notification);
			// 存储通知用户关联关系
			Notification notification1 = new Notification();
			notification1 = notificationService.getByMsgId(notification.getMsg_id());
			Long notif_id = notification1.getId();

			CustNotification custNotification = new CustNotification();
			custNotification.setMsgId(notif_id);
			custNotification.setDeviceId(null);
			custNotification.setCustId(custId);
			custNotification.setIsRead(0L);
			custNotification.setPushTime(DateUtil.smartFormat(pushtime));
			custNotificationService.add(custNotification);
		}
	}

	/**
	 * 
	 * @Title 邀请好友注册推荐活动
	 * @Description 每天早上9点，积分大于500 ，开始推送活动
	 * @author ZhangXingLiang
	 * @date 2017年6月15日下午2:51:28
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	//	@Scheduled(cron = "0 30 17 * * ?")
	public void pushReferActivities() {

		List<Mark> currentMark = markService.getReferPeopleMark();
		if (currentMark.size() > 0) {
			// 推迟推送时间
			String pushtime = DateUtil.nextNDay(new Date(), 0, "yyyy-MM-dd") + " 09:05:00";
			//String pushtime = DateUtil.nextNDay(new Date(), 0, "yyyy-MM-dd") + " 17:32:00";
			currentMark.stream().forEach(m -> {
				if (m.getCurrentMark() >= 500) {
					Customer customer = customerService.get(m.getCustId());
					if (customer != null) {
						try {
							scheduleInviteNotification(customer.getMobilePhone(), "恭喜您获得一次兑换奖励的机会，快来领取吧", customer.getId(), pushtime, WEBAPP, customer.getCurrentCityCode());
						} catch (Exception e) {
							logger.error("----Task|pushReferActivities(error)---" + "error：" + e.toString());
						}
						//scheduleNotification(customer.getMobilePhone(), content, customer.getId(), pushtime, OPENLAUNCH, null);
					}
				}

			});
		}
	}

	/**
	 * 
	 * @Title 推送医疗账户信息
	 * @Description 定时推送医疗信息给用户
	 * @author ZhangXingLiang
	 * @date 2017年5月25日下午2:30:05
	 */

	//	@Scheduled(cron = "0 55 17 * * ?")
	@Scheduled(cron = "0 0 1 2 * ?")
	public void pushSocialMedical() {
		List<Check> userList = checkService.getRealNameUser();
		if (userList != null) {
			userList.stream().forEach(u -> {
				Long custId = u.getCustId();
				String idCard = u.getIdCard();
				String phone = u.getCheckPhone();
				getSocialMedical(custId, idCard, phone);
			});
		}
	}

	/**
	 * @Title 医保信息推送
	 * @Description 每月2号凌晨2点开始推送
	 * @param custId 用户ID      
	 * @param idCard 用户身份证号      
	 * @param phone 用户手机号          
	 * @author ZhangXingLiang
	 * @date 2017年5月25日下午3:09:39
	 */
	private void getSocialMedical(Long custId, String idCard, String phone) {
		logger.info("--------|getSocialMedical|(start)--------");
		logger.info("-----getSocialMedical(传递参数)----|custId:" + custId + "|idCard:" + idCard + "|phone:" + phone);
		// 推迟推送时间
		String pushtime = DateUtil.nextNDay(new Date(), 0, "yyyy-MM-dd") + " 09:00:00";
		//		String pushtime = DateUtil.nextNDay(new Date(), 0, "yyyy-MM-dd") + " 18:05:00";
		// 请求参数
		Calendar cal = Calendar.getInstance();
		// 获取当前年
		int endYear = cal.get(Calendar.YEAR);
		// 获取当前月
		int endMonth = cal.get(Calendar.MONTH) + 1;
		// 获取上一个月
		// 取得系统当前时间所在月第一天时间对象
		cal.set(Calendar.DAY_OF_MONTH, 1);
		// 日期减一,取得上月最后一天时间对象
		cal.add(Calendar.DAY_OF_MONTH, -1);
		// 获取上个月
		int startMonth = cal.get(Calendar.MONTH) + 1;
		// 获取上一年
		int startYear = cal.get(Calendar.YEAR);
		String lastMonth = (startMonth < 10 ? "0" + String.valueOf(startMonth) : String.valueOf(startMonth));
		String startDate = String.valueOf(startYear) + lastMonth;
		String endDate = String.valueOf(endYear) + (endMonth < 10 ? "0" + String.valueOf(endMonth) : String.valueOf(endMonth));

		Uddi uddi = new Uddi();
		UddiPortType uddiPortType = uddi.getUddiHttpSoap11Endpoint();
		// List<Map<String, Object>> list = new ArrayList<Map<String,
		// Object>>();
		try {
			idCard = idCard.toUpperCase();

			// 调用养老个人账户查询接口
			// 请求参数
			String xmlPara = "<?xml version=\"1.0\" encoding=\"GBK\"?><p><s sfzhm=\"" + idCard + "\" /><s qsny=\"" + startDate + "\" /><s zzny=\"" + startDate + "\" /></p>";
			logger.info("--------|getSocialMedical|(请求参数)--------|xmlPara:" + xmlPara);
			String result = uddiPortType.invokeService("SiService", "getCardGrService", xmlPara);
			// logger.info("-----getSocialMedical----医疗个人账户明细接口返回数据：" + result);
			Medical medical = XmlToBean.xmlToMedical(result);
			logger.info("--------|getSocialMedical|(response医疗信息)--------|medical:" + medical);
			//个人医保信息发送通知
			BigDecimal income = null;
			BigDecimal expend = null;
			if (medical != null) {
				income = BigDecimal.valueOf(0.0);
				expend = BigDecimal.valueOf(0.0);
				List<MedicalDetail> medicalDetail = medical.getMedicalDetail();
				System.out.println(medicalDetail);
				for (int i = 0; i < medicalDetail.size(); i++) {
					//System.out.println(medicalDetail.get(i).getJe());
					if (medicalDetail.get(i).getJe().compareTo(BigDecimal.ZERO) == 1 || medicalDetail.get(i).getJe().compareTo(BigDecimal.ZERO) == 0) {
						income = income.add(medicalDetail.get(i).getJe());
					}
					if (medicalDetail.get(i).getJe().compareTo(BigDecimal.ZERO) == -1) {
						expend = expend.add(medicalDetail.get(i).getJe());
					}
				}
				Long medicareId = Long.valueOf(Config.getValue("medicareId"));
				String content = "【医保账单】亲，您" + startYear + "年" + lastMonth + "月的账单新鲜出炉啦！医保月收入" + income + "元，支出" + expend.abs() + "元。医保实时查询，尽在“爱城市网”APP。快来查看账户详情吧";
				scheduleNotification(phone, content, custId, pushtime, OPENAPP, medicareId);
			}

			logger.info("--------|getSocialMedical|(end)--------");
		} catch (Exception e) {
			logger.info("-----getSocialMedical(error)----接口异常error：" + e.toString());
		}
	}

	/**
	 * 
	 * @Title 定时推送水电煤暖
	 * @Description TODO void
	 * @author ZhangXingLiang
	 * @date 2017年6月22日上午10:03:45
	 */
	@Scheduled(cron = "0 0 1 1 * ?")
	public void pushWaterPowerGasHeating() {
		List<Check> userList = checkService.getRealNameUser();
		if (userList != null) {
			String pushtime = DateUtil.nextNDay(new Date(), 0, "yyyy-MM-dd") + " 08:30:00";
			userList.stream().forEach(u -> {
				Long custId = u.getCustId();
				//Long custId = 2121l;
				String phone = u.getCheckPhone();
				//String phone = "18239775215";
				dueWaterFee(custId, phone, pushtime);
				dueElectricFee(custId, phone, pushtime);
				dueGasFee(custId, phone, pushtime);
				dueHeatingFee(custId, phone, pushtime);
			});
		}
	}

	/** 
	 * @Title 水费欠费通知
	 * @Description 水费欠费通知极光推送
	 * @param custId 用户id
	 * @param phone 手机号
	 * @param pushtime  推送时间
	 * @author ZhangXingLiang
	 * @date 2017年6月20日下午3:18:58
	 */
	private void dueWaterFee(Long custId, String phone, String pushtime) {
		logger.info("-----dueWaterFee(传递参数)----|custId:" + custId + "|phone:" + phone + "|pushtime:" + pushtime);
		Double due = 0.0;
		try {
			List<Map<String, Object>> water = payService.getByCustIdPayList(custId, Constants.WATERRETE);
			if (water.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("waterRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				boolean flag = false;
				String userName = "";
				for (Map<String, Object> m : water) {
					params.put("userNo", (String) m.get("accountId"));//参数
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					logger.info("--------------dueWaterFee(查询水费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------dueWaterFee(查询水费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						String displayValue = JsonStr.getString("shouldPay");
						userName = JsonStr.getString("userName");
						due += Double.valueOf(displayValue);
						if (due > 0) {
							flag = true;
						}
					}
					logger.info("--------------dueWaterFee(查询水费响应码)-------------" + "|" + "code:" + code);
				}
				if (flag) {
					//极光推送
					Long waterId = Long.valueOf(Config.getValue("waterId"));
					String ymd = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
					String content = "【账单】" + userName + "先生/女士，您家的水费该缴费啦！截止到" + ymd + "，您本次水费欠费:" + due + "元，缴费请戳：";

					scheduleNotification(phone, content, custId, pushtime, OPENAPP, waterId);
				}
			}

		} catch (Exception e) {
			logger.error("--------------dueWaterFee(查询水费出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());

		}

	}

	/**
	 * 
	 * @Title 电费欠费通知
	 * @Description 电费欠费通知极光推送
	 * @param custId 用户ID
	 * @param phone 手机号
	 * @param pushtime 推送时间
	 * @author ZhangXingLiang
	 * @date 2017年6月20日下午2:18:58
	 */
	private void dueElectricFee(Long custId, String phone, String pushtime) {
		String displayValue = "";
		Double due = 0.0;
		try {
			List<Map<String, Object>> power = payService.getByCustIdPayList(custId, Constants.POWERRATE);
			if (power.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("powerRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				boolean flag = false;
				String userName = "";
				for (Map<String, Object> m : power) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					params.put("accoutingUnit", (String) m.get("payUnitName"));

					logger.info("--------------dueElectricFee(查询电费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------dueElectricFee(查询电费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						displayValue = JsonStr.getString("shouldPay");
						userName = JsonStr.getString("userName");
						due += Double.valueOf(displayValue);
						if (due > 0) {
							flag = true;
						}
					}
					logger.info("--------------dueElectricFee(查询电费响应码)-------------" + "|" + "code:" + code);
				}

				if (flag) {
					//极光推送
					Long waterId = Long.valueOf(Config.getValue("powerId"));
					String ymd = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
					String content = "【账单】" + userName + "先生/女士，您家的电费该缴费啦！截止到" + ymd + "，您本次电费欠费:" + due + "元，缴费请戳：";
					scheduleNotification(phone, content, custId, pushtime, OPENAPP, waterId);
				}
			}
		} catch (Exception e) {
			logger.error("--------------dueElectricFee(查询电费出现异常error)-------------" + "|" + "fromModule:Task" + "|" + "error:" + e.toString());

		}

	}

	/**
	 * 
	 * @Title 燃气欠费通知
	 * @Description 燃气欠费极光推送
	 * @param custId 用户ID
	 * @param phone 手机号
	 * @param pushtime 推送时间
	 * @author ZhangXingLiang
	 * @date 2017年6月20日下午3:17:06
	 */
	private void dueGasFee(Long custId, String phone, String pushtime) {
		String displayValue = "";
		Double due = 0.0;
		try {
			List<Map<String, Object>> gas = payService.getByCustIdPayList(custId, Constants.GASRATE);
			if (gas.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String url = Config.getValue("gasRate");
				String response = null;
				JSONObject JsonStr = new JSONObject();
				boolean flag = false;
				String userName = "";
				for (Map<String, Object> m : gas) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					logger.info("--------------dueGasFee(查询燃气费开始调用第三方接口)-------------" + "|" + "params:" + params);
					response = HttpUtil.post(url, params);
					logger.info("--------------dueGasFee(查询燃气费第三方接口调用完毕)-------------" + "|" + "response:" + response);
					JsonStr = JsonUtil.strToJson((Object) response);
					String code = JsonStr.getString("code");
					if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						displayValue = JsonStr.getString("shouldPay");
						userName = JsonStr.getString("userName");
						due += Double.valueOf(displayValue);
						if (due > 0) {
							flag = true;
						}
					}

					logger.info("--------------dueGasFee(查询燃气费响应码)-------------" + "|" + "code:" + code);
				}
				if (flag) {
					//极光推送
					Long waterId = Long.valueOf(Config.getValue("gasId"));
					String ymd = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
					String content = "【账单】" + userName + "先生/女士，您家的燃气费该缴费啦！截止到" + ymd + "，您本次燃气费欠费:" + due + "元，缴费请戳：";
					scheduleNotification(phone, content, custId, pushtime, OPENAPP, waterId);
				}
			}
		} catch (Exception e) {
			logger.error("--------------dueGasFee(查询燃气费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());

		}

	}

	/**
	 * 
	 * @Title 暖气费欠费通知
	 * @Description  暖气费欠费通知极光推送
	 * @param custId 用户ID
	 * @param phone 手机号
	 * @param pushtime 推迟时间
	 * @author ZhangXingLiang
	 * @date 2017年6月20日下午3:31:41
	 */
	private void dueHeatingFee(Long custId, String phone, String pushtime) {
		String displayValue = "";
		Double due = 0.0;
		try {
			List<Map<String, Object>> heatingRate = payService.getByCustIdPayList(custId, Constants.HEATINGRATE);
			if (heatingRate.size() > 0) {
				HashMap<String, String> params = new HashMap<String, String>();
				String urlForce = Config.getValue("heatingForceRate");//热力
				//				String urlEle = Config.getValue("heatingEleRate");//热电
				String response = null;
				JSONObject JsonStr = new JSONObject();
				boolean flag = false;
				String userName = "";
				for (Map<String, Object> m : heatingRate) {
					params.put("userNo", (String) m.get("accountId"));
					params.put("verifyString", MD5Encrypt.MD5(Constants.CHANNEL + (String) m.get("accountId") + Constants.WPGH_KEY));
					if ("370101".equals(m.get("payUnitName"))) { //热力
						logger.info("--------------dueHeatingFee(查询暖气费开始调用第三方热力接口)-------------" + "|" + "params:" + params);
						response = HttpUtil.post(urlForce, params);
						logger.info("--------------dueHeatingFee(查询暖气费第三方接口热力调用完毕)-------------" + "|" + "response:" + response);
						JsonStr = JsonUtil.strToJson((Object) response);
						String code = JsonStr.getString("code");
						if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
							displayValue = JsonStr.getString("shouldPay");
							userName = JsonStr.getString("userName");
							due += Double.valueOf(displayValue);
							if (due > 0) {
								flag = true;
							}
						}
						logger.info("--------------dueHeatingFee(热力响应码)-------------" + "|" + "code:" + code);
					}
					if ("370102".equals(m.get("payUnitName"))) { //热电
						//						logger.info("--------------getMyHomePageInfo(查询暖气费开始调用第三方热电接口)-------------" + "|" + "params:" + params);
						//						response = HttpUtil.post(urlEle, params);
						//						logger.info("--------------getMyHomePageInfo(查询暖气费第三方接口热电调用完毕)-------------" + "|" + "response:" + response);
						//						JsonStr = JsonUtil.strToJson((Object) response);
						//						String code = JsonStr.getString("code");
						//						if (!BeanUtil.isNullString(code) && code.equalsIgnoreCase("1001")) {
						//							displayValue = JsonStr.getString("shouldPay");
						//							due+= Double.valueOf(displayValue);
						//							if (due > 0) {
						//								flag = true;
						//							}
						//						}

					}
				}
				if (flag) {
					//极光推送
					Long waterId = Long.valueOf(Config.getValue("heatingId"));
					String ymd = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
					String content = "【账单】" + userName + "先生/女士，您家的暖气费该缴费啦！截止到" + ymd + "，您本次暖气费欠费:" + due + "元，缴费请戳：";
					scheduleNotification(phone, content, custId, pushtime, OPENAPP, waterId);
				}
			}
		} catch (Exception e) {
			logger.error("--------------getMyHomePageInfo(查询暖气费出现异常error)-------------" + "|" + "fromModule:CustController" + "|" + "error:" + e.toString());
		}

	}
}
