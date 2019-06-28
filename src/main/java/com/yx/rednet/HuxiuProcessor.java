package com.yx.rednet;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * 虎嗅网解析器
 * @Title: 
 * @Package com.yx.rednet  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class HuxiuProcessor implements PageProcessor {
    @Override
    public void process(Page page) {
        List<String> requests = page.getHtml().links().regex(".*article.*").all();
        page.addTargetRequests(requests);
        page.putField("author",page.getHtml().xpath("//span[@class='author-name']//a/text()"));
        page.putField("title",page.getHtml().xpath("//div[@class='article-wrap']//h1/text()"));
        page.putField("content",page.getHtml().xpath("//div[@class='article-content-wrap']/tidyText()"));
    }

    @Override
    public Site getSite() {
        return Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(6000).setDomain("www.huxiu.com");
    }

    public static void main(String[] args) {
        Spider.create(new HuxiuProcessor()).addUrl("http://www.huxiu.com/")
        .addPipeline(new FilePipeline("D:/webmagic/"))
        .addPipeline(new JsonFilePipeline("D:/webmagic/"))
        .run();
    }

}