# Sandboxed MySQL Runner
$MySqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin"
$DataDir = Join-Path $PSScriptRoot "mysql-data"

if (-not (Test-Path $DataDir)) {
    New-Item -ItemType Directory -Path $DataDir | Out-Null
}

$MySqlDir = Join-Path $DataDir "mysql"
if (-not (Test-Path $MySqlDir)) {
    Write-Host "Initializing MySQL data directory..." -ForegroundColor Cyan
    & "$MySqlBin\mysqld.exe" --initialize-insecure --datadir=$DataDir --console
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to initialize MySQL data directory."
        exit $LASTEXITCODE
    }
    Write-Host "MySQL initialized successfully." -ForegroundColor Green
}

Write-Host "Starting MySQL Server on port 3306..." -ForegroundColor Cyan
Write-Host "Data Directory: $DataDir" -ForegroundColor Cyan
& "$MySqlBin\mysqld.exe" --datadir=$DataDir --port=3306 --console
