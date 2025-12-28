package core.aop.transaction;

/**
 * Minimal transaction SPI.
 *
 * In a real system this would be tied to a DataSource/Connection.
 * For your mini-spring, start with this small interface.
 */
public interface TransactionManagerInterface {

    TransactionStatus begin();

    void commit(TransactionStatus status);

    void rollback(TransactionStatus status);

    /**
     * Simple holder to keep transaction state.
     */
    class TransactionStatus {
        private final String id;
        private boolean completed;

        public TransactionStatus(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void markCompleted() {
            this.completed = true;
        }
    }
}
