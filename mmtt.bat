@echo off
REM dev script for eazy testing
REM only for already assembled jar!

chcp 65001

java -jar %~dp0\build\libs\MinecraftModpackTool-1.0-SNAPSHOT-all.jar %*