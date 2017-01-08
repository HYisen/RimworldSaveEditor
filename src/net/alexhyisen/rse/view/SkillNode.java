package net.alexhyisen.rse.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import net.alexhyisen.rse.model.Skill;

/**
 * Created by Alex on 2017/1/8.
 * SkillNode is the ner.alexhyisen.rse.view of a skill.
 */
public class SkillNode extends HBox{
    Skill orig;

    Label defLabel;
    ChoiceBox<String> passionChoiceBox;
    TextField levelTextField;
    Slider levelSlider;

    private void init(){
        setSpacing(10);
        defLabel=new Label(orig.getDef());
        defLabel.setPrefWidth(150);
        defLabel.setAlignment(Pos.CENTER);
        this.getChildren().add(defLabel);
        passionChoiceBox=new ChoiceBox<>(FXCollections.observableArrayList(Skill.VACUUM_PASSION,"Minor","Major"));
        passionChoiceBox.setPrefWidth(100);
        passionChoiceBox.setValue(orig.getPassion());
        passionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> orig.setPassion(newValue));
        this.getChildren().add(passionChoiceBox);
        levelTextField=new TextField(orig.getLevel());
        levelTextField.setPrefWidth(50);
        levelTextField.setAlignment(Pos.CENTER);
        levelTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Integer value=null;
            try {
                value=Integer.parseInt(newValue);
            }catch (NumberFormatException ignored){}
            if(value!=null&&value>=0&&value<=20){
                //levelTextField.setText(newValue);
                levelSlider.setValue(value);
                orig.setLevel(newValue);
            }else {
                levelTextField.setText(oldValue);
            }
        });
        this.getChildren().add(levelTextField);
        levelSlider=new Slider(0,20, Double.valueOf(orig.getLevel()));
        levelSlider.setPrefWidth(200);
        levelSlider.setBlockIncrement(1);
        levelSlider.valueProperty().addListener((observable, oldValue, newValue) -> levelTextField.setText(String.valueOf(newValue.intValue())));
        this.getChildren().add(levelSlider);
    }

    public SkillNode(Skill orig) {
        this.orig = orig;
        init();
    }
}
