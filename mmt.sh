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
java -jar "$SCRIPT_DIR/build/libs/MinecraftModpackTool-1.0-SNAPSHOT-all.jar" "$@"