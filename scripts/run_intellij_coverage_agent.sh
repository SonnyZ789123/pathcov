#!/usr/bin/env bash
set -Eeuo pipefail

# ============================================================
# CONFIG
# ============================================================
readonly HOME_DIR="$HOME"

# Specific to the SUT
readonly JDART_EXAMPLES_DIR="$HOME_DIR/dev/jdart-examples"
readonly CLASS_PATH="$JDART_EXAMPLES_DIR/out/production/jdart-examples"
readonly TEST_CLASS_PATH="$JDART_EXAMPLES_DIR/out/test/jdart-examples"

# Fixed config
readonly JUNIT_CONSOLE_JAR="$HOME_DIR/.m2/repository/org/junit/platform/junit-platform-console-standalone/1.12.2/junit-platform-console-standalone-1.12.2.jar"

readonly AGENT_JAR="$HOME_DIR/.m2/repository/org/jetbrains/intellij/deps/intellij-coverage-agent/1.0.771/intellij-coverage-agent-1.0.771.jar"

readonly CONFIG_ARGS_PATH="$HOME_DIR/dev/intellij-coverage/config.args"

echo  "[INFO] ⚙️ Running JUnit tests with coverage agent"

set +e
java \
  -javaagent:"$AGENT_JAR=$CONFIG_ARGS_PATH" \
  -cp "$JUNIT_CONSOLE_JAR:$TEST_CLASS_PATH:$CLASS_PATH" \
  org.junit.platform.console.ConsoleLauncher \
  --scan-classpath

exit_code=$?
set -e

if [[ $exit_code -ne 0 ]]; then
  warn "========================================================="
  warn "    ⚠️ Some JUnit tests FAILED, continuing anyway ⚠️"
  warn "========================================================="
fi

echo  "[INFO] ✅ Running JUnit tests completed"

