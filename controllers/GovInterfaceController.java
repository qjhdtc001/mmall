package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.app.service.AppRecommentService;
import com.inspur.icity.logic.cust.model.Comment;
import com.inspur.icity.logic.cust.model.Gov;
import com.inspur.icity.logic.cust.model.Praise;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.GovService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.gov.model.Item;
import com.inspur.icity.logic.gov.service.GovRecommentService;
import com.inspur.icity.logic.gov.service.InterfaceService;
import com.inspur.icity.logic.gov.service.ItemService;
import com.inspur.icity.logic.gov.service.ScheduleUrlService;
import com.inspur.icity.logic.sensitive.model.SensitiveHist;
import com.inspur.icity.logic.sensitive.model.SensitiveWords;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.cust.builder.CommentToMapBuilder;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;
import com.inspur.icity.web.utils.IcityUtils;

/**
 * 政务相关接口
 */
@Controller
@RequestMapping(value = "/gov")
public class GovInterfaceController extends BaseAuthController{
    // 进度查询接口
    private static final String progressUrl = Config.getValue("progressUrl");
    @Autowired
    InterfaceService interfaceService;
    @Autowired
    AppRecommentService appRecommentService;
    @Autowired
    ItemService itemService;
    @Autowired
    GovRecommentService govRecommentService;
    @Autowired
    PraiseService praiseService;
    @Autowired
    CommentService commentService;
    @Autowired
    SensitiveWordService sensitiveWordService;
    @Autowired
    SensitiveHistService sensitiveHistService;
    @Autowired
    GovService govService;
    @Autowired
    CommentToMapBuilder commentToMapBuilder;
    @Autowired
    ScheduleUrlService scheduleUrlService;

    /**
     * 获取单个主题应用的政务列表
     * @param cityCode 城市标识
     * @param titleName 主题名称
     * @return 政务一览
     */
    @ResponseBody
    @RequestMapping(value = "/govList", method = {RequestMethod.GET})
    public Object govList(@RequestParam String cityCode, @RequestParam(required = false)String titleName) {
        return itemService.govList(cityCode, titleName, getPageBounds());
        /*List<Map<String, Object>> list = new ArrayList<>();
        if(titleName != null && !"".equals(titleName) && "综合其他".equals(titleName)){
            list = itemService.govOtherList(cityCode, getPageBounds());
        }else{
            list = itemService.govList(cityCode, titleName, getPageBounds());
        }
            return list;*/
    }

    /**
     * 政务评论页的政务详情
     * @param govId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "{govId}/govView")
    public Object newsView(@PathVariable Long govId){
        return itemService.get(govId);
    }

    /**
     * 获取政务详情
     * @param govId
     * @return 政务详情
     */
    @ResponseBody
    @RequestMapping(value = "/govDetail", method = {RequestMethod.GET})
    public Object govDetail(Long govId, String lat, String lng) {
        Long deviceId = getDeviceId();
        Long custId = getLoginUserId();
        Map<String, Object> result =null;
        if(!BeanUtil.isNullString(lat)&&!BeanUtil.isNullString(lng)){
        	if(lat.startsWith("0.0")&&lng.startsWith("0.0")){
	            lat = null;
	    		lng = null;
	    	}
        }
        
        if(custId!=null){
           result  = itemService.govDetail(deviceId, custId, govId, lat, lng);
        }else{
           result  = itemService.govDetail(deviceId, govId, lat, lng);
        }
        List<Map<String, Object>> tmpHall = (List<Map<String, Object>> )result.get("hall");
        // 特殊处理 当有用户坐标数据时 进行内容转化
        if(tmpHall != null){
            for (Map<String, Object> window : tmpHall){
                if(window.get("distance") == null){
                	window.put("distance", "");
                    continue;
                }
                window.put("distance", IcityUtils.getDistance(Double.parseDouble(window.get("distance").toString())));
            }
            result.put("hall", tmpHall);
        }
        return result;
    }

