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
import java.io.*;
import java.util.List;

/**
 * Created by Alex on 2017/1/7.
 * Pawn is a the valuable thing in save file that we want to edit.
 */
public class Pawn {
    private String name;
    private Trait[] traits=new Trait[3];
    private Skill[] skills=new Skill[12];
    private Node anchor;
    private Node sampleTrait;
    private Node sampleLevel;
    private Node samplePassion;

    public Pawn(Node anchor, Node sampleTrait, Node sampleLevel, Node samplePassion) {
        this.anchor = anchor;
        this.sampleTrait = sampleTrait;
        this.sampleLevel = sampleLevel;
        this.samplePassion = samplePassion;
        load();
    }

    private void load(){
        name=Data.getValue(Data.getNode(anchor,"name","nick"));

        Node traitsNode=Data.getNode(anchor,"story","traits","allTraits");
        List<Node> traitsSubs=Data.findAll(traitsNode.getFirstChild(),"li");
        assert traitsSubs.size()<=traits.length;
        //System.out.println("append "+name);
        for (int k=0;k!=traits.length;++k){
            if(k<traitsSubs.size()){
                traits[k]=new Trait(traitsSubs.get(k), sampleTrait);
            }else {
                Node one= sampleTrait.cloneNode(true);
                traits[k]=new Trait(traitsNode.appendChild(one), sampleTrait);
                traits[k].setDef(Trait.VACUUM_DEF);
                traits[k].setDegree(Trait.VACUUM_DEGREE);
            }
        }
        //System.out.println("finish "+name);

        Node skillsNode=Data.getNode(anchor,"skills","skills");
        List<Node> skillsSubs=Data.findAll(skillsNode.getFirstChild(),"li");
        for(int k=0;k!=skillsSubs.size();++k){
            skills[k]=new Skill(skillsSubs.get(k),sampleLevel,samplePassion);
        }
    }

    public void save(){
        for (Trait trait:traits){
            trait.save();
        }
        for(Skill skill:skills){
            skill.save();
        }
    }

    public Trait[] getTraits() {
        return traits;
    }

    public Skill[] getSkills() {
        return skills;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        String filepath="C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves\\";
        InputStream is = new FileInputStream(new File(filepath+"pawn"));
        Document doc=db.parse(is);
        Node root=doc.getDocumentElement();
        Node sampleTrait=Data.search(root,"degree").get(0).getParentNode();
        Node sampleLevel=Data.search(root,"level").get(0);
        Node samplePassion=Data.search(root,"passion").get(0);

        Pawn pawn=new Pawn(doc.getDocumentElement(),sampleTrait,sampleLevel,samplePassion);
        pawn.getTraits()[1].setDef("None");
        for(Skill skill:pawn.getSkills()){
            skill.setLevel("20");
            skill.setPassion("Minor");
        }
        pawn.save();

        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer transformer=tf.newTransformer();
        DOMSource source=new DOMSource(doc);
        StreamResult result=new StreamResult(new FileWriter(new File(filepath+"pawn_new")));
        transformer.transform(source,result);
    }
}
