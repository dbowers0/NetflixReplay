package com.example.NetflixData;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
@Service
public class FirebaseInitialization {
    @Value("${FIREBASE_ADMIN_SDK}")
    private String jsonString;
    @PostConstruct
    public void init() throws IOException {
        JSONObject jsonObject = new JSONObject(jsonString);
        File file = new File("firebase.json");
        try (FileWriter fileWriter = new FileWriter(file)) {
            // Write the JSON content to the file
            fileWriter.write(jsonObject.toString());
            // Or for JSONArray: fileWriter.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = file.getAbsolutePath();

        FileInputStream serviceAccount =
                new FileInputStream(filePath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://netflix-data-47399-default-rtdb.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

    }
}
