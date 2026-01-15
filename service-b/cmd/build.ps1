param(
  [Parameter(Mandatory=$true, Position=0)]
  [string]$Version
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$serviceDir = Resolve-Path (Join-Path $scriptDir '..')

Write-Host "Start building image for Service B"

docker build --no-cache -t "microservice-service-b:$Version" -f (Join-Path $serviceDir 'Dockerfile') $serviceDir

Write-Host "Finished building image for Service B"
