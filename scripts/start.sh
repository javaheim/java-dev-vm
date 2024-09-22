#!/bin/bash
# STARTS CONTAINER

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

readonly IMAGE_NAMESPACE="javaheim"
readonly IMAGE_NAME="java-dev-vm"
IMAGE_VERSION="$(grep -A 1 "<artifactId>java-dev-vm</artifactId>" "../pom.xml" | grep "<version>" | sed "s/.*<version>\(.*\)<\/version>.*/\1/")"
readonly IMAGE_VERSION
CONTAINER_NAME="jdvm-$(git rev-parse --short HEAD)"
readonly CONTAINER_NAME

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"

usage() {
  cat << EOF
Usage: $(basename "$0") [OPTION]...

Starts container
EOF
  exit 1
}

main() {
  cd ..
  start
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

start() {
  step "Start ${IMAGE_NAME}:${IMAGE_VERSION}"
  run docker container run --privileged -d \
    --name "${CONTAINER_NAME}" \
    --hostname "${CONTAINER_NAME}" \
    --mount type=bind,source=/tmp/.X11-unix,target=/tmp/.X11-unix \
    --env "DISPLAY=${DISPLAY}" \
    --shm-size 2g \
    "${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}"
}

run() {
	echo -e "${INFO} \e[1m$\e[0m $*"; "$@"
}

# The file is not executed, but sourced in another script
[[ "${BASH_SOURCE[0]}" != "${0}" ]] && return 0

main "$@"
