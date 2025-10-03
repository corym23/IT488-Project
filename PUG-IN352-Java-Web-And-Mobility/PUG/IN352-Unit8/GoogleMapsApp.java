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

public class GoogleMapsApp extends JFrame {

    private JTextField streetField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField zipField;
    private JRadioButton satelliteButton;
    private JRadioButton terrainButton;
    private JRadioButton streetViewButton;
    private JButton submitButton;
    private final String API_KEY = "AIzaSyB7HMRe8WO62nhDERJyJU8JL5oklzXXS78";

    public GoogleMapsApp() {
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
        satelliteButton.setSelected(true);

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

            double[] coordinates = overrideCoordinates(fullAddress, lat, lng);
            openMapView(coordinates[0], coordinates[1], fullAddress);
            performReverseGeocoding(coordinates[0], coordinates[1]);

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

        // This is the core fix for the NumberFormatException.
        // It removes all characters that are not a digit, a period, or a minus sign.
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

    private void openMapView(double lat, double lng, String address) {
        String mapsUrl = "";
        String viewType = "";

        if (satelliteButton.isSelected()) {
            viewType = "k"; // satellite
        } else if (terrainButton.isSelected()) {
            viewType = "p"; // terrain
        } else if (streetViewButton.isSelected()) {
            viewType = "m"; // roadmap
        }

        mapsUrl = String.format("http://maps.google.com/maps?q=%s&t=%s",
                URLEncoder.encode(address, StandardCharsets.UTF_8), viewType);

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

    private double[] overrideCoordinates(String address, double lat, double lng) {
        // Logic for overriding coordinates can be extended dynamically if needed
        // Currently, it simply returns the provided lat/lng without hardcoding
        return new double[] { lat, lng };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GoogleMapsApp app = new GoogleMapsApp();
            app.setVisible(true);
        });
    }
}