package mizdooni.controllers;


import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.User;
import mizdooni.service.RestaurantService;
import mizdooni.service.ServiceUtils;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RestaurantController.class)
public class TestRestaurantController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RestaurantService service;


    private User manager;
    private Restaurant r1 , r2;

    @BeforeEach
    public void setUp(){
        manager = new User("Manager1", "TestManagerPassword", "manager@example.com", new Address("TestCountry", "TestCity", null), User.Role.manager);
        r1 = new Restaurant("testRestaurant-1", manager,"TestType1", LocalTime.of(12,0),LocalTime.of(23,0),"test Descriptions1", Mockito.mock(Address.class), "Test Image1");
        r2 = new Restaurant("testRestaurant-2", manager,"TestType2", LocalTime.of(18,0),LocalTime.of(22,0),"test Descriptions2", Mockito.mock(Address.class), "Test Image2");
    }


    @Test
    public void someRestaurantsExist_tryToRetrieveOne_retrievedCorrectly() throws Exception {
        when(service.getRestaurant(anyInt())).thenReturn(r1);
        mockMvc.perform(get("/restaurants/{restaurantId}",1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant found"))
                .andExpect(jsonPath("$.data.name").value(r1.getName()))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.type").value(r1.getType()))
                .andExpect(jsonPath("$.data.startTime").value(r1.getStartTime().toString()))
                .andExpect(jsonPath("$.data.endTime").value(r1.getEndTime().toString()))
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.address").exists())
                .andExpect(jsonPath("$.data.averageRating").exists())
                .andExpect(jsonPath("$.data.maxSeatsNumber").exists())
                .andExpect(jsonPath("$.data.starCount").exists())
                .andExpect(jsonPath("$.data.managerUsername").value(manager.getUsername()))
                .andExpect(jsonPath("$.data.image").exists())
                .andExpect(jsonPath("$.data.totalReviews").exists());

    }

    @Test
    public void someRestaurantExists_tryToRetrieveARestaurantWithInvalidID_notFoundReturned() throws Exception {
        mockMvc.perform(get("/restaurants/{restaurantId}",2002))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    @Test
    public


}
