package com.inspur.icity.web.app.builder;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.logic.base.utils.AppDICTConstants;

/**
 * 添加应用列表分类
 */
@Component
public class AppToMapBuilder {

    public Map<String, Object> build(List<Map<String, Object>> appList){
        Map<String, Object> map = Maps.newHashMap();
        if(appList == null || appList.size() == 0){
            return map;
        }
        List<Map<String, Object>> govServiceList = Lists.newArrayList();
        List<Map<String, Object>> lifePaymentList = Lists.newArrayList();
        List<Map<String, Object>> trafficServiceList = Lists.newArrayList();
        List<Map<String, Object>> socialSecurityServiceList = Lists.newArrayList();
        List<Map<String, Object>> otherServiceList = Lists.newArrayList();
        for(Map<String, Object> appMap : appList){
            Map<String, Object> app = Maps.newHashMap();
            app.put("appId", appMap.get("id"));
            app.put("imgUrl", appMap.get("imgUrl"));
            app.put("appName", appMap.get("name"));
            app.put("type", appMap.get("type"));
            app.put("hasAdded", (appMap.get("appId") != null && !"".equals(appMap.get("appId").toString()) ? 1 : 0));
            if(AppDICTConstants.ADDAPP_GOVSERVICE.equals(appMap.get("type").toString())){
                govServiceList.add(app);
            }else if(AppDICTConstants.ADDAPP_LIFESERVICE.equals(appMap.get("type").toString())){
                lifePaymentList.add(app);
            }else if(AppDICTConstants.ADDAPP_TRAFFICSERVICE.equals(appMap.get("type").toString())){
                trafficServiceList.add(app);
            }else if(AppDICTConstants.ADDAPP_SOCIALSECURITY.equals(appMap.get("type").toString())){
                socialSecurityServiceList.add(app);
            }else{
                otherServiceList.add(app);
            }
        }
        map.put("govServices", govServiceList);
        map.put("lifeServices", lifePaymentList);
        map.put("trafficService", trafficServiceList);
        map.put("socialService", socialSecurityServiceList);
        map.put("otherService", otherServiceList);
        return map;
    }
    
    public Map<String, Object> buildHome(List<Map<String, Object>> list,String type,String imageUrl){
    	Map<String, Object> map = Maps.newHashMap();
    	if(list!=null&&list.size()>0){
	    	 for(Map<String, Object> mapApp :list){
		   		  for (Object k : mapApp.keySet())  
		   	      {  
		   	        map.put(k.toString(), mapApp.get(k));
		   	      }
	   	     }
    	}
        map.put("homeType", type);
        map.put("homeImageUrl", imageUrl);
        return map;
	}
    
    public Map<String, Object> buildHome(List<Map<String, Object>> list,String type,String imageUrl,String comment){
    	Map<String, Object> map = Maps.newHashMap();
    	if(list!=null&&list.size()>0){
	    	 for(Map<String, Object> mapApp :list){
		   		  for (Object k : mapApp.keySet())  
		   	      { map.put(k.toString(), mapApp.get(k));
		   	      }
	   	     }
    	}
        map.put("homeType", type);
        map.put("homeImageUrl", imageUrl);
        map.put("comment", comment);
        return map;
	}
}
