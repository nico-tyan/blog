package com.yx.weibo;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yx.youchubank.Send2ServerTask;
import com.yx.youchubank.YouchubankTask;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.Selectable;

/**
 * 微博解析器
 * @Title: 
 * @Package com.yx.weibo  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class WeiboProcessor implements PageProcessor {
	public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_SMALL_STR = "yyyy-MM-dd";
	private String DETAIL_JSON;
	private String HOST;
	private String MAC;
	private String PWD;
	private String MUST_KEY="湖南 长沙 湘潭 株洲 邵阳 岳阳 常德 益阳 郴州 衡阳 娄底 怀化 永州 张家界 湘西";
	
	private HttpClientBuilder httpClientBuilder = HttpClients.custom();
	protected final static Logger logger = LoggerFactory.getLogger(WeiboProcessor.class);

	public WeiboProcessor(){
		
	}
	
	public WeiboProcessor(String DETAIL_JSON,String HOST,String MAC,String PWD){
		this.DETAIL_JSON=DETAIL_JSON;
		this.HOST=HOST;
		this.MAC=MAC;
		this.PWD=PWD;
	}
	
	@Override
	public void process(Page page) {
		// String convert = convert(page.getRawText());cards.card_group
		Selectable listSelectable = page.getJson().jsonPath(".cards..card_group");
		JSONArray listItems = (JSONArray) JSONArray.parse(listSelectable.get());
		for (Object item : listItems) {
			Selectable detailSelectable = new Json(item.toString());
			Selectable text = detailSelectable.jsonPath(".mblog..text");
			Selectable longText = detailSelectable.jsonPath(".mblog..longText.longTextContent");
			Selectable mid = detailSelectable.jsonPath(".mid");
			String detailUrl="https://m.weibo.cn/status/"+mid.get();
			String created = detailSelectable.jsonPath(".created_at").get();
			Selectable author = detailSelectable.jsonPath(".mblog.user.screen_name");
			Matcher matcher=null;
			if ("刚刚".equals(created)) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());// 设置当前日期搜索
				created = DateFormatUtils.format(calendar, DATE_FULL_STR);
			} else if ((matcher=Pattern.compile("(\\d+)天").matcher(created)).find()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());// 设置当前日期搜索
				calendar.add(Calendar.DATE, -Integer.parseInt(matcher.group(1)));
				created = DateFormatUtils.format(calendar, DATE_FULL_STR);

			} else if ((matcher=Pattern.compile("(\\d+)时").matcher(created)).find()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());// 设置当前日期搜索
				calendar.add(Calendar.HOUR, -Integer.parseInt(matcher.group(1)));
				created = DateFormatUtils.format(calendar, DATE_FULL_STR);
			} else if ((matcher=Pattern.compile("(\\d+)分").matcher(created)).find()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());// 设置当前日期搜索
				calendar.add(Calendar.MINUTE, -Integer.parseInt(matcher.group(1)));
				created = DateFormatUtils.format(calendar, DATE_FULL_STR);

			} else if (created.matches("\\d{1,2}-\\d{1,2}")) {
				Calendar calendar = Calendar.getInstance();
				created = calendar.get(Calendar.YEAR)+"-"+created;
				try {
					created = DateFormatUtils.format(DateUtils.parseDate(created, DATE_SMALL_STR), DATE_FULL_STR);
				} catch (ParseException e) {
					e.printStackTrace();
					throw new RuntimeException("日期解析错误");
				}
			} else if (created.matches("\\d+-\\d+-\\d+")) {
				try {
					created = DateFormatUtils.format(DateUtils.parseDate(created, DATE_SMALL_STR), DATE_FULL_STR);
				} catch (ParseException e) {
					e.printStackTrace();
					throw new RuntimeException("日期解析错误");
				}
			} else {
				created = DateFormatUtils.format(new Date(), DATE_FULL_STR);
			}
			
			//优先长文本
			if(longText!=null&&!StringUtils.isBlank(longText.get())){
				text=longText;
			}
			String content=text.get();
			//
			Selectable pics = detailSelectable.jsonPath(".mblog.pics");
			//将图片增加进去
			if(pics!=null&&!StringUtils.isBlank(pics.get())){
				String picArray = pics.get();
				JSONArray jSONArray=JSONArray.parseArray(picArray);
				if(jSONArray.size()>0){
					StringBuilder sb=new StringBuilder();
					for (Object object : jSONArray) {
						Json json=new Json(object.toString());
						Selectable url = json.jsonPath(".url");
						sb.append("</br><img");
						sb.append(" src='");
						sb.append(url);
						sb.append("'");
						Selectable width = json.jsonPath(".large.geo.width");
						sb.append(" width='");
						sb.append(width);
						sb.append("'");
						Selectable height = json.jsonPath(".large.geo.height");
						sb.append(" height='");
						sb.append(height);
						sb.append("'");
						sb.append(" ></img>");
					}
					content+=sb.toString();
				}
				
			}
			Selectable address = detailSelectable.jsonPath(".mblog.longText..keyword");
			//加入地址
			if(address!=null&&!StringUtils.isBlank(address.get())){
				content+=address.get();
			}
			
			boolean flag=true;
			for (String key : MUST_KEY.split(" ")) {
				if(content.contains(key)){
					flag=false;
					break;
				}
			}
			if(flag){
				return;
			}
			JSONObject detail=new JSONObject();
			String title =  content.replaceAll("[^\u4E00-\u9FA5]", "");
			if(title.length()>25){
				title=title.substring(0,25);
			}
			detail.put("Content", content);
			detail.put("Title", author.get()+"-"+title);
			detail.put("pubDateTime", created);
			detail.put("location", detailUrl);
			detail.put("Author", author.get());
			detail.put("siteName", "新浪网");
			detail.put("warning", 0);
			detail.put("feature", 0);
			detail.put("weiboFlag", 1);
			detail.put("From", "新浪网");
			
			logger.info(detailUrl);
			JSONObject json_node=JSONObject.parseObject(DETAIL_JSON);
			json_node.put("detail", detail);
			
			JSONObject items=new JSONObject();
			items.put("Url", detailUrl);
			
			json_node.put("item", items);
			
			String sign = json_node.getJSONObject("item").getString("Url");
			String cuid = json_node.getJSONObject("task").getString("companyId")+"-"+json_node.getJSONObject("task").getString("id")+"-"+MD5Util.encrypt16(sign);
			json_node.put("cuid", cuid);
			
			String action = "referDetailsArticle";
			CloseableHttpClient client =httpClientBuilder.build();
			CloseableHttpResponse response = null;
			HttpPost post = new HttpPost(HOST + action);
			StringEntity entity = new StringEntity(json_node.toJSONString(), "utf-8");
			entity.setContentEncoding("utf-8");
			entity.setContentType("application/json");
			post.setEntity(entity);
			post.addHeader("mac", MAC);
			post.addHeader("pwd", PWD);
			
			try {
				response = client.execute(post);
				logger.info(EntityUtils.toString(response.getEntity()));
			}  catch (IOException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		
		}
	}

	@Override
	public Site getSite() {
		return Site.me().setRetryTimes(3).setTimeOut(5000).setDomain("weibo").setSleepTime(5000);
	}

	public static String convert(String unicodeString) {
		StringBuilder stringBuilder = new StringBuilder();
		int i = -1;
		int pos = 0;
		while ((i = unicodeString.indexOf("\\u", pos)) != -1) {
			stringBuilder.append(unicodeString.substring(pos, i));
			if (i + 5 < unicodeString.length()) {
				pos = i + 6;
				stringBuilder.append((char) Integer.parseInt(unicodeString.substring(i + 2, i + 6), 16));
			}
		}
		return stringBuilder.toString();
	}

}
