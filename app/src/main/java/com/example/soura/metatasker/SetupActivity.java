package com.example.soura.metatasker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SetupActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    EditText NameField;

    private DatabaseReference mDatabaseUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");

        NameField=(EditText)findViewById(R.id.name);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    private void logout()
    {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI();
                    }
                });
    }

    private void updateUI()
    {
        //Toast.makeText(SetupActivity.this,"",Toast.LENGTH_SHORT).show();
        Intent account = new Intent(SetupActivity .this,LoginActivity.class);
        startActivity(account);
        finish();
    }

    public void later(View view)
    {
        logout();
    }

    public void setup(View view)
    {
        startSetupAccount();
    }

    private void startSetupAccount()
    {
        String device_Token = FirebaseInstanceId.getInstance().getToken();
        String name= NameField.getText().toString().trim();
        String userid=mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name))
        {
            mDatabaseUsers.child(userid).child("device_token").setValue(device_Token);
            mDatabaseUsers.child(userid).child("name").setValue(name);
            mDatabaseUsers.child(userid).child("image").setValue("default");
            mDatabaseUsers.child(userid).child("thumb_image").setValue("default");

            updateUI();


        }

    }
}
