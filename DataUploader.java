import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DataUploader {
    public static void uploadData(String serverUrl, String data) throws Exception {
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
        System.out.println("Réponse du serveur : " + responseCode);

        connection.disconnect();
    }
}
