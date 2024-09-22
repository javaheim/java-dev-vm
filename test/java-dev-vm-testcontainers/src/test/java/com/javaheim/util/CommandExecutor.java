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

package com.javaheim.util;

import static com.javaheim.util.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

/**
 * Command Executor is responsible for executing commands in a Docker Container
 * and asserting the results of those commands.
 *
 * <p>It leverages Testcontainers' {@link GenericContainer} to execute commands inside a running container.</p>
 */
public record CommandExecutor(GenericContainer<?> container) {

    public void assertCommandOutputEquals(String expected, String command) throws IOException, InterruptedException {
        assertThat(getCommandOutput(command)).isEqualTo(expected);
    }

    public void assertCommandOutputContains(String expected, String command) throws IOException, InterruptedException {
        assertThat(getCommandOutput(command)).contains(expected);
    }

    public String getCommandOutput(String command) throws IOException, InterruptedException {
        GenericContainer.ExecResult executedCommand = container().execInContainer(
                commandAsImageUser("bash", "-i", "-c", command));
        assertThat(executedCommand.getExitCode()).as("\n%s%s", executedCommand.getStdout(), executedCommand.getStderr()).isZero();
        return executedCommand.getStdout().trim();
    }

    public void assertPathExistsAndContains(String path, String... expected) throws IOException, InterruptedException {
        GenericContainer.ExecResult ls = container().execInContainer(commandAsImageUser("ls", "-a", path));
        assertThat(ls.getExitCode()).as("\n%s%s", ls.getStdout(), ls.getStderr()).isZero();
        assertThat(ls.getStdout()).contains(expected);
    }

    public void assertPathExistsAndNotContains(String path, String... expected) throws IOException, InterruptedException {
        GenericContainer.ExecResult ls = container().execInContainer(commandAsImageUser("ls", "-a", path));
        assertThat(ls.getExitCode()).as("\n%s%s", ls.getStdout(), ls.getStderr()).isZero();
        assertThat(ls.getStdout()).doesNotContain(expected);
    }

    public void assertPathExists(String path) throws IOException, InterruptedException {
        GenericContainer.ExecResult ls = container().execInContainer(commandAsImageUser("ls", "-a", path));
        assertThat(ls.getExitCode()).as("\n%s%s", ls.getStdout(), ls.getStderr()).isZero();
    }

    public void assertPathNotExists(String path) throws IOException, InterruptedException {
        GenericContainer.ExecResult ls = container().execInContainer(commandAsImageUser("ls", "-a", path));
        assertThat(ls.getExitCode()).as("\n%s%s", ls.getStdout(), ls.getStderr()).isNotZero();
    }

    public void assertPathFilesAndDirsCount(String path, int count) throws IOException, InterruptedException {
        GenericContainer.ExecResult wc = container().execInContainer(commandAsImageUser("bash", "-i", "-c",
                "find " + path + " -mindepth 1 -maxdepth 1 \\( -type f -o -type d \\) | wc -l"));
        assertThat(wc.getExitCode()).as("\n%s%s", wc.getStdout(), wc.getStderr()).isZero();
        assertThat(wc.getStdout()).isEqualToIgnoringNewLines(String.valueOf(count));
    }

    public void assertExecutablePathEquals(String executable, String expectedPath) throws IOException, InterruptedException {
        GenericContainer.ExecResult command = container().execInContainer(
                commandAsImageUser("bash", "-i", "-c", "command -v " + executable));
        assertThat(command.getExitCode()).as("\n%s%s", command.getStdout(), command.getStderr()).isZero();
        assertThat(command.getStdout()).isEqualToIgnoringNewLines(expectedPath);
    }

    public void assertExecutablePathAndSymLinkEquals(String executable, String expectedPath,
            String expectedLink) throws IOException, InterruptedException {
        GenericContainer.ExecResult command = container().execInContainer(
                commandAsImageUser("bash", "-i", "-c", "command -v " + executable));
        assertThat(command.getExitCode()).as("\n%s%s", command.getStdout(), command.getStderr()).isZero();
        assertThat(command.getStdout()).isEqualToIgnoringNewLines(expectedPath);
        assertSymLinkEquals(expectedPath, expectedLink);
    }