    /**
     * 进度查询接口
     * @param phoneNum 手机号码
     * @return 查询进度一览
     */
    @ResponseBody
    @RequestMapping(value = "/getAllBusinessInfo", method = {RequestMethod.GET})
    public Object getAllBusinessInfo(String phoneNum,String cityCode) {
		JsonResultModel model = getJsonResultModel();
        // 参数设置
        //Map<?, ?> map = scheduleUrlService.getByCitycode(cityCode);
        //String url = "http://202.110.200.199:8137/icity/c/api.wechat/getBusinessSearchQuery"; //map.get("url").toString();
        Map<String,String> params = new HashMap<String,String>();
        params.put("phone",phoneNum);
        params.put("page","1");
        params.put("rows","10");
        String response = null;
        List<Map<String, Object>> bussinessList = new ArrayList<>();
        try {
            response = HttpUtil.post(progressUrl, params);
            // 转换JSON对象
            JSONObject jsonResult = JsonUtil.strToJson(response);
             if(jsonResult.getString("state").equalsIgnoreCase("1")){
	            // 获取事项列表
	            JSONArray bussinessArrary = jsonResult.getJSONArray("data");
	            // 解析事项列表
	            if(bussinessArrary != null){
	                for (int i = 0;i < bussinessArrary.size();i++){
	                    // 存放单个事项
	                    Map<String, Object> bussiness = Maps.newHashMap();
	                    // 获取单个事项
	                    JSONObject bussinessJson = (JSONObject) bussinessArrary.get(i);
	
	                    // 解析事项基本信息
	                    if(bussinessJson.containsKey("INDEX")){
	                        JSONObject indexObject = bussinessJson.getJSONObject("INDEX");
	                        // 受理编号
	                        if(indexObject.containsKey("RECEIVE_NUMBER")){
	                            bussiness.put("RECEIVE_NUMBER", indexObject.getString("RECEIVE_NUMBER"));
	                        }else{
	                            bussiness.put("RECEIVE_NUMBER", "");
	                        }
	                        // 事项名称
	                        if(indexObject.containsKey("ITEM_NAME")){
	                            bussiness.put("ITEM_NAME", indexObject.getString("ITEM_NAME"));
	                        }else{
	                            bussiness.put("ITEM_NAME", "");
	                        }
	                        // 承诺时间
	                        if(indexObject.containsKey("TIME_LIMIT")){
	                            bussiness.put("TIME_LIMIT", indexObject.getString("TIME_LIMIT"));
	                        }else{
	                            bussiness.put("TIME_LIMIT", "");
	                        }
	                        // 法定期限
	                        if(indexObject.containsKey("LAW_TIME")){
	                            bussiness.put("LAW_TIME", indexObject.getString("LAW_TIME"));
	                        }else{
	                            bussiness.put("LAW_TIME", "");
	                        }
	                        //当前状态
	                        if(indexObject.containsKey("STATE")){
	                            bussiness.put("STATE", indexObject.getString("STATE"));
	                        }else{
	                            bussiness.put("STATE", "");
	                        }
	                    }else{
	                        bussiness.put("RECEIVE_NUMBER", "");
	                        bussiness.put("ITEM_NAME", "");
	                        bussiness.put("TIME_LIMIT", "");
	                        bussiness.put("LAW_TIME", "");
	                        bussiness.put("STATE", "");
	                    }
	
	                    // 存放事项的进度列表
	                    List<Map<String, Object>> courseList = new ArrayList<>();
	
	                    if(bussinessJson.containsKey("COURSELIST")) {
	                        // 获取事项的进度列表
	                        JSONArray courseArrary = bussinessJson.getJSONArray("COURSELIST");
	                        // 解析事项的进度列表
	                        if (courseArrary != null) {
	                            for (int j = 0; j < courseArrary.size(); j++) {
	                                // 存放单条进度的信息
	                                Map<String, Object> course = Maps.newHashMap();
	                                // 获取单条进度的信息
	                                JSONObject courseJson = (JSONObject) courseArrary.get(j);
	
	                                // 解析单条进度的信息
	                                // 环节名称
	                                if (courseJson.containsKey("CURRENT_NODE_NAME")) {
	                                    course.put("CURRENT_NODE_NAME", courseJson.getString("CURRENT_NODE_NAME"));
	                                } else {
	                                    course.put("CURRENT_NODE_NAME", "");
	                                }
	                                // 环节处理人
	                                if (courseJson.containsKey("USER_NAME")) {
	                                    course.put("USER_NAME", courseJson.getString("USER_NAME"));
	                                } else {
	                                    course.put("USER_NAME", "");
	                                }
	                                // 环节提交时间
	                                if (courseJson.containsKey("SEND_TIME")) {
	                                    course.put("SEND_TIME", courseJson.getString("SEND_TIME"));
	                                } else {
	                                    course.put("SEND_TIME", "");
	                                }
	                                // 环节完成时间
	                                if (courseJson.containsKey("FINISH_TIME")) {
	                                    course.put("FINISH_TIME", courseJson.getString("FINISH_TIME"));
	                                } else {
	                                    course.put("FINISH_TIME", "");
	                                }
	                                courseList.add(course);
	                            }
	                        }
	                    }
	                    bussiness.put("COURSELIST", courseList);
	                    bussinessList.add(bussiness);
	                }
	             }
	            model.setCode("0000");
				model.setError("");
				model.setResult(bussinessList);
				model.setMessage("调用成功");
				model.setState("1");
            }else{
            	model.setCode("0000");
			    model.setError("");
			    model.setResult(bussinessList);
			    model.setMessage("未查询到业务信息");
				model.setState("1"); 
            }
        } catch (Exception e){
        	model.setCode("0100");
	     	model.setError(e.toString());
	     	model.setResult(bussinessList);
	        model.setMessage("调用失败");
	     	model.setState("0");  
        }
        return model;
    }

