package com.yx.youchubank;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

/**
 * HtmlUnit工具类
 * @Title: 
 * @Package com.yx.youchubank  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class HtmlunitUtil {
	
	public static WebClient getWebClient(){
		WebClient webClient = new WebClient(BrowserVersion.CHROME);// 新建一个模拟谷歌Chrome浏览器的浏览器客户端对象
		webClient.getOptions().setThrowExceptionOnScriptError(false);// 当JS执行出错的时候是否抛出异常,
		// 这里选择不需要
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);// 当HTTP的状态非200时是否抛出异常,
		// 这里选择不需要
		webClient.getOptions().setActiveXNative(false);
		webClient.getOptions().setCssEnabled(false);// 是否启用CSS, 因为不需要展现页面,
//		webClient.getOptions().setThrowExceptionOnScriptError(false);
		// 所以不需要启用
		webClient.getOptions().setJavaScriptEnabled(true); // 很重要，启用JS
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());// 很重要，设置支持AJAX
		return webClient;
	}
	
	public static String downPage(WebClient webClient,String url){
		try {
			HtmlPage page = webClient.getPage(url);// 尝试加载上面图片例子给出的网页
			webClient.waitForBackgroundJavaScript(30000);// 异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束
			String pageXml = page.asXml() ;// 直接将加载完成的页面转换成xml格式的字符串
			return pageXml;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public static void main(String[] args) throws IOException {
		WebClient webClient = HtmlunitUtil.getWebClient();
//		for (int i = 2; i < 7; i++) {
//			String downPage = HtmlunitUtil.downPage(webClient, "http://www.zhgnj.com/e/search/result/?searchid=671");
//			Page page =new Page();
//			page.setHtml(new Html(downPage));
//			page.setRawText(downPage);
//			String detailRegurl=".*zhgnj.com/.*[\\d]+.html";
//			List<String> targetRequest = page.getHtml().links().regex(detailRegurl).all();
//			Set<String> urlset=new HashSet<>();
//			for (String url : targetRequest) {
//				urlset.add(url);
//			}
//			//FileUtils.writeStringToFile(new File("E:/测试/"+i+".txt"), downPage, false);
//			FileUtils.writeLines(new File("E:/测试/"+i+".txt"),
//					urlset, false);
//		}
		
		String downPage = HtmlunitUtil.downPage(webClient, "https://zt.voc.com.cn/Topic/youchuyinhangshoujiyinhang/mobile/");
		Page page =new Page();
		page.setHtml(new Html(downPage));
		List<String> all = page.getHtml().links().regex(".*youchuyinhangshoujiyinhang/mobile/article.*html").all();
		webClient.close();
	}
	
}
