${intro_comments}
# Author: Kent Dodds
# Manager: Doug Reid

@kentsTest @devUnit @access @kpiList
Feature: Get a list of user's KPIs

    Scenario: User has access to many KPIs
    #Given I authenticate with my 'kent.dodds@domo.com' and 'KentsDomo1!'
        Given I authenticate with my '#{Variables.enduserLogin}' and '#{Variables.enduserPassword}'
        Then I test the endpoint using:
        """
            {
                :endPointName => '/domoweb/access/kpilist',
                :method => 'Get',
                :responseType => 200
            }
        """