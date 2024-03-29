package com.yx.rednet;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * 自定义redis URL管理器
 * @Title: 
 * @Package com.yx.rednet  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
@Component
public class CustomRedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover{
	@Resource
	protected JedisPool pool;
	@Resource
	protected RedisTemplate redisTemplate;

    private static final String QUEUE_PREFIX = "queue_";

    private static final String SET_PREFIX = "set_";

    private static final String ITEM_PREFIX = "item_";

    public CustomRedisScheduler() {
    	 setDuplicateRemover(this);
    }

    @Override
    public void resetDuplicateCheck(Task task) {
        Jedis jedis = pool.getResource();
        try {
            jedis.del(getSetKey(task));
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
        Jedis jedis = pool.getResource();
        try {
        	if(request.getUrl().matches(".*rednet.cn/c/.*htm")){
        		 return jedis.sadd(getSetKey(task), request.getUrl()) == 0;
            }else if(request.getUrl().matches(".*rednet.cn/Article.asp.*")){
            	 return jedis.sadd(getSetKey(task), request.getUrl()) == 0;
            }else if(request.getUrl().matches(".*rednet.cn/content.*html")){
            	 return jedis.sadd(getSetKey(task), request.getUrl()) == 0;
            }else if(request.getUrl().matches(".*rednet.cn/PeopleShow.asp.*")){
            	 return jedis.sadd(getSetKey(task), request.getUrl()) == 0;
            }else if(request.getUrl().matches(".*live.rednet.cn/[\\S]+")){
            	 return jedis.sadd(getSetKey(task), request.getUrl()) == 0;
            }
            return false;
        } finally {
            pool.returnResource(jedis);
        }

    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
        Jedis jedis = pool.getResource();
        try {
        	if(jedis.sadd("test_waitUrl", request.getUrl())==0){
        		return;
        	}
            jedis.rpush(getQueueKey(task), request.getUrl());
            if (request.getExtras() != null) {
                String field = DigestUtils.shaHex(request.getUrl());
                String value = JSON.toJSONString(request);
                jedis.hset((ITEM_PREFIX + task.getUUID()), field, value);
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public synchronized Request poll(Task task) {
        Jedis jedis = pool.getResource();
        try {
            String url = jedis.lpop(getQueueKey(task));
            if (url == null) {
                return null;
            }
            String key = ITEM_PREFIX + task.getUUID();
            String field = DigestUtils.shaHex(url);
            byte[] bytes = jedis.hget(key.getBytes(), field.getBytes());
            if (bytes != null) {
                Request o = JSON.parseObject(new String(bytes), Request.class);
                return o;
            }
            Request request = new Request(url);
            return request;
        } finally {
            pool.returnResource(jedis);
        }
    }

    protected String getSetKey(Task task) {
        return SET_PREFIX + task.getUUID();
    }

    protected String getQueueKey(Task task) {
        return QUEUE_PREFIX + task.getUUID();
    }

    protected String getItemKey(Task task)
    {
        return ITEM_PREFIX + task.getUUID();
    }

    @Override
    public int getLeftRequestsCount(Task task) {
    	redisTemplate.opsForList();
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.llen(getQueueKey(task));
            return size.intValue();
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.scard(getSetKey(task));
            return size.intValue();
        } finally {
            pool.returnResource(jedis);
        }
    }
}
