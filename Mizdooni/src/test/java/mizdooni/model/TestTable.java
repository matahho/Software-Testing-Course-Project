package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTable {
    private User user;
    private Restaurant restaurant;
    private Table table;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void setUpFixtures() {
        user = Mockito.mock(User.class);
        restaurant = Mockito.mock(Restaurant.class);
        table = new Table(1, restaurant.getId(), 4);

        reservation1 = new Reservation(user, restaurant, table,LocalDateTime.of(2024, 10, 20, 18, 0));
        reservation2 = new Reservation(user, restaurant, table,LocalDateTime.of(2024, 10, 21, 20, 0));

        table.addReservation(reservation1);
        table.addReservation(reservation2);
    }

    @Test
    public void reservationForATableExists_tryToSeeIfTableIsReserved_reservedCorrectly(){
        assertTrue(table.isReserved(LocalDateTime.of(2024, 10, 20, 18, 0)),
                "Table should be reserved at the time of reservation1.");
    }

    @Test
    void someReservationExists_tryToSeeIfTableIsReservedAtAnotherTime_notReserved() {
        assertFalse(table.isReserved(LocalDateTime.of(2024, 10, 20, 20, 0)),
                "Table should not be reserved at this time.");
    }

    @Test
    void cancelledReservationExists_tryToSeeIfTableIsReserved_notReserved() {
        reservation1.cancel();
        assertFalse(table.isReserved(LocalDateTime.of(2024, 10, 20, 18, 0)),
                "Table should not be reserved when reservation is cancelled.");
    }
}