package com.sample.controllers.addedComponentControllers;

import com.sample.App;
import com.sample.BLL.AdminLogic;
import com.sample.BLL.ComponentDeleter;
import com.sample.DAL.OpenFile.Subtypes.OpenCases;
import com.sample.Exceptions.ValidationException;
import com.sample.DAL.OpenFile.Subtypes.OpenAddedComponents;
import com.sample.DAL.OpenFile.Subtypes.OpenMice;
import com.sample.Models.ComputerComponents.Case;
import com.sample.Models.ComputerComponents.Mouse;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MouseViewController {
    @FXML private AnchorPane componentPane;
    @FXML private TableView<Mouse> table;
    @FXML private TableColumn<Mouse, Double> price;
    private OpenAddedComponents opener = new OpenMice(true);
    private OpenAddedComponents deleter = new OpenMice(true);
    @FXML private ChoiceBox<String> filter;
    @FXML private TextField querySearch;
    //this function sets the tableview as editable, sets a cellfactory for price, as we need to handle exceptions if
    //somebody writes something that won't parse from text to double.
    //finally it starts the thread responsible for loading all added components to the view's tableview.
    public void initialize() {
        table.setEditable(true);
        filter.getItems().add("Name");
        filter.getItems().add("Serial number");
        filter.getSelectionModel().selectFirst();
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

    //this function uses loads added components in their own thread.
    private void startThread(){
        Thread openMouseFilesThread = new Thread(opener);
        opener.setOnSucceeded(this::handleSucceed);
        opener.setOnFailed(this::handleError);
        openMouseFilesThread.setDaemon(true);
        toggleGUIDisable();
        openMouseFilesThread.start();
    }

    private void toggleGUIDisable() {
        componentPane.setDisable(!componentPane.isDisable());
    }

    //sets the table's placeholder text to an error message if something failed (most likely invalid data injected into a file)
    private void handleError(WorkerStateEvent workerStateEvent) {
        var exception = workerStateEvent.getSource().getException().getMessage();
        Label errorPlaceholder = new Label("Could not retrieve saved mice, because" + exception);
        table.placeholderProperty().setValue(errorPlaceholder);
        toggleGUIDisable();
    }

    //loads every added component to the tableview.
    private void handleSucceed(WorkerStateEvent workerStateEvent) {
        table.getItems().setAll((List<Mouse>) opener.getValue());
        toggleGUIDisable();
    }

    //back-button that loads the view where the user selects what added components to see
    @FXML
    private void viewSwapper(){
        try {
            App.changeView("/fxml/viewAddedComponents.fxml", 1200, 900);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //this function gets the selected item, asks for confirmation and sends the component to be deleted, after it was deleted successfully the tableview
    //is updated to reflect this deletion. if one presses the delete button without selecting a component, an error pop-up will appear.
    @FXML
    private void deleteMouse(){
        try{
            final File folder = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/");
            Mouse toBeDeleted = table.getSelectionModel().getSelectedItem();
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

    //this seperate thread does the same task as startThread(). The reason we needed a seperate function to update the tableview after deletion
    // is because thread.start() will only run once. If we tried to run the same thread again, it would not actually perform its task.
    private void startDeleteThread(){
        Thread openFilesThread = new Thread(deleter);
        deleter.setOnSucceeded(this::handleDeleteSucceed);
        deleter.setOnFailed(this::handleDeleteError);
        openFilesThread.setDaemon(true);
        toggleGUIDisable();
        openFilesThread.start();
    }
    //resets the tableview so it only contains the components that are saved. Without this the deleted item would still appear.
    private void handleDeleteSucceed(WorkerStateEvent workerStateEvent) {
        try{
            table.getItems().setAll((List<Mouse>) deleter.getValue());
            if(table.getItems().toArray().length == 0){
                table.placeholderProperty().setValue(new Label("No mice saved"));
            }
        } catch (NullPointerException e){
            table.placeholderProperty().setValue(new Label("Something went wrong"));
        }
        toggleGUIDisable();
    }

    private void handleDeleteError(WorkerStateEvent workerStateEvent) {
        Alert errorBox = new Alert(Alert.AlertType.ERROR);
        errorBox.setTitle("Something went wrong while deleting");
        toggleGUIDisable();
    }

    private void search(String query) {
        List<Mouse> newList;
        OpenAddedComponents searcher = new OpenMice(false);
        try{
            List<Mouse> listToSearch = (List<Mouse>) searcher.perform();
            table.getItems().clear();
            switch (filter.getValue()){
                case "Name":
                    newList = listToSearch.stream().filter(c -> c.getProductName().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
                    table.getItems().addAll(newList);
                    break;
                case "Serial number":
                    newList = listToSearch.stream().filter(c -> c.getSerialNumber().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
                    table.getItems().addAll(newList);
            }
        } catch (IOException | ValidationException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startSearch(){
        querySearch.textProperty().addListener((observable, oldText, newText) -> {
            search(newText);
        });
    }

    //because of Java-FX's quirks, we needed a single function for every single tablecolumn that could be edited. All these functions
    //perform the same task, which is:
    // - get a hold of the original value of the edited tablecell
    //   (if the computercomponent did not validate, the tablecell
    //   would still show the wrong value after committing the edit).
    //   This is used for rolling it back to the original value if a validationException is thrown.
    //
    // - create a path using the selected components product name (filenames are the product names)
    //
    // - use the model's set-methods to change the selected object
    //
    // - validate the new object
    //
    // - if no ValidationException is thrown, edit the existing file to reflect the new changes (This is a bit different in editName(). See the comment there.)
    //
    // - since the tableview's edit methods are dynamic, we dont need to update it the same way our delete solution does, table.refresh() is enough.

    @FXML
    private void editDescription(TableColumn.CellEditEvent cellEditEvent){
        String originalDescription = cellEditEvent.getOldValue().toString();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
        try{
            selectedMouse.setDescription(cellEditEvent.getNewValue().toString());
            selectedMouse.validate();
            AdminLogic.editFile(file, selectedMouse);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedMouse.setDescription(originalDescription);
            table.refresh();
        }
    }

    @FXML
    private void editPrice(TableColumn.CellEditEvent cellEditEvent){
        double originalPrice = (double)cellEditEvent.getOldValue();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");

        try{
            if(String.valueOf(cellEditEvent.getNewValue()).equals("NaN")){
                throw new ValidationException("");
            }
            selectedMouse.setPrice((double)cellEditEvent.getNewValue());
            selectedMouse.validate();
            AdminLogic.editFile(file, selectedMouse);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText("Invalid price");
            errorBox.showAndWait();
            selectedMouse.setPrice(originalPrice);
            table.refresh();
        }
    }

    @FXML
    private void editName(TableColumn.CellEditEvent cellEditEvent) {
        String originalName = cellEditEvent.getOldValue().toString();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File originalFile = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
        try{
            selectedMouse.setProductName(cellEditEvent.getNewValue().toString());
            selectedMouse.validate();

            //because components are stored with their product names as their file names, we need to take extra care not to
            //save a component to a new file just because their name has been updated (FileOutputStream creates a new file if one is not found).
            // This if-test checks if newFile and originalFile are the same, and if not - updates the old filename before doing any outputstreams.
            File newFile = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
            if (newFile.equals(originalFile)){
                AdminLogic.editFile(originalFile, selectedMouse);
            } else {
                //difference detected, change the filename before outputstreaming "new" object
                if (AdminLogic.editFileName(originalFile, newFile)){
                    AdminLogic.editFile(newFile, selectedMouse);
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
            selectedMouse.setProductName(originalName);
            table.refresh();
        }
    }

    @FXML
    private void editManufacturer(TableColumn.CellEditEvent cellEditEvent) {
        String originalManufacturer = cellEditEvent.getOldValue().toString();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
        try{
            selectedMouse.setProductionCompany(cellEditEvent.getNewValue().toString());
            selectedMouse.validate();
            AdminLogic.editFile(file, selectedMouse);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            selectedMouse.setProductionCompany(originalManufacturer);
            errorBox.showAndWait();
            table.refresh();
        }
    }

    @FXML
    private void editSerialNumber(TableColumn.CellEditEvent cellEditEvent) {
        String originalSerialNumber = cellEditEvent.getOldValue().toString();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
        try{
            selectedMouse.setSerialNumber(cellEditEvent.getNewValue().toString());
            selectedMouse.validate();
            AdminLogic.editFile(file, selectedMouse);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedMouse.setSerialNumber(originalSerialNumber);
            table.refresh();
        }
    }

    @FXML
    private void editWireless(TableColumn.CellEditEvent cellEditEvent) {
        String originalWireless = cellEditEvent.getOldValue().toString();
        Mouse selectedMouse = (Mouse)cellEditEvent.getRowValue();
        File file = new File("src/main/java/com/sample/DAL/SavedFiles/NewComponents/Mice/"+selectedMouse.getProductName()+".jobj");
        try{
            selectedMouse.setIsWireLess(cellEditEvent.getNewValue().toString());
            selectedMouse.validate();
            AdminLogic.editFile(file, selectedMouse);
            table.refresh();
        } catch (IOException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Something went wrong while editing file");
        } catch (ValidationException e) {
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setHeaderText(e.getLocalizedMessage());
            errorBox.showAndWait();
            selectedMouse.setIsWireLess(originalWireless);
            table.refresh();
        }
    }
}
