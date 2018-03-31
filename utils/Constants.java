package com.inspur.icity.web.utils;

/**
 * 基础常量定义专用类
 */
public class Constants {

	// 生产库URL
	public static final String HTTP_URL = Config.getValue("HTTP_URL");

	/** 图片类型 TODO*/
	public static final String SYSTEM_TYPE = Config.getValue("SYSTEM_TYPE");
	// 用户相关图片目录
	public static final String IMAGE_TYPE_USER = "User";
	// 政府事务
	public static final String IMAGE_TYPE_GOVERNMENTAFFAIR = "GovernmentAffair";
	// 资讯
	public static final String IMAGE_TYPE_INFORMATION = "Information";
	// 生活
	public static final String IMAGE_TYPE_LIFE = "Life";
	//图片临时存放目录
	public static final String IMAGE_TYPE_TEMP = "temp";
	//啄木鸟图片存放目录
	public static final String IMAGE_TYPE_WOODPECKER = "WoodPecker";
	//点赞表、评论表
	public static final String TYPE_NEWS = "news";

	public static final String TYPE_GOV = "gov";

	public static final String TYPE_CONSULTS = "consults";

	public static final String TYPE_COMMENT = "comment";

	public static final String TYPE_COMPLAIN = "complain";
	
	public static final String ACTIVITY_TYPE_INVITE_CODE = "7";//绑定邀请码参与活动类型

	public static final String WATERRETE = "waterRate";//水
	public static final String POWERRATE = "powerRate";//电
	public static final String GASRATE = "gasRate";//燃气
	public static final String HEATINGRATE = "heatingRate";//暖气
	public static final String WPGH_KEY = "MFtd0xrLwARurAM";//水电煤暖加密key值
	public static final String CHANNEL = "1007";//水电煤暖通道号
    
    //银联appKey
    public static final String UMS_APPKEY = Config.getValue("ums.appKey");
    
    //银联appId
    public static final String UMS_APPID = Config.getValue("ums.appId");
    
    //银联获取accesstoken请求地址
    public static final String UMS_ACCESSTOKEN_URL = Config.getValue("ums.accesstoken.url");
    
    //银联实名认证请求地址
    public static final String UMS_CHECK_URL = Config.getValue("ums.ckeck.url");
    
    //银联实名认证身份证证件类型
    public static final String UMS_IDCARD_TYPE = "01";
    
    //银联实名认证接口返回系统错误码（正常）
    public static final String UMS_NORMAL_CODE = "0000";
    //图片验证码属性
    public static final String IMAGECODE="imageVerifyCode";
    //银联实名认证接口返回应答码
    public static enum UmsAnswerCode {
    	SUCCESS("00"), FAIL("01"), NONSUPPORT("02"), WRONGFORMAT("03"), ERROR("04");
    	
    	private String code;

		private UmsAnswerCode(String code) {
			this.code = code;
		}
		
		public String getValue() {
	        return code;
	    }
		
		public String toString() {
           return String.valueOf (this.code);
		}
    }
}
