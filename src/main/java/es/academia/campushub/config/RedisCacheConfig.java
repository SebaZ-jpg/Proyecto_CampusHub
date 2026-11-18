package es.academia.campushub.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Configuración de la caché Redis: TTL, serialización y política de errores. Los fallos de caché
 * se registran para que la caché no sea la fuente de verdad.
 */

@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {
  private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);
  private final RedisConnectionFactory connectionFactory;

  public RedisCacheConfig(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
      // El detalle se considera temporal y caduca solo tras diez minutos incluso si no hay cambios.
    RedisCacheConfiguration configuration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .disableKeyPrefix()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    RedisSerializer.string()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    return RedisCacheManager.builder(connectionFactory).cacheDefaults(configuration).build();
  }

  @Override
  public CacheErrorHandler errorHandler() {
      // Los errores de caché se registran y no se propagan como fallos de negocio: PostgreSQL y
      // MongoDB siguen siendo las fuentes persistentes de los datos.
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Redis cache read failed for {}", key, exception);
      }

      @Override
      public void handleCachePutError(
          RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Redis cache write failed for {}", key, exception);
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Redis cache eviction failed for {}", key, exception);
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Redis cache clear failed for {}", cache.getName(), exception);
      }
    };
  }
}
