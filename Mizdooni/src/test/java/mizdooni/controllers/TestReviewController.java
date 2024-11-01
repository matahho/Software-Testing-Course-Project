package mizdooni.controllers;

import mizdooni.model.*;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestReviewController {
    @Mock
    private ReviewService reviewService;
    @Mock
    private RestaurantService restaurantService;
    @InjectMocks
    private ReviewController reviewController;

    private User user, manager;
    private Restaurant restaurant;
    private Review review;
    private Map<String, Object> params;
    private Map<String, Double> ratings;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = mock(User.class);
        manager = mock(User.class);
        restaurant = new Restaurant("testRestaurant", manager,"TestType", LocalTime.of(12,0),LocalTime.of(23,0),"test Descriptions", Mockito.mock(Address.class), "Test Image");
        review = new Review(manager, mock(Rating.class), "testReview", LocalDateTime.now());
        restaurant.addReview(review);

        params = new HashMap<>();
        ratings = new HashMap<>();
        ratings.put("food", 1.5);
        ratings.put("service", 2.5);
        ratings.put("ambiance", 4.0);
        ratings.put("overall", 3.5);
        params.put("rating", ratings);
        params.put("comment", "testReview2");
    }

    @Test
    public void someReviewsExist_reviewRetrievalCalled_reviewsRetrieved() throws Exception {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reviewService.getReviews(eq(restaurant.getId()), eq(1))).thenReturn(new PagedList<>(List.of(review), 1, 1));

        Response response = reviewController.getReviews(restaurant.getId(), 1);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("reviews for restaurant (" + restaurant.getId() + "): " + restaurant.getName(), response.getMessage());

        verify(reviewService).getReviews(eq(restaurant.getId()), eq(1));
        verify(restaurantService).getRestaurant(eq(restaurant.getId()));
    }

    @Test
    public void noReviewsExist_aReviewsIsSubmitted_newReviewAddedSuccessfully() throws Exception {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        Rating newRating = new Rating();
        newRating.overall = 3.5;
        newRating.food = 1.5;
        newRating.ambiance = 4.0;
        newRating.service = 2.5;

        Review new_review = new Review(user, newRating, "testReview2", LocalDateTime.now());

        doAnswer(invocationOnMock -> {
            restaurant.addReview(new_review);
            return null;
        }).when(reviewService).addReview(restaurant.getId(), newRating, new_review.getComment());

        Response response = reviewController.addReview(restaurant.getId(), params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());

        verify(reviewService).addReview(eq(restaurant.getId()), any(), eq(new_review.getComment()));
        verify(restaurantService).getRestaurant(eq(restaurant.getId()));
    }
}
