package com.yx.pipeline;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.yx.entity.Article;
import com.yx.entity.mapper.ArticleMapper;
import com.yx.service.mybatis.ArticleService;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Component
public class ArticlePipeline implements Pipeline {

	@Resource
	private ArticleService articleService;

	@Override
	@SuppressWarnings("unused")
	public void process(ResultItems resultItems, Task task) {
		Object article = resultItems.getAll().get("article");
		if(article instanceof Article){
			articleService.insert((Article)article);
		}
		
	}
}
