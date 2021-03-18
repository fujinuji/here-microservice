package ro.fujinuji.bookaseat.here.microservice.service;

import org.springframework.stereotype.Service;
import ro.fujinuji.bookaseat.here.microservice.http.HttpRequests;
import ro.fujinuji.bookaseat.here.microservice.model.ContactDetails;
import ro.fujinuji.bookaseat.here.microservice.model.Coordinates;
import ro.fujinuji.bookaseat.here.microservice.model.LocationSearch;

@Service
public class LocationService {

    private final HttpRequests httpRequests;

    public LocationService(HttpRequests httpRequests) {
        this.httpRequests = httpRequests;
    }

    public Coordinates getCoordinatesFromAddress(String address) throws Exception {
        return httpRequests.getLocationFromAddress(address);
    }

    public ContactDetails searchNearLocation(LocationSearch locationSearch) throws Exception {
        return httpRequests.getNearestLocation(locationSearch);
    }
}
