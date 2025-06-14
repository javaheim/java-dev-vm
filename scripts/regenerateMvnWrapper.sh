#!/bin/bash
# REGENERATE MAVEN WRAPPER

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
set -o pipefail # DON'T HIDE ERRORS WITHIN PIPES

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"

usage() {
  cat << EOF
Usage: $(basename "$0")

Regenerate Maven Wrapper
EOF
  exit 1
}

main() {
  readOptions "$@"
  readOptions "$@"
  cd ..
  step "Regenerate Maven Wrapper"
  mvn wrapper:wrapper
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
  shift $((OPTIND - 1))
}

main "$@"
