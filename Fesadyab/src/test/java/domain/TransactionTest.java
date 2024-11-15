package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {
    Transaction t1 ;
    Transaction t2 ;
    @BeforeEach
    public void set_up(){
        // Set up T1
        t1 = new Transaction(1, 1, 100, false);
        // Set up T2
        t2 = new Transaction(2, 2, 200, false);

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
