package com.epam.indigoeln.eln.quarkus.cachecontrol.deployment;

import com.epam.indigoeln.eln.quarkus.cachecontrol.Cached;
import com.epam.indigoeln.eln.quarkus.cachecontrol.CachedEndpoints;
import com.epam.indigoeln.eln.quarkus.cachecontrol.CachedEndpointsRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import jakarta.inject.Singleton;
import org.jboss.jandex.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CacheControlProcessor {

    /**
     * Scans the Jandex index for {@link Cached}-annotated interface methods, resolves their
     * concrete implementors, and records a {@code Map<"ResourceClass#method" → maxAgeSeconds>}
     * into a synthetic CDI {@link CachedEndpoints} bean.
     *
     * <p>No reflection is used at runtime: the map is fully baked into the native image at
     * augmentation time ({@code STATIC_INIT}).
     */
    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem buildCachedEndpoints(CombinedIndexBuildItem indexBuildItem, CachedEndpointsRecorder recorder) {
        Map<String, Integer> maxAgeByMethod = new HashMap<>();
        Collection<AnnotationInstance> annotations = indexBuildItem.getIndex().getAnnotations(DotName.createSimple(Cached.class.getName()));

        for (AnnotationInstance annotation : annotations) {
            if (annotation.target().kind() != AnnotationTarget.Kind.METHOD) {
                continue;
            }
            MethodInfo method = annotation.target().asMethod();
            ClassInfo declaringInterface = method.declaringClass();
            int interval = annotation.value("interval").asInt();
            // Jandex represents enum values as strings, don't cast directly
            ChronoUnit unit = ChronoUnit.valueOf(annotation.value("unit").asEnum());
            int maxAge = (int) Duration.of(interval, unit).getSeconds();

            // Find every concrete class that implements the annotated interface and
            // map "ConcreteClass#methodName" → maxAge so the filter needs no reflection
            for (ClassInfo implementor : indexBuildItem.getIndex().getAllKnownImplementors(declaringInterface.name())) {
                String key = implementor.name().toString() + "#" + method.name();
                maxAgeByMethod.put(key, maxAge);
            }
        }

        return SyntheticBeanBuildItem
                .configure(CachedEndpoints.class)
                .scope(Singleton.class)
                .supplier(recorder.create(maxAgeByMethod))
                .done();
    }
}
