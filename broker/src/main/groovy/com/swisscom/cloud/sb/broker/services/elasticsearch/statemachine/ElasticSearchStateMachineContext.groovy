package com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine

import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshStateMachineContext
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchFreePortFinder
import groovy.transform.CompileStatic

@CompileStatic
class ElasticSearchStateMachineContext extends BoshStateMachineContext {
    ElasticSearchConfig elasticSearchConfig
    ElasticSearchFreePortFinder elasticSearchFreePortFinder
}
