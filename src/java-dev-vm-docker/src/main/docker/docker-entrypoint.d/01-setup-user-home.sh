#!/bin/bash
# SETUPS USER HOME

#
# © 2024-2025 Javaheim
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -o errexit  # ABORT ON NON-ZERO EXIT STATUS
set -o nounset  # TREAT UNSET VARIABLES AS AN ERROR AND EXIT
set -o pipefail # DON'T HIDE ERRORS WITHIN PIPES

readonly INFO="\e[1;34m>\e[0m"

main() {
  setupApps
  setupFirefox
  setupGedit
  setupGit
  setupGo
  setupKitty
  setupKubectlKrew
  setupLauncher
  setupMaven
  setupProjects
  setupSdkMan
  setupTealdeer
  setupYarn
}

setupApps() {
  if [[ ! -e "/home/${USER}/apps" ]]; then
    echo -e "${INFO} Setup Apps..."
    mkdir -v --parents "/home/${USER}/apps"
  fi
}

setupFirefox() {
  local profileDir
  profileDir="$(printf '%s\n' "/home/${USER}/.mozilla/firefox"/*.dev)"
  if [[ ! -e "${profileDir}/user.js" ]]; then
    echo -e "${INFO} Setup Firefox..."
    if [[ ! -e "${profileDir}" ]]; then
      firefox --headless -CreateProfile "${USER}" > /dev/null 2>&1
      profileDir="$(printf '%s\n' "/home/${USER}/.mozilla/firefox"/*.dev)"
      echo -e "${INFO} Created $(basename "${profileDir}") Firefox profile"
    fi
    ln -v --symbolic --force "/etc/jdvm-config/betterfox-user.js" "${profileDir}/user.js"
  else
    local currentSha savedSha
    currentSha=$(sha256sum "$(readlink -f "${profileDir}/user.js")" | awk '{print $1}')
    if [[ -f "${profileDir}/.userjs.sha256" ]]; then
        savedSha="$(cat "${profileDir}/.userjs.sha256")"
    fi
    if [[ "${currentSha}" != "${savedSha-}" ]]; then
        echo -e "${INFO} Detected change in Firefox user.js - removing prefs.js to reload settings"
        echo "${currentSha}" > "${profileDir}/.userjs.sha256"
        if [[ -f "${profileDir}/prefs.js" ]]; then
          rm -v "${profileDir}/prefs.js"
        fi
    fi
  fi
}

setupGedit() {
  if [[ ! -e "/home/${USER}/.config/dconf/user" ]]; then
    echo -e "${INFO} Setup Gedit..."
    if [[ ! -e "/home/${USER}/.config/dconf" ]]; then
      mkdir -v --parents "/home/${USER}/.config/dconf"
    fi
    ln -v --symbolic --force "/etc/jdvm-templates/base/dconf/user" "/home/${USER}/.config/dconf/user"
  fi
}

setupGit() {
  if [[ ! -e "/home/${USER}/.gitconfig" || -z "$(git config --global "init.defaultBranch")" ]]; then
    echo -e "${INFO} Setup Git: set init.defaultBranch=main..."
    git config --global "init.defaultBranch" "main"
  fi
  if [[ ! -e "/home/${USER}/.gitconfig" || -z "$(git config --global "core.autocrlf")" ]]; then
    echo -e "${INFO} Setup Git: set core.autocrlf=input..."
    git config --global "core.autocrlf" "input"
  fi
}

setupGo() {
  if [[ ! -e "/home/${USER}/.config/go/telemetry/mode" ]]; then
    echo -e "${INFO} Setup Go..."
    if [[ ! -e "/home/${USER}/.config/go/telemetry" ]]; then
      mkdir -v --parents "/home/${USER}/.config/go/telemetry"
    fi
    echo "off" | tee "/home/${USER}/.config/go/telemetry/mode" > /dev/null
    echo "Telemetry turned off"
  fi
}

