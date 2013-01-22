package com.domo.featurebuilder.model;

import com.domo.featurebuilder.controller.TemplateController;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Scenario {

    private String name;

    public Scenario(String name) {
        this.name = name;
    }

    public String getTemplateInfo(Map endpointTestMap) throws IOException, TemplateException {
        StringWriter out = new StringWriter();

        Template scenarioTemplate = TemplateController.getInstance().getScenarioTemplate();
        Map rootMap = getTemplateMap();
        rootMap.putAll(endpointTestMap);
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
