package com.inspur.icity.web.controllers;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.core.utils.DateUtil;
import com.inspur.icity.core.utils.HttpUtil;
import com.inspur.icity.core.utils.JsonUtil;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.base.service.ImageService;
import com.inspur.icity.logic.cust.model.Customer;
import com.inspur.icity.logic.life.model.DriverSchool;
import com.inspur.icity.logic.life.model.HotLine;
import com.inspur.icity.logic.life.model.HotLineAnswer;
import com.inspur.icity.logic.life.model.Pay;
import com.inspur.icity.logic.life.model.PeckerAddress;
import com.inspur.icity.logic.life.model.PeckerAnswer;
import com.inspur.icity.logic.life.model.PeckerEvaluate;
import com.inspur.icity.logic.life.model.PeckerInfo;
import com.inspur.icity.logic.life.model.PeckerPicture;
import com.inspur.icity.logic.life.model.TrafficUserInfo;
import com.inspur.icity.logic.life.model.TrafficViolation;
import com.inspur.icity.logic.life.model.Woodpecker;
import com.inspur.icity.logic.life.model.WoodpeckerPicture;
import com.inspur.icity.logic.life.service.DriverSchoolInfoSrvice;
import com.inspur.icity.logic.life.service.DriverSchoolService;
import com.inspur.icity.logic.life.service.HotLineAnswerService;
import com.inspur.icity.logic.life.service.HotLineService;
import com.inspur.icity.logic.life.service.PayService;
import com.inspur.icity.logic.life.service.PeckerAddressService;
import com.inspur.icity.logic.life.service.PeckerAnswerService;
import com.inspur.icity.logic.life.service.PeckerEvaluateService;
import com.inspur.icity.logic.life.service.PeckerInfoService;
import com.inspur.icity.logic.life.service.PeckerPictureService;
import com.inspur.icity.logic.life.service.TrafficUserInfoService;
import com.inspur.icity.logic.life.service.TrafficViolationService;
import com.inspur.icity.logic.life.service.WoodpeckerPictureService;
import com.inspur.icity.logic.life.service.WoodpeckerService;
import com.inspur.icity.logic.sensitive.model.SensitiveHist;
import com.inspur.icity.logic.sensitive.model.SensitiveWords;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.utils.Config;
import com.inspur.icity.web.utils.Constants;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sun.misc.BASE64Decoder;
/**
 * 爱城市本地应用支撑接口
 * @author gaoheng
 *
 */
@Controller
@RequestMapping(value = "/localApp")
public class NativeAppController extends BaseAuthController {
	 /**
	  * 请求违章查询接口
	  */
	 Logger logger = LoggerFactory.getLogger(getClass());
	 public static final String  TrafficUrl= Config.getValue("TrafficUrl");
	 @Autowired
	 PayService payService;
	 @Autowired
	 TrafficViolationService trafficViolationService;
	 @Autowired
	 TrafficUserInfoService trafficUserInfoService;
	 @Autowired
	 DictService dictService;
	 @Autowired
	 ImageService imageService;
	 @Autowired
	 WoodpeckerService woodpeckerService;
	 @Autowired
	 WoodpeckerPictureService woodpeckerPictureService;
	 @Autowired
	 DriverSchoolInfoSrvice driverSchoolInfoSrvice;
	 @Autowired
	 DriverSchoolService driverSchoolService;
	 @Autowired
	 HotLineService hotLineService;
	 @Autowired
	 HotLineAnswerService hotLineAnswerService;
	 @Autowired
	 SensitiveWordService sensitiveWordService;
	 @Autowired
	 SensitiveHistService sensitiveHistService;
	 @Autowired
	 PeckerInfoService peckerInfoService;
	 @Autowired
	 PeckerPictureService peckerPictureService;
	 @Autowired
	 PeckerAddressService peckerAddressService; 
	 @Autowired
	 PeckerAnswerService peckerAnswerService;
	 @Autowired
	 PeckerEvaluateService peckerEvaluateService;
	 
