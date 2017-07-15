package today.tree.role.redis.factory;

import javax.annotation.PostConstruct;

/**
* description: redis pool factory
*
*
* @author today zhaojintian@ediankai.com
* @date 2017年1月7日
* @Company: ediankai
*/
import redis.clients.jedis.Jedis;  
import redis.clients.jedis.JedisPool;  
import redis.clients.jedis.JedisPoolConfig;  

public class RedisPoolFactory implements RedisPool{  
     
     private static volatile JedisPool jedisPool;  
     
     private static int redisPoolMaxTotal;
     
     private static int redisPoolMaxIdle;
     
     private static int redisPoolMaxWaitMillis;
     
     private static boolean redisPoolTestOnReturn;
     
     private static String redisPoolHostName;
    
     private static int redisPoolPort;
    
     private static int redisPoolTimeout;
     
     
     
//init redis param
//	/**
//	 * @param redisPoolMaxTotal the redisPoolMaxTotal to set
//	 */
//     @Value("${redis.pool.maxTotal}")
//	public void setRedisPoolMaxTotal(int redisPoolMaxTotal) {
//		RedisPoolFactory.redisPoolMaxTotal = redisPoolMaxTotal;
//	}
//
//	/**
//	 * @param redisPoolMaxIdle the redisPoolMaxIdle to set
//	 */
//     @Value("${redis.pool.maxIdle}")
//	public void setRedisPoolMaxIdle(int redisPoolMaxIdle) {
//		RedisPoolFactory.redisPoolMaxIdle = redisPoolMaxIdle;
//	}
//
//	/**
//	 * @param redisPoolMaxWaitMillis the redisPoolMaxWaitMillis to set
//	 */
//     @Value("${redis.pool.maxWaitMillis}")
//	public void setRedisPoolMaxWaitMillis(int redisPoolMaxWaitMillis) {
//		RedisPoolFactory.redisPoolMaxWaitMillis = redisPoolMaxWaitMillis;
//	}
//
//	/**
//	 * @param redisPoolTestOnReturn the redisPoolTestOnReturn to set
//	 */
//     @Value("${redis.pool.testOnReturn}")
//	public  void setRedisPoolTestOnReturn(boolean redisPoolTestOnReturn) {
//		RedisPoolFactory.redisPoolTestOnReturn = redisPoolTestOnReturn;
//	}
//
//	/**
//	 * @param redisPoolHostName the redisPoolHostName to set
//	 */
//     @Value("${rediscache.pool.hostName}")
//	public void setRedisPoolHostName(String redisPoolHostName) {
//		RedisPoolFactory.redisPoolHostName = redisPoolHostName;
//	}
//
//	/**
//	 * @param redisPoolPort the redisPoolPort to set
//	 */
//     @Value("${rediscache.pool.port}")
//	public void setRedisPoolPort(int redisPoolPort) {
//		RedisPoolFactory.redisPoolPort = redisPoolPort;
//	}
//
//	/**
//	 * @param redisPoolTimeout the redisPoolTimeout to set
//	 */
//     @Value("${redis.pool.timeout}")
//	public void setRedisPoolTimeout(int redisPoolTimeout) {
//		RedisPoolFactory.redisPoolTimeout = redisPoolTimeout;
//	}
     
     /** 
      * 构造方法执行后，初始化， 
      * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次
      */
     @PostConstruct  
     public void init(){
   	  
   	  if( jedisPool == null ){
   		  try {  
           	  
	        	 /* ResourceBundle bundle = ResourceBundle.getBundle("spring/properties/test/redis");
				    if (bundle == null) {
					throw new IllegalArgumentException(
						"[redis-data.properties] is not found!");
				    }*/
	        	 
	               JedisPoolConfig config = new JedisPoolConfig();  
	               config.setMaxTotal( redisPoolMaxTotal );  
	               config.setMaxIdle( redisPoolMaxIdle  );  
	               config.setMaxWaitMillis( redisPoolMaxWaitMillis );  
	               config.setTestOnBorrow( redisPoolTestOnReturn );  
	               
	               jedisPool = new JedisPool ( config, redisPoolHostName, redisPoolPort , redisPoolTimeout ); 
	          } catch (Exception e) {  
	               e.printStackTrace();  
	          }  
   	  }
   	  
     }
        
      /** 
       * 获取Jedis实例 
       * @return 
       */  
      public Jedis getResource() { 
    	  
    	  Jedis resource = null;
    	  
          try {  
              if (jedisPool != null) {  
            	  resource = jedisPool.getResource();   
              } 
          } catch (Exception e) {  
              e.printStackTrace();  
              
          }  
          
          return resource;  
      }  
             
      /** 
       * 释放jedis资源 
       * @param jedis 
       */  
    @SuppressWarnings("deprecation")
	public void returnResource(final Jedis jedis, boolean conectionBroken) {  
           if (jedis == null) {  
        	   return;
           } 
           try {
               if (conectionBroken) {
                   jedisPool.returnBrokenResource(jedis);
               } else {
                   jedisPool.returnResource(jedis);
               }
           } catch (Exception e) {
//               logger.error("return back jedis failed, will fore close the jedis.", e);
        	   e.printStackTrace();
           
           }
           
       }

	/* (non-Javadoc)
	 * @see com.sdc.system.spring.redis.factory.pool.RedisPool#destory()
	 */
	@Override
	public void destory() {
		// TODO Auto-generated method stub
		RedisPoolFactory.jedisPool.destroy();
	}  
       
  
}  