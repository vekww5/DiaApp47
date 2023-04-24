package com.example.diaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.diaapp.ui.data_time.DatePickerFragment;
import com.example.diaapp.ui.data_time.TimePickerFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static java.text.DateFormat.getDateTimeInstance;

public class ManagementDiaryDataActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener{

    private FirebaseAuth auth;
    private TextView tvTime, tvDate, tvNumbers, tvUnits;
    private Button bAddRecord, bEditRecord;
    private SeekBar sbEdHalf, sbEd;
    private DatabaseReference ref;

    private float ProgressValueEd = 0;
    private float ProgressValueHalfEd = 0;

    private DataUser dataUser;
    private int selectData;
    private Calendar calendar;
    private String TAG = "dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_diary);

        ref = FirebaseDatabase.getInstance().getReference("User");
        auth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();

        tvDate = findViewById(R.id.text_view_date);
        tvTime = findViewById(R.id.text_view_time);

        getIntentData();

        Spinner spinner = findViewById(R.id.spinner_select);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.list_data)
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner.setAdapter(adapter);

        tvUnits = findViewById(R.id.text_view_ed_units);

        spinner.setSelection(selectData);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveDataUser(ProgressValueEd + ProgressValueHalfEd, selectData);

                float number =  getPositionSeek(position);
                int c = (int)number;
                sbEd.setProgress(c);
                sbEdHalf.setProgress(Math.round((number - c)*10));

                selectData = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tvNumbers = findViewById(R.id.text_view_numbers);
        tvNumbers.setText("0.0");

        sbEdHalf = findViewById(R.id.seek_bar_ed_0_1);
        sbEdHalf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ProgressValueHalfEd = (float) (progress * 0.1);
                tvNumbers.setText(String.valueOf(ProgressValueEd + ProgressValueHalfEd));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveDataUser((ProgressValueEd + ProgressValueHalfEd), selectData);
            }
        });

        sbEd = findViewById(R.id.seek_bar_ed_1);
        sbEd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ProgressValueEd = progress;
                tvNumbers.setText(String.valueOf(ProgressValueEd + ProgressValueHalfEd));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvNumbers.setText(String.valueOf(ProgressValueEd + ProgressValueHalfEd));
                saveDataUser((ProgressValueEd + ProgressValueHalfEd), selectData);
            }
        });

        bEditRecord = findViewById(R.id.button_edit_record);
        bAddRecord = findViewById(R.id.button_add_record);
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        int action = -1; // action - действие 0 - добавление, 1 - изменение
        //задаем название заголовка сверху
        ActionBar ab = getSupportActionBar();

        if (extras != null) {
            action = extras.getInt("action");
            switch (action) {
                case 0:
                    if(ab != null){ ab.setTitle("Добавление новой записи"); }
                    selectData = extras.getInt("select",0); // select - передает значение  от 0 - 3 (нажатая кнопка)
                    dataUser = new DataUser("0.0","0.0","0.0","0.0",
                            "0.0","0.0", calendar.getTimeInMillis());
                    tvDate.setText(getDateString(calendar.getTimeInMillis()));
                    tvTime.setText(getTimeString(calendar.getTimeInMillis()));
                    break;
                case 1:
                    if(ab != null){ ab.setTitle("Изменение записи"); }

                    HashMap<String, Object> map = (HashMap<String, Object>) getIntent()
                            .getSerializableExtra("data");
                    assert map != null;

                    //TODO придумать как сделать лучше
                    String temp = null;
                    for(HashMap.Entry<String, Object> entry : map.entrySet())
                    {
                        if(entry.getValue().equals("--")) {
                            map.put(entry.getKey(), "0.0");
                        }else {
                            // подумать как переделать
                            if ((entry.getKey().equals("inject_long")) || (entry.getKey().equals("inject_short"))
                                    || (entry.getKey().equals("glucose"))|| (entry.getKey().equals("xe"))
                                    || (entry.getValue().equals("0.0"))) {
                                temp = entry.getKey();
                            }
                        }
                    }

                    //установка данный ползунков и списка
                    assert temp != null;
                    selectData = getIndexData(temp);
                    float f = Float.parseFloat((String) map.get(temp));
                    int c = (int) f;
                    ProgressValueEd = c;
                    ProgressValueHalfEd = f - c;

                    dataUser = new DataUser(
                            (String) map.get("inject_long"),
                            (String) map.get("inject_short"),
                            (String) map.get("glucose"),
                            (String) map.get("xe"),
                            (String) map.get("date"),
                            (String) map.get("time"),
                            (Long) map.get("timestamp"));

                    long timestamp = (long) map.get("timestamp"); //TODO много повторений map.get("timestamp") положит в переменную
                    tvDate.setText(getDateString(timestamp));
                    tvTime.setText(getTimeString(timestamp));
                    calendar.setTimeInMillis(timestamp);
                    break;
            }
        }
    }

    private int getIndexData (String data){
        int index = 0;
        switch (data){
            case "inject_short" : index = 1; break;
            case "glucose" : index = 2; break;
            case "xe" : index = 3; break;
        }
        return index;
    }

    private void saveDataUser (float number, int nameField) {
        String strNumber =  String.valueOf(number);

        switch (nameField){
            case 0 :
                dataUser.setInject_long(strNumber);
                break;
            case 1:
                dataUser.setInject_short(strNumber);
                break;
            case 2:
                dataUser.setGlucose(strNumber);
                break;
            case 3:
                dataUser.setXe(strNumber);
                break;
        }
    }


    private float getPositionSeek (int numField) {
        float temp = 0;
        switch (numField){
            case 0 :
                temp = Float.parseFloat(dataUser.getInject_long());
                tvUnits.setText("Ед");
                break;
            case 1:
                temp = Float.parseFloat(dataUser.getInject_short());
                tvUnits.setText("Ед");
                break;
            case 2:
                temp =  Float.parseFloat(dataUser.getGlucose());
                tvUnits.setText("Ммоль/л");
                break;
            case 3:
                temp = Float.parseFloat(dataUser.getXe());
                tvUnits.setText("ХЕ");
                break;
        }
        return temp;
    }

    //TODO не оптимально, но списку отображать данные легче
     DataUser checkValidData(DataUser dataUser){
        if (dataUser.getGlucose().equals("0.0")) {
            dataUser.setGlucose("--");
        }

        if (dataUser.getInject_long().equals("0.0")) {
            dataUser.setInject_long("--");
        }

        if (dataUser.getInject_short().equals("0.0")) {
            dataUser.setInject_short("--");
        }

        if (dataUser.getXe().equals("0.0")) {
            dataUser.setXe("--");
        }
        return dataUser;
    }

    private void selectButtonForShow(int index){
        if (index == 0){
            bAddRecord.setVisibility(View.VISIBLE);
            bEditRecord.setVisibility(View.GONE);
        }else {
            bAddRecord.setVisibility(View.GONE);
            bEditRecord.setVisibility(View.VISIBLE);
        }
    }

    public void OnClickUpdateRecord(View view) {
        DataUser tempUserData = dataUser;
        tempUserData.setString_date(tvDate.getText().toString().trim());
        tempUserData.setString_time(tvTime.getText().toString().trim());

        tempUserData.setTimestamp(calendar.getTimeInMillis());

        ref.child(Objects.requireNonNull(auth.getUid())).child("Notes")
                .child(Objects.requireNonNull(getIntent().getStringExtra("id_record")))
                .setValue(checkValidData(tempUserData))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Если успешно
                Toast.makeText(ManagementDiaryDataActivity.this, "Запись изменена.", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManagementDiaryDataActivity.this, "Ошибка! Запись не изменена.", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectButtonForShow(Objects.requireNonNull(getIntent().getExtras()).getInt("action"));
    }

    public void OnClickAddRecord(View view) {
        DataUser tempUserData = dataUser;
        tempUserData.setString_date(tvDate.getText().toString().trim());
        tempUserData.setString_time(tvTime.getText().toString().trim());

        tempUserData.setTimestamp(calendar.getTimeInMillis());

        ref.child(Objects.requireNonNull(auth.getUid())).child("Notes").push()
                .setValue(checkValidData(tempUserData)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ManagementDiaryDataActivity.this, "Запись добавлена.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManagementDiaryDataActivity.this, "Ошибка! Запись не добавлена.", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    public void OnClickDate(View view) {
        showDatePicker(calendar);
    }

    public void OnClickTime(View view) {
        DialogFragment timePicker = new TimePickerFragment(calendar);
        timePicker.show(getSupportFragmentManager(), TAG);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        tvTime.setText(String.format("%02d:%02d", hourOfDay, minute));
    }

    public String getTimeString(long timestamp){
        try{
            DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "time";
        }
    }

    public String getDateString(long timestamp){
        try{
            DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "time";
        }
    }

    private void showDatePicker(Calendar calender ) {
        DatePickerFragment date = new DatePickerFragment();
        /**
         * Set Up Current Date Into dialog
         */
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getSupportFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvDate.setText(dateFormat.format(calendar.getTime()));
        }
    };

}