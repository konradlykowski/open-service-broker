package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapper
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.KubernetesFacade
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator.KubernetesTemplateVariablesDecorator
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators.KubernetesTemplatePasswordGenerator
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
@Log4j
@CompileStatic
class KubernetesFacadeRedis implements KubernetesFacade {

    private final KubernetesClient<?> kubernetesClient
    private final KubernetesTemplateManager kubernetesTemplateManager
    private final EndpointMapperParamsDecorated endpointMapperParamsDecorated
    private final KubernetesRedisConfig kubernetesConfig


    @Autowired
    KubernetesFacadeRedis(KubernetesRedisConfig kubernetesConfig, KubernetesClient kubernetesClient, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
        this.kubernetesConfig = kubernetesConfig
    }


    Collection<ServiceDetail> provision(ProvisionRequest context) {
        Map<String, String> passMap = (new KubernetesTemplatePasswordGenerator()).generatePassword()
        List<ResponseEntity> responses = new LinkedList()
        for (KubernetesTemplate kubernetesTemplate : kubernetesTemplateManager.getTemplates()) {
            (new KubernetesTemplateVariablesDecorator()).replaceTemplate(kubernetesTemplate, context, passMap, kubernetesConfig.redisConfigurationDefaults)
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(kubernetesTemplate.getKind(), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            responses.add(kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, kubernetesTemplate.build(), urlReturn.getSecond().class))
        }
        return buildServiceDetailsList(passMap, responses)
    }


    void deprovision(DeprovisionRequest request) {
        kubernetesClient.exchange(EndpointMapper.INSTANCE.getEndpointUrlByType("Namespace").getFirst() + "/" + request.serviceInstanceGuid,
                HttpMethod.DELETE, "", Object.class)
    }

    private Collection<ServiceDetail> buildServiceDetailsList(Map<String, String> passMap, List<ResponseEntity> responses) {
        return new LinkedList() {
            {
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_HOST, kubernetesConfig.getKubernetesRedisHost()))
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PORT, getRedisMasterPort(responses)))
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD, passMap.get(KubernetesTemplateConstants.REDIS_PASS.getValue())))
            }
        }
    }

    private String getRedisMasterPort(List<ResponseEntity> responses) {
        for (ResponseEntity r : responses) {
            if (r != null && r.getBody() instanceof ServiceResponse) {
                ServiceResponse s = (ServiceResponse) r.getBody()
                if (KubernetesTemplateConstants.ROLE_MASTER.getValue().equals(s.spec.selector.role)) {
                    return s.spec.ports[0].nodePort
                }
            }
        }
    }


}
