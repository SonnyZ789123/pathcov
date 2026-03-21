# The Pathcov pipeline

> First take a look at `/Users/yoran.mertens/dev/master-thesis/claude-context`. This contains the context about the whole project (CONTEXT.md) and how to work (HOW_TO_WORK.md).

## The workflow

![The pathcov pipeline](./pathcov-pipeline.svg)

Input: the entry method, paths to the Java bytecode of the classes and test-classes.

1. Run the test suite with the intellij-coverage-agent and get the coverage as bytecode format.
2. Generate the classes that are in the call graph based on the entry method and the project filter.
3. Convert the coverage data to JSON format. We use the classes of the call graph to remove the coverage data that we don't need. 
4. With that JSON format we can generate the coverage block map. We just map the coverage data to the (interprocedural) CFG.
5. With the coverage block map we can do 2 extra things: calculate the branch coverage and generating the coverage graph for visualization. 

The pathcov pipeline is built on top of SootUp and intellij-coverage-reporter. Normally you would not need to update one of those dependencies. 

## Installation

I use my version of SootUp: sonnyz789123/SootUp v2.0.1 which is **not** on Maven Central. I'll make sure that it is in my local Maven repository. If you run into problems just tell me. 

The project uses Maven as build tool, and for production we bake the dependencies in 1 single JAR. But for development you don't need to do that, I added a flag in the `pom.xml` file to skip that. 

## Development

In the `/Users/yoran.mertens/dev/master-thesis/suts` folder are programs I do testing on. When working on a feature. You can ask me to setup a dev program to test the feature. Then you can confirm your changes are working correctly by testing it on that dev program.

> We moved to ICFG, away from CFG. It could be that we use some methods from the cfg package. But you should not make any updates to the cfg part of the pathcov project. 

## Improvements

In the JDart pipeline I would like a more refined coverage heuristic, I get the coverage data from the pathcov pipeline. The coverage data I generate is based on the CFG. So currently we mark only the CFG blocks (nodes) as

- `FULLY_COVERED`
- `PARTIALLY_COVERED`
- `UNCOVERED`

and give that as the coverage block map to JDart. So we lose information about which edges/branches are covered. I want to keep information about that in the coverage block map, while keeping every other feature working, so only add information. 

On JDart side, we will also need to know which branch we have to take (to follow uncovered paths). There we have control over the bytecode execution, so we know which instruction it is (IF, SWITCH, ...). I suggest we just add information about which branch is covered and what type of branching statement it is. If you think another way is better just tell me. The intellij-coverage-agent already exports this data. You may want to update the intellij-coverage-model (/Users/yoran.mertens/dev/master-thesis/intellij-coverage-model) because that defines the coverage block map, and the intermediate JSON reprsentation. If you can't update it just make a .md file about the changes and I'll assign another agent. 