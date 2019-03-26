package com.yx.rednet.task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yx.pipeline.ArticlePipeline;
import com.yx.rednet.CustomFileCacheQueueScheduler;
import com.yx.rednet.CustomRedisScheduler;
import com.yx.rednet.RednetProcessor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

//@Component
public class RednetTask {
	
	@Autowired
	private ArticlePipeline articlePipeline;
	
	@Autowired
	private CustomRedisScheduler customRedisScheduler;
	
	@Resource
	protected JedisPool pool;
	
	//@Scheduled(initialDelay=5000, fixedDelay=1000*60*60) 
	public void run(){
		//Jedis jedis = pool.getResource();
		//jedis.del("waitUrl");
		System.out.println("红网爬虫启动中··········");
		Spider.create(new RednetProcessor())
        .addUrl("https://hn.rednet.cn/")
        //.addUrl("https://hn.rednet.cn/channel/7399.html")
        //.setScheduler(new CustomFileCacheQueueScheduler("D:/webmagic/url/"))
        .setScheduler(customRedisScheduler)
        //.addPipeline(new FilePipeline("D:/webmagic/result/"))
        .addPipeline(articlePipeline)
        .thread(50)
        .run();
		
//		try {
//			Jedis jedis = pool.getResource();
//			Set<String> smembers = jedis.smembers("test_waitUrl");
//			PrintWriter fileCursorWriter = new PrintWriter(new FileWriter("D:/webmagic/test/url.txt", false));
//			for (String url : smembers) {
//				fileCursorWriter.println(url);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	
	}
	
}
