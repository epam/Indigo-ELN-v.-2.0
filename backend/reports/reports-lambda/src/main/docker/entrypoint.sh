 #!/bin/sh

# /usr/bin/java \
# -cp \
# /var/task:/var/task/lib/* \
# -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
# --add-opens java.base/java.lang=ALL-UNNAMED \
# --add-opens java.base/jdk.internal.misc=ALL-UNNAMED \
# -Djava.net.preferIPv6Addresses=true \
# -XX:+UseSerialGC \
# -XX:MaxDirectMemorySize=256m \
# -XX:+HeapDumpOnOutOfMemoryError \
# -XX:+CrashOnOutOfMemoryError \
# -XX:+UnlockDiagnosticVMOptions \
# -XX:+LogVMOutput \
# -XX:+PrintGCDetails \
# -XX:NativeMemoryTracking=detail \
# -Xcheck:jni \
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
# -Dcom.amazonaws.services.lambda.runtime.api.client.runtimeapi.NativeClient.JNI=/var/task/jni/libaws-lambda-jni.linux_musl-x86_64.so \
#com.epam.indigoeln.lambda.LambdaClient
# com.amazonaws.services.lambda.runtime.api.client.AWSLambda \
# io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest

/usr/bin/java \
  -cp \
  /var/task:/var/task/lib/* \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.misc=ALL-UNNAMED \
  com.epam.indigoeln.lambda.LambdaClient

cat /home/default/hs_err_pid*.log
cat /hs_err_pid*.log
