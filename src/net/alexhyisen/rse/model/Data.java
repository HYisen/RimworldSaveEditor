package net.alexhyisen.rse.model;

import com.sun.istack.internal.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Alex on 2017/1/6.
 * Data is a save file.
 */
public class Data {
    static private String getIndent(int depth){
        char[] rtn=new char[depth];
        Arrays.fill(rtn,'\t');
        return new String(rtn);
    }

    static void expand(Node node,int depth){
        NodeList subs=node.getChildNodes();
        if(subs.getLength()==1){
            System.out.println(getIndent(depth)+node.getNodeName()+"="+subs.item(0).getNodeValue());
        }else if (subs.getLength() != 0) {
            System.out.println(getIndent(depth)+node.getNodeName()+"->");
            for(int k=0;k!=subs.getLength();++k){
                expand(subs.item(k),depth+1);
            }
        }
    }

    //a whole deepest search
    //for parallel optimization, I can not use the strategy that searches one by one.
    static List<Node> search(Node root, String name){
        List<Node> rtn=new LinkedList<>();
        if(name.equals(root.getNodeName())){
            rtn.add(root);
        }else if(root.getChildNodes().getLength()>1){
            NodeList subs=root.getChildNodes();
            IntStream.range(0,subs.getLength())
                    .mapToObj(subs::item)
                    .parallel()
                    .flatMap(v->search(v,name).stream())
                    .sequential()//List is not an container that supports parallel operations.
                    .forEach(rtn::add);
        }
        return rtn;
    }

    @Nullable static Node findOne(@Nullable Node from,String name){
        if(from!=null){
            Node node=from;
            do {
                if(node.getNodeName().equals(name)){
                    return node;
                }
            }while ((node=node.getNextSibling())!=null);
        }
        return null;
    }

    static List<Node> findAll(@Nullable Node from,String name){
        List<Node> rtn=new ArrayList<>();//LL is cheaper, but AL is better for parallel optimization.
        Node one=findOne(from,name);
        while (one!=null){
            rtn.add(one);
            one=findOne(one.getNextSibling(),name);
        }
        return rtn;
    }

    //the name of root is exclusive in the path
    @Nullable static Node getNode(@Nullable Node root,String... path){
        Node one=root;
        Iterator<String> it=Arrays.asList(path).iterator();
        while(it.hasNext()&&one!=null){
            //check for attendance is not necessary, but I guess it benefits the performance.
            one=findOne(one.getFirstChild(),it.next());
        }
        return one;
    }

    static Optional<Node> getNodeOptional(@Nullable Node root,String... path){
        return Optional.ofNullable(getNode(root,path));
    }

    static void setValue(Node target,String value){
        assert target.getChildNodes().getLength()==1;
        target.getFirstChild().setNodeValue(value);
    }

    @Nullable static String getValue(Node source){
        assert source.getChildNodes().getLength()==1;
        return source.getFirstChild().getNodeValue();
    }

    static Optional<String> getValueOptional(@Nullable Node source){
        if(source==null){
            return Optional.empty();
        }else {
            return Optional.of(getValue(source));
        }
    }

    private Map<String,Pawn> pawns;
    private Document doc;

    public void load(String filename) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        InputStream is = new FileInputStream(new File(filename));
        doc=db.parse(is);
        Node root=Data.getNode(doc.getDocumentElement(),"game","maps","li","things");

        Node sampleTrait=Data.search(root,"degree").get(0).getParentNode().cloneNode(true);

        Node badNode=Data.findOne(sampleTrait.getFirstChild(),"scenForced");
        if(badNode!=null){
            sampleTrait.removeChild(badNode);
        }

        //the clone is unnecessary, just for extra safety
        Node sampleLevel=Data.search(root,"level").get(0).cloneNode(true);
        Node samplePassion=Data.search(root,"passion").get(0).cloneNode(true);

        pawns=new HashMap<>();
        Data.findAll(root.getFirstChild(),"thing")
                .stream()
                .filter(v->{
                    Node attribute=v.getAttributes().getNamedItem("Class");
                    return attribute!=null&&attribute.getNodeValue().equals("Pawn");
                })
                //Possible, but unnecessary filter
                //.filter(v->Data.findOne(v.getFirstChild(),"name").getAttributes().getNamedItem("Class").getNodeValue().equals("NameTriple"))
                .filter(v->Data.getValue(Data.getNode(v,"def")).equals("Human"))
                .forEach(v->{
                    //Data.expand(v,1);
                    Pawn pawn=new Pawn(v,sampleTrait,sampleLevel,samplePassion);
                    pawns.put(pawn.getName(),pawn);
                    System.out.println("find "+pawn.getName());
                });
    }

    public Map<String, Pawn> getPawns() {
        return pawns;
    }

    public void save(String filename) throws TransformerException, IOException {
        pawns.values().forEach(Pawn::save);
        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer transformer=tf.newTransformer();
        DOMSource source=new DOMSource(doc);
        StreamResult result=new StreamResult(new FileWriter(new File(filename)));
        transformer.transform(source,result);
    }



    public static void main(String[] args){
        Data data=new Data();
        try {
            data.load("C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves\\raw.rws");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        data.getPawns().entrySet().forEach(v->{
            String log=v.getKey()+" delete traits ";
            for (Trait trait : v.getValue().getTraits()) {
                log+=trait.getDef()+"("+trait.getDegree()+") ";
                trait.setDef("None");
            }
            log+=" skills ";
            for(Skill skill:v.getValue().getSkills()){
                log+=skill.getLevel()+"["+skill.getPassion()+"] ";
                skill.setLevel("20");
                skill.setPassion("Minor");
            }
            System.out.println(log);
        });

        try {
            data.save("C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves\\new.rws");
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }
}
