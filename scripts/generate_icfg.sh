#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.GenerateICFG" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.nested.Test: int bar(int)>\""

echo "✅ Done!"

echo "--- GENERATE SVG DOT FILE ---"

dot -Tsvg out/icfg.dot -o out/icfg.svg

open out/icfg.svg

echo "✅ Done!"