package io.jshift.kit.build.service.docker.auth;

import java.io.IOException;

import io.jshift.kit.build.api.auth.RegistryAuth;
import io.jshift.kit.build.api.auth.RegistryAuthHandler;
import io.jshift.kit.build.service.docker.auth.ecr.EcrExtendedAuth;
import io.jshift.kit.common.KitLogger;
import io.jshift.kit.build.service.docker.auth.ecr.EcrExtendedAuth;

/**
 * @author roland
 * @since 21.10.18
 */
public class EcrExtendedRegistryAuthHandler implements RegistryAuthHandler.Extender {

    private final KitLogger log;

    public EcrExtendedRegistryAuthHandler(KitLogger log) {
        this.log = log;
    }

    @Override
    public String getId() {
        return "ecr";
    }

    public RegistryAuth extend(RegistryAuth given, String registry) throws IOException {
        EcrExtendedAuth ecr = new EcrExtendedAuth(log, registry);
        if (ecr.isAwsRegistry()) {
            return ecr.extendedAuth(given);
        }
        return given;
    }
}
