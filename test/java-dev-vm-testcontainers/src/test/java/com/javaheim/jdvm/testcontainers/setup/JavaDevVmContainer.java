/*
 * © 2024-2025 Javaheim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.javaheim.jdvm.testcontainers.setup;

import static com.javaheim.jdvm.testcontainers.constant.TestConstants.IMAGE_TAG;

import java.time.Duration;
import com.github.dockerjava.api.model.Volume;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.utility.DockerImageName;

import com.javaheim.jdvm.testcontainers.docker.DockerExecutor;

/**
 * The JavaDevVmContainer class provides a pre-configured singleton Docker container.
 * It ensures that the necessary setup is applied when the container is started.
 */
public class JavaDevVmContainer {

    private static final GenericContainer<?> JAVA_DEV_VM = new GenericContainer<>(
            DockerImageName.parse(IMAGE_TAG)).withPrivilegedMode(true)
            .withCreateContainerCmdModifier(cmd -> cmd.withVolumes(new Volume("/var/lib/docker")))
            .withEnv("DISPLAY", StringUtils.EMPTY)
            .waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(5 * 60)));

    static {
        getJavaDevVm().start();
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> getJavaDevVm().stop()));
    }

    /**
     * Provides access to the pre-configured JavaDevVm container.
     * This container is a singleton instance and is started during class initialization to
     * ensure it is running and ready for use in related operations and tests.
     */
    protected static GenericContainer<?> getJavaDevVm() {
        return JAVA_DEV_VM;
    }

    /**
     * Provides a command execution step for interacting with the pre-configured JavaDevVm container.
     * This method initializes a fluent API for defining and executing shell commands within the container.
     */
    protected static DockerExecutor.CommandStep getJavaDevVmExecutor() {
        return DockerExecutor.in(getJavaDevVm());
    }

}
