package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.AbstractBoshBasedServiceProviderSpec
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate
import com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine.ElasticSearchProvisionState
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey

class ElasticSearchServiceProviderSpec extends AbstractBoshBasedServiceProviderSpec<ElasticSearchServiceProvider> {
    private String serviceInstanceGuid = 'serviceInstanceGuid'


    def setup() {
        serviceProvider.serviceConfig = new ElasticSearchConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
    }

    def "template customization works correctly"() {
        given:
        String username = 'elastic_search_user'
        String password = 'elastic_search_password'
        String port = '1234'
        String portMgmt = '2345'
        String portInternal = '3456'

        and:
        def serviceInstance = new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_USER, username),
                                                            ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PASSWORD, password),
                                                            ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT, port),
                                                            ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT_MGMT, portMgmt),
                                                            ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT_INTERNAL, portInternal)])
        1 * serviceProvider.provisioningPersistenceService.getServiceInstance(serviceInstanceGuid) >> serviceInstance

        and:
        BoshTemplate template = Mock(BoshTemplate)
        def instanceCount = 3
        1 * template.instanceCount() >> instanceCount

        and:
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)

        when:
        serviceProvider.customizeBoshTemplate(template, request)

        then:
        1 * template.replace(ElasticSearchServiceProvider.PORT_HTTP, port)
        1 * template.replace(ElasticSearchServiceProvider.PORT_INTERNAL, portInternal)
        1 * template.replace(ElasticSearchServiceProvider.PORT_MGMT, portMgmt)
    }

    def "StateMachineContext is created correctly"() {
        given:
        def context = new LastOperationJobContext()
        when:
        def stateMachineContext = serviceProvider.createStateMachineContext(context)
        then:
        stateMachineContext.lastOperationJobContext == context
    }

    def "provision state is initialized correctly if context does not contain any state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == ElasticSearchProvisionState.FIND_PORTS
    }

    def "provision state is initialized correctly if context include some previous state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: ElasticSearchProvisionState.FIND_PORTS.toString()))
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == ElasticSearchProvisionState.FIND_PORTS
    }

    def "happy path: requestProvision"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: ElasticSearchProvisionState.PROVISION_SUCCESS.toString()))
        when:
        def result=serviceProvider.requestProvision(context)
        then:
        result
    }

}
