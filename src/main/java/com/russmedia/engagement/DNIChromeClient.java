package com.russmedia.engagement;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.russmedia.engagement.activities.ActivityWebview;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DNIChromeClient extends WebChromeClient {

    private ActivityWebview act;


    WebView webView;
    ValueCallback<Uri[]> filePathCallback;
    WebChromeClient.FileChooserParams fileChooserParams;

    Boolean permissionsOK = true;
    public DNIChromeClient(ActivityWebview act) {
        this.act = act;
    }


    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        act.setPermissionRequest(request);
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (request.getResources().length > 0 && request.getResources()[0].contains("VIDEO_CAPTURE")) {
                        act.requestPermissions(new String[]{Manifest.permission.CAMERA}, 111222);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }
            }
        });
    }



    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        permissionsOK = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            this.webView = webView;
            this.filePathCallback = filePathCallback;
            this.fileChooserParams = fileChooserParams;

            if (act.checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || act.checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || act.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsOK = false;
                act.requestPermissions( new String[] {Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1111);

            }

            if (!permissionsOK) {
                return false;
            }



            if (act.uploadMessage != null) {
                act.uploadMessage.onReceiveValue(null);
                act.uploadMessage = null;
            }

            if (act.mFilePathCallback != null) {
                act.mFilePathCallback.onReceiveValue(null);
            }
            act.mFilePathCallback = filePathCallback;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(act.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", act.mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e("nk55", "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    act.mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            act.startActivityForResult(chooserIntent, act.INPUT_FILE_REQUEST_CODE);

            return true;

        } else {
            Toast.makeText(act, "Es tut uns leid, der Fileupload wird erst ab Android 5.0 Lollipop unterstützt", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            act.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (act.checkSelfPermission( Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && act.checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    act.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                onShowFileChooser(webView, filePathCallback, fileChooserParams);

            }

            }

        }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }
    }
