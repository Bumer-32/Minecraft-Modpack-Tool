@echo off
chcp 65001 >nul

setlocal

set "JAR=%CD%\.mmt\mmt.jar"

java -jar "%JAR%" %*
exit /b %errorlevel%