package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.web.client.HttpClientErrorException

class BindingForFailedProvisioningFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyServiceManagerBased', ServiceProviderLookup.findInternalName(DummyServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2, true, true, ['success': false])
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
        when:
        def response = serviceLifeCycler.requestBindService('someKindaServiceInstanceGuid')
        then:
        def ex = thrown(HttpClientErrorException)
        ex.rawStatusCode == 412
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 35)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}