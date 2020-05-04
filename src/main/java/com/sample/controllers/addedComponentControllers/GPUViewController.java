package com.sample.controllers.addedComponentControllers;

import com.sample.App;
import com.sample.BLL.AdminLogic;
import com.sample.BLL.ComponentDeleter;
import com.sample.BLL.InputValidation.ValidationException;
import com.sample.DAL.OpenFile.Subtypes.OpenAddedComponents;
import com.sample.DAL.OpenFile.Subtypes.OpenGPUs;
import com.sample.Models.ComputerComponents.GraphicsCard;
import com.sample.Models.ComputerComponents.Processor;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GPUViewController {
    @FXML
    private TableView<GraphicsCard> table;
    @FXML private TableColumn<GraphicsCard, Double> price;
    private OpenAddedComponents opener = new OpenGPUs();
    private OpenAddedComponents deleter = new OpenGPUs();

    public void initialize() {
        table.setEditable(true);
        price.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter(){
            @Override
            public Double fromString(String s) {
                try{
                    return super.fromString(s);
                } catch (NumberFormatException e){
                    return Double.NaN;
                }
            }
        }));
        startThread();

    }

    private void startThread(){
        try {
            Thread openCaseFilesThread = new Thread(opener);
            opener.setOnSucceeded(this::handleSucceed);
            opener.setOnFailed(this::handleError);
            openCaseFilesThread.setDaemon(true);
            openCaseFilesThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(WorkerStateEvent workerStateEvent) {
        Label errorPlaceholder = new Label("Could not retrieve saved GPUs");
        table.placeholderProperty().setValue(errorPlaceholder);
    }

    private void handleSucceed(WorkerStateEvent workerStateEvent) {
        table.getItems().setAll((List<GraphicsCard>) opener.getValue());
    }
    @FXML
    private void viewSwapper(){
        try {
            App.changeView("/fxml/viewAddedComponents.fxml", 1200, 900);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteGPU(){
        try{
            final File folder = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/");
            GraphicsCard toBeDeleted = table.getSelectionModel().getSelectedItem();
            Alert deleteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
            deleteConfirmation.setTitle("Confirm deletion");
            deleteConfirmation.setHeaderText("Are you sure you want to delete "+toBeDeleted.getProductName()+"?");
            deleteConfirmation.setContentText("This will permanently remove the computer component");
            Optional<ButtonType> result = deleteConfirmation.showAndWait();
            try {
                if (result.get() == ButtonType.OK){
                    ComponentDeleter.deleteComponent(folder, toBeDeleted);
                    startDeleteThread();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch(NullPointerException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("No component selected");
            errorBox.setHeaderText("You must select a component to delete it");
            errorBox.showAndWait();
        }

    }

    private void startDeleteThread(){
        Thread openFilesThread = new Thread(deleter);
        deleter.setOnSucceeded(this::handleDeleteSucceed);
        deleter.setOnFailed(this::handleDeleteError);
        openFilesThread.setDaemon(true);
        openFilesThread.start();
    }

    private void handleDeleteSucceed(WorkerStateEvent workerStateEvent) {
        try{
            table.getItems().setAll((List<GraphicsCard>) deleter.getValue());
            if(table.getItems().toArray().length == 0){
                table.placeholderProperty().setValue(new Label("No GPUs saved"));
            }
        } catch (NullPointerException e){
            table.placeholderProperty().setValue(new Label("Something went wrong"));
        }
    }

    private void handleDeleteError(WorkerStateEvent workerStateEvent) {
        Alert errorBox = new Alert(Alert.AlertType.ERROR);
        errorBox.setTitle("Something went wrong while deleting");
    }

    @FXML
    private void editDescription(TableColumn.CellEditEvent cellEditEvent){
        String originalDescription = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setDescription(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedGraphicsCard.setDescription(originalDescription);
            table.refresh();
        }
    }

    @FXML
    private void editPrice(TableColumn.CellEditEvent cellEditEvent){
        double originalPrice = (double)cellEditEvent.getOldValue();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            if(String.valueOf(cellEditEvent.getNewValue()).equals("NaN")){
                System.out.println("bobo");
                throw new ValidationException("");
            }
            selectedGraphicsCard.setPrice((double)cellEditEvent.getNewValue());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            e.printStackTrace();
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText("Invalid price"+e.getMessage());
            errorBox.showAndWait();
            selectedGraphicsCard.setPrice(originalPrice);
            table.refresh();
        }
    }

    @FXML
    private void editName(TableColumn.CellEditEvent cellEditEvent) {
        String originalName = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File originalFile = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setProductName(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();

            //because components are stored with their product names as their file names, we need to take extra care not to
            //save a component to a new file just because their name has been updated (FileOutputStream creates a new file if one is not found).
            // This if-test checks if newFile and originalFile are the same, and if not - updates the old filename before doing any outputstreams.
            File newFile = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
            if (newFile.equals(originalFile)){
                AdminLogic.editFile(originalFile, selectedGraphicsCard);
            } else {
                //difference detected, change the filename before outputstreaming "new" object
                if (AdminLogic.editFileName(originalFile, newFile)){
                    AdminLogic.editFile(newFile, selectedGraphicsCard);
                }
            }
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedGraphicsCard.setProductName(originalName);
            table.refresh();
        }
    }

    @FXML
    private void editManufacturer(TableColumn.CellEditEvent cellEditEvent) {
        String originalManufacturer = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCardsa/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setProductionCompany(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            selectedGraphicsCard.setProductionCompany(originalManufacturer);
            errorBox.showAndWait();
            table.refresh();
        }
    }

    @FXML
    private void editSerialNumber(TableColumn.CellEditEvent cellEditEvent) {
        String originalSerialNumber = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setSerialNumber(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedGraphicsCard.setSerialNumber(originalSerialNumber);
            table.refresh();
        }
    }

    @FXML
    private void editMemoryType(TableColumn.CellEditEvent cellEditEvent) {
        String oldMemoryType = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setMemoryType(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            selectedGraphicsCard.setMemoryType(oldMemoryType);
            errorBox.showAndWait();
            table.refresh();
        }
    }

    @FXML
    private void editMemoryCapacity(TableColumn.CellEditEvent cellEditEvent) {
        String originalMemoryCapacity = cellEditEvent.getOldValue().toString();
        GraphicsCard selectedGraphicsCard = (GraphicsCard)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/GraphicsCards/"+selectedGraphicsCard.getProductName()+".jobj");
        try{
            selectedGraphicsCard.setMemoryCapacity(cellEditEvent.getNewValue().toString());
            selectedGraphicsCard.validate();
            AdminLogic.editFile(file, selectedGraphicsCard);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            selectedGraphicsCard.setMemoryCapacity(originalMemoryCapacity);
            errorBox.showAndWait();
            table.refresh();
        }
    }
}
