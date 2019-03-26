package com.yx.service.mybatis.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.yx.entity.Article;
import com.yx.entity.mapper.ArticleMapper;
import com.yx.service.mybatis.ArticleService;

@Service
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
	@Autowired
	protected ArticleMapper articleMapper;

	@Override
	public List<Article> pageById(Integer pageNo, Integer PageSize) {
		
		return articleMapper.pageById(pageNo, PageSize);
	}

	@Override
	public Long pageCount() {
		
		return articleMapper.pageCount();
	}

}
