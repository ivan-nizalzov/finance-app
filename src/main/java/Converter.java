import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Converter {
    private final HttpClient client;

    public Converter() {
        client = HttpClient.newHttpClient();
    }

    public void convert(double rubles, String currency) {
        double rate = getRate(currency);
        if (rate > 0) {
            System.out.println("Ваши сбережения: " + rubles * rate + " " + currency);
        }
    }

    private double getRate(String currencySymbol) {
        double rateRequest = 0.0;
        URI url = URI.create("https://api.exchangerate.host/latest?base=RUB&symbols=" + currencySymbol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // проверяем, успешно ли обработан запрос
            if (response.statusCode() == 200) {
                JsonElement jsonElement = JsonParser.parseString(response.body());
                if (!jsonElement.isJsonObject()) { // проверяем, точно ли мы получили JSON-объект
                    System.out.println("Ответ от сервера не соответствует ожидаемому.");
                }
                // преобразуем результат разбора текста в JSON-объект
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                rateRequest = jsonObject.get("rates").getAsJsonObject().get(currencySymbol).getAsDouble();
            } else {
                System.out.println("Что-то пошло не так. Сервер вернул код состояния: " + response.statusCode());
            }
        } catch (NullPointerException | IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return rateRequest;
    }
}