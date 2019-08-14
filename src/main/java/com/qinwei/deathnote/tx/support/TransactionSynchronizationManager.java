package com.qinwei.deathnote.tx.support;

import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-08-02
 */
public class TransactionSynchronizationManager {

    private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> currentTransactionReadOnly = new ThreadLocal<>();

    private static final ThreadLocal<Integer> currentTransactionIsolationLevel = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

    public static boolean isSynchronizationActive() {
        return synchronizations.get() != null;
    }

    public static void initSynchronization() throws IllegalStateException {
        if (isSynchronizationActive()) {
            throw new IllegalStateException("Cannot activate transaction synchronization - already active");
        }
        synchronizations.set(new LinkedHashSet<>());
    }

    public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
        Set<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("Transaction synchronization is not active");
        }
        if (synchs.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
            AnnotationOrderComparator.sort(sortedSynchs);
            return Collections.unmodifiableList(sortedSynchs);
        }
    }

    public static void registerSynchronization(TransactionSynchronization synchronization)
            throws IllegalStateException {
        Set<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs == null) {
            throw new IllegalStateException("Transaction synchronization is not active");
        }
        synchs.add(synchronization);
    }

    public static void clearSynchronization() throws IllegalStateException {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
        }
        synchronizations.remove();
    }

    public static boolean isCurrentTransactionReadOnly() {
        return (currentTransactionReadOnly.get() != null);
    }

    public static void setCurrentTransactionReadOnly(boolean readOnly) {
        currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
    }

    public static Integer getCurrentTransactionIsolationLevel() {
        return currentTransactionIsolationLevel.get();
    }

    public static void setCurrentTransactionIsolationLevel(Integer isolationLevel) {
        currentTransactionIsolationLevel.set(isolationLevel);
    }

    public static boolean isActualTransactionActive() {
        return (actualTransactionActive.get() != null);
    }

    public static void setActualTransactionActive(boolean active) {
        actualTransactionActive.set(active ? Boolean.TRUE : null);
    }

    public static void clear() {
        synchronizations.remove();
        currentTransactionReadOnly.remove();
        currentTransactionIsolationLevel.remove();
        actualTransactionActive.remove();
    }
}
