package com.sample.controllers.regularUserControllers;

import com.sample.App;
import com.sample.Models.Computer.ComputerWithAccessories;
import com.sample.Models.Users.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class saveAccessorisedComputer {
    private regularUserController connector = new regularUserController();
    private static addAccessoriesController aac = new addAccessoriesController();
    private static User loggedInUser = aac.getLoggedInUser();
    private ComputerWithAccessories computerToBeSaved = (ComputerWithAccessories) loggedInUser.getComputerInProduction();
    @FXML
    private VBox container;
    @FXML
    void initialize() {
        placeComponentInfo();
    }
    private void placeComponentInfo() {
        List<Node> aps = container.getChildren();
        ((Label)((AnchorPane)aps.get(0)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getComputerCase().getProductName());
        ((Label)((AnchorPane)aps.get(1)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getMemory().getProductName());
        ((Label)((AnchorPane)aps.get(2)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getCPU().getProductName());
        ((Label)((AnchorPane)aps.get(3)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getCooling().getProductName());
        ((Label)((AnchorPane)aps.get(4)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getMotherboard().getProductName());
        ((Label)((AnchorPane)aps.get(5)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getGraphicsCard().getProductName());
        ((Label)((AnchorPane)aps.get(6)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getStorageComponent().getProductName());
        ((Label)((AnchorPane)aps.get(7)).getChildren().get(1)).setText(computerToBeSaved.getComputer().getPowerSupply().getProductName());
        DecimalFormat df2 = new DecimalFormat("#0.00");
        ((Label)((AnchorPane)aps.get(12)).getChildren().get(1)).setText("$ "+df2.format(computerToBeSaved.getPrice()));
        if(computerToBeSaved.getMonitor() != null) {
            ((Label)((AnchorPane)aps.get(8)).getChildren().get(1)).setText(computerToBeSaved.getMonitor().getProductName());
        }
        if(computerToBeSaved.getSpeaker() != null) {
            ((Label)((AnchorPane)aps.get(9)).getChildren().get(1)).setText(computerToBeSaved.getSpeaker().getProductName());
        }
        if(computerToBeSaved.getMouse() != null) {
            ((Label)((AnchorPane)aps.get(10)).getChildren().get(1)).setText(computerToBeSaved.getMouse().getProductName());
        }
        if(computerToBeSaved.getKeyboard() != null) {
            ((Label)((AnchorPane)aps.get(11)).getChildren().get(1)).setText(computerToBeSaved.getKeyboard().getProductName());
        }
    }
    @FXML
    void back(ActionEvent event) throws IOException {
        App.changeView("/fxml/BuildComputer/addAccessories.fxml", 0, 0);
    }

    @FXML
    void logOut(ActionEvent event) throws IOException {
        connector.logOut();
    }

    @FXML
    void onClickHome(ActionEvent event) throws IOException {
        connector.onClickHome();
    }

    @FXML
    void onClickMyComputers(ActionEvent event) {
        connector.onClickMyComputers();
    }

    @FXML
    void onClickSave(ActionEvent event) {

    }

}
