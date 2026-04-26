package com.epam.indigoeln.common.lambda;

import com.epam.indigoeln.common.config.TraceHelper;
import com.epam.indigoeln.common.config.TraceSegment;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * CDI interceptor that wraps {@link TraceSegment}-annotated methods in a trace span via
 * {@link TraceHelper#begin}.
 *
 * <p>Subsegment name resolution order:
 * <ol>
 *   <li>{@code @TraceSegment("custom name")} — uses the provided value.</li>
 *   <li>No value — derives {@code "ClassName.methodName"} from the invocation context.</li>
 * </ol>
 *
 * <p>When {@link TraceHelper#isActive()} returns {@code false} (unit tests, local dev) the
 * interceptor is a complete no-op with zero overhead.
 *
 * <p>Priority is {@code APPLICATION - 10} (1990) so this interceptor wraps <em>outside</em>
 * {@code DataAccessInterceptor} (priority 2000), meaning the span covers both the
 * {@code SET_CONFIG} DB call and the actual business logic.
 */
@Slf4j
@TraceSegment
@Interceptor
@Priority(Interceptor.Priority.APPLICATION - 10)
public class TraceSegmentInterceptor {

    @Inject
    TraceHelper traceHelper;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        if (!traceHelper.isActive()) {
            return context.proceed();
        }

        String name = resolveSpanName(context);
        Object span = traceHelper.begin(name);
        try {
            return context.proceed();
        } catch (Exception e) {
            traceHelper.attachException(span, e);
            throw e;
        } finally {
            traceHelper.end(span);
        }
    }

    private String resolveSpanName(InvocationContext context) {
        TraceSegment methodAnnotation = context.getMethod().getAnnotation(TraceSegment.class);
        if (methodAnnotation != null && !methodAnnotation.value().isEmpty()) {
            return methodAnnotation.value();
        }
        TraceSegment classAnnotation = context.getTarget().getClass().getAnnotation(TraceSegment.class);
        if (classAnnotation != null && !classAnnotation.value().isEmpty()) {
            return classAnnotation.value();
        }
        // Derive from class + method name, stripping Weld/Arc proxy suffixes ("$$CDISubclass$$...")
        String simpleClassName = context.getTarget().getClass().getSimpleName()
                .replaceAll("\\$.*", "");
        return simpleClassName + "." + context.getMethod().getName();
    }
}
