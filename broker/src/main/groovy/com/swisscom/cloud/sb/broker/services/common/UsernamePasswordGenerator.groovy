package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.transform.CompileStatic


@CompileStatic
class UsernamePasswordGenerator {
    static String generateUsername(int length = 16) {
        new StringGenerator().randomAlphaNumeric(length)
    }

    static String generatePassword(int length = 30) {
        new StringGenerator().randomAlphaNumeric(length)
    }
}
