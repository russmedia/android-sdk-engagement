package com.russmedia.engagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;
    Context ctx = null;

    public DownloadImageTask(Context ctx) {
        this.ctx = ctx;
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


        //imageView.getLayoutParams().height = (int) ctx.getResources().getDimension(R.dimen.imageview_height);
        //imageView.getLayoutParams().width = (int) ctx.getResources().getDimension(R.dimen.imageview_width);
        //imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
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