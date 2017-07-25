package com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine

import com.swisscom.cloud.sb.broker.services.common.UsernamePasswordGenerator
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchFreePortFinder
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import spock.lang.Specification

class ElasticSearchProvisionStateSpec extends Specification {
    private ElasticSearchStateMachineContext context

    def setup() {
        context = new ElasticSearchStateMachineContext()
        context.elasticSearchFreePortFinder = Mock(ElasticSearchFreePortFinder)
        context.elasticSearchConfig = Stub(ElasticSearchConfig)
    }

    def "FIND_PORTS"() {
        given:
        1 * context.elasticSearchFreePortFinder.findFreePorts(3) >> [1234, 2345, 3456]

        when:
        def result = ElasticSearchProvisionState.FIND_PORTS.triggerAction(context)

        then:
        def helper = ServiceDetailsHelper.from(result.details)
        helper.getValue(ServiceDetailKey.ELASTIC_SEARCH_PORT) == '1234'
        helper.getValue(ServiceDetailKey.ELASTIC_SEARCH_PORT_INTERNAL) == '2345'
        helper.getValue(ServiceDetailKey.ELASTIC_SEARCH_PORT_MGMT) == '3456'
    }

    def "GENERATE_USERNAME_PASSWORD"() {
        given:
        GroovyMock(UsernamePasswordGenerator, global: true)
        UsernamePasswordGenerator.generateUsername() >> "highsecureusername"
        UsernamePasswordGenerator.generatePassword() >> "highsecurepassword"

        when:
        def result = ElasticSearchProvisionState.GENERATE_USERNAME_PASSWORD.triggerAction(context)

        then:
        def helper = ServiceDetailsHelper.from(result.details)
        helper.getValue(ServiceDetailKey.ELASTIC_SEARCH_USER) == 'highsecureusername'
        helper.getValue(ServiceDetailKey.ELASTIC_SEARCH_PASSWORD) == 'highsecurepassword'
    }
}
