package com.wip.major.mytrail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    private EditText mName;
    private EditText mEmail;
    private EditText mPass;
    private EditText mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mPass = (EditText) findViewById(R.id.password);
        mUID = (EditText) findViewById(R.id.UID);

        Button register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("singup", "register clicked");
                attemptSignup();
            }
        });
    }

    private void attemptSignup() {
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String pass = mPass.getText().toString().trim();
        String uid = mUID.getText().toString().trim();
        Log.i("signup",email);
        Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
        Users mUser = new Users(name, email, pass, uid);
        mUser.signUp(mUser);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!= null) {
            Intent mIntent = new Intent(this, FrontActivity.class);
            SharedPreferences pref = getApplicationContext().getSharedPreferences("session", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("email", email);
            editor.putString("pass", pass);
            this.startActivity(mIntent);
        }
        else{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
