#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.cfg.GenerateCFG" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.branches.Test: int foo(int, int, test.branches.Test\$Mode)>\""

echo "✅ Done!"

echo "--- GENERATE SVG DOT FILE ---"

dot -Tsvg out/cfg.dot -o out/cfg.svg

open out/cfg.svg

echo "✅ Done!"