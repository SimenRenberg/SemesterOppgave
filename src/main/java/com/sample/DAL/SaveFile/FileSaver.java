package com.sample.DAL.SaveFile;

import com.sample.BLL.ComponentFactory;
import com.sample.Models.ComputerComponents.ComputerComponent;
import com.sample.Models.ComputerComponents.CoolingSystem;
import com.sample.Models.Users.User;
import javafx.concurrent.Task;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;

abstract public class FileSaver extends Task<Boolean> {
    protected final Path path = Paths.get("src/main/java/com/sample/DAL/SavedFiles/Users.txt");
    protected User userToRegister;
    public FileSaver(User userToRegister) {
        this.userToRegister = userToRegister;
    }

     boolean writeToFile() throws IOException {
         // Declaring two long fileSize variables, one before writing and one after writing. If the size is changed after writing the writing went successfully
         long fileSize = Files.size(path);
         String contentToWrite = userToRegister.getMail()+":"+userToRegister.getPassword()+":"+userToRegister.getAdmin()+"\n";
         Files.write(path, contentToWrite.getBytes(), StandardOpenOption.APPEND);
         long fileSizeAfter = Files.size(path);
         System.out.println(fileSize+"------"+fileSizeAfter);
         System.out.println(fileSizeAfter>fileSize);
         return fileSizeAfter>fileSize;
     }

    //this function takes in a computercomponent and a string identifying which type of component it is. Because we save every component
    //to their own JOBJ file, we first need to figure out which path to generate. after generating the correct path we add the components
    //product name to the end of the path, to get the component name as the filename.
    public static <T extends ComputerComponent> Boolean saveComponent(T component, String type) throws IOException {
        Path filePath = Paths.get(ComponentFactory.createPath(type)+component.getProductName()+".jobj"); //saves in correct directory with components name as file name.
        try(FileOutputStream os = new FileOutputStream(String.valueOf(filePath)); ObjectOutputStream out = new ObjectOutputStream(os)) {
            out.writeObject(component);
            return true;
        }
    }
}
