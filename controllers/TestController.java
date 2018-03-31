package com.inspur.icity.web.controllers;

import com.inspur.icity.core.web.BaseController;
import com.inspur.icity.logic.base.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/")
public class TestController extends BaseController {

    @Autowired
    DictService dictService;

    @ResponseBody
    @RequestMapping(value = "")
    public Object findTollStations(){
        return "服务器启动成功了~！";
    }

    @ResponseBody
    @RequestMapping(value = "/makeAccessToken")
    public Object findTollStations2(){
        return dictService.get(10002l);
    }
}
