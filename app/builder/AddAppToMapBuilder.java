package com.inspur.icity.web.app.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.cust.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加应用列表分类(新的)
 */
@Component
public class AddAppToMapBuilder {

    @Autowired
    AppService appService;
    @Autowired
    ApplicationService applicationService;

    public List build(List<Map<String, Object>> appList,String cityCode){
        //从字典表里取添加应用的类型
        //List<Map<String, Object>> listType = appService.findAddType(cityCode);
        //从新表中取数据
        List<Map<String, Object>> listType = applicationService.queryByCityCode(cityCode);
        if(listType == null){
            return null;
        }
        List<Map<String, Object>> lifeAddAppList = new ArrayList<>();
        for(Map<String, Object> addMap : listType){
            List<Map<String, Object>> lifeList = new ArrayList<>();
            Map<String, Object> app1 = new HashMap<>();
            app1.put("type", addMap.get("type"));
            app1.put("name", addMap.get("name"));
            for (Map<String, Object> appMap : appList) {
                int hasAdded = 0;
                Map<String, Object> app = new HashMap<>();
                if(appMap.get("hasAppId") != null && !"".equals(appMap.get("hasAppId").toString())){
                    hasAdded = 1;
                }else{
                    hasAdded = 0;
                }
                app.put("hasAdded", hasAdded);
                if(addMap.get("type").toString().equals(appMap.get("type").toString())){
                    app.put("appId", appMap.get("id"));
                    app.put("imgUrl", appMap.get("imgUrl"));
                    app.put("appName", appMap.get("name"));
                    app.put("gotoUrl", appMap.get("gotoUrl"));
                    app.put("code", appMap.get("code"));
                    app.put("type", appMap.get("type"));
                    lifeList.add(app);
                }
            }
            app1.put("list", lifeList);
            lifeAddAppList.add(app1);
        }
        return lifeAddAppList;
    }
}
