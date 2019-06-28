package com.yx.weibo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Spider;

/**
 * 微博定时爬取任务
 * @Title: 
 * @Package com.yx.weibo  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Component
public class WeiboTask {
	@Value("${sendserver.DETAIL_JSON}")
	private String DETAIL_JSON;
	
	@Value("${sendserver.HOST}")
	private String HOST;
	// 爬虫MAC
	@Value("${sendserver.MAC}")
	private String MAC;
	// 爬虫PWD
	@Value("${sendserver.PWD}")
	private String PWD;
	
	@Scheduled(initialDelay = 5000, fixedDelay = 1000 * 60 * 60) // *60*60
	public void run() {
		Spider spider = Spider.create(new WeiboProcessor(DETAIL_JSON,HOST,MAC,PWD));
		for (int i = 1; i < 21; i++) {
			spider.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D邮储银行&page_type=searchall&page="+i);
		}
		
		for (int i = 1; i < 21; i++) {
			spider.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D邮政银行&page_type=searchall&page="+i);
		}
		
		for (int i = 1; i < 21; i++) {
			spider.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D邮政储蓄银行&page_type=searchall&page="+i);
		}
		
		spider.thread(3)
		.run();
		
	}
	
	public static void main(String[] args) {
		Spider.create(new WeiboProcessor())
		.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D还有爆料称两人疑似开撕&page_type=searchall&page=1")
//		.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D61%26q%3D邮政银行真的是我见过最无语的银行&page_type=searchall&page=1")
		.thread(3)
		.run();
	}

}
