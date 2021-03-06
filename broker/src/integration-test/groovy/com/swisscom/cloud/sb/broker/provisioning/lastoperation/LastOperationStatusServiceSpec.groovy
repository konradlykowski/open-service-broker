package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import org.springframework.beans.factory.annotation.Autowired

class LastOperationStatusServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationStatusService lastOperationStatusService
    @Autowired
    DBTestUtil dbTestUtil

    def "happy case: last operation status is returned correctly"() {
        given:
        def id = 'someId'
        LastOperation lastOperation = dbTestUtil.createLastOperation(id, LastOperation.Status.IN_PROGRESS)
        when:
        def dto = lastOperationStatusService.pollJobStatus(id)
        then:
        dto.status == CFLastOperationStatus.IN_PROGRESS
    }

    def "when the last operation status is not found an exception should be thrown"() {
        when:
        lastOperationStatusService.pollJobStatus('someUnknownId')
        then:
        Exception ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.LAST_OPERATION_NOT_FOUND)
    }
}
