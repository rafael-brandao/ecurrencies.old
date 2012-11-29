package ecurrencies.integration.amqpimport static ecurrencies.commons.util.PropertyResolver.getRequiredPropertyimport static java.lang.String.formatimport java.util.concurrent.ConcurrentHashMapimport org.springframework.beans.factory.annotation.Autowiredimport org.springframework.context.ApplicationContextimport org.springframework.integration.endpoint.AbstractEndpointimport org.springframework.scheduling.annotation.Async


class EcurrencyMessageChannelTracker  {    private static final String ECURRENCY_PROVIDER = 'ecurrency-provider'
    private static final String ECURRENCY_SERVICE = 'ecurrency-service'
    private static final String BEAN_NAME = 'amqpInboundChannelAdapter'
    private static final String ID_FORMAT = '%s-%s'
    // <Long, AbstractEndpoint>
    private final Map endpointMap = [:] as ConcurrentHashMap

    @Autowired
    private ApplicationContext applicationContext

    EcurrencyMessageChannelTracker() {
        super()    }

    @Async
    void addedChannel(messageChannel, properties) {
        def ep = getRequiredProperty(ECURRENCY_PROVIDER, properties)
        def es = getRequiredProperty(ECURRENCY_SERVICE, properties)

        def endpoint = (AbstractEndpoint) applicationContext.getBean(BEAN_NAME, messageChannel, ep, es)
        endpointMap << [(id(ep, es)):endpoint]    }

    public void removingChannel(messageChannel, properties) {
        def ep = getRequiredProperty(ECURRENCY_PROVIDER, properties)
        def es = getRequiredProperty(ECURRENCY_SERVICE, properties)

        AbstractEndpoint endpoint

        if ((endpoint = endpointMap.remove((id(ep, es)))) {
            endpoint.stop()        }    }    private static def id(String[] args) {        def hash = 0L        args.each {            hash += it.hashCode()        }        hash    }}