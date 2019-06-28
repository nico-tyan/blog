package com.yx.youchubank;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 定时发送数据至服务端
 * @Title: 
 * @Package com.yx.youchubank  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Component
public class Send2ServerTask {
	@Autowired
	protected JedisPool pool;
	// 提交地址
	@Value("${sendserver.HOST}")
	private String HOST;
//	private static final String HOST = "http://localhost:8080/Consensus/Client/";
	// 爬虫MAC
	@Value("${sendserver.MAC}")
	private String MAC;
//	private static final String MAC = "52:54:00:1D:D2:FE";
	// 爬虫PWD
	@Value("${sendserver.PWD}")
	private String PWD;
//	private static final String PWD = "123456";
	@Value("${sendserver.JSON}")
	private String JSON;
	
	private HttpClientBuilder httpClientBuilder = HttpClients.custom();
	
	protected final static Logger logger = LoggerFactory.getLogger(Send2ServerTask.class);
	
	@Scheduled(cron="0/3 * * * * ?") // *60*60
	public void run() {
		Jedis jedis = pool.getResource();
		Set<String> urlset = new HashSet<>();
		List<Map> list = new ArrayList<>();
		try {
			int size = 0;
			String url = null;
			String formatNow = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
			while (size < 10 && ((url = jedis.lpop("youchubank_send_list")) != null)) {
				size++;
				urlset.add(url);
				Map<String, String> map = new HashMap();
				map.put("Url", url);
				map.put("Title", "测试");
				map.put("pubDateTime", formatNow);
				list.add(map);
			}
			if(list.isEmpty()){
				logger.info("没有新地址············");
				return;
			}
			String action = "referListArticle";
			CloseableHttpClient client =httpClientBuilder.build();
			CloseableHttpResponse response = null;
			HttpPost post = new HttpPost(HOST + action);
			//String filepath=Send2ServerTask.class.getClassLoader().getResource("send_task_json.txt").getPath();
			//String readFileToString = FileUtils.readFileToString(new File(filepath), "utf-8");//"E:/测试.txt"
			//JSONObject jsonParam = JSONObject.parseObject(readFileToString);
			JSONObject jsonParam = JSONObject.parseObject(JSON);
			jsonParam.put("items", list);
			StringEntity entity = new StringEntity(jsonParam.toJSONString(), "utf-8");
			entity.setContentEncoding("utf-8");
			entity.setContentType("application/json");
			post.setEntity(entity);
			post.addHeader("mac", MAC);
			post.addHeader("pwd", PWD);
			response = client.execute(post);
			logger.info(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			for (String url : urlset) {
				jedis.rpush("youchubank_send_list",url);
			}
			e.printStackTrace();
			logger.info(e.getMessage());
		} finally {
			pool.returnResource(jedis);
		}
	}
}
