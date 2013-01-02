${intro_comments}

<#list tags as tag>tag <#list>
Feature: {$feature_name}

    Scenario: User has access to many KPIs
        Given I authenticate with my '${r"#{Variables.enduserLogin}"}' and '${r"#{Variables.enduserPassword}"}'
        Then I test the endpoint using:
        """
            ${r"{"}
                :endPointName => ${path},
                :method => '${method}',
                :responseType => ${response_code}
            ${r"}"}
        """