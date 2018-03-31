package com.inspur.icity.web.controllers;


import com.google.common.collect.Lists;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.logic.base.model.Dict;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.cust.model.MyHall;
import com.inspur.icity.logic.cust.service.MyHallService;
import com.inspur.icity.logic.gov.service.HallService;
import com.inspur.icity.web.gov.builder.HallToMapBuilder;
import com.inspur.icity.web.utils.IcityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大厅服务
 */
@Controller
@RequestMapping(value = "/hall")
public class HallController extends BaseAuthController{
    @Autowired
    HallService hallService;
    @Autowired
    DictService dictService;
    @Autowired
    MyHallService myHallService;
    @Autowired
    HallToMapBuilder hallToMapBuilder;

    /**
     * 大厅列表、大厅搜索列表
     * @param lng (经度)、lat (纬度)、key (输入框)、townName (经度)、distance (距离)、netpointType (网店类型)、workTime (上班时间)
     * @return 大厅搜索列表
     */
    @ResponseBody
    @RequestMapping(value = "/search")
    public Object addComplain(@RequestParam(required = true) Double lng,@RequestParam(required = true) Double lat,
                              @RequestParam(required = false) String key,@RequestParam(required = false) String townName,
                              @RequestParam(required = false) Double distance,@RequestParam(required = false) String netpointType,
                              @RequestParam(required = false) String workTime, @RequestParam(required = true) String cityCode){
        List<Dict> dicts = Lists.newArrayList();
        if(StringUtils.isNotEmpty(key)){
            dicts = dictService.getCodeAndType(key);
        }
        StringBuilder  gldwTypesb = new StringBuilder();
        StringBuilder netpointTypesb = new StringBuilder();
        for(Dict dict : dicts){
            if("gldwType".equals(dict.getType())){
                gldwTypesb.append(dict.getCode()+",");
            }
            if("netpointType".equals(dict.getType())){
                netpointTypesb.append(dict.getCode() + ",");
            }
        }
        String gldwTypes = "";
        if(gldwTypesb.length() > 0){
            gldwTypes = gldwTypesb.toString().substring(0, gldwTypesb.length() - 1);
        }
        String netpointTypes = "";
        if(netpointTypesb.length() > 0){
            netpointTypes = netpointTypesb.toString().substring(0, netpointTypesb.length() - 1);
        }
        List<Map> lists = hallService.search(lng,lat,gldwTypes,netpointTypes,key,townName,distance,netpointType,workTime, cityCode,getPageBounds());
        return map(lists, hall -> {
            return hallToMapBuilder.build((Map) hall);
        });
    }

    /**
     * 获取区县列表
     * @param cityCode 城市code
     * @return 区县列表
     */
    @ResponseBody
    @RequestMapping(value = "/getTownList", method = {RequestMethod.GET})
    public Object getTownList(String cityCode) {
        return hallService.getTownList(cityCode);
    }

    /**
     * 获取网点类别列表
     * @param cityCode 城市code
     * @return 网点类别列表
     */
    @ResponseBody
    @RequestMapping(value = "/queryPointType", method = {RequestMethod.GET})
    public Object queryPointType(String cityCode) {
        return hallService.queryPointType(cityCode);
    }

    /**
     * 大厅服务详情列表
     * @param lat 纬度
     * @param lng  经度
     * @param serviceId  大厅id
     * @return 详情列表
     */
    @ResponseBody
    @RequestMapping(value = "/getServiceDetails", method = {RequestMethod.GET})
    public Object getServiceDetails(String lat,String lng,Long serviceId,String cityCode) {
        Map resultMap = hallService.getServiceDetails(lat, lng, serviceId, getLoginUserId(), cityCode);
        Double doubleDis = Double.parseDouble(resultMap.get("distance").toString());
        resultMap.put("distance", IcityUtils.getDistance(doubleDis));
        return resultMap;
    }
    /**
     * 大厅详情页收藏接口（登录）
     * @param hallId 大厅id
     */
    @ResponseBody
    @RequestMapping(value = "/{hallId}/favorite", method = RequestMethod.POST)
    public void hallFavorite(@PathVariable Long hallId) {
        if (getLoginUserId() != null) {
            MyHall myHall = new MyHall();
            myHall.setCustId(getLoginUserId());
            myHall.setDeviceId(getDeviceId());
            myHall.setHallId(hallId);
            myHallService.addByCustId(myHall);

        } else {
            throw new ApplicationException(900,"您还没有登录，不能使用收藏功能");
        }
    }

    /**
     * 大厅详情页取消收藏接口（登录）
     *
     * @param hallId 大厅id
     */
    @ResponseBody
    @RequestMapping(value = "/{hallId}/cancelFavorite", method = RequestMethod.POST)
    public void cancelHallFavorite(@PathVariable Long hallId) {
        if (getLoginUserId() != null) {
            myHallService.removeBycondition(getLoginUserId(), hallId);
        } else {
            throw new ApplicationException(900,"您还没有登陆，不能使用取消收藏功能");
        }
    }



    /**
     * 政务办理地点详情页收藏接口（登录）
     *
     * @param hallId 办理地点id
     */
    @ResponseBody
    @RequestMapping(value = "/{hallId}/govFavorite", method = RequestMethod.POST)
    public void favorite(@PathVariable Long hallId) {
        if (getLoginUserId() != null) {
            MyHall myHall = new MyHall();
            myHall.setCustId(getLoginUserId());
            myHall.setDeviceId(getDeviceId());
            myHall.setHallId(hallId);
            myHallService.addByCustId(myHall);
        } else {
            throw new ApplicationException(900,"您还没有登录，不能使用收藏功能");
        }
    }

    /**
     * 政务办理地点详情页取消收藏接口（登录）
     *
     * @param hallId 大厅id
     */
    @ResponseBody
    @RequestMapping(value = "/{hallId}/cancelGovFavorite", method = RequestMethod.POST)
    public void cancelFavorite(@PathVariable Long hallId) {
        if (getLoginUserId() != null) {
            myHallService.removeBycondition(getLoginUserId(), hallId);
        } else {
            throw new ApplicationException(900,"您还没有登陆，不能使用取消收藏功能");
        }
    }


    /**
     * 政务办理地点详情页
     *
     * @param hallId  大厅id
     * @param lat lng 经纬度
     */
    @ResponseBody
    @RequestMapping(value = "/{hallId}",method = {RequestMethod.GET})
    public Object findDetails(String lat,String lng,@PathVariable Long hallId) {
    	
    	int latResult = lat.indexOf("0.00");
    	int lngResult = lng.indexOf("0.00");
    	
    	if(latResult != -1 || lngResult != -1){
    		lat = null;
    		lng = null;
    	}
    	
        Map<String, Object> resultMap = hallService.getById(hallId,lat,lng);
        // 获取大厅详情
        MyHall myHall = myHallService.findDetails(getLoginUserId(), hallId);
//        String distance = hallService.getWindowDistance(lat, lng, hallId);
        String distance = null;
        if(!"".equals(resultMap.get("distance"))&&(resultMap.get("distance") !=null)){
            distance = resultMap.get("distance").toString();
            resultMap.put("distance", IcityUtils.getDistance(Double.parseDouble(distance)));
        }
        if(distance == null){
        	resultMap.put("distance", "");
            return resultMap;
        }
//        Map<String, Object> resultMap = transBean2Map(hall);
        // 是否已收藏
        if (myHall != null) {
            resultMap.put("hasAdded", 1);
        } else {
            resultMap.put("hasAdded", 0);
        }
        return resultMap;
    }

    public static Map<String, Object> transBean2Map(Object obj) {

        if (obj == null) {
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
