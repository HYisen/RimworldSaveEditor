package net.alexhyisen.rse.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Alex on 2017/1/15.
 * Trans is the function to translate string from the basic language in raw file to other languages.
 * Though mapping by the translation data of the game itself.
 * Trans can update Info, and that's the way it translate it.
 */
public class Trans {
    private Map<String,String> data=new HashMap<>();

    public Trans(String path) throws FileNotFoundException, XMLStreamException {
        load(path);
    }

    public Trans() {
    }

    private void read(String filename, Function<String[],String> abstractor) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory f=XMLInputFactory.newInstance();
                    XMLEventReader r=f.createXMLEventReader(new FileInputStream(filename));
                    String key=null;
                    while (r.hasNext()){
                        XMLEvent e=r.nextEvent();
                        switch (e.getEventType()){
                            case XMLEvent.START_ELEMENT:
                                String name=e.asStartElement().getName().toString();
                                //System.out.println("START_ELEMENT\t"+name);
                                String[] content=name.split("\\.");
                    /*
                    for(int k=0;k!=content.length;++k){
                        System.out.println("\t"+k+"->"+content[k]);
                    }
                    */
                    key=abstractor.apply(content);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    String value=e.asCharacters().getData();
                    //System.out.println("CHARACTERS\t"+value);
                    if(key!=null){
                        data.put(key,value);
                    }
                    key=null;
                    break;
            }
        }
    }

    private void load(String path) throws FileNotFoundException, XMLStreamException {
        read(path+"\\SkillDef\\Skills.xml", v-> (v.length==2&&"label".equals(v[1]))?v[0]:null);
        read(path+"\\TraitDef\\Traits_Spectrum.xml", v->(v.length==4&&"label".equals(v[3]))?v[0]+"."+v[2]:null);
        read(path+"\\TraitDef\\Traits_Singular.xml", v->(v.length==4&&"label".equals(v[3]))?v[0]+"."+v[2]:null);
    }

    public String translate(String orig){
        return data.getOrDefault(orig,orig);
    }

    //return the old, unchanged Info.
    public Info translate(Info target){
        Info rtn=new Info(target);
        target.getData().forEach((def,map)->{
            int k=0;
            for (String s : map.keySet()) {
                map.put(s, translate(def + "." + k));
                k++;
            }
        });
        return rtn;
    }

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        final String path="D:\\Steam\\steamapps\\common\\RimWorld\\Mods\\Core\\Languages\\ChineseSimplified\\DefInjected";
        Trans t=new Trans();
        t.read(path+"\\SkillDef\\Skills.xml", v-> (v.length==2&&"label".equals(v[1]))?v[0]:null);
        System.out.println(t.translate("Research"));
        //t.read(path+"\\TraitDef\\Traits_Spectrum.xml", v->(v.length==4&&"label".equals(v[3]))?v[0]+"."+v[2]:null);
        //System.out.println(t.translate("Industriousness.0"));
    }
}
