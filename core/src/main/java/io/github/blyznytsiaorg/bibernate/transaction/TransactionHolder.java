package io.github.blyznytsiaorg.bibernate.transaction;


public class TransactionHolder {

  private static final ThreadLocal<Transaction> transactionHolder = new ThreadLocal<>();

  public static Transaction getTransaction() {
    return transactionHolder.get();
  }

  public static void setTransaction(Transaction transaction) {
    transactionHolder.set(transaction);
  }

  public static void removeTransaction() {
    transactionHolder.remove();
  }


}
