#FROM bellsoft/liberica-openjdk-alpine:24-cds
FROM bellsoft/liberica-openjdk-alpine:latest-cds


# Create user and home directory
RUN adduser -D -h /home/notSec2075 -s /bin/sh notSec2075 \
    && mkdir -p /opt \
    && chown -R notSec2075:notSec2075 /opt

# Copy the jar to /opt
COPY target/sender.jar /opt/sender.jar

# Change to non-root user
USER notSec2075

# Define entrypoint
ENTRYPOINT ["java", "--add-opens", "java.base/java.time.zone=ALL-UNNAMED", \
            "--add-opens", "java.base/java.time=ALL-UNNAMED",\
            "--add-opens", "java.base/java.math=ALL-UNNAMED",  "--enable-native-access=ALL-UNNAMED", \
            "-XX:+UnlockExperimentalVMOptions", "-XX:+UseGraalJIT",\
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
            "--add-opens", "java.management/sun.management=ALL-UNNAMED", \
            "--add-opens", "java.base/java.time.LocalDateTime=ALL-UNNAMED", \
            "-XX:+UseZGC",  "-Xss2m", "-XX:MinHeapSize=256M", \
            "-XX:InitialHeapSize=256M", "-XX:TieredStopAtLevel=1", \
            "-XX:CICompilerCount=1", "-Xshare:on", "-XX:ParallelGCThreads=100", \
            "-XX:ConcGCThreads=30", "--enable-preview", "-XX:SoftRefLRUPolicyMSPerMB=0", \
            "-XX:ZCollectionInterval=30", "-XX:ZFragmentationLimit=10", "-XX:MaxGCPauseMillis=50", \
            "-XX:+UseCompressedClassPointers", "-XX:-UseCompressedOops", "-XX:ZUncommitDelay=30", \
            "-XX:InitiatingHeapOccupancyPercent=35", "-XX:G1HeapRegionSize=16M", \
            "-XX:MinMetaspaceFreeRatio=50", "-XX:MaxMetaspaceFreeRatio=100", \
            "-XX:InitialCodeCacheSize=8m", "-XX:+HeapDumpOnOutOfMemoryError", \
            "-Dsun.net.inetaddr.ttl=60", "-Dsun.net.client.defaultConnectTimeout=10000", \
            "-Dsun.net.client.defaultReadTimeout=30000", "-XX:+UnlockExperimentalVMOptions", \
            "-XshowSettings:vm", "-jar", "/opt/sender.jar"]

# Expose ports
EXPOSE 3333 4444
