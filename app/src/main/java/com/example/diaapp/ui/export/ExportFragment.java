package com.example.diaapp.ui.export;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.diaapp.DataUser;
import com.example.diaapp.R;
import com.example.diaapp.ui.data_time.DatePickerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ExportFragment extends Fragment {

    private RadioGroup rGroupPeriod, rGroupFormat;
    private RadioButton rbSelectDate;
    private Button btnExport;
    private TextView tvEndDate, tvStartDate;
    private LinearLayout llStartDate, llEndDate, llSelectDate;

    private DatabaseReference ref;
    private FirebaseAuth auth;

    private Calendar calendarStart;
    private Calendar calendarEnd;
    boolean boolDateStartOrEnd;

    long _currentTime;
    long _7days;
    long _1month;

    long _3months;

    SimpleDateFormat dateFormat;
    private String TAG = "dialog";

    //создание фрагмента экспорта
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_export, container, false);

        //создание фомата отображении даты
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        //получение текущей даты
        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        //иниц. переменных tvStartDate и tvEndDate
        tvStartDate = root.findViewById(R.id.textViewExportStartDate);
        tvEndDate = root.findViewById(R.id.textViewExportEndDate);

        calendarStart.add(Calendar.MONTH, -1);

        tvStartDate.setText(dateFormat.format(calendarStart.getTime()));
        tvEndDate.setText(dateFormat.format(calendarEnd.getTime()));

        Calendar calendar = Calendar.getInstance();

        _currentTime = calendar.getTimeInMillis();
        //данные за 7
        _7days = _currentTime - (7 * 24 * 60 * 60 * 1000L);
        //данные за месяц
        calendar.add(Calendar.MONTH, -1);
        _1month = calendar.getTimeInMillis();
        //данные за 3 месяца
        calendar.add(Calendar.MONTH, -2);
        _3months = calendar.getTimeInMillis();

        //инициализация переченных для работы с базой данных
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("User");
        ref.keepSynced(true);

        //иниц. переменных и установка обработчиков
        btnExport = root.findViewById(R.id.btn_export);
        rGroupPeriod = root.findViewById(R.id.radioGroupPeriod);
        rGroupFormat = root.findViewById(R.id.radioGroupFormatExport);

        llSelectDate = root.findViewById(R.id.ll_select_date);
        llSelectDate.setVisibility(View.GONE);

        rbSelectDate = root.findViewById(R.id.radioButtonSelectDate);

        rGroupPeriod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbSelectDate.getId() == checkedId){
                    llSelectDate.setVisibility(View.VISIBLE);
                }else {
                    llSelectDate.setVisibility(View.GONE);
                }
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectPeriod = rGroupPeriod.getCheckedRadioButtonId();
                int selectFormat = rGroupFormat.getCheckedRadioButtonId();

                String del;
                if (selectFormat == R.id.radioButtonExportCsv) {
                    del = ",";
                } else {
                    del = "\t" ;
                }
                switch (selectPeriod) {
                    case R.id.radioButtonWeak:
                        export(root, _7days, _currentTime, del);
                        break;
                    case R.id.radioButtonMonth:
                        export(root, _1month, _currentTime, del);
                        break;
                    case R.id.radioButtonThreeMonths:
                        export(root, _3months, _currentTime, del);
                        break;
                    case R.id.radioButtonSelectDate:
                        export(root, calendarStart.getTimeInMillis(), calendarEnd.getTimeInMillis(), del);
                        break;
                }
            }
        });

        llStartDate = root.findViewById(R.id.ll_start_date);
        llEndDate = root.findViewById(R.id.ll_end_date);

        llStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(calendarStart);
                boolDateStartOrEnd = true;
            }
        });
        llEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(calendarEnd);
                boolDateStartOrEnd = false;
            }
        });

        return root;
    }

    // метод эспорта данных
    public void export (final View view, final long startDate, final long endDate, final String del){

        // запрос на получение данных за выбранный период
        ref.child(Objects.requireNonNull(auth.getUid()))
                .child("Notes")
                .orderByChild("timestamp")
                .startAt(startDate)
                .endAt(endDate)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // формирование заголовка данных
                        StringBuilder exportData = new StringBuilder();
                        exportData.append('"' + "Продленный инсулин" + '"').append(del).append('"')
                                .append("Короткий инсулин").append('"').append(del).append('"')
                                .append("Сахар").append('"').append(del).append('"')
                                .append("Хлебные единицы").append('"').append(del).append('"')
                                .append("Дата").append('"').append(del).append('"').append("Время")
                                .append('"');
                        // заполнение данными
                        for (DataSnapshot ds : snapshot.getChildren()){
                            DataUser user = ds.getValue(DataUser.class);
                            exportData.append("\n").append('"'+user.getInject_long()+'"')
                                    .append(del).append('"'+user.getInject_short()+'"').append(del)
                                    .append('"'+user.getGlucose()+'"').append(del).append('"'+user.getXe()+'"')
                                    .append(del).append('"'+user.getString_date()+'"').append(del)
                                    .append('"'+user.getString_time()+'"');
                        }
                        sendDataFromSelectPeriod(view, exportData, startDate, endDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled");
                    }
                });
    }

    // метод отправки файла на почту
    private void sendDataFromSelectPeriod(View view, StringBuilder eDBuilder, long startDate, long endDate){
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(startDate);
        String startDateString = dateFormat.format(calendar.getTime());

        calendar.setTimeInMillis(endDate);
        String endDateString = dateFormat.format(calendar.getTime());

        String fileNameExport = "Export " + startDateString + "-" + endDateString + ".csv";

        try {
            FileOutputStream out = view.getContext().openFileOutput(fileNameExport, Context.MODE_PRIVATE);
            out.write((eDBuilder.toString()).getBytes());
            out.close();

            Context context = view.getContext();
            File filelocation = new File(context.getFilesDir(), fileNameExport);
            Uri path = FileProvider.getUriForFile(context, "com.example.diaapp.fileprovider", filelocation);

            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/scv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Экспорт данных DiaApp: " + startDateString + "-" + endDateString);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent,"Send email"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // создание диалога для выюора даты
    private void showDatePicker(Calendar calender ) {
        DatePickerFragment date = new DatePickerFragment();
        /**
         * Задаем текущую дату в диалоговом окне
         */
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Задаем обратный вызов для получение выбранной даты
         */
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // определяем поле для вывода и сохранения даты, начала или конца
            if (boolDateStartOrEnd){
                calendarStart.set(Calendar.YEAR, year);
                calendarStart.set(Calendar.MONTH, monthOfYear);
                calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvStartDate.setText(dateFormat.format(calendarStart.getTime()));
            }else {
                calendarEnd.set(Calendar.YEAR, year);
                calendarEnd.set(Calendar.MONTH, monthOfYear);
                calendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvEndDate.setText(dateFormat.format(calendarEnd.getTime()));
            }
        }
    };


}