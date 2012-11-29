package ecurrencies.service.executor

import static groovy.transform.PackageScopeTarget.*
import groovy.transform.PackageScope

@PackageScope([CLASS, FIELDS])
interface Constants {
    static final String ALLOW_CORE_THREAD_TIMEOUT = "ALLOW_CORE_THREAD_TIMEOUT"
    static final String CORE_POOL_SIZE = "CORE_POOL_SIZE"
    static final String KEEP_ALIVE_TIME = "KEEP_ALIVE_TIME"
    static final String MAXIMUM_POOL_SIZE = "MAXIMUM_POOL_SIZE"
    static final String PRE_START_ALL_CORE_THREADS = "PRE_START_ALL_CORE_THREADS"
    static final String TIME_UNIT = "TIME_UNIT"
}