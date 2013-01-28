    @hourlyWIP <#list tags as tag>@${tag} </#list>
    Scenario: ${name}
        Given I authenticate to play
        Then I test the endpoint using:
        """
            ${r"{"}
                :endPointName => '${endpoint_path}',
                :method => '${endpoint_method}'<#if response_code??>,
                :responseType => ${response_code}</#if><#if request_body??>,
                :body => '${request_body}',
                :bodyType => '${body_type}'</#if>
            ${r"}"}
        """<#if response_content??>
        Then I validate the endpoint additive JSON by key using:
        """

            ${response_content}

        """</#if>