package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class ElasticSearchBindResponseDto implements BindResponseDto {
    String username
    String password
    List<String> hosts
    Integer port
    Integer internalPort
    Integer mgmtPort

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder {
            credentials {
                host(hosts.join(','))
                port(port)
                internal_port(internalPort)
                mgmt_port(mgmtPort)
                username(username)
                password(password)
            }
        }
        return jsonBuilder.toPrettyString()
    }
}
