package com.example.diaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    private TextInputLayout textInputEmail, textInputPassword;
    private TextView tvRegister, tvforgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        textInputEmail = findViewById(R.id.text_input_email_login);
        textInputPassword = findViewById(R.id.text_input_password_login);

        tvRegister = findViewById(R.id.text_view_account_register);
        tvforgetPassword = findViewById(R.id.text_view_forget_password);
    }

    private boolean isNetworkConnect() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Убеждаемся, что пользователь вошел в систему (не ноль), и обновляем пользовательский интерфейс соответствующим образом.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
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

    public void OnClickLogIn(View view) {

        if (isNetworkConnect()) {
            String email = validEmail();
            String password = validPassword();

            if ((email != null) && (password != null)) {

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Успешный вход, обновите пользовательский интерфейс, указав информацию о вошедшем пользователе
                                    FirebaseUser user = auth.getCurrentUser();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    //Если войти не удалось, отобразите сообщение для пользователя.
                                    Toast.makeText(LoginActivity.this, "Введен неверный логин или пароль",
                                            Toast.LENGTH_SHORT).show();
                                }
                                // ...
                            }
                        });
            }
        }else {
            Toast.makeText(LoginActivity.this, "Нет доступа к интернету",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnClickRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void OnClickOpenResetPassword(View view) {
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(intent);
    }
}