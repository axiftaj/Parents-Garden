package com.example.pg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterSpecialListActivity extends AppCompatActivity {

    EditText mEmailtEt , mPasswordEt , mNameEt , mUserNameEt , mSpeciaLiSTEt ;
    Button mRegisterBtn ;
    TextView mHaveAccountTv ;
    ProgressDialog progressDialog ;

    private FirebaseAuth mAuth ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_special_list);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mEmailtEt = findViewById(R.id.emailET);
        mPasswordEt = findViewById(R.id.passwordEt);
        mNameEt = findViewById(R.id.nameET);
        mUserNameEt = findViewById(R.id.userNameET);
        mRegisterBtn = findViewById(R.id.regisertBtn);
        mHaveAccountTv = findViewById(R.id.have_an_accountTv);
        mSpeciaLiSTEt = findViewById(R.id.specialistET);



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User..");

        mAuth = FirebaseAuth.getInstance() ;

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailtEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String name = mNameEt.getText().toString().trim();
                String userName = mUserNameEt.getText().toString().trim();
                String specialistaion = mSpeciaLiSTEt.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailtEt.setError("Invalid Email");
                    mEmailtEt.setFocusable(true);
                }else if (password.length() < 8){
                    mPasswordEt.setError("Passord lenght must be at least 8 character ");
                    mPasswordEt.setFocusable(true);
                }else if(name.isEmpty()){
                    mNameEt.setError("enter name");
                }else if (userName.isEmpty()){
                    mUserNameEt.setError("enter user name");
                }else if(specialistaion.isEmpty()){
                    mSpeciaLiSTEt.setError("enter specialistaion");
                }
                else {
                    registerUser( name , userName ,email , password , specialistaion);
                }
            }
        });

        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterSpecialListActivity.this , LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(final String name, final String userName, String email, String password, final String specialistaion) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = user.getEmail();
                            String uid = user.getUid();

                            //when user is register store user information in database
                            HashMap<Object , String> hashMap = new HashMap<>();
                            hashMap.put("email" , email);
                            hashMap.put("uid" , uid);
                            hashMap.put("name" , name );
                            hashMap.put("userName" , userName);
                            hashMap.put("specialistaion" , specialistaion);


                            //intilizing firebase database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store the user data nameed "Users"

                            DatabaseReference reference = database.getReference("Users");
                            DatabaseReference ref = database.getReference("Specialisation");


                            //put data with in hashmap in database
                            reference.child(uid).setValue(hashMap);
                            ref.child(uid).setValue(hashMap);

                            startActivity(new Intent(RegisterSpecialListActivity.this , DashboardActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();

                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterSpecialListActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterSpecialListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}