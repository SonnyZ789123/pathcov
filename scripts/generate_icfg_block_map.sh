#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.GenerateBlockMap" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.trycatch.Test: int foo(int)>\" $HOME/dev/master-thesis/coverage-guided-concolic-pipeline/development/data/coverage/coverage_data.json"

echo "✅ Done!"