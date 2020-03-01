package com.example.dexter007bot.AIML;

import com.example.dexter007bot.MainActivity;

import java.util.Set;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.AIMLProcessorExtension;
import org.alicebot.ab.ParseState;
import org.alicebot.ab.Utilities;
import org.w3c.dom.Node;

public class sample implements AIMLProcessorExtension {

    public Set<String> extensionTagNames = Utilities.stringSet("button","text","postback");
    public Set<String> extensionTagSet() {
        // TODO Auto-generated method stub
        return extensionTagNames;
    }
    private String button(Node node, ParseState ps) {
        //System.out.println("Button");
        String id = AIMLProcessor.evalTagContent(node, ps, null);
        //System.out.println(id);
        MainActivity.al.add(id);
        return "";
    }
    private String text(Node node, ParseState ps) {
        //System.out.println("Text");
        String id = AIMLProcessor.evalTagContent(node, ps, null);
        return id;
    }
    private String postback(Node node, ParseState ps) {
        String id = AIMLProcessor.evalTagContent(node, ps, null);
        return id;
    }

    public String recursEval(Node node, ParseState ps) {
        // TODO Auto-generated method stub
        try {
            String nodeName = node.getNodeName();
            if (nodeName.equals("button"))
                return button(node, ps);
            else if (nodeName.equals("text"))
                return text(node, ps);
            else if (nodeName.equals("postback"))
                return postback(node, ps);
            return (AIMLProcessor.genericXML(node, ps));
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

}
