package net.alexhyisen.rse.view;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import net.alexhyisen.rse.model.Data;
import net.alexhyisen.rse.model.Info;
import net.alexhyisen.rse.model.Pawn;
import net.alexhyisen.rse.model.Trans;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Controller{
    @FXML private Label msgLabel;
    @FXML private TextField savePathTextField;
    @FXML private TextField saveNameTextField;
    @FXML private TextField gamePathTextField;
    @FXML private ListView<String> pawnsListView;
    @FXML private VBox traitsVBox;
    @FXML private VBox skillsVBox;
    @FXML private ComboBox<String> langComboBox;

    private Logger logger;
    private Data data;
    private Info info,rawInfo;
    private Trans trans;

    private static final String FILENAME_SUFFIX=".rws";
    private static final String VACUUM_TRANS_NAME="default";

    @FXML private void initialize(){
        //Believe it or not, despite the inspection, it does initializes.
        //Moreover, the interface Initializable is superseded, therefore I use the no-arg prototype.
        //System.out.println("init0004");
        langComboBox.getItems().add(VACUUM_TRANS_NAME);
        langComboBox.setValue(VACUUM_TRANS_NAME);
        langComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(VACUUM_TRANS_NAME.equals(newValue)){
                trans=new Trans();
                info=rawInfo;
            }else {
                try {
                    trans=new Trans(gamePathTextField.getText()+"\\Mods\\Core\\Languages\\"+newValue+"\\DefInjected");
                    logger.push("succeed to load language "+newValue);
                } catch (FileNotFoundException | XMLStreamException e) {
                    e.printStackTrace();
                    logger.push("fail to load language "+newValue);
                }
                if(oldValue.equals(VACUUM_TRANS_NAME)){
                    rawInfo=trans.translate(info);
                }
            }
        });
        loadUserProfile();
    }

    void handleCloseEvent(){
        //System.out.println("saving the user profile");
        saveUserProfile();
    }

    private void loadUserProfile(){
        Path path=Paths.get(".","config");

        try {
            List<String> data=Files.lines(path).collect(Collectors.toList());
            savePathTextField.setText(data.get(0));
            saveNameTextField.setText(data.get(1));
            gamePathTextField.setText(data.get(2));
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("doesn't matter, just failed to load the config.");
        }
    }

    private void saveUserProfile(){
        String savePath=savePathTextField.getText();
        String saveName=saveNameTextField.getText();
        String gamePath=gamePathTextField.getText();
        Path path=Paths.get(".","config");
        try {
            PrintWriter writer=new PrintWriter(new FileWriter((path.toFile()),false));
            writer.println(savePath);
            writer.println(saveName);
            writer.println(gamePath);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                .map(v->new TraitNode(v,info,trans))
                .forEach(traitsVBox.getChildren()::add);
        Arrays.stream(pawn.getSkills())
                .map(v->new SkillNode(v,trans))
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
        if (trans==null){
            trans=new Trans();
            logger.push("initiate trans");
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
            Files.newDirectoryStream(Paths.get(gamePathTextField.getText()+"\\Mods\\Core\\Languages"))
                    .forEach(v->{
                        //Damn, where is the filter in stream structure?
                        if(v.toFile().isDirectory()){
                            langComboBox.getItems().add(v.toFile().getName());
                        }
                    });
            logger.push("succeed to read");
        } catch (IOException e) {
            e.printStackTrace();
            logger.push("failed to read");
        }
    }
}
