package com.example.examen_pm1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegistroEntrevista extends AppCompatActivity {

    String currentPhotoPathFirebase;
    Uri imagenSeleccionada;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    String currentPhotoPath;
    static final int Peticion_ElegirGaleria = 103;

    static final int Peticion_AccesoCamara = 101;
    static final int Peticion_TomarFoto = 102;

    EditText descripcion, periodista, fecha;
    ImageView imagen;
    Button btn_guardar, btn_listar;
    FloatingActionButton btn_grabar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_entrevista);

        descripcion = (EditText) findViewById(R.id.et_descripcion_actua);
        periodista = (EditText) findViewById(R.id.et_periodista_actua);
        fecha = (EditText) findViewById(R.id.et_fecha_actua);
        imagen = (ImageView) findViewById(R.id.img_actua);
        btn_guardar = (Button) findViewById(R.id.btn_actualizar);
        btn_listar = (Button) findViewById(R.id.btn_listar);
        btn_grabar = (FloatingActionButton) findViewById(R.id.btn_grabar);

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MostrarDialogoOpciones();
            }
        });

        btn_listar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ListadoEntrevistas.class);
                startActivity(intent);
            }
        });

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String descrip = descripcion.getText().toString();
                    String nombrePeriodista = periodista.getText().toString();
                    String fechar =  fecha.getText().toString();
                    guardarRegistro(currentPhotoPath,nombrePeriodista,descrip,fechar);
                    limpiarCajas();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }

    private void limpiarCajas(){
        descripcion.setText("");
        periodista.setText("");
        fecha.setText("");
        imagen.setImageURI(null);
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
                imagen.setImageURI(Uri.fromFile(foto));

            } catch (Exception ex) {
                ex.toString();
            }
        } else if (requestCode == Peticion_ElegirGaleria && resultCode == RESULT_OK) {
            // Elegir de la galería
            Uri selectedImage = data.getData();
            imagen.setImageURI(selectedImage);

        }
    }

    private void guardarRegistro(String urlImagen,String periodista,String desc, String fecha) throws FileNotFoundException {
        // Referencia a la base de datos
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(urlImagen));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Se genera una clave única para el nuevo registro
        String registroKey = database.child("Entrevistas").push().getKey();

        // Se crea la referencia al nodo de la entrevista con la clave generada
        DatabaseReference nuevoRegistro = database.child("Entrevistas").child(registroKey);

        long timestampSeconds = System.currentTimeMillis() / 1000;
        StorageReference reference = storageRef.child("imagenEntrevistado/"+registroKey);
        UploadTask uploadTask = reference.putStream(stream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText((Context) RegistroEntrevista.this, "Error "+exception, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the URL of the uploaded image
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = String.valueOf(uri);
                        Log.d("url", downloadUrl);
                        Entrevista entrevista = new Entrevista(downloadUrl, periodista,desc, fecha);
                        nuevoRegistro.setValue(entrevista)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(RegistroEntrevista.this, "Se registró exitosamente.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegistroEntrevista.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });

    }

    public void subirImagenAFirebase_galeria() {
        // Crear una referencia al lugar donde se almacenará la imagen en Firebase Storage
        StorageReference filePath = storageRef.child("imagenEntrevistado").child(imagenSeleccionada.getLastPathSegment());

        // Subir la imagen
        filePath.putFile(imagenSeleccionada)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // La imagen se subió exitosamente

                        Toast.makeText(RegistroEntrevista.this, "Se subió exitosamente la foto.", Toast.LENGTH_SHORT).show();
                        // Obtén la URL de descarga
                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Obtén la URL de descarga de la imagen
                           currentPhotoPathFirebase = uri.toString();

                            Log.d("URL IMAGEN", currentPhotoPathFirebase);
                        }).addOnFailureListener(e -> {
                            // Manejar el caso en que la obtención de la URL falle
                            Toast.makeText(RegistroEntrevista.this, "Error al obtener la URL de la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejar el caso en que la subida falle
                        Toast.makeText(RegistroEntrevista.this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}