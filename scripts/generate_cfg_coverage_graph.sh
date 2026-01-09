#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.cfg.coverage.GenerateCoverageGraph" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples $HOME/dev/master-thesis/data/coverage_paths.json"

echo "✅ Done!"

echo "--- GENERATE SVG FROM DOT FILE ---"

dot -Tsvg out/visualization/cfg_coverage0.dot -o out/visualization/svg/cfg_coverage0.svg

open out/visualization/svg/cfg_coverage0.svg

echo "✅ Done!"