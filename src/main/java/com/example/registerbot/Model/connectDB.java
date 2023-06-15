package com.example.registerbot.Model;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;


public class connectDB {
    public static String mongoUri;

    public connectDB(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        connectDB.mongoUri = mongoUri;
    }

    public void connectUserRegistration() {
        MongoClientURI uri = new MongoClientURI("mongodb+srv://quanphamlsc:quan_2002@chatbot.trqhh6o.mongodb.net/testDB");
        MongoClient client = new MongoClient(uri);
        MongoDatabase database = client.getDatabase("testDB");
        MongoCollection<Document> userCollection = database.getCollection(UserRegistration.USER_REGISTRATION);
    }
}
