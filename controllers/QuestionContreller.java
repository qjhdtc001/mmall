package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.cust.model.Answer;
import com.inspur.icity.logic.cust.model.CollectionQuestion;
import com.inspur.icity.logic.cust.model.Comment;
import com.inspur.icity.logic.cust.model.GovAnswer;
import com.inspur.icity.logic.cust.model.GovQuestion;
import com.inspur.icity.logic.cust.model.Praise;
import com.inspur.icity.logic.cust.model.Question;
import com.inspur.icity.logic.cust.service.AnswerService;
import com.inspur.icity.logic.cust.service.CollectionQuestionService;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.GovAnswerService;
import com.inspur.icity.logic.cust.service.GovQuestionService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.cust.service.QuestionService;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.cust.builder.CommentToMapBuilder;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;

/**
 * 咨询相关接口
 */
@Controller
@RequestMapping(value = "/question")
public class QuestionContreller extends BaseAuthController{
    @Autowired
    QuestionService questionService;
    @Autowired
    PraiseService praiseService;
    @Autowired
    CommentService commentService;
    @Autowired
    AnswerService answerService;
    @Autowired
    CollectionQuestionService collectionQuestionService;
    @Autowired
    CommentToMapBuilder commentToMapBuilder;
    @Autowired
    SensitiveWordService sensitiveWordService;
    @Autowired
    SensitiveHistService sensitiveHistService;
    @Autowired
    GovAnswerService govAnswerService;
    @Autowired
    GovQuestionService govQuestionService;
    
    private	final String DETAIL_URL_TEST = "http://60.31.98.34:8089/icity/c/api.inlcity/iCityGuestBookListByPage";
    private	final String DETAIL_URL_OFFICIAL = "http://123.178.103.151:80/icity/c/api.inlcity/iCityGuestBookListByPage";
    Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 热门咨询列表
     * @param cityCode 城市标识
     * @return 咨询一览
     */
    @ResponseBody
    @RequestMapping(value = "/hotConsults", params = {"cityCode"})
    public Object hotConsults(String cityCode){
        return questionService.hotConsults(cityCode, getPageBounds());
    }

    /**
     * 最新咨询列表
     * @param cityCode 城市标识
     * @return 咨询一览
     */
    @ResponseBody
    @RequestMapping(value = "/newConsults", params = {"cityCode"})
    public Object newConsults(String cityCode){
        return questionService.newConsults(cityCode, getPageBounds());
    }

