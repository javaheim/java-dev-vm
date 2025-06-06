#!/bin/bash
# BUILDS WHOLE PROJECT

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

readonly CURRENT_VERSIONS="test/java-dev-vm-testcontainers/target/versions.md"

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"
readonly ERROR="[\e[1;31mERROR\e[0m]"

usage() {
  cat << EOF
Usage: $(basename "$0") [OPTION]...

Builds whole project

OPTIONS:
  -i                     Build image and remove unused images
  -r                     Remove buildx cache
  -t                     Run tests
  -d                     Dry-run JReleaser release
EOF
  exit 1
}

main() {
  cd ..
  readOptions "$@"
  scripts/updateCopyright.sh
  mvnCleanInstall
  forceRemoveBuildxCache
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

readOptions() {
  while [[ "$#" -gt 0 ]]; do
    case "${1}" in
      -i) profile+="build-image," ;;
      -r) profile+="remove-buildx-cache," ;;
      -t) profile+="integration-tests,prepare-release," ;;
      -d) dryRunJReleaserRelease ;;
      -h|--help) usage ;;
      *) remainingOptions+=("${1}") ;;
    esac
    shift
  done
  if [[ -n "${profile-}" ]]; then
    profile="-P${profile%,}"
    if [[ "${profile}" == *"remove-buildx-cache"* && "${profile}" != *"build-image"* ]]; then
      forceRemoveBuildxCache=true
    fi
  fi
}

dryRunJReleaserRelease() {
  if [[ ! -f "${CURRENT_VERSIONS}" ]]; then
    echo -e "${ERROR} The ${CURRENT_VERSIONS} is missing. Run VersionsTest#versionsOutput to generate it"
    exit 1
  fi
  if [[ -z "${GITHUB_TOKEN-}" ]]; then
    echo -e "${ERROR} The GITHUB_TOKEN env variable is not set"
    exit 1
  fi
  printf "## Changelog\n\n{{changelogChanges}}{{changelogContributors}}%s" "$(cat test/java-dev-vm-testcontainers/target/versions.md)" > "test/java-dev-vm-testcontainers/target/changelog.tpl"
  JRELEASER_GITHUB_TOKEN=${GITHUB_TOKEN-} jreleaser release --dry-run --output-directory=target
  exit 0
}

forceRemoveBuildxCache() {
  if [[ "${forceRemoveBuildxCache:-false}" == true ]] && docker volume ls -q | grep -q "java-dev-vm"; then
    local volumeName
    volumeName="$(docker volume ls -q | grep "java-dev-vm")"
    echo; step "Remove ${volumeName} volume"
    docker volume rm "${volumeName}"
  fi
}

mvnCleanInstall() {
  step "Clean and Install"
  if [[ -n "${profile-}" ]]; then
    run mvn clean install "${profile}" "${remainingOptions[@]}"
  else
    run mvn clean install "${remainingOptions[@]}"
  fi
}

run() {
	echo -e "${INFO} \e[1m$\e[0m $*"; "$@"
}

main "$@"
