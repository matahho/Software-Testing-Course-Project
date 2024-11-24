package mizdooni.controllers;

import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import mizdooni.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    void aTableInARestaurantExists_getTablesCalledForRestaurant_tableReturned() throws Exception {
        when(restaurantService.getRestaurant(anyInt())).thenReturn(mock(Restaurant.class));
        when(tableService.getTables(anyInt())).thenReturn(List.of(
                new Table(0, 0, 1),
                new Table(1, 0, 2)
        ));

        this.mockMvc.perform(get("/tables/{restaurantId}", 0))
                .andExpect(status().isOk()) // Assert HTTP 200 status
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data[0].tableNumber").value(0))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(1))
                .andExpect(jsonPath("$.data[1].tableNumber").value(1))
                .andExpect(jsonPath("$.data[1].seatsNumber").value(2));
    }
}
