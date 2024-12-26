package mizdooni.model;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TestUser_Gherkin {
    User user ;
    Restaurant restaurant = Mockito.mock(Restaurant.class);
    Table table = Mockito.mock(Table.class);
    Reservation reservation ;

    // Scenario 1 and 2

    @Given("No reservation exists for a user")
    public void no_reservation_exists_for_a_user() {
        user = new User("testUser", "testPassword", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        reservation = new Reservation(user, restaurant, table, LocalDateTime.now());
        assertEquals(user.getReservations().size() , 0);
    }

    @When("New reservation assign to the user")
    public void new_reservation_assign_to_the_user() {
        user.addReservation(reservation);
    }

    @Then("The reservation added to user's reservations")
    public void the_reservation_added_to_user_s_reservations() {
        assertEquals(user.getReservations().size() , 1);
        assertEquals(user.getReservations().getFirst(), reservation);
    }

    @Then("The reservation number assigned based on reservation counter correctly")
    public void the_reservation_number_assigned_based_on_reservation_counter_correctly() {
        assertEquals(user.getReservation(0), reservation);
        assertEquals(reservation.getReservationNumber() , 0 );
    }


    // Scenario 3


    @Given("{int} reservations exists for a user")
    public void reservations_exists_for_a_user(Integer int1) {
        user = new User("testUser", "testPassword", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        for (int i =0 ; i < int1 ; i++){
            user.addReservation(new Reservation(user, restaurant, table, LocalDateTime.now()));
        }

        reservation = new Reservation(user, restaurant, table, LocalDateTime.now());
    }


    @Then("The new reservation number assigned based on reservation counter to {int}")
    public void the_new_reservation_number_assigned_based_on_reservation_counter_to(Integer int1) {
        assertEquals(reservation,user.getReservation(int1));
        assertEquals(int1 + 1, user.getReservations().size() );
        assertEquals(int1 , reservation.getReservationNumber());

    }


}
