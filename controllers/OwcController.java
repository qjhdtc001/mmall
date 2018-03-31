package com.inspur.icity.web.controllers;

import com.google.common.collect.Maps;
import com.inspur.icity.logic.base.model.Dict;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.owc.model.OwcForecast;
import com.inspur.icity.logic.owc.service.OwcForecastService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 天气相关接口
 */
@Controller
@RequestMapping(value = "/owc")
public class OwcController extends BaseAuthController {

    @Autowired
    OwcForecastService owcForecastService;
    @Autowired
    DictService dictService;

    /**
     * 获取我的界面上面的城市天气
     * @param city 城市名称
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/homeWeather")
    public Object getHomeWeather(@RequestParam String city){
        Map<String, Object> map = Maps.newHashMap();
        List<OwcForecast> owcForecastList = owcForecastService.findByProvCnDistrictCn(city);
        if(owcForecastList != null && owcForecastList.size() > 0){
            if(owcForecastList.get(0).getDayWeatherCode() != null && !"".equals(owcForecastList.get(0).getDayWeatherCode())){
                Dict dict = dictService.getByCodeAndOwcMapWeatherType(owcForecastList.get(0).getDayWeatherCode());
                map.put("weather", dict.getContent());
                map.put("weatherType", dict.getDescription());
            }else{
                Dict dict = dictService.getByCodeAndOwcMapWeatherType(owcForecastList.get(0).getNightWeatherCode());
                map.put("weather", dict.getContent());
                map.put("weatherType", dict.getDescription());
            }
            String minTemp = owcForecastList.get(0).getNightTemp();
            String maxTemp = StringUtils.isEmpty(owcForecastList.get(0).getDayTemp()) ? owcForecastList.get(1).getDayTemp() : owcForecastList.get(0).getDayTemp();
            map.put("tempRange", minTemp + "～" + maxTemp + "℃");
        }
        return map;
    }
}
