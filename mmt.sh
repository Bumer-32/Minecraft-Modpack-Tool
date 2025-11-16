#!/usr/bin/env bash
# dev script for easy testing

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

"$SCRIPT_DIR/gradlew" shadowJar

echo
echo
echo
echo
echo

# run jar
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar "$SCRIPT_DIR/build/libs/MinecraftModpackTool-1.0-SNAPSHOT-all.jar" "$@"