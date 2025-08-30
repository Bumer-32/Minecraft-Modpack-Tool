@echo off
REM dev script for eazy testing

cd /d %~dp0


chcp 65001

call gradlew shadowJar

java -jar .\build\libs\MinecraftModpackTool-1.0-SNAPSHOT-all.jar %*