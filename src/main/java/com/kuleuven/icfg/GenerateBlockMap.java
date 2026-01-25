package com.kuleuven.icfg;

import com.kuleuven.cg.SootUpCGWrapper;
import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;
import org.jspecify.annotations.Nullable;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateBlockMap {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: entryMethod            (e.g., "<com.kuleuven._examples.Foo: int foo(int)>")
         *   2: outputPath             (e.g., "./output/block_map.json")
         *   3: projectPrefixes        (e.g., "com.kuleuven,test.SimpleExample")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <entryMethod> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String entryFQMethodSignature = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;
        List<String> projectPrefixes = args.length >= 4
                ? List.of(args[3].split(","))
                : null;

        BlockInfoByIdMap blockMap = createICfgBlockMap(classPath, entryFQMethodSignature, projectPrefixes);

        try {
            blockMap.dump(outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write ICFG block map: " + e.getMessage());
            System.exit(1);
        }
    }

    private static BlockInfoByIdMap createICfgBlockMap(String classPath, String entryFQMethodSignature, @Nullable List<String> projectPrefixes) {
        Generator generator = new Generator(classPath, entryFQMethodSignature);

        JimpleBasedInterproceduralCFG icfg = generator.getICfg();
        CallGraph callGraph = icfg.getCg();

        SootUpCGWrapper cgWrapper = new SootUpCGWrapper(callGraph, projectPrefixes);
        Set<MethodSignature> methodSignatures = cgWrapper.getNodes();

        List<SootMethod> methods = methodSignatures.stream()
                .map(sig -> generator.view.getMethod(sig))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new BlockInfoByIdMap(methods);
    }
}
