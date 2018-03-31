package com.inspur.icity.web.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.cust.model.CollectionQuestion;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.cust.model.GovQuestion;
import com.inspur.icity.logic.cust.model.Question;
import com.inspur.icity.logic.cust.service.CollectionQuestionService;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.GovAnswerService;
import com.inspur.icity.logic.cust.service.GovQuestionService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.cust.service.QuestionService;
import com.inspur.icity.logic.sensitive.model.SensitiveHist;
import com.inspur.icity.logic.sensitive.model.SensitiveWords;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.cust.builder.CommentToMapBuilder;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import com.inspur.icity.logic.cust.model.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 咨询、投诉
 */
@Controller
@RequestMapping(value = "/consult")
public class ConsultController extends BaseAuthController {

    @Autowired
    QuestionService questionService;
    @Autowired
    DictService dictService;
    @Autowired
    CommentService commentService;
    @Autowired
    CommentToMapBuilder commentToMapBuilder;
    @Autowired
    CollectionQuestionService collectionQuestionService;
    @Autowired
    SensitiveWordService sensitiveWordService;
    @Autowired
    SensitiveHistService sensitiveHistService;
    @Autowired
    PraiseService praiseService;
    @Autowired
    GovAnswerService govAnswerService;
    @Autowired
    GovQuestionService govQuestionService;

