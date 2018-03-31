package com.inspur.icity.web.controllers;


import com.inspur.icity.logic.app.service.AdvertService;
import com.inspur.icity.logic.app.service.AppRecommentService;
import com.inspur.icity.logic.app.service.ApplicationService;
import com.inspur.icity.logic.base.service.CityService;
import com.inspur.icity.logic.base.service.DepartmentService;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.cust.service.*;
import com.inspur.icity.logic.gov.service.*;
import com.inspur.icity.logic.life.service.*;
import com.inspur.icity.logic.manager.service.UserCityService;
import com.inspur.icity.logic.manager.service.UserService;
import com.inspur.icity.logic.news.service.HotNewsService;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.owc.service.OwcAreaService;
import com.inspur.icity.logic.owc.service.OwcForecastService;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 清空缓存接口
 */
@Controller
@RequestMapping(value = "/flushCache")
public class FlushCacheController extends BaseAuthController {
    @Autowired
    AdvertService advertService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    AppRecommentService appRecommentService;
    @Autowired
    CityService cityService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    DictService dictService;
    @Autowired
    AccesstokenService accesstokenService;
    @Autowired
    AccountService accountService;
    @Autowired
    AnswerService answerService;
    @Autowired
    AppService appService;
    @Autowired
    CollectionQuestionService collectionQuestionService;
    @Autowired
    CommentService commentService;
    @Autowired
    CustomerService customerService;
    @Autowired
    DeviceService deviceService;
    @Autowired
    FeedBackService feedBackService;
    @Autowired
    GovService govService;
    @Autowired
    HotSearchService hotSearchService;
    @Autowired
    MyDeviceService myDeviceService;
    @Autowired
    MyHallService myHallService;
    @Autowired
    MyWindowService myWindowService;
    @Autowired
    NewsService newsService;
    @Autowired
    PraiseService praiseService;
    @Autowired
    QuestionService questionService;
    @Autowired
    SearchService searchService;
    @Autowired
    ConditionService conditionService;
    @Autowired
    GovRecommentService govRecommentService;
    @Autowired
    HallService hallService;
    @Autowired
    InterfaceService interfaceService;
    @Autowired
    ItemHallService itemHallService;
    @Autowired
    ItemService itemService;
    @Autowired
    MaterialService materialService;
    @Autowired
    ScheduleUrlService scheduleUrlService;
    @Autowired
    WindowService windowService;
    @Autowired
    BuildService buildService;
    @Autowired
    LifeApplicationService lifeApplicationService;
    @Autowired
    LifeAppService lifeAppService;
    @Autowired
    ParkService parkService;
    @Autowired
    TypeService typeService;
    @Autowired
    UserCityService userCityService;
    @Autowired
    UserService userService;
    @Autowired
    HotNewsService hotNewsService;
    @Autowired
    MsgService msgService;
    @Autowired
    OwcAreaService owcAreaService;
    @Autowired
    OwcForecastService owcForecastService;
    @Autowired
    SensitiveHistService sensitiveHistService;
    @Autowired
    SensitiveWordService sensitiveWordService;


    @ResponseBody
    @RequestMapping("/app")
    public String flushCacheApp() {
        advertService.flushCache();
        applicationService.flushCache();
        appRecommentService.flushCache();
        return "App Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/base")
    public String flushCacheBase(){
        cityService.flushCache();
        departmentService.flushCache();
        dictService.flushCache();
        return "Base Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/cust")
    public String flushCacheCust(){
        accesstokenService.flushCache();
        accountService.flushCache();
        answerService.flushCache();
        appService.flushCache();
        collectionQuestionService.flushCache();
        commentService.flushCache();
        customerService.flushCache();
        deviceService.flushCache();
        feedBackService.flushCache();
        govService.flushCache();
        hotSearchService.flushCache();
        myDeviceService.flushCache();
        myHallService.flushCache();
        myWindowService.flushCache();
        newsService.flushCache();
        praiseService.flushCache();
        questionService.flushCache();
        searchService.flushCache();
        return "Cust Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/gov")
    public String flushCacheGov(){
        conditionService.flushCache();
        govRecommentService.flushCache();
        hallService.flushCache();
        interfaceService.flushCache();
        itemHallService.flushCache();
        itemService.flushCache();
        materialService.flushCache();
        scheduleUrlService.flushCache();
        windowService.flushCache();
        return "Gov Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/life")
    public String flushCacheLife(){
        buildService.flushCache();
        lifeApplicationService.flushCache();
        lifeAppService.flushCache();
        parkService.flushCache();
        typeService.flushCache();
        return "Life Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/manager")
    public String flushCacheManager(){
        userCityService.flushCache();
        userService.flushCache();
        return "Manager Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/news")
    public String flushCacheNews(){
        hotNewsService.flushCache();
        msgService.flushCache();
        return "News Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/owc")
    public String flushCacheOwc(){
        owcAreaService.flushCache();
        owcForecastService.flushCache();
        return "Owc Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/sensitive")
    public String flushCacheSensitive(){
        sensitiveHistService.flushCache();
        sensitiveWordService.flushCache();
        return "Sensitive Repo FlushCache Ok";
    }

    @ResponseBody
    @RequestMapping("/all")
    public String flushCacheAll(){
        //app
        advertService.flushCache();
        applicationService.flushCache();
        appRecommentService.flushCache();
        //base
        cityService.flushCache();
        departmentService.flushCache();
        dictService.flushCache();
        //cust
        accesstokenService.flushCache();
        accountService.flushCache();
        answerService.flushCache();
        appService.flushCache();
        collectionQuestionService.flushCache();
        commentService.flushCache();
        customerService.flushCache();
        deviceService.flushCache();
        feedBackService.flushCache();
        govService.flushCache();
        hotSearchService.flushCache();
        myDeviceService.flushCache();
        myHallService.flushCache();
        myWindowService.flushCache();
        newsService.flushCache();
        praiseService.flushCache();
        questionService.flushCache();
        searchService.flushCache();
        //gov
        conditionService.flushCache();
        govRecommentService.flushCache();
        hallService.flushCache();
        interfaceService.flushCache();
        itemHallService.flushCache();
        itemService.flushCache();
        materialService.flushCache();
        scheduleUrlService.flushCache();
        windowService.flushCache();
        //life
        buildService.flushCache();
        lifeApplicationService.flushCache();
        lifeAppService.flushCache();
        parkService.flushCache();
        typeService.flushCache();
        //manager
        userCityService.flushCache();
        userService.flushCache();
        //news
        hotNewsService.flushCache();
        msgService.flushCache();
        //owc
        owcAreaService.flushCache();
        owcForecastService.flushCache();
        //sensitive
        sensitiveHistService.flushCache();
        sensitiveWordService.flushCache();
        return "All Repo FlushCache Ok";
    }
}
