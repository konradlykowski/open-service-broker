package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.AbstractBoshBasedServiceProviderSpec
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate
import com.swisscom.cloud.sb.broker.services.common.UsernamePasswordGenerator
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardFacade
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardFacadeFactory
import com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine.ElasticSearchProvisionState
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import groovy.json.JsonSlurper

class ElasticSearchServiceProviderSpec extends AbstractBoshBasedServiceProviderSpec<ElasticSearchServiceProvider> {
    private String serviceInstanceGuid = 'serviceInstanceGuid'
    private SearchGuardFacade searchGuardFacade

    def setup() {
        serviceProvider.serviceConfig = new ElasticSearchConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
        serviceProvider.searchGuardFacadeFactory = Mock(SearchGuardFacadeFactory)
        searchGuardFacade = Mock(SearchGuardFacade)
        serviceProvider.searchGuardFacadeFactory.build(_, _) >> searchGuardFacade
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
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)
        def template = Mock(BoshTemplate)

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

    def "provision state is initialized correctly if context does not contain any state"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == ElasticSearchProvisionState.FIND_PORTS
    }

    def "provision state is initialized correctly if context include some previous state"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: ElasticSearchProvisionState.FIND_PORTS.toString()))
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == ElasticSearchProvisionState.FIND_PORTS
    }

    def "happy path: requestProvision"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: ElasticSearchProvisionState.PROVISION_SUCCESS.toString()))
        when:
        def result = serviceProvider.requestProvision(context)
        then:
        result
    }


    def "Bind functions correctly"() {
        given:
        BindRequest request = new BindRequest(
                serviceInstance: new ServiceInstance(guid: 'guid',
                        details: [ServiceDetail.from(ServiceDetailKey.UID, "uid"),
                                  ServiceDetail.from(ServiceDetailKey.HOST, 'host1'),
                                  ServiceDetail.from(ServiceDetailKey.HOST, 'host2'),
                                  ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT, '9200'),
                                  ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT_INTERNAL, '19200'),
                                  ServiceDetail.from(ServiceDetailKey.ELASTIC_SEARCH_PORT_MGMT, '29200')])
        )

        and:
        1 * searchGuardFacade.createSearchGuardUser() >> new ElasticSearchBindResponseDto(
                username: UsernamePasswordGenerator.generateUsername(),
                password: UsernamePasswordGenerator.generatePassword(),
                hosts: ["host1", "host2"],
                port: 9200
        )

        when:
        def bindResult = serviceProvider.bind(request)

        then:
        def details = ServiceDetailsHelper.from(bindResult.details)

        and:
        def credentials = bindResult.credentials.toJson()
        def json = new JsonSlurper().parseText(credentials)
        json.credentials.username.length() == 16
        json.credentials.password.length() == 30
    }

}
