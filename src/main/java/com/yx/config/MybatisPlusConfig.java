package com.yx.config;


import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.baomidou.mybatisplus.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.mapper.ISqlInjector;
import com.baomidou.mybatisplus.mapper.LogicSqlInjector;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;

/**
 * mybatis配置
 * @Title: 
 * @Package com.yx.config  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Configuration
@MapperScan(basePackages={"com.yx.entity.mapper*"})//扫描多个mapper接口
@EntityScan("com.yx.entity*")//实体扫描
public class MybatisPlusConfig {
	
	@Bean(name="mybatisSqlSessionFactory")
    @Autowired
    @Qualifier("mysqlDS")
    public SqlSessionFactory sqlSessionFactory(DataSource datasource) throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        // 配置mapper的扫描，找到所有的mapper.xml映射文件
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:/com/yx/entity/mapper/*.xml");
        sessionFactory.setMapperLocations(resources);
        sessionFactory.setDataSource(datasource);
        return sessionFactory.getObject();
    }
	
	
    /**
     * mybatis-plus SQL执行效率插件【生产环境可以关闭】
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }

    /**
     * mybatis-plus分页插件<br>
     * 文档：http://mp.baomidou.com<br>
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setLocalPage(true);// 开启 PageHelper 的支持
        return paginationInterceptor;
    }


    /**
     * 注入主键生成器
     */
    @Bean
    public IKeyGenerator keyGenerator(){
        return new H2KeyGenerator();
    }

    /**
     * 注入sql注入器
     */
    @Bean
    public ISqlInjector sqlInjector(){
        return new LogicSqlInjector();
    }

}