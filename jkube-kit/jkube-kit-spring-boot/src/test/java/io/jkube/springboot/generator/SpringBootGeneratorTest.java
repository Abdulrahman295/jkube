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
package io.jkube.springboot.generator;

import io.jkube.generator.api.GeneratorContext;
import io.jkube.kit.build.service.docker.ImageConfiguration;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author roland
 * @since 28/11/16
 */
public class SpringBootGeneratorTest {

    @Mocked
    private GeneratorContext context;

    @Mocked
    private MavenProject project;

    @Mocked
    private Build build;

    @Test
    public void notApplicable() throws IOException {
        SpringBootGenerator generator = new SpringBootGenerator(createGeneratorContext());
        assertFalse(generator.isApplicable((List<ImageConfiguration>) Collections.EMPTY_LIST));
    }

    @Test
    public void javaOptions() throws IOException, MojoExecutionException {
        SpringBootGenerator generator = new SpringBootGenerator(createGeneratorContext());
        List<String> extraOpts = generator.getExtraJavaOptions();
        assertNotNull(extraOpts);
        assertEquals(0, extraOpts.size());

        List<ImageConfiguration> configs = generator.customize(new ArrayList<>(), true);
        assertEquals(1, configs.size());
        Map<String, String> env = configs.get(0).getBuildConfiguration().getEnv();
        assertNull(env.get("JAVA_OPTIONS"));
    }

    private GeneratorContext createGeneratorContext() throws IOException {
        new Expectations() {{
            context.getProject(); result = project;
            project.getBuild(); result = build;
            String tempDir = Files.createTempDirectory("springboot-test-project").toFile().getAbsolutePath();

            // TODO: Prepare more relastic test setup
            build.getDirectory(); result = tempDir;
            build.getOutputDirectory(); result = tempDir;
            project.getPlugin(anyString); result = null; minTimes = 0;
            project.getBuildPlugins(); result = null;
            project.getVersion(); result = "1.0.0"; minTimes = 0;
        }};
        return context;
    }
}
