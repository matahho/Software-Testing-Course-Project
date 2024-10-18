package mizdooni.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class TestRating {
    private Rating rating;
    @BeforeEach
    public void setUpFixtures(){
        rating = new Rating();
    }

    @Test
    public void someRatingExistWithInvalidOverall_tryToGetTotalStars_starsCalculatedCorrectly(){
        rating.overall = 100.20;
        assertEquals(5, rating.getStarCount());
    }

    @Test
    public void someRatingExistWithNegativeOverall_tryToGetTotalStars_starsCalculatedCorrectly(){
        rating.overall = -90;
        assertEquals(0, rating.getStarCount());
    }

    @Test
    public void someRatingExistsWithFloatValue_tryToGetTotalStars_starsCalculatedCorrectly(){
        rating.overall = 4.23838;
        assertEquals(4, rating.getStarCount());
    }
}
