#!/bin/sh

set -eu

PRG="$0"
while [ -h "$PRG" ]; do
  ls_output=$(ls -ld "$PRG")
  link_target=$(expr "$ls_output" : '.*-> \(.*\)$')
  if expr "$link_target" : '/.*' >/dev/null; then
    PRG="$link_target"
  else
    PRG=$(dirname "$PRG")"/$link_target"
  fi
done

APP_HOME=$(cd "$(dirname "$PRG")" >/dev/null && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "${JAVA_HOME:-}" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
  if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="java"
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  fi
fi

exec "$JAVACMD" \
  "-Xmx64m" \
  "-Xms64m" \
  "-Dorg.gradle.appname=gradlew" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
