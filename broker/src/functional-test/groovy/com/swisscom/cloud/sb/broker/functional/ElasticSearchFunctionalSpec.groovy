package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class ElasticSearchFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext

    @Autowired
    private ElasticSearchConfig elasticSearchConfig

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('elasticsearch', ServiceProviderLookup.findInternalName(ElasticSearchServiceProvider), 'bosh-deployment-teamplate-elasticsearch')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter(BoshFacade.PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE, 'elasticsearch-service', plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision ElasticSearch service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(600, true, true)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        noExceptionThrown()
    }

    def "deprovision ElasticSearch service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 400)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        noExceptionThrown()
    }

}