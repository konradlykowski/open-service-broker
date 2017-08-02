package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardClient
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class SearchGuardClientSpec extends Specification {
    public static final String URL = 'searchguard.localhost'
    public static final Integer PORT = 1234

    SearchGuardClient searchGuardClient
    RestTemplateFactory restTemplateFactory
    MockRestServiceServer mockServer

    def setup() {
        restTemplateFactory = Mock(RestTemplateFactory)
        RestTemplate restTemplate = new RestTemplate()
        restTemplateFactory.buildWithSSLClientCertificate(_, _) >> restTemplate
        mockServer = MockRestServiceServer.createServer(restTemplate)
        and:
        searchGuardClient = new SearchGuardClient(restTemplateFactory, new ElasticSearchConfig(), URL, PORT)
    }

    def "updateInternalUsers functions correctly"() {
        given:
        def expectedPayload = '{"internalusers": "' + SearchGuardFacadeSpec.defaultAdminAndCustUserAsBase64() + '"}'
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + SearchGuardClient.USER_INDEX))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockRestRequestMatchers.content().string(expectedPayload))
                .andRespond(MockRestResponseCreators.withSuccess('', MediaType.APPLICATION_JSON))
        when:
        def internalUsersJsonValue = SearchGuardFacadeSpec.defaultAdminAndCustUserAsBase64()
        searchGuardClient.updateInternalUsers(internalUsersJsonValue)

        then:
        mockServer.verify()
    }

    def "queryInternalUsers functions correctly"() {
        given:
        def expectedInternalUsersAsBase64 = "W3siZm9vIjoiYmFyIn1d"
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + SearchGuardClient.USER_INDEX_SOURCE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andRespond(MockRestResponseCreators.withSuccess('{"internalusers":"' + expectedInternalUsersAsBase64 + '"}', MediaType.APPLICATION_JSON))
        when:
        def internalUsersAsBase64 = searchGuardClient.queryInternalUsers()

        then:
        internalUsersAsBase64.equals(expectedInternalUsersAsBase64)
        mockServer.verify()
    }

    private String baseUrl() {
        "https://" + URL + ":" + PORT + ""
    }
}
