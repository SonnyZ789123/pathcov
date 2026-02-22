package com.kuleuven.blockmap.diff;

import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.blockmap.model.BlockMapDTO;

import java.util.*;

public class BlockHashTreeDiff {

    public static class DiffResult {
        public final Set<String> addedBlockHashes;
        public final Set<String> removedBlockHashes;

        public DiffResult(Set<String> addedBlockHashes, Set<String> removedBlockHashes) {
            this.addedBlockHashes = addedBlockHashes;
            this.removedBlockHashes = removedBlockHashes;
        }
    }

    private final BlockMapDTO previous;
    private final BlockMapDTO current;
    private final BlockLookUp lookUpForPrevious;
    private final BlockLookUp lookUpForCurrent;

    public BlockHashTreeDiff(BlockMapDTO previous, BlockMapDTO current) {
        this.previous = previous;
        this.current = current;
        this.lookUpForPrevious = new BlockLookUp(previous);
        this.lookUpForCurrent = new BlockLookUp(current);
    }

    /**
     * Computes the structural diff between the previous and current CFG.
     *
     * A block is considered unchanged iff:
     *  - It has the same blockHash, and
     *  - Its parent and successor blockHash multisets are identical.
     *
     * Blocks from the previous CFG that cannot be matched structurally
     * are classified as removed. Blocks from the current CFG that remain
     * unmatched are classified as added.
     *
     * @return DiffResult containing added and removed block hashes
     */
    public DiffResult diff() {
        Set<BlockDataDTO> matchedCurrent = new HashSet<>();

        Set<String> removed = new HashSet<>();
        Set<String> added = new HashSet<>();

        // Try to match previous blocks to current blocks
        for (BlockDataDTO prevBlock : lookUpForPrevious.getAllBlocks()) {

            List<BlockDataDTO> candidates =
                    lookUpForCurrent.getBlocksByHash(prevBlock.blockHash);

            BlockDataDTO matched = null;

            for (BlockDataDTO candidate : candidates) {
                if (matchedCurrent.contains(candidate)) continue;

                // If you also want to check the parent/successor hashes, use structurallyEqual instead
                if (prevBlock.blockHash.equals(candidate.blockHash)) {
                    matched = candidate;
                    break;
                }
            }

            if (matched != null) {
                matchedCurrent.add(matched);
            } else {
                removed.add(prevBlock.blockHash);
            }
        }

        // Any current block not matched is added
        for (BlockDataDTO currBlock : lookUpForCurrent.getAllBlocks()) {
            if (!matchedCurrent.contains(currBlock)) {
                added.add(currBlock.blockHash);
            }
        }

        return new DiffResult(added, removed);
    }

    /**
     * Determines whether two blocks are structurally equivalent.
     *
     * Structural equivalence requires:
     *  - Equal blockHash
     *  - Equal multiset of parent block hashes
     *  - Equal multiset of successor block hashes
     *
     * Block IDs are ignored since they are not stable across versions.
     *
     * @param a block from previous CFG
     * @param b block from current CFG
     * @return true if blocks are structurally equivalent
     */
    private boolean structurallyEqual(BlockDataDTO a, BlockDataDTO b) {
        if (!a.blockHash.equals(b.blockHash)) {
            return false;
        }

        List<String> aParentHashes = resolveHashes(a.parentBlockIds, lookUpForPrevious);
        List<String> bParentHashes = resolveHashes(b.parentBlockIds, lookUpForCurrent);

        if (!multisetEquals(aParentHashes, bParentHashes)) {
            return false;
        }

        List<String> aSuccessorHashes = resolveHashes(a.successorBlockIds, lookUpForPrevious);
        List<String> bSuccessorHashes = resolveHashes(b.successorBlockIds, lookUpForCurrent);

        return multisetEquals(aSuccessorHashes, bSuccessorHashes);
    }

    /**
     * Resolves a list of block IDs to their corresponding block hashes.
     *
     * The lookup is version-specific and ID-based. Null lookups are ignored.
     *
     * @param blockIds list of parent or successor block IDs
     * @param lookup   lookup instance for the corresponding CFG version
     * @return list of resolved block hashes (order preserved)
     */
    private List<String> resolveHashes(List<Integer> blockIds, BlockLookUp lookup) {
        List<String> hashes = new ArrayList<>();

        for (Integer id : blockIds) {
            BlockDataDTO block = lookup.getBlockDataById(id);
            if (block != null) {
                hashes.add(block.blockHash);
            }
        }

        return hashes;
    }

    /**
     * Compares two lists as multisets.
     *
     * Returns true iff both lists contain the same elements with identical
     * multiplicities. Element order is ignored.
     *
     * @param a first list
     * @param b second list
     * @return true if both lists represent the same multiset
     */
    private boolean multisetEquals(List<String> a, List<String> b) {
        if (a.size() != b.size()) {
            return false;
        }

        Map<String, Integer> counts = new HashMap<>();

        // Create a frequency map for list a
        // a = [A, B, B, C]
        // A → 1
        // B → 2
        // C → 1
        for (String s : a) {
            counts.merge(s, 1, Integer::sum);
        }

        for (String s : b) {
            Integer count = counts.get(s);
            if (count == null) return false;

            if (count == 1) {
                counts.remove(s);
            } else {
                counts.put(s, count - 1);
            }
        }

        return counts.isEmpty();
    }
}
