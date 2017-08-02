package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardClientFactory
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import spock.lang.Ignore

@Ignore
class SearchGuardClientTest extends BaseSpecification {
    @Autowired
    SearchGuardClientFactory searchGuardClientFactory

    def "test get internal Users"() {
        given:
        def searchGuardClient = createClient()
        def jsonParser = new BasicJsonParser()

        when:
        def internalUsersAsBase64 = searchGuardClient.queryInternalUsers()
        def internalUsers = jsonParser.parseMap(new String(Base64.decodeBase64(internalUsersAsBase64)))

        then:
        internalUsers.size() > 0
        noExceptionThrown()
    }

    def createClient() {
        return searchGuardClientFactory.build("localhost", 9201)
    }
}
