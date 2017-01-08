package net.alexhyisen.rse.view;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import net.alexhyisen.rse.model.Trait;

import java.util.function.Consumer;

/**
 * Created by Alex on 2017/1/8.
 * TraitNode is the ner.alexhyisen.rse.view of a Trait.
 */
public class TraitNode extends HBox{
    private Trait orig;
    private ComboBox<String> defComboBox,degreeComboBox;

    public TraitNode(Trait orig) {
        this.orig = orig;
        init();
    }

    private void addComboBox(ComboBox<String> anchor,Consumer<String> func,double width, String value, String... candidates){
        anchor=new ComboBox<>(FXCollections.observableArrayList(candidates));
        anchor.setPrefWidth(width);
        anchor.setEditable(true);
        anchor.setValue(value);
        anchor.valueProperty().addListener((observable, oldValue, newValue) -> func.accept(newValue));
        this.getChildren().add(anchor);
    }

    private void init(){
        this.setSpacing(10);
        addComboBox(defComboBox,orig::setDef,130,orig.getDef(),orig.getDef(),Trait.VACUUM_DEF);
        addComboBox(degreeComboBox,orig::setDegree,70,orig.getDegree(),orig.getDegree(),Trait.VACUUM_DEGREE);
    }
}
