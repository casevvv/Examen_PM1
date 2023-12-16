package com.example.examen_pm1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btn_nuevo, btn_listado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_nuevo = (Button) findViewById(R.id.btn_actualizar);
        btn_listado = (Button) findViewById(R.id.btn_listar);

        btn_nuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegistroEntrevista.class);
                startActivity(intent);
            }
        });

        btn_listado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListadoEntrevistas.class);
                startActivity(intent);
            }
        });
    }

}