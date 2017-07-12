package com.swisscom.cloud.sb.broker.services.elasticsearch

import org.springframework.stereotype.Component

@Component
class ElasticSearchClientFactory {

    ElasticSearchClient build(String host, int port, String username, String password, String database) {
        return new ElasticSearchClient(username, password, host, port, database)
    }
}
