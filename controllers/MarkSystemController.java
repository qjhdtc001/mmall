package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.inspur.icity.logic.cust.model.CheckIn;
import com.inspur.icity.logic.cust.model.Mark;
import com.inspur.icity.logic.cust.model.MarkMission;
import com.inspur.icity.logic.cust.model.MarkRecord;
import com.inspur.icity.logic.cust.service.CheckInService;
import com.inspur.icity.logic.cust.service.MarkMissionService;
import com.inspur.icity.logic.cust.service.MarkRecordService;
import com.inspur.icity.logic.cust.service.MarkService;

/**
 * 积分系统接口
 * 
 * @author zxl
 *
 */
@Controller
@RequestMapping(value = "/mark")
public class MarkSystemController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	// 积分任务
	@Autowired
	MarkMissionService markMissionService;
	// 积分记录
	@Autowired
	MarkRecordService markRecordService;
	// 签到接口
	@Autowired
	CheckInService checkInService;
	// 用户积分接口
	@Autowired
	MarkService markService;

	/**
	 * 积分任务接口
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findMarkMission", method = RequestMethod.POST)
	public Object findMarkMission() {
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Long custId = getLoginUserId();
		try {
			CheckIn checkIn = checkInService.get(custId);
			Integer continuousCheckInDays = null;
			if (checkIn != null) {
				continuousCheckInDays = checkIn.getContinuousCheckInDays();
			}

			List<Map<String, Object>> markMission = markMissionService.findMarkMesByCusrId(custId);
			if (markMission != null && markMission.size() > 0) {
				for (Map<String, Object> m : markMission) {
					if (continuousCheckInDays != null && continuousCheckInDays >= 7) {
						if ("连续7天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}

					}
					if (continuousCheckInDays != null && continuousCheckInDays >= 14) {
						if ("连续14天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}

					}
					if (continuousCheckInDays != null && continuousCheckInDays >= 21) {
						if ("连续21天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}

					}
					MarkRecord markRecord = markRecordService.getMarkOneDay(custId,
							markMissionService.findMarkMesByInterUrl("/mark/checkIn30").getId());
					if (markRecord != null) {
						if ("连续7天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}
						if ("连续14天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}
						if ("连续21天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}
						if ("连续30天签到".equals(m.get("mission"))) {
							m.put("missionPlan", "<font color=\"#666666\">已完成</font>");
						}
					}
					list.add(m);
				}
			}
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("查询成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(new ArrayList<Map<String, Object>>());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------findMarkMission(error)-------------" + "|" + "fromModule:MarkSystemController"
					+ "|" + "interfaceName:积分任务" + "|" + "error:" + e.toString());
			return model;
		}

	}

	/**
	 * 积分记录接口
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findMarkRecord", method = RequestMethod.POST)
	public Object findMarkRecord() {
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<Map<String, Object>> markRecord = markRecordService.findMarkRecordByCustId(getLoginUserId(),
					getPageBounds());
			logger.info("--------------findMarkRecord(积分记录)-------------" + "|" + "fromModule:MarkSystemController"
					+ "|" + "interfaceName:积分记录" + "|" + "markRecord:" + markRecord.toString());
			if (markRecord != null && markRecord.size() > 0) {
				list = markRecord;
			}

			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("查询成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(new ArrayList<Map<String, Object>>());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------findMarkRecord(error)-------------" + "|" + "fromModule:MarkSystemController"
					+ "|" + "interfaceName:积分记录" + "|" + "error:" + e.toString());
			return model;
		}

	}

	/**
	 * 我的积分接口
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findMyMarks", method = RequestMethod.POST)
	public Object findMyMarks() {
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Long custId = getLoginUserId();
		try {
			Integer yesterdayMark = markRecordService.findYesterdayMark(custId);
			Mark mark = markService.findByCustId(custId);
			if (mark == null) {
				mark = new Mark();
				mark.setCustId(custId);
				mark.setCurrentMark(0);
				mark.setTotalMark(0);
				mark.setYesterdayMark(0);
				markService.add(mark);
			}
			logger.info("--------------findMarkRecord(我的积分接口)-------------" + "|" + "fromModule:MarkSystemController"
					+ "|" + "interfaceName:我的积分接口" + "|" + "mark:" + mark + "|" + "yesterdayMark:" + yesterdayMark);
			Map<String, Object> map = Maps.newHashMap();
			if (yesterdayMark != null) {
				mark.setYesterdayMark(yesterdayMark);
				markService.update(mark);
			} else {
				mark.setYesterdayMark(0);
				markService.update(mark);
			}

			map.put("currentMark", mark.getCurrentMark() == null ? 0 : mark.getCurrentMark());
			map.put("yesterdayMark", mark.getYesterdayMark() == null ? 0 : mark.getYesterdayMark());
			map.put("totalMark", mark.getTotalMark() == null ? 0 : mark.getTotalMark());

			list.add(map);
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("查询成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(new ArrayList<Map<String, Object>>());
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------findMyMarks(error)-------------" + "|" + "fromModule:MarkSystemController" + "|"
					+ "interfaceName:我的积分" + "|" + "error:" + e.toString());
			return model;
		}
	}

	/**
	 * 签到接口
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkIn", method = RequestMethod.POST)
	public Object checkIn() {
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Long custId = getLoginUserId();
		try {
			model = checkInService.getMarkCheckIn(model, custId, "/mark/checkIn");
			return model;
		} catch (Exception e) {
			model.setCode("0100");
			model.setError(e.toString());
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
			logger.error("--------------checkIn(error)-------------" + "|" + "fromModule:MarkSystemController" + "|"
					+ "interfaceName:签到接口" + "|" + "error:" + e.toString());
			return model;
		}

	}

}
