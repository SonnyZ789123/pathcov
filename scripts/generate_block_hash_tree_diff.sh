#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.blockmap.diff.GenerateBlockHashTreeDiff" -Dexec.args="out/previous_icfg_block_map.json out/icfg_block_map.json out/block_hash_tree_diff.json"

echo "✅ Done!"