package com.inspur.icity.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.base.model.Department;
import com.inspur.icity.logic.base.service.DepartmentService;
import com.inspur.icity.logic.gov.model.GovGuideDept;
import com.inspur.icity.logic.gov.model.GovGuideRegion;
import com.inspur.icity.logic.gov.service.GovGuideDeptService;
import com.inspur.icity.logic.gov.service.GovGuideRegionService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.ResponseUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门相关接口
 */
@Controller
@RequestMapping(value = "/department")
public class DepartmentController extends BaseAuthController{
    @Autowired
    DepartmentService departmentService;
    @Autowired
    GovGuideRegionService govGuideRegionService;
    @Autowired
    GovGuideDeptService govGuideDeptService;
    Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 数据导入接口
     * @return 查询进度一览
     */
    @ResponseBody
    @RequestMapping(value = "/importData", method = {RequestMethod.GET})
    public Object importData() {
        // 参数设置
        String url =Config.getValue("url");

        String response = null;
        List<Map<String, Object>> bussinessList = new ArrayList<>();
        try{
            response = HttpUtil.get(url);
            // 转换JSON对象
            JSONObject jsonResult = JsonUtil.strToJson(response);
            // 检查远程访问结果
            ApplicationException applicationException = ResponseUtils.checkState(jsonResult);
            if(applicationException != null){
            	 throw new ApplicationException(900,"操作失败！");
            }
            // 获取部门列表
            JSONArray bussinessArrary = jsonResult.getJSONArray("organ");
            // 解析部门列表
            if(bussinessArrary != null){
                for (int i = 0;i < bussinessArrary.size();i++){
                    // 存放单个部门
                    Map<String, Object> bussiness = Maps.newHashMap();
                    // 获取单个部门
                    JSONObject bussinessJson = (JSONObject) bussinessArrary.get(i);

                    Department department = new Department();

                    // 解析ID
                    if(bussinessJson.containsKey("ID")) {
                        department.setDepartmentId(bussinessJson.getString("ID"));
                        bussiness.put("ID", bussinessJson.getString("ID"));
                    }else{
                        department.setDepartmentId("");
                        bussiness.put("ID", "");
                    }
                    // 解析NAME
                    if(bussinessJson.containsKey("NAME")) {
                        department.setName(bussinessJson.getString("NAME"));
                        bussiness.put("NAME", bussinessJson.getString("NAME"));
                    }else{
                        department.setName("");
                        bussiness.put("NAME", "");
                    }
                    // 解析REGION_CODE
                    if(bussinessJson.containsKey("REGION_CODE")) {
                        department.setCityCode(bussinessJson.getString("REGION_CODE"));
                        bussiness.put("REGION_CODE", bussinessJson.getString("REGION_CODE"));
                    }else{
                        department.setName("");
                        bussiness.put("REGION_CODE", "");
                    }
                    // 解析REGION_NAME
                    if(bussinessJson.containsKey("REGION_NAME")) {
                        department.setCityName(bussinessJson.getString("REGION_NAME"));
                        bussiness.put("REGION_NAME", bussinessJson.getString("REGION_NAME"));
                    }else{
                        department.setName("");
                        bussiness.put("REGION_NAME", "");
                    }

                    departmentService.add(department);
                    bussinessList.add(bussiness);
                }
            }
        }catch (ApplicationException e){
        	 throw new ApplicationException(900,"修改生日失败：日期不合法！");
        } catch (Exception e){
        	 throw new ApplicationException(900,"修改生日失败：日期不合法！");
        }
        return bussinessList;
    }

    /**
     * 获取部门列表
     * @param cityCode 城市标识
     * @return 部门列表
     */
    @ResponseBody
    @RequestMapping(value = "/getList", method = {RequestMethod.GET})
    public Object getDepartmentList(String cityCode) {
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		try {
			int c = Integer.parseInt(cityCode);
			if (c == 370100) {
				list = departmentService.getDepartmentList(cityCode);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			}else{

				String monthId = (String) govGuideRegionService.getMaxMonthId(cityCode).get("monthId");
				List <GovGuideRegion> regionList = new ArrayList<GovGuideRegion>();
				logger.info("--------------getAllDept(根据monthid和cityCode获取部门列表)-------------"+"|"+"monthId:"+monthId+"|"+"cityCode:"+cityCode);
				regionList = govGuideRegionService.getGovGuideRegions(monthId, cityCode);
				List<Map<String,String>> result = new ArrayList<Map<String,String>>();
				 List <GovGuideDept> deptList = new ArrayList<GovGuideDept>();
				if(regionList!=null&&regionList.size()>0){
					for(GovGuideRegion govGuideRegion: regionList){
						deptList = govGuideDeptService.getRegionByRegionCode(monthId,govGuideRegion.getRegionCode());				 
					 }
				}
				if(deptList!=null&&deptList.size()>0){
					
					for(GovGuideDept govGuideDept :deptList){
						Map<String,String> map = new HashMap<String,String>();
						map.put("deptName", govGuideDept.getDeptName());
						map.put("deptCode", govGuideDept.getDeptCode());
						result.add(map);
					}
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(result);
				model.setMessage("调用成功");
				model.setState("1");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			model.setCode("0100");
	     	model.setError("系统未知异常！");
	     	model.setResult(list);
	        model.setMessage("调用失败");
	     	model.setState("0");
		}
    	return model;
    }
}
