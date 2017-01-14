package net.alexhyisen.rse.view;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import net.alexhyisen.rse.model.Data;
import net.alexhyisen.rse.model.Info;
import net.alexhyisen.rse.model.Pawn;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Controller{
    @FXML private Label msgLabel;
    @FXML private TextField savePathTextField;
    @FXML private TextField saveNameTextField;
    @FXML private TextField gamePathTextField;
    @FXML private ListView<String> pawnsListView;
    @FXML private VBox traitsVBox;
    @FXML private VBox skillsVBox;

    private Logger logger;
    private Data data;
    private Info info;

    private static final String FILENAME_SUFFIX=".rws";

    private void updatePawnsList(){
        //I shall remove the listener first and then add it back again, to avoid the influence of clear().
        pawnsListView.getSelectionModel().selectedItemProperty().removeListener(pawnsListListener);
        pawnsListView.getItems().clear();
        pawnsListView.getItems().addAll(data.getPawns().keySet());
        pawnsListView.getSelectionModel().selectedItemProperty().addListener(pawnsListListener);
        logger.push("update pawnsList");
    }

    private ChangeListener<String> pawnsListListener=(observable, oldValue, newValue) -> {
        logger.push("select "+newValue);
        traitsVBox.getChildren().remove(2,traitsVBox.getChildren().size());
        skillsVBox.getChildren().remove(2,skillsVBox.getChildren().size());
        Pawn pawn=data.getPawns().get(newValue);
        Arrays.stream(pawn.getTraits())
                .map(v->new TraitNode(v,info))
                .forEach(traitsVBox.getChildren()::add);
        Arrays.stream(pawn.getSkills())
                .map(SkillNode::new)
                .forEach(skillsVBox.getChildren()::add);
    };

    @FXML protected void handleGoButtonAction(){
        if(logger==null){
            logger=new Logger(msgLabel);
            logger.push("initiate logger");
        }
        if (data==null){
            data=new Data();
            logger.push("initiate data");
        }
        if (info==null){
            info=new Info();
            logger.push("initiate info");
        }
        if(saveNameTextField.getText().isEmpty()){
            saveNameTextField.setText("Autosave-2");
            logger.push("offer a default save path");
        }
        if(savePathTextField.getText().isEmpty()){
            savePathTextField.setText("C:\\Users\\Alex\\AppData\\LocalLow\\Ludeon Studios\\RimWorld by Ludeon Studios\\Saves");
            logger.push("offer a default save name");
        }
        if(gamePathTextField.getText().isEmpty()){
            gamePathTextField.setText("D:\\Steam\\steamapps\\common\\RimWorld");
            logger.push("offer a default game path");
        }
    }

    private String getSaveURI(){
        String rtn=savePathTextField.getText()+"\\"+saveNameTextField.getText();
        if(!saveNameTextField.getText().endsWith(FILENAME_SUFFIX)){
            rtn+=FILENAME_SUFFIX;
        }
        return rtn;
    }

    @FXML protected void handleLoadButtonAction(){
        try {
            data.load(getSaveURI());
            logger.push("succeed to load");
            updatePawnsList();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            logger.push("failed to load");
        }
    }

    @FXML protected void handleSaveButtonAction(){
        try {
            data.save(getSaveURI().replace(FILENAME_SUFFIX,"_new"+FILENAME_SUFFIX));
            logger.push("succeed to save");
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
            logger.push("failed to save");
        }
    }

    @FXML protected void handleReadButtonAction(){
        try {
            Files.newDirectoryStream(
                    Paths.get(gamePathTextField.getText()+"\\Mods\\Core\\Defs\\TraitDefs"))
                    .forEach(v->{
                logger.push("read "+v.getFileName());
                try {
                    info.read(v.toString());
                } catch (SAXException | ParserConfigurationException | IOException e) {
                    e.printStackTrace();
                    logger.push("failed to read");
                }
            });
            logger.push("succeed to read");
        } catch (IOException e) {
            e.printStackTrace();
            logger.push("failed to read");
        }
    }
}
