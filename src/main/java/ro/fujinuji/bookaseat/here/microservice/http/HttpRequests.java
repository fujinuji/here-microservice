package ro.fujinuji.bookaseat.here.microservice.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ro.fujinuji.bookaseat.here.microservice.model.ContactDetails;
import ro.fujinuji.bookaseat.here.microservice.model.Coordinates;
import ro.fujinuji.bookaseat.here.microservice.model.LocationSearch;
import ro.fujinuji.bookaseat.here.microservice.model.TokenHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class HttpRequests {

    private static final String HERE_API_BASE = "https://geocode.search.hereapi.com/v1/";

    private final TokenHolder tokenHolder;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public HttpRequests(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    public Coordinates getLocationFromAddress(String address) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(HERE_API_BASE + "geocode");
        uriBuilder.setParameter("q", address);

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder.getToken());

        Coordinates coordinates = new Coordinates();

        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Not 200");
            }

            JSONObject responseJson = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONArray locations = responseJson.getJSONArray("items");

            if (locations.length() == 0) {
                throw new Exception("location cannot be found");
            }

            JSONObject position = locations.getJSONObject(0).getJSONObject("position");

            coordinates.setLatitude(position.getDouble("lat"));
            coordinates.setLongitude(position.getDouble("lng"));

        }

        return coordinates;
    }

    public ContactDetails getNearestLocation(LocationSearch locationSearch) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(HERE_API_BASE + "discover");
        uriBuilder.setParameter("q", locationSearch.getLocationName());

        List<String> parameters = List.of("circle" + ":" +
                locationSearch.getUserCoordinates().getLatitude().toString() + "," + locationSearch.getUserCoordinates().getLongitude().toString(),
                "r=" + locationSearch.getRadius());

        uriBuilder.addParameter("in", String.join(";", parameters));

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder.getToken());

        ContactDetails contactDetails = new ContactDetails();

        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Not 200");
            }

            JSONObject responseJson = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONArray locations = responseJson.getJSONArray("items");

            if (locations.length() == 0) {
                throw new Exception("location cannot be found");
            }

            Optional<JSONArray> firstLocationWithPhone = getFirstWithField(locations, "contacts");

            if (firstLocationWithPhone.isPresent()) {
                Optional<JSONArray> locationWithMobile = getFirstWithField(firstLocationWithPhone.get(), "mobile");

                if (locationWithMobile.isPresent()) {
                    contactDetails.setPhoneNumber(locationWithMobile.get().getJSONObject(0).getString("value"));
                } else {
                    Optional<JSONArray> locationWithPhone = getFirstWithField(firstLocationWithPhone.get(), "phone");
                    if (locationWithPhone.isPresent()) {
                        contactDetails.setPhoneNumber(locationWithPhone.get().getJSONObject(0).getString("value"));
                    } else {
                        contactDetails.setAddress(locations.getJSONObject(0).getJSONObject("address").getString("street"));
                    }
                }
            } else {
                contactDetails.setAddress(locations.getJSONObject(0).getJSONObject("address").getString("street"));
            }
        }

        return contactDetails;
    }

    private Optional<JSONArray> getFirstWithField(JSONArray array, String field) {
        List<JSONObject> jsonObjectList =  IntStream.range(0, array.length()).mapToObj(index -> {
            try {
                return array.getJSONObject(index);
            } catch (JSONException ignored) {
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        return jsonObjectList.stream().map(location -> {
            try {
                return location.getJSONArray(field);
            } catch (JSONException ignored) {
                return null;
            }
        }).filter(Objects::nonNull)
                .findFirst();
    }
}