    /**
     * 咨询、投诉详情
     * @param questionId 咨询主键
     * @return 咨询、投诉一览
     */
    @ResponseBody
    @RequestMapping(value = "/answerDetail", params = {"questionId"}, method = RequestMethod.POST)
    public Object answerDetail(Long questionId){
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	logger.info("---------------questionDetail(start)--------|fromModule:QuestionController-------|questionId：" + questionId );
    	if(questionId != null){
    		GovQuestion govQuestion = govQuestionService.get(questionId);
    		logger.info("-----------------------------------------根据questionId取得的question： " + govQuestion);
    		if(govQuestion != null){
    			String verifyId = govQuestion.getVerifyId();
    			String isAnswer = String.valueOf(govQuestion.getIsAnswered());
    			logger.info("-----------------------------------verifyId:" + verifyId + ",isAnswer:" + isAnswer);
    			if (isAnswer.equalsIgnoreCase("1")) {
					List<Map<String, Object>> answerList = new ArrayList<Map<String, Object>>();
					try {
						answerList = govAnswerService.findAnswer(questionId);
						logger.info("-------------------------------------answer : " + answerList);
						for(Map<String, Object> map : answerList){
							map.put("state", 1);
						}
						list = answerList;
						model.setState("1");
						model.setCode("0000");
						model.setError("");
						model.setResult(list);
						model.setMessage("调用成功");
						return model;
					} catch (Exception e) {
						// TODO: handle exception
						logger.error("--------------------------查询cust_gov_answer表出现异常------------------------");
						model.setState("0");
						model.setCode("0100");
						model.setError("系统未知异常");
						model.setResult(list);
						model.setMessage("调用失败");
						return model;
					}
					
				}else {//----question的isAnswered为0
					logger.info("----------------------------数据库中查询到该问题未回答,转向满洲里第三方接口----------------------------");
	    			HashMap<String, String> params = new HashMap<String, String>();
	    			String page = "1";
	    			String limit = "10";
	    			params.put("where", "And ID=?");
	    			params.put("params", "[\'" + verifyId + "\']");
	    			params.put("page", page);
	    			params.put("limit", limit);
	    			logger.info("-----------------------------条件|where: " + params.get("where") + ",params:" + params.get("params") + ",page:" + page + ",limit:" + limit);
	    			try {
	    				String DETAIL_URL = "";
        				if(Config.getValue("currentDeployment").equalsIgnoreCase("test")){
        					DETAIL_URL = DETAIL_URL_TEST;
        				}
        				if(Config.getValue("currentDeployment").equalsIgnoreCase("official")){
        					DETAIL_URL = DETAIL_URL_OFFICIAL;
        				}
	    				String response = HttpUtil.post(DETAIL_URL, params);
	    				JSONObject jsonObj = JsonUtil.strToJson((Object)response);
	    				logger.info("---------------------------------------满洲里返回结果response : " + response);
	    				GovAnswer govAnswer = new GovAnswer();//用于插入cust_gov_answer表中
	    				//展示给前台用的变量
	    				Map<String, Object> map = new HashMap<String, Object>();
	    				String qTitle = "";
	    				String qContent = "";
	    				String qType = "";
	    				String quesId = "";
	    				String content = "";
	    				String answerName = "";
	    				String dealTime = "";
	    				if(jsonObj.has("data") && jsonObj.has("state") && !BeanUtil.isNullString(jsonObj.getString("data")) && !BeanUtil.isNullString(jsonObj.getString("state"))
	    						&& jsonObj.getString("state").equalsIgnoreCase("1")){
	    					String dataStr = jsonObj.getString("data");
	    					logger.info("-------------------------------获取到接口返回data：" + dataStr);
	    					dataStr = dataStr.replace("[", "");
	    					dataStr = dataStr.replace("]", "");
	    					JSONObject data = JsonUtil.strToJson(dataStr);
  	    					if(data.getString("STATUS").equalsIgnoreCase("1")){
  	    						JSONObject date = JsonUtil.strToJson(data.getString("DEAL_DATE"));//获取处理时间
  		    					if(date != null && !BeanUtil.isNullString(date.getString("time"))){
  		    						String tmpS = new String(date.getString("time"));
  		    						tmpS = tmpS.substring(0, tmpS.length()-3);
  		    						dealTime = DateUtil.timeStamp2Date(tmpS, "yyyy-MM-dd");
  		    					}else{
  		    						logger.error("---------------------------获取处理时间出现异常-----------------------------");
  			    					model.setState("0");
  									model.setCode("0100");
  									model.setError("系统未知异常");
  									model.setResult(list);
  									model.setMessage("调用失败");
  									return model;
  		    					}
	    						qTitle = data.getString("TITLE");
		    					qContent = data.getString("CONTENT");
		    					qType = data.getString("TYPE");
		    					quesId = String.valueOf(questionId);
		    					content = data.getString("DEAL_RESULT");
		    					answerName = data.getString("DEPART_NAME");
		    					map.put("title", qTitle);
		    					map.put("qContent", qContent);
		    					map.put("qType", qType);
		    					map.put("questionId", quesId);
		    					map.put("content", content);
		    					map.put("answerName", answerName);
		    					map.put("dealTime", dealTime);
		    					map.put("state", 1);
		    					list.add(map);
		    					//将answer添加到cust_answer表中
		    					try {
		    						govAnswer.setAnswerName(answerName);
		    						govAnswer.setContent(content);
		    						govAnswer.setQuestionId(questionId);
		    						govAnswer.setTitle(qTitle);
		    						govAnswer.setDealTime(dealTime);
									govAnswerService.add(govAnswer);
									logger.info("---------------------插入数据库cust_answer成功--------------------");
								} catch (Exception e) {
									// TODO: handle exception
									logger.error("---------------------------插入数据库出现异常-----------------------------");
			    					model.setState("0");
									model.setCode("0100");
									model.setError("系统未知异常");
									model.setResult(list);
									model.setMessage("调用失败");
									return model;
								}
		    					//更新cust_question表中的isAnswered字段
		    					try {
									govQuestion.setIsAnswered(1l);
									govQuestionService.update(govQuestion);
									logger.info("----------------------------更新数据库cust_question成功----------------");
								} catch (Exception e) {
									// TODO: handle exception
									logger.error("---------------------------更新数据库出现异常-----------------------------");
			    					model.setState("0");
									model.setCode("0100");
									model.setError("系统未知异常");
									model.setResult(list);
									model.setMessage("调用失败");
									return model;
								}
		    					model.setState("1");
								model.setCode("0000");
								model.setError("");
								model.setResult(list);
								model.setMessage("调用成功");
								return model;
	    					}else{
	    						logger.info("----------------------------问题尚未处理-------------------------------");
	    						map.put("state", 0);
	    						list.add(map);
	    						model.setState("1");
								model.setCode("0000");
								model.setError("");
								model.setResult(list);
								model.setMessage("调用成功");
								return model;
	    					}
	    					
	    				}else{
	    					logger.error("---------------------------第三方返回参数出现异常-----------------------------");
	    					model.setState("0");
							model.setCode("0400");
							model.setError("第三方接口调用异常");
							model.setResult(list);
							model.setMessage("调用失败");
							return model;
	    				}
					} catch (Exception e) {
						// TODO: handle exception
						model.setState("0");
						model.setCode("0400");
						model.setError("第三方接口调用异常");
						model.setResult(list);
						model.setMessage("调用失败");
						return model;
					}
	    			
				}
    		}else{
    			model.setState("0");
				model.setCode("0100");
				model.setError("系统未知异常");
				model.setResult(list);
				model.setMessage("调用失败");
				return model;
    		}
    	}else{
    		logger.error("------------------------------questionId值为NULL-------------------------------");
			model.setState("0");
			model.setCode("0203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			return model;
    	}
    	//return model;
    }
/*        Map<String, Object> map = Maps.newHashMap();
        Question question =questionService.get(consultId);
        map.put("id",question.getId());
        map.put("title",question.getTitle());
        map.put("type", question.getType());
        map.put("content",question.getContent());
        map.put("createTime",question.getCreateTime());
        map.put("usefulCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS,1l,consultId));
        map.put("uselessCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS, 0l, consultId));
        map.put("commentCount",commentService.findCommentCount(Constants.TYPE_CONSULTS,consultId));
        String hasPraised = praiseService.hasPraised(getDeviceId(), Constants.TYPE_CONSULTS, consultId);
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
        List<Answer> answers =  answerService.getAnswerByQuestionId(consultId, getPageBounds());
        map.put("answers",answers);
        return map;*/
    @ResponseBody
    @RequestMapping(value = "/consultDetail", params = {"consultId"})
    public Object consultDetail(Long consultId){
        Map map = Maps.newHashMap();
        Question question =questionService.get(consultId);
        map.put("id",question.getId());
        map.put("title",question.getTitle());
        map.put("type", question.getType());
        map.put("content",question.getContent());
        map.put("createTime",question.getCreateTime());
        map.put("usefulCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS,1l,consultId));
        map.put("uselessCount",praiseService.findPraiseCount(Constants.TYPE_CONSULTS, 0l, consultId));
        map.put("commentCount",commentService.findCommentCount(Constants.TYPE_CONSULTS,consultId));
        String hasPraised = praiseService.hasPraised(getDeviceId(), null, Constants.TYPE_CONSULTS, consultId);
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
        List<Answer> answers =  answerService.getAnswerByQuestionId(consultId, getPageBounds());
        map.put("answers",answers);
        return map;
    }
    /**
     * 投诉详情
     * @param complainId 投诉主键
     * @return 投诉一览
     */
    @ResponseBody
    @RequestMapping(value = "/complainDetail", params = {"complainId"})
    public Object complainDetail(Long complainId) {
    	Map<String, Object> map = Maps.newHashMap();
        Question question = questionService.get(complainId);
        map.put("id", question.getId());
        map.put("title", question.getTitle());
        map.put("type", question.getType());
        map.put("content", question.getContent());
        map.put("createTime", question.getCreateTime());
        map.put("usefulCount", praiseService.findPraiseCount(Constants.TYPE_COMPLAIN, 1l, complainId));
        map.put("uselessCount", praiseService.findPraiseCount(Constants.TYPE_COMPLAIN, 0l, complainId));
        map.put("commentCount", commentService.findCommentCount(Constants.TYPE_COMPLAIN, complainId));
        String hasPraised = praiseService.hasPraised(getDeviceId(), null, Constants.TYPE_COMPLAIN, complainId);
        if ("1".equals(hasPraised)) {
            map.put("usefulStatus", 1);
            map.put("uselessStatus", 0);
        } else if ("0".equals(hasPraised)) {
            map.put("usefulStatus", 0);
            map.put("uselessStatus", 1);
        } else {
            map.put("usefulStatus", 0);
            map.put("uselessStatus", 0);
        }
       /* CollectionQuestion collectionQuestion = collectionQuestionService.hasCollectioned(getLoginUserId(), consultId);
        if (collectionQuestion != null) {
            map.put("hasCollectioned", 1);
        } else {
            map.put("hasCollectioned", 0);
        }*/
        List<Answer> answers = answerService.getAnswerByQuestionId(complainId, getPageBounds());
        map.put("answers", answers);
        return map;
    }

    /**
     * 投诉详情有用
     */
    @ResponseBody
    @RequestMapping(value = "/useful", method = RequestMethod.POST)
    public void useful(Long complainId) {
        Long deviceId = getDeviceId();
        Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_COMPLAIN, complainId);
        if (praiseOld != null) {
            throw new ApplicationException(900,"您已执行过此操作，不能再次执行！");
        }
        try {
            Praise praise = new Praise();
            praise.setCustId(getLoginUserId());
            praise.setDeviceId(getDeviceId());
            praise.setObjectType(Constants.TYPE_COMPLAIN);
            praise.setObjectId(complainId);
            praise.setIsUseful(1l);
            praiseService.add(praise);
        } catch (Exception e) {
            throw new ApplicationException(900,"操作失败");
        }

    }
    /**
     * 取消投诉有用操作
     * @param consultId
     */
    @ResponseBody
    @RequestMapping(value = "/delUseful", method = RequestMethod.POST)
    public void delUseful(Long complainId) {
        Long deviceId = getDeviceId();
        try {
           praiseService.removeByObjectId(deviceId, Constants.TYPE_COMPLAIN, complainId);;
        }catch (Exception e){
            throw new ApplicationException(900,"操作失败");
        }
    }
    /**
     * 投诉详情没用
     *
     * @parm consultId
     */
    @ResponseBody
    @RequestMapping(value = "/useless", method = RequestMethod.POST)
    public void useless(Long complainId) {
        Long deviceId = getDeviceId();
        Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_COMPLAIN, complainId);
        if (praiseOld != null) {
            throw new ApplicationException(900,"您已执行过此操作，不能再次执行！");
        }
        try {
            Praise praise = new Praise();
            praise.setCustId(getLoginUserId());
            praise.setDeviceId(getDeviceId());
            praise.setObjectType(Constants.TYPE_COMPLAIN);
            praise.setObjectId(complainId);
            praise.setIsUseful(0l);
            praiseService.add(praise);
        } catch (Exception e) {
            throw new ApplicationException(900,"操作失败");
        }

    }

    /**
     * 投诉的评论列表
     * @param complainId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{complainId}/comments")
    public Object newsComments(@PathVariable Long complainId) {
        return map(commentService.eachModuleComments(Constants.TYPE_COMPLAIN, complainId, getPageBounds()), comment -> {
            return commentToMapBuilder.build((Comment) comment, getDeviceId(),getLoginUserId());
        });
    }

    /**
     * 咨询信息
     */
    @ResponseBody
    @RequestMapping(value = "/findConsults", params = {"consultId"})
    public Object findConsults(Long consultId) {
        return questionService.findConsults(Constants.TYPE_CONSULTS,consultId);
    }

    /**
     * 投诉信息
     */
    @ResponseBody
    @RequestMapping(value = "/findComplain", params = {"complainId"})
    public Object findComplain(Long complainId) {
        return questionService.findConsults(Constants.TYPE_COMPLAIN, complainId);
    }
}
