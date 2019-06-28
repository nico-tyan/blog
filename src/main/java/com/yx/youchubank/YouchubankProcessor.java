package com.yx.youchubank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 自定义通用解析器
 * @Title: 
 * @Package com.yx.youchubank  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class YouchubankProcessor implements PageProcessor {
	
	private String domain=null;
	
	private String listRegurl=null;
	
	private String detailRegurl=null;
	
	private JedisPool pool=null;
	
	public YouchubankProcessor(JedisPool pool,String domain,String listRegurl,String detailRegurl){
		this.pool=pool;
		this.domain=domain;
		this.listRegurl=listRegurl;
		this.detailRegurl=detailRegurl;
	}
    @Override
    public void process(Page page) {
    	//处理需要继续爬取的地址-begin
        Integer _level = 0;
        String _listRegurl=null;
        String _detailRegurl=null;
		if (page.getRequest().getExtra("_level") != null) {
			_level = (Integer) page.getRequest().getExtra("_level");
			if (page.getRequest().getExtra("_listRegurl") != null) {
				_listRegurl =  (String) page.getRequest().getExtra("_listRegurl");
			}
			if (page.getRequest().getExtra("_detailRegurl") != null) {
				_detailRegurl = (String) page.getRequest().getExtra("_detailRegurl");
			}
		}
		//增加爬取的目标站点
		 List<String> targetRequest = page.getHtml().links().regex(_listRegurl!=null?_listRegurl:listRegurl).all();
		// 如果步数高于1，不再爬取
		if (_level < 1) {
			for (String url : targetRequest) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", _level + 1);
				extras.put("_listRegurl", listRegurl);
				extras.put("_detailRegurl", detailRegurl);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
		}
		//处理需要继续爬取的地址-end
		
        //处理需要发送到服务器的地址-begin
		List<String> sendRequests = page.getHtml().links().regex(_detailRegurl!=null?_detailRegurl:detailRegurl).all();
		Jedis jedis = pool.getResource();
		try {
			for (String targeturl : sendRequests) {
				if(jedis.sadd(domain+"_send_duplicate", targeturl)!=0){
					   jedis.rpush(domain+"_send_list", targeturl);
				}
				//
			}
        } finally {
            pool.returnResource(jedis);
        }
		//处理需要发送到服务器的地址-end
    }

    @Override
    public Site getSite() {
        return Site.me().setRetryTimes(3).setTimeOut(5000).setDomain(domain);
    }

    public static void main(String[] args) {
    	
        Spider.create(new YouchubankProcessor(null,null,null,null))
        .addUrl("http://www.rednet.cn/index.html/")
        //.setScheduler(new YouchubankFileCacheQueueScheduler("D:/webmagic/url6/",".*rednet.cn/c/.*htm|.*rednet.cn/content.*html"))
        .setScheduler(new YouchubankRedisScheduler())
        .thread(50)
        .run();
    }

}