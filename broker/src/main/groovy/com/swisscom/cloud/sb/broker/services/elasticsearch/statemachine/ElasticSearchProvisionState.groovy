package com.swisscom.cloud.sb.broker.services.elasticsearch.statemachine

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshProvisionState
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.util.logging.Slf4j

import static com.swisscom.cloud.sb.broker.model.ServiceDetail.from

@Slf4j
enum ElasticSearchProvisionState implements ServiceStateWithAction<ElasticSearchStateMachineContext> {
    FIND_PORTS(LastOperation.Status.IN_PROGRESS, new OnStateChange<ElasticSearchStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(ElasticSearchStateMachineContext stateContext) {
            List<Integer> ports = stateContext.elasticSearchFreePortFinder.findFreePorts(3)
            return new StateChangeActionResult(go2NextState: true, details: [
                    from(ServiceDetailKey.ELASTIC_SEARCH_PORT, ports.get(0).toString()),
                    from(ServiceDetailKey.ELASTIC_SEARCH_PORT_INTERNAL, ports.get(1).toString()),
                    from(ServiceDetailKey.ELASTIC_SEARCH_PORT_MGMT, ports.get(2).toString())
            ])
        }
    }),

    PROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp())

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshProvisionState.values()) {
            if (map.containsKey(serviceState.getServiceInternalState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceInternalState()} already exists in:${ElasticSearchProvisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceInternalState(), serviceState);
            }
        }
    }

    private final LastOperation.Status status
    private final OnStateChange<ElasticSearchStateMachineContext> onStateChange

    ElasticSearchProvisionState(LastOperation.Status lastOperationStatus, OnStateChange<ElasticSearchStateMachineContext> onStateChange) {
        this.status = lastOperationStatus
        this.onStateChange = onStateChange
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    public static ServiceStateWithAction of(String state) {
        return map.get(state)
    }

    @Override
    StateChangeActionResult triggerAction(ElasticSearchStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }
}