package com.example.diaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;

public class AdapterFirebase extends FirebaseRecyclerAdapter<DataUser, AdapterFirebase.ViewHolder>
{
    private OnItemClickListener listener;
    public AdapterFirebase(@NonNull FirebaseRecyclerOptions<DataUser> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull DataUser model) {
        holder.inject_short.setText(model.getInject_short());
        holder.inject_long.setText(model.getInject_long());
        holder.xe.setText(model.getXe());
        holder.glucose.setText(model.getGlucose());
        holder.time.setText(model.getString_time());
        holder.date.setText(model.getString_date());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diary_single_row, parent, false);
        return new ViewHolder(view);
    }

    /*@NonNull
    @Override
    public DataUser getItem(int position) {
        return super.getItem(getItemCount() - 1 - position);
    }*/

    public void deleteItem(int position){
        //int pos = getItemCount() - 1 - position;
        getSnapshots().getSnapshot(position).getRef().removeValue();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView inject_short, inject_long, xe, glucose, date, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            inject_short = itemView.findViewById(R.id.inject_short_text);
            inject_long = itemView.findViewById(R.id.inject_long_text);
            glucose = itemView.findViewById(R.id.glucose_text);
            xe = itemView.findViewById(R.id.xe_text);
            date = itemView.findViewById(R.id.text_view_date);
            time = itemView.findViewById(R.id.text_view_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int position = getItemCount() - 1 - getAdapterPosition();
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DataSnapshot dataSnapshot, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


}
