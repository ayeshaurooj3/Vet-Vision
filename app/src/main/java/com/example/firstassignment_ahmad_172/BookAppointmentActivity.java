package com.example.firstassignment_ahmad_172;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookAppointmentActivity extends AppCompatActivity {

    TextView tvDoctorName;
    EditText etUserName, etDate;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        tvDoctorName = findViewById(R.id.tvDoctorNameBooking);
        etUserName = findViewById(R.id.etYourName);
        etDate = findViewById(R.id.etAppointmentDate);
        btnSubmit = findViewById(R.id.btnConfirmAppointment);

        String doctorName = getIntent().getStringExtra("doctor_name");
        tvDoctorName.setText("Book Appointment with " + doctorName);

        btnSubmit.setOnClickListener(v -> {
            Toast.makeText(this, "Appointment Booked!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