    private	final String CONSULT_URL_TEST = "http://60.31.98.34:8089/icity/c/api.inlcity/iCityAddGuestBook";
    private	final String CONSULT_URL_OFFICIAL = "http://123.178.103.151:80/icity/c/api.inlcity/iCityAddGuestBook";
    Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 咨询搜索
     * @param key
     * @param cityCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/search", params = {"key", "cityCode"})
    public Object search(String key, String cityCode) {
    	logger.info("----------------------search(start)-------------------"+
    		"fromModule:ConsultController|interfaceInfo:咨询搜索|"+"key:"+key+"|cityCode:"+cityCode);
        List<Question> list = Lists.newArrayList();
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(cityCode)) {
            list = questionService.search(key, cityCode, getPageBounds());
        }
        return list;
    }

    /**
     * 咨询部门
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/departments")
    public Object getDepartments() {
        return dictService.findByType("");
    }

    /**
     * 咨询和投诉的评论列表
     * @param consultId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{consultId}/comments")
    public Object newsComments(@PathVariable Long consultId) {
    	logger.info("----------------------newsComments(start)-------------------"+
    		"fromModule:ConsultController|interfaceInfo:咨询和投诉的评论列表|"+"consultId:"+consultId);
        return map(commentService.eachModuleComments(Constants.TYPE_CONSULTS, consultId, getPageBounds()), comment -> {
            return commentToMapBuilder.build((Comment) comment, getDeviceId(), getLoginUserId());
        });
    }

    /**
     * 咨询回复详情的咨询详情
     * @param consultId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{consultId}/consultView")
    public Object answerView(@PathVariable Long consultId) {
    	logger.info("----------------------answerView(start)-------------------"+
    		"fromModule:ConsultController|interfaceInfo:咨询回复详情的咨询详情|"+"consultId:"+consultId);
        Map<String, Object> map = Maps.newHashMap();
        Question question =questionService.get(consultId);
        map.put("id",question.getId());
        map.put("title",question.getTitle());
        map.put("content",question.getContent());
        map.put("createTime",question.getCreateTime());
        map.put("usefulCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS,1l,consultId));
        map.put("uselessCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS, 0l, consultId));
        map.put("commentCount",commentService.findCommentCount(Constants.TYPE_CONSULTS,consultId));
        String hasPraised = praiseService.hasPraised(getDeviceId(), null, Constants.TYPE_CONSULTS, consultId);
        logger.info("----------------------answerView------hasPraised:"+hasPraised);
        if("1".equals(hasPraised)){
            map.put("usefulStatus",1);
            map.put("uselessStatus",0);
        }else if("0".equals(hasPraised)){
            map.put("usefulStatus",0);
            map.put("uselessStatus",1);
        }else{
            map.put("usefulStatus",0);
            map.put("uselessStatus",0);
        }
        CollectionQuestion collectionQuestion = collectionQuestionService.hasCollectioned(getLoginUserId(),consultId);
        if(collectionQuestion != null){
            map.put("hasCollectioned",1);
        }else {
            map.put("hasCollectioned",0);
        }
        return map;
    }


    /**
     * 在线咨询（登录）
     * @param
     * @return result
     */
    @ResponseBody
    @RequestMapping(value = "/addConsult", method = RequestMethod.POST)
    public Object addConsult(String jsonStr){
    	logger.info("--------------addConsult(start)-------------"+"|"+"fromModule:ConsultController"+"|"+"jsonStr:"+jsonStr);
		JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
       	JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
        Long loginNum = getLoginUserId();
        logger.info("-------------------------------loginNum : " + loginNum);
        if(!BeanUtil.isNullString(jsonStr)){
        	if(jsonObject.has("departName") && jsonObject.has("userName") && jsonObject.has("phone") && jsonObject.has("title")
        			&& jsonObject.has("content") && jsonObject.has("regionId") && jsonObject.has("type") && jsonObject.has("ly") && jsonObject.has("open")
        			&& jsonObject.has("departId") && jsonObject.has("sxbm") && jsonObject.has("sxmc") && jsonObject.has("sxId") && jsonObject.has("busiId")){
        		String departName = jsonObject.getString("departName");
        		String userName = jsonObject.getString("userName");
        		String phone = jsonObject.getString("phone");
        		String title = jsonObject.getString("title");
        		String content = jsonObject.getString("content").replaceAll("\r|\n", "");
        		String regionId = jsonObject.getString("regionId");
        		String type = jsonObject.getString("type");
        		String ly = jsonObject.getString("ly");
        		String open = jsonObject.getString("open");
        		String departId = jsonObject.getString("departId");
        		String sxbm = jsonObject.getString("sxbm");
        		String sxmc = jsonObject.getString("sxmc");
        		String sxId = jsonObject.getString("sxId");
        		String busiId = BeanUtil.nullValueOf(jsonObject.getString("busiId")); 
        		
        		logger.info("----------------------开始解析jsonStr-----------|departName:" + departName + ",userName:" + userName + ",phone" + phone 
        				+ ",title:" + title + ",content:" + content + ",sxbm:" + sxbm + ",regionId:" + regionId + ",:open:" + open 
        				+ ",type:" + type + ",ly:" + ly + ",departId:" + departId + ",sxmc:" + sxmc + ",sxId:" + sxId + ",busiId:" + busiId);
         		if(loginNum != null){
	        		if(!BeanUtil.isNullString(departName) && !BeanUtil.isNullString(userName) || !BeanUtil.isNullString(phone) || !BeanUtil.isNullString(title) || !BeanUtil.isNullString(content)
	         				|| !BeanUtil.isNullString(sxbm) || !BeanUtil.isNullString(regionId) || !BeanUtil.isNullString(type) || !BeanUtil.isNullString(ly) 
	         				|| !BeanUtil.isNullString(departId) || !BeanUtil.isNullString(sxmc) || !BeanUtil.isNullString(sxId) || !BeanUtil.isNullString(open)){
	        			HashMap<String, String> params = new HashMap<String, String>();
	        			params.put("where", "DEPART_NAME,USERNAME,PHONE,TITLE,CONTENT,REGION_ID,TYPE,LY,DEPART_ID,SXBM,SXMC,SXID,OPEN");
	        			params.put("params", "[\'" + departName + "\',\'" + userName + "\',\'" + phone + "\',\'" + title + "\',\'" + content + "\',\'" + regionId + "\',\'" 
	        			+ type + "\',\'" + ly + "\',\'" + departId + "\',\'" + sxbm + "\',\'" + sxmc + "\',\'" + sxId + "\',\'" + open + "\']");
	        			//此处连接至满洲里第三方接口
	        			logger.info("------------------------------传入第三方接口的参数----|where:" + params.get("where") + ",params:" + params.get("params"));
	        			try {
	        				String CONSULT_URL = "";
	        				if(Config.getValue("currentDeployment").equalsIgnoreCase("test")){
	        					CONSULT_URL = CONSULT_URL_TEST;
	        				}
	        				if(Config.getValue("currentDeployment").equalsIgnoreCase("official")){
	        					CONSULT_URL = CONSULT_URL_OFFICIAL;
	        				}
	        				String response = HttpUtil.post(CONSULT_URL, params);
	        				JSONObject jsonObj = JsonUtil.strToJson((Object)response);
	        				logger.info("------------------------------------reponse:" + response);
	        				if(!BeanUtil.isNullString(response) && jsonObj.has("state") && jsonObj.getString("state").equalsIgnoreCase("1") && jsonObj.has("data") && !BeanUtil.isNullString(jsonObj.getString("data"))){
	        					JSONObject tempObj = JsonUtil.strToJson((Object)jsonObj.getString("data"));
	        					logger.info("---------------------tempObj: " + tempObj + ",ID:" + tempObj.getString("ID"));
	        					if(!tempObj.has("ID") || BeanUtil.isNullString(tempObj.getString("ID"))){
	        						logger.error("----------------------------第三方返回值缺失ID值---------------------------");
									model.setCode("0100");
							     	model.setError("第三方接口调用异常！");
							        model.setMessage("调用失败");
							        model.setResult(list);
							     	model.setState("0"); 
							     	return model;
	        					}
	        					try {
		        					//高恒 2016年7月18日添加敏感词过滤
		        					if(!checkSensitiveWord("addConsult",title)&&!checkSensitiveWord("addConsult",content)){
		    	        	            GovQuestion govQusetion = new GovQuestion();
		    	        	            govQusetion.setType(Constants.TYPE_CONSULTS);
		    	        	            govQusetion.setDepartId(departId);
		    	        	            govQusetion.setDepartName(departName);
		    	        	            govQusetion.setTitle(title);
		    	        	            govQusetion.setContent(content);
		    	        	            govQusetion.setCustId(getLoginUserId());
		    	        	            govQusetion.setUserName(userName);
		    	        	            govQusetion.setTelephone(phone);
		    	        	            govQusetion.setLy(ly);
		    	        	            govQusetion.setSxbm(sxbm);
		    	        	            govQusetion.setSxId(sxId);
		    	        	            govQusetion.setSxmc(sxmc);
		    	        	            govQusetion.setRegionId(regionId);
		    	        	            govQusetion.setBusiId(busiId);
		    	        	            govQusetion.setOpen(open);
		    	        	            govQusetion.setVerifyId(tempObj.getString("ID"));
		    	        	            govQuestionService.add(govQusetion);
		    	        	            model.setState("1");
		    		        			model.setCode("0000");
		    		        			model.setError("");
		    		        			model.setResult(list);
		    		        			model.setMessage("调用成功");
		    		        			return model;
		    	                    }
								} catch (Exception e) {
									// TODO: handle exception
									logger.error("-----------------插入数据库出现异常(error)|fromModule:ConsultController|interfaceInfo:在线咨询（登录）|error:"+e.toString());
									model.setCode("0100");
							     	model.setError("系统未知异常！");
							        model.setMessage("调用失败");
							        model.setResult(list);
							     	model.setState("0"); 
							     	return model;
								}
	        				}else {
	        					logger.error("----------------------------第三方接口调用失败，不执行插入数据库---------------------------");
								model.setCode("0400");
						     	model.setError("第三方接口调用失败");
						        model.setMessage("调用失败");
						        model.setResult(list);
						     	model.setState("0"); 
						     	return model;
							}
	        				
						} catch (Exception e) {
							// TODO: handle exception
							logger.error("-------------------------第三方接口调用异常error:"+e.toString());
							model.setCode("0100");
					     	model.setError("系统未知异常！");
					        model.setMessage("调用失败");
					        model.setResult(list);
					     	model.setState("0"); 
					     	return model;
						}
	        		}else{
	        			logger.error("---------------------------jsonStr中缺失必填项-----------------------------");
	        			model.setState("0");
	        			model.setCode("203");
	        			model.setError("参数缺失");
	        			model.setResult(list);
	        			model.setMessage("调用失败");
	        			return model;
	        		}
                }else {
                    throw new ApplicationException(900,"您还没有登录，不能使用在线咨询功能");
                }
        	}else{
        		logger.error("---------------------------jsonStr格式异常----------------------------");
        		model.setState("0");
    			model.setCode("202");
    			model.setError("参数格式异常");
    			model.setResult(list);
    			model.setMessage("调用失败");
    			return model;
        	}
        }else {
        	logger.error("------------------------------jsonStr值为NULL-------------------------------");
			model.setState("0");
			model.setCode("203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			return model;
		}
        return model;
    }

    /**
     * 在线投诉（登录）
     * @return result
     */
    @ResponseBody
    @RequestMapping(value = "/addComplain", method = RequestMethod.POST)
    public Object addComplain(String jsonStr){
    	logger.info("--------------addComplain(start)-------------"+"|"+"fromModule:ConsultController"+"|"+"jsonStr:"+jsonStr);
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
       	JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
        Long loginNum = getLoginUserId();
        logger.info("-------------------------------loginNum : " + loginNum);
        if(!BeanUtil.isNullString(jsonStr)){
        	if(jsonObject.has("departName") && jsonObject.has("userName") && jsonObject.has("phone") && jsonObject.has("title")
        			&& jsonObject.has("content") && jsonObject.has("regionId") && jsonObject.has("type") && jsonObject.has("ly") && jsonObject.has("open") 
        			&& jsonObject.has("departId") && jsonObject.has("sxbm") && jsonObject.has("sxmc") && jsonObject.has("sxId") && jsonObject.has("busiId")){
        		String departName = jsonObject.getString("departName");
        		String userName = jsonObject.getString("userName");
        		String phone = jsonObject.getString("phone");
        		String title = jsonObject.getString("title");
        		String content = jsonObject.getString("content");
        		String regionId = jsonObject.getString("regionId");
        		String type = jsonObject.getString("type");
        		String ly = jsonObject.getString("ly");
        		String open = jsonObject.getString("open");
        		String departId = jsonObject.getString("departId");
        		String sxbm = BeanUtil.nullValueOf(jsonObject.getString("sxbm"));
        		String sxmc = BeanUtil.nullValueOf(jsonObject.getString("sxmc"));
        		String sxId = BeanUtil.nullValueOf(jsonObject.getString("sxId"));
        		String busiId = jsonObject.getString("busiId");        		
        		logger.info("----------------------开始解析jsonStr-----------|departName:" + departName + ",userName:" + userName + ",phone" + phone 
        				+ ",title:" + title + ",content:" + content + ",regionId:" + regionId + ",type:" + type  + ",open:" + open
        				+ ",ly:" + ly + ",departId:" + departId + ",busiId:" + busiId);
         		if(loginNum != null){
	        		if(!BeanUtil.isNullString(departName) && !BeanUtil.isNullString(userName) || !BeanUtil.isNullString(phone) || !BeanUtil.isNullString(title) || !BeanUtil.isNullString(content) || !BeanUtil.isNullString(open)
	         				|| !BeanUtil.isNullString(regionId) || !BeanUtil.isNullString(type) || !BeanUtil.isNullString(ly) || !BeanUtil.isNullString(departId) || !BeanUtil.isNullString(busiId)){
	        			HashMap<String, String> params = new HashMap<String, String>();
	        			params.put("where", "DEPART_NAME,USERNAME,PHONE,TITLE,CONTENT,REGION_ID,TYPE,LY,DEPART_ID,BUSI_ID,OPEN");
	        			params.put("params", "[\'" + departName + "\',\'" + userName + "\',\'" + phone + "\',\'" + title + "\',\'" + content + "\',\'" +regionId + "\',\'" + type + "\',\'" 
	        			+ ly + "\',\'" + departId + "\',\'" + busiId + "\',\'" + open + "\']");
	        			//此处连接至满洲里第三方接口
	        			logger.info("------------------------------传入第三方接口的参数----|where:" + params.get("where") + ",params:" + params.get("params"));
	        			try {
	        				String CONSULT_URL = "";
	        				if(Config.getValue("currentDeployment").equalsIgnoreCase("test")){
	        					CONSULT_URL = CONSULT_URL_TEST;
	        				}
	        				if(Config.getValue("currentDeployment").equalsIgnoreCase("official")){
	        					CONSULT_URL = CONSULT_URL_OFFICIAL;
	        				}
	        				String response = HttpUtil.post(CONSULT_URL, params);
	        				JSONObject jsonObj = JsonUtil.strToJson((Object)response);
	        				logger.info("------------------------------------reponse:" + response);
	        				if(!BeanUtil.isNullString(response) && jsonObj.has("state") && jsonObj.getString("state").equalsIgnoreCase("1") && jsonObj.has("data") && !BeanUtil.isNullString(jsonObj.getString("data"))){
	        					JSONObject tempObj = JsonUtil.strToJson((Object)jsonObj.getString("data"));
	        					logger.info("---------------------tempObj: " + tempObj + ",ID:" + tempObj.getString("ID"));
	        					if(!tempObj.has("ID") || BeanUtil.isNullString(tempObj.getString("ID"))){
	        						logger.error("----------------------------第三方返回值缺失ID值---------------------------");
									model.setCode("0400");
							     	model.setError("第三方接口调用异常！");
							        model.setMessage("调用失败");
							        model.setResult(list);
							     	model.setState("0"); 
							     	return model;
	        					}
	        					try {
	        						//高恒 2016年7月18日添加敏感词过滤
	        			            if(!checkSensitiveWord("addComplain",title)&&!checkSensitiveWord("addComplain",content)){
	        				            GovQuestion govQuestion = new GovQuestion();
	        				            govQuestion.setType(Constants.TYPE_COMPLAIN);
	        				            govQuestion.setDepartId(departId);
	        				            govQuestion.setDepartName(departName);
	        				            govQuestion.setTitle(title);
	        				            govQuestion.setContent(content);
	        				            govQuestion.setCustId(getLoginUserId());
	        				            govQuestion.setUserName(userName);
	        				            govQuestion.setTelephone(phone);
	        				            govQuestion.setSxbm(sxbm);
	        				            govQuestion.setSxId(sxId);
	        				            govQuestion.setSxmc(sxmc);
		    	        	            govQuestion.setBusiId(busiId);
		    	        	            govQuestion.setLy(ly);
		    	        	            govQuestion.setOpen(open);
		    	        	            govQuestion.setRegionId(regionId);
	        				            govQuestion.setVerifyId(tempObj.getString("ID"));
	        				            govQuestionService.add(govQuestion);
		    	        	            model.setState("1");
		    		        			model.setCode("0000");
		    		        			model.setError("");
		    		        			model.setResult(list);
		    		        			model.setMessage("调用成功");
		    		        			return model;
		    	                    }
								} catch (Exception e) {
									// TODO: handle exception
									logger.error("-------------插入数据库出现异常(error)|fromModule:ConsultController|interface:在线投诉（登录）|error:"+e.toString());
									model.setCode("0100");
							     	model.setError("系统未知异常！");
							        model.setMessage("调用失败");
							        model.setResult(list);
							     	model.setState("0"); 
							     	return model;
								}
	        				}else {
	        					logger.error("----------------------------第三方接口调用失败，不执行插入数据库---------------------------");
								model.setCode("0400");
						     	model.setError("第三方接口调用失败");
						        model.setMessage("调用失败");
						        model.setResult(list);
						     	model.setState("0"); 
						     	return model;
							}
	        				
						} catch (Exception e) {
							// TODO: handle exception
							logger.error("-------------------------第三方接口调用异常error:"+e.toString());
							model.setCode("0100");
					     	model.setError("系统未知异常！");
					        model.setMessage("调用失败");
					        model.setResult(list);
					     	model.setState("0"); 
					     	return model;
						}
	        		}else{
	        			logger.error("---------------------------jsonStr中缺失必填项-----------------------------");
	        			model.setState("0");
	        			model.setCode("203");
	        			model.setError("参数缺失");
	        			model.setResult(list);
	        			model.setMessage("调用失败");
	        			return model;
	        		}
                }else {
                    throw new ApplicationException(900,"您还没有登录，不能使用在线咨询功能");
                }
        	}else{
        		logger.error("---------------------------jsonStr格式异常----------------------------");
        		model.setState("0");
    			model.setCode("202");
    			model.setError("参数格式异常");
    			model.setResult(list);
    			model.setMessage("调用失败");
    			return model;
        	}
        }else {
        	logger.error("------------------------------jsonStr值为NULL-------------------------------");
			model.setState("0");
			model.setCode("203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			return model;
		}
        return model;
    }
       /* if(loginNum != null){
            if (Strings.isNullOrEmpty(deptNo)) {
                result.put("result", "error");
                result.put("object", "deptNo");
                result.put("msg", "请选择部门");
                return result;
            }
            if (Strings.isNullOrEmpty(title)) {
                result.put("result", "error");
                result.put("object", "title");
                result.put("msg", "请填写标题");
                return result;
            }
            if (Strings.isNullOrEmpty(content)) {
                result.put("result", "error");
                result.put("object", "content");
                result.put("msg", "请填写咨询内容");
                return result;
            }
            if (Strings.isNullOrEmpty(name)) {
                result.put("result", "error");
                result.put("object", "name");
                result.put("msg", "请填写姓名");
                return result;
            }
            if (Strings.isNullOrEmpty(telephone)) {
                result.put("result", "error");
                result.put("object", "telephone");
                result.put("msg", "请填写电话");
                return result;
            }
            if (Strings.isNullOrEmpty(idCard)) {
                result.put("result", "error");
                result.put("object", "idCard");
                result.put("msg", "请填写身份证号码");
                return result;
            }
            //高恒 2016年7月18日添加敏感词过滤
            if(!checkSensitiveWord("addComplain",title)&&!checkSensitiveWord("addComplain",content)){
	            Question question = new Question();
	            question.setType(Constants.TYPE_COMPLAIN);
	            question.setDepartment(deptNo);
	            question.setTitle(title);
	            question.setContent(content);
	            question.setCustId(getLoginUserId());
	            question.setName(name);
	            question.setTelephone(telephone);
	            question.setIdCard(idCard);
	            question.setCityCode(cityCode);
	            questionService.add(question);
            }
            result.put("result", "ok");
        }else {
            throw new ApplicationException(900,"您还没有登录，不能使用在线投诉功能");
        }
        return result;
    }*/

    /**
     * 咨询收藏(登录)
     * @param consultId
     */
    @ResponseBody
    @RequestMapping(value = "/{consultId}/favorite", method = RequestMethod.POST)
    public void favoriteConsult(@PathVariable Long consultId) {
    	logger.info("--------------favoriteConsult(start)-------------"+"|"+"fromModule:ConsultController"+"|interfaceInfo:咨询收藏(登录)|"+"consultId:"+consultId);
        if (getLoginUserId() != null) {
            CollectionQuestion collectionQuestion = new CollectionQuestion();
            collectionQuestion.setCustId(getLoginUserId());
            collectionQuestion.setDeviceId(getDeviceId());
            collectionQuestion.setQuestionId(consultId);
            collectionQuestionService.addByCustId(collectionQuestion);
        } else {
            throw new ApplicationException(900,"您还没有登录，不能使用收藏功能");
        }
    }

    /**
     * 咨询收藏 -- 取消(登录)
     * @param consultId
     */
    @ResponseBody
    @RequestMapping(value = "/{consultId}/cancelFavorite", method = RequestMethod.POST)
    public void cancelFavoriteConsult(@PathVariable Long consultId) {
    	logger.info("--------------cancelFavoriteConsult(start)-------------"+"|"+"fromModule:ConsultController"+"|interfaceInfo:咨询收藏 -- 取消(登录)|"+"consultId:"+consultId);
        if(getLoginUserId() != null) {
            collectionQuestionService.cancelFavorite(getLoginUserId(), consultId);
        }else {
            throw new ApplicationException(900,"您还没有登录，不能使用取消收藏功能");
        }
    }
    /**
     * 咨询和投诉发布评论(登录)
     * @param objectType 评论类型 objectId 咨询id
     */
    @ResponseBody
    @RequestMapping(value = "/addComment/{objectType}/{objectId}", method = RequestMethod.POST)
    public void Comment(@PathVariable String objectType, @PathVariable Long objectId,
                           @RequestParam String comment, @RequestParam(required = false) Long commentId) throws Exception {
    	logger.info("--------------Comment(start)-------------"+"|"+
    			"fromModule:ConsultController"+"|interfaceInfo:咨询和投诉发布评论(登录)|"+"objectType:"+objectType+
    			"|objectId"+objectId+"|comment"+comment+"|commentId"+commentId);
        if (getLoginUserId() != null) {
        	//gaoheng于20160718注释
            /*String matchWord = addComment(objectType,objectId, comment, commentId);
            if (matchWord != null) {
                throw new ApplicationException("您发布的评论内容中包含敏感文字，请重新编辑后再提交。");
            }*/
        	addComment(objectType,objectId, comment, commentId);
        } else {
            throw new ApplicationException(900,"您还没有登陆，不能进行评论");
        }
    }

    //向评论表添加数据
    public String addComment(String objectType, Long objectId, String comment, Long commentId) {
        Long custId = getLoginUserId();
        logger.info("-----------addComment--------|custId:"+custId);
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

    //判断敏感词，伪删除路况,并且添加到敏感词记录表中
    private String checkSensitiveWord(String objectType, Long objectId, String content) {
    	logger.info("-------------checkSensitiveWord--------|objectType"+objectType+"|objectId"+objectId+"|content"+content);
        List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
        content = StringUtils.defaultString(content);
        for (SensitiveWords sensitiveWord : sensitiveWords) {
            Boolean hasSensitiveWord = content.contains(sensitiveWord.getWord());
            logger.info("-------------checkSensitiveWord--------|hasSensitiveWord"+hasSensitiveWord);
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
    //添加咨询敏感词过滤
    private boolean checkSensitiveWord(String objectType,String content){
    	List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
    	List<SensitiveHist> list = new ArrayList<SensitiveHist>();
        content = StringUtils.defaultString(content);
        for (SensitiveWords sensitiveWord : sensitiveWords) {
        	Boolean hasSensitiveWord = content.contains(sensitiveWord.getWord());
        	if (hasSensitiveWord) {
                //添加到敏感词记录表中
                SensitiveHist sensitiveHist = new SensitiveHist();
                sensitiveHist.setCustId(getLoginUserId());
                sensitiveHist.setObjectType(objectType);
                sensitiveHist.setObjectId(0l);
                sensitiveHist.setSensitiveWord(sensitiveWord.getWord());
                sensitiveHistService.add(sensitiveHist);
                list.add(sensitiveHist);
             }
        }
        if(list.size()>0){
        	return true;
        }else{
        	return false;
        }
    }
    /**
     * 获取我的咨询和投诉列表
     * @param type
	 * @param custId 
	 * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getConsultDept", method = RequestMethod.POST)
    public Object getConsultDept(String type){
    	logger.info("--------------getConsultDept(start)-------------"+"|"+"fromModule:ConsultController"
			+"|interfaceInfo:获取我的咨询和投诉列表"+"|type:"+type);
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Customer customer= getLoginUser();
		if (customer == null) {// 检查用户是否登录
			model.setCode("0300");
			model.setResult(list);
			model.setError("用户未登录");
			model.setMessage("需要您登录！");
			model.setState("0");
			return model;
		}
		if (type != null && type != "") {
			
			if (type.equals("complain")) {
				logger.info("---getComplainDept(参数：type:" + type+",custId:"+customer.getId()+ ")---");
				list = govQuestionService.getComplainDept(type,customer.getId());
				model.setCode("0000");
				model.setResult(list);
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				return model;
			} else {
				logger.info("---getComplainDept(参数：type:" + type+",custId:"+customer.getId()+ ")---");
				list = govQuestionService.getConsultDept(type, customer.getId());
				model.setCode("0000");
				model.setResult(list);
				model.setError("");
				model.setMessage("调用成功");
				model.setState("1");
				return model;
			}
		} else {
			model.setCode("0203");
			model.setResult(list);
			model.setError("");
			model.setMessage("参数缺失");
			model.setState("0");
			return model;
		}
     	//return model;
    }
}
