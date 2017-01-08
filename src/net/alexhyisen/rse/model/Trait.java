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
 * Trait is a part of a pawn.
 */
public class Trait {
    private String def;
    private String degree;

    private Node anchor;

    private Node sample;

    public static final String VACUUM_DEF="None";
    public static final String VACUUM_DEGREE="N/A";

    public Trait(Node anchor, Node sample) {
        this.anchor = anchor;
        this.sample = sample;
        load();
    }

    private void load(){
        Node defNode=Data.getNode(anchor,"def");
        def=Data.getValue(defNode);
        Node degreeNode=Data.getNode(anchor,"degree");
        degree=Data.getValueOptional(degreeNode).orElse(VACUUM_DEGREE);
    }

    public void save(){
        if(VACUUM_DEF.equals(def)){
            anchor.getParentNode().removeChild(anchor);
        }else {
            Node node=sample.cloneNode(true);
            Node defNode=Data.getNode(node,"def");
            Data.setValue(defNode,def);
            Node degreeNode=Data.getNode(node,"degree");
            if(VACUUM_DEGREE.equals(degree)){
                degreeNode.getParentNode().removeChild(degreeNode);
            }else{
                Data.setValue(degreeNode,degree);
            }
            anchor.getParentNode().replaceChild(node,anchor);
        }
    }

    public String getDef() {
        return def;
    }

    public String getDegree() {
        return degree;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        String filepath="C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves\\";
        Document doc=db.parse(new File(filepath+"test"));
        Node root=doc.getDocumentElement();
        Node traitSample=Data.search(root,"degree").get(0).getParentNode();

        String[] path="traits allTraits".split(" ");
        Node node=Data.getNode(root,path);
        Data.expand(node,0);
        List<Node> subs=Data.findAll(node.getFirstChild(),"li");
        subs.forEach(v->{
            System.out.println("find ");
            Data.expand(v,0);
        });
        Trait[] traits=new Trait[3];
        assert subs.size()<=traits.length;
        for (int k=0;k!=traits.length;++k){
            if(k<subs.size()){
                traits[k]=new Trait(subs.get(k),traitSample);
            }else {
                System.out.println("add 1 at "+k);
                Node one= traitSample.cloneNode(true);
                traits[k]=new Trait(node.appendChild(one),traitSample);
                traits[k].setDef(VACUUM_DEF);
                traits[k].setDegree(VACUUM_DEGREE);
            }
        }
        traits[2].setDef("Check");
        traits[2].setDegree("-2");
        traits[1].setDegree("N/A");
        traits[0].setDef("None");

        for (Trait trait:traits
             ) {
            trait.save();
        }
        Data.expand(root,0);

        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer transformer=tf.newTransformer();
        DOMSource source=new DOMSource(doc);
        StreamResult result=new StreamResult(new FileWriter(new File(filepath+"test_new")));
        transformer.transform(source,result);
    }
}
