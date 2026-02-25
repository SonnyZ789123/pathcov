#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.icfg.GenerateBlockMap" -Dexec.args="$HOME/dev/jdart-examples/out/production/jdart-examples \"<test.blockhash.BlockHashExample: int foo(int)>\" null"

echo "✅ Done!"