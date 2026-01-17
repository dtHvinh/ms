#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-}"

if [[ -z "${VERSION}" ]]; then
  echo "Usage: $(basename "$0") <version>" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "Start building image for Service B"
docker build --no-cache -t "microservice-service-b:${VERSION}" -f "${SERVICE_DIR}/Dockerfile" "${SERVICE_DIR}"
echo "Finished building image for Service B"
