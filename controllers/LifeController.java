package com.inspur.icity.web.controllers;

import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.logic.life.service.BuildService;
import com.inspur.icity.logic.life.service.ParkService;
import com.inspur.icity.web.gov.builder.HallToMapBuilder;

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
 * 生活相关接口
 */
@Controller
@RequestMapping(value = "/life")
public class LifeController extends BaseAuthController{
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BuildService buildService;
    @Autowired
    ParkService parkService;
    @Autowired
    HallToMapBuilder hallToMapBuilder;

    /**
     * 施工信息列表
     * @Parma cityCode
     */
    @ResponseBody
    @RequestMapping(value = "/buildList", params = {"cityCode"}, method = RequestMethod.GET)
    public Object list(String cityCode){
        return buildService.buildList(cityCode);
    }

    /**
     * 施工详情
     * @Parma buildId
     */
    @ResponseBody
    @RequestMapping(value = "/buildDetail",params = {"buildId"},method = RequestMethod.GET)
    public Object detail(Long buildId){
        return buildService.buildDetail(buildId);
    }

    /**
     * 停车场列表
     * @Parma lat, lng 经纬度
     * @Parma cityCode 城市Code
     */
    @ResponseBody
    @RequestMapping(value = "/parkList",params = {"lat","lng","cityCode"},method = RequestMethod.GET)
    public Object list(String lat,String lng,String cityCode){
    	if(BeanUtil.isNullString(lat)||BeanUtil.isNullString(lng)
    			||lat.equalsIgnoreCase("0.000000")||lng.equalsIgnoreCase("0.000000")){
    		lat = null;
    		lng = null;
    	}
        List<?> list = parkService.parkList(lat, lng, cityCode);
        return map(list, hall -> {
            return hallToMapBuilder.build((Map) hall);
        });

    }

    /**
     * 停车场地搜索
     * @Parma lat, lng 经纬度
     * @Parma cityCode 城市Code
     * @Parma key 搜索内容
     */
    @ResponseBody
    @RequestMapping(value = "/search",params = {"lat", "lng", "cityCode","key"},method = RequestMethod.GET)
    public Object parkSearch(String lat, String lng, String cityCode,String key){
        List list = parkService.parkSearch(lat, lng, cityCode,key);
        return map(list, hall -> {
            return hallToMapBuilder.build((Map) hall);
        });
    }

}
