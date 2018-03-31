package com.inspur.icity.web.news.builder;

import com.google.common.collect.Maps;
import com.inspur.icity.core.builder.ToMapBuilder;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.news.model.Msg;
import com.inspur.icity.web.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class MsgToMapBuilder implements ToMapBuilder<Msg> {
    @Autowired
    CommentService commentService;

    public Map<String,Object> build(Msg msg) {
        if(msg == null){
            return null;
        }
        Map<String,Object> map = Maps.newHashMap();
        map.put("id",msg.getId());
        map.put("type",msg.getType());
        map.put("title",msg.getTitle());
        map.put("source",msg.getSource());
        map.put("imgUrl",msg.getImgUrl());
        map.put("gotoUrl",msg.getGotoUrl());
        map.put("isTop",msg.getIsTop());
        map.put("createTime",msg.getCreateTime());
        map.put("commentCount",commentService.findCommentCount(Constants.TYPE_NEWS,msg.getId()));
        map.put("pageView", msg.getPageView().setScale(0, BigDecimal.ROUND_HALF_UP));
        return map;
    }
}
