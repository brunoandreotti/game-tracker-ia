# Loads root .env into the process environment, then runs mvn spring-boot:run.
# Usage: .\scripts\run-local.ps1
# Extra Maven args are forwarded: .\scripts\run-local.ps1 -Dspring-boot.run.arguments=--debug

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
	Write-Error "Missing .env. Copy .env.example to .env and set RAWG_API_KEY."
}

Get-Content $envFile | ForEach-Object {
	$line = $_.Trim()
	if ($line -eq "" -or $line.StartsWith("#")) {
		return
	}
	$eq = $line.IndexOf("=")
	if ($eq -lt 1) {
		return
	}
	$name = $line.Substring(0, $eq).Trim()
	$value = $line.Substring($eq + 1).Trim()
	if (
		($value.StartsWith('"') -and $value.EndsWith('"')) -or
		($value.StartsWith("'") -and $value.EndsWith("'"))
	) {
		$value = $value.Substring(1, $value.Length - 2)
	}
	Set-Item -Path "Env:$name" -Value $value
}

if (-not $env:RAWG_API_KEY) {
	Write-Error "RAWG_API_KEY is empty in .env. Set your RAWG key and try again."
}

Set-Location $root
mvn spring-boot:run @args
