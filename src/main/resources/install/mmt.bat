@echo off
chcp 65001 >nul

setlocal

set "CALL_DIR=%CD%"
set "SCRIPT_DIR=%~dp0"

set "LOCAL_JAR=%CALL_DIR%\.mmt\mmt.jar"
set "USER_JAR=%USERPROFILE%\.mmt\mmt.jar"

if exist "%LOCAL_JAR%" (
    java -jar "%LOCAL_JAR%" %*
    exit /b %errorlevel%
)

if exist "%USER_JAR%" (
    java -jar "%USER_JAR%" %*
    exit /b %errorlevel%
)

echo mmt.jar not found
exit /b 1