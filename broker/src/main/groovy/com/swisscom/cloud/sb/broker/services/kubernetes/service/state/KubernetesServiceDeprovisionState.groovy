package com.swisscom.cloud.sb.broker.services.kubernetes.service.state

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import groovy.transform.CompileStatic

@CompileStatic
enum KubernetesServiceDeprovisionState implements ServiceStateWithAction<KubernetesServiceStateMachineContext> {

    KUBERNETES_NAMESPACE_DELETION(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            stateContext.kubernetesFacade.deprovision(stateContext.lastOperationJobContext.deprovisionRequest)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),

    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp())

    public static final Map<String, ServiceStateWithAction> map = new TreeMap<>()

    static {
        for (KubernetesServiceDeprovisionState serviceState : KubernetesServiceDeprovisionState.values()) {
            map.put(serviceState.getServiceInternalState(), serviceState)
        }
    }

    private final LastOperation.Status status
    private final OnStateChange<KubernetesServiceStateMachineContext> onStateChange

    KubernetesServiceDeprovisionState(LastOperation.Status lastOperationStatus, OnStateChange<KubernetesServiceStateMachineContext> onStateChange) {
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

    static ServiceStateWithAction of(String state) {
        return map.get(state)
    }

    @Override
    StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }

}
