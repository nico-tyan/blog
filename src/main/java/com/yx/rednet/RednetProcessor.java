package com.yx.rednet;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.yx.entity.Article;

/**
 * @author code4crafter@gmail.com <br>
 */
public class RednetProcessor implements PageProcessor {
    @Override
    public void process(Page page) {
        List<String> requests1 = page.getHtml().links().regex(".*rednet.cn/c/.*").all();
        List<String> requests2 = page.getHtml().links().regex(".*rednet.cn/content/.*").all();
        List<String> requests3 = page.getHtml().links().regex(".*rednet.cn/PeopleShow/.*").all();
        List<String> requests4 = page.getHtml().links().regex("http://live.rednet.cn/.*").all();
        List<String> requests5 = page.getHtml().links().regex(".*rednet.cn/Article.*").all();
        
        List<String> requests6 = page.getHtml().links().regex(".*rednet.cn/.*").all();

        Integer level = 0;
		if (page.getRequest().getExtra("_level") != null) {
			level = (Integer) page.getRequest().getExtra("_level");
		}
		// 如果步数高于1，不再爬取
		if (level <= 3) {
			
			//page.addTargetRequests(requests1);
			for (String url : requests1) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
			//page.addTargetRequests(requests2);
			for (String url : requests2) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
			//page.addTargetRequests(requests3);
			for (String url : requests3) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
			//page.addTargetRequests(requests4);
			for (String url : requests4) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
			//page.addTargetRequests(requests5);
			for (String url : requests5) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
			
			for (String url : requests6) {
				Request request = new Request();
				request.setUrl(url);
				Map<String, Object> extras = new HashMap<String, Object>();
				// 获取上层页面的深度再加一就是这个URL的深度
				extras.put("_level", level + 1);
				request.setExtras(extras);
				//
				page.addTargetRequest(request);
			}
		}
		
        
        if(page.getUrl().regex(".*rednet.cn/c/.*").match()){
        	String url=page.getUrl().get();
        	String pubdate=page.getHtml().xpath("//span[@class='pubdate']/text()|//span[@class='p_l_25']/text()").get();
        	String author=page.getHtml().xpath("//span[@class='author']/text()|//div[@class='m_b_25']/tidyText()").get();
        	String title=page.getHtml().xpath("//div[@class='title']//h1/text()|//h1[@class='detail_title']/text()").get();
        	String content=page.getHtml().xpath("//div[@id='articlecontent']/tidyText()|//section[@class='detail_article_content']/tidyText()").get();
        	pipeline(page, url, pubdate, title, content,author);
        }else if(page.getUrl().regex(".*rednet.cn/content/.*").match()){
        	String url=page.getUrl().get();
        	String pubdate=page.getHtml().xpath("//span[@class='p_l_25']/text()|//div[@class='time']/text()").get();
        	String author=page.getHtml().xpath("//div[@class='content_info']/text()|//div[@class='m_b_25']/text()|//div[@class='content_info']/span[3]/text()|//section[@class='content']/p[2]/text()").get();
        	String title=page.getHtml().xpath("//h1[@class='p_t_30 f36 p_b_40 h36']/text()|//h1[@class='detail_title']/text()|//div[@class='title']/text()").get();
        	String content=page.getHtml().xpath("//article[@class='f18 detail-article m_b_30']/html()|//section[@class='f_right detail_article_content']/html()|//section[@class='f_right detail_article_content m_t_20']/html()|//section[@class='f_left detail_article_content']/html()|//div[@class='description']/html()").get();
        	pipeline(page, url, pubdate, title, content,author);
        }else if(page.getUrl().regex(".*rednet.cn/PeopleShow/.*").match()){
        	String url=page.getUrl().get();
        	String pubdate=page.getHtml().xpath("//html/body/table[4]/tbody/tr/td/table[2]/tbody/tr/td[1]/font/text())").get();
        	String author=page.getHtml().xpath("//html/body/table[4]/tbody/tr/td/table[2]/tbody/tr/td[1]/font/font/text()").get();
        	String title=page.getHtml().xpath("//div[@class='wz12']/text()").get();
        	String content=page.getHtml().xpath("//article[@class='f18 detail-article m_b_30']/html()|//section[@class='f_right detail_article_content']/html()|//div[@class='news']/html()|//td[@class='wordwrap']/html()").get();
        	pipeline(page, url, pubdate, title, content,author);
        }else if(page.getUrl().regex("http://live.rednet.cn/.*").match()){
        	String url=page.getUrl().get();
        	String pubdate=page.getHtml().xpath("//div[@class='intro fl']/text()").get();
        	String author=null;//page.getHtml().xpath("//span[@class='author']/text()|//div[@class='m_b_25']/tidyText()").get();
        	String title=page.getHtml().xpath("//div[@class='title fl']/h2/text()").get();
        	String content=page.getHtml().xpath("//div[@class='m20 textlist']/html()").get();
        	pipeline(page, url, pubdate, title, content,author);
        }else if(page.getUrl().regex(".*rednet.cn/Article.*").match()){
        	String url=page.getUrl().get();
        	String pubdate=page.getHtml().xpath("//html/body/table[2]/tbody/tr/td/table[2]/tbody/tr[2]/td/text()|//div[@class='tie']/text()").get();
        	String author=page.getHtml().xpath("//html/body/table[4]/tbody/tr/td/table[2]/tbody/tr/td[1]/font/font/text()").get();
        	String title=page.getHtml().xpath("//div[@class='Title_wz12']/text()|//div[@class='content']/h2/text()").get();
        	String content=page.getHtml().xpath("//td[@class='wordwrap']/html()|//div[@class='nr']/html()").get();
        	pipeline(page, url, pubdate, title, content,author);
        }
    }

	public void pipeline(Page page, String url, String pubdate, String title, String content,String author) {
		if (StringUtils.isNotBlank(pubdate) 
				&& StringUtils.isNotBlank(title)
				&& StringUtils.isNotBlank(content)) {
			Article article = new Article();
			author=author.replace("编辑：", "").replace("作者：", "").trim();
			article.setAuthor(author);
			article.setContent(content);
			article.setPubdate(pubdate);
			article.setTitle(title);
			article.setUrl(url);
			article.setCreateDate(new Date());
			page.putField("article", article);
		}
	}

    @Override
    public Site getSite() {
    	Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();
    	DEFAULT_STATUS_CODE_SET.add(200);
    	DEFAULT_STATUS_CODE_SET.add(403);
    	DEFAULT_STATUS_CODE_SET.add(404);
        return Site.me().setRetryTimes(3).setTimeOut(5000).setDomain("www.rednet.cn").setAcceptStatCode(DEFAULT_STATUS_CODE_SET);
    }

    public static void main(String[] args) {
        Spider.create(new RednetProcessor())
        .addUrl("http://www.rednet.cn/index.html/")
        .setScheduler(new CustomFileCacheQueueScheduler("D:/webmagic/url/"))
        //.addPipeline(new FilePipeline("D:/webmagic/"))
        .addPipeline(new JsonFilePipeline("D:/webmagic/"))
        .thread(50)
        .run();
    }

}