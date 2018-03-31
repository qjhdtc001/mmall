package com.inspur.icity.web.controllers;

import com.inspur.icity.logic.base.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 从字典表获取数据的相关接口
 */
@Controller
@RequestMapping(value = "/dict")
public class DictController extends BaseAuthController{
    @Autowired
    DictService dictService;

    /**
     * 通过类型查询上班时间
     * @param type 类型
     * @return 信息一览
     */
    @ResponseBody
    @RequestMapping(value = "/getList", method = {RequestMethod.GET})
    public Object getList(String type) {
        return dictService.findByType(type);
    }

    /**
     * 通过类型查询网点类别
     * @param type 类型
     * @return 信息一览
     */
    @ResponseBody
    @RequestMapping(value = "/getPointType", method = {RequestMethod.GET})
    public Object getPointType(String type, String cityCode) {
        return dictService.findPointType(type, cityCode);
    }
}
