package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionEngineTest {

    private TransactionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TransactionEngine();
    }

    //getAverageTransactionAmountByAccount Tests
    @Test
    void noTransaction_getAverageTransactionAmountByAccount_zeroReturned() {
        assertEquals(0, engine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    public void someTransactionsExists_tryToGetAvgAmount_avgAmountReturnedCorrectly(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 2, 300, false));

        assertEquals(150, engine.getAverageTransactionAmountByAccount(1));
    }

    //addTransactionAndDetectFraud Tests
    @Test
    public void transactionExists_tryToAddSameTransaction_noTransactionAdded(){
        var txn = new Transaction(1, 1 , 100, false);
        engine.transactionHistory.add(txn);
        assertEquals(1, engine.transactionHistory.size());
        assertEquals(0 , engine.addTransactionAndDetectFraud(txn));
        assertEquals(1, engine.transactionHistory.size());
    }

    @Test
    public void transactionExist_NewTransactionAdded_Fraudulent() {
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));

        Transaction txn = new Transaction(3, 1, 450, false);
        assertEquals(0, engine.addTransactionAndDetectFraud(txn));
        assertEquals(3, engine.transactionHistory.size());
    }
}
