package com.occamlab.te;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class HeadlessForm {

    public HeadlessForm(TECore teCore) {
        
        String sessionId = teCore.getTestPath();
        File f = new File("forms/" + sessionId + ".xml");
        System.out.println("Checking for " + f.getAbsolutePath());
        if (!f.exists() && sessionId.contains("/")) {
            f = new File("forms/yes.xml");
        }
        if (!f.exists()) {
            throw new RuntimeException("Could not find form file:" + f.getAbsolutePath());
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            teCore.setFormResults(doc);
        } 
        catch(Exception e) {
            throw new RuntimeException("Could not parse form file" + f.getAbsolutePath(), e);
        }
    }

}
