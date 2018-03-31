package com.inspur.icity.web.controllers;


import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.cust.model.MyWindow;
import com.inspur.icity.logic.cust.service.MyWindowService;
import com.inspur.icity.logic.gov.model.Window;
import com.inspur.icity.logic.gov.service.WindowService;
import com.inspur.icity.web.utils.IcityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 办理地点
 */
@Controller
@RequestMapping(value = "/window")
public class WindowController extends BaseAuthController {
    @Autowired
    WindowService windowService;
    @Autowired
    DictService dictService;
    @Autowired
    MyWindowService myWindowService;

    /**
     * 政务办理地点详情页收藏接口（登录）
     *
     * @param windowId 办理地点id
     */
    @ResponseBody
    @RequestMapping(value = "/{windowId}/favorite", method = RequestMethod.POST)
    public void favorite(@PathVariable Long windowId) {
        if (getLoginUserId() != null) {
            MyWindow myWindow = new MyWindow();
            myWindow.setCustId(getLoginUserId());
            myWindow.setDeviceId(getDeviceId());
            myWindow.setWindowId(windowId);
            myWindowService.addByCustId(myWindow);

        } else {
            throw new ApplicationException(900,"您还没有登录，不能使用收藏功能");
        }
    }

    /**
     * 政务办理地点详情页取消收藏接口（登录）
     *
     * @param windowId 大厅id
     */
    @ResponseBody
    @RequestMapping(value = "/{windowId}/cancelFavorite", method = RequestMethod.POST)
    public void cancelFavorite(@PathVariable Long windowId) {
        if (getLoginUserId() != null) {
            myWindowService.removeBycondition(getLoginUserId(), windowId);
        } else {
            throw new ApplicationException(900,"您还没有登陆，不能使用取消收藏功能");
        }
    }


    /**
     * 政务办理地点详情页
     * @param windowId 大厅id
     * @param lat、lng 经纬度
     */
    @ResponseBody
    @RequestMapping(value = "/{windowId}")
    public Object findDetails(String lat, String lng, @PathVariable Long windowId) {
        Window window = windowService.get(windowId);
        // 获取大厅详情
        MyWindow myWindow = myWindowService.findDetails(getLoginUserId(),windowId);
        String distance = windowService.getWindowDistance(lat, lng, windowId);
        Map<String, Object> resultMap = transBean2Map(window);
        resultMap.put("distance", IcityUtils.getDistance(Double.parseDouble(distance)));
        // 是否已收藏
        if(myWindow != null){
            resultMap.put("hasAdded", 1);
        }else{
            resultMap.put("hasAdded", 0);
        }
        return resultMap;
    }

    public static Map<String, Object> transBean2Map(Object obj) {

        if(obj == null){
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }
        return map;
    }
}
