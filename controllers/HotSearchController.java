package com.inspur.icity.web.controllers;

import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.cust.service.HotSearchService;
import com.inspur.icity.logic.cust.service.QuestionService;
import com.inspur.icity.logic.gov.service.ItemService;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.web.utils.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 热搜相关接口
 */
@Controller
@RequestMapping(value = "/hotSearch")
public class HotSearchController extends BaseAuthController{
	Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    HotSearchService hotSearchService;
    @Autowired
    ItemService itemService;
    @Autowired
    MsgService msgService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    QuestionService questionService;
    /**
     * 获取热搜词汇
     * @param cityCode
     * @param type
     * @return 政务详情
     */
    @ResponseBody
    @RequestMapping(value = "/hotWords", method = {RequestMethod.GET})
    public Object hotWords(String cityCode, String type) {
        return hotSearchService.hotSearch(cityCode, type);
    }

    /**
     * 搜索列表
     * @Parma key
     * @Parma type(gov,news,life)
     */
    @ResponseBody
    @RequestMapping(value = "/searchList",method = {RequestMethod.GET})
    public Object search(String type,String key,@RequestParam(required = false) String cityCode){
		logger.info("--------------searchList(start)-------------"+"|"+"fromModule:HotSearchController"+"|" + ",cityCode:"+cityCode+"|"+",type:"+type+",key:" + key);
		key = key.replaceAll("\\%", "\\\\%");
		key = key.replaceAll("\\_", "\\\\_");
        List<Map<String, Object>> govList = itemService.searchList(key, cityCode);
        List<Map<String,Object>> newsList = msgService.newsSearch(key, cityCode);
        List<Map<String,Object>> appList = applicationService.appSearch(key, cityCode);
        List<Map<String, Object>> consultList = questionService.searchList(key, cityCode);
        if(type.equals(Constants.TYPE_GOV)){
            for(Map<String, Object> map:govList){
                map.put("module",Constants.TYPE_GOV);
            }
            return govList;
        }else if(type.equals(Constants.TYPE_NEWS)){
            for (Map<String, Object> map : newsList) {
                map.put("module", Constants.TYPE_NEWS);
            }
            return newsList;
        }else if(type.equals(Constants.TYPE_CONSULTS)){
            for (Map<String, Object> map : consultList) {
                map.put("module", Constants.TYPE_CONSULTS);
            }
            return consultList;
        } else {
            for (Map<String, Object> map : appList) {
                map.put("module", "life");
            }
            return appList;
        }
    }
}
