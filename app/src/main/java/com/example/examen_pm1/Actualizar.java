package com.example.examen_pm1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Actualizar extends AppCompatActivity {
    EditText periodista, descripcion, fecha;
    ImageView imgActualizar;
    FirebaseStorage storage = FirebaseStorage.getInstance();StorageReference storageRef = storage.getReference();
    private DatabaseReference mDatabase;String currentPhotoPath;
    static final int Peticion_ElegirGaleria = 103;

    static final int Peticion_AccesoCamara = 101;
    static final int Peticion_TomarFoto = 102;
    Button btnActualizar, btnELiminar;;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar);

        periodista = (EditText) findViewById(R.id.et_periodista_actua);
        descripcion = (EditText) findViewById(R.id.et_descripcion_actua);
        fecha = (EditText) findViewById(R.id.et_fecha_actua);
        btnActualizar = (Button) findViewById(R.id.btn_actualizar);
        imgActualizar = (ImageView) findViewById(R.id.img_actua);
        btnELiminar = (Button) findViewById(R.id.btnEliminar);

        imgActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarDialogoOpciones();
            }
        });

        // Recibir la entrevista desde el adaptador
        Intent intent = getIntent();
        if (intent != null) {
            Entrevista entrevista = (Entrevista) intent.getSerializableExtra("registro");
            // Usa Glide para cargar la imagen desde la URL en el ImageView
            Glide.with(this)
                    .load(entrevista.getImagen()) // Asegúrate de que este método devuelve la URL de la imagen
                    .into(imgActualizar);
            periodista.setText(entrevista.getPeriodista());
            descripcion.setText(entrevista.getDescripcion());
            fecha.setText(entrevista.getFecha());

            // Puedes usar la entrevista aquí para lo que necesites
            btnActualizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actualizar(entrevista.getId(), currentPhotoPath);
                }
            });
            btnELiminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MostrarAlertaEliminar(entrevista.getId());
                }
            });
        }
    }
    private void actualizar(String entrevistaId, String urlImagen){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Entrevistas").child(entrevistaId);
        Map<String, Object> entrevistaActualizada = new HashMap<>();

        // Obtén la referencia al archivo en Firebase Storage que quieres actualizar
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenEntrevistado").child(entrevistaId);
        if (urlImagen != null && !urlImagen.isEmpty()) {
            // Crea un File desde la ruta de la imagen
            File file = new File(urlImagen);

            // Sube la nueva imagen a esa referencia en Storage
            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(file));

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Maneja la falla en la carga de la imagen
                    Toast.makeText(Actualizar.this, "Error al subir la imagen: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Obtiene la URL del archivo recién subido a Storage
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Obtiene la URL de la imagen actualizada
                            String nuevaUrlImagen = uri.toString();

                            // Actualiza la URL de la imagen en Realtime Database
                            entrevistaActualizada.put("imagen", nuevaUrlImagen);

                            // Actualiza los campos en la base de datos
                            actualizarCamposBD(mDatabase, entrevistaActualizada);
                        }
                    });
                }
            });
        } else {
            // Si no hay una nueva imagen, actualiza solo los campos en la base de datos
            actualizarCamposBD(mDatabase, entrevistaActualizada);
        }
    }

    private void actualizarCamposBD(DatabaseReference mDatabase, Map<String, Object> campos) {
        campos.put("descripcion", descripcion.getText().toString());
        campos.put("fecha", fecha.getText().toString());
        campos.put("periodista", periodista.getText().toString());

        mDatabase.updateChildren(campos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Actualizar.this, "Actualización exitosa", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),ListadoEntrevistas.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Actualizar.this, "Error al actualizar en la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarEntrevista(String entrevistaId){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Entrevistas").child(entrevistaId);

        // Eliminar la entrada de la entrevista en Realtime Database
        mDatabase.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Si la eliminación en la base de datos es exitosa, eliminar la imagen en Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenEntrevistado").child(entrevistaId);
                storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La imagen en Storage se eliminó correctamente
                        Toast.makeText(getApplicationContext(), "Entrevista eliminada correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),ListadoEntrevistas.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ocurrió un error al eliminar la imagen en Storage
                        Toast.makeText(getApplicationContext(), "Error al eliminar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Ocurrió un error al eliminar la entrada en Realtime Database
                Toast.makeText(getApplicationContext(), "Error al eliminar la entrevista: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Permisos(){
        // Metodo para obtener los permisos requeridos de la aplicacion
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},Peticion_AccesoCamara);
        }
        else
        {
            dispatchTakePictureIntent();
            //TomarFoto();
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, Peticion_AccesoCamara);
        }
    }

    private void MostrarDialogoOpciones() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una opción");
        String[] opciones = {"Tomar foto", "Elegir de la galería"};
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Tomar foto
                    Permisos();
                } else {
                    // Abrir galería
                    AbrirGaleria();
                }
            }
        });
        builder.show();
    }
    private void MostrarAlertaEliminar(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Estás seguro de eliminar?");
        String[] opciones = {"Eliminar", "Cancelar"};
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Eliminar
                    eliminarEntrevista(id);
                } else {
                    // Cerrar
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.examen_pm1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Peticion_TomarFoto);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use  with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }
    private void AbrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Peticion_ElegirGaleria);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==  Peticion_AccesoCamara){
            if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }{
                Toast.makeText(getApplicationContext(),"Se necesita permiso de la camara",Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Peticion_TomarFoto) {
            try {
                File foto = new File(currentPhotoPath);
                imgActualizar.setImageURI(Uri.fromFile(foto));

            } catch (Exception ex) {
                ex.toString();
            }
        } else if (requestCode == Peticion_ElegirGaleria && resultCode == RESULT_OK) {
            // Elegir de la galería
            Uri selectedImage = data.getData();
            imgActualizar.setImageURI(selectedImage);

        }
    }
}