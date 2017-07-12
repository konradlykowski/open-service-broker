package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class ElasticSearchBindResponseDto implements BindResponseDto {
    String elasticSearchUsername
    String elasticSearchPassword
    String elasticSearchHost
    Integer elasticSearchPort
    Integer elasticSearchInteralPort
    Integer elasticSearchMgmtPort

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder {
            credentials {
                host(elasticSearchHost)
                port(elasticSearchPort)
                internal_port(elasticSearchInteralPort)
                mgmt_port(elasticSearchMgmtPort)
                username(elasticSearchUsername)
                password(elasticSearchPassword)
            }
        }
        return jsonBuilder.toPrettyString()
    }
}
