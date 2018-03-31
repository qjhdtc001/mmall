package com.inspur.icity.web.app.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.logic.app.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 生活应用列表（新）
 */
@Component
public class AppToLifeBuild {
    @Autowired
    ApplicationService applicationService;
    public List build(List<Map<String, Object>> appList,String cityCode) {

        //从字典表里取数据
        //List<Map<String, Object>> typeMap = applicationService.findByCityCode(cityCode);
        //从新添加的表里取数据
        List<Map<String, Object>> typeMap = applicationService.queryByCityCode(cityCode);
        if(typeMap == null ||typeMap.size() == 0){
            return null;
        }

        List<Map<String, Object>> lifeAppList = Lists.newArrayList();
        for (Map<String, Object> map1 : typeMap) {
            List<Map<String, Object>> lifeList = new ArrayList<>();
            Map<String, Object> app1 = Maps.newHashMap();
            app1.put("type", map1.get("type"));
            app1.put("name", map1.get("name"));
            for (Map<String, Object> appMap : appList) {
                if(map1.get("type").toString().equals(appMap.get("type").toString())){
                    lifeList.add(appMap);
                }
            }
            app1.put("list",lifeList);
            lifeAppList.add(app1);
        }
        return lifeAppList;
    }
}
