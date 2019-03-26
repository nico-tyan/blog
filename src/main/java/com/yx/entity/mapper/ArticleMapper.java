package com.yx.entity.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.yx.entity.Article;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2019-02-13
 */
public interface ArticleMapper extends BaseMapper<Article> {

	@Select("select * from Article ORDER BY id LIMIT #{pageNo},#{PageSize} ")
	List<Article> pageById(@Param("pageNo")Integer pageNo,@Param("PageSize")Integer PageSize);
	
	@Select("select count(*) from Article ")
	Long pageCount();
	
}
