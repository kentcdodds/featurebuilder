package com.domo.featurebuilder.model;

import com.domo.featurebuilder.controller.TemplateController;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Feature {

    private String fileName;
    private String parentDirectory;
    private List<Scenario> scenarios;
    private List<String> tags;
    private String featureText;

    public Feature(String fileName, String parentDirectory, List<Scenario> scenarios, String... tags) {
        this.fileName = fileName;
        this.parentDirectory = parentDirectory;
        this.scenarios = scenarios;
        this.tags = Arrays.asList(tags);
    }

    public void generateFeatureText(Map endpointTestMap) throws TemplateException, IOException {
        StringWriter out = new StringWriter();
        Template template = TemplateController.getInstance().getFeatureTemplate();
        template.process(getTemplateMap(endpointTestMap), out);
        featureText = new String(out.getBuffer());
        out.close();
    }

    private Map getTemplateMap(Map endpointTestMap) throws IOException, TemplateException {
        Map root = new HashMap();
        root.put("name", fileName);
        root.put("tags", tags);
        root.put("scenarios", getScenarioInfo(endpointTestMap));
        return root;
    }

    private List<String> getScenarioInfo(Map endpointTestMap) throws IOException, TemplateException {
        List<String> scenarioInfo = new ArrayList<String>(scenarios.size());
        for (Scenario scenario : scenarios)
            scenarioInfo.add(scenario.getTemplateInfo(endpointTestMap));
        return scenarioInfo;
    }


    public void save() throws IOException {
        File featureFile = new File(parentDirectory, fileName + ".feature");
        if (!featureFile.getParentFile().exists())
            featureFile.getParentFile().mkdirs();
        PrintWriter printWriter = new PrintWriter(featureFile);
        printWriter.write(featureText);
        printWriter.close();
    }

    public String getFeatureText() {
        return featureText;
    }

    public String getFilename() {
        return fileName;
    }

}
