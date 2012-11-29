package ecurrencies.service.executor

import static ecurrencies.service.executor.Constants.ALLOW_CORE_THREAD_TIMEOUT
import static ecurrencies.service.executor.Constants.CORE_POOL_SIZE
import static ecurrencies.service.executor.Constants.KEEP_ALIVE_TIME
import static ecurrencies.service.executor.Constants.MAXIMUM_POOL_SIZE
import static ecurrencies.service.executor.Constants.PRE_START_ALL_CORE_THREADS
import static ecurrencies.service.executor.Constants.TIME_UNIT
import static java.lang.String.format
import static java.lang.System.getSecurityManager
import static java.lang.Thread.NORM_PRIORITY
import static java.lang.invoke.MethodHandles.lookup
import static java.util.concurrent.TimeUnit.valueOf
import static org.slf4j.LoggerFactory.getLogger
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.core.env.Environment

@Configuration
@PropertySource("classpath:service.properties")
class ExecutorServiceConfiguration {

    @Autowired
    private Environment env

    @Bean(destroyMethod = "shutdown")
    @Scope(SCOPE_SINGLETON)
    ScheduledThreadPoolExecutor executor() {

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                env.getRequiredProperty(CORE_POOL_SIZE, Integer.class),
                new CustomizedThreadFactory())

        executor.maximumPoolSize = env.getRequiredProperty(MAXIMUM_POOL_SIZE, Integer.class)
        executor.setKeepAliveTime(env.getRequiredProperty(KEEP_ALIVE_TIME, Long.class),
                valueOf(env.getRequiredProperty(TIME_UNIT)))

        // this MUST be set after increasing keep alive time
        executor.allowCoreThreadTimeOut env.getRequiredProperty(ALLOW_CORE_THREAD_TIMEOUT,Boolean.class)

        if(env.getProperty(PRE_START_ALL_CORE_THREADS, Boolean.class))
            executor.prestartAllCoreThreads()

        executor
    }

    private static final class CustomizedThreadFactory implements ThreadFactory {

        private static final String NAME_PREFIX = "EcurrenciesProjectThread-"

        private final ThreadGroup group
        private final AtomicInteger threadNumber = new AtomicInteger(1)

        CustomizedThreadFactory() {
            final SecurityManager s = getSecurityManager()
            group = s ? s.threadGroup : new CustomizedThreadGroup()
        }

        @Override
        Thread newThread(final Runnable r) {
            final Thread thread = new Thread(group, r, NAME_PREFIX + threadNumber.getAndIncrement(), 0)
            if (thread.daemon) {
                thread.daemon = false
            }
            if (thread.priority != NORM_PRIORITY) {
                thread.priority = NORM_PRIORITY
            }
            thread
        }
    }

    // @Slf4j
    private static final class CustomizedThreadGroup extends ThreadGroup {

        private static final String THREAD_GROUP_NAME = "EcurrenciesProjectThreadGroup"

        CustomizedThreadGroup() {
            super(THREAD_GROUP_NAME)
        }

        @Override
        void uncaughtException(final Thread thread, final Throwable t) {
            //log.error "Uncaught exception on ${thread.name}", t
            super.uncaughtException thread, t
        }
    }
}