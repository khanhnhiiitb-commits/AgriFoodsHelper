package com.group5.agrifoodshelper.views.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.group5.agrifoodshelper.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            // Tạm thời bạn có thể in ra Log hoặc Toast để biết đã lấy được tên
            System.out.println("Xin chào: " + userName);
        }
    }
}