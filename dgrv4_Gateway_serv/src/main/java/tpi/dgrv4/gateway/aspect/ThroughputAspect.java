package tpi.dgrv4.gateway.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import tpi.dgrv4.gateway.filter.GatewayFilter;

import java.util.Optional;

@Aspect
@Component
public class ThroughputAspect {

    @Pointcut("@annotation(tpi.dgrv4.gateway.aspect.annotation.ThroughputPoint)")
    public void tps() {
    }

    @Around("tps()")
    public Object tpsAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<Object> result = Optional.empty();
        Optional<Throwable> error = Optional.empty();
        var isDeferred = false;
        try {
            GatewayFilter.setApiReqThroughput();
            var exec = joinPoint.proceed();
            result = Optional.ofNullable(exec);
            if (exec instanceof DeferredResult<?> deferredResult) {
                deferredResult.onCompletion(GatewayFilter::setApiRespThroughput);
                isDeferred = true;
            }
        } catch (Throwable t) {
            error = Optional.of(t);
        } finally {
            if (!isDeferred) {
                GatewayFilter.setApiRespThroughput();
            }
        }

        if (error.isPresent()) {
            throw error.get();
        }

        return result.orElse(null);
    }
}