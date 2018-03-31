package com.inspur.icity.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.core.model.JsonResultModel;
import com.inspur.icity.core.utils.BeanUtil;
import com.inspur.icity.logic.base.model.Dict;
import com.inspur.icity.logic.base.model.NewsTab;
import com.inspur.icity.logic.base.service.DictService;
import com.inspur.icity.logic.base.service.NewsTabService;
import com.inspur.icity.logic.cust.model.Comment;
import com.inspur.icity.logic.cust.model.News;
import com.inspur.icity.logic.cust.model.Praise;
import com.inspur.icity.logic.cust.service.CommentService;
import com.inspur.icity.logic.cust.service.NewsService;
import com.inspur.icity.logic.cust.service.PraiseService;
import com.inspur.icity.logic.news.model.Msg;
import com.inspur.icity.logic.news.service.HotNewsService;
import com.inspur.icity.logic.news.service.MsgService;
import com.inspur.icity.logic.sensitive.model.SensitiveHist;
import com.inspur.icity.logic.sensitive.model.SensitiveWords;
import com.inspur.icity.logic.sensitive.service.SensitiveHistService;
import com.inspur.icity.logic.sensitive.service.SensitiveWordService;
import com.inspur.icity.web.cust.builder.CommentToMapBuilder;
import com.inspur.icity.web.news.builder.MsgToMapBuilder;
import com.inspur.icity.web.utils.Constants;
import com.rabbitmq.client.AMQP.Basic.Return;

/**
 * 资讯相关接口
 */
@Controller
@RequestMapping(value = "/news")
public class NewsController extends BaseAuthController {

	@Autowired
	DictService dictService;
	@Autowired
	MsgService msgService;
	@Autowired
	CommentService commentService;
	@Autowired
	SensitiveWordService sensitiveWordService;
	@Autowired
	SensitiveHistService sensitiveHistService;
	@Autowired
	HotNewsService hotNewsService;
	@Autowired
	PraiseService praiseService;
	@Autowired
	NewsService newsService;
	@Autowired
	NewsTabService newsTabService;
	@Autowired
	MsgToMapBuilder msgToMapBuilder;
	@Autowired
	CommentToMapBuilder commentToMapBuilder;

	Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 资讯列表
	 * @param type 资讯类别
	 * @param cityCode 城市标识
	 * @return 资讯一览
	 */
	@ResponseBody
	@RequestMapping(value = "/list", params = { "cityCode", "type" })
	public Object findNewsList(String type, String cityCode) {
		/*if(getVersion()!=null){
			if(getVersion()<231){*/
		return msgService.findNewsList(type, cityCode, getPageBounds());
		/*}
		}
		return msgService.findNewsList_v2(type, cityCode, getPageBounds());*/
	}

	/**
	 * 资讯评论页的资讯详情
	 * @param newsId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "{newsId}/newsView")
	public Object newsView(@PathVariable Long newsId) {
		Map map = Maps.newHashMap();
		Msg msg = msgService.get(newsId);
		map.put("id", msg.getId());
		map.put("title", msg.getTitle());
		map.put("createTime", msg.getCreateTime());
		map.put("imgUrl", msg.getImgUrl());
		map.put("gotoUrl", msg.getGotoUrl());
		map.put("commentCount", commentService.findCommentCount("news", newsId));
		return map;
	}

	/**
	 * 获取热门资讯
	 * @param cityCode 城市标识
	 * @return 热门资讯一览
	 */
	@ResponseBody
	@RequestMapping(value = "/hotNews", params = { "cityCode" })
	public Object hotNews(String cityCode) {
		PageBounds page = getPageBounds();
		if (page.getLimit() > 8) {
			page.setLimit(8);
		}
		return hotNewsService.hotNews(cityCode, page);
	}

	/** 
	 * @Description  资讯详情
	 * @param newsId 咨询ID
	 * @param h5Flag H5访问标志
	 * @return 资讯详情信息
	 * @date 2017年5月26日下午2:18:31
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}")
	public Object findNewsDetails(@PathVariable Long newsId, @RequestParam(value = "h5Flag", required = false) String h5Flag) {
		//String h5Flag=null;
		logger.info("---------findNewsDetails----fromModule:NewsController|newsId: " + newsId + "|h5Flag: " + h5Flag);
		Long deviceId = getDeviceId();
		Long custId = getLoginUserId();
		if (custId != null) {
			return msgService.findNewsDetails(deviceId, custId, newsId, h5Flag);
		} else {
			return msgService.findNewsDetails(deviceId, newsId, h5Flag);
		}
	}

	/**
	 * 资讯详情
	 * @param newsId 资讯Id
	 * @return 资讯详情信息
	 */
	@ResponseBody
	@RequestMapping(value = "/shared/{newsId}")
	public Object sharedNewsDetails(@PathVariable Long newsId, @RequestParam(value = "h5Flag", required = false) String h5Flag) {
		Long deviceId = getDeviceId();
		return msgService.findNewsDetails(deviceId, newsId, h5Flag);
	}

