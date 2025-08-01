package com.example.firstassignment_ahmad_172;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    private Context context;
    private List<Doctor> doctorList;

    public DoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doc = doctorList.get(position);
        holder.tvName.setText(doc.getName());
        holder.tvContact.setText("Contact: " + doc.getContact());
        holder.tvEmail.setText("Email: " + doc.getEmail());
        holder.tvAddress.setText("Address: " + doc.getClinicAddress());

        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookAppointmentActivity.class);
            intent.putExtra("doctor_id", doc.getId());
            intent.putExtra("doctor_name", doc.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvEmail, tvAddress;
        Button btnBook;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvContact = itemView.findViewById(R.id.tvDoctorContact);
            tvEmail = itemView.findViewById(R.id.tvDoctorEmail);
            tvAddress = itemView.findViewById(R.id.tvClinicAddress);
            btnBook = itemView.findViewById(R.id.btnBookAppointment);
        }
    }
}
