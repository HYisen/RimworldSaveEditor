package net.alexhyisen.rse.model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Alex on 2017/1/13.
 * Info is the system to load, save, and control the information acquired from the game.
 */
public class Info{
    private Map<String,Map<String,String>> data;

    //a copy constructor
    @SuppressWarnings("WeakerAccess")
    public Info(Info orig) {
        data=orig.getData().entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey,e-> new LinkedHashMap<>(e.getValue())));
    }

    public Info() {
        data=new HashMap<>();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static class TraitsReader extends DefaultHandler{
        String name=null;

        String label;
        String degree;

        Map<String,String> degreeDatas;
        Map<String,Map<String,String>> traitDefs;
        //Those names come from the structure of the raw XML data.

        TraitsReader(Map<String, Map<String, String>> traitDefs) {
            this.traitDefs = traitDefs;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            name=localName;
            //System.out.println("start "+localName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName){
                case "TraitDef":
                    degreeDatas =null;
                    break;
                case "li":
                    if (label!=null) {
                        //System.out.println(degree+"\t"+label);
                        degreeDatas.put(Optional.ofNullable(degree).orElse(Trait.VACUUM_DEGREE),label);
                        label=null;
                        degree=null;
                    }
                    break;
            }
            name=null;
            //System.out.println("end "+localName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(name!=null){
                String content=new String(ch,start,length);
                switch (name){
                    case "defName":
                        //System.out.println("["+content+"]");
                        degreeDatas =new LinkedHashMap<>();//I need it linked in input order to do Trans a favor.
                        traitDefs.put(content, degreeDatas);
                        break;
                    case "degree":
                        //System.out.println("<"+content+">");
                        degree=content;
                        break;
                    case "label":
                        //System.out.println("("+content+")");
                        label=content;
                        break;
                }
            }
        }
    }

    public void read(String filename) throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf=SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser=spf.newSAXParser();
        XMLReader xmlReader=saxParser.getXMLReader();
        xmlReader.setContentHandler(new TraitsReader(data));
        xmlReader.parse(filename);
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        Info info=new Info();
        info.read("D:\\Steam\\steamapps\\common\\RimWorld\\Mods\\Core\\Defs\\TraitDefs\\Traits_Spectrum.xml");
        info.read("D:\\Steam\\steamapps\\common\\RimWorld\\Mods\\Core\\Defs\\TraitDefs\\Traits_Singular.xml");
        System.out.println("report");
        info.getData().forEach((name,item)->{
            StringBuilder sb=new StringBuilder(name+"\n");
            item.forEach((degree,label)->sb.append("\t").append(degree).append("=").append(label).append("\n"));
            System.out.println(sb);
        });
    }
}
