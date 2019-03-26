package com.yx.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author code4crafter@gmail.com <br>
 */
public class CustomOneStepProcessor implements PageProcessor {
	private String domain = null;
	private String schedulerFilePath = null;
	private String pipelineFilePath = null;
	private Integer depthStep = 1;

	public CustomOneStepProcessor(String domain) {
		this.domain = domain;
	}

	public CustomOneStepProcessor(String domain, Integer depthStep) {
		this.domain = domain;
		this.depthStep = depthStep;
	}

	@Override
	public void process(Page page) {
		Integer level = 0;
		if (page.getRequest().getExtra("_level") != null) {
			level = (Integer) page.getRequest().getExtra("_level");
		}
		page.putField("article", page.getHtml().xpath("h1[@id='comment-subject']/allText()"));

		// 如果步数高于1，不再爬取
		if (level >= depthStep) {
			return;
		}
		// http://www.xxcb.cn/event/caijing/
		List<String> requests = page.getHtml().links().regex(".*html").all();// .regex("http://www.xxcb.cn/event/caijing/.*/\\d+.html").all();
		for (String url : requests) {
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

	@Override
	public Site getSite() {
		return Site.me().setDomain(domain);
	}
	
	public Site getSite(String a) {
		return Site.me().setDomain(domain);
	}
	
	public static void main(String[] args) {
//		System.setProperty("webdriver.chrome.driver",
//				"C:/Program Files (x86)/Google/Chrome/Application/chromedriver.exe");
//		// 新建一个WebDriver 的对象，但是new 的是chrome的驱动
//		WebDriver driver = new ChromeDriver();
//		// 打开指定的网站
//		driver.get("http://www.baidu.com");
//		System.out.println(driver.getTitle());
//
//		/**
//		 * dr.quit()和dr.close()都可以退出浏览器,简单的说一下两者的区别：第一个close，
//		 * 如果打开了多个页面是关不干净的，它只关闭当前的一个页面。第二个quit，
//		 * 是退出了所有Webdriver所有的窗口，退的非常干净，所以推荐使用quit最为一个case退出的方法。
//		 */
//		driver.quit();// 退出浏览器
		System.out.println(1111111);
		System.out.println(1111111);
		String url = "https://zt.voc.com.cn/Topic/youchuyinhangshoujiyinhang/mobile/";
		// selenium系统配置，其中的路径写自己config文件的路径
		System.setProperty("selenuim_config", CustomOneStepProcessor.class.getClassLoader().getResource("config.ini").getPath());
		Spider.create(new CustomOneStepProcessor("zt.voc.com.cn")).addUrl(url)
				// .addUrl("http://www.xxcb.cn/event/caijing/")
				//.setScheduler(new CustomFileCacheQueueScheduler("D:/webmagic/url/"))
				.addPipeline(new FilePipeline("D:/webmagic/"))
				.setDownloader(new SeleniumDownloader("C:/Program Files (x86)/Google/Chrome/Application/chromedriver.exe"))
				.thread(3).run();
	}

}