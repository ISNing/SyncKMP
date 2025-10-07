-dontwarn ch.qos.logback.**
-dontwarn com.oracle.svm.core.annotate.**
#-dontwarn org.slf4j.**
-dontwarn io.github.oshai.kotlinlogging.logback.internal.LogbackLoggerWrapper

-keep class io.github.oshai.kotlinlogging.** { *; }

-dontoptimize
-dontpreverify