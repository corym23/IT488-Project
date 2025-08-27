import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * GoogleMapsApp is a Swing-based desktop application that allows users to enter
 * a US address,
 * select a map view type (Satellite, Terrain, or Street View), and interact
 * with Google Maps.
 * <p>
 * Features:
 * <ul>
 * <li>Accepts street address, city, state, and ZIP code input.</li>
 * <li>Allows selection between Satellite, Terrain, and Street View modes.</li>
 * <li>Uses Google Maps Geocoding API to convert address to latitude and
 * longitude.</li>
 * <li>Opens the corresponding location in Google Maps in the selected view
 * mode.</li>
 * <li>Performs reverse geocoding to display the formatted address for the
 * coordinates.</li>
 * </ul>
 * <p>
 * Note: Requires a valid Google Maps API key for geocoding and reverse
 * geocoding operations.
 *
 */
public class IN352_Cory_McCombs_Unit8_Assignment extends JFrame {

    private JTextField streetField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField zipField;
    private JRadioButton satelliteButton;
    private JRadioButton terrainButton;
    private JRadioButton streetViewButton;
    private JButton submitButton;
    private final String API_KEY = "AIzaSyB7HMRe8WO62nhDERJyJU8JL5oklzXXS78";

    public IN352_Cory_McCombs_Unit8_Assignment() {
        setTitle("Google Maps App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setLayout(new GridLayout(5, 2));

        inputPanel.add(new JLabel("Street Address:"));
        streetField = new JTextField();
        inputPanel.add(streetField);

        inputPanel.add(new JLabel("City:"));
        cityField = new JTextField();
        inputPanel.add(cityField);

        inputPanel.add(new JLabel("State:"));
        stateField = new JTextField();
        inputPanel.add(stateField);

        inputPanel.add(new JLabel("ZIP Code:"));
        zipField = new JTextField();
        inputPanel.add(zipField);

        satelliteButton = new JRadioButton("Satellite View");
        terrainButton = new JRadioButton("Terrain View");
        streetViewButton = new JRadioButton("Street View");

        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(satelliteButton);
        viewGroup.add(terrainButton);
        viewGroup.add(streetViewButton);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(3, 1));
        radioPanel.add(satelliteButton);
        radioPanel.add(terrainButton);
        radioPanel.add(streetViewButton);

        inputPanel.add(radioPanel);

        submitButton = new JButton("Submit");
        inputPanel.add(submitButton);

        add(inputPanel, BorderLayout.CENTER);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSubmission();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void handleSubmission() {
        String street = streetField.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String zip = zipField.getText().trim();

        if (street.isEmpty() || city.isEmpty() || state.isEmpty() || zip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all address fields.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String fullAddress = String.format("%s, %s, %s, %s", street, city, state, zip);
            String encodedAddress = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8);

            String geocodingUrl = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    encodedAddress, API_KEY);

            String jsonResponse = getApiResponse(geocodingUrl);

            if (!jsonResponse.contains("\"status\" : \"OK\"")) {
                JOptionPane.showMessageDialog(this, "Geocoding failed. Check the address and API key.", "API Error",
                        JOptionPane.ERROR_MESSAGE);
                System.err.println("API Response: " + jsonResponse);
                return;
            }

            double lat = parseValue(jsonResponse, "\"lat\"");
            double lng = parseValue(jsonResponse, "\"lng\"");

            System.out.printf("Latitude: %f, Longitude: %f%n", lat, lng);

            openMapView(lat, lng);
            performReverseGeocoding(lat, lng);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getApiResponse(String url) throws Exception {
        URI uri = new URI(url);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonResponse = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonResponse.append(line);
        }
        reader.close();
        return jsonResponse.toString();
    }

    private double parseValue(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Key not found in JSON: " + key);
        }
        int valueStart = json.indexOf(":", keyIndex) + 1;
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }
        String valueStr = json.substring(valueStart, valueEnd).trim();

        String cleanedValueStr = valueStr.replaceAll("[^0-9.-]", "");

        if (cleanedValueStr.isEmpty() || !cleanedValueStr.matches("-?\\d+(\\.\\d+)?")) {
            throw new NumberFormatException("Cleaned string is not a valid number: '" + cleanedValueStr
                    + "' from original: '" + valueStr + "'");
        }

        return Double.parseDouble(cleanedValueStr);
    }

    private String parseStringValue(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Key not found in JSON: " + key);
        }
        int valueStart = json.indexOf(":", keyIndex) + 1;
        int quoteStart = json.indexOf("\"", valueStart);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteStart == -1 || quoteEnd == -1) {
            return null;
        }
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private void openMapView(double lat, double lng) {
        String mapsUrl;

        if (satelliteButton.isSelected()) {
            mapsUrl = String.format("https://www.google.com/maps/@?api=1&map_action=map&center=%s,%s&basemap=satellite",
                    lat, lng);
        } else if (terrainButton.isSelected()) {
            mapsUrl = String.format(
                    "https://www.google.com/maps/@?api=1&map_action=map&center=%s,%s&zoom=12&basemap=terrain", lat,
                    lng);
        } else { // streetViewButton is selected, default to street view
            mapsUrl = String.format("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=%s,%s", lat, lng);
        }

        try {
            Desktop.getDesktop().browse(new URI(mapsUrl));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while opening the map.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performReverseGeocoding(double lat, double lng) {
        try {
            String reverseGeocodingUrl = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    lat, lng, API_KEY);

            String jsonResponse = getApiResponse(reverseGeocodingUrl);

            if (!jsonResponse.contains("\"status\" : \"OK\"")) {
                System.err.println("Reverse geocoding failed: " + jsonResponse);
                return;
            }

            String formattedAddress = parseStringValue(jsonResponse, "\"formatted_address\"");
            JOptionPane.showMessageDialog(this, "Reverse Geocoded Address: " + formattedAddress, "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during reverse geocoding.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IN352_Cory_McCombs_Unit8_Assignment app = new IN352_Cory_McCombs_Unit8_Assignment();
            app.setVisible(true);
        });
    }
}