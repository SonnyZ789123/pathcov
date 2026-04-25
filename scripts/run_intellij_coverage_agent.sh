#!/usr/bin/env bash
# Copyright (c) 2025-2026 Yoran Mertens
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

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

