Feature: Adding new reservation to user
  Everybody wants to be able to add new reservation to user model

  Scenario: AReservationAddedCorrectly
    Given No reservation exists for a user
    When New reservation assign to the user
    Then The reservation added to user's reservations

  Scenario: AReservationNumberAssignedWell
    Given No reservation exists for a user
    When New reservation assign to the user
    Then The reservation number assigned based on reservation counter correctly


  Scenario Outline: ReservationCounterWorksCorrectly
    Given <"N"> reservations exists for a user
    When New reservation assign to the user
    Then The new reservation number assigned based on reservation counter to <"ExpectedReservationNumber">

    Examples:
      | "N" | "ExpectedReservationNumber" |
      | 1   | 1                           |
      | 2   | 2                           |
      | 23  | 23                          |

