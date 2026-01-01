#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.jdart.GenerateJDartInstructionCoverage" -Dexec.args="$HOME/dev/master-thesis/data/coverage.out $HOME/dev/master-thesis/pathcov/out/cfg_block_map.json $HOME/dev/master-thesis/pathcov/out/jdart_instruction_paths.json"

echo "✅ Done!"
