package com.yx.binzhou;


import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Spider;

/**
 * 彬州站定时爬取任务
 * @Title: 
 * @Package com.yx.binzhou  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Component
public class BinzhouTask {
	protected final static Logger logger = LoggerFactory.getLogger(BinzhouTask.class);
	
	@Autowired
	protected JedisPool pool;
	
	@Scheduled(initialDelay=5000, fixedDelay=1000*60*60) //*60*60
	public void run(){
		Date now=new Date();
		String month = DateFormatUtils.format(now, "yyyy-MM");
		String day = DateFormatUtils.format(now, "dd");
		Spider.create(new BinzhouProcessor(pool))
		.addUrl("http://szb.czxww.cn/html/"+month+"/"+day+"/node_2.htm").run();
	}
	public static void main(String[] args) {
		Date now=new Date();
		String month = DateFormatUtils.format(now, "yyyy-MM");
		String day = DateFormatUtils.format(now, "dd");
		System.out.println();
	}
}
