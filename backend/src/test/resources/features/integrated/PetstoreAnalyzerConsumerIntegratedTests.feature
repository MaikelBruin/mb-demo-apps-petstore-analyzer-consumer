Feature: Petstore analyzer integrated tests

    ################### same tests as isolated ##################################################

  Scenario: Receive event
    When I receive an event that includes an eventType and payload
    Then I expect an "Accepted" result

