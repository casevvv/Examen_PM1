package com.example.examen_pm1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListadoEntrevistas extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference datos;

    private AdaptadorEntrevista adaptadorEntrevista;
    private RecyclerView recyclerView;

    private Entrevista idEntrevista;
    private ArrayList<Entrevista> entrevistaArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_entrevistas);
        recyclerView = (RecyclerView) findViewById(R.id.listado);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear referencia a la base de datos
        datos = database.getReference("Entrevistas");

        // Leer desde la base de datos
        getRegistros();
    }

    private void getRegistros(){
        datos.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String id = childSnapshot.getKey();
                        String urlImagen = childSnapshot.child("imagen").getValue().toString();
                        String descripcion = childSnapshot.child("descripcion").getValue().toString();
                        String fecha = childSnapshot.child("fecha").getValue().toString();
                        String periodista = childSnapshot.child("periodista").getValue().toString();

                        entrevistaArrayList.add(new Entrevista(id,urlImagen,periodista, descripcion, fecha));

                    }

                    adaptadorEntrevista = new AdaptadorEntrevista(entrevistaArrayList,R.layout.activity_listado_entrevistas);
                    recyclerView.setAdapter(adaptadorEntrevista);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar error en la lectura de la base de datos
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
