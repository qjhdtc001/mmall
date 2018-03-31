package com.inspur.icity.web.controllers;

import com.inspur.icity.logic.base.service.CityService;
import com.inspur.icity.web.city.builder.CityToMapBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 城市相关接口
 */
@Controller
@RequestMapping(value = "/city")
public class CityController extends BaseAuthController {
	Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	CityService cityService;
	@Autowired
	CityToMapBuilder cityToMapBuilder;

	@ResponseBody
	@RequestMapping(value = "/getCityList", method = RequestMethod.GET)
	public Object cityList() {
		String isNews="";
		Integer version=getVersion();
		if(version<243){
			//0：代表资讯
			isNews="0";
			List<Map<String, Object>> list = cityService.cityList(isNews);
			return cityToMapBuilder.build(list);
		}else{
			List<Map<String, Object>> list = cityService.cityList(isNews);
			return cityToMapBuilder.build(list);
		}
	}

	/**
	 * 
	 * @Title 选择资讯或周边服务
	 * @Description 不同的城市显示不同菜单栏信息
	 * @return Object
	 * @author ZhangXingLiang
	 * @date 2017年6月2日下午1:08:23
	 */
	@ResponseBody
	@RequestMapping(value = "/isNewsOrCir", method = RequestMethod.GET)
	public Object opinionNewsOrCir() {
		logger.info("--------------opinionNewsOrCir(start)-------------|CityController|interfaceInfo:选择资讯或周边服务");
		Map<String, Object> m = null;
		try {
			String cityCode = getCityCode();
			logger.info("--------------opinionNewsOrCir-------------|CityController|cityCode:" + cityCode);
			if (cityCode == null || "".equals(cityCode)) {
				cityCode = "370100";
			}
			m = cityService.findNewsOrCir(cityCode);//0：代表资讯  1：代表周边服务
			logger.info("--------------opinionNewsOrCir(end)-------------|CityController|result:" + m);
		} catch (Exception e) {
			logger.error("--------------opinionNewsOrCir(error)-------------|" + e.toString());
		}
		return m;
	}
}
