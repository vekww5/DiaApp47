package com.example.diaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout textInputEmail;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        
        textInputEmail = findViewById(R.id.text_input_email);
    }

    public void OnClickResetPassword(View view) {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()){
            textInputEmail.setError("Поле не должно быть пустым");
        }else {
            textInputEmail.setError(null);
            textInputEmail.setErrorEnabled(false);
            auth.sendPasswordResetEmail(emailInput).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ResetPasswordActivity.this,
                                "Мы отправили вам письмо с ссылкой для восстановления пароля",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        textInputEmail.setError("Неверный Email-адрес, пожалуйста введите другой");
                    }
                }
            });
        }
    }
}