setupKitty() {
  if [[ ! -e "/home/${USER}/.config/kitty/kitty.conf" ]]; then
    echo -e "${INFO} Setup Kitty..."
    if [[ ! -e "/home/${USER}/.config/kitty" ]]; then
      mkdir -v --parents "/home/${USER}/.config/kitty"
    fi
    ln -v --symbolic --force "/etc/jdvm-config/kitty.conf" "/home/${USER}/.config/kitty/kitty.conf"
  fi
}

setupKubectlKrew() {
  if [[ ! -e "/home/${USER}/.krew/index" ]]; then
    echo -e "${INFO} Setup Kubectl Krew: index..."
    if [[ ! -e "/home/${USER}/.krew" ]]; then
      mkdir -v "/home/${USER}/.krew"
    fi
    ln -v --symbolic --force "/opt/krew/index" "/home/${USER}/.krew/index"
  fi
  shopt -s nullglob
  for dir in "/opt/krew"/*; do
    if [[ ! -e "/home/${USER}/.krew/$(basename "${dir}")" ]]; then
      echo -e "${INFO} Setup Kubectl Krew: $(basename "${dir}")..."
      mkdir -v --parents "/home/${USER}/.krew/$(basename "${dir}")"
    fi
  done
  shopt -u nullglob
}

setupLauncher() {
  if [[ ! -e "/home/${USER}/.jdvm-launcher" ]]; then
    echo -e "${INFO} Setup Launcher..."
    mkdir -v --parents "/home/${USER}/.jdvm-launcher"
  fi
  shopt -s nullglob
  for app in "/etc/jdvm-systemd/config/jdvm-launcher/apps"/*.app; do
    local homeJdvmLauncherAppPath
    homeJdvmLauncherAppPath="/home/${USER}/.jdvm-launcher/$(basename "${app}")"
    if [[ ! -e "${homeJdvmLauncherAppPath}" ]]; then
      echo -e "${INFO} Setup Launcher: $(basename "${app}")..."
      ln -v --symbolic --force "${app}" "${homeJdvmLauncherAppPath}"
    fi
  done
  shopt -u nullglob
}

setupMaven() {
  if [[ ! -e "/home/${USER}/.m2/repository" ]]; then
    echo -e "${INFO} Setup Maven..."
    mkdir -v --parents "/home/${USER}/.m2/repository"
  fi
}

setupProjects() {
  if [[ ! -e "/home/${USER}/projects" ]]; then
    echo -e "${INFO} Setup Projects..."
    mkdir -v --parents "/home/${USER}/projects"
  fi
}

setupSdkMan() {
  if [[ ! -e "/home/${USER}/.sdkman/candidates" ]]; then
    echo -e "${INFO} Setup SdkMan..."
    mkdir -v --parents "/home/${USER}/.sdkman/candidates"
  fi
  shopt -s nullglob
  for dir in "/opt/sdkman"/*; do
    if [[ ! -e "/home/${USER}/.sdkman/$(basename "${dir}")" ]]; then
      echo -e "${INFO} Setup SdkMan: $(basename "${dir}")..."
      ln -v --symbolic --force "${dir}" "/home/${USER}/.sdkman/$(basename "${dir}")"
    fi
  done
  shopt -u nullglob
}

setupTealdeer() {
  if [[ ! -e "/home/${USER}/.config/tealdeer/config.toml" ]]; then
    echo -e "${INFO} Setup Tealdeer..."
    if [[ ! -e "/home/${USER}/.config/tealdeer" ]]; then
      mkdir -v --parents "/home/${USER}/.config/tealdeer"
    fi
    ln -v --symbolic --force "/etc/jdvm-config/tealdeer-config.toml" "/home/${USER}/.config/tealdeer/config.toml"
  fi
}

setupYarn() {
  if [[ ! -e "/home/${USER}/.yarnrc.yml" ]]; then
    echo -e "${INFO} Setup Yarn..."
    echo "enableTelemetry: false" | tee "/home/${USER}/.yarnrc.yml" > /dev/null
    echo "Telemetry turned off"
  fi
}

main
