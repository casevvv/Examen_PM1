package com.example.examen_pm1;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
public class AdaptadorEntrevista extends RecyclerView.Adapter<AdaptadorEntrevista.AdaptadorViewHolder> {
    private ArrayList<Entrevista> entrevistaArrayList;
    private int resource;
    public AdaptadorEntrevista(ArrayList<Entrevista> entrevistaArrayList, int resource){
        this.entrevistaArrayList = entrevistaArrayList;
        this.resource = resource;
    }

    public static class AdaptadorViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView descripcion;
        public TextView periodista;
        public TextView fecha;

        public int id;

        public AdaptadorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializa tus vistas aquí
            img = itemView.findViewById(R.id.imgRecycler);
            periodista = itemView.findViewById(R.id.periodistaRecycler);
            descripcion = itemView.findViewById(R.id.descripcionRecycler);
            fecha = itemView.findViewById(R.id.fechaRecycler);

        }
    }

    @NonNull
    @Override
    public AdaptadorEntrevista.AdaptadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista, parent, false);
        return new AdaptadorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEntrevista.AdaptadorViewHolder holder, int position) {
        Entrevista entrevista = entrevistaArrayList.get(position);

        if (entrevista.getImagen() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(entrevista.getImagen())
                    .into(holder.img);
        }
        holder.periodista.setText(entrevista.getPeriodista());
        holder.descripcion.setText(entrevista.getDescripcion());
        holder.fecha.setText(entrevista.getFecha());

        // Agregar un Listener al item del RecyclerView
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener la posición del elemento que se ha clicado
                int position = holder.getAdapterPosition();

                // Obtener la entrevista seleccionada
                Entrevista entrevistaSeleccionada = entrevistaArrayList.get(position);

                // Crear un Intent para la nueva actividad y pasar toda la entrevista
                Intent intent = new Intent(holder.itemView.getContext(), Actualizar.class);

                // Pasar la entrevista completa a través del Intent
                intent.putExtra ("registro",entrevistaSeleccionada);

                holder.itemView.getContext().startActivity(intent);
            }
        });



    }
    @Override
    public int getItemCount() {
        return entrevistaArrayList.size();
    }
}


