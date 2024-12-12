package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionEngineTest {

    private TransactionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TransactionEngine();
    }

    //getAverageTransactionAmountByAccount Tests
    @Test
    public void noTransaction_getAverageTransactionAmountByAccount_zeroReturned() {
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
    public void transactionExist_NewTransactionAdded_nonFraudulent() {
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));

        Transaction txn = new Transaction(3, 1, 450, false);
        assertEquals(0, engine.addTransactionAndDetectFraud(txn));
        assertEquals(3, engine.transactionHistory.size());
    }

    @Test
    public void transactionExist_NewTransactionAdded_Fraudulent() {
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));

        Transaction txn = new Transaction(3, 1, 450, true);
        assertNotEquals(0, engine.addTransactionAndDetectFraud(txn));
        assertEquals(3, engine.transactionHistory.size());
    }

    @Test
    public void noTransaction_getTransactionPatternAboveThreshold_zeroReturned() {
        assertEquals(0, engine.getTransactionPatternAboveThreshold(1));
    }

    @Test
    public void transactionsWithEqualGapsExist_getTransactionPatternWithThreshold_correctDifferenceReturned(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 2, 300, false));

        assertEquals(100, engine.getTransactionPatternAboveThreshold(50));
    }

    @Test
    public void transactionsWithDifferentGapsExist_getTransactionPatternWithThreshold_correctDifferenceReturned(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 2, 400, false));

        assertEquals(0, engine.getTransactionPatternAboveThreshold(101));
    }

    @Test
    public void transactionsWithDifferentGapsExist_getTransactionPatternWithExactThreshold_ZeroReturned(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 2, 400, false));

        assertEquals(0, engine.getTransactionPatternAboveThreshold(400));
    }

    @Test
    public void transactionsExists_newTemporaryTransactionComes_newTransactionDetectedAsSuspicious(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 1, 300, false));

        assertNotEquals(0, engine.detectFraudulentTransaction(new Transaction(4, 1, 500, true)));
    }

    @Test
    public void detectFraudulentTransaction_whenTransactionPatternIsSuspicious_returnsCorrectFraudScore(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 1, 300, false));
        assertEquals(200 , engine.getAverageTransactionAmountByAccount(1));

        assertEquals(0, engine.detectFraudulentTransaction(new Transaction(4, 1, 300, true)));
    }

    @Test
    public void transactionsExists_newFraudulentTransactionComes_newTransactionDetectedAsSuspiciousWithSpecificAmount(){
        engine.transactionHistory.add(new Transaction(1, 1, 100, false));
        engine.transactionHistory.add(new Transaction(2, 1, 200, false));
        engine.transactionHistory.add(new Transaction(3, 1, 300, false));

        assertEquals(100, engine.detectFraudulentTransaction(new Transaction(4, 1, 500, true)));
    }

}
