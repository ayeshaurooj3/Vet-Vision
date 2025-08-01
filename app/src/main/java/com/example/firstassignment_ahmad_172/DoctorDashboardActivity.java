package com.example.firstassignment_ahmad_172;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.firstassignment_ahmad_172.DBHelper;

public class DoctorDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DBHelper dbHelper;
    DoctorAdapter adapter;
    List<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_dashboard);

        recyclerView = findViewById(R.id.recyclerViewDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        doctorList = dbHelper.getAllDoctors();

        adapter = new DoctorAdapter(this, doctorList);
        recyclerView.setAdapter(adapter);
    }
}
