package com.inspur.icity.web.cust.builder;

import com.google.common.collect.Maps;
import com.inspur.icity.core.builder.ToMapBuilder;
import com.inspur.icity.logic.cust.model.FeedBack;
import com.inspur.icity.logic.cust.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class FeedBackToMapBuilder implements ToMapBuilder<FeedBack> {

    @Autowired
    CustomerService customerService;

    public Map<String,Object> build(FeedBack feedBack) {
        return null;
    }
    public Map<String,Object> build(FeedBack feedBack,Map customer) {
        if(feedBack == null){
            return null;
        }
        Map map = Maps.newHashMap();
        map.put("id",feedBack.getId());
        map.put("msg",feedBack.getMsg());
        map.put("createTime",feedBack.getCreateTime());
        map.put("isManager",feedBack.getIsManager());
        if(!feedBack.getIsManager()){
            if(customer != null){
                map.put("nickName",customer.get("nickName") != null ? customer.get("nickName"):"游客");
                map.put("imgUrl",customer.get("imgUrl")!=null?customer.get("imgUrl"):"");
            }else {
                map.put("nickName","游客");
                map.put("imgUrl","");
            }
        }else {
            map.put("nickName","小爱");
            map.put("imgUrl","");
        }
        return map;
    }
}
