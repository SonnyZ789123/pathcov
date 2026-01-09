#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.cfg.coverage.GenerateCoverageGraph" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples $HOME/dev/master-thesis/data/coverage_paths.json"

echo "✅ Done!"

echo "====== Generating SVGs from DOT files ======"

# See application.properties in pathcov repository for output dir
COVERAGE_DIR="$HOME/dev/master-thesis/pathcov/out/visualization/cfg/coverage"
DOT_DIR="$COVERAGE_DIR/dot"
SVG_DIR="$COVERAGE_DIR/svg"
mkdir -p "$SVG_DIR"

for dot_file in "$DOT_DIR"/*.dot; do
  base=$(basename "$dot_file" .dot)
  dot -Tsvg "$dot_file" -o "$SVG_DIR/$base.svg"
  echo "✅ Generated $SVG_DIR/$base.svg"
done