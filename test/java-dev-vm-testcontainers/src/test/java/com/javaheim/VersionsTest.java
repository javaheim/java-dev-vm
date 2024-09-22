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

import static com.javaheim.util.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
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
 * Java DEV VM Versions Tests
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
class VersionsTest {

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
    void testJavaDevVmVersion() throws IOException, InterruptedException {
        commandExecutor.assertVersionEquals("image.version",
                "cat \"/etc/versions/" + MAVEN.getProperty("image.name") + ".version\"");
    }

    @Test
    void testUbuntu() throws IOException, InterruptedException {
        commandExecutor.assertVersionStartsWith("ubuntu.version",
                "grep \"VERSION=\" \"/etc/os-release\" | sed \"s/.*=\\\"//;s/ .*//\"");
    }

    @Test
    void testKitty() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/kitty", "bin", "lib");
        commandExecutor.assertPathExists(TestConstants.USER_HOME + "/.config/kitty/kitty.conf");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.config/kitty/kitty.conf", "font_family MesloLGS NF");
        commandExecutor.assertVersionEquals("kitty.version", "kitty --version | sed \"s/kitty //;s/ .*//\"");
        commandExecutor.assertVersionEquals("kitty.version", "kitten --version | sed \"s/kitten //;s/ .*//\"");
    }

    @Test
    void testTmux() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("tmux", "/usr/bin/tmux");
        commandExecutor.assertVersionNotEmpty("tmux -V | sed \"s/tmux //\"");
    }

    @Test
    void testFirefox() throws IOException, InterruptedException {
        String firefoxProfile = commandExecutor.getCommandOutput(
                "ls \"" + TestConstants.USER_HOME + "/.mozilla/firefox\" | grep \"" + TestConstants.IMAGE_USER + "\"");
        commandExecutor.assertPathExists(TestConstants.USER_HOME + "/.mozilla/firefox/" + firefoxProfile + "/user.js");
        commandExecutor.assertExecutablePathEquals("firefox", "/usr/local/bin/firefox");
        commandExecutor.assertVersionEquals("firefox.version", "firefox --version | sed \"s/.* //\"");
    }

    @Test
    void testGit() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("git", "/usr/bin/git");
        commandExecutor.assertPathExists("/etc/bash_completion.d/git-prompt");
        commandExecutor.assertVersionNotEmpty("git version | sed \"s/.*version //\"");
    }

    @Test
    void testGitFilterRepo() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/local/bin/git-filter-repo");
        commandExecutor.assertVersionEquals("git-filter-repo.version", "cat /etc/versions/git-filter-repo.version");
    }

    @Test
    void testGitLFS() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("git-lfs", "/usr/bin/git-lfs");
        commandExecutor.assertVersionNotEmpty("git lfs version | sed \"s/.*\\///;s/ (.*//\"");
    }

    @Test
    void testGitHubCLI() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("gh", "/usr/local/bin/gh");
        commandExecutor.assertPathExists("/etc/bash_completion.d/gh");
        commandExecutor.assertVersionEquals("github-cli.version", "gh --version | grep gh | sed \"s/.*version //;s/ (.*//\"");
    }

    @Test
    void testSdkMan() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/sdkman", "bin", "src", "candidates");
        commandExecutor.assertSymLinkEquals(TestConstants.USER_HOME + "/.sdkman/bin", "/opt/sdkman/bin");
        commandExecutor.assertSymLinkEquals(TestConstants.USER_HOME + "/.sdkman/src", "/opt/sdkman/src");
        commandExecutor.assertNotSymLink(TestConstants.USER_HOME + "/.sdkman/candidates");
        commandExecutor.assertEnvPropertyEquals("SDKMAN_DIR", TestConstants.USER_HOME + "/.sdkman");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.sdkman/etc/config", "sdkman_auto_answer=true",
                "sdkman_auto_env=true", "sdkman_colour_enable=false", "sdkman_curl_connect_timeout=10",
                "sdkman_curl_max_time=120", "sdkman_selfupdate_feature=false");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.sdkman/bin", 1, "\\$(find", "\\$(find -L");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.sdkman/src", 2, "\\$(find", "\\$(find -L");
        commandExecutor.assertVersionNotEmpty("cat " + TestConstants.USER_HOME + "/.sdkman/var/version");
        commandExecutor.assertVersionNotEmpty("sdk version | grep \"script\" | sed \"s/.* //\"");
    }

    @Test
    void testJava() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/java", "current",
                MAVEN.getProperty("jdk-lts.version") + "-" + MAVEN.getProperty("jdk.distribution"),
                MAVEN.getProperty("jdk-sts.version") + "-" + MAVEN.getProperty("jdk.distribution"));
        commandExecutor.assertPathNotExists("/opt/sdkman/candidates/java");
        commandExecutor.assertVersionEquals("jdk-lts.version&jdk.distribution", "readlink " + "/opt/java/current");
        commandExecutor.assertVersionEquals("jdk-lts.version",
                "java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"");
        commandExecutor.assertVersionEquals("jdk-sts.version",
                "/opt/java/" + MAVEN.getProperty("jdk-sts.version") + "-" + MAVEN.getProperty("jdk.distribution") +
                        "/bin/java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"");
    }

    @Test
    void testMaven() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/maven/bin", "mvn");
        commandExecutor.assertPathExistsAndNotContains("/opt/maven/bin", "mvn.cmd");
        commandExecutor.assertPathNotExists("/opt/sdkman/candidates/maven");
        commandExecutor.assertPathExists(TestConstants.USER_HOME + "/.m2/repository");
        commandExecutor.assertVersionEquals("mvn.version",
                "mvn -B -v | grep \"Apache Maven\" | sed \"s/Apache Maven \\([^ ]*\\).*/\\1/\"");
    }

    @Test
    void testSpringBootCLI() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/springboot/bin", "spring");
        commandExecutor.assertPathNotExists("/opt/sdkman/candidates/springboot");
        commandExecutor.assertVersionEquals("spring-boot-cli.version", "spring --version | sed \"s/.*v//\"");
    }

    @Test
    void testAsyncProfiler() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/async-profiler", "bin", "lib");
        commandExecutor.assertVersionEquals("async-profiler.version", "asprof --version | sed \"s/.*profiler //;s/ .*//\"");
        commandExecutor.assertPathExists("/etc/sysctl.d/999-async-profiler.conf");
        commandExecutor.assertCommandOutputEquals("kernel.perf_event_paranoid = 1", "sysctl kernel.perf_event_paranoid");
        commandExecutor.assertCommandOutputEquals("kernel.kptr_restrict = 0", "sysctl kernel.kptr_restrict");
    }

    @Test
    void testKafka() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/kafka", "bin", "config", "libs");
        commandExecutor.assertPathExistsAndNotContains("/opt/kafka/bin", "windows");
        commandExecutor.assertExecutablePathEquals("kafka-topics.sh", "/opt/kafka/bin/kafka-topics.sh");
        commandExecutor.assertVersionEquals("kafka.version",
                "ls \"/opt/kafka/libs\" | grep -m 1 \"kafka-server\" | sed \"s/.*-//;s/.jar//\"");
    }

    @Test
    void testNode() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/node", "bin", "include", "lib");
        commandExecutor.assertExecutablePathEquals("node", "/opt/node/bin/node");
        commandExecutor.assertVersionEquals("node.version", "node --version | sed \"s/v//\"");
    }

    @Test
    void testCorepack() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/node", ".cache");
        commandExecutor.assertSymLinkEquals("/home/dev/.cache/node/corepack", "/opt/node/.cache/corepack");
        commandExecutor.assertExecutablePathEquals("corepack", "/opt/node/bin/corepack");
        commandExecutor.assertVersionNotEmpty("corepack --version");
    }

    @Test
    void testNpm() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("npm", "/opt/node/bin/npm");
        commandExecutor.assertExecutablePathEquals("npx", "/opt/node/bin/npx");
        commandExecutor.assertPathExists("/etc/bash_completion.d/npm");
        commandExecutor.assertVersionNotEmpty("npm --version");
        commandExecutor.assertVersionNotEmpty("npx --version");
    }

    @Test
    void testPnpm() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("pnpm", "/opt/node/bin/pnpm");
        commandExecutor.assertExecutablePathEquals("pnpx", "/opt/node/bin/pnpx");
        commandExecutor.assertPathExists("/etc/bash_completion.d/pnpm");
        commandExecutor.assertVersionEquals("pnpm.version", "pnpm --version");
    }

    @Test
    void testYarn() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("yarn", "/opt/node/bin/yarn");
        commandExecutor.assertExecutablePathEquals("yarnpkg", "/opt/node/bin/yarnpkg");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.yarnrc.yml", "enableTelemetry: false");
        commandExecutor.assertVersionEquals("yarn.version", "yarn --version");
        commandExecutor.assertVersionEquals("yarn.version", "yarnpkg --version");
    }

    @Test
    void testGulpCLI() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("gulp", "/opt/node/bin/gulp");
        commandExecutor.assertVersionEquals("gulp-cli.version", "gulp --version | grep \"CLI\" | sed \"s/.*: //\"");
    }

    @Test
    void testPython() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("python3", "/usr/bin/python3");
        commandExecutor.assertVersionNotEmpty("python3 --version | sed \"s/.* //\"");
    }

    @Test
    void testUv() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("uv", "/usr/local/bin/uv");
        commandExecutor.assertExecutablePathEquals("uvx", "/usr/local/bin/uvx");
        commandExecutor.assertVersionNotEmpty("uv --version | sed \"s/.* //\"");
    }

    @Test
    void testGo() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/go", "bin");
        commandExecutor.assertEnvPropertyEquals("GOPATH", TestConstants.USER_HOME + "/.go");
        commandExecutor.assertFileContains(TestConstants.USER_HOME + "/.config/go/telemetry/mode", "off");
        commandExecutor.assertExecutablePathEquals("go", "/opt/go/bin/go");
        commandExecutor.assertVersionEquals("go.version", "go version | sed \"s/.* go//;s/ .*//\"");
    }

    @Test
    void testContainerd() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/bin/containerd");
        commandExecutor.assertExecutablePathEquals("containerd", "/usr/bin/containerd");
        commandExecutor.assertVersionEquals("containerd.io.version",
                "containerd --version | sed -e \"s/.*io //\" -e \"s/ .*//\"");
    }

    @Test
    void testDocker() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/bin/docker");
        commandExecutor.assertExecutablePathEquals("docker", "/usr/bin/docker");
        commandExecutor.assertPathExists("/etc/bash_completion.d/docker");
        commandExecutor.assertVersionEquals("docker.version", "docker version --format \"{{.Client.Version}}\"");
        commandExecutor.assertVersionEquals("docker.version", "docker version --format \"{{.Server.Version}}\"");
        commandExecutor.assertCommandOutputEquals("overlay2", "docker system info --format \"{{.Driver}}\"");
    }

    @Test
    void testDockerBuildx() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/libexec/docker/cli-plugins/docker-buildx");
        commandExecutor.assertVersionEquals("docker-buildx.version", "docker buildx version | sed \"s/.* v//;s/ .*//\"");
    }

    @Test
    void testDockerCompose() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/libexec/docker/cli-plugins/docker-compose");
        commandExecutor.assertVersionEquals("docker-compose.version", "docker compose version --short");
    }

    @Test
    void testDockerScout() throws IOException, InterruptedException {
        commandExecutor.assertPathExists("/usr/local/lib/docker/cli-plugins/docker-scout");
        commandExecutor.assertVersionEquals("docker-scout.version",
                "docker scout version | grep version | sed \"s/.* v//;s/ (.*//\"");
    }

    @Test
    void testDive() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("dive", "/usr/local/bin/dive");
        commandExecutor.assertVersionEquals("dive.version", "dive --version | sed \"s/.* //\"");
    }

    @Test
    void testHadolint() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("hadolint", "/usr/local/bin/hadolint");
        commandExecutor.assertVersionEquals("hadolint.version", "hadolint --version | sed \"s/.* //\"");
    }

    @Test
    void testSlim() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("mint", "/usr/local/bin/mint");
        commandExecutor.assertExecutablePathEquals("mint-sensor", "/usr/local/bin/mint-sensor");
        commandExecutor.assertExecutablePathEquals("slim", "/usr/local/bin/slim");
        commandExecutor.assertExecutablePathEquals("slim-sensor", "/usr/local/bin/slim-sensor");
        commandExecutor.assertVersionNotEmpty("slim --version | sed \"s/.*version [^|]*|[^|]*|.\\.\\([^|]*\\)|.*/\\1/\"");
    }

    @Test
    void testKubectl() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("kubectl", "/usr/local/bin/kubectl");
        commandExecutor.assertPathExists("/etc/bash_completion.d/kubectl");
        commandExecutor.assertVersionEquals("kubectl.version",
                "kubectl version --client | grep \"Client Version:\" | sed \"s/.*v//\"");
    }

    @Test
    void testKubectlKrew() throws IOException, InterruptedException {
        commandExecutor.assertPathExistsAndContains("/opt/krew", "bin", "index", "receipts", "store");
        commandExecutor.assertPathExistsAndContains("/opt/krew/receipts", "krew.yaml");
        commandExecutor.assertPathExists("/opt/krew/store/krew/v" + MAVEN.getProperty("kubectl-krew.version") + "/krew");
        commandExecutor.assertSymLinkEquals("/opt/krew/bin/kubectl-krew",
                "/opt/krew/store/krew/v" + MAVEN.getProperty("kubectl-krew.version") + "/krew");
        commandExecutor.assertPathExistsAndContains("/opt/krew/index/default", "plugins", "plugins.md");

        commandExecutor.assertPathExistsAndContains(TestConstants.USER_HOME + "/.krew", "bin", "index", "receipts", "store");
        commandExecutor.assertPathExistsAndNotContains(TestConstants.USER_HOME + "/.krew/bin", "kubectl-krew");
        commandExecutor.assertPathExistsAndNotContains(TestConstants.USER_HOME + "/.krew/receipts", "krew.yaml");
        commandExecutor.assertPathExistsAndNotContains(TestConstants.USER_HOME + "/.krew/store", "krew");
        commandExecutor.assertPathExistsAndContains(TestConstants.USER_HOME + "/.krew/index/default", "plugins", "plugins.md");

        commandExecutor.assertVersionEquals("kubectl-krew.version", "kubectl krew version | grep \"GitTag\" | sed \"s/.*v//\"");
    }

    @Test
    void testK3d() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("k3d", "/usr/local/bin/k3d");
        commandExecutor.assertPathExists("/etc/bash_completion.d/k3d");
        commandExecutor.assertVersionEquals("k3d.version", "k3d version | grep \"k3d\" | sed \"s/.*v//\"");
        commandExecutor.assertVersionNotEmpty("k3d version | grep \"k3s\" | sed \"s/.*v//;s/ (.*//\"");
    }

    @Test
    void testHelm() throws IOException, InterruptedException {
        commandExecutor.assertExecutablePathEquals("helm", "/usr/local/bin/helm");
        commandExecutor.assertPathExists("/etc/bash_completion.d/helm");
        commandExecutor.assertVersionEquals("helm.version",
                "helm version --template=\"Version: {{.Version}}\" | sed \"s/.*v//\"");
    }

    @Test
    void versionsOutput() throws IOException, InterruptedException {
        commandExecutor.assertCommandOutputEquals("Versions saved to: /tmp/versions.md", "jdvm-versions -o");
        JAVA_DEV_VM.copyFileFromContainer("/tmp/versions.md", "target/versions.md");
    }

}
