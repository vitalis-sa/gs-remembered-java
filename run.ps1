# Carrega o .env e sobe a API:  .\run.ps1
# Swagger: http://localhost:8080/swagger-ui.html
$ErrorActionPreference = "Stop"
$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "Arquivo .env nao encontrado. Copie o modelo:  cp .env.example .env" -ForegroundColor Yellow
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $linha = $_.Trim()
    if ($linha -eq "" -or $linha.StartsWith("#")) { return }
    $partes = $linha.Split("=", 2)
    if ($partes.Length -ne 2 -or $partes[1].Trim() -eq "") { return }
    [System.Environment]::SetEnvironmentVariable($partes[0].Trim(), $partes[1].Trim(), "Process")
}

foreach ($obrigatoria in @("DB_USER", "DB_PASSWORD", "JWT_SECRET", "CLOUDAMQP_URL")) {
    if (-not [System.Environment]::GetEnvironmentVariable($obrigatoria, "Process")) {
        Write-Host "Faltando no .env: $obrigatoria" -ForegroundColor Red
        exit 1
    }
}

Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run
