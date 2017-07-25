package com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine

import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchFreePortFinder
import spock.lang.Specification

class ElasticSearchDeprovisionStateSpec extends Specification {
    private ElasticSearchStateMachineContext context

    def setup() {
        context = new ElasticSearchStateMachineContext()
        context.elasticSearchFreePortFinder = Mock(ElasticSearchFreePortFinder)
        context.elasticSearchConfig = Stub(ElasticSearchConfig)
    }

    def "DEPROVISION_SUCCESS"() {
        when:
        ElasticSearchDeprovisionState.DEPROVISION_SUCCESS.triggerAction(null)

        then:
        0 * _._
    }
}