    /**
     * 政务首页推荐事项
     * @param cityCode
     * @return  推荐事项一览
     */
    @ResponseBody
    @RequestMapping(value = "/banners", params = {"cityCode"})
    public Object findBannersByCityCode(String cityCode){
        return govRecommentService.findBannersByCityCode(cityCode);
    }

    /**
     * 政务首页推荐应用
     * @param cityCode
     * @return 推荐应用一览
     */
    /*@ResponseBody
    @RequestMapping(value = "/recommendGuides", params = {"cityCode"})
    public Object findRecommendGuides(String cityCode){
        return appRecommentService.findRecommendGuides(cityCode, getPageBounds());
    }*/

    /**
     * 政务首页推荐应用（新）
     * @param cityCode
     * @return 推荐应用一览
     */
    @ResponseBody
    @RequestMapping(value = "/recommendGuides", params = {"cityCode"})
    public Object findRecommendGuides(String cityCode) {
        return appRecommentService.findGovAppGuide(cityCode, getPageBounds());
    }

    /**
     * 政务首页大厅服务应用
     * @param cityCode
     * @return 大厅服务应用一览
     */
    /*@ResponseBody
    @RequestMapping(value = "/serviceApps", params = {"cityCode"})
    public Object findServiceApps(String cityCode){
        return appRecommentService.findServiceApps(cityCode, getPageBounds());
    }*/

    /**
     * 政务首页大厅服务应用（新）
     * @param cityCode
     * @return 大厅服务应用一览
     */
    @ResponseBody
    @RequestMapping(value = "/serviceApps", params = {"cityCode"})
    public Object findServiceApps(String cityCode) {
        return appRecommentService.findServiceApp(cityCode, getPageBounds());
    }

