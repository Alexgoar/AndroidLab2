package com.example.flickrapp;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;



public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private MyAdapter myAdapter = new MyAdapter();
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        myAdapter = new MyAdapter();


        listView = findViewById(R.id.list);
        listView.setAdapter(myAdapter);
        AsyncFlickrJSONDataForList dataForList = new AsyncFlickrJSONDataForList(myAdapter);
        dataForList.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=trees&format=json");
    }


    public class MyAdapter extends BaseAdapter {

        Vector<String> vector = new Vector<String>();

        public void dd(String url)
        {
            vector.add(url);
            Log.i("JFL", "Adding to adapter url : " + url);
        }

        @Override
        public int getCount() {
            return vector.size();
        }

        @Override
        public Object getItem(int position) {
            return vector.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            RequestQueue queue = MySingleton.getInstance(listView.getContext()).
                    getRequestQueue();

            Log.i("JFL", "TODO");

            if( convertView == null ){
                //We must create a View:
                convertView = LayoutInflater.from(listView.getContext())
                        .inflate(R.layout.bitmaplayout, listView, false);
            }

            String imageURL = (String)getItem(position);

            ImageView Image = convertView.findViewById(R.id.ImageViewBitmapLayout);

            Response.Listener<Bitmap> rep_listener = bmp -> {
                Image.setImageBitmap(bmp);

            };

            ImageRequest request = new ImageRequest(
                    imageURL, rep_listener, 300,
                    300, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, null);

            queue.add(request);
            return convertView;
        }
    }


    private class AsyncFlickrJSONDataForList extends AsyncTask<String, MyAdapter, JSONObject>
    {
        MyAdapter adapt;
        private AsyncFlickrJSONDataForList(MyAdapter myAdapt)
        {
            adapt = myAdapt;
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            URL url = null;
            String s = "";
            JSONObject jsonobj = null;
            try {
                System.out.println(strings[0]);
                url = new URL(strings[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                //String basicAuth = "Basic " + Base64.encodeToString((editName.getText() + ":" + editPass.getText()).getBytes(), Base64.NO_WRAP);
                //urlConnection.setRequestProperty("Authorization", basicAuth);
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
            String linkOfPicture = null;

            try {
                System.out.println(jsonObject.getJSONArray("items").length());
                System.out.println(jsonObject.getJSONArray("items").getJSONObject(0));
                System.out.println(jsonObject.getJSONArray("items").getJSONObject(0).getString("media"));
                for (int i = 0; i < jsonObject.getJSONArray("items").length(); i++)
                {
                    linkOfPicture = jsonObject.getJSONArray("items").getJSONObject(i).getString("media");
                    int debut = linkOfPicture.indexOf("https");
                    int fin = linkOfPicture.lastIndexOf("g");
                    linkOfPicture = linkOfPicture.substring(debut, fin + 1 );
                    adapt.dd(linkOfPicture);

                }
                adapt.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String readStream(InputStream is) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int i = is.read();
                while(i != -1) {
                    bo.write(i);
                    i = is.read();
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }
    }

}