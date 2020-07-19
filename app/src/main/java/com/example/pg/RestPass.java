package com.example.pg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RestPass extends AppCompatActivity {

    private Button sendEmail;
    private EditText userEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_pass);

        userEmail = findViewById(R.id.EmailEnter);
        sendEmail = findViewById(R.id.send);
        mAuth = FirebaseAuth.getInstance();
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString();
                if(TextUtils.isEmpty(email)){

                    Toast.makeText(RestPass.this,"Enter your correct Email here",Toast.LENGTH_SHORT).show();
                }
                else  {

                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener< Void >() {
                        @Override
                        public void onComplete(@NonNull Task< Void > task) {
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(RestPass.this, "Please visit your email",Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(RestPass.this,MainActivity.class));
                                }
                                else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(RestPass.this,"Eroror"+ message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                }
            }
        });







    }


}