    /**
     * 政务详情页有用动作接口
     * @param govId 政务主键
     */
    @ResponseBody
    @RequestMapping(value = "/useful", method = RequestMethod.POST)
    public void govUseful(@RequestParam Long govId) {
        Long deviceId = getDeviceId();
        Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_GOV, govId);
        if (praiseOld != null) {
            throw new ApplicationException(900,"您已执行过此操作，不能再次执行！");
        }
        try{
            Praise praise = new Praise();
            praise.setCustId(getLoginUserId());
            praise.setDeviceId(deviceId);
            praise.setObjectType(Constants.TYPE_GOV);
            praise.setObjectId(govId);
            praise.setIsUseful(1l);
            praiseService.add(praise);
        }catch(Exception e){
            throw new ApplicationException(900,"操作失败");
        }
    }
    /**
     * 取消政务详情页有用接口
     * @param consultId
     */
    @ResponseBody
    @RequestMapping(value = "/delUseful", method = RequestMethod.POST)
    public void delUseful(@RequestParam Long govId) {
        Long deviceId = getDeviceId();
        try {
           praiseService.removeByObjectId(deviceId, Constants.TYPE_GOV, govId);;
        }catch (Exception e){
            throw new ApplicationException(900,"操作失败");
        }
    }
    /**
     * 政务详情页无用动作接口
     * @param govId 政务主键
     */
    @ResponseBody
    @RequestMapping(value = "/useless", method = RequestMethod.POST)
    public void govUseless(@RequestParam Long govId) {
        Long deviceId = getDeviceId();
        Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_GOV, govId);
        if (praiseOld != null) {
            throw new ApplicationException(900,"您已执行过此操作，不能再次执行！");
        }
        try {
            Praise praise = new Praise();
            praise.setCustId(getLoginUserId());
            praise.setDeviceId(getDeviceId());
            praise.setObjectType(Constants.TYPE_GOV);
            praise.setObjectId(govId);
            praise.setIsUseful(0l);
            praiseService.add(praise);
        }catch (Exception e){
            throw new ApplicationException(900,"操作失败");
        }
    }

    /**
     * 政务收藏接口（登录）
     * @param govId 政务主键
     */
    @ResponseBody
    @RequestMapping(value = "/{govId}/favorite", method = RequestMethod.POST)
    public void favorite(@PathVariable Long govId) {
        if(getLoginUserId() != null) {
            try{
                Gov gov = new Gov();
                gov.setCustId(getLoginUserId());
                gov.setDeviceId(getDeviceId());
                gov.setGovId(govId);
                govService.addByCustId(gov);
            }catch (Exception e){
                throw new ApplicationException(900,"操作失败");
            }
        }else {
            throw new ApplicationException(900,"您还没有登录，不能使用收藏功能");
        }
    }

    /**
     * 政务取消收藏接口（登录）
     * @param govId 政务主键
     */
    @ResponseBody
    @RequestMapping(value = "/{govId}/cancelFavorite", method = RequestMethod.POST)
    public void cancelFavorite(@PathVariable Long govId) {
        if(getLoginUserId() != null) {
            try{
                govService.removeBycondition(getLoginUserId(), govId);
            }catch (Exception e){
                throw new ApplicationException(900,"您还没有登陆，不能使用取消收藏功能");
            }
        }else {
            throw new ApplicationException(900,"您还没有登陆，不能使用取消收藏功能");
        }
    }

    /**
     * 政务发表评论(登录)
     * @param objectId  政务主键
     * @param comment  评论内容
     * @param commentId  评论主键
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/addComment/{objectType}/{objectId}", method= RequestMethod.POST)
    public Object comment(@PathVariable String objectType,@PathVariable Long objectId, @RequestParam String comment,
                          @RequestParam(required = false) Long commentId) throws Exception {
        if(getLoginUserId() != null) {
            String matchWord = addComment(objectType, objectId, comment, commentId);
            if (matchWord != null) {
                throw new ApplicationException(900,"您发布的评论内容中包含敏感文字，请重新编辑后再提交。");
            }
        }else {
            throw new ApplicationException(900,"您还没有登陆，不能进行评论");
        }
        return "";
    }


    /**
     * 向评论表添加数据
     * @param objectId  政务主键
     * @param comment  评论内容
     * @param commentId  评论主键
     * @return
     */

    public String addComment(String objectType, Long objectId, String comment, Long commentId) {
        Long custId = getLoginUserId();
        Comment comments = new Comment();
        comments.setCustId(custId);
        comments.setObjectType(objectType);
        comments.setObjectId(objectId);
        comments.setCommentId(commentId);
        comments.setComment(comment);
        comments.setIp(getRequest().getRemoteAddr());
        comments.setDeleted(0l);
        commentService.add(comments);
        return checkSensitiveWord(objectType, comments.getId(), comment);
    }
    /**
     * 判断敏感词，伪删除路况,并且添加到敏感词记录表中
     * @param objectId  政务主键
     * @param content  评论内容
     * @param objectType
     * @return
     */

    private String checkSensitiveWord(String objectType, Long objectId, String content) {
        List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
        content = StringUtils.defaultString(content);
        for (SensitiveWords sensitiveWord : sensitiveWords) {
            Boolean hasSensitiveWord = content.contains(sensitiveWord.getWord());
            if (hasSensitiveWord) {
                commentService.deleteMyComment(objectId);
                //添加到敏感词记录表中
                SensitiveHist sensitiveHist = new SensitiveHist();
                sensitiveHist.setCustId(getLoginUserId());
                sensitiveHist.setObjectType(objectType);
                sensitiveHist.setObjectId(objectId);
                sensitiveHist.setSensitiveWord(sensitiveWord.getWord());
                sensitiveHistService.add(sensitiveHist);
                return sensitiveWord.getWord();
            }
        }
        return null;
    }
    /**
     * 办事指南搜索
     * @param key 搜索关键词
     * @param cityCode 城市标识
     * @return 办事指南搜索结果列表
     */
    @ResponseBody
    @RequestMapping(value = "/search", params = {"key","cityCode"})
    public Object search(String key,String cityCode) {
        List<Item> list = Lists.newArrayList();
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(cityCode)) {
            list = itemService.search(key,cityCode,getPageBounds());
        }
        return list;
    }

    /**
     * 获取政务的评论列表
     * @param govId
     * @return 评论列表一览
     */
    @ResponseBody
    @RequestMapping(value = "/{govId}/comments")
    public Object newsComments (@PathVariable Long govId) {
        return map(commentService.eachModuleComments("gov", govId, getPageBounds()), comment -> {
            return commentToMapBuilder.build((Comment) comment,getDeviceId(), getLoginUserId());
        });
    }
    /**
     * 获取预约部门列表
     * @param service
     * @param reserve
     * @return
     */
    /*@ResponseBody
    @RequestMapping(value = "/getBizDepts", method = RequestMethod.POST)
	public Object getBizDepts(){
    	List<Object> listJson = new ArrayList<Object>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(listJson);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("map", "{\"Service\":\"Queue.GetBizDepts\",\"Reserve\":\"true\"}");
		try {
			String response = HttpUtil.post(Lobby_URL, params);
			JSONObject jsonStr = JsonUtil.strToJson((Object)response);
			JSONObject data = jsonStr.getJSONObject("data");
			if (data.has("depts")) {
				JSONArray listArray = data.getJSONArray("depts");
				for (int i = 0; i < listArray.size(); i++) {
	            	   Map<String,Object> map = new HashMap<String, Object>();
		               JSONObject obj = JSONObject.fromObject(listArray.getString(i));
		               System.out.println("DeptName=="+obj.get("DeptName"));
		               map.put("DeptID",obj.get("DeptID"));
		               map.put("DeptName",obj.get("DeptName"));
		               map.put("DeptNameEng",obj.get("DeptNameEng"));
		               map.put("Enable",obj.get("Enable"));
		               listJson.add(map);
		         }
				model.setCode("0000");
				model.setError("");
				model.setResult(listJson);
				model.setMessage("调用成功");
				model.setState("1");
			}
		} catch (Exception e) {
			model.setCode("0100");
	     	model.setError(e.toString());
	        model.setMessage("调用失败");
	     	model.setState("0"); 
		}
		return model;
    }*/
    /**
     * 根据部门名称获取可预约业务
     * @param DeptName
     * @return
     */
    /*@ResponseBody
    @RequestMapping(value = "/getListBusiness", method = RequestMethod.POST)
	public Object getListBusiness(String deptName){
    	List<Object> listJson = new ArrayList<Object>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(listJson);
		if(!BeanUtil.isNullString(deptName)){
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("map", "{\"Service\":\"Reserve.ListBusiness\",\"DeptName\":\""+deptName+"\"}");
			try {
				String response = HttpUtil.post(Lobby_URL, params);
				JSONObject jsonStr = JsonUtil.strToJson((Object)response);
				JSONObject data = jsonStr.getJSONObject("data");
				if(data !=null){
					   String success = data.getString("Succ");
					   if(!BeanUtil.isNullString(success)&&success.equalsIgnoreCase("1")){
						   if (data.has("Biz")) {
							    JSONArray listArray = data.getJSONArray("Biz");
								for (int i = 0; i < listArray.size(); i++) {
					            	   Map<String,Object> map = new HashMap<String, Object>();
						               JSONObject obj = JSONObject.fromObject(listArray.getString(i));
						               map.put("DeptName",obj.get("DeptName"));
						               map.put("BizName",obj.get("BizName"));
						               map.put("BizID",obj.get("BizID"));
						               if(obj.has("TimeConfig")){
						            	   JSONArray listTime= obj.getJSONArray("TimeConfig");
						            	   List<Map<String,String>> list = new ArrayList<Map<String,String>>();
						            	   for(int n = 0;n<listTime.size();n++){
						            		   Map<String,String> mapTime = new HashMap<String, String>();
						            		   JSONObject objTime = JSONObject.fromObject(listTime.getString(n));
						            		   mapTime.put("YYETime", objTime.get("YYETime").toString());
						            		   mapTime.put("YYMax", objTime.get("YYMax").toString());
						            		   mapTime.put("YYSTime", objTime.get("YYSTime").toString());
						            		   list.add(mapTime);
						            	   }
						            	   map.put("TimeConfig", list);
						               }
						               listJson.add(map);
						         }
						     }
					   }
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(listJson);
				model.setMessage("调用成功");
				model.setState("1");
				
			} catch (Exception e) {
				model.setCode("0100");
		     	model.setError(e.toString());
		        model.setMessage("调用失败");
		     	model.setState("0");
			}
		}else{
			model.setCode("0203");
			model.setError("参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
		}
    	return model;
    }*/
    /**
     * 获取业务已预约数
     * @param date
     * @param bizId
     * @return
     */
    /*@ResponseBody
    @RequestMapping(value = "/getRecordCount", method = RequestMethod.POST)
	public Object getRecordCount(String date,String bizId){
    	List<Object> listJson = new ArrayList<Object>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(listJson);
		if(!BeanUtil.isNullString(date)&&!BeanUtil.isNullString(bizId)&&DateUtil.isValidDate(date,"yyyy-MM-dd")){
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("map","{\"Service\":\"Reserve.GetRecordCount\",\"YYDate\":\""+date+"\",\"BizID\":\""+bizId+"\"}");
			try {
				String response = HttpUtil.post(Lobby_URL,params);
				JSONObject jsonStr = JsonUtil.strToJson((Object)response);
				JSONObject data = jsonStr.getJSONObject("data");
				if(data !=null){
					String success = data.getString("Succ");
					if(!BeanUtil.isNullString(success)&&success.equalsIgnoreCase("1")){
						JSONArray listArray = data.getJSONArray("Record");
						for (int i = 0; i < listArray.size(); i++) {
							   Map<String,Object> map = new HashMap<String, Object>();
				               JSONObject obj = JSONObject.fromObject(listArray.getString(i));
				               map.put("YYDate",obj.get("YYDate"));
				               map.put("BizID ",obj.get("BizID"));
				               if(obj.has("TimeRecord")){
				            	   JSONArray listTime= obj.getJSONArray("TimeRecord");
				            	   List<Map<String,String>> list = new ArrayList<Map<String,String>>();
				            	   for(int n = 0;n<listTime.size();n++){
				            		   Map<String,String> mapTime = new HashMap<String, String>();
				            		   JSONObject objTime = JSONObject.fromObject(listTime.getString(n));
				            		   mapTime.put("Time", objTime.get("Time").toString());
				            		   mapTime.put("Count", objTime.get("Count").toString());
				            		   list.add(mapTime);
				            	   }
				            	   map.put("TimeRecord", list);
				               }
				               listJson.add(map);
						}
					}
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(listJson);
				model.setMessage("调用成功");
				model.setState("1");
				
			} catch (Exception e) {
				model.setCode("0100");
		     	model.setError("系统未知异常！");
		        model.setMessage("调用失败");
		     	model.setState("0");
			}
		}else{
			model.setCode("0203");
			model.setError("参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
    }*/
    /**
     * 添加预约信息
     * @param date
     * @param bizId
     * @param time
     * @param idCard
     * @param phone
     * @return
     */
   /* @ResponseBody
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
	public Object addRecord(String date,String bizId,String time,String idCard,String phone){
    	List<Object> listJson = new ArrayList<Object>();
		JsonResultModel model =getJsonResultModel();
		model.setResult(listJson);
		if(!BeanUtil.isNullString(date)&&!BeanUtil.isNullString(bizId)
				&&DateUtil.isValidDate(date+" "+time,"yyyy-MM-dd HH:mm")&&!BeanUtil.isNullString(time)
				&&!BeanUtil.isNullString(idCard)&&!BeanUtil.isNullString(phone)){
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("map","{\"Service\":\"Reserve.AddRecord\",\"BizID\":\""+bizId+"\",\"Date\":\""+date+"\",\"Time\":\""+time+"\",\"IDCard\":\""+idCard+"\",\"Phone\":\""+phone+"\"}");
			try {
				String response = HttpUtil.post(Lobby_URL,params);
				JSONObject jsonStr = JsonUtil.strToJson((Object)response);
				JSONObject data = jsonStr.getJSONObject("data");
				if(data !=null){
					String success = data.getString("Succ");
					String message = data.getString("Msg");
					if(!BeanUtil.isNullString(success)){
						Map<String,Object> map = new HashMap<String, Object>();
			            map.put("Msg",message);
			            map.put("Succ",success);
			            listJson.add(map);
			            model.setCode("0000");
						model.setError("");
						model.setResult(listJson);
						model.setMessage("调用成功");
						model.setState("1");
					}else{
						model.setCode("0400");
				     	model.setError("第三方接口调用异常");
				        model.setMessage("调用失败");
				     	model.setState("0");
					}
				}else{
					model.setCode("0400");
			     	model.setError("第三方接口调用异常");
			        model.setMessage("调用失败");
			     	model.setState("0");
				}
			} catch (Exception e) {
				model.setCode("0100");
		     	model.setError("系统未知异常！");
		        model.setMessage("调用失败");
		     	model.setState("0");
			}
			
		}else{
			model.setCode("0203");
			model.setError("参数缺失");
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
    }*/
}
