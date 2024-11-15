package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {
    Transaction t1 = new Transaction();
    Transaction t2 = new Transaction();
    @BeforeEach
    public void set_up(){
        // Set up T1
        t1.transactionId = 1;
        t1.accountId = 1;
        t1.amount = 100;
        t1.isDebit = false;

        // Set up T2
        t2.transactionId = 2;
        t2.accountId = 2;
        t2.amount = 200;
        t2.isDebit = false;

    }
    @Test
    public void twoTransactionExistsWithDifferentID_checkIfTheyAreEqual_returnedNotEqual(){
        assertFalse(t1.equals(t2));
    }
    @Test
    public void twoTransactionExistsWithSameID_checkIfTheyAreEqual_returnedEqual(){
        t2.transactionId = t1.transactionId;
        assertTrue(t1.equals(t2));
    }
    @Test
    public void oneTransactionExists_checkIfItsEqualWithAnotherOBJ_returnedNotEqual(){
        assertFalse(t1.equals(new String("Mahdi")));
    }

}
