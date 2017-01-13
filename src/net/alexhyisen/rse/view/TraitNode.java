package net.alexhyisen.rse.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.alexhyisen.rse.model.Info;
import net.alexhyisen.rse.model.Trait;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Alex on 2017/1/8.
 * TraitNode is the ner.alexhyisen.rse.view of a Trait.
 */
class TraitNode extends HBox{
    private Trait orig;
    private Info info;
    private ComboBox<String> defComboBox,degreeComboBox,labelComboBox;
    private Map<String,String> degreeDatas;

    TraitNode(Trait orig, Info info) {
        this.orig = orig;
        this.info = info;
        init();
    }

    private ComboBox<String> addComboBox(Consumer<String> func,
                                         double width,boolean isEditable,
                                         String value, String... candidates){
        ComboBox<String> anchor=new ComboBox<>(FXCollections.observableArrayList(candidates));
        anchor.setPrefWidth(width);
        anchor.setEditable(isEditable);
        anchor.setValue(value);
        anchor.valueProperty().addListener((observable, oldValue, newValue) -> func.accept(newValue));
        this.getChildren().add(anchor);

        return anchor;
    }

    private void updateDegreeDatas(){
        degreeDatas=info.getData().getOrDefault(defComboBox.getValue(),Collections.emptyMap());
    }

    //I use lambdas, rather than functions to offer those methods, to insure that
    //those methods mustn't be called directly. We should use ComboBox.setValue() instead.
    private final Consumer<String> handleDefAction=(def)->{
        //System.out.println("handleDefAction "+defComboBox.getValue()+" -> "+def);
        orig.setDef(def);
        if(info.getData().containsKey(def)){
            degreeComboBox.getItems().clear();
            updateDegreeDatas();
            degreeDatas.keySet().forEach(degreeComboBox.getItems()::add);
            if(degreeDatas.keySet().size()==1){
                degreeComboBox.setValue(Trait.VACUUM_DEGREE);
            }
        }
    };

    private final Consumer<String> handleDegreeAction=(degree)->{
        if(degree!=null){
            //System.out.println("handleDegreeAction "+degreeComboBox.getValue()+" -> "+degree);
            orig.setDegree(degree);
            if(degreeDatas==null){
                updateDegreeDatas();
            }
            //System.out.println("check "+degree+" from "+defComboBox.getValue());

            String label=degreeDatas.getOrDefault(degree,"unavailable");
            //System.out.println(labelComboBox.getValue()+" -> "+label);
            if(!labelComboBox.getValue().equals(label)){
                labelComboBox.setValue(label);
            }
        }
    };

    private final Consumer<String> handleLabelAction=(value)->{
        //System.out.println("handleLabelAction "+labelComboBox.getValue()+" -> "+value);
        info.getData().forEach((def,data)-> data.forEach((degree, label)->{
            if(value.equals(label)){
                if (!def.equals(defComboBox.getValue())) {
                    defComboBox.setValue(def);
                }
                if (!degree.equals(degreeComboBox.getValue())) {
                    degreeComboBox.setValue(degree);
                }
            }
        }));
    };

    private void init(){
        this.setSpacing(10);
        List<String> candidates=info.getData().keySet().stream().collect(Collectors.toList());
        candidates.add(Trait.VACUUM_DEF);
        defComboBox=addComboBox(handleDefAction,140,true,
                orig.getDef(),candidates.toArray(new String[candidates.size()]));
        updateDegreeDatas();
        candidates=degreeDatas.keySet().stream().collect(Collectors.toList());
        degreeComboBox=addComboBox(handleDegreeAction,70,true,
                orig.getDegree(),candidates.toArray(new String[candidates.size()]));
        candidates=info.getData().values().stream().flatMap(v->v.values().stream()).collect(Collectors.toList());
        labelComboBox=addComboBox(handleLabelAction,120,false,
                Optional.ofNullable(degreeDatas.get(orig.getDef())).orElse("unavailable"),
                candidates.toArray(new String[candidates.size()]));
    }
}
