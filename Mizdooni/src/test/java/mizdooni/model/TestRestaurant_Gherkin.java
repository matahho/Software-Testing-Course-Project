package mizdooni.model;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;


public class TestRestaurant_Gherkin {
    User manager = Mockito.mock(User.class);
    Restaurant restaurant = new Restaurant("testRestaurant-1",manager,"TestType1",LocalTime.of(12,0),LocalTime.of(23,0),"test Descriptions1", Mockito.mock(Address.class), "Test Image1");
    Rating rating;

    @Given("No restaurant's review exists")
    public void no_restaurant_s_review_exists() {
        assertEquals(restaurant.getReviews().size() , 0 );
    }

    @When("Try to get restaurant rating")
    public void try_to_get_restaurant_rating() {
        rating = restaurant.getAverageRating();
    }

    @Then("Null ratings returned")
    public void null_ratings_returned() {
        assertEquals(0 , rating.overall);
        assertEquals(0 , rating.service);
        assertEquals(0 , rating.food);
        assertEquals(0 , rating.ambiance);
        assertEquals(0 , rating.getStarCount());

    }


    @Given("{int} review on the restaurant exist")
    public void review_on_the_restaurant_exist(Integer int1) {
        for (int i = 1 ; i < int1+1 ; i++){
            Rating the_rating = new Rating();
            the_rating.overall = i ; the_rating.food = i ; the_rating.service = i ; the_rating.ambiance = i ;
            Review review = new Review(Mockito.mock(User.class), the_rating, "Comment", LocalDateTime.now());
            restaurant.addReview(review);
        }
    }

    @Then("the average rating for food should be {double}")
    public void the_average_rating_for_food_should_be(Double double1) {
        assertEquals(double1, restaurant.getAverageRating().food);
    }

    @Then("the average rating for service should be {double}")
    public void the_average_rating_for_service_should_be(Double double1) {
        assertEquals(double1, restaurant.getAverageRating().service);
    }

    @Then("the average rating for ambiance should be {double}")
    public void the_average_rating_for_ambiance_should_be(Double double1) {
        assertEquals(double1, restaurant.getAverageRating().ambiance);
    }

    @Then("the average rating for overall should be {double}")
    public void the_average_rating_for_overall_should_be(Double double1) {
        assertEquals(double1, restaurant.getAverageRating().overall);
    }

    @Then("the stars count should be {int}")
    public void the_stars_count_should_be(Integer int1) {
        assertEquals(int1,  restaurant.getAverageRating().getStarCount());
    }

}
