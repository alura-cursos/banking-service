package com.alura.agencias.service.cache;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Command;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.Request;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisCacheService {

    private final Redis redisClient;

    public RedisCacheService(Redis redisClient) {
        this.redisClient = redisClient;
    }

    public Uni<Void> set(String key, String value, int ttlSeconds) {
        return redisClient.send(Request.cmd(Command.SETEX).arg(key).arg(ttlSeconds).arg(value)).replaceWithVoid();
    }

    public Uni<String> get(String key) {
        return redisClient.send(Request.cmd(Command.GET).arg(key))
                .map(result -> result == null ? null : result.toString());
    }

    public Uni<Void> del(String key) {
        return redisClient.send(Request.cmd(Command.DEL).arg(key)).replaceWithVoid();
    }
}
