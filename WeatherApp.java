package com.weather;

import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WeatherApp extends JFrame {
    private JTextField cityTextField;
    private JButton searchButton;
    private JTextArea resultArea;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;

    private WeatherService weatherService;
    private DatabaseService databaseService;

    public WeatherApp() {
        initializeServices();
        initializeUI();
    }

    private void initializeServices() {
    Dotenv dotenv = Dotenv.configure()
            .directory(".")
            .ignoreIfMissing()
            .load();

    String apiKey = dotenv.get("OPENWEATHER_API_KEY", "");
    String databaseUrl = dotenv.get("VITE_DATABASE_URL", "");
    String databaseKey = dotenv.get("VITE_DATABASE_ANON_KEY", "");

    weatherService = new WeatherService(apiKey);
    databaseService = new DatabaseService(databaseUrl, databaseKey);
}

  

    private void initializeUI() {
        setTitle("Weather Forecast Application");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel rightPanel = createRightPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        loadSearchHistory();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter City:");
        cityTextField = new JTextField(20);
        searchButton = new JButton("Search");

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchWeather();
            }
        });

        cityTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchWeather();
            }
        });

        panel.add(label);
        panel.add(cityTextField);
        panel.add(searchButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel resultLabel = new JLabel("Weather Information:");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(resultArea);

        panel.add(resultLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        panel.setPreferredSize(new Dimension(180, 0));

        JLabel historyLabel = new JLabel("Search History:");
        historyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCity = historyList.getSelectedValue();
                if (selectedCity != null) {
                    cityTextField.setText(selectedCity);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyList);

        panel.add(historyLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchWeather() {
        String city = cityTextField.getText().trim();

        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a city name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        searchButton.setEnabled(false);
        resultArea.setText("Loading weather data...");

        SwingWorker<WeatherData, Void> worker = new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                return weatherService.getWeather(city);
            }

            @Override
            protected void done() {
                try {
                    WeatherData data = get();
                    displayWeatherData(data);
                    databaseService.saveWeatherData(data);
                    loadSearchHistory();
                } catch (Exception e) {
                    resultArea.setText("Error: " + e.getMessage());
                    JOptionPane.showMessageDialog(WeatherApp.this,
                            "Failed to fetch weather data: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    searchButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void displayWeatherData(WeatherData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================\n");
        sb.append("  WEATHER FORECAST\n");
        sb.append("==============================================\n\n");
        sb.append(String.format("Location: %s, %s\n\n", data.getCityName(), data.getCountry()));
        sb.append(String.format("Temperature: %.1f°C\n", data.getTemperature()));
        sb.append(String.format("Feels Like: %.1f°C\n", data.getFeelsLike()));
        sb.append(String.format("Min Temp: %.1f°C\n", data.getTempMin()));
        sb.append(String.format("Max Temp: %.1f°C\n\n", data.getTempMax()));
        sb.append(String.format("Condition: %s\n\n", capitalize(data.getDescription())));
        sb.append(String.format("Humidity: %d%%\n", data.getHumidity()));
        sb.append(String.format("Pressure: %d hPa\n", data.getPressure()));
        sb.append(String.format("Wind Speed: %.1f m/s\n", data.getWindSpeed()));
        sb.append("\n==============================================");

        resultArea.setText(sb.toString());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void loadSearchHistory() {
        SwingWorker<java.util.List<String>, Void> worker = new SwingWorker<java.util.List<String>, Void>() {
            @Override
            protected java.util.List<String> doInBackground() throws Exception {
                return databaseService.getRecentSearches(10);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<String> searches = get();
                    historyModel.clear();
                    for (String search : searches) {
                        historyModel.addElement(search);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load search history: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                WeatherApp app = new WeatherApp();
                app.setLocationRelativeTo(null);
                app.setVisible(true);
            }
        });
    }
}
