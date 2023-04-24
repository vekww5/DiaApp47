package com.example.diaapp.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diaapp.DataUser;
import com.example.diaapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static java.text.DateFormat.getDateTimeInstance;

public class StatisticsFragment extends Fragment {

    private FirebaseAuth auth;

    private LineChartView chart_hello;
    private LineChartData chart_data_hello;

    private TextView tvAverageDay, tvAverageWeek, tvAverageMonth;
    private RadioGroup rgDayWeak;
    private RadioButton rbDay, rbWeak;

    private ArrayList<DataUser> _1dayData;
    private ArrayList<DataUser> _7daysData;
    private ArrayList<DataUser> _30daysData;

    private long currentTime;
    private long _1day;
    private long _7days;
    private long _30days;

    // создание фрагмента, иниц. элеметов
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        //инициализация переченных для работы с базой данных
        auth = FirebaseAuth.getInstance();

        //инициализация переченных
        tvAverageDay = root.findViewById(R.id.text_view_text_average_day);
        tvAverageWeek = root.findViewById(R.id.text_view_text_average_week);
        tvAverageMonth = root.findViewById(R.id.text_view_text_average_month);

        //инициализация переченных для работы с базой данных
        _1dayData = new ArrayList<DataUser>();
        _7daysData = new ArrayList<DataUser>();
        _30daysData = new ArrayList<DataUser>();

        setupDataChart(root);

        //инициализация  кнопок rbDay и rbWeak
        rbDay = root.findViewById(R.id.radioButtonDay);
        rbWeak = root.findViewById(R.id.radioButtonWeak);

