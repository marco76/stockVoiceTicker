package ch.javaee.voiceStockTicker;

import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by marco on 24/01/16.
 */
public class YahooQuote implements Callable<String>{

    private final String jsonURL;

    public YahooQuote(String jsonURL) {
        this.jsonURL = jsonURL;
    }

    @Override
    public String call() {

        StringBuilder total = new StringBuilder();

        try {
            URL url = new URL(jsonURL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line + "\n");
                }

            } catch (Exception e) {

                Log.d("Error:",e.getCause().toString());
            } finally {
                urlConnection.disconnect();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

         return total.toString();
    }



    }
