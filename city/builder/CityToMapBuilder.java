package com.inspur.icity.web.city.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.logic.base.utils.AppDICTConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加应用列表分类
 */
@Component
public class CityToMapBuilder {
    public Map<String, Object> build(List<Map<String, Object>> list){
        Map<String, Object> map = Maps.newHashMap();
        if(list == null || list.size() == 0){
            return map;
        }
        List<Map<String, Object>> list1 = new ArrayList<>();
        List<Map<String, Object>> list2 = new ArrayList<>();
        for(Map<String, Object> cityCode: list){
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", cityCode.get("code"));
            map1.put("name", cityCode.get("name"));
            map1.put("disable", cityCode.get("disable"));
            if (cityCode.get("disable").toString().equals("0")) {
                list1.add(map1);
            } else if (cityCode.get("disable").toString().equals("1")) {
                list2.add(map1);
            }
            map.put("open", list1);
            map.put("close", list2);
        }
        return map;
    }
}
