package com.wip.major.mytrail;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Users {
    private String mName;
    private String mPass;
    private String mEmail;
    private String mUID;

    public Users(String name, String email, String pass, String uid){
        mName = name;
        mPass = pass;
        mEmail = email;
        mUID = uid;
    }

    public void signUp(final Users user){
        Log.i("signup", "firebase");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(user.mEmail, user.mPass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users");
                            //start next activity
                            mDatabase.child(user.mUID).child("Name").setValue(user.mName);
                            mDatabase.child(user.mUID).child("Email").setValue(user.mEmail);
                            Log.i("firebase", "signed in");
                        }
                        else{
                            Log.i("firebase", "not signed in");
                            Log.i("firebase", task.getException().getMessage());
                        }

                    }
                });
    }
}
