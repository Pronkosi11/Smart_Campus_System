#!/usr/bin/env bash
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
exec java -cp out Main
