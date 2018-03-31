package com.inspur.icity.web.cust.builder;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.inspur.icity.logic.cust.model.Comment;
import com.inspur.icity.logic.cust.model.Question;
import com.inspur.icity.logic.cust.service.AppService;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.CustomerService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.cust.service.QuestionService;
import com.inspur.icity.logic.gov.model.Item;
import com.inspur.icity.logic.gov.service.ItemService;
import com.inspur.icity.logic.news.model.Msg;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.web.utils.Constants;

@Component
public class CommentToMapBuilder{

    @Autowired
    CommentService commentService;
    @Autowired
    CustomerService customerService;
    @Autowired
    PraiseService praiseService;
    @Autowired
    MsgService msgService;

    @Autowired
    AppService appService;
    @Autowired
    ItemService itemService;
    @Autowired
    QuestionService questionService;


    public Map<String,Object> build(Comment comment,Long deviceId, Long custId) {
        if(comment == null){
            return null;
        }
        Map<String,Object> map = Maps.newHashMap();
        map.put("id",comment.getId());
        map.put("custId",comment.getCustId());
        map.put("isDeleted", comment.getDeleted());
        // 追加对应类型及ID
        map.put("objectId", comment.getObjectId());
        map.put("objectType", comment.getObjectType());
        if(comment.getCommentId() != null){
        	Comment resComment = commentService.get(comment.getCommentId());
        	if(resComment != null){
        		Map customer = customerService.getPersonalData(resComment.getCustId());
        		if(customer != null){
        			map.put("replayTo", customer.get("nickName"));
        		}
        	}
        }
        Map customer = customerService.getPersonalData(comment.getCustId());
        map.put("nickName",customer.get("nickName"));
        map.put("comment",comment.getComment());
        map.put("imgUrl",customer.get("imgUrl"));
        map.put("createTime",comment.getCreateTime());
        
        //该用户是否点赞过
        if(custId == null){
        	map.put("praisedStatus",0);
        }else{
        	String praisedType = praiseService.hasPraised(null, custId, "comment", comment.getId());
        	if("1".equals(praisedType)){
        		map.put("praisedStatus",1);
        	}else{
        		map.put("praisedStatus",0);
        	}
        }
        // 点赞数量
        map.put("praiseCount",praiseService.countPraise("comment", comment.getId()));
        map.put("commentCount",commentService.myCommentsCount(comment.getId()));
        // 追加所评论信息的标题
        String title = "";
        String gotoUrl = "";
        String imgUrl = "";
        if(Constants.TYPE_NEWS.equals(comment.getObjectType())){
            Msg msg = msgService.get(comment.getObjectId());
            title = msg.getTitle();
            gotoUrl = msg.getGotoUrl();
            imgUrl = msg.getImgUrl();
        } else if(Constants.TYPE_GOV.equals(comment.getObjectType())){
            Item item = itemService.get(comment.getObjectId());
            title = item.getName();
        } else if (Constants.TYPE_CONSULTS.equals(comment.getObjectType())) {
            Question question = questionService.get(comment.getObjectId());
            title = question.getTitle();
        }else if(Constants.TYPE_COMPLAIN.equals(comment.getObjectType())){
            Question question = questionService.get(comment.getObjectId());
            title = question.getTitle();
        }
        map.put("commentImgUrl", imgUrl);
        map.put("title", title);
        map.put("gotoUrl", gotoUrl);
        return map;
    }

}
