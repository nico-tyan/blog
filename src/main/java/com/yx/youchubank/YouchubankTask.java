package com.yx.youchubank;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.yx.App;
import com.yx.entity.UrlListTemplate;
import com.yx.entity.mapper.UrlListTemplateMapper;
import com.yx.pipeline.ArticlePipeline;
import com.yx.rednet.CustomRedisScheduler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.selector.Html;

/**
 * 邮储银行爬取定时任务 
 * @Title:
 * @Package com.yx.youchubank  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Component
public class YouchubankTask {
	protected final static Logger logger = LoggerFactory.getLogger(YouchubankTask.class);
	
	@Autowired
	private ArticlePipeline articlePipeline;
	
	@Autowired
	private CustomRedisScheduler customRedisScheduler;
	
	@Autowired
	private YouchubankRedisScheduler youchubankRedisScheduler;
	
	@Autowired
	protected JedisPool pool;
	
	@Autowired
	private UrlListTemplateMapper urlListTemplateMapper;
	
	@Scheduled(initialDelay=5000, fixedDelay=1000*60*60) //*60*60
	public void run(){
		Jedis jedis = pool.getResource();
		String domain="youchubank";
		String listRegurl=".*rednet.cn/channel/.*";
		String detailRegurl=".*rednet.cn/c/.*htm|.*rednet.cn/content.*html";
		//String downType="htmlunit";
		String downType="httpdown";
		String listurl="https://hn.rednet.cn/channel/7399.html";
		try{
			jedis.del(domain+"_duplicate_url");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("邮储银行爬虫启动中··········");
			List<UrlListTemplate> selectList = urlListTemplateMapper.selectList(null);
			for (UrlListTemplate urlListTemplate : selectList) {
				domain=urlListTemplate.getDomain();
				listRegurl=urlListTemplate.getListRegurl();
				detailRegurl=urlListTemplate.getDetailRegurl();
				//String downType="htmlunit";
				downType=urlListTemplate.getDownType();
				listurl=urlListTemplate.getUrl();
				logger.info("开始爬取："+listurl);
				if("htmlunit".equalsIgnoreCase(downType)){
					//htmlunit浏览器模拟下载爬取
					htmlunitDown(listRegurl, detailRegurl, listurl,jedis,domain);
				}else{
					//webmagic爬取
					Spider.create(new YouchubankProcessor(pool,domain,
							listRegurl,detailRegurl))
					.addUrl(listurl)
					//文章列表  进行去重管理
					//.setScheduler(new YouchubankFileCacheQueueScheduler("D:/webmagic/url/",".*rednet.cn/c/.*htm|.*rednet.cn/content.*html"))
					.setScheduler(youchubankRedisScheduler)
					//文章列表  不需要持久化
					//.addPipeline(new FilePipeline("D:/webmagic/result/"))
					.thread(50)
					.run();
				}
				logger.info("爬取完成："+listurl);
			}
		}finally {
			System.out.println("下载完毕···········");
			pool.returnResource(jedis);
		}
	}
	
	/**
	 * //htmlunit浏览器模拟下载爬取
	 * 只爬取一层
	 * @Title: 
	 * @Description: 
	 * @date 2019年2月26日  
	 * @param listRegurl
	 * @param detailRegurl
	 * @param listurl        
	 * @throws
	 */
	public void htmlunitDown(String listRegurl, String detailRegurl, String listurl,Jedis jedis,String domain) {
		WebClient webClient = HtmlunitUtil.getWebClient();
		try {
			String downPage = HtmlunitUtil.downPage(webClient, listurl);
			Page page =new Page();
			page.setHtml(new Html(downPage));
			page.setRawText(downPage);
			//站点一级域名
			String[] split = listurl.split("//");
			String url_per =split[0];
			String url_host = split[0]+"//"+split[1].substring(0, split[1].indexOf("/"));
			
			//去重详情地址
			Set<String> urlset=new HashSet<>();
			//提取列表地址
			List<String> listRequest = page.getHtml().links().regex(listRegurl).all();
			if(!listRequest.isEmpty()){
				Set<String> listset=new HashSet<>();
				for (String url : listRequest) {
					if(!listset.add(url)){
						continue;
					}
					try {
						String downlistPage = HtmlunitUtil.downPage(webClient, listurl);
						Page listpage =new Page();
						page.setHtml(new Html(downlistPage));
						page.setRawText(downlistPage);
						List<String> targetRequest = page.getHtml().links().regex(detailRegurl).all();
						System.out.println(split[0]+"//"+url_per);
						for (String targeturl : targetRequest) {
							if(targeturl.startsWith("//")){
								targeturl=url_per+targeturl;
							}
							if(targeturl.startsWith("/")){
								targeturl=url_host+targeturl;
							}
							if(urlset.add(targeturl)){
								if(jedis.sadd(domain+"_send_duplicate", targeturl)!=0){
								   jedis.rpush(domain+"_send_list", targeturl);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e.getMessage());
					}
				}
			}
			//提取详情地址
			List<String> targetRequest = page.getHtml().links().regex(detailRegurl).all();
			for (String targeturl : targetRequest) {
				if(targeturl.startsWith("//")){
					targeturl=url_per+targeturl;
				}
				if(targeturl.startsWith("/")){
					targeturl=url_host+targeturl;
				}
				if(urlset.add(targeturl)){
					if(jedis.sadd(domain+"_send_duplicate", targeturl)!=0){
						   jedis.rpush(domain+"_send_list", targeturl);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}finally {
			webClient.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		String readFileToString = FileUtils.readFileToString(new File("E:/测试.txt"), "utf-8");
		JSONObject jsonParam = JSONObject.parseObject(readFileToString);
		System.out.println(jsonParam);
		String listurl="https://zt.voc.com.cn/Topic/youchuyinhangshoujiyinhang/mobile/";
		String[] split = listurl.split("//");
		System.out.println(split[1]);
		String url_per = split[1].substring(0, split[1].indexOf("/"));
		System.out.println(split[0]+"//"+url_per);
	}
	
}
