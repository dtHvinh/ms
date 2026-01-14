param(
  [Parameter(Mandatory=$true, Position=0)]
  [string]$Version
  [int]$Port = 8182,
  [bool]$Detach = $false
)

$ErrorActionPreference = 'Stop'

if ($Detach) {
    Write-Host "Starting Service A in detached mode"
    docker run -d -p $Port:8181 --name "service-a-$Version" microservice-service-a:$Version
} else {
    Write-Host "Starting Service A in foreground mode"
    docker run --rm -p $Port:8181 microservice-service-a:$Version
}