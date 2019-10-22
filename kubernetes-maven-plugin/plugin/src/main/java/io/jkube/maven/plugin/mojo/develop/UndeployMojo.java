/**
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package io.jkube.maven.plugin.mojo.develop;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.jkube.maven.plugin.mojo.build.ApplyMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.List;
import java.util.Set;

import static io.jkube.kit.config.service.kubernetes.KubernetesClientUtil.deleteEntities;

/**
 * Undeploys (deletes) the kubernetes resources generated by the current project.
 * <br>
 * This goal is the opposite to the <code>k8s:run</code> or <code>k8s:deploy</code> goals.
 */
@Mojo(name = "undeploy", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.INSTALL)
public class UndeployMojo extends ApplyMojo {
    @Override
    protected void applyEntities(KubernetesClient kubernetes, String namespace, String fileName, Set<HasMetadata> entities) throws Exception {
        deleteCustomEntities(kubernetes, namespace, resources.getCrdContexts());
        deleteEntities(kubernetes, namespace, entities, s2iBuildNameSuffix, log);
    }

    private void deleteCustomEntities(KubernetesClient kubernetes, String namespace, List<String> customResourceDefinitions) throws Exception {
        processCustomEntities(kubernetes, namespace, customResourceDefinitions, true);
    }
}
