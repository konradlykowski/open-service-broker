package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.IgnoreIf
import spock.lang.Stepwise

import javax.transaction.Transactional

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class ElasticSearchFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext

    @Autowired
    private ElasticSearchConfig elasticSearchConfig

    @Autowired
    private PlanRepository planRepository

    @Autowired
    private CFServiceRepository cfServiceRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('elasticsearch', ServiceProviderLookup.findInternalName(ElasticSearchServiceProvider), 'bosh-deployment-teamplate-elasticsearch')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter(BoshFacade.PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE, 'elasticsearch-service', plan)
    }

    def cleanupSpec() {
        //serviceLifeCycler.cleanup()
    }

    /**
     * After provisioning a service adjust the forwarded local ports to the new instance (see table `service_details` for `elastic_search_port`)
     * @return
     */
    def "provision ElasticSearch service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(600, true, true)

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        noExceptionThrown()
    }

    def "bind ElasticSearch service"() {
        when:
        def serviceInstance = serviceInstanceRepository.listAllForInternalName('elasticSearch').last()
        serviceLifeCycler.serviceInstanceId = serviceInstance.guid
        serviceLifeCycler.cfService = cfServiceRepository.findByName('elasticsearch')
        serviceLifeCycler.plan = planRepository.findAll().last()
        def bindResponse = serviceLifeCycler.bindServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }

    @Transactional
    def "unbind ElasticSearch service"() {
        when:
        def serviceInstance = serviceInstanceRepository.listAllForInternalName('elasticSearch').last()
        def serviceBinding = serviceInstance.bindings.last()
        serviceLifeCycler.serviceInstanceId = serviceInstance.guid
        serviceLifeCycler.serviceBindingId = serviceBinding.guid
        serviceLifeCycler.cfService = cfServiceRepository.findByName('elasticsearch')
        serviceLifeCycler.deleteServiceBindingAndAssert()

        then:
        noExceptionThrown()
    }

    def "deprovision ElasticSearch service instance"() {
        when:
        def serviceInstance = serviceInstanceRepository.listAllForInternalName('elasticSearch').last()
        serviceLifeCycler.serviceInstanceId = serviceInstance.guid
        serviceLifeCycler.cfService = cfServiceRepository.findByName('elasticsearch')
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 400)

        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        noExceptionThrown()
    }

}