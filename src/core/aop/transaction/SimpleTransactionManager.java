package core.aop.transaction;

import java.util.UUID;

/**
 * Demo TransactionManager.
 *
 * Uses ThreadLocal to simulate "current transaction".
 * In a real implementation you would:
 * - obtain a JDBC Connection
 * - set autoCommit=false
 * - bind connection to thread
 * - commit/rollback
 */
public class SimpleTransactionManager implements TransactionManagerInterface {

    private final ThreadLocal<TransactionStatus> current = new ThreadLocal<>();

    @Override
    public TransactionStatus begin() {
        TransactionStatus existing = current.get();
        if (existing != null && !existing.isCompleted()) {
            // Very simple behavior: treat as "join existing".
            // Later you can implement propagation.
            System.out.println("[TX] join existing tx=" + existing.getId());
            return existing;
        }

        TransactionStatus status = new TransactionStatus(UUID.randomUUID().toString());
        current.set(status);
        System.out.println("[TX] begin tx=" + status.getId());
        return status;
    }

    @Override
    public void commit(TransactionStatus status) {
        if (status == null || status.isCompleted()) return;
        System.out.println("[TX] commit tx=" + status.getId());
        status.markCompleted();
        current.remove();
    }

    @Override
    public void rollback(TransactionStatus status) {
        if (status == null || status.isCompleted()) return;
        System.out.println("[TX] rollback tx=" + status.getId());
        status.markCompleted();
        current.remove();
    }

    public TransactionStatus getCurrent() {
        return current.get();
    }
}
