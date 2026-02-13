import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StrategyCalculator extends JFrame {
    private JTextField guildNameField;
    private JTextField totalGuildPointsField;
    private JSpinner activeGuildMembersSpinner;
    private JTextField assumedGpEfficiencyField;
    private JTextField[] missionEfficiencyFields;
    private JTextField[] depthFields;
    private JCheckBox includeMissionsCheckBox;
    private JCheckBox zeffoReadyCheckBox;
    private JCheckBox mandaloreReadyCheckBox;
    private JPanel missionPanel;
    private JButton loadGuildButton;
    private JPanel loadButtonPanel;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel dataEntryPanel;
    private JPanel resultsPanel;

    private JTextPane resultTextArea;
    private JLabel resultIndexLabel;
    private JLabel statusLabel;
    private JButton startRunButton;
    private JButton prevResultButton;
    private JButton nextResultButton;

    private String guildName = "";
    private double totalGuildPoints;
    private int activeGuildMembers;
    private double assumedGpEfficiency;
    private double[] assumedMissionEfficiency;
    private int[][] operationsPossible;
    private boolean mandaloreReady = false;
    private boolean zeffoReady = false;

    private GuildData activeGuild;
    private SimulationController simmer;
    private ArrayList<TbRun> results;
    private int currentResultIndex = 0;

    private static final String SAVE_FILE_NAME = "guild_save.txt";
    private static final Path SAVE_FILE_PATH = Paths.get(SAVE_FILE_NAME);

    private static final String[] MISSION_LABELS = {
            "R5 mission", "R6 mission", "R7 mission", "R8 mission", "R9 (1) mission", "R9 (2) mission"
    };

    private static final String DATA_ENTRY_VIEW = "dataEntry";
    private static final String RESULTS_VIEW = "results";

    private static final String NUMBER_FORMAT_ERROR_MESSAGE = "Invalid number format.\n\n"
            + "Please use a dot (.) as the decimal separator instead of a comma.\n"
            + "For example: 300.5 instead of 300,5";

    public StrategyCalculator() {
        setTitle("Guild Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(550, 900));
        setMinimumSize(new Dimension(550, 900));
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        dataEntryPanel = buildDataEntryPanel();
        resultsPanel = buildResultsPanel();

        cardPanel.add(dataEntryPanel, DATA_ENTRY_VIEW);
        cardPanel.add(resultsPanel, RESULTS_VIEW);

        add(cardPanel);
        cardLayout.show(cardPanel, DATA_ENTRY_VIEW);

        pack();
    }

    private JPanel buildDataEntryPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel guildInfoPanel = new JPanel(new GridBagLayout());
        guildInfoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Guild Information",
                TitledBorder.LEFT, TitledBorder.TOP));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        guildInfoPanel.add(new JLabel("Guild Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        guildNameField = new JTextField(20);
        guildInfoPanel.add(guildNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        guildInfoPanel.add(new JLabel("Total Guild Points (mil):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        totalGuildPointsField = new JTextField(20);
        guildInfoPanel.add(totalGuildPointsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        guildInfoPanel.add(new JLabel("Active Guild Members:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        SpinnerNumberModel memberModel = new SpinnerNumberModel(25, 1, 50, 1);
        activeGuildMembersSpinner = new JSpinner(memberModel);
        guildInfoPanel.add(activeGuildMembersSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        guildInfoPanel.add(new JLabel("Assumed GP Efficiency (%):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        assumedGpEfficiencyField = new JTextField(20);
        assumedGpEfficiencyField.setText("0");
        guildInfoPanel.add(assumedGpEfficiencyField, gbc);

        mainPanel.add(guildInfoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        includeMissionsCheckBox = new JCheckBox("Include missions in calculation?");
        includeMissionsCheckBox.setSelected(false);
        includeMissionsCheckBox.addActionListener(e -> {
            missionPanel.setVisible(includeMissionsCheckBox.isSelected());
            revalidate();
            repaint();
        });
        checkBoxPanel.add(includeMissionsCheckBox);
        mainPanel.add(checkBoxPanel);

        missionPanel = new JPanel(new GridBagLayout());
        missionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Assumed Mission Efficiency (%)",
                TitledBorder.LEFT, TitledBorder.TOP));
        missionPanel.setVisible(false);

        missionEfficiencyFields = new JTextField[6];
        GridBagConstraints mgbc = new GridBagConstraints();
        mgbc.insets = new Insets(5, 5, 5, 5);
        mgbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < 6; i++) {
            mgbc.gridx = 0;
            mgbc.gridy = i;
            mgbc.weightx = 0.3;
            missionPanel.add(new JLabel(MISSION_LABELS[i]), mgbc);

            mgbc.gridx = 1;
            mgbc.weightx = 0.7;
            missionEfficiencyFields[i] = new JTextField(20);
            missionEfficiencyFields[i].setText("0");
            missionPanel.add(missionEfficiencyFields[i], mgbc);
        }

        mainPanel.add(missionPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel readyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        zeffoReadyCheckBox = new JCheckBox("Zeffo ready?");
        zeffoReadyCheckBox.setSelected(false);
        zeffoReadyCheckBox.addActionListener(e -> zeffoReady = zeffoReadyCheckBox.isSelected());

        mandaloreReadyCheckBox = new JCheckBox("Mandalore ready?");
        mandaloreReadyCheckBox.setSelected(false);
        mandaloreReadyCheckBox.addActionListener(e -> mandaloreReady = mandaloreReadyCheckBox.isSelected());

        readyPanel.add(zeffoReadyCheckBox);
        readyPanel.add(mandaloreReadyCheckBox);
        mainPanel.add(readyPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel depthPanel = new JPanel(new GridBagLayout());
        depthPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "# of possible platoons per planet",
                TitledBorder.LEFT, TitledBorder.TOP));

        depthFields = new JTextField[7];
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(5, 5, 5, 5);
        dgbc.fill = GridBagConstraints.HORIZONTAL;

        String[] depthLabels = {
                "Mustafar, Corellia, Coruscant: ", "Geonosis, Felucia, Bracca: ", "Dathomir, Tatooine, Kashyyk: ",
                "Haven, Kessel, Lothal: ", "Malachor, Vandor, Kafrene: ", "Death Star, Hoth, Scarif: ",
                "Zeffo, Mandalore: "
        };

        for (int i = 0; i < 7; i++) {
            dgbc.gridx = 0;
            dgbc.gridy = i;
            dgbc.weightx = 0.3;
            depthPanel.add(new JLabel(depthLabels[i]), dgbc);

            dgbc.gridx = 1;
            dgbc.weightx = 0.7;
            depthFields[i] = new JTextField(20);
            if (i < 6) {
                depthFields[i].setText("0, 0, 0");
            } else {
                depthFields[i].setText("0, 0");
            }

            depthPanel.add(depthFields[i], dgbc);
        }

        mainPanel.add(depthPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        statusLabel = new JLabel(" ");
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveGuildButton = new JButton("Save guild data");
        startRunButton = new JButton("Start strategy calculation");

        saveGuildButton.addActionListener(e -> handleSave());
        startRunButton.addActionListener(e -> handleStartRun());

        buttonPanel.add(saveGuildButton);
        buttonPanel.add(startRunButton);
        mainPanel.add(buttonPanel);

        loadButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loadGuildButton = new JButton("Load saved guild");
        loadGuildButton.addActionListener(e -> handleLoad());
        loadButtonPanel.add(loadGuildButton);
        loadButtonPanel.setVisible(Files.exists(SAVE_FILE_PATH));
        mainPanel.add(loadButtonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Simulation Results");
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        resultTextArea = new JTextPane();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));

        StyledDocument doc = resultTextArea.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        JScrollPane textScroll = new JScrollPane(resultTextArea);
        textScroll.setPreferredSize(new Dimension(520, 700));
        panel.add(textScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        resultIndexLabel = new JLabel("Result 1 of 1");
        resultIndexLabel.setHorizontalAlignment(JLabel.CENTER);
        resultIndexLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        bottomPanel.add(resultIndexLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        prevResultButton = new JButton("Previous result");
        nextResultButton = new JButton("Next result");

        prevResultButton.addActionListener(e -> showResult(currentResultIndex - 1));
        nextResultButton.addActionListener(e -> showResult(currentResultIndex + 1));

        navPanel.add(prevResultButton);
        navPanel.add(nextResultButton);
        bottomPanel.add(navPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton backButton = new JButton("Back to data entry");
        JButton runAgainButton = new JButton("Run again");

        backButton.addActionListener(e -> switchToDataEntry());
        runAgainButton.addActionListener(e -> handleRunAgain());

        actionPanel.add(backButton);
        actionPanel.add(runAgainButton);
        bottomPanel.add(actionPanel);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void handleSave() {
        try {
            parseAndStoreValues();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    NUMBER_FORMAT_ERROR_MESSAGE,
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            saveGuildToFile();
            loadButtonPanel.setVisible(true);
            revalidate();
            JOptionPane.showMessageDialog(this,
                    "Guild data has been saved.",
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save guild data:\n" + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveGuildToFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(SAVE_FILE_PATH, StandardCharsets.UTF_8)) {
            String safeName = guildNameField.getText().replaceAll("[\\r\\n\\t]", "").trim();
            if (safeName.length() > 200) {
                safeName = safeName.substring(0, 200);
            }

            writer.write("guildName=" + safeName);
            writer.newLine();
            writer.write("totalGuildPoints=" + totalGuildPoints);
            writer.newLine();
            writer.write("activeGuildMembers=" + activeGuildMembers);
            writer.newLine();
            writer.write("assumedGpEfficiency=" + assumedGpEfficiency);
            writer.newLine();
            writer.write("zeffoReady=" + zeffoReady);
            writer.newLine();
            writer.write("mandaloreReady=" + mandaloreReady);
            writer.newLine();

            // Mission efficiencies as semicolon-separated (6 values)
            StringBuilder missionSb = new StringBuilder("missionEfficiency=");
            for (int i = 0; i < 6; i++) {
                if (i > 0)
                    missionSb.append(";");
                missionSb.append(assumedMissionEfficiency[i]);
            }
            writer.write(missionSb.toString());
            writer.newLine();

            StringBuilder opsSb = new StringBuilder("operationsPossible=");
            for (int i = 0; i < 7; i++) {
                if (i > 0)
                    opsSb.append(";");
                for (int j = 0; j < operationsPossible[i].length; j++) {
                    if (j > 0)
                        opsSb.append("|");
                    opsSb.append(operationsPossible[i][j]);
                }
            }
            writer.write(opsSb.toString());
            writer.newLine();
        }
    }

    private void handleLoad() {
        try {
            loadGuildFromFile();
            populateFieldsFromStoredValues();
            JOptionPane.showMessageDialog(this,
                    "Guild data loaded successfully.",
                    "Load Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load guild data:\n" + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Save file is corrupted or has been tampered with:\n" + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGuildFromFile() throws IOException, IllegalArgumentException {
        Map<String, String> data = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(SAVE_FILE_PATH, StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount > 20) {
                    throw new IllegalArgumentException("Save file has too many lines.");
                }
                if (line.length() > 1000) {
                    throw new IllegalArgumentException("Save file contains a line that is too long.");
                }

                int eqIndex = line.indexOf('=');
                if (eqIndex < 0)
                    continue;
                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();

                if (isValidKey(key)) {
                    data.put(key, value);
                }
            }
        }

        guildName = sanitizeString(getRequired(data, "guildName"));
        totalGuildPoints = parseDoubleStrict(getRequired(data, "totalGuildPoints"), "totalGuildPoints");
        activeGuildMembers = parseIntStrict(getRequired(data, "activeGuildMembers"), "activeGuildMembers", 1, 50);
        assumedGpEfficiency = parseDoubleStrict(getRequired(data, "assumedGpEfficiency"), "assumedGpEfficiency");
        zeffoReady = parseBooleanStrict(getRequired(data, "zeffoReady"), "zeffoReady");
        mandaloreReady = parseBooleanStrict(getRequired(data, "mandaloreReady"), "mandaloreReady");

        // Parse mission efficiencies
        String missionStr = getRequired(data, "missionEfficiency");
        String[] missionParts = missionStr.split(";", -1);
        if (missionParts.length != 6) {
            throw new IllegalArgumentException("missionEfficiency must have exactly 6 values.");
        }
        assumedMissionEfficiency = new double[6];
        for (int i = 0; i < 6; i++) {
            assumedMissionEfficiency[i] = parseDoubleStrict(missionParts[i], "missionEfficiency[" + i + "]");
        }

        // Parse operations possible
        String opsStr = getRequired(data, "operationsPossible");
        String[] opsRows = opsStr.split(";", -1);
        if (opsRows.length != 7) {
            throw new IllegalArgumentException("operationsPossible must have exactly 7 rows.");
        }
        operationsPossible = new int[7][];
        for (int i = 0; i < 7; i++) {
            String[] vals = opsRows[i].split("\\|", -1);
            int expectedLen = (i < 6) ? 3 : 2;
            if (vals.length != expectedLen) {
                throw new IllegalArgumentException(
                        "operationsPossible row " + i + " must have " + expectedLen + " values.");
            }
            operationsPossible[i] = new int[expectedLen];
            for (int j = 0; j < expectedLen; j++) {
                operationsPossible[i][j] = parseIntStrict(vals[j], "operationsPossible[" + i + "][" + j + "]",
                        Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
        }
    }

    private boolean isValidKey(String key) {
        return key.equals("guildName") || key.equals("totalGuildPoints") || key.equals("activeGuildMembers")
                || key.equals("assumedGpEfficiency") || key.equals("zeffoReady") || key.equals("mandaloreReady")
                || key.equals("missionEfficiency") || key.equals("operationsPossible");
    }

    private String getRequired(Map<String, String> data, String key) throws IllegalArgumentException {
        String value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return value;
    }

    private String sanitizeString(String input) {
        // Remove any control characters and limit length
        String sanitized = input.replaceAll("[\\r\\n\\t\\p{Cntrl}]", "").trim();
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }
        return sanitized;
    }

    private double parseDoubleStrict(String value, String fieldName) throws IllegalArgumentException {
        // Only allow digits, optional minus, optional dot
        if (!value.matches("-?\\d+(\\.\\d+)?([Ee]-?\\d+)?")) {
            throw new IllegalArgumentException("Invalid number for " + fieldName + ": " + value);
        }
        try {
            double result = Double.parseDouble(value);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                throw new IllegalArgumentException("Invalid number for " + fieldName + ": " + value);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + fieldName + ": " + value);
        }
    }

    private int parseIntStrict(String value, String fieldName, int min, int max) throws IllegalArgumentException {
        if (!value.matches("-?\\d+")) {
            throw new IllegalArgumentException("Invalid integer for " + fieldName + ": " + value);
        }
        try {
            int result = Integer.parseInt(value);
            if (result < min || result > max) {
                throw new IllegalArgumentException(fieldName + " out of range (" + min + "-" + max + "): " + result);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + fieldName + ": " + value);
        }
    }

    private boolean parseBooleanStrict(String value, String fieldName) throws IllegalArgumentException {
        if (value.equals("true"))
            return true;
        if (value.equals("false"))
            return false;
        throw new IllegalArgumentException("Invalid boolean for " + fieldName + ": " + value);
    }

    private void populateFieldsFromStoredValues() {
        guildNameField.setText(guildName);
        totalGuildPointsField.setText(String.valueOf(totalGuildPoints));
        activeGuildMembersSpinner.setValue(activeGuildMembers);
        // Store as decimal internally, display as percentage
        assumedGpEfficiencyField.setText(String.valueOf(assumedGpEfficiency * 100.0));

        boolean hasMissions = false;
        for (double v : assumedMissionEfficiency) {
            if (v != 0.0) {
                hasMissions = true;
                break;
            }
        }
        includeMissionsCheckBox.setSelected(hasMissions);
        missionPanel.setVisible(hasMissions);
        for (int i = 0; i < 6; i++) {
            missionEfficiencyFields[i].setText(String.valueOf(assumedMissionEfficiency[i] * 100.0));
        }

        zeffoReadyCheckBox.setSelected(zeffoReady);
        mandaloreReadyCheckBox.setSelected(mandaloreReady);

        for (int i = 0; i < 7; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < operationsPossible[i].length; j++) {
                if (j > 0)
                    sb.append(", ");
                sb.append(operationsPossible[i][j]);
            }
            depthFields[i].setText(sb.toString());
        }

        revalidate();
        repaint();
    }

    private void parseAndStoreValues() throws NumberFormatException {
        guildName = guildNameField.getText();
        totalGuildPoints = Double.parseDouble(totalGuildPointsField.getText());
        activeGuildMembers = (Integer) activeGuildMembersSpinner.getValue();
        assumedGpEfficiency = Double.parseDouble(assumedGpEfficiencyField.getText()) / 100.0;

        assumedMissionEfficiency = new double[6];
        if (includeMissionsCheckBox.isSelected()) {
            for (int i = 0; i < 6; i++) {
                assumedMissionEfficiency[i] = Double.parseDouble(missionEfficiencyFields[i].getText()) / 100.0;
            }
        }

        zeffoReady = zeffoReadyCheckBox.isSelected();
        mandaloreReady = mandaloreReadyCheckBox.isSelected();

        operationsPossible = new int[7][];
        String[] depthLabels = {
                "Mustafar, Corellia, Coruscant", "Geonosis, Felucia, Bracca", "Dathomir, Tatooine, Kashyyk",
                "Haven, Kessel, Lothal", "Malachor, Vandor, Kafrene", "Death Star, Hoth, Scarif",
                "Zeffo, Mandalore"
        };
        for (int i = 0; i < 7; i++) {
            String[] parts = depthFields[i].getText().split(",");
            int expectedParts = (i < 6) ? 3 : 2;
            if (parts.length != expectedParts) {
                throw new NumberFormatException(
                        "Expected " + expectedParts + " comma-separated integers for " + depthLabels[i]);
            }
            operationsPossible[i] = new int[expectedParts];
            for (int j = 0; j < expectedParts; j++) {
                operationsPossible[i][j] = Integer.parseInt(parts[j].trim());
            }
        }
    }

    private void handleStartRun() {
        try {
            parseAndStoreValues();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    NUMBER_FORMAT_ERROR_MESSAGE,
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        prepareRun();
        simulationWrapper();
    }

    private void handleRunAgain() {
        prepareRun();
        simulationWrapper();
    }

    private void simulationWrapper() {
        statusLabel.setText("RotE run calculating ...");
        startRunButton.setEnabled(false);

        SwingWorker<ArrayList<TbRun>, Void> worker = new SwingWorker<>() {
            @Override
            protected ArrayList<TbRun> doInBackground() throws Exception {
                return simmer.executeSimulation(10);
            }

            @Override
            protected void done() {
                try {
                    results = get();
                    currentResultIndex = 0;
                    statusLabel.setText("Stars achieved in the best run: " + results.get(0).starCounter);
                    showResult(0);
                    switchToResultsView();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof IllegalArgumentException) {
                        statusLabel.setText("Calculation failed.");
                        JOptionPane.showMessageDialog(StrategyCalculator.this,
                                "No result could be calculated. Total Guild Points or Assumed GP Efficiency might be too low.",
                                "Calculation Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        statusLabel.setText("Error: " + ex.getMessage());
                        JOptionPane.showMessageDialog(StrategyCalculator.this,
                                "Simulation error: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    startRunButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void showResult(int index) {
        if (results == null || results.isEmpty())
            return;

        if (index < 0)
            index = 0;
        if (index >= results.size())
            index = results.size() - 1;

        currentResultIndex = index;
        resultTextArea.setText(results.get(currentResultIndex).getResultString(index));
        // Re-apply centered alignment after setText
        StyledDocument doc = resultTextArea.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        resultTextArea.setCaretPosition(0);
        resultIndexLabel.setText("Result " + (currentResultIndex + 1) + " of " + results.size());

        prevResultButton.setEnabled(currentResultIndex > 0);
        nextResultButton.setEnabled(currentResultIndex < results.size() - 1);
    }

    private void switchToResultsView() {
        cardLayout.show(cardPanel, RESULTS_VIEW);
        revalidate();
        repaint();
    }

    private void switchToDataEntry() {
        populateFieldsFromStoredValues();
        statusLabel.setText(" ");
        cardLayout.show(cardPanel, DATA_ENTRY_VIEW);
        revalidate();
        repaint();
    }

    public void prepareRun() {
        activeGuild = new GuildData(totalGuildPoints, activeGuildMembers, assumedGpEfficiency,
                assumedMissionEfficiency, operationsPossible, zeffoReady, mandaloreReady);
        simmer = new SimulationController(activeGuild);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StrategyCalculator frame = new StrategyCalculator();
            frame.setVisible(true);
        });
    }
}