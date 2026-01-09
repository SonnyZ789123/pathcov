#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.GenerateBlockMap" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.nested.Test: int bar(int)>\" $HOME/dev/master-thesis/pathcov/out/icfg_block_map.json"

echo "✅ Done!"