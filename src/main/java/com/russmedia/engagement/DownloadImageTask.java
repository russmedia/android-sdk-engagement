package com.russmedia.engagement;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.russmedia.engagement.helper.BMPCache;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;

    public DownloadImageTask() {
    }

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {

        this.imageView = imageViews[0];
        Bitmap bitmap = download_Image((String)imageView.getTag());
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }

    private Bitmap download_Image(String url) {

        Bitmap bmp = BMPCache.getInstance().getBitmapFromMemCache(url);

        if (bmp == null) {

            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);

                if (bmp != null) {
                    BMPCache.getInstance().addBitmapToMemoryCache(url, bmp);
                    return bmp;
                }


            }catch(Exception e){}
        }



        return bmp;
    }
}