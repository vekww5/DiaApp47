package com.example.diaapp.ui.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaapp.AdapterFirebase;
import com.example.diaapp.ManagementDiaryDataActivity;
import com.example.diaapp.DataUser;
import com.example.diaapp.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;


public class DiaryFragment extends Fragment {

    private AdapterFirebase adapterFirebase;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private FloatingActionButton fabInjectShort, fabInjectLong, fabGlucose, fabXE;
    private FloatingActionsMenu floatingActionsMenu;
    private Menu memu_fragment;
    private SharedPreferences sp;

    private final String  day = "День";
    private final String _3day = "3 дня";
    private final String weak = "Неделя";
    private final String month = "Месяц";
    private final String all = "Все";

    private String selectPeriod = "";
    private MenuItem item_fragment;

    //создание фрагмента дневника
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_diary, container, false);
        //указываем, что будем заполнять параметры меню
        setHasOptionsMenu(true);

        //настройка режима отображения recyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        //инициализация переченных для работы с базой данных
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("User");
        ref.keepSynced(true);

        //получаем сохранненых настроек из локальных данных
        sp = getActivity().getSharedPreferences("LocalDataApp", Context.MODE_PRIVATE);
        selectPeriod =  sp.getString("Период", "");

        //создаем и настраиваем адаптер для recyclerView
        adapterFirebase = new AdapterFirebase(getSelectDateRecyclerOptions(selectPeriod));
        recyclerView.setAdapter(adapterFirebase);

        //обработка нажатий на recyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            //обработка сдвига элемента recyclerView
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                Snackbar snackbar = Snackbar.make(getView(), "Удаление", Snackbar.LENGTH_SHORT)
                        .setAction("Отменить", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapterFirebase.notifyItemChanged(position);
                    }
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            adapterFirebase.deleteItem(position);
                        }
                        if (event == DISMISS_EVENT_CONSECUTIVE){
                            adapterFirebase.deleteItem(position);
                        }
                    }
                });
                snackbar.show();
            }

        }).attachToRecyclerView(recyclerView);

        //обработка нажатий на adapterFirebase
        adapterFirebase.setOnItemClickListener(new AdapterFirebase.OnItemClickListener() {
            @Override
            public void onItemClick(DataSnapshot dataSnapshot, int position) {
                DataUser note = dataSnapshot.getValue(DataUser.class);
                assert note != null;

                Intent intent = new Intent(root.getContext(), ManagementDiaryDataActivity.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("inject_long", note.getInject_long());
                map.put("inject_short", note.getInject_short());
                map.put("glucose", note.getGlucose());
                map.put("xe", note.getXe());
                map.put("date", note.getString_date());
                map.put("time", note.getString_time());
                map.put("timestamp", note.getTimestamp());

                // action - действие 0 - добавление, 1 - изменение, dataSnapshot.getKey() - номер записи
                intent.putExtra("action",1);
                intent.putExtra("id_record", dataSnapshot.getKey());
                intent.putExtra("data", map);
                startActivity(intent);
            }
        });

        //иниц. меню floatingActionsMenu
        floatingActionsMenu = root.findViewById(R.id.floatingActionsMenu);

        //иниц. кнопки меню fabInjectLong и задание обработчика
        fabInjectLong = root.findViewById(R.id.inject_long);
        fabInjectLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), ManagementDiaryDataActivity.class);
                // action - действие 0 - добавление, 1 - изменение, select - нажатая кнопка
                intent.putExtra("action",0);
                intent.putExtra("select",0);
                floatingActionsMenu.collapse();

                startActivity(intent);
            }
        });

        //иниц. кнопки меню fabInjectShort и задание обработчика
        fabInjectShort = root.findViewById(R.id.inject_short);
        fabInjectShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), ManagementDiaryDataActivity.class);
                // action - действие 0 - добавление, 1 - изменение, select - нажатая кнопка
                intent.putExtra("action",0);
                intent.putExtra("select",1);
                floatingActionsMenu.collapse();

                startActivity(intent);
            }
        });

        //иниц. кнопки меню fabGlucose и задание обработчика
        fabGlucose = root.findViewById(R.id.drop_blood);
        fabGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), ManagementDiaryDataActivity.class);
                // action - действие 0 - добавление, 1 - изменение
                intent.putExtra("action",0);
                intent.putExtra("select",2);
                floatingActionsMenu.collapse();

                startActivity(intent);
            }
        });

        //иниц. кнопки меню fabXE и задание обработчика
        fabXE = root.findViewById(R.id.xe);
        fabXE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), ManagementDiaryDataActivity.class);
                // action - действие 0 - добавление, 1 - изменение
                intent.putExtra("action",0);
                intent.putExtra("select",3);
                floatingActionsMenu.collapse();

                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // прослушивание изменения базы данных
        adapterFirebase.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // завершение прослушивание изменения базы данных
        adapterFirebase.stopListening();
    }

    // переопределение опций меню
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // делаем элементы видемые
        menu.findItem(R.id.action_filter_log).setVisible(true);
        menu.findItem(R.id.action_delete_all_items).setVisible(true);
        memu_fragment = menu;
        // меняем надпись нажатого элемента
        item_fragment = memu_fragment.findItem(R.id.action_filter_log);
        item_fragment.setTitle(selectPeriod);
    }

    // обработка нажатий на элементы меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        SharedPreferences.Editor editor = sp.edit();

        switch (id){
            case R.id.action_filter_day:
                // отображение за день
                adapterFirebase.updateOptions(getSelectDateRecyclerOptions(day));
                // сохранеие выбранного периода
                item_fragment.setTitle(day);
                editor.putString("Период", day);
                editor.apply();
                break;
            case R.id.action_filter_3days:
                // отображение за 3 дня
                adapterFirebase.updateOptions(getSelectDateRecyclerOptions(_3day));
                item_fragment.setTitle(_3day);
                editor.putString("Период", _3day);
                editor.apply();
                break;
            case R.id.action_filter_weak:
                // отображение за 7 дня
                adapterFirebase.updateOptions(getSelectDateRecyclerOptions(weak));
                // сохранеие выбранного периода
                item_fragment.setTitle(weak);
                editor.putString("Период", weak);
                editor.apply();
                break;
            case R.id.action_filter_month:
                // отображение за месяц
                adapterFirebase.updateOptions(getSelectDateRecyclerOptions(month));
                // сохранеие выбранного периода
                item_fragment.setTitle(month);
                editor.putString("Период", month);
                editor.apply();
                break;
            case R.id.action_filter_all:
                // отображение за все время
                adapterFirebase.updateOptions(getSelectDateRecyclerOptions(all));
                // сохранеие выбранного периода
                item_fragment.setTitle(all);
                editor.putString("Период", all);
                editor.apply();
                break;
            case  R.id.action_delete_all_items:
                // удаление записей
                new AlertDialog.Builder(getActivity())
                        .setTitle("Удаление всех записей")
                        .setMessage("Вы уверены что хотите удалить все записи?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAllRec();
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create().show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // удаление всех записей
    private void deleteAllRec(){
        ref.child(auth.getUid()).child("Notes").removeValue();
    }

    private FirebaseRecyclerOptions<DataUser> getSelectDateRecyclerOptions(String selectPeriod){
        long startDate = 0, endDate = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);

        endDate = calendar.getTimeInMillis();

        switch (selectPeriod){
            case day:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MILLISECOND, +1);
                startDate = calendar.getTimeInMillis();
                break;
            case _3day:
                calendar.add(Calendar.DAY_OF_MONTH, -3);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MILLISECOND, +1);
                startDate = calendar.getTimeInMillis();
                break;
            case weak:
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MILLISECOND, +1);
                startDate = calendar.getTimeInMillis();
                break;
            case month:
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MILLISECOND, +1);
                startDate = calendar.getTimeInMillis();
                break;
            default:
                endDate = Long.MAX_VALUE;
                break;
        }

        return new FirebaseRecyclerOptions
                .Builder<DataUser>().setQuery(ref.child(auth.getUid())
                .child("Notes").orderByChild("timestamp").startAt(startDate)
                .endAt(endDate).limitToLast(105), DataUser.class).build();
    }

}
