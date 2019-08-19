package com.qinwei.deathnote.tx.support;

import com.qinwei.deathnote.context.annotation.AnnotationOrderComparator;
import com.qinwei.deathnote.tx.datasource.ConnectionHolder;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qinwei
 * @date 2019-08-02
 */
@Slf4j
public class TransactionSynchronizationManager {

    private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> currentTransactionReadOnly = new ThreadLocal<>();

    private static final ThreadLocal<Integer> currentTransactionIsolationLevel = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = new ThreadLocal<>();

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

    public static ConnectionHolder getResource(DataSource dataSource) {
        ConnectionHolder holder = doGetResource(dataSource);
        if (holder != null) {
            log.debug("Retrieved holder [" + holder + "] for key [" + dataSource + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        return holder;
    }

    private static ConnectionHolder doGetResource(DataSource dataSource) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            return null;
        }
        ConnectionHolder value = map.get(dataSource);
        if (value.isVoid()) {
            map.remove(dataSource);
            if (map.isEmpty()) {
                resources.remove();
            }
            value = null;
        }
        return value;
    }

    public static void bindResource(DataSource dataSource, ConnectionHolder connectionHolder) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        ConnectionHolder oldValue = map.put(dataSource, connectionHolder);
        if (oldValue.isVoid()) {
            oldValue = null;
        }
        if (oldValue != null) {
            throw new IllegalStateException("Already value [" + oldValue + "] for key [" + dataSource + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        log.debug("Bound value [" + connectionHolder + "] for key [" + dataSource + "] to thread [" + Thread.currentThread().getName() + "]");
    }

    public static ConnectionHolder unbindResource(DataSource dataSource) {
        ConnectionHolder holder = doUnbindResource(dataSource);
        if (holder == null) {
            throw new IllegalStateException("No value for key [" + dataSource + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        return holder;
    }

    public static ConnectionHolder doUnbindResource(DataSource dataSource) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            return null;
        }
        ConnectionHolder value = map.remove(dataSource);
        if (map.isEmpty()) {
            resources.remove();
        }
        if (value.isVoid()) {
            value = null;
        }
        if (value != null) {
            log.debug("Removed value [" + value + "] for key [" + dataSource + "] from thread [" + Thread.currentThread().getName() + "]");
        }
        return value;
    }
}
