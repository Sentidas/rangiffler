package ru.sentidas.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sentidas.rangiffler.data.redis.RedisTestClient;

public class RedisExtension implements ExecutionCondition {

    private static final Logger LOG = LoggerFactory.getLogger(RedisExtension.class);


    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(RedisExtension.class);

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var store = context.getRoot().getStore(NS);
        Boolean cached = store.get("redisUp", Boolean.class);
        if (cached != null) {
            return cached
                    ? ConditionEvaluationResult.enabled("Redis доступен (результат из кэша)")
                    : ConditionEvaluationResult.disabled("Redis недоступен (результат из кэша)");
        }

        boolean up;
        String reason;
        try (RedisTestClient redis = new RedisTestClient()) {
            up = "PONG".equalsIgnoreCase(redis.ping());
            reason = up ? "Redis отвечает PONG" : "Redis не ответил PONG";
        } catch (Exception e) {
            up = false;
            reason = "Redis недоступен: " + e.getMessage();
        }

        LOG.warn("[RedisCondition] {}", reason);

        store.put("redisUp", up);

        return up
                ? ConditionEvaluationResult.enabled(reason)
                : ConditionEvaluationResult.disabled(reason);
    }
}