        rgDayWeak = root.findViewById(R.id.radioGroupDayWeak);
        rgDayWeak.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbDay.getId() == checkedId){
                    rbDay.setTextColor(Color.WHITE);
                    rbWeak.setTextColor(Color.RED);

                    setupChartHello(root, _1dayData, _1day, currentTime);
                }else {
                    rbDay.setTextColor(Color.RED);
                    rbWeak.setTextColor(Color.WHITE);

                    getMapAverangForDays(_7daysData);
                    setupChartHello7days(root, _7daysData);
                }
            }
        });

        return root;
    }
    // получение данных за заданный переод времени
    private void setupQueryFirebase (final View view, final long startDate, final long endDate,
                                     final TextView tvAverage, final ArrayList<DataUser> listData,
                                     final boolean fillChart){

        Query query = FirebaseDatabase.getInstance().getReference("User").child(Objects.requireNonNull(auth.getUid()))
                .child("Notes").orderByChild("timestamp").startAt(startDate).endAt(endDate).limitToLast(100);

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    DataUser user = ds.getValue(DataUser.class);
                    assert user != null;

                    String glu =  user.getGlucose();
                    if (!glu.equals("--"))
                        listData.add(user);
                }
                // расчет среднего
                calcAverange(listData, tvAverage);
                // заполнение графика данными
                if (fillChart)
                    setupChartHello(view, listData, startDate, endDate);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "Ошибка! Данные не загружены", Snackbar.LENGTH_SHORT);
            }
        });
    }

    private void setupDataChart(final View root){

        //получение текущей даты
        currentTime = Calendar.getInstance().getTimeInMillis();

        //данные за 1, 7, 30 дней
        _1day = currentTime - (24 * 60 * 60 * 1000L);
        _7days = currentTime - (7 * 24 * 60 * 60 * 1000L);
        _30days = currentTime - (30 * 24 * 60 * 60 * 1000L);

        setupQueryFirebase(root, _1day, currentTime, tvAverageDay, _1dayData, true);
        setupQueryFirebase(root, _7days, currentTime, tvAverageWeek, _7daysData,false);
        setupQueryFirebase(root, _30days, currentTime, tvAverageMonth, _30daysData,false);
    }

    // метод расчета среднего
    private void calcAverange(List<DataUser> dataUserList, TextView textViewDate) {
        float averange_float = 0;
        DecimalFormat f = new DecimalFormat("#0.0");

        for (DataUser du : dataUserList) {
                averange_float += Float.parseFloat(du.getGlucose());
        }

        float af = averange_float/dataUserList.size();

        if ((Double.isNaN(af)) || (Double.isInfinite(af))) af = 0;

        textViewDate.setText(f.format(af) + " ммоль");
    }

    /**
     * Метод для расчета среднего за каждый день
     * @param dataUserList измерения показателей
     * @return сркднее за каждый лень
     */
    private HashMap<Integer, Float> getMapAverangForDays(List<DataUser> dataUserList){

        HashMap<Integer, Integer> mapCount = new HashMap<Integer, Integer>();
        HashMap<Integer, Float> mapValue = new HashMap<Integer, Float>();

        Calendar calendar = Calendar.getInstance();

        for (DataUser dul : dataUserList) {

            calendar.setTimeInMillis(dul.getTimestamp());
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            Float aFloat = mapValue.get(day);
            Integer integer = mapCount.get(day);

            if (aFloat != null)
                mapValue.put(day, aFloat + Float.parseFloat(dul.getGlucose()));
            else
                mapValue.put(day, Float.parseFloat(dul.getGlucose()));

            if (integer !=null)
                mapCount.put(day,  integer + 1);
            else
                mapCount.put(day, 1);
        }

        for(Map.Entry<Integer, Integer> entry : mapCount.entrySet()){
            Integer x = entry.getKey();
            float y = mapValue.get(entry.getKey()) / entry.getValue();

            mapValue.put(x, y);
        }

        return mapValue;
    }

    /**
     * Метод формерование осей Х для графика
     * @param start_time Начальная дата
     * @param end_time Конечная дата
     * @return ось Х со значениями
     */
    private Axis xAxis(long start_time, long end_time) {
        int periodInt = 0;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        DateFormat timeFormat = new SimpleDateFormat("HH", Locale.getDefault());

        Calendar calendarEndTime = Calendar.getInstance();
        Calendar calendarStartTime = Calendar.getInstance();
        calendarStartTime.setTimeInMillis(start_time);
        calendarStartTime.set(Calendar.MINUTE, 0);
        calendarStartTime.set(Calendar.SECOND, 0);
        calendarStartTime.set(Calendar.MILLISECOND, 0);

        periodInt = Calendar.HOUR;
        calendarStartTime.add(periodInt, 1);

        while (calendarStartTime.getTimeInMillis() < calendarEndTime.getTimeInMillis()) {
            axisValues.add(new AxisValue((calendarStartTime.getTimeInMillis()), (timeFormat.format(calendarStartTime.getTimeInMillis())).toCharArray()));
            calendarStartTime.add(periodInt, 1);
        }
        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setHasLines(true);
        axis.setTextColor(Color.BLACK);
        return axis;
    }

    //метод для заполнение и настройка графика
    private void setupChartHello(View root, List<DataUser> dataUser, long startDate, long endDate){
        Line line;
        List<PointValue> values = new ArrayList<PointValue>();

        chart_hello = root.findViewById(R.id.chart_hello);

        // заполнение данными
        for (DataUser dul : dataUser) {
            float y = Float.parseFloat(dul.getGlucose());
            long x = dul.getTimestamp();

            PointValue point = new PointValue(x, y);
            point.setLabel(dul.getGlucose());

            values.add(point);
        }
//---------------------------// рисует GRAY LINE
        line = new Line(values);

        line.setColor(Color.GRAY);
        line.setHasLines(false); // скрыть показать линии между точками
        line.setPointRadius(4);
        line.setHasPoints(true);
        line.setHasLabels(true);
        line.setHasLines(true);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
//--------------------------// рисует RED LINE
        List<PointValue> lineValuesRed = new ArrayList<PointValue>();
        lineValuesRed.add(new PointValue(startDate, 4));
        lineValuesRed.add(new PointValue(endDate, 4));

        Line lineRed  = new Line(lineValuesRed);
        lineRed.setColor(Color.RED);
        lineRed.setHasLines(true);
        lineRed.setStrokeWidth(1);
        lineRed.setHasPoints(false);
        lineRed.setHasLabels(false);
        lineRed.setFilled(true);
        lines.add(lineRed);
//---------------------------// рисует Yellow LINE
        List<PointValue> lineValuesYellow = new ArrayList<PointValue>();
        lineValuesYellow.add(new PointValue(startDate, 10));
        lineValuesYellow.add(new PointValue(endDate, 10));

        Line LineYellow  = new Line(lineValuesYellow);
        LineYellow.setColor(Color.YELLOW);
        LineYellow.setHasLines(true);
        LineYellow.setStrokeWidth(1);
        LineYellow.setHasPoints(false);
        LineYellow.setHasLabels(false);
        lines.add(LineYellow);
//-------------------------------------------------
        chart_data_hello = new LineChartData(lines);

        // заполняем оси Х значения
        Axis axisX = xAxis(startDate, endDate);
        axisX.setMaxLabelChars(2);
        // заполняем оси У значения
        Axis axisY = yAxis();

        // задание осей Х и У
        chart_data_hello.setAxisXBottom(axisX);
        chart_data_hello.setAxisYLeft(axisY);

        // задание режима отображения
        chart_hello.setLineChartData(chart_data_hello);
        chart_hello.setZoomType(ZoomType.HORIZONTAL);

        // задание области видимоти
        Viewport v = chart_hello.getMaximumViewport();
        v.set(startDate, v.top, endDate, 0);

        chart_hello.setMaximumViewport(v);
        chart_hello.setCurrentViewport(v);
    }

    // метод для заполнение и настройка графика 7 дней
    private void setupChartHello7days(View root, List<DataUser> dataUser){
        int numValues = 7;

        Line line;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();

        HashMap<Integer, Float> mapReturn = getMapAverangForDays(dataUser);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_WEEK, -numValues);

        DecimalFormat f = new DecimalFormat("##.0");
        for (int i = 0; i <= numValues; i++){

            int day = c.get(Calendar.DAY_OF_MONTH);
            Float y = mapReturn.get(day);

            if (y != null){
                PointValue point = new PointValue(i, y);
                point.setLabel(f.format(y));
                values.add(point);
            }
            axisValues.add(new AxisValue(i).setLabel(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())));
            c.add(Calendar.DAY_OF_WEEK, 1);
        }

        chart_hello = root.findViewById(R.id.chart_hello);

