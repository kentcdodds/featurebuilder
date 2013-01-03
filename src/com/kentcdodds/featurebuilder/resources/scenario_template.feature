    Scenario: ${name}
        Given I authenticate with my '${r"#{Variables.enduserLogin}"}' and '${r"#{Variables.enduserPassword}"}'
        Then I test the endpoint using:
        """
            ${r"{"}
                :endPointName => ${endpoint_path},
                :method => '${endpoint_method}',
                :responseType => ${response_code!"N/A"}
            ${r"}"}
        """
