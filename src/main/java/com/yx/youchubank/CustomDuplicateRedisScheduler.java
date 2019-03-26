package com.yx.youchubank;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

@Component
public class CustomDuplicateRedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover{
	protected final static Logger logger = LoggerFactory.getLogger(CustomDuplicateRedisScheduler.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	// 待爬取队列
	private static final String QUEUE_PREFIX = "queue_";
	// 已爬取去重 set
	private static final String DUPLICATE_SET_PREFIX = "duplicate_set_";
	// 待爬取去重 set
	private static final String QUEUE_DUPLICATE_SET_PREFIX = "queue_duplicate_set_";
	// 待爬取队列附加属性
	private static final String ITEM_QUEUE_PREFIX = "item_queue_";

	protected String getDuplicateSetKey(Task task) {
		return DUPLICATE_SET_PREFIX;
	}

	protected String getQueueDuplicateSetKey(Task task) {
		return QUEUE_DUPLICATE_SET_PREFIX ;
	}

	protected String getQueueKey(Task task) {
		return QUEUE_PREFIX ;
	}

	protected String getItemQueueKey(Task task) {
		return ITEM_QUEUE_PREFIX ;
	}

	private CustomDuplicateRedisScheduler() {
		setDuplicateRemover(this);
	}

	@Override
	public void resetDuplicateCheck(Task task) {
		stringRedisTemplate.delete(getDuplicateSetKey(task));
		stringRedisTemplate.delete(getQueueDuplicateSetKey(task));
	}

	@Override
	public boolean isDuplicate(Request request,Task task) {
		SetOperations<String, String> opsForSet = stringRedisTemplate.opsForSet();
		if (opsForSet.add(getDuplicateSetKey(task), request.getUrl()) == 0) {
			return true;
		}
		return false;

	}

	protected void pushWhenNoDuplicate(Request request,Task task) {
		SetOperations<String, String> opsForSet = stringRedisTemplate.opsForSet();
		if (opsForSet.add(getQueueDuplicateSetKey(task), request.getUrl()) == 0) {
			return;
		}
		stringRedisTemplate.opsForList().rightPush(getQueueKey(task), request.getUrl());
		if (request.getExtras() != null) {
			String field = DigestUtils.shaHex(request.getUrl());
			String value = JSON.toJSONString(request);
			stringRedisTemplate.opsForHash().put(getItemQueueKey(task), field, value);
		}
	}

	@Override
	public synchronized Request poll(Task task) {
		String url = stringRedisTemplate.opsForList().leftPop(getQueueKey(task));
		if (url == null) {
			return null;
		}
		String key = getItemQueueKey(task);
		String field = DigestUtils.shaHex(url);
		Object value = stringRedisTemplate.opsForHash().get(key, field);
		if (value != null) {
			Request o = JSON.parseObject(value.toString(), Request.class);
			return o;
		}
		Request request = new Request(url);
		return request;
	}

	// 待爬取数量
	@Override
	public int getLeftRequestsCount(Task task) {
		Long size = stringRedisTemplate.opsForList().size(getQueueKey(task));
		return size.intValue();
	}

	// 已爬取数量
	@Override
	public int getTotalRequestsCount(Task task) {
		Long size = stringRedisTemplate.opsForSet().size(getDuplicateSetKey(task));
		return size.intValue();
	}

	@Override
	public void push(Request request,Task task) {
		pushWhenNoDuplicate(request,task);
	}
	
}