    /**
     * 添加缴费账户 
     * @param moduletype
     * @param accountName
     * @param accountId
     * @param payUnitName
     * @param cityCode
     * @return
     */
	@ResponseBody
    @RequestMapping(value = "/addPayAccount", method = RequestMethod.POST)
	public Object addPayAccountInfo(String moduleType,String accountName,String accountId,String payUnitName,String cityCode){
		List<Map<String,Object>> list = new ArrayList();
		JsonResultModel model = getJsonResultModel();
		model.setResult(list);
		Long custId = getLoginUserId();
		if(BeanUtil.isNullString(String.valueOf(custId))){
			model.setCode("0101");
    		model.setError("您还没有登录，请登入后再进行操作!");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
        }
		if(BeanUtil.isNullString(moduleType)||BeanUtil.isNullString(accountName)||BeanUtil.isNullString(accountId)
				||BeanUtil.isNullString(payUnitName)||BeanUtil.isNullString(cityCode)){
			model.setCode("0201");
    		model.setError("接口传递的参数异常！");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
		}
        try {
        	Pay pay = new Pay();
        	pay.setAccountId(accountId);
        	pay.setAccountName(accountName);
        	pay.setCityCode(cityCode);
        	pay.setCustId(custId);
        	pay.setPayUnitName(payUnitName);
        	pay.setModuleType(moduleType);
        	payService.add(pay);
        	model.setCode("0000");
    		model.setError("");
    		model.setMessage("调用成功");
    		model.setState("1");
    		return model;
        } catch (Exception e) {
        	model.setCode("0100");
    		model.setError("系统未知异常！");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
        }
	}
	/**
	 * 获取缴费账户列表
	 * @param moduletype
	 * @param cityCode
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getPayAccountList", method = RequestMethod.POST)
	public Object getPayAccountList(String moduleType,String cityCode){
		JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> list = new ArrayList();
		model.setResult(list);
		Long custId = getLoginUserId();
		if(BeanUtil.isNullString(String.valueOf(custId))){
            model.setCode("0101");
    		model.setError("您还没有登录，请登入后再进行操作！");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
        }else if(BeanUtil.isNullString(moduleType)||BeanUtil.isNullString(cityCode)){
        	model.setCode("0201");
    		model.setError("接口传递的参数异常！");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
        }else{
        	list = payService.payList(moduleType, custId, cityCode);
        	model.setCode("0000");
        	model.setError("");
        	model.setResult(list);
        	model.setMessage("调用成功");
        	model.setState("1");
        	return model;
        }
	}
	/**
	 * 获取当前账户详细信息
	 * @param Id
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getPayAccount", method = RequestMethod.POST)
	public Object getPayAccount(String id){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(list);
		try {
			if(!BeanUtil.isNullString(id)){
				Pay pay = payService.get(new Long(id));
				if(pay!=null){
				list.add(BeanUtil.getBeanToMap(pay));
				model.setResult(list);
				}
				model.setCode("0000");
	    		model.setError("");
				model.setMessage("调用成功");
        		model.setState("1");
        		return model;
			}else{
				model.setCode("0201");
	    		model.setError("接口传递的参数异常！");
	    		model.setMessage("调用失败");
	    		model.setState("0");
	    		return model;
			}
		} catch (Exception e) {
			model.setCode("0100");
    		model.setError("系统未知异常！");
    		model.setMessage("调用失败");
    		model.setState("0");
    		return model;
		}
	}
	/**
	 * 删除缴费账户
	 * @param id
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/removePayAccount", method = RequestMethod.POST)
	public Object removePayAccount(String id){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(list);
		try {
			if(BeanUtil.isNullString(id)){
				model.setCode("0201");
	    		model.setError("接口传递的参数异常！");
	    		model.setMessage("调用失败");
	    		model.setState("0");
	    		return model;
			} 
			payService.remove(new Long(id));
			model.setCode("0000");
    		model.setError("");
    		model.setMessage("调用成功");
    		model.setState("1");
    		return model;
		 } catch (Exception e) {
			model.setCode("0100");
     		model.setError("系统未知异常！");
     		model.setMessage("调用失败");
     		model.setState("0");
     		return model;
		}
	}
	/**
	 * 修改缴费账户信息
	 * @param id
	 * @param accountName
	 * @param accountId
	 * @param payUnitName
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/changePayAccount", method = RequestMethod.POST)
	public Object changePayAccount(String id,String accountName,String accountId,String payUnitName){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(list);
		try {
			if(BeanUtil.isNullString(id)||BeanUtil.isNullString(accountName)||BeanUtil.isNullString(accountId)
			   ||BeanUtil.isNullString(payUnitName)){
				model.setCode("0201");
	    		model.setError("接口传递的参数异常！");
	    		model.setMessage("调用失败");
	    		model.setState("0");
	    		return model;
			}
			Pay pay = new Pay();
			pay.setId(new Long(id));
			pay.setAccountId(accountId);
			pay.setAccountName(accountName);
			pay.setPayUnitName(payUnitName);
			payService.update(pay);
			model.setCode("0000");
    		model.setError("");
    		model.setMessage("调用成功");
    		model.setState("1");
    		return model;
		} catch (Exception e) {
			model.setCode("0100");
     		model.setError("系统未知异常！");
     		model.setMessage("调用失败");
     		model.setState("0");
     		return model;
		}
	}
	/*----------------------------------------------生活缴费end-------------------------------------------------------*/
	/**
	 * 查询交通违章信息的省份简写和车牌首字母
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getPrefixCarCode", method = RequestMethod.POST)
	public Object getPrefixCarCode(){
		List<Object> listJson = new ArrayList<Object>();
		JsonResultModel model = getJsonResultModel();
		model.setResult(listJson);
		try {
			List<Map<String,Object>> listTemp= trafficViolationService.getTrafficViolationList(); 
			String name ="";
			if(listTemp!=null&&listTemp.size()>0){
				for (Map<String,Object> map :listTemp){
					if (!map.get("name").toString().equalsIgnoreCase(name)){
						name = map.get("name").toString();
						Map<String,Object> mapT = new HashMap<String, Object>();
						ArrayList<String> arrayList = new ArrayList<String>();
						
						for(Map<String,Object> mapTemp:listTemp){
							if(mapTemp.get("name").toString().equalsIgnoreCase(name)){
								arrayList.add(mapTemp.get("code").toString());
							}
						}
						mapT.put("proName", name);
						mapT.put("cityCode",arrayList);
						listJson.add(mapT);
						
					}
				}
			}
			model.setCode("0000");
			model.setError("");
			model.setResult(listJson);
			model.setMessage("调用成功");
			model.setState("1");
			return model;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			model.setCode("0100");
     		model.setError("系统未知异常！");
     		model.setMessage("调用失败");
     		model.setState("0");
     		return model;
		}
	}
	/**
	 * 违章查询接口
	 * @param name 省份简写
	 * @param code 车牌首字母
	 * @param carCode 车牌号
	 * @param carNo 车架号
	 * @param engineNo 发动机号
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getTrafficInfo", method = RequestMethod.POST)
	public Object getTrafficInfo(String name,String code,String carCode,String carNo,String engineNo){
		String Key = Config.getValue("Key");//Key
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		if(!BeanUtil.isNullString(name)&&!BeanUtil.isNullString(code)&&!BeanUtil.isNullString(carCode)
				&&!BeanUtil.isNullString(carNo)&&!BeanUtil.isNullString(engineNo)){
			TrafficViolation trafficViolation = trafficViolationService.getTrafficViolation(name, code);
			if(trafficViolation!=null){//查询当前城市代码
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("key", Key);
			    params.put("dtype", "json");
			    params.put("city", trafficViolation.getCityCode());
			    params.put("hphm", name+code+carCode);
			    params.put("hpzl", "02");
				if(trafficViolation.getIsEngine().equalsIgnoreCase("1")){
				   if(!trafficViolation.getEngineNo().toString().equalsIgnoreCase("0")){
				       params.put("engineno",engineNo.substring(engineNo.length()-Integer.valueOf(trafficViolation.getEngineNo()),engineNo.length()));
				   }else{
					   params.put("engineno",engineNo);
				   }
				}
				if(trafficViolation.getIsCarNo().equalsIgnoreCase("1")){
				   if(!trafficViolation.getEngineNo().toString().equalsIgnoreCase("0")){
					   params.put("classno", carNo.substring(carNo.length()-Integer.valueOf(trafficViolation.getCarNo()),carNo.length()));
				   }else{
					   params.put("classno",carNo);    
				   }
				}
				try {
					String response = HttpUtil.post(TrafficUrl, params);
					//String response="{\"resultcode\":\"200\",\"reason\":\"查询成功\",\"result\":{\"province\":\"HB\",\"city\":\"HB_HD\",\"hphm\":\"冀DHL327\",\"hpzl\":\"02\",\"lists\":[{\"date\":\"2013-12-29 11:57:29\",\"area\":\"316省道53KM+200M\",\"act\":\"16362 : 驾驶中型以上载客载货汽车、校车、危险物品运输车辆以外的其他机动车在高速公路以外的道路上行驶超过规定时速20%以上未达50%的\",\"code\":\"\",\"fen\":\"6\",\"money\":\"100\",\"handled\":\"0\"}]}}";
					JSONObject jsonStr = JsonUtil.strToJson((Object)response);
					JSONObject resultStr = jsonStr.getJSONObject("result");
					//获取ArrayObject
			        if (resultStr.has("lists")) {
			            JSONArray transitListArray = resultStr.getJSONArray("lists");
			            for (int i = 0; i < transitListArray.size(); i++) {
			            	   Map<String,Object> mapT = new HashMap<String, Object>();
				               JSONObject obj = JSONObject.fromObject(transitListArray.getString(i));
				               mapT.put("act",obj.get("act"));//违章行为
				               mapT.put("date",obj.get("date"));//违章时间
				               mapT.put("area",obj.get("area"));//违章地点
				               mapT.put("fen",obj.get("fen"));//违章扣分
				               mapT.put("money",obj.get("money"));//违章罚款
				               mapT.put("handled",obj.get("handled"));//是否处理
				               list.add(mapT);
				         }
			        }
			        Long custId = getLoginUserId();
					if(custId!=null){//判断是否为登录用户
						List<Map<String,Object>> listTraffic = trafficUserInfoService.trafficList(custId, name+code+carCode);
						if(listTraffic!=null&&listTraffic.size()>0){
							trafficUserInfoService.removeByCarCode( name+code+carCode,custId);
						}
						//添加查询条件信息
						TrafficUserInfo trafficUserInfo = new TrafficUserInfo();
						trafficUserInfo.setCarCode(name+code+carCode);
						trafficUserInfo.setCarNo(carNo);
						trafficUserInfo.setCityCode(trafficViolation.getCityCode());
						trafficUserInfo.setCustId(custId);
						trafficUserInfo.setEngineNo(engineNo);
						trafficUserInfo.setFromCity(trafficViolation.getProvince()+"."+trafficViolation.getCityName());
					    trafficUserInfoService.add(trafficUserInfo);
					}
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
					
				} catch (Exception e) {
					model.setCode("0100");
			     	model.setError("系统未知异常！");
			        model.setMessage("调用失败");
			     	model.setState("0"); 
				}
			 }else{//查询城市不支持
				model.setCode("0100");
		     	model.setError("系统未知异常！");
		        model.setMessage("调用失败");
		     	model.setState("0"); 
		   }
		}else{//参数有问题
			model.setCode("0203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
	}
	/**
	 * 获取当前用户违章查询历史记录
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getCarCodeList", method = RequestMethod.POST)
	public Object getCarCodeList(){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		Long custId = getLoginUserId();
		if(custId!=null){//判断是否为登录用户
			List<Map<String,Object>> listTraffic = trafficUserInfoService.trafficList(custId,null);
			model.setCode("0000");
			model.setError("");
			if(listTraffic!=null&&listTraffic.size()>0){
				model.setResult(listTraffic);
			}else{
				model.setResult(list);
			}
			model.setMessage("调用成功");
			model.setState("1");
			
		}else{
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
	}
	/**
	 * 根据车牌号删除历史查询信息
	 * @param carCode
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/removeCarHis", method = RequestMethod.POST)
	public Object removeCarHis(String carCode){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		Long custId = getLoginUserId();
		if(!BeanUtil.isNullString(carCode)){
			if(custId!=null){//判断是否为登录用户
				try {
					trafficUserInfoService.removeByCarCode(carCode,custId);
					Map<String,Object> mapT = new HashMap<String, Object>();
					mapT.put("carCode", carCode);
					mapT.put("custId", custId);
					list.add(mapT);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				} catch (Exception e) {
					model.setCode("0100");
			     	model.setError("系统未知异常！");
			     	model.setResult(list);
			        model.setMessage("调用失败");
			     	model.setState("0");
				}
			}else{
				model.setCode("0300");
				model.setError("未登录");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		}
		else{
			model.setCode("0203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		} 
		return model;
	}
	
	/*----------------------------------------------违章查询end-------------------------------------------------------*/
	/**
	 * 保存啄木鸟信息数据
	 * @param jsonStr
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/saveWoodpecker", method = RequestMethod.POST)
	public Object saveWoodpecker(String  jsonStr){
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		Customer customer =  getLoginUser();
		Woodpecker woodpecker = new Woodpecker();
		if(customer!=null){
			JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
			if(jsonObject.has("comment")&&jsonObject.has("address")&&jsonObject.has("X")
					&&jsonObject.has("Y")&&jsonObject.has("images")){
				try {
					String comment = jsonObject.getString("comment");
					String address = jsonObject.getString("address");
					String X = jsonObject.getString("X");
					String Y = jsonObject.getString("Y");
					woodpecker.setCustId(customer.getId());
					woodpecker.setNickName(customer.getNickName()); 
					woodpecker.setHead(customer.getImgUrl());
					woodpecker.setAddress(address);
					woodpecker.setComment(comment);
					woodpecker.setX(X);
					woodpecker.setY(Y);
					woodpeckerService.add(woodpecker);
					JSONArray listArray = jsonObject.getJSONArray("images");
					for (int i = 0; i < listArray.size(); i++) {
					       JSONObject obj = JSONObject.fromObject(listArray.getString(i));
					       String imagePath ="";
					       if(!BeanUtil.isNullString(obj.get("base64").toString())){
					    	   imagePath = GenerateImage(obj.get("base64").toString());//图片编码
					    	   WoodpeckerPicture woodpeckerPicture = new WoodpeckerPicture();
					    	   woodpeckerPicture.setWoodPeckerId(woodpecker.getId());
					    	   woodpeckerPicture.setImagePath(imagePath);
					    	   woodpeckerPictureService.add(woodpeckerPicture);
					       }
					 }
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				} catch (Exception e) {
					model.setCode("0100");
			     	model.setError("系统未知异常！");
			     	model.setResult(list);
			        model.setMessage("调用失败");
			     	model.setState("0");
				}
				
			}else{
				model.setCode("0203");
				model.setError("参数缺失");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		}else{
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
	}
	/**
	 * 获取当前啄木鸟列表	
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value = "/getWoodpeckerList", method = RequestMethod.POST)
	public Object getWoodpeckerList(){
		JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		List<Map<String,Object>> listWoodpecker = new ArrayList<Map<String, Object>>();
		try {
			Customer customer =  getLoginUser();
			if(customer!=null){
				listWoodpecker = woodpeckerService.getList(customer.getId(), getPageBounds());
			}else{
				listWoodpecker = woodpeckerService.getList(null, getPageBounds());
			}
			if(listWoodpecker!=null&&listWoodpecker.size()>0){
		    	 for(Map<String, Object> mapApp :listWoodpecker){
			   		  for (Object k : mapApp.keySet())  
			   	      { 
			   	        if(k.toString().equalsIgnoreCase("id")){
			   	        	String id =mapApp.get(k).toString();
			   	        	mapApp.put("pictureList", woodpeckerPictureService.getPictureList( Long.valueOf(id).longValue()));
			   	      }
			   	    }
		   	     }
	      	}
			list = listWoodpecker;
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
		} catch (Exception e) {
			model.setCode("0100");
	     	model.setError("系统未知异常！");
	     	model.setResult(list);
	        model.setMessage("调用失败");
	     	model.setState("0");
		}
		return model;
	}
	
	@ResponseBody
    @RequestMapping(value = "/getOwnWoodpeckerList", method = RequestMethod.POST)
	public Object getOwnWoodpeckerList(){
		JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		try {
		    Customer customer =  getLoginUser();
			if(customer!=null){
				List<Map<String,Object>>  listWoodpecker = woodpeckerService.getOwnList(customer.getId(), getPageBounds());
				if(listWoodpecker!=null&&listWoodpecker.size()>0){
			    	 for(Map<String, Object> mapApp :listWoodpecker){
				   		  for (Object k : mapApp.keySet())  
				   	      { 
				   	        if(k.toString().equalsIgnoreCase("id")){
				   	        	String id =mapApp.get(k).toString();
				   	        	mapApp.put("pictureList", woodpeckerPictureService.getPictureList( Long.valueOf(id).longValue()));
				   	      }
				   	    }
			   	     }
		      	}
				list = listWoodpecker;
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			}else{
				model.setCode("0300");
				model.setError("未登录");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
			
		} catch (Exception e) {
			model.setCode("0100");
	     	model.setError("系统未知异常！");
	     	model.setResult(list);
	        model.setMessage("调用失败");
	     	model.setState("0");
		}
		return model;
	}
	//获取第三方接口的图片信息
    private  String  GenerateImage(String image){
    	//开始上传身份证头像信息
		String path=Config.getValue("picBaseUrl");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String fileName =  sdf.format(new Date())+".jpg";
		String imgFilePath = path + "/" + Constants.IMAGE_TYPE_WOODPECKER + "/"+ fileName;
    	try {
			BASE64Decoder decoder = new BASE64Decoder(); 
			byte[] b = decoder.decodeBuffer(image);
			for (int i = 0; i < b.length; ++i) {  
			    if (b[i] < 0) {// 调整异常数据  
			        b[i] += 256;  
			    }  
			}
			OutputStream out = new FileOutputStream(imgFilePath);  
			out.write(b);  
			out.flush();  
			out.close();
		  imgFilePath = imgFilePath.substring(imgFilePath.indexOf("/Image"), imgFilePath.length());
		  return imgFilePath;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  return "";
    }
    /*----------------------------------------------啄木鸟end-------------------------------------------------------*/
    /**
     * 获取驾校最新指数列表
     * @param areaCode
     * @param rate
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getDriverSchoolInfo", method = RequestMethod.POST)
	public Object getDriverSchoolInfo(String areaCode,String rate){
    	JsonResultModel model = getJsonResultModel();
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		Map<String,Object> monthMap= driverSchoolInfoSrvice.getDriverSchoolMonth();
		Long selectRate = Long.valueOf(rate).longValue();
		if(monthMap!=null){
			    try {
					String month = (String) monthMap.get("month");
					List<Map<String,Object>> listDriver = driverSchoolInfoSrvice.getDriverSchoolInfo(month,areaCode,selectRate,getPageBounds());
					list =listDriver; 
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				} catch (Exception e) {
					model.setCode("0100");
			     	model.setError("系统未知异常！");
			     	model.setResult(list);
			        model.setMessage("调用失败");
			     	model.setState("0");
				}
			    
		}else{
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
		}
		return model;
    }
    /**
     * 获取驾校历史指数
     * @param schoolId
     * @param n
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getDriverSchoolDetail", method = RequestMethod.POST)
	public Object getDriverSchoolDetail(String schoolId,String n){
    	JsonResultModel model = getJsonResultModel();
		List<DriverSchool> list = new ArrayList<DriverSchool>();
		Long id = Long.valueOf(schoolId).longValue();
		if(!BeanUtil.isNullString(schoolId)&&!BeanUtil.isNullString(n)&&Long.valueOf(n).longValue()<0){
			DriverSchool  driverSchool = driverSchoolService.getDriverSchool(id);
			Map<String,Object> mapMonth = driverSchoolInfoSrvice.getDriverSchoolMonth();
			if(driverSchool!=null&&mapMonth!=null){
				String month = (String) mapMonth.get("month");
				String startMonth = DateUtil.getCycle(month,n);
				List<Map<String,Object>> listSchoolInfo = driverSchoolInfoSrvice.getDriverSchoolHisInfo(month, startMonth, id);
				if(listSchoolInfo!=null&&listSchoolInfo.size()>0){
					driverSchool.setDriverSchoolInfoList(listSchoolInfo);
				}
				list.add(driverSchool);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			}else{
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("没有当前该驾校详细信息");
				model.setState("1");
			}
		}else{
			model.setCode("0200");
			model.setError("参数异常");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
    }
    /*----------------------------------------------驾校指数end-------------------------------------------------------*/
    @ResponseBody
    @RequestMapping(value = "/saveHotLine", method = RequestMethod.POST)
	public Object saveHotLine(String title,String content,String questionType,String isPublic,String cityCode){
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		Customer customer =  getLoginUser();
		if(customer!=null){
			 if(!BeanUtil.isNullString(title)&&!BeanUtil.isNullString(content)&&!BeanUtil.isNullString(questionType)&&
					 !BeanUtil.isNullString(isPublic)&&!BeanUtil.isNullString(cityCode)){
				 try {
					if(!checkSensitiveWord("hotline",content)&&!checkSensitiveWord("hotline",title)){
						 HotLine hotLine =new HotLine();
						 hotLine.setCustId(customer.getId());
						 hotLine.setContent(content);
						 hotLine.setCityCode(cityCode);
						 hotLine.setIsPublic(isPublic);
						 hotLine.setQuestionType(questionType);
						 hotLine.setTitle(title);
						 hotLineService.add(hotLine);
						 if(hotLine.getId()!=0l){
							 HotLineAnswer hotLineAnswer =new HotLineAnswer();
							 hotLineAnswer.setAnswerName("");
							 hotLineAnswer.setTitle("谢谢您对12345市民服务热线工作的支持");
							 hotLineAnswer.setHotLineId(hotLine.getId());
							 hotLineAnswer.setContent("");
							 hotLineAnswerService.add(hotLineAnswer);
						 }
					   }
					    model.setCode("0000");
						model.setError("");
						model.setResult(list);
						model.setMessage("调用成功");
						model.setState("1");
				  } catch (Exception e) {
					    model.setCode("0100");
				     	model.setError("未知异常！");
				     	model.setResult(list);
				        model.setMessage("调用失败");
				     	model.setState("0");
				}
			 }else{
				    model.setCode("0200");
			     	model.setError("参数异常！");
			     	model.setResult(list);
			        model.setMessage("调用失败");
			     	model.setState("0");
			 }
		}else{
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		return model;
    }
    /**
     * 获取12345历史信息
     * @param cityCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getHotLine", method = RequestMethod.POST)
	public Object getHotLine(String cityCode){
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
			try {
				    list = hotLineService.getHotLine(cityCode);
				    model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
			} catch (Exception e) {
				    model.setCode("0100");
			     	model.setError("未知异常！");
			     	model.setResult(list);
			        model.setMessage("调用失败");
			     	model.setState("0");
			}
		
		return model;
    }
    /*----------------------------------------------啄木鸟for native-------------------------------------------------------*/
    /**
	 * 提交啄木鸟信息数据
	 * 参数：jsonStr
	 * 开发人员：王建法
	 * 涉及数据库表：cust_peckerInfo,pecker_picture,pecker_address
	 * */
	@ResponseBody
	@RequestMapping(value = "/saveWoodpeckerForNative",params = {"jsonStr"}, method = RequestMethod.POST)
	public Object saveWoodpeckerForNative(String jsonStr){
		logger.info("--------------saveWoodpeckerForNative(start)-------------"+"|"+"fromModule:NativeAppController");
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		Customer customer =  getLoginUser();
		Map<String,Object> map = new HashMap<String, Object>();
		if(customer!=null){
			logger.info("--------------saveWoodpeckerForNative(当前用户ID)-------------custId:"+customer.getId());
			JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
			if(jsonObject.has("content")&&jsonObject.has("address")&&jsonObject.has("lat")
					&&jsonObject.has("lng")&&jsonObject.has("images")&&jsonObject.has("title")
					&&jsonObject.has("addressImg")&&jsonObject.has("open")){
				String comment = jsonObject.getString("content");
				logger.info("--------------saveWoodpeckerForNative(问题内容)-------------comment:"+comment);
				String address = jsonObject.getString("address");
				logger.info("--------------saveWoodpeckerForNative(问题所属地址)-------------comment:"+address);
				String addressX = jsonObject.getString("lat");
				logger.info("--------------saveWoodpeckerForNative(X坐标)纬度-------------lat:"+addressX);
				String addressY = jsonObject.getString("lng");
				logger.info("--------------saveWoodpeckerForNative(Y坐标)经度-------------lng:"+addressY);
				String title = jsonObject.getString("title");
				logger.info("--------------saveWoodpeckerForNative(标题)-------------标题:"+title);
				String open = jsonObject.getString("open");
				logger.info("--------------saveWoodpeckerForNative(open)-------------open:"+open);
				String cityCode = getCityCode();
				logger.info("--------------saveWoodpeckerForNative(cityCode)-------------cityCode:"+cityCode);
				
				PeckerInfo peckerInfoConn = new PeckerInfo();//用户查询peckerInfo的Id，和地址、图片关联
				PeckerInfo peckerInfo = new PeckerInfo();
				PeckerAddress peckerAddress = new PeckerAddress();
				try {
					peckerInfo.setCityCode(cityCode);
					peckerInfo.setContent(comment);
					peckerInfo.setCustId(customer.getId());
					peckerInfo.setTitle(title);
					peckerInfo.setReplayStatus("0");
					peckerInfo.setOpen(open);
					String randomStr = String.valueOf((int)(Math.random() * 100));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					String timeStr =  sdf.format(new Date());
					peckerInfo.setNumber("ZMN" + timeStr + randomStr);
					peckerInfoService.add(peckerInfo);
					logger.info("--------------添加信息至cust_peckerinfo完成--------------");
					peckerInfoConn = peckerInfoService.findByCustId(customer.getId());
					if(peckerInfoConn == null){
						logger.error("--------------cust_peckerinfo表中未查询到该用户的申报信息--------------");
						model.setCode("0100");
						model.setError("未知异常！");
						model.setResult(list);
						model.setMessage("调用失败");
						model.setState("0");
						return model;
					}
					Long peckerId = peckerInfoConn.getId();
					logger.info("--------------查询得到的peckerId：" + peckerId);
					//开始添加pecker_address表信息
					String addressImg = jsonObject.getString("addressImg");
					String addressImagePath = "";
					if (!BeanUtil.isNullString(addressImg)) {
						addressImagePath = GenerateImage(addressImg);// 图片编码
						logger.info("--------------经纬度地址图片位置：" + addressImagePath);
					}
					peckerAddress.setAddress(address);
					peckerAddress.setAddressX(addressX);
					peckerAddress.setAddressY(addressY);
					peckerAddress.setPeckerId(peckerId);
					peckerAddress.setAddressImg(addressImagePath);
					peckerAddressService.add(peckerAddress);
					logger.info("--------------添加信息至pecker_address完成--------------");
					//开始添加pecker_picture表信息
					JSONArray listArray = jsonObject.getJSONArray("images");
					Map<String, Object> picMap = new HashMap<String, Object>();
					List<PeckerPicture> pictureList = new ArrayList<PeckerPicture>();
					if(listArray!=null&&listArray.size()>0){
						for (int i = 0; i < listArray.size(); i++) {
							JSONObject obj = JSONObject.fromObject(listArray.getString(i));
							PeckerPicture peckerPicture = new PeckerPicture();
							String imagePath = "";
							if (!BeanUtil.isNullString(obj.get("base64").toString())) {
								imagePath = GenerateImage(obj.get("base64").toString());// 图片编码
								logger.info("--------------申报图片位置：" + imagePath);
							}
							peckerPicture.setImagePath(imagePath);
							peckerPicture.setPeckerId(peckerId);
							pictureList.add(peckerPicture);
					    }
						picMap.put("picList", pictureList);
						logger.info("--------------图片添加至Map完成---------------");
					}
					peckerPictureService.addList(picMap);
					logger.info("--------------list添加至数据库完成---------------");
					map.put("number","ZMN" +  timeStr + randomStr);
					map.put("peckerId", peckerId);
					list.add(map);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				}catch (Exception e) {
					// TODO: handle exception
					logger.error("--------------exception：" + e.toString());
					model.setCode("0100");
					model.setError("未知异常！");
					model.setResult(list);
					model.setMessage("调用失败");
					model.setState("0");
					return model;
				}
			}else {
				logger.error("--------------saveWoodpeckerForNative(参数缺失)-------------e:"+jsonObject.toString());
				model.setCode("0203");
				model.setError("参数缺失");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		}else {
			logger.error("--------------saveWoodpeckerForNative(未登录)-------------");
			model.setCode("0300");
			model.setError("未登录");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
		logger.info("--------------saveWoodpeckerForNative(end)-------------"+"|"+"fromModule:NativeAppController");
		return model;
	}
	/**
	 * 提交啄木鸟回复信息数据
	 * 参数：jsonStr
	 * 开发人员：王建法
	 * 涉及数据库表：cust_peckerInfo,pecker_picture,pecker_address,pecker_answer
	 * */
    @ResponseBody
    @RequestMapping(value = "/saveWoodpeckerAnswerForNative",params = {"jsonStr"}, method = RequestMethod.POST)
    public Object saveWoodpeckerAnswerForNative(String jsonStr){
    	logger.info("--------------saveWoodpeckerAnswerForNative(start)-------------"+"|"+"fromModule:NativeAppController");
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
    	Customer customer = getLoginUser();
		Map<String,Object> map = new HashMap<String, Object>();
		JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
		PeckerAnswer peckerAnswer = new PeckerAnswer();
		if(jsonObject.has("address")&&jsonObject.has("lat")&&jsonObject.has("lng")&&jsonObject.has("addressImg")
				&&jsonObject.has("answerType")&&jsonObject.has("toAnswer")&&jsonObject.has("fromAnswer")&&jsonObject.has("woodPeckerId")
				&&jsonObject.has("images")&&jsonObject.has("content")&&jsonObject.has("contentType")){
			String answerType = jsonObject.getString("answerType");
			logger.info("--------------saveWoodpeckerAnswerForNative(回复类型)-------------answerType:"+answerType);
			String address = jsonObject.getString("address");
			logger.info("--------------saveWoodpeckerAnswerForNative(回复所属地址)-------------comment:"+address);
			String addressX = jsonObject.getString("lat");
			logger.info("--------------saveWoodpeckerAnswerForNative(X坐标)纬度-------------lat:"+addressX);
			String addressY = jsonObject.getString("lng");
			logger.info("--------------saveWoodpeckerAnswerForNative(Y坐标)经度-------------lng:"+addressY);
			String toAnswer = jsonObject.getString("toAnswer");
			logger.info("--------------saveWoodpeckerAnswerForNative(回复了谁)-------------回复了谁:"+toAnswer);
			String fromAnswer = jsonObject.getString("fromAnswer");
			logger.info("--------------saveWoodpeckerAnswerForNative(谁回复)-------------谁回复:"+fromAnswer);
			String content = jsonObject.getString("content");
			logger.info("--------------saveWoodpeckerAnswerForNative(回复内容)-------------回复内容:"+content);
			String contentType = jsonObject.getString("contentType");
			logger.info("--------------saveWoodpeckerAnswerForNative(回复内容类型)-------------回复内容类型:"+contentType);
			Long peckerId = jsonObject.getLong("woodPeckerId");
			logger.info("--------------saveWoodpeckerAnswerForNative(啄木鸟说咨询id)-------------啄木鸟说咨询id:"+peckerId);
			peckerAnswer.setAnswerType(answerType);
			if(!BeanUtil.isNullString(answerType)){
				if (answerType.equalsIgnoreCase("0")) {
					logger.info("-------------用户回复--------------customer:" + customer.getId() + "|fromAnswer:" + fromAnswer);
					peckerAnswer.setFromAnswer(fromAnswer);
				}else{
					logger.info("-------------客服回复--------------custService|fromAnswer:" + fromAnswer);
					peckerAnswer.setFromAnswer(fromAnswer);
				}
			}else{
				logger.error("--------------saveWoodpeckerAnswerForNative(参数answerType缺失)-------------answerType:"+answerType);
				model.setCode("0203");
				model.setError("参数缺失");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
			peckerAnswer.setToAnswer(toAnswer);
			peckerAnswer.setPeckerId(peckerId);
			peckerAnswer.setIsReaded(0l);
			peckerAnswer.setContentType(contentType);
			if(contentType.equalsIgnoreCase("text")){
				peckerAnswer.setAnswerContent(content);
			}
			try {
				peckerAnswerService.add(peckerAnswer);
				logger.info("--------------添加记录到pecker_answer成功--------------");
				PeckerAnswer peckerAnswerConn = peckerAnswerService.findByPeckerId(peckerId);//用于关联pecker_address、pecker_picture
				logger.info("--------------获取到最新一条peckerId：" + peckerId + "|peckerAnswerConn" + peckerAnswerConn.toString() + "--------------");
				PeckerInfo peckerInfo = peckerInfoService.get(peckerId);
				if(peckerInfo != null){
					if(answerType.equalsIgnoreCase("1") && peckerInfo.getReplayStatus().equalsIgnoreCase("0")){
						peckerInfo.setReplayStatus("1");
						peckerInfoService.update(peckerInfo);
						logger.info("--------------如果是客服回复的更新cust_peckerinfo表中的replayStatus字段--------------");
					}
					
					Long answerId = peckerAnswerConn.getId();
					//开始添加pecker_address表信息
					if(contentType.equalsIgnoreCase("adr")){
						PeckerAddress peckerAddress = new PeckerAddress();
						String addressImg = jsonObject.getString("addressImg");
						String addressImagePath = "";
						if (!BeanUtil.isNullString(addressImg)) {
							addressImagePath = GenerateImage(addressImg);// 图片编码
							logger.info("--------------经纬度地址图片位置：" + addressImagePath);
						}
						peckerAddress.setAddress(address);
						peckerAddress.setAddressX(addressX);
						peckerAddress.setAddressY(addressY);
						peckerAddress.setPeckerId(peckerId);
						peckerAddress.setAnswerId(answerId);
						peckerAddress.setAddressImg(addressImagePath);
						peckerAddressService.add(peckerAddress);
						logger.info("--------------添加信息至pecker_address完成--------------");
					}
					if(contentType.equalsIgnoreCase("img")){
						//开始添加pecker_picture表信息
						JSONArray listArray = jsonObject.getJSONArray("images");
						Map<String, Object> picMap = new HashMap<String, Object>();
						List<PeckerPicture> pictureList = new ArrayList<PeckerPicture>();
						if(listArray!=null&&listArray.size()>0){
							for (int i = 0; i < listArray.size(); i++) {
								JSONObject obj = JSONObject.fromObject(listArray.getString(i));
								PeckerPicture peckerPicture = new PeckerPicture();
								String imagePath = "";
								if (!BeanUtil.isNullString(obj.get("base64").toString())) {
									imagePath = GenerateImage(obj.get("base64").toString());// 图片编码
									logger.info("--------------申报图片位置：" + imagePath);
								}
								peckerPicture.setImagePath(imagePath);
								peckerPicture.setPeckerId(peckerId);
								peckerPicture.setAnswerId(answerId);
								pictureList.add(peckerPicture);
						    }
							picMap.put("picList", pictureList);
							logger.info("--------------图片添加至Map完成---------------");
						}
						peckerPictureService.addList(picMap);
						logger.info("--------------list添加至数据库完成---------------");
					}
					map.put("number", peckerInfo.getNumber());
					list.add(map);
					model.setCode("0000");
					model.setError("");
					model.setResult(list);
					model.setMessage("调用成功");
					model.setState("1");
				}else{
					logger.error("--------------cust_peckerInfo表中未查询到peckerId对应的记录--------------");
					model.setCode("0100");
					model.setError("未知异常！");
					model.setResult(list);
					model.setMessage("调用失败");
					model.setState("0");
				}
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("--------------exception：" + e.toString());
				model.setCode("0100");
				model.setError("未知异常！");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
				return model;
			}
			
		}else{
			logger.error("--------------saveWoodpeckerAnswerForNative(参数缺失)-------------e:"+jsonObject.toString());
			model.setCode("0203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
    	logger.info("--------------saveWoodpeckerAnswerForNative(end)-------------"+"|"+"fromModule:NativeAppController");
    	return model;
    }
    /**
	 * 啄木鸟评价接口
	 * 参数：jsonStr
	 * 开发人员：王建法
	 * 涉及数据库表：pecker_evaluate
	 * */
    @ResponseBody
    @RequestMapping(value = "/evaluateWoodpeckerForNative",params = {"jsonStr"}, method = RequestMethod.POST)
    public Object evaluateWoodpeckerForNative(String jsonStr){
    	logger.info("--------------evaluateWoodpeckerForNative(start)-------------"+"|"+"fromModule:NativeAppController");
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		JSONObject jsonObject = JsonUtil.strToJson((Object)jsonStr);
		PeckerEvaluate peckerEvaluate = new PeckerEvaluate();
		if(jsonObject.has("peckerId") && jsonObject.has("evaluateContent") && jsonObject.has("evaluateScore") && jsonObject.has("open")){
			try {
				logger.info("--------------jsonObject:" + jsonObject.toString());
				Long peckerId  = jsonObject.getLong("peckerId");
				String evaluateContent = jsonObject.getString("evaluateContent");
				String evaluateScore = jsonObject.getString("evaluateScore");
				String open = jsonObject.getString("open");
				peckerEvaluate.setPeckerId(peckerId);
				peckerEvaluate.setEvaluateContent(evaluateContent);
				peckerEvaluate.setEvaluateScore(evaluateScore);
				peckerEvaluate.setOpen(open);
				peckerEvaluateService.add(peckerEvaluate);
				logger.info("--------------添加至pecker_evaluate表成功--------------");
				PeckerInfo peckerInfo = peckerInfoService.get(peckerId);
				if(peckerInfo != null){
					peckerInfo.setReplayStatus("3");
					peckerInfoService.update(peckerInfo);
					logger.info("--------------cust_peckerinfo设置为已评价--------------");
				}else{
					logger.error("--------------未查询到该条评价对应的申报信息--------------");
					model.setCode("0100");
					model.setError("未知异常！");
					model.setResult(list);
					model.setMessage("调用失败");
					model.setState("0");
					return model;
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("--------------exception：" + e.toString());
				model.setCode("0100");
				model.setError("未知异常！");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
				return model;
			}
		}else{
			logger.error("--------------evaluateWoodpeckerForNative(参数缺失)-------------e:"+jsonObject.toString());
			model.setCode("0203");
			model.setError("参数缺失");
			model.setResult(list);
			model.setMessage("调用失败");
			model.setState("0");
		}
    	logger.info("--------------evaluateWoodpeckerForNative(end)-------------"+"|"+"fromModule:NativeAppController");
    	return model;
    }
    /**
     * 查询啄木鸟历史信息数据
     * by wanghuadong
     * 关联数据库:cust_peckerinfo, pecker_address,pecker_picture,pecker_answer
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getOwnWoodpeckerListForNative", method = RequestMethod.POST)
	public Object getOwnWoodpeckerListForNative(){
		logger.info("--------------getOwnWoodpeckerListForNative(start)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceInfo:获取啄木鸟历史信息");
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
    	String cityCode ="";
		if(getClientInfo()!=null){
			cityCode= getClientInfo().getCityCode();
		}
    	try {
			Customer customer =  getLoginUser();
			if(customer!=null){
				logger.info("--------------getOwnWoodpeckerListForNsative(开始查询啄木鸟历史信息)-------------"+"|"+"customerId:"+customer.getId()+"|customerPhone:"+customer.getMobilePhone());
				list = peckerInfoService.getOwnPecker(customer.getId(),cityCode);
				if (list != null && list.size() > 0) {
					for (Map<String, Object> mapApp : list) {
						mapApp.put("nickName",customer.getNickName()); 
						mapApp.put("head",customer.getImgUrl());
						long peckerId =Long.valueOf(mapApp.get("id").toString()).longValue();
			   	        List<Map<String,Object>> listPic = peckerPictureService.getPicByPeckerId(peckerId);
						logger.info("--------------getPicByPeckerId(获取啄木鸟对应的图片信息)-------------"+"|listPic.size: "+listPic.size());
						if (listPic != null && listPic.size() > 0) {
							mapApp.put("pictureList", listPic);
						}
						Map<String,Object> map = peckerAnswerService.getUnReadNum(peckerId);
						logger.info("--------------getUnReadNum(获取未读的回复数)-------------"+"|UnReadNum: "+map);
						mapApp.put("unRead", map.get("count(distinct id)"));
					}
				}
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			}else{
				logger.info("--------------getOwnWoodpeckerListForNative(error)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceName:getOwnWoodpeckerListForNative"+"|"+"error:用户调用该接口时没有登录！");
				model.setCode("0300");
				model.setError("未登录");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		} catch (Exception e) {
			logger.info("--------------getOwnWoodpeckerListForNative(error)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceName:getOwnWoodpeckerListForNative"+"|"+"error:"+e.toString());
			model.setCode("0100");
	     	model.setError(e.toString());
	     	model.setResult(list);
	        model.setMessage("调用失败");
	     	model.setState("0");
		}
    	logger.info("--------------getOwnWoodpeckerListForNative(end)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceInfo:获取啄木鸟历史信息");
    	return model;
    }
    /**
     * 查询啄木鸟详情信息数据
     * by wanghuadong
     * 关联数据库:cust_peckerinfo, pecker_address,pecker_picture,pecker_answer
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/woodpeckerDetailForNative", method = RequestMethod.POST)
	public Object woodpeckerDetailForNative(Long id){
		logger.info("--------------woodpeckerDetailForNative(start)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceInfo:获取啄木鸟详情信息");
    	JsonResultModel model = getJsonResultModel();
    	List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
    	try {
			Customer customer =  getLoginUser();
			if(customer!=null){
			Map<String,Object> mapPeck = peckerInfoService.getPeckerById(id);
				mapPeck.put("nickName", customer.getNickName());
				mapPeck.put("head", customer.getImgUrl());
				Long peckerId = id;
				List<Map<String, Object>> listPic = peckerPictureService.getPicByPeckerId(peckerId);
				logger.info("--------------getPicByPeckerId(获取啄木鸟对应的图片信息)-------------"+ "|listPic.size: " + listPic.size());
				if (listPic != null && listPic.size() > 0) {
					mapPeck.put("pictureList", listPic);
				}
				List<Map<String, Object>> listAns = peckerAnswerService.getAnsByPeckerId(peckerId);
				logger.info("--------------getAnsByPeckerId(获取啄木鸟对应的回复信息)-------------"+ "|listAns.size: " + listAns.size());
				if (listAns != null && listAns.size() > 0) {
					for (Map<String, Object> mapApp : listAns) {
						String type = "";
						type = (String) mapApp.get("contentType");
						Long answerId = Long.valueOf(mapApp.get("id").toString()).longValue();
						if(type.equalsIgnoreCase("text")){
							Map<String,Object> mapText= peckerAnswerService.getAnswerText(answerId);
    						logger.info("--------------getAnswerText(获取回复的文本信息)-------------"+"|mapText: "+mapText.toString());
							mapApp.put("answerContent", mapText.get("answerContent"));
						}
                        if(type.equalsIgnoreCase("img")){
                        	List<Map<String,Object>> l = peckerPictureService.getPicByAnswerId(answerId);
    						logger.info("--------------getPicByAnswerId(获取回复的图片信息)-------------"+"|l.size: "+l.size());
    						if (l != null && l.size() > 0) {
    							mapApp.put("pictureList", l);
    						}
						}
                        if(type.equalsIgnoreCase("adr")){
                        	Map<String,Object> mapAdr = peckerAddressService.getAdrByAnswerId(answerId);
    						logger.info("--------------getAdrByAnswerId(获取回复的地址信息)-------------"+"|mapAdr: "+mapAdr.toString());
                        	mapApp.put("address", mapAdr.get("address"));
                        	mapApp.put("addressX", mapAdr.get("addressX"));
                        	mapApp.put("addressY", mapAdr.get("addressY"));
                        	mapApp.put("addressImg", mapAdr.get("addressImg"));
						}
                        PeckerAnswer peckerAnswer = peckerAnswerService.get(answerId);
                        if(peckerAnswer != null){
                        	peckerAnswer.setIsReaded((long) 1);
                        	peckerAnswerService.update(peckerAnswer);
    						logger.info("--------------完成回复信息阅读状态的更新-------------"+"answerId: "+answerId);
                        }
					}
					mapPeck.put("answer", listAns);
				}
				list.add(mapPeck);
				model.setCode("0000");
				model.setError("");
				model.setResult(list);
				model.setMessage("调用成功");
				model.setState("1");
			}else{
				logger.info("--------------woodpeckerDetailForNative(error)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceName:woodpeckerDetailForNative"+"|"+"error:用户调用该接口时没有登录！");
				model.setCode("0300");
				model.setError("未登录");
				model.setResult(list);
				model.setMessage("调用失败");
				model.setState("0");
			}
		} catch (Exception e) {
			logger.info("--------------woodpeckerDetailForNative(error)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceName:woodpeckerDetailForNative"+"|"+"error:"+e.toString());
			model.setCode("0100");
	     	model.setError(e.toString());
	     	model.setResult(list);
	        model.setMessage("调用失败");
	     	model.setState("0");
		}
    	logger.info("--------------woodpeckerDetailForNative(end)-------------"+"|"+"fromModule:NativeAppController"+"|"+"interfaceInfo:获取啄木鸟详情信息");
    	return model;
		}

	// 添加咨询敏感词过滤
	private boolean checkSensitiveWord(String objectType, String content) {
		List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
		List<SensitiveHist> list = new ArrayList<SensitiveHist>();
		content = StringUtils.defaultString(content);
		for (SensitiveWords sensitiveWord : sensitiveWords) {
			Boolean hasSensitiveWord = content
					.contains(sensitiveWord.getWord());
			if (hasSensitiveWord) {
				// 添加到敏感词记录表中
				SensitiveHist sensitiveHist = new SensitiveHist();
				sensitiveHist.setCustId(getLoginUserId());
				sensitiveHist.setObjectType(objectType);
				sensitiveHist.setObjectId(0l);
				sensitiveHist.setSensitiveWord(sensitiveWord.getWord());
				sensitiveHistService.add(sensitiveHist);
				list.add(sensitiveHist);
			}
		}
		if (list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
}

