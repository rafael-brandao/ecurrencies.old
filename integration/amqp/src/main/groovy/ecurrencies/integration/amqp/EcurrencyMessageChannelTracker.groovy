package ecurrencies.integration.amqp


class EcurrencyMessageChannelTracker  {
    private static final String ECURRENCY_SERVICE = 'ecurrency-service'

    private static final String ID_FORMAT = '%s-%s'

    private final Map endpointMap = [:] as ConcurrentHashMap

    @Autowired
    private ApplicationContext applicationContext

    EcurrencyMessageChannelTracker() {
        super()

    @Async
    void addedChannel(messageChannel, properties) {
        def ep = getRequiredProperty(ECURRENCY_PROVIDER, properties)
        def es = getRequiredProperty(ECURRENCY_SERVICE, properties)

        def endpoint = (AbstractEndpoint) applicationContext.getBean(BEAN_NAME, messageChannel, ep, es)


    public void removingChannel(messageChannel, properties) {
        def ep = getRequiredProperty(ECURRENCY_PROVIDER, properties)
        def es = getRequiredProperty(ECURRENCY_SERVICE, properties)

        AbstractEndpoint endpoint

        if ((endpoint = endpointMap.remove((id(ep, es)))) {
            endpoint.stop()