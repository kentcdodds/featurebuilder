package com.domo.featurebuilder.model;

import com.domo.featurebuilder.controller.TemplateController;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Scenario {

    private String name;
    private boolean happy;

    public Scenario(String name, boolean happy) {
        this.name = name;
        this.happy = happy;
    }

    public String getTemplateInfo(Map endpointTestMap) throws IOException, TemplateException {
        StringWriter out = new StringWriter();

        Template scenarioTemplate = TemplateController.getInstance().getScenarioTemplate();
        Map rootMap = getTemplateMap();
        rootMap.putAll(endpointTestMap);
        if (happy) {
            rootMap.put("tags", Arrays.asList("happyPath"));
        } else {
            rootMap.remove("response_content");
            rootMap.put("tags", Arrays.asList("failPath"));
            rootMap.put("response_code", "400");
        }
        scenarioTemplate.process(rootMap, out);

        String output = new String(out.getBuffer());
        out.close();
        return output;
    }

    private Map getTemplateMap() {
        Map root = new HashMap();
        root.put("name", name);
        return root;
    }
}
