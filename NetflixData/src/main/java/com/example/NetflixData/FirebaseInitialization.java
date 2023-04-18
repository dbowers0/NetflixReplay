package com.example.NetflixData;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FirebaseInitialization {
    @PostConstruct
    public void init() throws IOException {

        String filePath = "C:\\Users\\bower\\NetflixData\\NetflixData\\src\\main\\resources\\firebase-adminsdk.json";
        FileInputStream serviceAccount =
                new FileInputStream(filePath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://netflix-data-47399-default-rtdb.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

    }
}
