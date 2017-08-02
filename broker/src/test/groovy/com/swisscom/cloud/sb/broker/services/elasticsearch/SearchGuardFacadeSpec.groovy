package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardClient
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardClientFactory
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardFacade
import org.apache.commons.codec.binary.Base64
import org.springframework.security.crypto.bcrypt.BCrypt
import spock.lang.Specification

class SearchGuardFacadeSpec extends Specification {
    SearchGuardFacade searchGuardFacade
    SearchGuardClientFactory searchGuardClientFactory
    SearchGuardClient searchGuardClient
    ElasticSearchConfig elasticSearchConfig
    ServiceInstanceRepository serviceInstanceRepository

    static final String PASSWORDHASH_ADMIN = '$2a$12$VcCDgh2NDk07JGN0rjGbM.Ad41qVR/YFJcgHp0UGns5JDymv..TOG'
    static final String PASSWORDHASH_CUST = '$2a$12$53JDesH6FSi9SYloo/sqeO3w3WI7n0BoRhWcJf0V8Kzocpiwg6eXC'

    def setup() {
        searchGuardClient = Mock(SearchGuardClient)
        searchGuardClientFactory = Mock(SearchGuardClientFactory)
        searchGuardClientFactory.build(_, _) >> searchGuardClient

        and:
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        searchGuardFacade = new SearchGuardFacade(searchGuardClientFactory, elasticSearchConfig, serviceInstanceRepository, ["localhost"], 9200)
    }

    def "creation of user functions correctly"() {
        given:
        searchGuardClient.queryInternalUsers() >> defaultAdminAndCustUserAsBase64()
        searchGuardClient.queryRolesMapping() >> defaultRolesMappingAsBase64()

        when:
        def result = searchGuardFacade.createSearchGuardUser()

        then:
        1 * searchGuardClient.updateInternalUsers(_) >> { String internalUsersJsonValue ->
            new String(Base64.decodeBase64(internalUsersJsonValue.getBytes('UTF-8'))).startsWith('{"admin":{"hash":"' + PASSWORDHASH_ADMIN)
        }
        1 * searchGuardClient.updateRolesMapping(_) >> { String rolesMappingJsonValue ->
            new String(Base64.decodeBase64(rolesMappingJsonValue.getBytes('UTF-8'))).startsWith('{"sg_all_access":{"users":)')
        }

        expect:
        result.username.length() == 16
        result.password.length() == 30
    }

    def "password generation works correctly"() {
        when:
        def searchGuardDefaultSalt = "12"

        then:
        def bcryptedPassword = searchGuardFacade.generateBCryptedPassword("admin")

        expect:
        bcryptedPassword.startsWith("\$2a\$${searchGuardDefaultSalt}\$")
        BCrypt.checkpw("admin", bcryptedPassword)
    }

    def "getting internal users works correctly"() {
        when:
        searchGuardClient.queryInternalUsers() >> defaultAdminAndCustUserAsBase64()

        then:
        def internalUsers = searchGuardFacade.getInternalUsers()

        expect:
        internalUsers.get("admin").asType(Map.class).get("hash").equals(PASSWORDHASH_ADMIN)
        internalUsers.get("cust").asType(Map.class).get("hash").equals(PASSWORDHASH_CUST)
    }

    static String defaultAdminAndCustUserAsBase64() {
        def jsonDocument = '{"admin":{"hash":"' + PASSWORDHASH_ADMIN + '"},"cust":{"hash":"' + PASSWORDHASH_CUST + '"}}'
        new String(Base64.encodeBase64(jsonDocument.getBytes("UTF-8")))
    }

    static String defaultRolesMappingAsBase64() {
        def jsonDocument = '{"sg_all_access":{"users":["admin"]},"sg_cust_access":{"users":["cust"]}}'
        new String(Base64.encodeBase64(jsonDocument.getBytes("UTF-8")))
    }
}

