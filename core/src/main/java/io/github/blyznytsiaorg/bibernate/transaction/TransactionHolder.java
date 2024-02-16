package io.github.blyznytsiaorg.bibernate.transaction;


/**
 * The {@code TransactionHolder} class is a utility for managing thread-local transactions.
 * It uses a {@code ThreadLocal} variable to store and retrieve transactions associated with a particular thread.
 * This class is designed to be used in scenarios where transactional information needs to be accessible
 * within the scope of a single thread.
 * <p>
 *     The class provides methods for getting, setting, and removing transactions associated with the current thread.
 * </p>
 * <p>
 *     The class uses a {@code ThreadLocal} variable to ensure that each thread has its own isolated transaction reference,
 *     preventing interference between threads.
 * </p>
 *
 * @see Transaction
 * @see ThreadLocal
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class TransactionHolder {

    /**
     * A {@code ThreadLocal} variable for storing transactions associated with a specific thread.
     */
    private static final ThreadLocal<Transaction> transactionHolder = new ThreadLocal<>();

    /**
     * Retrieves the transaction associated with the current thread, if any.
     *
     * @return The transaction associated with the current thread, or {@code null} if no transaction is associated
     */
    public static Transaction getTransaction() {
        return transactionHolder.get();
    }

    /**
     * Sets the transaction for the current thread, allowing transactions to be associated
     * with the execution context of a thread.
     *
     * @param transaction The transaction to associate with the current thread
     */
    public static void setTransaction(Transaction transaction) {
        transactionHolder.set(transaction);
    }

    /**
     * Removes the transaction association for the current thread.
     * This method is typically used to clean up resources associated with a thread-local transaction.
     */
    public static void removeTransaction() {
        transactionHolder.remove();
    }
}
