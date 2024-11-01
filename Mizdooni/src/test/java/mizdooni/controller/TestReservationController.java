package mizdooni.controller;

import mizdooni.controllers.ControllerUtils;
import mizdooni.controllers.ReservationController;
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
import java.util.Collections;
import java.util.List;

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
        LocalDate date = LocalDate.of(2024, 10, 26);

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


}
