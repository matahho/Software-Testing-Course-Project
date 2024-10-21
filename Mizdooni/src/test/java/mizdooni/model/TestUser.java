package mizdooni.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestUser {
    private User user;
    private List<Reservation> reservations = new ArrayList<>();
    private Restaurant restaurant;

    @BeforeEach
    public void setupFixtures(){

        user = new User(
                "Username",
                "password@123",
                "test@test.com",
                Mockito.mock(Address.class),
                User.Role.client
        );
        restaurant = Mockito.mock(Restaurant.class);
        for (int i = 0 ; i < 10 ; i++)
            reservations.add(new Reservation(user, restaurant, Mockito.mock(Table.class), LocalDateTime.now()));


    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "", "salamKhobi?", "dwad32312adfwad"})
    public void newUserExists_tryToCheckHisPassword_hisPasswordCheckedCorrectly(String wrong_pass){
        String actualPassword = "password@123";
        assertTrue(user.checkPassword(actualPassword), "Checking password method functionality");
        assertFalse(user.checkPassword(wrong_pass), "Checking password method functionality");
    }

    @Test
    public void newUserExists_tryToAddReservations_reservationAddedCorrectly(){
        for (Reservation reservation : reservations)
            user.addReservation(reservation);
        assertEquals(reservations.size(),user.getReservations().size());

        for (Reservation reservation: user.getReservations())
            assertEquals(9 , user.getReservation(9).getReservationNumber());
    }

    @Test
    public void givenRestaurant_whenCheckIfReserved_thenCorrectReservationStatusReturned() {
        Restaurant differentRestaurant = Mockito.mock(Restaurant.class);
        Reservation reservation = new Reservation(user, restaurant, Mockito.mock(Table.class), LocalDateTime.now().minusDays(1));
        Reservation futureReservation = new Reservation(user, restaurant, Mockito.mock(Table.class), LocalDateTime.now().plusDays(1));

        user.addReservation(reservation);
        user.addReservation(futureReservation);

        assertTrue(user.checkReserved(restaurant), "The user should have a valid past reservation.");
        assertFalse(user.checkReserved(differentRestaurant), "The user should not have a reservation at a different restaurant.");

    }

    @Test
    public void reservationExists_userCanceledTheReservation_notCount(){
        Reservation cancelledReservation = new Reservation(user, restaurant, Mockito.mock(Table.class), LocalDateTime.now().minusDays(1));
        cancelledReservation.cancel();
        user.addReservation(cancelledReservation);
        assertFalse(user.checkReserved(restaurant));

    }


}
