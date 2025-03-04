package cz.dearfuture.utils;

import cz.dearfuture.models.Capsule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multi-Threaded Merge Sort for sorting Capsules.
 */
public class MultiThreadedMergeSort {

    /**
     * Sorts a list of capsules using multi-threaded merge sort.
     *
     * @param capsules The list of capsules to sort.
     * @param comparator The comparator to determine the sorting order.
     * @return A sorted list of capsules.
     */
    public static List<Capsule> sort(List<Capsule> capsules, Comparator<Capsule> comparator) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Capsule> sortedList = mergeSort(capsules, comparator, executor);
        executor.shutdown();
        return sortedList;
    }

    private static List<Capsule> mergeSort(List<Capsule> capsules, Comparator<Capsule> comparator, ExecutorService executor) {
        if (capsules.size() <= 1) {
            return capsules;
        }

        int mid = capsules.size() / 2;
        List<Capsule> leftPart = new ArrayList<>(capsules.subList(0, mid));
        List<Capsule> rightPart = new ArrayList<>(capsules.subList(mid, capsules.size()));

        Future<List<Capsule>> leftFuture = executor.submit(() -> mergeSort(leftPart, comparator, executor));
        Future<List<Capsule>> rightFuture = executor.submit(() -> mergeSort(rightPart, comparator, executor));

        List<Capsule> sortedLeft;
        List<Capsule> sortedRight;

        try {
            sortedLeft = leftFuture.get();
            sortedRight = rightFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return capsules; // Fallback to original list in case of failure
        }

        return merge(sortedLeft, sortedRight, comparator);
    }

    /**
     * Merges two sorted lists into a single sorted list.
     *
     * @param left The left sorted sublist.
     * @param right The right sorted sublist.
     * @param comparator The comparator to determine the sorting order.
     * @return A merged and sorted list.
     */
    private static List<Capsule> merge(List<Capsule> left, List<Capsule> right, Comparator<Capsule> comparator) {
        List<Capsule> merged = new ArrayList<>();
        int leftIndex = 0, rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (comparator.compare(left.get(leftIndex), right.get(rightIndex)) <= 0) {
                merged.add(left.get(leftIndex++));
            } else {
                merged.add(right.get(rightIndex++));
            }
        }

        while (leftIndex < left.size()) {
            merged.add(left.get(leftIndex++));
        }

        while (rightIndex < right.size()) {
            merged.add(right.get(rightIndex++));
        }

        return merged;
    }
}
