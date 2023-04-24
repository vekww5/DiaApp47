package com.example.diaapp.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.diaapp.InfoUser;
import com.example.diaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ProfileFragment extends Fragment  {

    private FirebaseAuth auth;
    private DatabaseReference ref;
    private FirebaseUser currentUser;

    private EditText etWeight, etHeight, etName, etLastName;
    private TextView tvDate, tvEmail, tvPassword;
    private Button btnSaveUserInfo;
    private ToggleButton tbDiaType, tbPol;
    private RelativeLayout rl_date_birth;
    private Spinner sTypeInjectShortSpinner, sTypeInjectLongSpinner;

    private HashMap<String, Object> listInjectShort;
    private HashMap<String, Object> listInjectLong;

    private String selectInjectShort = "";
    private String selectInjectLong = "";

    private Calendar calendar;
    private String TAG = "dialog";

    // создание фрагмента, иниц. элеметов
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);

        //иницыализация переченных дл работы с базой данных
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();

        // создание списков инсулинов
        listInjectShort = new HashMap<String, Object>();
        listInjectLong = new HashMap<String, Object>();

        //иницыализация переченных имени, фамилии
        etName = root.findViewById(R.id.edit_text_name_value);
        etLastName = root.findViewById(R.id.edit_text_name_last_value);

        tbDiaType = root.findViewById(R.id.toggle_button_dia);
        tbPol = root.findViewById(R.id.toggle_button_pol);

        tvPassword = root.findViewById(R.id.text_view_change_password);
        tvPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = auth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), "password1234");
                // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated.");
                                }else {
                                    Log.d(TAG, "User not re-authenticated. " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });

        tvEmail = root.findViewById(R.id.text_view_email);
        tvEmail.setText(currentUser.getEmail());

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        tvDate = root.findViewById(R.id.text_view_date);

        etWeight = root.findViewById(R.id.edit_text_weight_value);
        etHeight = root.findViewById(R.id.edit_text_height_value);

        etWeight.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(4,2)});
        etHeight.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(4,2)});

        //иниц. кнопки btnSaveUserInfo и задание обработчика
        btnSaveUserInfo = root.findViewById(R.id.button_save_user_info);
        btnSaveUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoUser infoUser = new InfoUser(
                        etName.getText().toString().trim(),
                        etLastName.getText().toString().trim(),
                        etHeight.getText().toString().trim(),
                        etWeight.getText().toString().trim(),
                        tbDiaType.getText().toString().trim(),
                        tbPol.getText().toString().trim(),
                        calendar.getTimeInMillis(),
                        selectInjectShort,
                        selectInjectLong);

                ref.child("User").child(auth.getUid()).child("UserInfo").setValue(infoUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(root.getContext(), "Данные сохранены.", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(root.getContext(), "Опс.. Произошла ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        rl_date_birth = root.findViewById(R.id.rl_date_birth);
        rl_date_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int myYear = calendar.get(Calendar.YEAR);
                int myMonth = calendar.get(Calendar.MONTH);
                int myDay = calendar.get(Calendar.DAY_OF_MONTH);

                // инициализируем диалог выбора даты текущими значениями
                DatePickerDialog datePickerDialog = new DatePickerDialog(root.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                calendar.set(year, monthOfYear, dayOfMonth);

                                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                                String currentDateString = dateFormat.format(calendar.getTime());

                                tvDate.setText(currentDateString);

                            }
                        }, myYear, myMonth, myDay);
                datePickerDialog.show();
            }
        });

        sTypeInjectShortSpinner = root.findViewById(R.id.spinner_inject_short);
        sTypeInjectLongSpinner = root.findViewById(R.id.spinner_inject_long);

        setupUserInfo(root);

        sTypeInjectShortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equals("Не выбран")){
                    selectInjectShort = "";
                }else {
                    selectInjectShort = item;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sTypeInjectLongSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equals("Не выбран")){
                    selectInjectLong = "";
                }else {
                    selectInjectLong = item;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        return root;
    }
    // заполнение повляющегося списка инсулинов
    private void setupSpinnerInject(View root, Spinner spinner, HashMap<String, Object> map){
        List<String> listKeys = new ArrayList<>();
        listKeys.add(0, "Не выбран");
        listKeys.addAll(map.keySet());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(root.getContext(),
                R.layout.custom_spinner_inject, listKeys);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private void setupUserInfo(final View root){

        // запрос на получение данных о пользователе и задание полей
        ref.child("User").child(auth.getUid()).child("UserInfo").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    InfoUser user = task.getResult().getValue(InfoUser.class);

                    if (user != null) {
                        etName.setText(user.getFirst_name());
                        etLastName.setText(user.getLast_name());
                        etHeight.setText(user.getHeight());
                        etWeight.setText(user.getWeight());

                        calendar.setTimeInMillis(user.getBirthday());

                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        String currentDateString = dateFormat.format(calendar.getTime());
                        tvDate.setText(currentDateString);

                        if(user.getType_dia().equals("II")) {
                            tbDiaType.setChecked(true);
                        }

                        if(user.getPol().equals("Ж")) {
                            tbPol.setChecked(true);
                        }

                        if (user.getType_short_insulin() != null){
                            selectInjectShort = user.getType_short_insulin();
                        }

                        if (user.getType_long_insulin() != null){
                            selectInjectLong = user.getType_long_insulin();
                        }
                    }
                }
            }
        });

        // запрос на получение наименований коротких инсуоинов
        ref.child("AppData").child("Short_Insulins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    HashMap<String, Object> map =  (HashMap) task.getResult().getValue();
                    listInjectShort.putAll(map);
                    setupSpinnerInject(root, sTypeInjectShortSpinner, map);

                    if (listInjectShort.get(selectInjectShort) != null){
                        selectSpinnerValue(sTypeInjectShortSpinner, selectInjectShort);
                    }
                }
            }});
        // запрос на получение наименований длинных инсуоинов
        ref.child("AppData").child("Long_Insulins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    HashMap<String, Object> map =  (HashMap) task.getResult().getValue();
                    listInjectLong.putAll(map);
                    setupSpinnerInject(root, sTypeInjectLongSpinner, map);

                    if (listInjectLong.get(selectInjectLong) != null){
                        selectSpinnerValue(sTypeInjectLongSpinner, selectInjectLong);
                    }
                }
            }});
    }

    private void selectSpinnerValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}