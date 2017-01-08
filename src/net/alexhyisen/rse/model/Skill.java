package net.alexhyisen.rse.model;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by Alex on 2017/1/7.
 * Skill is a part of a pawn.
 */
public class Skill {
    private String def;//Not necessary, but probably useful to display
    private String level;
    private String passion;

    private Node anchor;

    private Node sampleLevel;
    private Node samplePassion;

    public static final String VACUUM_LEVEL ="0";
    public static final String VACUUM_PASSION="None";

    public Skill(Node anchor, Node sampleLevel, Node samplePassion) {
        this.anchor = anchor;
        this.sampleLevel = sampleLevel;
        this.samplePassion = samplePassion;
        load();
    }

    private void load(){
        def=Data.getValue(Data.getNode(anchor,"def"));
        level=Data.getValueOptional(Data.getNode(anchor,"level")).orElse(VACUUM_LEVEL);
        passion=Data.getValueOptional(Data.getNode(anchor,"passion")).orElse(VACUUM_PASSION);
    }

    private void set(String key,String value,String vacuumValue,Node sample,String indexKey){
        Node node=Data.getNode(anchor,key);
        if(vacuumValue.equals(value)&&node!=null){
            anchor.removeChild(node);
        }else {
            if(node==null){
                node=sample.cloneNode(true);
                anchor.insertBefore(node,Data.getNode(anchor,indexKey));
            }
            Data.setValue(node,value);
        }
    }

    public void save(){
        set("level",level,VACUUM_LEVEL,sampleLevel,"xpSinceLastLevel");
        set("passion",passion,VACUUM_PASSION,samplePassion,"xpSinceMidnight");
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setPassion(String passion) {
        this.passion = passion;
    }

    public String getDef() {
        return def;
    }

    public String getLevel() {
        return level;
    }

    public String getPassion() {
        return passion;
    }

    public static void main(String[] args) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        String filepath="C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves\\";
        Document doc=db.parse(new File(filepath+"skill"));
        Node root=doc.getDocumentElement();

        Node sampleLevel=Data.search(root,"level").get(0);
        Node samplePassion=Data.search(root,"passion").get(0);

        Skill[] skills=new Skill[12];
        List<Node> subs=Data.findAll(root.getFirstChild(),"li");
        for(int k=0;k!=subs.size();++k){
            skills[k]=new Skill(subs.get(k),sampleLevel,samplePassion);
        }

        for (Skill skill:skills
                ) {
            System.out.println(skill.getDef()+"("+skill.getPassion()+")"+"="+skill.getLevel());
            skill.setLevel("20");
            skill.setPassion("Minor");
        }

        for (Skill skill:skills
             ) {
            skill.save();
        }

        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer transformer=tf.newTransformer();
        DOMSource source=new DOMSource(doc);
        StreamResult result=new StreamResult(new FileWriter(new File(filepath+"skill_new")));
        transformer.transform(source,result);
    }
}
