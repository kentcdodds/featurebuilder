package com.domo.featurebuilder.helper;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Authors: Mack Cope and Kent Dodds
 * Date: 1/18/13
 * Time: 11:34 MDST
 */
public class CSVHandler {

    public final int limit = -1, offset = 15;
    private static CSVHandler instance;

    private CSVHandler() {
    }

    public static CSVHandler getInstance() {
        if (instance == null)
            instance = new CSVHandler();
        return instance;
    }

    public List<String[]> readEndpointsFromCSVFile(String csvResourceLocation) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(csvResourceLocation)));
        @SuppressWarnings("UnusedAssignment") // To skip the header
                String[] next = reader.readNext();
        List<String[]> csvRows = new ArrayList<String[]>();
        int i = -1;
        while ((next = reader.readNext()) != null) {
            i++;
            if (limit > 0 && i < offset)
                continue;
            if ((limit + offset) < i && limit > 0)
                break;
            csvRows.add(next);
            System.out.println("Row Data >>>> " + next[0] + " " + next[1]);
        }
        System.out.println("Total rows retrieved: " + csvRows.size());
        return csvRows;
    }

}
