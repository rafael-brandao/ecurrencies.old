package ecurrencies.integration.amqpimport static ecurrencies.commons.util.PropertyResolver.getRequiredPropertyimport static java.lang.String.formatimport java.util.concurrent.ConcurrentHashMapimport org.springframework.beans.factory.annotation.Autowiredimport org.springframework.context.ApplicationContextimport org.springframework.scheduling.annotation.Async
class EcurrencyMessageChannelTracker  {    private static final String ECURRENCY_PROVIDER = 'ecurrency-provider'
    private static final String ECURRENCY_SERVICE = 'ecurrency-service'
    private static final String BEAN_NAME = 'amqpInboundChannelAdapter'
    // <Long, AbstractEndpoint>
    private final Map endpointMap = [:] as ConcurrentHashMap

    @Autowired
    private ApplicationContext applicationContext

    EcurrencyMessageChannelTracker() {
        super()    }

    @Async
    void addedChannel(messageChannel, properties) {        channelAction(messageChannel, properties) { String ep, es ->            endpointMap << [(id(ep, es)):(applicationContext.getBean(BEAN_NAME, messageChannel, ep, es))]        }    }

    public void removingChannel(messageChannel, properties) {        channelAction(messageChannel, properties) { String ep, es ->            endpointMap.remove(id(ep, es))?.stop()        }    }    private void channelAction(messageChannel, properties, closure) {        def ep = getRequiredProperty(ECURRENCY_PROVIDER, properties)        def es = getRequiredProperty(ECURRENCY_SERVICE, properties)        closure(ep, es)    }    private static def id(String[] args) {        def hash = 0L        args.each { hash += it.hashCode() }        hash    }}