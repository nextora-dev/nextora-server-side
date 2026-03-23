package lk.iit.nextora.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * AOP Aspect for pattern-based cache eviction.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheEvictPatternAspect {

    private final CacheService cacheService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(cacheEvictPattern)")
    public Object handleCacheEviction(ProceedingJoinPoint joinPoint, CacheEvictPattern cacheEvictPattern) throws Throwable {

        // Evict before if configured
        if (cacheEvictPattern.beforeInvocation()) {
            evictPatterns(joinPoint, cacheEvictPattern.patterns());
        }

        Object result = joinPoint.proceed();

        // Evict after (default behavior)
        if (!cacheEvictPattern.beforeInvocation()) {
            evictPatterns(joinPoint, cacheEvictPattern.patterns());
        }

        return result;
    }

    private void evictPatterns(ProceedingJoinPoint joinPoint, String[] patterns) {
        EvaluationContext context = createEvaluationContext(joinPoint);

        for (String pattern : patterns) {
            String resolvedPattern = resolvePattern(pattern, context);
            log.debug("Evicting cache pattern: {}", resolvedPattern);
            cacheService.deleteByPattern(resolvedPattern);
        }
    }

    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
            // Also set by index
            context.setVariable("p" + i, args[i]);
        }

        return context;
    }

    private String resolvePattern(String pattern, EvaluationContext context) {
        // Check if pattern contains SpEL expression
        if (pattern.contains("#")) {
            try {
                // Replace SpEL expressions in pattern
                String resolved = pattern;
                while (resolved.contains("#{")) {
                    int start = resolved.indexOf("#{");
                    int end = resolved.indexOf("}", start);
                    if (end > start) {
                        String expression = resolved.substring(start + 2, end);
                        Object value = parser.parseExpression(expression).getValue(context);
                        resolved = resolved.substring(0, start) + value + resolved.substring(end + 1);
                    }
                }
                return resolved;
            } catch (Exception e) {
                log.warn("Failed to resolve SpEL in pattern {}: {}", pattern, e.getMessage());
            }
        }
        return pattern;
    }
}

