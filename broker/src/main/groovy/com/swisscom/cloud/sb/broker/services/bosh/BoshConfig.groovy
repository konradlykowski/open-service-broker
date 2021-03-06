package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.CompileStatic

@CompileStatic
trait BoshConfig implements Config {
    String boshDirectorBaseUrl
    String boshDirectorUsername
    String boshDirectorPassword
}
