package com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.UsernamePasswordGenerator
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchBindResponseDto
import com.swisscom.cloud.sb.broker.services.elasticsearch.ElasticSearchConfig
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component

@Slf4j
@CompileStatic
class SearchGuardFacade {

    private final SearchGuardClientFactory searchGuardClientFactory
    private final ElasticSearchConfig elasticSearchConfig
    private final ServiceInstanceRepository serviceInstanceRepository
    private final List<String> hosts
    private final Integer port

    SearchGuardFacade(SearchGuardClientFactory searchGuardClientFactory, ElasticSearchConfig elasticSearchConfig, ServiceInstanceRepository serviceInstanceRepository, List<String> hosts, Integer port) {
        this.searchGuardClientFactory = searchGuardClientFactory
        this.elasticSearchConfig = elasticSearchConfig
        this.serviceInstanceRepository = serviceInstanceRepository
        this.hosts = hosts
        this.port = port
    }

    ElasticSearchBindResponseDto createSearchGuardUser() {
        String username = UsernamePasswordGenerator.generateUsername()
        String password = UsernamePasswordGenerator.generatePassword()
        addInternalUser(username, password)
        addCustRolesMapping(username)
        new ElasticSearchBindResponseDto(
                username: username,
                password: password,
                hosts: hosts,
                port: port
        )
    }

    String deleteSearchGuardUser(String username) {
        removeInternalUser(username)
        removeCustRolesMapping(username)
    }

    private addCustRolesMapping(String username) {
        def rolesMapping = getRolesMapping()
        rolesMapping.get('sg_cust_access').asType(Map.class).get('users').asType(List.class).add(username)
        executeUpdateRolesMapping(rolesMapping)
    }

    private addInternalUser(String username, String password) {
        def internalUsers = getInternalUsers()
        internalUsers.put(username, [hash: generateBCryptedPassword(password)])
        executeUpdateInternalUsers(internalUsers, username)
    }

    private removeCustRolesMapping(String username) {
        def rolesMapping = getRolesMapping()
        rolesMapping.get('sg_cust_access').asType(Map.class).get('users').asType(List.class).remove(username)
        executeUpdateRolesMapping(rolesMapping)
    }

    private removeInternalUser(String username) {
        def internalUsers = getInternalUsers()
        internalUsers.remove(username)
        executeUpdateInternalUsers(internalUsers, username)
    }

    private SearchGuardClient createSearchGuardClient() {
        searchGuardClientFactory.build(hosts.first(), port)
    }

    private executeUpdateInternalUsers(Map<String, Object> internalUsers, String username) {
        String internalUsersJsonValue = new String(Base64.encodeBase64(new JsonBuilder(internalUsers).toString().getBytes("UTF-8")))
        createSearchGuardClient().updateInternalUsers(internalUsersJsonValue) ? username : null
    }

    private executeUpdateRolesMapping(Map<String, Object> rolesMapping) {
        String rolesMappingJsonValue = new String(Base64.encodeBase64(new JsonBuilder(rolesMapping).toString().getBytes("UTF-8")))
        createSearchGuardClient().updateRolesMapping(rolesMappingJsonValue)
    }

    @VisibleForTesting
    private String generateBCryptedPassword(String passwordToHash) {
        BCrypt.hashpw(Objects.requireNonNull(passwordToHash), BCrypt.gensalt(12))
    }

    @VisibleForTesting
    private Map<String, Object> getInternalUsers() {
        BasicJsonParser jsonParser = new BasicJsonParser()
        def internalUsersAsBase64 = createSearchGuardClient().queryInternalUsers()
        def internalUsers = jsonParser.parseMap(new String(Base64.decodeBase64(internalUsersAsBase64)))
        return internalUsers
    }

    @VisibleForTesting
    private Map<String, Object> getRolesMapping() {
        BasicJsonParser jsonParser = new BasicJsonParser()
        def rolesMappingAsBase64 = createSearchGuardClient().queryRolesMapping()
        def rolesMapping = jsonParser.parseMap(new String(Base64.decodeBase64(rolesMappingAsBase64)))
        return rolesMapping
    }
}