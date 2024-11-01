package mizdooni.controllers;

import mizdooni.exceptions.ReservationCannotBeCancelled;
import mizdooni.exceptions.UserNotManager;
import mizdooni.model.*;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TestReservationController {
    @Mock
    ReservationService reserveService;
    @Mock
    RestaurantService restaurantService;
    @InjectMocks
    ReservationController reservationController;

    private User client;
    private User manager;
    private Restaurant restaurant;
    private List<Table> tables;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        client = new User("Mahdi", "1234Password", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        manager = new User("Manager1", "TestManagerPassword", "manager@example.com", new Address("TestCountry", "TestCity", null), User.Role.manager);
        restaurant = new Restaurant("testRestaurant", manager,"TestType", LocalTime.of(12,0),LocalTime.of(23,0),"test Descriptions", Mockito.mock(Address.class), "Test Image");
        tables = List.of(
                new Table(1, restaurant.getId(), 2),
                new Table(2, restaurant.getId(),5),
                new Table(3, restaurant.getId(), 10)
        );
    }




    @Test
    public void reservationsExist_managerTryToGetCurrentReservations_retrievedCorrectly() throws Exception{
        List<Reservation> mockReservations = List.of(new Reservation(client,restaurant,tables.get(0),LocalDateTime.now()));
        // I had to use eq() because of null date
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.getReservations(eq(restaurant.getId()), eq(1),any())).thenReturn(mockReservations);

        Response response = reservationController.getReservations(restaurant.getId(), 1,null);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("restaurant table reservations", response.getMessage());
        assertEquals(mockReservations, response.getData());


        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reserveService).getReservations(eq(restaurant.getId()), eq(1), any());
    }

    @Test
    public void reservationExist_nonManagerUserTryToGetAllRestaurantReservations_notAllowed() throws Exception{
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        doThrow(new UserNotManager()).when(reserveService).getReservations(anyInt(), anyInt(), any(LocalDate.class));

        ResponseException exception = assertThrows(ResponseException.class,()->reservationController.getReservations(restaurant.getId(), 1, null));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatus());
        assertEquals("UserNotManager", exception.getError());

        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reserveService).getReservations(anyInt(), anyInt(), any(LocalDate.class));
    }

    @Test
    public void reservationTimesAvailable_userAsksForAvailableTimes_availableTimesShown() throws Exception{
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.getAvailableTimes(anyInt(), anyInt(), any(LocalDate.class))).thenReturn(List.of());

        Response response = reservationController.getAvailableTimes(restaurant.getId(), 1,LocalDate.now().toString());

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("available times", response.getMessage());

        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reserveService).getAvailableTimes(anyInt(), eq(1), any(LocalDate.class));
    }

    @Test
    public void noReservationsExist_userAddsReservation_reservationSuccessfullyAdded() throws Exception{
        Reservation mockReservation = new Reservation(client, restaurant, tables.getFirst(), LocalDateTime.now());
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("people", "2");
        reservationParams.put("datetime", "2024-04-08 12:30");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reserveService.reserveTable(anyInt(), anyInt(), any(LocalDateTime.class))).thenReturn(mockReservation);

        Response response = reservationController.addReservation(restaurant.getId(), reservationParams);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reservation done", response.getMessage());

        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reserveService).reserveTable(eq(restaurant.getId()), eq(2), any(LocalDateTime.class));
    }

    @Test
    public void reservationsExist_userCancelsReservation_reservationSuccessfullyCancelled() throws Exception{
        Reservation mockReservation = new Reservation(client, restaurant, tables.getFirst(), LocalDateTime.now().plusDays(1));
        doAnswer(invocation -> {
            mockReservation.cancel();
            return null;
        }).when(reserveService).cancelReservation(mockReservation.getReservationNumber());
        Response response = reservationController.cancelReservation(mockReservation.getReservationNumber());


        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(mockReservation.isCancelled());

        verify(reserveService).cancelReservation(mockReservation.getReservationNumber());
    }

    @Test
    public void reservationsExist_userCancelsReservationAfterReservedTime_fails() throws Exception{
        Reservation mockReservation = new Reservation(client, restaurant, tables.getFirst(), LocalDateTime.now().minusDays(1));
        doThrow(new ReservationCannotBeCancelled()).when(reserveService).cancelReservation(mockReservation.getReservationNumber());

        ResponseException exception = assertThrows(ResponseException.class, ()->reservationController.cancelReservation(mockReservation.getReservationNumber()));

        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatus());
        assertEquals("ReservationCannotBeCancelled", exception.getError());

        verify(reserveService).cancelReservation(mockReservation.getReservationNumber());
    }

}
