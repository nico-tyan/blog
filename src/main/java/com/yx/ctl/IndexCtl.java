package com.yx.ctl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 测试接口
 * @Title: 
 * @Package com.yx.ctl  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Controller
public class IndexCtl {
	
	@RequestMapping("/index")
	public String index(){
		
		return "index";
	}
	
	@RequestMapping("/index2")
	@ResponseBody
	public String index2(){
		
		return "index";
	}
}
