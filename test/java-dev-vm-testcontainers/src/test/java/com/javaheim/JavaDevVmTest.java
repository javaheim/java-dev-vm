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

package com.javaheim;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.time.Year;
import com.github.dockerjava.api.model.Volume;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.javaheim.util.CommandExecutor;
import com.javaheim.util.TestConstants;

/**
 * Java DEV VM General Tests
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class JavaDevVmTest {

    @Container
    private static final GenericContainer<?> JAVA_DEV_VM = new GenericContainer<>(
            DockerImageName.parse(TestConstants.IMAGE_TAG)).withPrivilegedMode(true)
            .withCreateContainerCmdModifier(cmd -> cmd.withVolumes(new Volume("/var/lib/docker")))
            .waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(5 * 60)));

    private final CommandExecutor commandExecutor = new CommandExecutor(JAVA_DEV_VM);

    @Test
    void runningAndHealthy() {
        assertThat(JAVA_DEV_VM.isPrivilegedMode()).isTrue();
        assertThat(JAVA_DEV_VM.isRunning()).isTrue();
        assertThat(JAVA_DEV_VM.isHealthy()).isTrue();
    }

    @Test
    void testLauncher() throws IOException, InterruptedException {
        System.out.println(JAVA_DEV_VM.getLogs());
        commandExecutor.assertPathExistsAndContains(TestConstants.USER_HOME + "/.jdvm-launcher", "01-terminal.app",
                "02-firefox.app");
    }

    @Test
    void testLicenses() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/licenses", "LICENSE", "NOTICE");
        commandExecutor.assertFileContains("/licenses/LICENSE", "© 2024-" + Year.now().getValue() + " Javaheim");
        commandExecutor.assertFileContains("/licenses/LICENSE", "Apache License, Version 2.0");
        commandExecutor.assertFileContains("/licenses/NOTICE", "© 2024-" + Year.now().getValue() + " Javaheim");
        commandExecutor.assertFileContains("/licenses/NOTICE", "Apache License, Version 2.0");
        commandExecutor.assertFileContains("/licenses/NOTICE", "LicenseRef-ThirdParty");
    }

    @Test
    void testConfigs() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-config", "betterfox-policies.json", "betterfox-user.js",
                "docker-daemon.json", "kitty.conf", "tealdeer-config.toml");
        commandExecutor.assertPathFilesAndDirsCount("/etc/jdvm-config", 5);
    }

    @Test
    void testDockerfileEnv() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-templates/base", "dockerfile-env");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/dockerfile-env", "export DEBCONF_NOWARNINGS=yes");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/dockerfile-env", "export DEBIAN_FRONTEND=noninteractive");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/dockerfile-env", "export DISPLAY=:0");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/dockerfile-env", "export JDVM_USER=dev");
        commandExecutor.assertFileLinesCount("/etc/jdvm-templates/base/dockerfile-env", 4);
    }

    @Test
    void testDockerEnv() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-templates/base", "docker-env");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/docker-env", "export DISPLAY=:0");
        commandExecutor.assertFileContains("/etc/jdvm-templates/base/docker-env", "export HOSTNAME=");
        commandExecutor.assertFileLinesCount("/etc/jdvm-templates/base/docker-env", 2);
    }

    @Test
    void testBaseTemplates() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-templates/base", "dockerfile-env", "docker-env", "dconf", "env",
                "p10k.zsh");
        commandExecutor.assertPathFilesAndDirsCount("/etc/jdvm-templates/base", 5);
    }

    @Test
    void testLocalTemplates() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-templates/local", ".aliases.local", ".bash_aliases.local",
                ".bash_env.local", ".bash_logout.local", ".bashrc.local", ".env.local", ".profile.local", ".zlogout.local",
                ".zprofile.local", ".zshaliases.local", ".zshenv.local", ".zshrc.local");
        commandExecutor.assertPathFilesAndDirsCount("/etc/jdvm-templates/local", 12);
    }

    @Test
    void testUserTemplates() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/etc/jdvm-templates/user", ".aliases", ".bash_aliases", ".bash_env", ".env",
                ".ps1", ".zlogout", ".zprofile", ".zshenv", ".zshrc");
        commandExecutor.assertPathFilesAndDirsCount("/etc/jdvm-templates/user", 9);
    }

}
