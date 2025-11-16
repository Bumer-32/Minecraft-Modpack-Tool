@echo off
REM dev script for easy testing

chcp 65001

pushd %~dp0
call gradlew shadowJar
popd

echo.
echo.
echo.
echo.
echo.

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar %~dp0\build\libs\MinecraftModpackTool-1.0-SNAPSHOT-all.jar %*