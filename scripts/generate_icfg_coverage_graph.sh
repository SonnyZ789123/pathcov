#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.coverage.GenerateCoverageGraph" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.blockhash.BlockHashExample: int combine(int, int)>\" $HOME/dev/master-thesis/coverage-guided-concolic-pipeline/development/data/blockmaps/icfg_block_map.json"

echo "✅ Done!"

echo "--- GENERATE SVG DOT FILE ---"

dot -Tsvg out/visualization/icfg/coverage/coverage_graph.dot -o out/visualization/icfg/coverage/coverage_graph.svg

open out/visualization/icfg/coverage/coverage_graph.svg

echo "✅ Done!"