param(
  [Parameter(Mandatory, Position = 0)]
  [string]$Version,
  [int]$Port = 8181,
  [switch]$Detach = $true
)
$ErrorActionPreference = 'Stop'

if ($Detach) {
  Write-Host "Starting Service A in detached mode on port $Port"
  docker run -d --rm -p "${Port}:8181" -e "KAFKA_BOOTSTRAP_SERVER=host.docker.internal:29092" "microservice-service-a:$Version"

} else {
  Write-Host "Starting Service A in foreground mode on port $Port"
  docker run --rm -p "${Port}:8181" -e "KAFKA_BOOTSTRAP_SERVER=host.docker.internal:29092" "microservice-service-a:$Version"
}
