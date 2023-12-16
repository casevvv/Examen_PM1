package com.example.examen_pm1;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Entrevista implements Serializable {
        public String descripcion,fecha,periodista,imagen,id;

        public Entrevista() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public Entrevista(String imagen, String periodista,String descripcion, String fecha) {
            this.imagen = imagen;
            this.periodista = periodista;
            this.descripcion = descripcion;
            this.fecha = fecha;
        }

    public Entrevista(String id, String imagen, String periodista, String descripcion, String fecha) {
        this.id = id;
        this.imagen = imagen;
        this.periodista = periodista;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

}
