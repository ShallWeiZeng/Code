package dp.zsw.middleware.handler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.util.Set;

/**
 * Created by ShangWei on 2017/12/8.
 *
 */
public class JedisPoolsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JedisPoolsUtils.class);
    private static Pool<Jedis> jedisPool = null;

    public JedisPoolsUtils() {
    }

    public static void init(String masterName, Set<String> sentinels) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxWaitMillis(5000L);
        init(masterName, sentinels, jedisPoolConfig);
    }

    public static void init(String masterName, Set<String> sentinels, JedisPoolConfig jedisPoolConfig) {
        if(jedisPool != null) {
            LOG.warn("jedisPool is not null");
        }

        LOG.info("init jedisPool with sentinel {}, {}", masterName, sentinels);
        jedisPool = new JedisSentinelPool(masterName, sentinels, jedisPoolConfig);
    }

    public static void init(String host, int port) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxWaitMillis(5000L);
        init(host, port, jedisPoolConfig);
    }

    public static void init(String host, int port, JedisPoolConfig jedisPoolConfig) {
        if(jedisPool != null) {
            LOG.warn("jedisPool is not null");
        }

        LOG.info("init jedisPool with redis {}:{}", host, Integer.valueOf(port));
        jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }

    public static Jedis getJedis() {
        if(jedisPool == null) {
            LOG.warn("jedisPool is null when getJedis");
            return null;
        } else {
            Jedis jedis = null;
            int tryCount = 0;

            while(tryCount < 3) {
                try {
                    jedis = (Jedis)jedisPool.getResource();
                    break;
                } catch (JedisConnectionException var3) {
                    ++tryCount;
                    ThreadUtils.sleep(10L);
                    if(tryCount == 3) {
                        LOG.warn("Failed to get jedis from pool, tryCount {}", Integer.valueOf(tryCount), var3);
                    } else {
                        LOG.warn("Failed to get jedis from pool, tryCount {}, {}", Integer.valueOf(tryCount), var3.getMessage());
                    }
                }
            }

            return jedis;
        }
    }

    public static void destroy() {
        if(jedisPool != null) {
            jedisPool.destroy();
            jedisPool = null;
        }

    }
}
