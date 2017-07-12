package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.services.bosh.BoshBasedServiceConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.elasticsearch")
class ElasticSearchConfig implements BoshBasedServiceConfig {

}
