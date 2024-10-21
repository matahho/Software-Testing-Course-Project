package mizdooni.model;

import ch.qos.logback.core.testUtil.MockInitialContext;
import mizdooni.exceptions.DateTimeInThePast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestRestaurant {
    private Restaurant restaurant;
    private User user;
    private Table table;
    private Review review;
    private Rating rating;

    @BeforeEach
    public void setUpFixtures() {
        rating = new Rating();
        rating.food = 2.5;
        rating.service = 3;
        rating.ambiance = 4;
        rating.overall = 3.5;
        user = Mockito.mock(User.class);
        review = new Review(user, rating, "Test Comment", Mockito.mock(LocalDateTime.class));
        table = Mockito.mock(Table.class);
        restaurant = new Restaurant("Test Restaurant", user, "Test Type", Mockito.mock(LocalTime.class), Mockito.mock(LocalTime.class), "Test Description", Mockito.mock(Address.class), "Test Image");
        restaurant.addTable(table);
        restaurant.addReview(review);
    }

    @Test
    public void aTableExists_aNewTableIsAdded_secondTableWithCorrectNumberIsAdded() {
        Table new_table = new Table(0, 0, 3);
        restaurant.addTable(new_table);
        assertEquals(restaurant.getTables().getLast().getTableNumber(), new_table.getTableNumber());
    }

    @Test
    public void aReviewExists_aNewReviewByTheSameUserIsSubmitted_newerReviewReplacesTheOldOne(){
        Rating new_rating = new Rating();
        new_rating.overall = 1;
        new_rating.food = 2;
        new_rating.service = 3;
        new_rating.ambiance = 4;
        Review new_review = new Review(user, new_rating, "Test Comment", Mockito.mock(LocalDateTime.class));
        restaurant.addReview(new_review);
        assertEquals(restaurant.getReviews().getFirst().getRating().overall, new_review.getRating().overall);
    }

    @Test
    public void tablesExist_lookForAnInvalidTableNumber_nullIsReturned(){
        assertNull(restaurant.getTable(20));
    }

    @Test
    public void oneReviewExists_averageRatingCalculationCalled_averageRatingReturned(){
        Rating avgRating = restaurant.getAverageRating();
        assertEquals(avgRating.overall, rating.overall);
        assertEquals(avgRating.food, rating.food);
        assertEquals(avgRating.ambiance, rating.ambiance);
        assertEquals(avgRating.overall, rating.overall);
    }
}