	/**
	 * 发表评论（登录）
	 * @param objectType 评论类型 objectId 资讯id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addComment/{objectType}/{objectId}", method = RequestMethod.POST)
	public Object comment(@PathVariable String objectType, @PathVariable Long objectId, @RequestParam String comment, @RequestParam(required = false) Long commentId) throws Exception {
		if (getLoginUserId() != null) {
			Integer version = getVersion();
			Map<String, Object> map = addComment(objectType, objectId, comment, commentId);
			if (version > 232) {
				String flag = map == null ? "" : map.get("flag").toString();
				if ("1".equals(flag)) {
					return map == null ? new Comment() : map.get("comments");
				}
			}
			/*String matchWord = addComment(objectType, objectId,comment, commentId);
			if (matchWord != null) {
			    throw new ApplicationException("您发布的评论内容中包含敏感文字，请重新编辑后再提交。");
			}*/
		} else {
			throw new ApplicationException(900, "您还没有登录，不能进行评论");
		}
		return "";
	}

	/**
	 * 评论列表--资讯的评论列表
	 * @param newsId
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/comments")
	public Object newsComments(@PathVariable Long newsId) {
		return map(commentService.eachModuleComments(Constants.TYPE_NEWS, newsId, getPageBounds()), comment -> {
			Map<String, Object> map = commentToMapBuilder.build((Comment) comment, getDeviceId(), getLoginUserId());
			if (((Comment) comment).getVersion() != null && ((Comment) comment).getVersion() >= 240) {
				String content = (String) map.get("comment");
				if (map.containsKey("replayTo")) {
					content = "@" + map.get("replayTo") + ": " + content;
					map.put("comment", content);
				}
			}
			return map;
		});
	}

	/**
	 * 2.4.0新版本评论列表--资讯的评论列表
	 * @param newsId
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/commentsForNew")
	public Object newsCommentsForNew(@PathVariable Long newsId) {
		logger.info("--------------newsCommentsForNew(start)-------------" + "|" + "fromModule:NewsController" + "|" + "newsId:新闻编号" + newsId);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<?> commentsList = map(commentService.eachModuleCommentsForNew(Constants.TYPE_NEWS, newsId, getPageBounds()), comment -> {
				Map<String, Object> map = new HashMap<String, Object>();
				List<Map<String, Object>> replayList = new ArrayList<Map<String, Object>>();
				map = commentToMapBuilder.build((Comment) comment, getDeviceId(), getLoginUserId());
				List<Comment> replayCommentsList = commentService.getReplayComments(((Comment) comment).getId());
				for (Comment replayComment : replayCommentsList) {
					Map<String, Object> replayMap = commentToMapBuilder.build((Comment) replayComment, getDeviceId(), getLoginUserId());
					if (getLoginUserId() != null) {
						if (getLoginUserId().equals(replayMap.get("custId"))) {
							replayMap.put("isMine", "1");
						} else {
							replayMap.put("isMine", "0");
						}
					}
					if (replayComment.getVersion() == null || replayComment.getVersion() < 240) {
						String content = (String) replayMap.get("comment");
						content = content.substring(content.indexOf(": ") + 1, content.length());
						replayMap.put("comment", content);
					}
					replayList.add(replayMap);
				}
				if (getLoginUserId() != null) {
					if (getLoginUserId().equals(map.get("custId"))) {
						map.put("isMine", "1");
					} else {
						map.put("isMine", "0");
					}
				}
				map.put("replayComments", replayList);
				map.put("replayCommentsCount", commentService.getReplayCommentsCount(((Comment) comment).getId()).get("replayCommentsCount"));
				return map;
			});

			//不包含删除的所有评论的个数
			Integer commentCount = commentService.findCommentCount("news", newsId);
			//不包含已删除的评论的一级评论的个数
			int stairCommentCount = commentService.findStairCommentCount("news", newsId);

			Msg msg = msgService.get(newsId);

			resultMap.put("comments", commentsList);
			resultMap.put("stairCommentCount", stairCommentCount);
			resultMap.put("commentCount", commentCount);
			resultMap.put("title", msg.getTitle());
			resultMap.put("createTime", msg.getUpdateTime());
			resultMap.put("imgUrl", msg.getImgUrl());
		} catch (Exception e) {
			logger.error(e.toString());
		}
		logger.info("--------------newsCommentsForNew(end)-------------|" + "fromModule:NewsController");
		return resultMap;
	}

	/**
	 * 2.4.0新版本评论列表--查看全部回复评论
	 * @param newsId
	 */
	@ResponseBody
	@RequestMapping(value = "/showAllReplayComments", params = { "rootCommentId" }, method = RequestMethod.POST)
	public Object showAllReplayComments(Long rootCommentId) {
		logger.info("--------------showAllReplayComments(start)-------------" + "|" + "fromModule:NewsController" + "|" + "rootCommentId:根评论编号" + rootCommentId);
		return map(commentService.getAllReplayComments(rootCommentId, getPageBounds()), comment -> {
			logger.info("--------------comment:" + comment.toString());
			Long custId = getLoginUserId();
			Map<String, Object> map = new HashMap<String, Object>();
			map = commentToMapBuilder.build((Comment) comment, getDeviceId(), getLoginUserId());
			if (custId != null) {
				if (custId.equals(map.get("custId"))) {
					map.put("isMine", "1");
				} else {
					map.put("isMine", "0");
				}
			}
			if (((Comment) comment).getVersion() == null || ((Comment) comment).getVersion() < 240) {
				String content = (String) map.get("comment");
				content = content.substring(content.indexOf(": ") + 1, content.length());
				map.put("comment", content);
			}
			return map;
		});
	}

	/**
	 * 2.4.0新版本评论列表--删除评论
	 * @param newsId
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteComments", params = { "commentId" }, method = RequestMethod.POST)
	public Object deleteComments(Long commentId) {
		logger.info("--------------deleteComments(start)-------------|" + "fromModule:NewsController" + "|commentId:" + commentId);
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> listResult = new ArrayList<Map<String, Object>>();
		try {
			commentService.deleteMyComment(commentId);
			logger.info("--------------伪删除完成--------------");
			model.setCode("0000");
			model.setError("");
			model.setMessage("调用成功");
			model.setState("1");
			model.setResult(listResult);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.toString());
			model.setCode("0100");
			model.setError("未知异常");
			model.setMessage("调用失败");
			model.setState("0");
			model.setResult(listResult);
		}
		logger.info("--------------deleteComments(end)-------------|" + "fromModule:NewsController");
		return model;
	}

	/**
	 * 资讯详情页喜欢接口
	 * @param newsId 用户id
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/love", method = RequestMethod.POST)
	public void govLove(@PathVariable Long newsId) {
		Long deviceId = getDeviceId();
		Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_NEWS, newsId);
		if (praiseOld != null) {
			throw new ApplicationException(900, "您已执行过此操作，不能再次执行！");
		}
		try {
			Praise praise = new Praise();
			praise.setCustId(getLoginUserId());
			praise.setDeviceId(deviceId);
			praise.setObjectType(Constants.TYPE_NEWS);
			praise.setObjectId(newsId);
			praise.setIsUseful(1l);
			praiseService.add(praise);
		} catch (Exception e) {
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 资讯详情页
	 * @param consultId
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/delLove", method = RequestMethod.POST)
	public void delUseful(@PathVariable Long newsId) {
		Long deviceId = getDeviceId();
		try {
			praiseService.removeByObjectId(deviceId, Constants.TYPE_NEWS, newsId);
		} catch (Exception e) {
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 资讯详情页不喜欢接口
	 * @param newsId 资讯id
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/hate", method = RequestMethod.POST)
	public void govhate(@PathVariable Long newsId) {
		Long deviceId = getDeviceId();
		Praise praiseOld = praiseService.getByDepartIdAndObjectId(deviceId, Constants.TYPE_NEWS, newsId);
		if (praiseOld != null) {
			throw new ApplicationException(900, "您已执行过此操作，不能再次执行！");
		}
		try {
			Praise praise = new Praise();
			praise.setCustId(getLoginUserId());
			praise.setDeviceId(deviceId);
			praise.setObjectType(Constants.TYPE_NEWS);
			praise.setObjectId(newsId);
			praise.setIsUseful(0l);
			praiseService.add(praise);
		} catch (Exception e) {
			throw new ApplicationException(900, "操作失败");
		}
	}

	/**
	 * 资讯详情页收藏接口（登录）
	 * @param newsId 资讯id
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/favorite", method = RequestMethod.POST)
	public void favorite(@PathVariable Long newsId) {
		if (getLoginUserId() != null) {
			News news = new News();
			news.setDeviceId(getDeviceId());
			news.setCustId(getLoginUserId());
			news.setNewsId(newsId);
			newsService.addByCustId(news);
		} else {
			throw new ApplicationException(900, "您还没有登录，不能使用收藏功能");
		}
	}

	/**
	 * 资讯详情页取消收藏接口（登录）
	 *
	 * @param newsId 资讯id
	 */
	@ResponseBody
	@RequestMapping(value = "/{newsId}/cancelFavorite", method = RequestMethod.POST)
	public void cancelFavorite(@PathVariable Long newsId) {
		if (getLoginUserId() != null) {
			newsService.removeBycondition(getLoginUserId(), newsId);
		} else {
			throw new ApplicationException(900, "您还没有登陆，不能使用取消收藏功能");
		}
	}

	//向评论表添加数据
	public Map<String, Object> addComment(String objectType, Long objectId, String comment, Long commentId) {
		Long version = Long.valueOf(getVersion());
		Long custId = getLoginUserId();
		Comment comments = new Comment();
		comments.setCustId(custId);
		comments.setObjectType(objectType);
		comments.setObjectId(objectId);
		comments.setCommentId(commentId);
		comments.setComment(comment);
		comments.setIp(getRequest().getRemoteAddr());
		comments.setDeleted(0l);
		if (version >= 240) {
			comments.setVersion(version);
		}
		Long rootCommentId = null;
		while (!BeanUtil.isNullString(BeanUtil.nullValueOf(commentId))) {
			rootCommentId = commentId;
			Comment tmpComment = commentService.get(commentId);
			if (tmpComment == null) {
				commentId = null;
			} else {
				commentId = tmpComment.getCommentId();
			}
		}
		comments.setRootCommentId(rootCommentId);
		commentService.add(comments);
		Map<String, Object> map = new HashMap<String, Object>();
		String check = checkSensitiveWord(objectType, comments.getId(), comment);
		if (check == null) {
			map.put("flag", "1");//不包含敏感词
			map.put("comments", comments);
		} else {
			map.put("flag", "0");//包含敏感词
			map.put("check", check);
		}
		return map;
	}

	//判断敏感词，伪删除路况,并且添加到敏感词记录表中
	private String checkSensitiveWord(String objectType, Long objectId, String content) {
		List<SensitiveWords> sensitiveWords = sensitiveWordService.findAll();
		content = StringUtils.defaultString(content);
		for (SensitiveWords sensitiveWord : sensitiveWords) {
			Boolean hasSensitiveWord = content.contains(sensitiveWord.getWord());
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

	/**
	 * 资讯搜索
	 * @param key
	 * @param cityCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/search", params = { "key", "cityCode" })
	public Object search(String key, String cityCode) {
		List<Map> list = Lists.newArrayList();
		key = key.replaceAll("\\%", "\\\\%");
		key = key.replaceAll("\\_", "\\\\_");
		if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(cityCode)) {
			list = map(msgService.search(key, cityCode, getPageBounds()), (msg) -> {
				return msgToMapBuilder.build((Msg) msg);
			});
		}
		return list;
	}

	/**
	 * 获取咨询分类标题
	 * @param newsTab
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getNewsType", method = RequestMethod.POST)
	public Object getNewsType(String newsTab) {
		logger.info("---------------getNewsType(start)----------------" + "|" + "fromModule:NewsController" + "|" + "interfaceInfo:获取资讯分类标题");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		try {
			int version = getVersion().intValue();
			//2.3.0之前的版本前端没有传递cityCode参数，2.3.1/2版本ios不支持切换城市刷新数据，所以一律按370100
			String cityCode = "370100";
			if (version > 232) {
				cityCode = getCityCode();
			}
			List<NewsTab> listDict = newsTabService.getByCityCode(cityCode);
			if (listDict != null && listDict.size() > 0) {
				for (NewsTab obj : listDict) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("content", obj.getContent());
					map.put("type", obj.getTabCode());
					list.add(map);
				}
				if (version < 240 && listDict.size() <= 2) {
					for (int i = listDict.size(); i < 3; i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("content", "");
						map.put("type", "");
						list.add(map);
					}
				}
			}
			model.setCode("0000");
			model.setError("");
			model.setResult(list);
			model.setMessage("调用成功");
			model.setState("1");
			logger.info("---------------getNewsType(end)----------------" + "|" + "fromModule:NewsController" + "|" + "interfaceInfo:获取资讯分类标题" + "|" + "cityCode:" + cityCode);
		} catch (Exception e) {
			logger.error("---------------getNewsType(error)----------------" + "|" + "fromModule:NewsController" + "|" + "interfaceInfo:获取资讯分类标题" + "|" + "error:" + e.toString());
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * 获取咨询banner列表
	 * @param cityCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getBannerNews", method = RequestMethod.POST)
	public Object getBannerNews(String cityCode) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonResultModel model = getJsonResultModel();
		List<Map<String, Object>> ls = msgService.findBannerList(cityCode);
		if (ls != null && ls.size() > 0) {
			list = ls;
		}
		model.setCode("0000");
		model.setError("");
		model.setResult(list);
		model.setMessage("调用成功");
		model.setState("1");
		return model;
	}

}
