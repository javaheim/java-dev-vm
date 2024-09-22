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

package com.javaheim.jdvm.testcontainers;

import static com.javaheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.javaheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static com.javaheim.jdvm.testcontainers.property.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.javaheim.jdvm.testcontainers.setup.JavaDevVmContainer;

/**
 * Java DEV VM Versions Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class VersionsTest extends JavaDevVmContainer {

    @Test
    void runningAndHealthy() {
        assertThat(getJavaDevVm().isPrivilegedMode()).isTrue();
        assertThat(getJavaDevVm().isRunning()).isTrue();
        assertThat(getJavaDevVm().isHealthy()).isTrue();
    }

    @Test
    void testJavaDevVmVersion() throws IOException, InterruptedException {
        String javaDevVmVersion = getJavaDevVmExecutor().cat(
                "/etc/versions/%s.version".formatted(MAVEN.getProperty("image.name"))).exec();
        assertThat(javaDevVmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("image.version"));
    }

    @Test
    void testUbuntu() throws IOException, InterruptedException {
        String ubuntuVersion = getJavaDevVmExecutor().run("grep \"VERSION=\" \"/etc/os-release\" | sed \"s/.*=\\\"//;s/ .*//\"")
                .exec();
        assertThat(ubuntuVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("ubuntu.version"));
    }

    @Test
    void testKitty() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "lib"
        };
        String kittyDir = getJavaDevVmExecutor().ls("/opt/kitty").exec();
        assertThat(kittyDir).contains(dirs).hasLineCount(dirs.length);

        String kittyConf = getJavaDevVmExecutor().cat(USER_HOME + "/.config/kitty/kitty.conf").exec();
        assertThat(kittyConf).contains("font_family MesloLGS NF");

        String kittyVersion = getJavaDevVmExecutor().run("kitty --version | sed \"s/kitty //;s/ .*//\"").exec();
        assertThat(kittyVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kitty.version"));

        String kittenVersion = getJavaDevVmExecutor().run("kitten --version | sed \"s/kitten //;s/ .*//\"").exec();
        assertThat(kittenVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kitty.version"));
    }

    @Test
    void testTmux() throws IOException, InterruptedException {
        String tmuxPath = getJavaDevVmExecutor().executablePath("tmux").exec();
        assertThat(tmuxPath).isEqualTo("/usr/bin/tmux");

        String tmuxVersion = getJavaDevVmExecutor().run("tmux -V | sed \"s/tmux //\"").exec();
        assertThat(tmuxVersion).isEqualTo("3.5a");
    }

    @Test
    void testFirefox() throws IOException, InterruptedException {
        String firefoxPath = getJavaDevVmExecutor().executablePath("firefox").exec();
        assertThat(firefoxPath).isEqualTo("/usr/local/bin/firefox");

        String firefoxProfile = getJavaDevVmExecutor().run(
                "ls \"" + USER_HOME + "/.mozilla/firefox\" | grep \"" + IMAGE_USER + "\"").exec();
        String firefoxProfileDir = getJavaDevVmExecutor().ls(USER_HOME + "/.mozilla/firefox/" + firefoxProfile).exec();
        assertThat(firefoxProfileDir).contains("user.js");

        String firefoxVersion = getJavaDevVmExecutor().run("firefox --version | sed \"s/.* //\"").exec();
        assertThat(firefoxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("firefox.version"));
    }

    @Test
    void testGit() throws IOException, InterruptedException {
        String gitPath = getJavaDevVmExecutor().executablePath("git").exec();
        assertThat(gitPath).isEqualTo("/usr/bin/git");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("git-prompt");

        String gitVersion = getJavaDevVmExecutor().run("git version | sed \"s/.*version //\"").exec();
        assertThat(gitVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git.version"));
    }

    @Test
    void testGitFilterRepo() throws IOException, InterruptedException {
        String gitFilterRepoPath = getJavaDevVmExecutor().executablePath("git-filter-repo").exec();
        assertThat(gitFilterRepoPath).isEqualTo("/usr/local/bin/git-filter-repo");

        String gitFilterRepoVersion = getJavaDevVmExecutor().cat("/etc/versions/git-filter-repo.version").exec();
        assertThat(gitFilterRepoVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git-filter-repo.version"));
    }

    @Test
    void testGitLFS() throws IOException, InterruptedException {
        String gitLfsPath = getJavaDevVmExecutor().executablePath("git-lfs").exec();
        assertThat(gitLfsPath).isEqualTo("/usr/local/bin/git-lfs");

        String gitLfsVersion = getJavaDevVmExecutor().run("git lfs version | sed \"s/.*\\///;s/ (.*//\"").exec();
        assertThat(gitLfsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git-lfs.version"));
    }

    @Test
    void testGitHubCLI() throws IOException, InterruptedException {
        String ghPath = getJavaDevVmExecutor().executablePath("gh").exec();
        assertThat(ghPath).isEqualTo("/usr/local/bin/gh");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("gh");

        String ghVersion = getJavaDevVmExecutor().run("gh --version | grep gh | sed \"s/.*version //;s/ (.*//\"").exec();
        assertThat(ghVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("github-cli.version"));
    }

    @Test
    void testSdkMan() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "candidates", "contrib", "etc", "ext", "libexec", "src", "tmp", "var"
        };
        String sdkManDir = getJavaDevVmExecutor().ls("/opt/sdkman").exec();
        assertThat(sdkManDir).contains(dirs).hasLineCount(dirs.length);

        String homeSdkManDir = getJavaDevVmExecutor().ls(USER_HOME + "/.sdkman").exec();
        assertThat(homeSdkManDir).contains(dirs).hasLineCount(dirs.length);

        for (CharSequence dir : dirs) {
            String homeSdkManDirSymlink = "%s/.sdkman/%s".formatted(USER_HOME, dir);
            if ("candidates".contentEquals(dir)) {
                getJavaDevVmExecutor().symlinkPath(homeSdkManDirSymlink).execShouldFail();
                String homeSdkManDirPath = getJavaDevVmExecutor().ls(homeSdkManDirSymlink).exec();
                assertThat(homeSdkManDirPath).isEmpty();
            } else {
                String homeSdkManDirPath = getJavaDevVmExecutor().symlinkPath(homeSdkManDirSymlink).exec();
                assertThat(homeSdkManDirPath).isEqualTo("/opt/sdkman/" + dir);
            }
        }

        String sdkManDirEnv = getJavaDevVmExecutor().printenv("SDKMAN_DIR").exec();
        assertThat(sdkManDirEnv).isEqualTo(USER_HOME + "/.sdkman");

        String sdkManConfig = getJavaDevVmExecutor().cat(USER_HOME + "/.sdkman/etc/config").exec();
        assertThat(sdkManConfig).contains("sdkman_auto_answer=true");
        assertThat(sdkManConfig).contains("sdkman_auto_env=true");
        assertThat(sdkManConfig).contains("sdkman_colour_enable=false");
        assertThat(sdkManConfig).contains("sdkman_curl_connect_timeout=10");
        assertThat(sdkManConfig).contains("sdkman_curl_max_time=120");
        assertThat(sdkManConfig).contains("sdkman_selfupdate_feature=false");

        Set<String> executables = Set.of(USER_HOME + "/.sdkman/bin/sdkman-init.sh", USER_HOME + "/.sdkman/src/sdkman-list.sh",
                USER_HOME + "/.sdkman/src/sdkman-upgrade.sh");
        for (String executable : executables) {
            String sdkManExecutable = getJavaDevVmExecutor().cat(executable).exec();
            assertThat(sdkManExecutable).containsOnlyOnce("$(find").containsOnlyOnce("$(find -L");
        }

        String sdkManVersion = getJavaDevVmExecutor().cat(USER_HOME + "/.sdkman/var/version").exec();
        assertThat(sdkManVersion).startsWith("5.");

        String sdkVersion = getJavaDevVmExecutor().run("sdk version | grep \"script\" | sed \"s/.* //\"").exec();
        assertThat(sdkVersion).startsWith("5.");
    }

    @Test
    void testJava() throws IOException, InterruptedException {
        CharSequence jdkLtsVersionWithDistribution =
                MAVEN.getProperty("jdk-lts.version") + "-" + MAVEN.getProperty("jdk.distribution");
        CharSequence jdkStsVersionWithDistribution =
                MAVEN.getProperty("jdk-sts.version") + "-" + MAVEN.getProperty("jdk.distribution");
        CharSequence[] dirs = new CharSequence[]{
                "current", jdkLtsVersionWithDistribution, jdkStsVersionWithDistribution
        };
        String javaDir = getJavaDevVmExecutor().ls("/opt/java").exec();
        assertThat(javaDir).contains(dirs).hasLineCount(dirs.length);

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("java");

        String javaCurrentPath = getJavaDevVmExecutor().symlinkPath("/opt/java/current").exec();
        assertThat(javaCurrentPath).isEqualTo(jdkLtsVersionWithDistribution);

        String jdkLtsVersion = getJavaDevVmExecutor().run(
                "java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"").exec();
        assertThat(jdkLtsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jdk-lts.version"));

        String jdkStsVersion = getJavaDevVmExecutor().run("/opt/java/" + jdkStsVersionWithDistribution +
                "/bin/java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"").exec();
        assertThat(jdkStsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jdk-sts.version"));
    }

    @Test
    void testJBang() throws IOException, InterruptedException {
        String jbangPath = getJavaDevVmExecutor().executablePath("jbang").exec();
        assertThat(jbangPath).isEqualTo("/opt/jbang/bin/jbang");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jbang");

        String jbangVersion = getJavaDevVmExecutor().run("jbang version").exec();
        assertThat(jbangVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jbang.version"));
    }

    @Test
    void testJMeter() throws IOException, InterruptedException {
        String jmeterPath = getJavaDevVmExecutor().executablePath("jmeter").exec();
        assertThat(jmeterPath).isEqualTo("/opt/jmeter/bin/jmeter");

        String jmeterDir = getJavaDevVmExecutor().ls(USER_HOME + "/.java/.userPrefs/org/apache/jmeter").exec();
        assertThat(jmeterDir).contains("prefs.xml");

        String homeDir = getJavaDevVmExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).doesNotContain("jmeter.log");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jmeter");

        String jmeterVersion = getJavaDevVmExecutor().run(
                        "jmeter -n --version -j /dev/null 2>/dev/null | grep -m 1 -E \"[0-9]+\" | sed \"s/.* \\([0-9]\\+\\)/\\1/\"")
                .exec();
        assertThat(jmeterVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jmeter.version"));
    }

    @Test
    void testJReleaser() throws IOException, InterruptedException {
        String jreleaserPath = getJavaDevVmExecutor().executablePath("jreleaser").exec();
        assertThat(jreleaserPath).isEqualTo("/opt/jreleaser/bin/jreleaser");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jreleaser");

        String jreleaserVersion = getJavaDevVmExecutor().run("jreleaser --version | grep jreleaser | sed \"s/.* //\"").exec();
        assertThat(jreleaserVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jreleaser.version"));
    }

    @Test
    void testGradle() throws IOException, InterruptedException {
        String gradlePath = getJavaDevVmExecutor().executablePath("gradle").exec();
        assertThat(gradlePath).isEqualTo("/opt/gradle/bin/gradle");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("gradle");

        String gradleVersion = getJavaDevVmExecutor().run(
                "gradle --version | grep -m 1 \"Gradle \" | sed -e \"s/.* //\" -e \"s/\\!//\"").exec();
        assertThat(gradleVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("gradle.version"));
    }

    @Test
    void testMaven() throws IOException, InterruptedException {
        String mvnPath = getJavaDevVmExecutor().executablePath("mvn").exec();
        assertThat(mvnPath).isEqualTo("/opt/maven/bin/mvn");

        String mavenBinDir = getJavaDevVmExecutor().ls("/opt/maven/bin").exec();
        assertThat(mavenBinDir).doesNotContain("mvn.cmd");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("maven");

        String m2Dir = getJavaDevVmExecutor().ls(USER_HOME + "/.m2").exec();
        assertThat(m2Dir).contains("repository");

        String mvnVersion = getJavaDevVmExecutor().run(
                "mvn -B -v | grep \"Apache Maven\" | sed \"s/Apache Maven \\([^ ]*\\).*/\\1/\"").exec();
        assertThat(mvnVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("mvn.version"));
    }

    @Test
    void testSpringBootCLI() throws IOException, InterruptedException {
        String springPath = getJavaDevVmExecutor().executablePath("spring").exec();
        assertThat(springPath).isEqualTo("/opt/springboot/bin/spring");

        String sdkmanCandidatesDir = getJavaDevVmExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("springboot");

        String springVersion = getJavaDevVmExecutor().run("spring --version | sed \"s/.*v//\"").exec();
        assertThat(springVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("spring-boot-cli.version"));
    }

    @Test
    void testAsyncProfiler() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "lib"
        };
        String asyncProfilerDir = getJavaDevVmExecutor().ls("/opt/async-profiler").exec();
        assertThat(asyncProfilerDir).contains(dirs).hasLineCount(dirs.length);

        String asprofPath = getJavaDevVmExecutor().executablePath("asprof").exec();
        assertThat(asprofPath).isEqualTo("/opt/async-profiler/bin/asprof");

        String jfrconvPath = getJavaDevVmExecutor().executablePath("jfrconv").exec();
        assertThat(jfrconvPath).isEqualTo("/opt/async-profiler/bin/jfrconv");

        String sysctlDir = getJavaDevVmExecutor().ls("/etc/sysctl.d").exec();
        assertThat(sysctlDir).contains("999-async-profiler.conf");

        String kernelPerfEventParanoid = getJavaDevVmExecutor().run("sysctl kernel.perf_event_paranoid").exec();
        assertThat(kernelPerfEventParanoid).isEqualTo("kernel.perf_event_paranoid = 1");

        String kernelKptrRestrict = getJavaDevVmExecutor().run("sysctl kernel.kptr_restrict").exec();
        assertThat(kernelKptrRestrict).isEqualTo("kernel.kptr_restrict = 0");

        String asyncProfilerVersion = getJavaDevVmExecutor().run("asprof --version | sed \"s/.*profiler //;s/ .*//\"").exec();
        assertThat(asyncProfilerVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("async-profiler.version"));
    }

    @Test
    void testKafka() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "config", "libs"
        };
        String kafkaDir = getJavaDevVmExecutor().ls("/opt/kafka").exec();
        assertThat(kafkaDir).contains(dirs).hasLineCount(dirs.length);

        String kafkaBinDir = getJavaDevVmExecutor().ls("/opt/kafka/bin").exec();
        assertThat(kafkaBinDir).doesNotContain("windows");

        String kafkaTopicsPath = getJavaDevVmExecutor().executablePath("kafka-topics.sh").exec();
        assertThat(kafkaTopicsPath).isEqualTo("/opt/kafka/bin/kafka-topics.sh");

        String kafkaVersion = getJavaDevVmExecutor().run(
                "ls \"/opt/kafka/libs\" | grep -m 1 \"kafka-server\" | sed \"s/.*-//;s/.jar//\"").exec();
        assertThat(kafkaVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kafka.version"));
    }

    @Test
    void testNode() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "include", "lib"
        };
        String nodeDir = getJavaDevVmExecutor().ls("/opt/node").exec();
        assertThat(nodeDir).contains(dirs).hasLineCount(dirs.length);

        String nodePath = getJavaDevVmExecutor().executablePath("node").exec();
        assertThat(nodePath).isEqualTo("/opt/node/bin/node");

        String nodeVersion = getJavaDevVmExecutor().run("node --version | sed \"s/v//\"").exec();
        assertThat(nodeVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("node.version"));
    }

    @Test
    void testNpm() throws IOException, InterruptedException {
        String npmPath = getJavaDevVmExecutor().executablePath("npm").exec();
        assertThat(npmPath).isEqualTo("/opt/node/bin/npm");

        String npxPath = getJavaDevVmExecutor().executablePath("npx").exec();
        assertThat(npxPath).isEqualTo("/opt/node/bin/npx");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("npm");

        String npmVersion = getJavaDevVmExecutor().run("npm --version").exec();
        assertThat(npmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("npm.version"));

        String npxVersion = getJavaDevVmExecutor().run("npx --version").exec();
        assertThat(npxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("npm.version"));
    }

    @Test
    void testPnpm() throws IOException, InterruptedException {
        String pnpmPath = getJavaDevVmExecutor().executablePath("pnpm").exec();
        assertThat(pnpmPath).isEqualTo("/opt/node/bin/pnpm");

        String pnpxPath = getJavaDevVmExecutor().executablePath("pnpx").exec();
        assertThat(pnpxPath).isEqualTo("/opt/node/bin/pnpx");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("pnpm");

        String pnpmVersion = getJavaDevVmExecutor().run("pnpm --version").exec();
        assertThat(pnpmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("pnpm.version"));
    }

    @Test
    void testYarn() throws IOException, InterruptedException {
        String yarnPath = getJavaDevVmExecutor().executablePath("yarn").exec();
        assertThat(yarnPath).isEqualTo("/opt/node/bin/yarn");

        String yarnpkgPath = getJavaDevVmExecutor().executablePath("yarnpkg").exec();
        assertThat(yarnpkgPath).isEqualTo("/opt/node/bin/yarnpkg");

        String yarnRc = getJavaDevVmExecutor().cat(USER_HOME + "/.yarnrc.yml").exec();
        assertThat(yarnRc).contains("enableTelemetry: false");

        String yarnVersion = getJavaDevVmExecutor().run("yarn --version").exec();
        assertThat(yarnVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("yarn.version"));

        String yarnpkgVersion = getJavaDevVmExecutor().run("yarnpkg --version").exec();
        assertThat(yarnpkgVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("yarn.version"));
    }

    @Test
    void testGulpCLI() throws IOException, InterruptedException {
        String gulpPath = getJavaDevVmExecutor().executablePath("gulp").exec();
        assertThat(gulpPath).isEqualTo("/opt/node/bin/gulp");

        String gulpVersion = getJavaDevVmExecutor().run("gulp --version | grep \"CLI\" | sed \"s/.*: //\"").exec();
        assertThat(gulpVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("gulp-cli.version"));
    }

    @Test
    void testPython() throws IOException, InterruptedException {
        String pythonPath = getJavaDevVmExecutor().executablePath("python3").exec();
        assertThat(pythonPath).isEqualTo("/usr/bin/python3");

        String pythonVersion = getJavaDevVmExecutor().run("python3 --version | sed \"s/.* //\"").exec();
        assertThat(pythonVersion).isEqualTo("3.13.3");
    }

    @Test
    void testUv() throws IOException, InterruptedException {
        String uvPath = getJavaDevVmExecutor().executablePath("uv").exec();
        assertThat(uvPath).isEqualTo("/usr/local/bin/uv");

        String uvxPath = getJavaDevVmExecutor().executablePath("uvx").exec();
        assertThat(uvxPath).isEqualTo("/usr/local/bin/uvx");

        String uvVersion = getJavaDevVmExecutor().run("uv --version | sed \"s/.* //\"").exec();
        assertThat(uvVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("uv.version"));
    }

    @Test
    void testGo() throws IOException, InterruptedException {
        String goPath = getJavaDevVmExecutor().executablePath("go").exec();
        assertThat(goPath).isEqualTo("/opt/go/bin/go");

        String goPathEnv = getJavaDevVmExecutor().printenv("GOPATH").exec();
        assertThat(goPathEnv).isEqualTo(USER_HOME + "/.go");

        String goTelemetryMode = getJavaDevVmExecutor().cat(USER_HOME + "/.config/go/telemetry/mode").exec();
        assertThat(goTelemetryMode).isEqualTo("off");

        String goVersion = getJavaDevVmExecutor().run("go version | sed \"s/.* go//;s/ .*//\"").exec();
        assertThat(goVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("go.version"));
    }

    @Test
    void testContainerd() throws IOException, InterruptedException {
        String containerdPath = getJavaDevVmExecutor().executablePath("containerd").exec();
        assertThat(containerdPath).isEqualTo("/usr/bin/containerd");

        String containerdVersion = getJavaDevVmExecutor().run("containerd --version | sed -e \"s/.*io //\" -e \"s/ .*//\"")
                .exec();
        assertThat(containerdVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("containerd.io.version"));
    }

    @Test
    void testDocker() throws IOException, InterruptedException {
        String dockerPath = getJavaDevVmExecutor().executablePath("docker").exec();
        assertThat(dockerPath).isEqualTo("/usr/bin/docker");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("docker");

        String dockerClientVersion = getJavaDevVmExecutor().run("docker version --format \"{{.Client.Version}}\"").exec();
        assertThat(dockerClientVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker.version"));

        String dockerServerVersion = getJavaDevVmExecutor().run("docker version --format \"{{.Server.Version}}\"").exec();
        assertThat(dockerServerVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker.version"));

        String dockerDriver = getJavaDevVmExecutor().run("docker system info --format \"{{.Driver}}\"").exec();
        assertThat(dockerDriver).isEqualTo("overlay2");
    }

    @Test
    void testDockerBuildx() throws IOException, InterruptedException {
        String dockerCliPluginsDir = getJavaDevVmExecutor().ls("/usr/libexec/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-buildx");

        String dockerBuildxVersion = getJavaDevVmExecutor().run("docker buildx version | sed \"s/.* v//;s/ .*//\"").exec();
        assertThat(dockerBuildxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-buildx.version"));
    }

    @Test
    void testDockerCompose() throws IOException, InterruptedException {
        String dockerCliPluginsDir = getJavaDevVmExecutor().ls("/usr/libexec/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-compose");

        String dockerComposeVersion = getJavaDevVmExecutor().run("docker compose version --short").exec();
        assertThat(dockerComposeVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-compose.version"));
    }

    @Test
    void testDockerScout() throws IOException, InterruptedException {
        String dockerCliPluginsDir = getJavaDevVmExecutor().ls("/usr/local/lib/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-scout");

        String dockerScoutVersion = getJavaDevVmExecutor().run("docker scout version | grep version | sed \"s/.* v//;s/ (.*//\"")
                .exec();
        assertThat(dockerScoutVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-scout.version"));
    }

    @Test
    void testDive() throws IOException, InterruptedException {
        String divePath = getJavaDevVmExecutor().executablePath("dive").exec();
        assertThat(divePath).isEqualTo("/usr/local/bin/dive");

        String diveVersion = getJavaDevVmExecutor().run("dive --version | sed \"s/.* //\"").exec();
        assertThat(diveVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("dive.version"));
    }

    @Test
    void testHadolint() throws IOException, InterruptedException {
        String hadolintPath = getJavaDevVmExecutor().executablePath("hadolint").exec();
        assertThat(hadolintPath).isEqualTo("/usr/local/bin/hadolint");

        String hadolintVersion = getJavaDevVmExecutor().run("hadolint --version | sed \"s/.* //\"").exec();
        assertThat(hadolintVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("hadolint.version"));
    }

    @Test
    void testSlim() throws IOException, InterruptedException {
        String mintPath = getJavaDevVmExecutor().executablePath("mint").exec();
        assertThat(mintPath).isEqualTo("/usr/local/bin/mint");

        String mintSensorPath = getJavaDevVmExecutor().executablePath("mint-sensor").exec();
        assertThat(mintSensorPath).isEqualTo("/usr/local/bin/mint-sensor");

        String slimPath = getJavaDevVmExecutor().executablePath("slim").exec();
        assertThat(slimPath).isEqualTo("/usr/local/bin/slim");

        String slimSensorPath = getJavaDevVmExecutor().executablePath("slim-sensor").exec();
        assertThat(slimSensorPath).isEqualTo("/usr/local/bin/slim-sensor");

        String slimVersion = getJavaDevVmExecutor().run(
                "slim --version | sed \"s/.*version [^|]*|[^|]*|.\\.\\([^|]*\\)|.*/\\1/\"").exec();
        assertThat(slimVersion).isEqualTo("1.42.2");
    }

    @Test
    void testKubectl() throws IOException, InterruptedException {
        String kubectlPath = getJavaDevVmExecutor().executablePath("kubectl").exec();
        assertThat(kubectlPath).isEqualTo("/usr/local/bin/kubectl");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("kubectl");

        String kubectlVersion = getJavaDevVmExecutor().run(
                "kubectl version --client | grep \"Client Version:\" | sed \"s/.*v//\"").exec();
        assertThat(kubectlVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kubectl.version"));
    }

    @Test
    void testKubectlKrew() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "index", "receipts", "store"
        };
        String krewDir = getJavaDevVmExecutor().ls("/opt/krew").exec();
        assertThat(krewDir).contains(dirs).hasLineCount(dirs.length);

        String homeKrewDir = getJavaDevVmExecutor().ls(USER_HOME + "/.krew").exec();
        assertThat(homeKrewDir).contains(dirs).hasLineCount(dirs.length);

        for (CharSequence dir : dirs) {
            String homeKrewDirSymlink = "%s/.krew/%s".formatted(USER_HOME, dir);
            if ("index".contentEquals(dir)) {
                String homeKrewDirPath = getJavaDevVmExecutor().symlinkPath(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).isEqualTo("/opt/krew/%s".formatted(dir));
            } else {
                getJavaDevVmExecutor().symlinkPath(homeKrewDirSymlink).execShouldFail();
                String homeKrewDirPath = getJavaDevVmExecutor().ls(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).isEmpty();
            }
        }

        String krewPath = getJavaDevVmExecutor().executablePath("kubectl-krew").exec();
        assertThat(krewPath).isEqualTo("/opt/krew/bin/kubectl-krew");

        String kubectlKrewPath = getJavaDevVmExecutor().symlinkPath("/opt/krew/bin/kubectl-krew").exec();
        assertThat(kubectlKrewPath).isEqualTo(
                "/opt/krew/store/krew/v%s/krew".formatted(MAVEN.getProperty("kubectl-krew.version")));

        String indexDefaultDir = getJavaDevVmExecutor().ls("/opt/krew/index/default").exec();
        assertThat(indexDefaultDir).contains("plugins", "plugins.md");

        String krewYaml = getJavaDevVmExecutor().cat("/opt/krew/receipts/krew.yaml").exec();
        assertThat(krewYaml).contains("krew is now installed");

        String krewStoreDir = getJavaDevVmExecutor().ls(
                "/opt/krew/store/krew/v%s".formatted(MAVEN.getProperty("kubectl-krew.version"))).exec();
        assertThat(krewStoreDir).contains("LICENSE", "krew");

        String krewVersion = getJavaDevVmExecutor().run("kubectl krew version | grep \"GitTag\" | sed \"s/.*v//\"").exec();
        assertThat(krewVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kubectl-krew.version"));
    }

    @Test
    void testK3d() throws IOException, InterruptedException {
        String k3dPath = getJavaDevVmExecutor().executablePath("k3d").exec();
        assertThat(k3dPath).isEqualTo("/usr/local/bin/k3d");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("k3d");

        String k3dVersion = getJavaDevVmExecutor().run("k3d version | grep \"k3d\" | sed \"s/.*v//\"").exec();
        assertThat(k3dVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("k3d.version"));

        String k3sVersion = getJavaDevVmExecutor().run("k3d version | grep \"k3s\" | sed \"s/.*v//;s/ (.*//\"").exec();
        assertThat(k3sVersion).isEqualTo("1.31.5-k3s1");
    }

    @Test
    void testHelm() throws IOException, InterruptedException {
        String helmPath = getJavaDevVmExecutor().executablePath("helm").exec();
        assertThat(helmPath).isEqualTo("/usr/local/bin/helm");

        String bashCompletionDir = getJavaDevVmExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("helm");

        String helmVersion = getJavaDevVmExecutor().run("helm version --template=\"Version: {{.Version}}\" | sed \"s/.*v//\"")
                .exec();
        assertThat(helmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("helm.version"));
    }

    @Test
    void versionsOutput() throws IOException, InterruptedException {
        String jdvmVersions = getJavaDevVmExecutor().run("jdvm-versions -o").exec();
        assertThat(jdvmVersions).contains("Versions saved to: /tmp/versions.md");

        String versions = getJavaDevVmExecutor().cat("/tmp/versions.md").exec();
        assertThat(versions).isNotEmpty().doesNotContain("****");

        String homeDir = getJavaDevVmExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).doesNotContain("jmeter.log");

        getJavaDevVm().copyFileFromContainer("/tmp/versions.md", "target/versions.md");
    }

}
