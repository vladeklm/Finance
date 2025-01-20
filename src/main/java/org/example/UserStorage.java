package org.example;

import org.example.Exceptions.UserAlreadyExistsException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserStorage implements FileLoadable{
    private final String fileName;
    private Map<String, Integer> usernamePassword;

    public UserStorage() {
        fileName = "UserStorage.txt";
        usernamePassword = new HashMap<>();
    }

    public boolean registerUser(String username, String password) {
        var alreadyContainUser = usernamePassword.containsKey(username);
        if (alreadyContainUser) {
            throw new UserAlreadyExistsException("User already exist");
        }
        usernamePassword.put(username, password.hashCode());
        return true;
    }

    public boolean verifyUser(String username, String password) {
        var actualPassword = usernamePassword.get(username);
        if (actualPassword != null) {
            if (actualPassword == password.hashCode()) {
                return true;
            }
        }
        return false;
    }

    public boolean deleteUser(String username) {
        throw new UserAlreadyExistsException("User not exist");
    }

    @Override
    public void loadFromFile() {
        usernamePassword = new HashMap<>();
        try (var reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var parts = line.split(" ");
                usernamePassword.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveToFile() {
        try (var fos = new FileOutputStream(new File(fileName), true)) {
            var writer = new OutputStreamWriter(fos);
            for(var entry : usernamePassword.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
