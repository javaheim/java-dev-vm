#!/bin/bash
# PREPARES NEW FEATURE OR BUGFIX BRANCH

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

readonly ARTIFACT_ID="java-dev-vm"

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"
readonly WARNING="[\e[1;33mWARNING\e[0m]"
readonly SUCCESS="[\e[1;32mSUCCESS\e[0m]"

usage() {
  cat << EOF
Usage: $(basename "$0")

Prepares new feature or bugfix branch
EOF
  exit 1
}

main() {
  readOptions "$@"
  if [[ "$(git branch --show-current)" =~ ^(feature|bugfix) ]]; then
    isUpdated=false
    local branchVersion
    branchVersion="$(git branch --show-current | sed "s|.*/||")"
    step "Update ${ARTIFACT_ID} to ${branchVersion}"
    updateArtifactIdVersion "${ARTIFACT_ID}" "${branchVersion}" "pom.xml"
    updateArtifactIdVersionInModules
    updatePropertyInFile "IMAGE_VERSION=" "${branchVersion}" "support-scripts/restart.bat"
    updatePropertyInFile "IMAGE_VERSION=" "${branchVersion}" "support-scripts/restart.sh"
    updatePropertyInFile "IMAGE_VERSION=" "${branchVersion}" "support-scripts/load-image.bat"
    updatePropertyInFile "version:" "${branchVersion}" "jreleaser.yml"
    if [[ "${isUpdated}" == false ]]; then
      echo -e "${WARNING} Nothing to update"
    fi
    echo
  fi
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

readOptions() {
  while getopts ":h" option; do
    case "${option}" in
      h|?) usage ;;
    esac
  done
}

updateArtifactIdVersion() {
  local artifactId="${1}"
  local version="${2}"
  local pomPath="${3}"
  if ! grep -A 1 "<artifactId>${artifactId}</artifactId>" "${pomPath}" | grep -q "${version}"; then
    echo -e "${INFO} Updating: ${pomPath}"
    sed -z -i "s|\(<artifactId>${artifactId}</artifactId>[^<]*<version>\)[^<]*\(</version>\)|\1${version}\2|" "${pomPath}";
    isUpdated=true
    echo -e "${SUCCESS}"
  fi
}

updateArtifactIdVersionInModules() {
  local modules
  modules="$(grep -oP "(?<=<module>).*?(?=</module>)" "pom.xml")"
  for module in ${modules}; do
    updateArtifactIdVersion "${ARTIFACT_ID}" "${branchVersion}" "${module}/pom.xml"
  done
}

updatePropertyInFile() {
  local propertyName="${1}"
  local propertyValue="${2}"
  local file="${3}"
  if ! grep -q "${propertyName}.*${propertyValue}" "${file}"; then
    echo -e "${INFO} Updating: ${file}"
    sed -E -i "s/(${propertyName})( ?)(\"?)[^\"]*(\"?)/\1\2\3${propertyValue}\4/" "${file}"
    isUpdated=true
    echo -e "${SUCCESS}"
  fi
}

main "$@"
