#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-}"
PORT="${2:-8182}"

# Set DETACH=false to run in foreground
DETACH="${DETACH:-true}"

# On Linux, host.docker.internal may not exist by default; we add host-gateway mapping.
KAFKA_BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVER:-host.docker.internal:29092}"

if [[ -z "${VERSION}" ]]; then
  echo "Usage: $(basename "$0") <version> [port]" >&2
  echo "Env: DETACH=true|false, KAFKA_BOOTSTRAP_SERVER=host:port" >&2
  exit 1
fi

RUN_ARGS=(
  --rm
  -p "${PORT}:8181"
  -e "KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER}"
  --add-host=host.docker.internal:host-gateway
  "microservice-service-b:${VERSION}"
)

if [[ "${DETACH}" == "true" ]]; then
  echo "Starting Service B in detached mode on port ${PORT}"
  docker run -d "${RUN_ARGS[@]}"
else
  echo "Starting Service B in foreground mode on port ${PORT}"
  docker run "${RUN_ARGS[@]}"
fi
