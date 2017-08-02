package com.swisscom.cloud.sb.broker.services.elasticsearch

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class ElasticSearchBindResponseDtoSpec extends Specification {

    def "json serialization works correctly"() {
        given:
        ElasticSearchBindResponseDto credentials = new ElasticSearchBindResponseDto(
                hosts: ['https://hosts'],
                port: 1234,
                internalPort: 2345,
                mgmtPort: 3456,
                username: 'username',
                password: 'password'
        )
        and:
        String expected = """{
                            "credentials": {
                                "host": "https://hosts",
                                "port": 1234,
                                "internal_port": 2345,
                                "mgmt_port": 3456,
                                "username": "username",
                                "password": "password",
                            }
                        }"""
        expect:
        JSONAssert.assertEquals(expected, credentials.toJson(), true)
    }
}
