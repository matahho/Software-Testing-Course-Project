package domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    int transactionId;
    int accountId;
    int amount;
    boolean isDebit;

    public Transaction(int transactionId, int accountId, int amount, boolean isDebit) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.isDebit = isDebit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction transaction) {
            return transactionId == transaction.transactionId;
        }
        return false;
    }
}
