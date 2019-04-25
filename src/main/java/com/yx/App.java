package com.yx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 项目启动入口
 * 
 * @Title:
 * @Package com.yx
 * @Description:
 * @author
 * @date
 * @version
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class App {

	protected final static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
		logger.info("Application is success start!");
	}

}
