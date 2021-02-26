package com.example.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private Button buttonGetAnImage;
    private ImageView image;
    private Button buttonGoToListView;

    private String linkOfPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGetAnImage = findViewById(R.id.buttonGetAnImage);
        image = findViewById(R.id.image);
        buttonGoToListView = findViewById(R.id.buttonToListActivity);

        buttonGetAnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncFlickrJSONData data = new AsyncFlickrJSONData();
                data.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");
            }
        });

        Intent intentListView = new Intent(this, ListActivity.class);

        buttonGoToListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentListView);
            }
        });

    }

    private class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            URL url = null;
            String s = "";
            JSONObject jsonobj = null;
            try {
                System.out.println(strings[0]);
                url = new URL(strings[0]);
                System.out.println("doInbackground");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    s = readStream(in);
                    Log.i("JFL", s);
                    try {
                        int debut = s.indexOf("(");
                        int fin = s.lastIndexOf(")");
                        String newJsonFormat = s.substring(debut + 1, fin);
                        jsonobj = new JSONObject(newJsonFormat);
                        System.out.println("50");
                    } catch (JSONException err){
                        System.out.println("04404");
                        Log.d("Error", err.toString());
                    }
                } finally {
                    System.out.println("60");
                    urlConnection.disconnect();
                }
            }
            catch (MalformedURLException e) {
                System.out.println("70");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("90");
                e.printStackTrace();
            }
            System.out.println(jsonobj);
            return jsonobj;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            System.out.println(jsonObject);
            try {
                System.out.println(jsonObject);
                linkOfPicture = jsonObject.getJSONArray("items").getJSONObject(1).getString("media");
                int debut = linkOfPicture.indexOf("https");
                int fin = linkOfPicture.lastIndexOf("g");
                linkOfPicture = linkOfPicture.substring(debut, fin + 1 );
                System.out.println("My Thread is running");

                System.out.println(linkOfPicture);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AsyncBitmapDownloader DownloadBitMap = new AsyncBitmapDownloader();
            DownloadBitMap.execute(linkOfPicture);
        }

        private String readStream(InputStream in) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int i = in.read();
                while(i != -1) {
                    bo.write(i);
                    i = in.read();
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }


    }

    private class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            Bitmap Render = null;
            try {
                url = new URL(strings[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Bitmap bm = BitmapFactory.decodeStream(in);
                //Render = Bitmap.createScaledBitmap(bm,  600 ,600, true);//thi
                Render = bm;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Render;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            System.out.println("10");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("20");
                    image.setImageBitmap(bitmap);
                }
            });
        }

    }


}

