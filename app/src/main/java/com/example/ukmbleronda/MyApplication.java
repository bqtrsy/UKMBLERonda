package com.example.ukmbleronda;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyAEJWQhLD7DSIooHAYTHGvu9ELsr-FKXKw")
                .setApplicationId("1:89437287621:web:54b72c9f19097cee8eecac")
                .setProjectId("ukmronda")
                .setStorageBucket("ukmronda.appspot.com")
                .build();

        FirebaseApp.initializeApp(this, options);
    }
}
