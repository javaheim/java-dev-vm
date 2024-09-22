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
import java.time.Year;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.javaheim.jdvm.testcontainers.setup.JavaDevVmContainer;

/**
 * Java DEV VM General Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class JavaDevVmTest extends JavaDevVmContainer {

    @Test
    void runningAndHealthy() {
        assertThat(getJavaDevVm().isPrivilegedMode()).isTrue();
        assertThat(getJavaDevVm().isRunning()).isTrue();
        assertThat(getJavaDevVm().isHealthy()).isTrue();
    }

    @Test
    void testJdvmDBusSessionService() throws IOException, InterruptedException {
        String isActive = getJavaDevVmExecutor().run("systemctl is-active jdvm-dbus-session.service").exec();
        assertThat(isActive).isEqualTo("active");

        String subState = getJavaDevVmExecutor().run("systemctl show -p SubState jdvm-dbus-session.service").exec();
        assertThat(subState).isEqualTo("SubState=exited");

        String execMainStatus = getJavaDevVmExecutor().run("systemctl show -p ExecMainStatus jdvm-dbus-session.service").exec();
        assertThat(execMainStatus).isEqualTo("ExecMainStatus=0");

        String xdgRuntimeDirName = "/run/user/%s".formatted(MAVEN.getProperty("image.user.uid"));
        String dbusDaemons = getJavaDevVmExecutor().run("pgrep -a \"dbus-daemon\"").exec();
        assertThat(dbusDaemons).contains(
                "@dbus-daemon --system --address=systemd: --nofork --nopidfile --systemd-activation --syslog-only");
        assertThat(dbusDaemons).contains(
                "dbus-daemon --session --address=unix:path=%s/bus --nofork --nopidfile --syslog-only".formatted(
                        xdgRuntimeDirName));
        assertThat(dbusDaemons).hasLineCount(2);

        String xdgRuntimeDir = getJavaDevVmExecutor().ls(xdgRuntimeDirName).exec();
        assertThat(xdgRuntimeDir).contains("bus");
    }

    @Test
    void testJdvmLauncherService() throws IOException, InterruptedException {
        String isActive = getJavaDevVmExecutor().run("systemctl is-active jdvm-launcher.service").exec();
        assertThat(isActive).isEqualTo("active");

        String subState = getJavaDevVmExecutor().run("systemctl show -p SubState jdvm-launcher.service").exec();
        assertThat(subState).isEqualTo("SubState=running");
    }

    @Test
    void testJdvmLauncherAtHome() throws IOException, InterruptedException {
        CharSequence[] apps = new CharSequence[]{
                "01-terminal.app", "02-firefox.app", "03-file-manager.app"
        };
        String jdvmLauncherDir = getJavaDevVmExecutor().ls(USER_HOME + "/.jdvm-launcher").exec();
        assertThat(jdvmLauncherDir).contains(apps).hasLineCount(apps.length);
    }

    @Test
    void testLicenses() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "LICENSE", "NOTICE"
        };
        String licensesDir = getJavaDevVmExecutor().ls("/licenses").exec();
        assertThat(licensesDir).contains(files).hasLineCount(files.length);

        String license = getJavaDevVmExecutor().cat("/licenses/LICENSE").exec();
        String copyright = "© 2024-%s Javaheim".formatted(Year.now().getValue());
        assertThat(license).contains(copyright);
        String licenseType = "Apache License, Version 2.0";
        assertThat(license).contains(licenseType);

        String notice = getJavaDevVmExecutor().cat("/licenses/NOTICE").exec();
        assertThat(notice).contains(copyright);
        assertThat(notice).contains(licenseType);
        assertThat(notice).contains("LicenseRef-ThirdParty");
    }

    @Test
    void testJdvmConfig() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "betterfox-policies.json", "betterfox-user.js", "docker-daemon.json", "kitty.conf", "tealdeer-config.toml"
        };
        String jdvmConfigDir = getJavaDevVmExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesBase() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "dockerfile-env", "docker-env", "dconf", "env", "p10k.zsh"
        };
        String jdvmTemplatesBaseDir = getJavaDevVmExecutor().ls("/etc/jdvm-templates/base").exec();
        assertThat(jdvmTemplatesBaseDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testDockerfileEnv() throws IOException, InterruptedException {
        String dockerfileEnv = getJavaDevVmExecutor().cat("/etc/jdvm-templates/base/dockerfile-env").exec();
        assertThat(dockerfileEnv).contains("export DEBCONF_NOWARNINGS=yes");
        assertThat(dockerfileEnv).contains("export DEBIAN_FRONTEND=noninteractive");
        assertThat(dockerfileEnv).contains("export DISPLAY=:0");
        assertThat(dockerfileEnv).contains("export JDVM_USER=dev");
        assertThat(dockerfileEnv).hasLineCount(4);
    }

    @Test
    void testDockerEnv() throws IOException, InterruptedException {
        String dockerEnv = getJavaDevVmExecutor().cat("/etc/jdvm-templates/base/docker-env").exec();
        assertThat(dockerEnv).contains("export DISPLAY=");
        assertThat(dockerEnv).contains("export HOSTNAME=");
        assertThat(dockerEnv).hasLineCount(2);
    }

    @Test
    void testJdvmTemplatesLocal() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                ".aliases.local", ".bash_aliases.local", ".bash_env.local", ".bash_logout.local", ".bashrc.local", ".env.local",
                ".profile.local", ".zlogout.local", ".zprofile.local", ".zshaliases.local", ".zshenv.local", ".zshrc.local"
        };
        String jdvmTemplatesLocalDir = getJavaDevVmExecutor().ls("/etc/jdvm-templates/local").exec();
        assertThat(jdvmTemplatesLocalDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesUser() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                ".aliases", ".bash_aliases", ".bash_env", ".env", ".ps1", ".zlogout", ".zprofile", ".zshenv", ".zshrc"
        };
        String jdvmTemplatesUserDir = getJavaDevVmExecutor().ls("/etc/jdvm-templates/user").exec();
        assertThat(jdvmTemplatesUserDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testWhoAmI() throws IOException, InterruptedException {
        String user = getJavaDevVmExecutor().run("whoami").exec();
        assertThat(user).isEqualTo(IMAGE_USER);
    }

}
