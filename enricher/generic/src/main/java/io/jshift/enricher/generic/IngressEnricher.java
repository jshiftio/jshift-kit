package io.jshift.enricher.generic;

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressSpecBuilder;
import io.jshift.kit.config.resource.JshiftAnnotations;
import io.jshift.kit.config.resource.PlatformMode;
import io.jshift.kit.config.resource.ResourceConfig;
import io.jshift.maven.enricher.api.BaseEnricher;
import io.jshift.maven.enricher.api.MavenEnricherContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enricher which generates an Ingress for each exposed Service
 */

public class IngressEnricher extends BaseEnricher {

    public IngressEnricher(MavenEnricherContext buildContext) {
        super(buildContext, "jshift-ingress");
    }

    @Override
    public void create(PlatformMode platformMode, final KubernetesListBuilder listBuilder) {
        if (platformMode == PlatformMode.kubernetes) {
            final List<Ingress> ingresses = new ArrayList<>();
            listBuilder.accept(new TypedVisitor<ServiceBuilder>() {
                @Override
                public void visit(ServiceBuilder serviceBuilder) {
                    addIngress(listBuilder, serviceBuilder);
                }
            });

        }
    }

    private void addIngress(KubernetesListBuilder listBuilder, ServiceBuilder serviceBuilder) {
        ObjectMeta metadata = serviceBuilder.getMetadata();
        if (metadata != null && isExposedService(serviceBuilder)) {
            String name = metadata.getName();
            if (!hasIngress(listBuilder, name)) {
                Integer servicePort = getServicePort(serviceBuilder);
                if (servicePort != null) {
                    ResourceConfig resourceConfig = getConfiguration().getResource().orElse(null);

                    IngressBuilder ingressBuilder = new IngressBuilder().
                            withMetadata(serviceBuilder.getMetadata()).
                            withNewSpec().

                            endSpec();
                    IngressSpecBuilder specBuilder = new IngressSpecBuilder().withBackend(new IngressBackendBuilder().
                            withNewServiceName(name).
                            withNewServicePort(getServicePort(serviceBuilder)).
                            build());

                    if (resourceConfig != null) {
                        specBuilder.addAllToRules(resourceConfig.getIngressRules());
                    }
                }
            }
        }
    }

    private Integer getServicePort(ServiceBuilder serviceBuilder) {
        ServiceSpec spec = serviceBuilder.getSpec();
        if (spec != null) {
            List<ServicePort> ports = spec.getPorts();
            if (ports != null && ports.size() > 0) {
                for (ServicePort port : ports) {
                    if (port.getName().equals("http") || port.getProtocol().equals("http")) {
                        return port.getPort();
                    }
                }
                ServicePort servicePort = ports.get(0);
                if (servicePort != null) {
                    return servicePort.getPort();
                }
            }
        }
        return null;
    }

    /**
     * Returns true if we already have a route created for the given name
     */
    private boolean hasIngress(final KubernetesListBuilder listBuilder, final String name) {
        final AtomicBoolean answer = new AtomicBoolean(false);
        listBuilder.accept(new TypedVisitor<IngressBuilder>() {

            @Override
            public void visit(IngressBuilder builder) {
                ObjectMeta metadata = builder.getMetadata();
                if (metadata != null && name.equals(metadata.getName())) {
                    answer.set(true);
                }
            }
        });
        return answer.get();
    }

    private boolean isExposedService(ServiceBuilder serviceBuilder) {
        Service service = serviceBuilder.build();
        return isExposedService(service);
    }

    private boolean isExposedService(Service service) {
        ObjectMeta metadata = service.getMetadata();
        if (metadata != null) {
            Map<String, String> labels = metadata.getLabels();
            if (labels != null) {
                if ("true".equals(labels.get("expose")) || "true".equals(labels.get(JshiftAnnotations.SERVICE_EXPOSE_URL.value()))) {
                    return true;
                }
            }
        } else {
            log.info("No Metadata for service! " + service);
        }
        return false;
    }
}
