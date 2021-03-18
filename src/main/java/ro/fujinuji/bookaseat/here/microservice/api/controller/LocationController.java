package ro.fujinuji.bookaseat.here.microservice.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.fujinuji.bookaseat.here.microservice.api.model.RequestNearestLocation;
import ro.fujinuji.bookaseat.here.microservice.model.Coordinates;
import ro.fujinuji.bookaseat.here.microservice.model.LocationSearch;
import ro.fujinuji.bookaseat.here.microservice.service.LocationService;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/search-location")
    public ResponseEntity<?> getNearestLocation(@RequestBody RequestNearestLocation requestNearestLocation) throws Exception {
        Coordinates userLocation = locationService.getCoordinatesFromAddress(requestNearestLocation.getAddress());
        LocationSearch locationSearch = LocationSearch.builder().locationName(requestNearestLocation.getLocationName())
                .radius(requestNearestLocation.getRadius())
                .userCoordinates(userLocation)
                .build();

        return new ResponseEntity<>( locationService.searchNearLocation(locationSearch), HttpStatus.OK);
    }
}
