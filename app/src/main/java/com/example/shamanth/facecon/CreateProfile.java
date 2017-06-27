package com.example.shamanth.facecon;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ViewPropertyAnimatorCompatSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateProfile extends AppCompatActivity { //creates a profile when "create profile" button is pressed
    private String pictureImagePath = "";
    private static final int PICK_FILE_REQUEST = 1;
    private static final int WRITE_PERMISSION = 0x01;
    public String SERVER_URL = "https://backendmcc.herokuapp.com/uploadImage";
    private String selectedFilePath;
    private static final String TAG = MainActivity.class.getSimpleName();
    ImageView ivAttachment;
    Button bUpload;
    TextView tvFileName;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        ivAttachment = (ImageView) findViewById(R.id.ivAttachment);
        bUpload = (Button) findViewById(R.id.b_upload);
        tvFileName = (TextView) findViewById(R.id.tv_file_name);

    }
    public void Select_Image(View view){
        showFileChooser();
    }
    public void upload(View view){
        if(selectedFilePath != null){
            dialog = ProgressDialog.show(this,"","Uploading File...",true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //creating new thread to handle Http Operations
                    uploadFile(selectedFilePath);
                }
            }).start();
        }else{
            Toast.makeText(this,"Please choose a File First",Toast.LENGTH_SHORT).show();
        }

    }
    public void Click_Image(View view){
        int i;File photo = null;
        File pictureFolder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File folder = new File(pictureFolder, "/images");
        boolean success = true;
        if (!folder.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = folder.mkdir();
        }
        if (success) {
            Toast.makeText(this, "Directory Created", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed - Error", Toast.LENGTH_SHORT).show();
        }
        for(i = 0;i < 4;i++) {
            Intent getCameraImage = new Intent("android.media.action.IMAGE_CAPTURE");

            File cameraFolder = null;

            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                cameraFolder = new File(android.os.Environment.getExternalStorageDirectory(), "images/");

            if (!cameraFolder.exists())
                cameraFolder.mkdirs();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            String timeStamp = dateFormat.format(new Date());
            String imageFileName = "picture_" + timeStamp +i+ ".jpg";

            photo = new File(Environment.getExternalStorageDirectory(), "images/" + imageFileName);
            getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            pictureImagePath = photo.getAbsolutePath();
            startActivityForResult(getCameraImage, 1);
        }

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                createProfile();
                if (data == null) {
                    //no data present
                    return;
                }
                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    tvFileName.setText(selectedFilePath);
                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Image not saved to:\n", Toast.LENGTH_LONG).show();
            } else {
                //createProfile();
                Toast.makeText(this, "Image not saved to:\n", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void createProfile(){
        Thread t = new Thread() {

            public void run() {
                Looper.prepare(); //For Preparing Message Pool for the child Thread
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                HttpResponse response;
                JSONObject json = new JSONObject();
                String URL = "https://backendmcc.herokuapp.com/createProfile";

                String responseStr = null;
                try {
                    HttpPost post = new HttpPost(URL);
                    json.put("Userid", "asdd");
                    json.put("FirstName", "sdsds");
                    json.put("LastName", "sdsdsd");
                    json.put("Contact", "4545445");
                    json.put("Emailid", "sssss");
                    json.put("Interest", "cvcvcvcv");
                    json.put("path1", "vfvfvvf/vf/vf");
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);
                    responseStr = EntityUtils.toString(response.getEntity());

                    /*Checking response */

                    Log.d("yexy1", "no response" + responseStr);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("text", "respinse=");
                }

                Looper.loop(); //Loop in the message queue
            }
        };


        t.start();

    }
    public int uploadFile(final String selectedFilePath){

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            return serverResponseCode;
        }

    }
}
