# =========================================================================
#  InterCollege Platform - PowerShell Launcher
# =========================================================================

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Green
Write-Host "  Starting InterCollege Event Discovery Platform...      " -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green
Write-Host ""

$localMaven = "C:\Users\sreen\.gemini\antigravity\scratch\apache-maven-3.9.9\bin\mvn.cmd"

if (Test-Path $localMaven) {
    Write-Host "[INFO] Running via portable Maven: $localMaven" -ForegroundColor Cyan
    & $localMaven spring-boot:run
} else {
    Write-Host "[INFO] Running via system mvn..." -ForegroundColor Cyan
    mvn spring-boot:run
}
