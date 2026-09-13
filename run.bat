@echo off
REM =========================================================================
REM  InterCollege Platform - Quick Start Launcher for Windows
REM =========================================================================

echo.
echo =========================================================================
echo   Starting InterCollege Event Discovery Platform...
echo =========================================================================
echo.

set MAVEN_EXE=C:\Users\sreen\.gemini\antigravity\scratch\apache-maven-3.9.9\bin\mvn.cmd

if exist "%MAVEN_EXE%" (
    echo [INFO] Using configured Maven at %MAVEN_EXE%
    call "%MAVEN_EXE%" spring-boot:run
) else (
    echo [INFO] Attempting system mvn command...
    mvn spring-boot:run
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed to start. Please verify Java 17+ is installed.
    pause
)
