#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.coverage.GenerateCoverageGraph" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.nested.Test: int bar(int)>\" $HOME/dev/master-thesis/data/coverage_paths.json"

echo "✅ Done!"

echo "--- GENERATE SVG DOT FILE ---"

dot -Tsvg out/icfg_coverage_graph.dot -o out/icfg_coverage_graph.svg

open out/icfg_coverage_graph.svg

echo "✅ Done!"