package com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class SearchGuardFacadeFactory {
    private final SearchGuardClientFactory searchGuardClientFactory
    private final ElasticSearchConfig elasticSearchConfig
    private final ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    SearchGuardFacadeFactory(SearchGuardClientFactory searchGuardClientFactory, ElasticSearchConfig elasticSearchConfig, ServiceInstanceRepository serviceInstanceRepository) {
        this.searchGuardClientFactory = searchGuardClientFactory
        this.elasticSearchConfig = elasticSearchConfig
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    SearchGuardFacade build(List<String> hosts, int port) {
        return new SearchGuardFacade(searchGuardClientFactory, elasticSearchConfig, serviceInstanceRepository , hosts, port)
    }
}
