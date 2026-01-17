param(
  [Parameter(Mandatory=$true, Position=0)]
  [string]$Version
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$serviceDir = Resolve-Path (Join-Path $scriptDir '..')

Write-Host "Start building image for Service A"

docker build --no-cache -t "microservice-service-a:$Version" -f (Join-Path $serviceDir 'Dockerfile') $serviceDir

Write-Host "Finished building image for Service A"