    public void assertSymLinkEquals(String expectedPath, String expectedLink) throws IOException, InterruptedException {
        GenericContainer.ExecResult readLink = container().execInContainer(commandAsImageUser("readlink", expectedPath));
        assertThat(readLink.getExitCode()).as("\n%s%s", readLink.getStdout(), readLink.getStderr()).isZero();
        assertThat(readLink.getStdout()).isEqualToIgnoringNewLines(expectedLink);
    }

    public void assertNotSymLink(String expectedPath) throws IOException, InterruptedException {
        GenericContainer.ExecResult readLink = container().execInContainer(commandAsImageUser("readlink", expectedPath));
        assertThat(readLink.getExitCode()).as(readLink.getStdout()).isNotZero();
    }

    public void assertVersionEquals(String mavenProperty, String command) throws IOException, InterruptedException {
        GenericContainer.ExecResult version = container().execInContainer(commandAsImageUser("bash", "-i", "-c", command));
        assertThat(version.getExitCode()).as("\n%s%s", version.getStdout(), version.getStderr()).isZero();
        assertThat(version.getStdout()).isEqualToIgnoringNewLines(convertMavenProperty(mavenProperty));
    }

    private String convertMavenProperty(String mavenProperty) {
        return Arrays.stream(StringUtils.split(mavenProperty, "&")).map(MAVEN::getProperty).collect(Collectors.joining("-"));
    }

    public void assertVersionStartsWith(String mavenProperty, String command) throws IOException, InterruptedException {
        GenericContainer.ExecResult version = container().execInContainer(commandAsImageUser("bash", "-i", "-c", command));
        assertThat(version.getExitCode()).as("\n%s%s", version.getStdout(), version.getStderr()).isZero();
        assertThat(version.getStdout()).startsWith(MAVEN.getProperty(mavenProperty));
    }

    public void assertVersionNotEmpty(String command) throws IOException, InterruptedException {
        GenericContainer.ExecResult version = container().execInContainer(commandAsImageUser("bash", "-i", "-c", command));
        assertThat(version.getExitCode()).as("\n%s%s", version.getStdout(), version.getStderr()).isZero();
        assertThat(version.getStdout()).isNotEmpty();
    }

    public void assertEnvPropertyEquals(String envProperty, String expected) throws IOException, InterruptedException {
        GenericContainer.ExecResult env = container().execInContainer(
                commandAsImageUser("bash", "-i", "-c", "echo $" + envProperty));
        assertThat(env.getExitCode()).as("\n%s%s", env.getStdout(), env.getStderr()).isZero();
        assertThat(env.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    public void assertFileContains(String path, String... expected) throws IOException, InterruptedException {
        GenericContainer.ExecResult cat = container().execInContainer(commandAsImageUser("cat", path));
        assertThat(cat.getExitCode()).as("\n%s%s", cat.getStdout(), cat.getStderr()).isZero();
        assertThat(cat.getStdout()).contains(expected);
    }

    public void assertFileContains(String path, int count, String content1,
            String content2) throws IOException, InterruptedException {
        GenericContainer.ExecResult grepFind = container().execInContainer(commandAsImageUser("grep", "-R", content1, path));
        assertThat(grepFind.getExitCode()).as("\n%s%s", grepFind.getStdout(), grepFind.getStderr()).isZero();
        assertThat(grepFind.getStdout()).hasLineCount(count);
        GenericContainer.ExecResult grepFindL = container().execInContainer(commandAsImageUser("grep", "-R", content2, path));
        assertThat(grepFindL.getExitCode()).as("\n%s%s", grepFindL.getStdout(), grepFindL.getStderr()).isZero();
        assertThat(grepFindL.getStdout()).hasLineCount(count);
    }

    public void assertFileLinesCount(String path, int count) throws IOException, InterruptedException {
        GenericContainer.ExecResult wc = container().execInContainer(commandAsImageUser("bash", "-i", "-c", "wc -l < " + path));
        assertThat(wc.getExitCode()).as("\n%s%s", wc.getStdout(), wc.getStderr()).isZero();
        assertThat(wc.getStdout()).isEqualToIgnoringNewLines(String.valueOf(count));
    }

    public static ExecConfig commandAsImageUser(String... command) {
        return ExecConfig.builder().user(TestConstants.IMAGE_USER).workDir(TestConstants.USER_HOME).command(command).build();
    }

}
