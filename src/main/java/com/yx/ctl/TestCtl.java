package com.yx.ctl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yx.entity.Article;
import com.yx.service.mybatis.ArticleService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class TestCtl {
	@Resource
	private ArticleService articleService;
	@Resource
	protected JedisPool pool;
	
	@GetMapping("/test1")
	public String test(){
		Article article = new Article();
		article.setAuthor("pubdate");
		article.setContent("content");
		article.setPubdate("pubdate");
		article.setTitle("title");
		article.setUrl("url");
		articleService.insert(article);
		return "OK";
	}
	
	@GetMapping("/addUrl")
	public String addUrl(){
		Jedis jedis = pool.getResource();
		Long pageCount = articleService.pageCount();
		for (int i = 0; i < pageCount/5000; i++) {
			List<Article> pageById = articleService.pageById(i*5000, 5000);
			for (Article article : pageById) {
				jedis.sadd("set_rednet.cn", article.getUrl());
			}
		}
		
		return "OK";
	}
	
}
