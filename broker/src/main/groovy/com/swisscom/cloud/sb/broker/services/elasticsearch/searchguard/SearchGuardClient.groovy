package com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard

import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.error.ErrorCode.ELASTIC_SEARCH_SEARCH_GUARD_AUTHENTICATION_FAILED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.UNAUTHORIZED

@Slf4j
@CompileStatic
class SearchGuardClient {
    public static final String USER_INDEX = '/searchguard/internalusers/0'
    public static final String USER_INDEX_SOURCE = USER_INDEX + '/_source'
    public static final String ROLESMAPPING_INDEX = '/searchguard/rolesmapping/0'
    public static final String ROLESMAPPING_INDEX_SOURCE = ROLESMAPPING_INDEX + '/_source'

    private final RestTemplateFactory restTemplateFactory
    private final ElasticSearchConfig elasticSearchConfig
    private final String hostname
    private final Integer port

    SearchGuardClient(RestTemplateFactory restTemplateFactory, ElasticSearchConfig elasticSearchConfig, String hostname, Integer port = 9200) {
        this.restTemplateFactory = restTemplateFactory
        this.elasticSearchConfig = elasticSearchConfig
        this.hostname = hostname
        this.port = port
    }

    String updateInternalUsers(String internalUsersJsonValue) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        String payload = generateInternalUsersDocument(internalUsersJsonValue)
        ResponseEntity<String> response = createRestTemplate().exchange(baseUrl() + USER_INDEX, HttpMethod.POST, new HttpEntity<String>(payload, headers), String.class)
        return response.statusCode.'2xxSuccessful' ? response.body : null
    }

    String updateRolesMapping(String rolesMappingJsonValue) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        String payload = generateInternalRolesMappingDocument(rolesMappingJsonValue)
        ResponseEntity<String> response = createRestTemplate().exchange(baseUrl() + ROLESMAPPING_INDEX, HttpMethod.POST, new HttpEntity<String>(payload, headers), String.class)
        return response.statusCode.'2xxSuccessful' ? response.body : null
    }

    String queryInternalUsers() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        ResponseEntity<Map<String, Object>> response = createRestTemplate().exchange(baseUrl() + USER_INDEX_SOURCE, HttpMethod.GET, new HttpEntity<Object>(headers), Map.class)
        Map<String, Object> responseMap = response.statusCode.'2xxSuccessful' ? response.body : null
        return responseMap ? responseMap.getOrDefault("internalusers", null) : null
    }

    String queryRolesMapping() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        ResponseEntity<Map<String, Object>> response = createRestTemplate().exchange(baseUrl() + ROLESMAPPING_INDEX_SOURCE, HttpMethod.GET, new HttpEntity<Object>(headers), Map.class)
        Map<String, Object> responseMap = response.statusCode.'2xxSuccessful' ? response.body : null
        return responseMap ? responseMap.getOrDefault("rolesmapping", null) : null
    }

    private String generateInternalUsersDocument(String internalUsersJsonValue) {
        return """{"internalusers": "${internalUsersJsonValue}"}"""
    }

    private String generateInternalRolesMappingDocument(String rolesMappingJsonValue) {
        return """{"rolesmapping": "${rolesMappingJsonValue}"}"""
    }

    private RestTemplate createRestTemplate() {
        def restTemplate = restTemplateFactory.buildWithSSLClientCertificate(elasticSearchConfig.clientKeystorePath, elasticSearchConfig.clientKeystorePassword)
        restTemplate.setErrorHandler(new CustomErrorHandler())
        return restTemplate
    }

    @Slf4j
    private static class CustomErrorHandler extends DefaultResponseErrorHandler {
        @Override
        void handleError(ClientHttpResponse response) throws IOException {
            if (UNAUTHORIZED == response.statusCode || FORBIDDEN == response.statusCode) {
                ELASTIC_SEARCH_SEARCH_GUARD_AUTHENTICATION_FAILED.throwNew()
            }
            log.error("Elasticsearch call failed, status:${response.statusCode}, statusText:${response.statusText}, body:${response.getBody().text}")
            super.handleError(response)
        }
    }

    private String baseUrl() {
        "https://" + hostname + ":" + port
    }
}