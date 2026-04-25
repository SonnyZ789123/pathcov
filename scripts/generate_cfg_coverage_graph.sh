#!/bin/bash
# Copyright (c) 2025-2026 Yoran Mertens
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.


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