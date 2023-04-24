package com.example.diaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    private TextInputLayout textInputAgainPassword, textInputEmail, textInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        textInputAgainPassword = findViewById(R.id.text_input_layout_again_password);
        textInputEmail = findViewById(R.id.text_input_layout_email);
        textInputPassword = findViewById(R.id.text_input_layout_password);
    }

    // уточнить, так оставить или все же лечше true - false
    private String validEmail(){
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
       if (emailInput.isEmpty()){
           textInputEmail.setError("Поле не должно быть пустым");
           return null;
       }else{
           textInputEmail.setError(null);
           textInputEmail.setErrorEnabled(false);
           return emailInput;
       }
    }

    private String validUserName() {
        String nameInput = textInputAgainPassword.getEditText().getText().toString().trim();
        if (nameInput.isEmpty()){
            textInputAgainPassword.setError("Поле не должно быть пустым");
            return null;
        }else {
            if (!nameInput.equals(textInputPassword.getEditText().getText().toString().trim())) {
                textInputAgainPassword.setError("Пароли не совпадают");
                return null;
            }else {
                textInputAgainPassword.setError(null);
                textInputAgainPassword.setErrorEnabled(false);
                return nameInput;
            }
        }
    }

    private String validPassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()){
            textInputPassword.setError("Поле не должно быть пустым");
            return null;
        }else {
            textInputPassword.setError(null);
            textInputPassword.setErrorEnabled(false);
            return passwordInput;
        }
    }

    public void confirmInput(View v){

        String email = validEmail();
        String password = validPassword();
        String name = validUserName();

        if ((email != null) && (password != null ) && ( name != null)){
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(RegisterActivity.this, "Успешная регистрация", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    }else{
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthUserCollisionException e) {
                            Toast.makeText(RegisterActivity.this, "Такой пользователь уже есть.", Toast.LENGTH_SHORT).show();
                        } catch(Exception e) {
                            Toast.makeText(RegisterActivity.this, "Ошибка, попробуйте еще раз", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
