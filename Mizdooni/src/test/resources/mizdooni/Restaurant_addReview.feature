Feature: Adding review to restaurant successfully
  Users must be able to add new reviews to restaurants

  Scenario: AddingNewReviewByAUser
    Given There is no review on restaurant
    When A user try to add new review to restaurant
    Then Review added correctly to restaurant


  Scenario: AddingNewReviewBySomeUserWhichHadAReviewBefore
    Given There is a review by a user in the restaurant
    When The user try to add new review to the restaurant
    Then Review added correctly to restaurant and the previous review deleted
