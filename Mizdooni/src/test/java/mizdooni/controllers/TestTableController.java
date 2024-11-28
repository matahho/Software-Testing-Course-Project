package mizdooni.controllers;

import mizdooni.exceptions.RestaurantNotFound;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.model.User;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TableController.class)
public class TestTableController {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private TableService tableService;
    @MockBean
    private RestaurantService restaurantService;

    @Test
    void twoTablesInARestaurantExists_getTablesCalledForRestaurant_tablesReturned() throws Exception {
        when(restaurantService.getRestaurant(anyInt())).thenReturn(mock(Restaurant.class));
        when(tableService.getTables(anyInt())).thenReturn(List.of(
                new Table(0, 0, 1),
                new Table(1, 0, 2)
        ));

        this.mockMvc.perform(get("/tables/{restaurantId}", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data[0].tableNumber").value(0))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(1))
                .andExpect(jsonPath("$.data[1].tableNumber").value(1))
                .andExpect(jsonPath("$.data[1].seatsNumber").value(2));
    }

    @Test
    void tablesExist_getTablesCalledWithWrongRestaurantId_restaurantNotFound() throws Exception {
        when(restaurantService.getRestaurant(anyInt())).thenReturn(mock(Restaurant.class));
        when(tableService.getTables(anyInt())).thenThrow(new RestaurantNotFound());

        this.mockMvc.perform(get("/tables/{restaurantId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty()) // Ensuring timestamp is included
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("RestaurantNotFound"))
                .andExpect(jsonPath("$.message").value("Restaurant not found.")); //Complete this call

    }

    @Test
    void aRestaurantExists_addTableWithValidInput_ReturnSuccessResponse() throws Exception {
        String requestBody = "{\"seatsNumber\":\"4\"}";
        User manager = new User("Manager1", "TestManagerPassword", "manager@example.com", new Address("TestCountry", "TestCity", null), User.Role.manager);
        Restaurant r1 = new Restaurant("testRestaurant-1", manager,"TestType1", LocalTime.of(12,0),LocalTime.of(23,0),"test Descriptions1", Mockito.mock(Address.class), "Test Image1");

        when(restaurantService.getRestaurant(anyInt())).thenReturn(r1);

        // Mocking addTable to not throw any exception
        doNothing().when(tableService).addTable(anyInt(), anyInt());

        this.mockMvc.perform(post("/tables/{restaurantId}", r1.getId())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("table added"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void aRestaurantExists_addTableWithMissingParams_ReturnBadRequestParamsMissing() throws Exception {
        String requestBody = "{\"wrongKey\":\"4\"}";

        when(restaurantService.getRestaurant(anyInt())).thenReturn(mock(Restaurant.class));

        this.mockMvc.perform(post("/tables/{restaurantId}", 1)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("parameters missing"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void aRestaurantExists_addTableWithWrongTypeParams_ReturnBadRequestParamsBadType() throws Exception {
        String requestBody = "{\"seatsNumber\":\"test\"}";

        when(restaurantService.getRestaurant(anyInt())).thenReturn(mock(Restaurant.class));

        this.mockMvc.perform(post("/tables/{restaurantId}", 1)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad parameter type"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

}
