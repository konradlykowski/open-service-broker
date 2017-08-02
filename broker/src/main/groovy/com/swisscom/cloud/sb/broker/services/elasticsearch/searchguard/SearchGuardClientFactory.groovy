package com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard

import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class SearchGuardClientFactory {
    private final RestTemplateFactory restTemplateFactory
    private final ElasticSearchConfig elasticSearchConfig

    @Autowired
    SearchGuardClientFactory(RestTemplateFactory restTemplateFactory, ElasticSearchConfig elasticSearchConfig) {
        this.restTemplateFactory = restTemplateFactory
        this.elasticSearchConfig = elasticSearchConfig
    }
    SearchGuardClient build(String host, int port) {
        return new SearchGuardClient(restTemplateFactory, elasticSearchConfig, host, port)
    }
}
