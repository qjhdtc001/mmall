package com.inspur.icity.web.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.miemiedev.mybatis.paginator.domain.Order;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.google.common.base.Strings;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.inspur.icity.logic.cust.model.ClientInfo;
public class BaseController {
	
    protected HttpServletRequest getRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    protected HttpSession getSession(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    }
    //从session获取版本信息
    protected ClientInfo getClientInfo(){
		return (ClientInfo) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("clientInfo");
	}

    protected Object convertResult(Object object){
        String callback = getRequest().getParameter("callback");
        if(Strings.isNullOrEmpty(callback)){
            return object;
        }
        return new JSONPObject(callback, object);
    }

    protected PageBounds getPageBounds(){
        HttpServletRequest request = getRequest();
        String page = request.getParameter("page");
        String limit = request.getParameter("limit");
        String sort = request.getParameter("sort");
        String dir = request.getParameter("dir");

        PageBounds pageBounds = new PageBounds();

        if(!Strings.isNullOrEmpty(page) && !Strings.isNullOrEmpty(limit)){
            pageBounds = new PageBounds(Integer.valueOf(page), Integer.valueOf(limit));
        }else if(!Strings.isNullOrEmpty(limit)){
            pageBounds = new PageBounds(Integer.valueOf(limit));
        }

        if(!Strings.isNullOrEmpty(sort) && !Strings.isNullOrEmpty(dir)){
            pageBounds.getOrders().add(Order.create(sort, dir));
        }else if(!Strings.isNullOrEmpty(sort)){
            pageBounds.getOrders().add(Order.create(sort, "asc"));
        }

        return pageBounds;
    }

    protected List map(List list, Function function){
        List result = (List)list.stream().map(function).collect(Collectors.toList());
        if(list instanceof PageList){
            PageList pageList = (PageList)list;
            return new PageList(result,pageList.getPaginator());
        }
        return result;
    }

    protected String getFullUrl(){
        HttpServletRequest request = getRequest();
        String scheme = request.getScheme();             // http
        String serverName = request.getServerName();     // hostname.com
        int serverPort = request.getServerPort();        // 80
        String contextPath = request.getContextPath();

        StringBuffer url =  new StringBuffer();
        url.append(scheme).append("://").append(serverName);

        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        return url.toString()+request.getRequestURI();
    }

    protected String getServerContextPath(){
        HttpServletRequest request = getRequest();
        String scheme = request.getScheme();             // http
        String serverName = request.getServerName();     // hostname.com
        int serverPort = request.getServerPort();        // 80
        String contextPath = request.getContextPath();

        StringBuffer url =  new StringBuffer();
        url.append(scheme).append("://").append(serverName);

        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }

}
