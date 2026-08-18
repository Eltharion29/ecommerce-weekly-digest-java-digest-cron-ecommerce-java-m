#!/bin/sh
set -eu
BUILD_DIR="${TMPDIR:-/tmp}/ecommerce-weekly-digest-test-classes"
mkdir -p "$BUILD_DIR"
find src/main/java src/test/java -name '*.java' -print | xargs javac -d "$BUILD_DIR"
java -cp "$BUILD_DIR" dev.infrai.digest.service.WeeklyDigestServiceTest
