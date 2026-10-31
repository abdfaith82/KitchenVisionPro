#!/bin/sh

##############################################################################
# Gradle start script for Unix
##############################################################################

# Attempt to set APP_HOME
APP_HOME=$( cd "${APP_HOME:-$( dirname "$0" )}" && pwd -P ) || exit

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

# Add default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Determine the Java command
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Download wrapper jar if missing
if [ ! -f "$CLASSPATH" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -sL -o "$CLASSPATH" \
      "https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar" \
      || curl -sL -o "$CLASSPATH" \
      "https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar"
fi

exec "$JAVACMD" \
  $DEFAULT_JVM_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
