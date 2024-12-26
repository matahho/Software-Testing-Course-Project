Feature: Getting average rating
  Restaurant average rating is calculated correctly

  Scenario: RestaurantRatingWithoutReview
    Given No restaurant's review exists
    When Try to get restaurant rating
    Then Null ratings returned


  Scenario Outline: RestaurantRatingWithMultipleReview
    Given <"N"> review on the restaurant exist
    When Try to get restaurant rating
    Then the average rating for food should be <foodRating>
    And the average rating for service should be <serviceRating>
    And the average rating for ambiance should be <ambianceRating>
    And the average rating for overall should be <overallRating>
    And the stars count should be <starCount>
    Examples:
      | "N" | foodRating | serviceRating | ambianceRating | overallRating | starCount |
      | 2   | 1.5        | 1.5           | 1.5            | 1.5           | 2         |
      | 3   | 2.0        | 2.0           | 2.0            | 2.0           | 2         |
      | 4   | 2.5        | 2.5           | 2.5            | 2.5           | 3         |
      | 100 | 50.5       | 50.5          | 50.5           | 50.5          | 5         |




