package com.yx.service.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.service.IService;
import com.yx.entity.Article;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2019-02-13
 */
public interface ArticleService extends IService<Article> {

	List<Article> pageById(Integer pageNo,Integer PageSize);
	
	Long pageCount();
}
