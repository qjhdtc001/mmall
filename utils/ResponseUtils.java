package com.inspur.icity.web.utils;

import com.inspur.icity.core.exception.ApplicationException;
import net.sf.json.JSONObject;

/**
 * Created by dinggao on 2016/4/20.
 */
public class ResponseUtils {

    /**
     * 【共通函数】检查远程查询结果（政务等）
     * @param result JSON结果
     * @return ApplicationException 应用异常 正常：对象 异常：null
     */
    public static ApplicationException checkState(JSONObject result){
        if("1".equals(result.get("state").toString())){
            return null;
        }else{
            return new ApplicationException(900,result.getString("error"));
        }
    }
}
