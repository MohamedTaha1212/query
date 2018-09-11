package com.example.mota.myapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** URL to query the USGS dataset for earthquake information */
    private static final String AAPG_ARTICLES_REQUEST_URL =
            "http://www.aapgsuez.net/sarticles.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kick off an {@link AsyncTask} to perform the network request
        ArticlesAsyncTask task = new ArticlesAsyncTask();
        task.execute();
    }

    /**
     * Update the screen to display information from the given {@link Article}.
     * @param earthquake
     */
    public void updateUi(Article earthquake) {
        // Display the earthquake title in the UI
        TextView idTextView = (TextView) findViewById(R.id.idTextView);
        idTextView.setText(earthquake.id);

        // Display the earthquake date in the UI
        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(earthquake.name);

        // Display whether or not there was a tsunami alert in the UI
        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        descriptionTextView.setText(earthquake.description);

        ImageView articlesImageView = (ImageView) findViewById(R.id.imageView);
        String imageUri = earthquake.image;

        Picasso.get().load(imageUri).into(articlesImageView);


    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class ArticlesAsyncTask extends AsyncTask<URL, Void, Article> {

        @Override
        protected Article doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(AAPG_ARTICLES_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Extract relevant fields from the JSON response and create an {@link Article} object
            Article earthquake = extractFeatureFromJson(jsonResponse);

            // Return the {@link Article} object as the result fo the {@link TsunamiAsyncTask}
            return earthquake;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link ArticlesQuery.ArticlesAsyncTask}).
         */
        @Override
        protected void onPostExecute(Article earthquake) {
            if (earthquake == null) {
                return;
            }

            updateUi(earthquake);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Article} object by parsing out information
         * about the first article from the input articlesJSON string.
         */
        public Article extractFeatureFromJson(String articlesJSON) {
            // If the JSON string is empty or null, then return early.
            if (TextUtils.isEmpty(articlesJSON)) {
                return null;
            }

            try {
                JSONObject baseJsonResponse = new JSONObject(articlesJSON);
                JSONArray ArticlesArray = baseJsonResponse.getJSONArray("Articles");

                // If there are results in the Articles array
                if (ArticlesArray.length() > 0) {
                    // Extract out the first feature (which is an earthquake)
                    JSONObject firstArticle = ArticlesArray.getJSONObject(0);

                    // Extract out the id, name, and description values
                    String id = firstArticle.getString("id");
                    String name = firstArticle.getString("name");
                    String description = firstArticle.getString("description");
                    String image = firstArticle.getString("image");

                    // Create a new {@link Article} object
                    return new Article(id, name, description , image );
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the Articles JSON results", e);
            }
            return null;
        }
    }
}
