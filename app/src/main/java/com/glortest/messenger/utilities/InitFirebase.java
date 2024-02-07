package com.glortest.messenger.utilities;

import com.google.firebase.firestore.FirebaseFirestore;

public class InitFirebase {
    public static FirebaseFirestore firebaseFirestore;
    public static void init(){
        firebaseFirestore = FirebaseFirestore.getInstance();
    }
}
