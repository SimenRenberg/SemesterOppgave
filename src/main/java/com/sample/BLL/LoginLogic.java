package com.sample.BLL;

import com.sample.DAL.OpenFile.OpenTxt;
import com.sample.Models.Users.User;
import com.sample.controllers.regularUserControllers.regularUserController;
import javafx.scene.control.Button;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;




public class LoginLogic {
    private static regularUserController controller = new regularUserController();
    private static final Path pathToUserFile = Paths.get("src/main/java/com/sample/DAL/SavedFiles/Users.txt");


    public static User validateSignIn(String mail, String password) throws IOException {
        OpenTxt opener = new OpenTxt(null);
        String hashedPassword = DigestUtils.shaHex(password);
        User userFromFile = opener.getUserTryingToLogIn(mail);
        if (userFromFile == null) {
            return null;
        }
        if (userFromFile.getMail().equals(mail) && userFromFile.getPassword().equals(hashedPassword)) {
            userFromFile.setLoggedIn(true);
            return userFromFile;
        } else {
            return null;
        }
    }



    /*public static HBox renderNavBar() throws IOException {
        HBox navBar = new HBox();
        Button btnHome = new Button("Home");
        Button btnMyComputers = new Button("My computers");
        Button btnLogOut = new Button("Log out");
        btnHome.setOnMouseClicked((e)-> {
            try {
                controller.onClickHome();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        btnMyComputers.setOnMouseClicked((e)->{
            controller.onClickMyComputers();
        });
        btnLogOut.setOnMouseClicked((e)-> {
            try {
                controller.logOut();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        navBar.getChildren().addAll(styleButtons(btnHome, btnMyComputers, btnLogOut));
        navBar.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 0");
        navBar.setLayoutY(0);
        navBar.setLayoutX(0);
        navBar.setPrefWidth(1200);
        navBar.setPrefHeight(100);
        navBar.setAlignment(CENTER_RIGHT);
        navBar.setSpacing(30);
        navBar.setPadding(new Insets(0, 50, 0, 0));
        return navBar;
    }*/

    private static Button[] styleButtons(Button... buttons) {
        for (Button btn : buttons) {
            btn.setStyle("-fx-border-color: black; -fx-background-color: white; -fx-cursor: hand");
            btn.setPrefWidth(200);
            btn.setPrefHeight(50);
        }
        return buttons;
    }


}
