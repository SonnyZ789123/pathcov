#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.coverage.GenerateCFGCoverageGraph" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples out/cfg_block_map.json $HOME/dev/master-thesis/data/coverage.out"

echo "✅ Done!"

echo "--- GENERATE SVG FROM DOT FILE ---"

dot -Tsvg out/cfg_coverage0.dot -o out/cfg_coverage0.svg

open out/cfg_coverage0.svg

echo "✅ Done!"