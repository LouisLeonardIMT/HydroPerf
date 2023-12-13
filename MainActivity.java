package dataupload;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Exemple d'utilisation
        String serverUrl = "http://adresse_ip_raspberry:8080/upload";
        String dataToSend = "Données à envoyer depuis l'application Android";

        new UploadDataTask().execute(serverUrl, dataToSend);
    }

    private static class UploadDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String serverUrl = params[0];
            String dataToSend = params[1];

            try {
                uploadData(serverUrl, dataToSend);
            } catch (Exception e) {
                Log.e("UploadDataTask", "Erreur lors de l'envoi des données", e);
            }

            return null;
        }

        private void uploadData(String serverUrl, String data) throws Exception {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configurer la connexion pour une requête POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Écrire les données dans le corps de la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse du serveur (facultatif)
            int responseCode = connection.getResponseCode();
            Log.i("UploadDataTask", "Réponse du serveur : " + responseCode);

            connection.disconnect();
        }
    }
}
