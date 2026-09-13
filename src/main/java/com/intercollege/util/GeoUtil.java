package com.intercollege.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for geographic distance calculations using the Haversine formula.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * The Earth is roughly a sphere, not a flat plane. Normal Pythagorean distance
 * (sqrt(dx^2 + dy^2)) is inaccurate for large geographic distances.
 * The Haversine Formula calculates the great-circle distance between two points
 * on a sphere given their longitudes and latitudes.
 */
public class GeoUtil {

    // Approximate radius of the Earth in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Preset coordinates for major educational cities/hubs for quick lookup and fallback
    private static final Map<String, double[]> CITY_COORDINATES;

    static {
        Map<String, double[]> map = new HashMap<>();
        // Format: [latitude, longitude]
        map.put("thiruvananthapuram", new double[]{8.5241, 76.9366});
        map.put("trivandrum", new double[]{8.5241, 76.9366});
        map.put("kochi", new double[]{9.9312, 76.2673});
        map.put("cochin", new double[]{9.9312, 76.2673});
        map.put("kozhikode", new double[]{11.2588, 75.7804});
        map.put("calicut", new double[]{11.2588, 75.7804});
        map.put("thrissur", new double[]{10.5276, 76.2144});
        map.put("kottayam", new double[]{9.5916, 76.5222});
        map.put("kollam", new double[]{8.8932, 76.6141});
        map.put("palakkad", new double[]{10.7867, 76.6548});
        map.put("kannur", new double[]{11.8745, 75.3704});
        map.put("malappuram", new double[]{11.0732, 76.0740});
        map.put("bangalore", new double[]{12.9716, 77.5946});
        map.put("bengaluru", new double[]{12.9716, 77.5946});
        map.put("chennai", new double[]{13.0827, 80.2707});
        CITY_COORDINATES = Collections.unmodifiableMap(map);
    }

    /**
     * Calculates great-circle distance between two latitude/longitude points in kilometers.
     *
     * @param lat1 Latitude of first point in decimal degrees
     * @param lon1 Longitude of first point in decimal degrees
     * @param lat2 Latitude of second point in decimal degrees
     * @param lon2 Longitude of second point in decimal degrees
     * @return Distance in kilometers rounded to 1 decimal place
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;

        // Round to 1 decimal place (e.g., 3.2 km)
        return Math.round(distance * 10.0) / 10.0;
    }

    /**
     * Looks up default latitude and longitude for known city names.
     * Returns null if not found.
     */
    public static double[] getCoordinatesForCity(String cityName) {
        if (cityName == null) return null;
        String normalized = cityName.trim().toLowerCase();
        return CITY_COORDINATES.get(normalized);
    }
}
