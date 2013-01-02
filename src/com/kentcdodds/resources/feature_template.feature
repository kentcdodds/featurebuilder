${intro_comments}

@kentsTest @devUnit @access @kpiList
Feature: Get a list of user's KPIs

    Scenario: User has access to many KPIs
    #Given I authenticate with my 'kent.dodds@domo.com' and 'KentsDomo1!'
        Given I authenticate with my '${r"#{Variables.enduserLogin}"}' and '${r"#{Variables.enduserPassword}"}'
        Then I test the endpoint using:
        """
            ${r"{
                :endPointName => '/domoweb/access/kpilist',
                :method => 'Get',
                :responseType => 200
            }"}
        """