//---------------------------------------------------- GRAY LINE
        line = new Line(values);
        line.setColor(Color.GRAY);
        line.setHasLines(true);
        line.setPointRadius(4);
        line.setHasPoints(true);
        line.setHasLabels(true);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
//---------------------------------------------------- RED LINE
        List<PointValue> lineValuesRed = new ArrayList<PointValue>();
        lineValuesRed.add(new PointValue(0, 4));
        lineValuesRed.add(new PointValue(numValues, 4));

        Line lineRed  = new Line(lineValuesRed);
        lineRed.setColor(Color.RED);
        lineRed.setHasLines(true);
        lineRed.setStrokeWidth(1);
        lineRed.setHasPoints(false);
        lineRed.setHasLabels(false);
        lineRed.setFilled(true);
        lines.add(lineRed);
//-------------------------------------------------Yellow LINE
        List<PointValue> lineValuesYellow = new ArrayList<PointValue>();
        lineValuesYellow.add(new PointValue(0, 10));
        lineValuesYellow.add(new PointValue(numValues, 10));

        Line LineYellow  = new Line(lineValuesYellow);
        LineYellow.setColor(Color.YELLOW);
        LineYellow.setHasLines(true);
        LineYellow.setStrokeWidth(1);
        LineYellow.setHasPoints(false);
        LineYellow.setHasLabels(false);
        lines.add(LineYellow);
//-------------------------------------------------
        chart_data_hello = new LineChartData(lines);

        Axis axisX = new Axis(axisValues).setHasLines(true);
        axisX.setMaxLabelChars(2);
        axisX.setTextColor(Color.BLACK);

        Axis axisY = yAxis();

        chart_data_hello.setAxisXBottom(axisX);
        chart_data_hello.setAxisYLeft(axisY);

        chart_hello.setLineChartData(chart_data_hello);

        Viewport v = chart_hello.getMaximumViewport();
        v.set(1, v.top, numValues, 0);

        chart_hello.setMaximumViewport(v);
        chart_hello.setCurrentViewport(v);
        chart_hello.setZoomType(ZoomType.HORIZONTAL);
    }

    // метод для заполнение оси У данными
    public Axis yAxis() {
        Axis yAxis = new Axis();
        yAxis.setAutoGenerated(false);
        List<AxisValue> axisValues = new ArrayList<AxisValue>();

        for(int j = 1; j <= 12; j += 1) {
                axisValues.add(new AxisValue(j*2));
        }
        yAxis.setValues(axisValues);
        yAxis.setHasLines(true);
        yAxis.setMaxLabelChars(1);
        yAxis.setTextColor(Color.BLACK);
        //yAxis.setInside(true); // сделать внутрт графика числа
        return yAxis;
    }

    // метод для перевода даты в текст
    public static String getTimeDate(long timestamp){
        try{
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "date";
        }
    }

    // метод для перевода времи в текст
    public static String getTime(long timestamp){
        try{
            DateFormat dateFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "date";
        }
    }

}