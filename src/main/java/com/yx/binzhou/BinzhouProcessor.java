package com.yx.binzhou;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 彬州网解析器
 * @Title: 
 * @Package com.yx.binzhou  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class BinzhouProcessor implements PageProcessor {
	private String domain="youchubank";
	public JedisPool pool;
	
	public BinzhouProcessor(JedisPool pool){
		this.pool=pool;
	}
	
	@Override
	public void process(Page page) {
		List<String> all = page.getHtml().xpath("//ul/li/a").links().all();
//		System.out.println(all);
		Jedis jedis = pool.getResource();
		try {
			for (String targeturl : all) {
				if(jedis.sadd(domain+"_send_duplicate", targeturl)!=0){
					jedis.rpush(domain+"_send_list", targeturl);
				}
			}
		} finally {
			pool.returnResource(jedis);
		}
	}

	public static void main(String[] args) {
		Spider.create(new BinzhouProcessor(null))
		.addUrl("http://szb.czxww.cn/html/2019-05/16/node_2.htm").run();
	}

	@Override
	public Site getSite() {
		return Site.me().setRetryTimes(3).setTimeOut(5000).setDomain("szb.czxww.cn");
	}

}