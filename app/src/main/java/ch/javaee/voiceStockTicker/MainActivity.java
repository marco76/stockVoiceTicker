package ch.javaee.voiceStockTicker;


import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    Timer t;

    final String YAHOO_URL_PRE = "https://query.yahooapis.com/v1/public/yql?q=select%20LastTradePriceOnly%20from%20yahoo.finance.quote%20where%20symbol%20in%20(%22";
    final String YAHOO_URL_POST = "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMinValue(5);
        numberPicker.setMaxValue(3600);
        numberPicker.setValue(60);


        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // verifiy if the the user started the function

                // the user clicked the button, the function is already active, it should be stopped
                if (toggleButton.isActivated()) {

                    // stop the timer and the speak
                    t.cancel();

                    // the button should show 'OFF'
                    toggleButton.setActivated(false);

                } else {  // the button was on  'OFF'

                    // set the button to 'ON'
                    toggleButton.setActivated(true);

                    // create a new timer
                    t = new Timer();

                    // a Task must be started every predefined number of seconds
                    t.scheduleAtFixedRate(new TimerTask() {

                                             String lastPrice;


                                              @Override
                                              public void run() {
                                                  // read the text result from the web
                                                  String str = readFromWeb();
                                                  // transform in json and tell the result
                                                  try {

                                                     String newPrice = readPrice(str);
                                                      if (!newPrice.equals(lastPrice)){
                                                          speakJSON(newPrice);
                                                          lastPrice = newPrice;
                                                      }

                                                  } catch (JSONException e) {
                                                      Log.e("ERROR", "Impossible read JSON");
                                                  }
                                              }
                                          }, 0,
                            // frequency in milliseconds for the repetition
                            ((NumberPicker) findViewById(R.id.numberPicker)).getValue() * 1000);
                }
            }
        });
    }

    private String readFromWeb() {
        // THREAD
        final ExecutorService service;
        final Future<String> task;

        // read the symbol of the equity edited by the user
        EditText ticker = (EditText) findViewById(R.id.editText);

        String json = null;

        service = Executors.newFixedThreadPool(1);
        task = service.submit(new YahooQuote(YAHOO_URL_PRE + ticker.getText() + YAHOO_URL_POST));

        try {
            json = task.get(); // this raises ExecutionException if thread dies

        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        } catch (final ExecutionException ex) {
            ex.printStackTrace();
        } finally {
            service.shutdownNow();

        }
        return json;
    }

    private void speakJSON(String price) {
        try {
            createTextToSpeech(price);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readPrice(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        JSONObject query = jsonObject.getJSONObject("query");
        JSONObject results = query.getJSONObject("results");
        JSONObject quote = results.getJSONObject("quote");
        return quote.getString("LastTradePriceOnly");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void createTextToSpeech(final String quote) {
        tts = new TextToSpeech(this.getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.speak(quote, TextToSpeech.QUEUE_ADD, null);
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
