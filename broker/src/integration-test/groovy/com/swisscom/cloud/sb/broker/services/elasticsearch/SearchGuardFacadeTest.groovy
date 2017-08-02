package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.services.elasticsearch.searchguard.SearchGuardFacadeFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class SearchGuardFacadeTest extends BaseSpecification {

    @Autowired
    SearchGuardFacadeFactory searchGuardFacadeFactory

    def "add new SearchGuard user"() {
        when:
        def result = searchGuardFacadeFactory.build(["localhost"], 9201).createSearchGuardUser()

        then:
        noExceptionThrown()
        result
    }

    def "delete SearchGuard user"() {
        given:
        def searchGuardFacade = searchGuardFacadeFactory.build(["localhost"], 9201)
        def result = searchGuardFacade.createSearchGuardUser()
        def internalUsersSizeBefore = searchGuardFacade.getInternalUsers().size()

        when:
        searchGuardFacade.deleteSearchGuardUser(result.username)

        then:
        searchGuardFacade.getInternalUsers().size() == (internalUsersSizeBefore - 1)
        noExceptionThrown()
    }
}
