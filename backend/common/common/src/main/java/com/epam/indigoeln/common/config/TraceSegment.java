package com.epam.indigoeln.common.config;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * CDI interceptor binding that wraps the annotated method (or all methods of the annotated class)
 * in an AWS X-Ray subsegment.
 *
 * <p>Usage:
 * <pre>
 *   // Whole class — subsegment name derived as "ClassName.methodName"
 *   {@literal @}TraceSegment
 *   {@literal @}ApplicationScoped
 *   public class MyService { ... }
 *
 *   // Single method with a custom name
 *   {@literal @}TraceSegment("PubChem API")
 *   public List<Result> search(...) { ... }
 * </pre>
 *
 * <p>Outside a Lambda execution context (e.g. unit tests) the interceptor is a no-op,
 * so no {@code SegmentNotFoundException} will be thrown.
 */
@Inherited
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceSegment {

    /**
     * Optional subsegment name. When empty (the default) the interceptor derives the name
     * from the declaring class and method: {@code "ClassName.methodName"}.
     */
    @Nonbinding
    String value() default "";
}
