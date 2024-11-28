package mizdooni.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.exceptions.DuplicatedRestaurantName;
import mizdooni.exceptions.UserNotManager;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.User;
import mizdooni.response.PagedList;
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
    public void someRestaurantsExist_tryToListAllWithoutAnyFilter_allListed() throws Exception{
        PagedList<Restaurant> pagedList = new PagedList<>(List.of(r1, r2), 1, 10);
        when(service.getRestaurants(anyInt(), any())).thenReturn(pagedList);

        mockMvc.perform(get("/restaurants").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.pageList[0].id").value(0))
                .andExpect(jsonPath("$.data.pageList[0].name").value("testRestaurant-1"))
                .andExpect(jsonPath("$.data.pageList[0].type").value("TestType1"))
                .andExpect(jsonPath("$.data.pageList[0].startTime").value("12:00"))
                .andExpect(jsonPath("$.data.pageList[0].endTime").value("23:00"))
                .andExpect(jsonPath("$.data.pageList[0].description").value("test Descriptions1"))
                .andExpect(jsonPath("$.data.pageList[0].managerUsername").value("Manager1"))
                .andExpect(jsonPath("$.data.pageList[0].image").value("Test Image1"))
                .andExpect(jsonPath("$.data.pageList[0].maxSeatsNumber").value(1))
                .andExpect(jsonPath("$.data.pageList[0].starCount").value(0))
                .andExpect(jsonPath("$.data.pageList[1].id").value(1))
                .andExpect(jsonPath("$.data.pageList[1].name").value("testRestaurant-2"))
                .andExpect(jsonPath("$.data.pageList[1].type").value("TestType2"))
                .andExpect(jsonPath("$.data.pageList[1].startTime").value("18:00"))
                .andExpect(jsonPath("$.data.pageList[1].endTime").value("22:00"))
                .andExpect(jsonPath("$.data.pageList[1].description").value("test Descriptions2"))
                .andExpect(jsonPath("$.data.pageList[1].managerUsername").value("Manager1"))
                .andExpect(jsonPath("$.data.pageList[1].image").value("Test Image2"))
                .andExpect(jsonPath("$.data.pageList[1].maxSeatsNumber").value(1))
                .andExpect(jsonPath("$.data.pageList[1].starCount").value(0))
                .andDo(print());

    }

    @Test
    public void someRestaurantsExist_tryToListAllWithSingleObjectPaginator_paginatedCorrectly() throws Exception {
        List<Restaurant> restaurants = List.of(r1, r2);

        // Test Page 1
        PagedList<Restaurant> page1 = new PagedList<>(restaurants, 1, 1);
        when(service.getRestaurants(eq(1), any())).thenReturn(page1);

        mockMvc.perform(get("/restaurants").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(true)) // More pages exist
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.pageList[0].id").value(0))
                .andExpect(jsonPath("$.data.pageList[0].name").value("testRestaurant-1"));

        // Test Page 2
        PagedList<Restaurant> page2 = new PagedList<>(restaurants, 2, 1);
        when(service.getRestaurants(eq(2), any())).thenReturn(page2);

        mockMvc.perform(get("/restaurants").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("restaurants listed"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false)) // No more pages
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.pageList[0].id").value(1))
                .andExpect(jsonPath("$.data.pageList[0].name").value("testRestaurant-2"));

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

}
