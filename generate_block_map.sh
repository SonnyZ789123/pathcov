#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.coverage.GenerateBlockMap" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.ControlFlowGraph.Test: int foo(int)>\""

echo "✅ Done!"