    @hourlyWIP <#list tags as tag>@${tag} </#list>
    Scenario Outline: ${name}
        Given I authenticate to play
        Then I test the endpoint using:
        """
            ${r"{"}
                :endPointName => '${endpoint_path}',
                :method => '${endpoint_method}'<#if response_code??>,
                :responseType => ${response_code}</#if>
            ${r"}"}
        """<#if response_content??>
        And I validate the endpoint using:
        """

            ${response_content}

        """</#if>

    @hourlyWIP
    Examples:
