package mizdooni.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.exceptions.DuplicatedRestaurantName;
import mizdooni.exceptions.UserNotManager;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static mizdooni.controllers.ControllerUtils.PLACEHOLDER_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    @Test void someRestaurantExists_tryToCreateNewOne_successfullyCreated() throws Exception{
        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "09:00",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "image", "http://image.url/pizza.jpg",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );

        when(service.addRestaurant(
                eq("Pizza Palace"),
                eq("Pizza"),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(22, 0)),
                eq("Best pizza in town"),
                any(Address.class),
                eq("http://image.url/pizza.jpg")
        )).thenReturn(1);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(1));
    }


    @Test void someRestaurantExists_tryToCreateNewOneWithOutImage_successfullyCreatedWithImagePlaceholder() throws Exception{

        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "09:00",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );

        when(service.addRestaurant(
                eq("Pizza Palace"),
                eq("Pizza"),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(22, 0)),
                eq("Best pizza in town"),
                any(Address.class),
                eq(PLACEHOLDER_IMAGE)
        )).thenReturn(3);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant added"))
                .andExpect(jsonPath("$.data").value(3));


    }


    @Test
    public void someRestaurantExists_tryToCreateNewOneWithMissingParam_failed() throws Exception{

        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza"
        );

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("parameters missing"));

    }

    @Test
    public void someRestaurantExists_tryToCreateNewOneWithInvalidParamType_failed() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "invalid-time",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("bad parameter type"));
    }


    @Test
    public void someExists_tryToAddRestaurantButServiceException_failure() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "09:00",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "image", "http://image.url/pizza.jpg",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );

        when(service.addRestaurant(
                anyString(), anyString(), any(LocalTime.class), any(LocalTime.class),
                anyString(), any(Address.class), anyString()
        )).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));
    }
    @Test
    public void someExists_tryToCreateOneWithDuplicateName_failed() throws Exception{

        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "09:00",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );
        when(service.addRestaurant(anyString(), anyString(), any(), any(), anyString(), any(Address.class), anyString()))
                .thenThrow(new DuplicatedRestaurantName());

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("DuplicatedRestaurantName"));
    }

    @Test
    void someRestaurantsExists_userNotManagerTryToCreateOne_throwsException() throws Exception {
        when(service.addRestaurant(anyString(), anyString(), any(), any(), anyString(), any(Address.class), anyString()))
                .thenThrow(new UserNotManager());

        Map<String, Object> request = Map.of(
                "name", "Pizza Palace",
                "type", "Pizza",
                "startTime", "09:00",
                "endTime", "22:00",
                "description", "Best pizza in town",
                "address", Map.of(
                        "country", "USA",
                        "city", "New York",
                        "street", "5th Avenue"
                )
        );

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not a manager."));
    }

    @Test
    void noRestaurantExists_tryToValidateNewName_nameIsAvailable() throws Exception {
        when(service.restaurantExists(anyString())).thenReturn(false);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "Pizza Place"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant name is available"));
    }

    @Test
    void aRestaurantExists_tryToValidateNewName_nameAlreadyExists() throws Exception {
        when(service.restaurantExists(anyString())).thenReturn(true);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", "Pizza Place"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("restaurant name is taken"));
    }

    @Test
    void someRestaurantsExist_callRestaurantTypes_restaurantTypesReturned() throws Exception {
        Set<String> restaurantTypes = Set.of("Persian", "Chinese", "Mexican");

        when(service.getRestaurantTypes()).thenReturn(restaurantTypes);

        mockMvc.perform(get("/restaurants/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant types"))
                .andExpect(jsonPath("$.data.length()").value(restaurantTypes.size()))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "Mexican", "Persian", "Chinese"
                )));
    }

    @Test
    void someRestaurantsExist_tryToFindRestaurantWithSomeManager_restaurantOfManagerReturned() throws Exception {
        List<Restaurant> restaurants = List.of(r1, r2);
        when(service.getManagerRestaurants(manager.getId())).thenReturn(restaurants);

        mockMvc.perform(get("/restaurants/manager/{managerId}", manager.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("manager restaurants listed"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(restaurants.size()))
                .andExpect(jsonPath("$.data[0].name").value("testRestaurant-1"))
                .andExpect(jsonPath("$.data[0].type").value("TestType1"))
                .andExpect(jsonPath("$.data[0].description").value("test Descriptions1"))
                .andExpect(jsonPath("$.data[1].name").value("testRestaurant-2"))
                .andExpect(jsonPath("$.data[1].type").value("TestType2"))
                .andExpect(jsonPath("$.data[1].description").value("test Descriptions2"));
    }

    @Test
    void getRestaurantLocations_returnsLocationsSuccessfully() throws Exception {
        Map<String, Set<String>> locations = Map.of(
                "USA", Set.of("New York", "Los Angeles"),
                "Canada", Set.of("Toronto", "Vancouver")
        );

        when(service.getRestaurantLocations()).thenReturn(locations);

        mockMvc.perform(get("/restaurants/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurant locations"))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.USA").isArray())
                .andExpect(jsonPath("$.data.USA.length()").value(2))
                .andExpect(jsonPath("$.data.USA").value(org.hamcrest.Matchers.containsInAnyOrder("New York", "Los Angeles")))
                .andExpect(jsonPath("$.data.Canada").isArray())
                .andExpect(jsonPath("$.data.Canada.length()").value(2))
                .andExpect(jsonPath("$.data.Canada").value(org.hamcrest.Matchers.containsInAnyOrder("Toronto", "Vancouver")));
    }

}
