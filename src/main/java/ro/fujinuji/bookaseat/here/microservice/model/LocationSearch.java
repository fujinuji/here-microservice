package ro.fujinuji.bookaseat.here.microservice.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationSearch {
    private String locationName;
    private Coordinates userCoordinates;
    private Long radius;
}
