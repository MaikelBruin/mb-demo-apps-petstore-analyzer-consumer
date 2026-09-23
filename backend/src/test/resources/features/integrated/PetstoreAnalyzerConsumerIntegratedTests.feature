Feature: Petstore analyzer consumer integrated tests

  Scenario: Receive event including required properties
    When I receive an event that includes an eventType and payload
    Then I expect a "Accepted" response
    And the response should include an id

  Scenario: Receive event missing eventType
    When I receive an event missing an eventType
    Then I expect a "Bad Request" response
    And the response should include an error message

  Scenario: Receive event missing payload
    When I receive an event missing a payload
    Then I expect a "Bad Request" response
    And the response should include an error message


