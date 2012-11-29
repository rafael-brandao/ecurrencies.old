package ecurrencies.service.executor

import static ecurrencies.service.executor.Constants.*
import static java.util.concurrent.TimeUnit.valueOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue

import java.util.concurrent.ThreadPoolExecutor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.testng.annotations.Test

@ContextConfiguration(classes = ExecutorServiceConfiguration.class)
class ExecutorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private Environment env

    @Autowired
    private ThreadPoolExecutor executor

    @Test
    void assertNotNull() {
        assertThat(env, is(notNullValue()))
        assertThat(executor, is(notNullValue()))
    }

    @Test
    void ensureExecutorServiceIsProperlyConfigured() {
        assertThat(executor.corePoolSize, is(env.getRequiredProperty(CORE_POOL_SIZE, Integer.class)))

        assertThat(executor.maximumPoolSize,
                is(env.getRequiredProperty(MAXIMUM_POOL_SIZE, Integer.class)))

        assertThat(executor.allowsCoreThreadTimeOut(),
                is(env.getRequiredProperty(ALLOW_CORE_THREAD_TIMEOUT, Boolean.class)))

        assertThat(executor.getKeepAliveTime(valueOf(env.getRequiredProperty(TIME_UNIT))),
                is(env.getRequiredProperty(KEEP_ALIVE_TIME, Long.class)))
    }
}