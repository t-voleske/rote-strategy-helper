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
import javax.swing.JSeparator;
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

public class StrategyHelper extends JFrame {
    private JTextField guildNameField;
    private JTextField totalGuildPointsField;
    private JSpinner activeGuildMembersSpinner;
    private JTextField assumedGpEfficiencyField;
    private JTextField[] missionEfficiencyFields;
    private JTextField[] depthFields;
    private JCheckBox includeMissionsCheckBox;
    private JCheckBox includeMissionsManualCheckBox;
    private JCheckBox zeffoReadyCheckBox;
    private JCheckBox mandaloreReadyCheckBox;
    private JPanel missionPanel;
    private JButton loadGuildButton;
    private JPanel loadButtonPanel;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JPanel dataEntryPanel;
    private final JPanel resultsPanel;

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

    private ManualSimulation manualSim;
    private JPanel manualSimPanel;
    private JLabel msPhaseLabel;
    private JLabel msStarLabel;
    private JLabel msBudgetLabel;
    private JLabel[] msPlanetNameLabels;
    private JLabel[] msPlanetStarLabels;
    private JLabel[] msPlanetPointsLabels;
    private JButton[][] msCheckpointButtons;
    private JButton[] msLeftoverButtons;
    private JButton[] msCustomPointsButtons;
    private JTextField[] msCustomPointsFields;
    private JPanel[] msCustomPointsRows;
    private JPanel[] msPlanetRows;
    private JSeparator[] msPlanetSeparators;
    private JButton msAdvanceButton;
    private JButton msReviewButton;
    private JLabel msStatusLabel;
    private CardLayout msCardLayout;
    private JPanel msCardPanel;
    private JTextPane msReviewTextPane;

    private boolean addMissionPoints = false;

    private static final String SAVE_FILE_NAME = "guild_save.txt";
    private static final Path SAVE_FILE_PATH = Paths.get(SAVE_FILE_NAME);

    private static final String[] MISSION_LABELS = {
            "R5 mission", "R6 mission", "R7 mission", "R8 mission", "R9 (1) mission", "R9 (2) mission"
    };

    private static final String DATA_ENTRY_VIEW = "dataEntry";
    private static final String RESULTS_VIEW = "results";
    private static final String MANUAL_SIM_VIEW = "manualSim";

    private static final String NUMBER_FORMAT_ERROR_MESSAGE = """
            Invalid input format.

            Please use a dot (.) as the decimal separator instead of a comma for numbers.
            For example: 300.5 instead of 300,5.""";

    public StrategyHelper() {
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
        mainPanel.setBorder(new EmptyBorder(15, 15, 0, 15));

        JPanel guildInfoPanel = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
            }
        };
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
        guildNameField = new JTextField("Guild Name", 20);
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
        assumedGpEfficiencyField.setText("50");
        guildInfoPanel.add(assumedGpEfficiencyField, gbc);

        mainPanel.add(guildInfoPanel);
        // mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

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

        includeMissionsManualCheckBox = new JCheckBox("Automatically add mission points in manual simulation?");
        includeMissionsManualCheckBox.setSelected(false);
        includeMissionsManualCheckBox.addActionListener(e -> {
            toggleMissionPoints();
        });

        missionEfficiencyFields = new JTextField[6];
        GridBagConstraints mgbc = new GridBagConstraints();
        mgbc.insets = new Insets(5, 5, 5, 5);
        mgbc.fill = GridBagConstraints.HORIZONTAL;

        missionPanel.add(includeMissionsManualCheckBox, mgbc);

        for (int i = 0; i < 6; i++) {
            mgbc.gridx = 0;
            mgbc.gridy = i + 1;
            mgbc.weightx = 0.3;
            missionPanel.add(new JLabel(MISSION_LABELS[i]), mgbc);

            mgbc.gridx = 1;
            mgbc.weightx = 0.7;
            missionEfficiencyFields[i] = new JTextField(20);
            missionEfficiencyFields[i].setText("0");
            missionPanel.add(missionEfficiencyFields[i], mgbc);
        }

        mainPanel.add(missionPanel);
        // mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

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
        // mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel depthPanel = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
            }
        };
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

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Status",
                TitledBorder.LEFT, TitledBorder.TOP));
        statusLabel = new JLabel(" ");
        statusPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveGuildButton = new JButton("Save guild data");
        startRunButton = new JButton("Start strategy calculation");

        saveGuildButton.addActionListener(e -> handleSave());
        startRunButton.addActionListener(e -> handleStartRun());

        JButton manualSimButton = new JButton("Manual Simulation");
        manualSimButton.addActionListener(e -> handleManualSimulation());

        buttonPanel.add(saveGuildButton);
        buttonPanel.add(startRunButton);
        buttonPanel.add(manualSimButton);

        loadButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loadGuildButton = new JButton("Load saved guild");
        loadGuildButton.addActionListener(e -> handleLoad());
        loadButtonPanel.add(loadGuildButton);
        loadButtonPanel.setVisible(Files.exists(SAVE_FILE_PATH));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        bottomPanel.add(statusPanel);
        bottomPanel.add(buttonPanel);
        bottomPanel.add(loadButtonPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(mainPanel, BorderLayout.NORTH);
        wrapper.add(bottomPanel, BorderLayout.SOUTH);
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
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            saveGuildToFile();
            loadButtonPanel.setVisible(true);
            revalidate();
            statusLabel.setText("Guild data has been saved.");
        } catch (IOException ex) {
            statusLabel.setText("Failed to save guild data: " + ex.getMessage());
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
            writer.write("addMissionPoints=" + addMissionPoints);
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
            statusLabel.setText("Guild data loaded successfully.");
        } catch (IOException ex) {
            statusLabel.setText("Failed to load guild data:\n" + ex.getMessage());
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
        addMissionPoints = parseBooleanStrict(getRequired(data, "addMissionPoints"), "addMissionPoints");

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
                || key.equals("missionEfficiency") || key.equals("operationsPossible")
                || key.equals("addMissionPoints");
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

        includeMissionsManualCheckBox.setSelected(addMissionPoints);

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

    private void parseAndStoreValues() throws IllegalArgumentException {
        if (guildNameField.getText().equalsIgnoreCase("")) {
            throw new IllegalArgumentException("Please provide a guild name.");
        }
        guildName = guildNameField.getText();
        if (totalGuildPointsField.getText().isBlank()
                || Double.parseDouble(totalGuildPointsField.getText()) < 200) {
            throw new IllegalArgumentException("Guild GP has to be at least 200m, to be able to enter RotE TB.");
        }
        totalGuildPoints = Double.parseDouble(totalGuildPointsField.getText());
        activeGuildMembers = (Integer) activeGuildMembersSpinner.getValue();
        if (assumedGpEfficiencyField.getText().isBlank()
                || Double.parseDouble(assumedGpEfficiencyField.getText()) < 0) {
            throw new IllegalArgumentException(
                    "Please enter a valid number for Assumed GP Efficiency. Negative numbers are not allowed.");
        }
        assumedGpEfficiency = Double.parseDouble(assumedGpEfficiencyField.getText()) / 100.0;

        assumedMissionEfficiency = new double[6];
        if (includeMissionsCheckBox.isSelected()) {
            for (int i = 0; i < 6; i++) {
                if (missionEfficiencyFields[i].getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Please provide values for Assumed Mission Efficiency fields, or uncheck the inclusion checkmark above.");
                } else if (Double.parseDouble(missionEfficiencyFields[i].getText()) < 0
                        || Double.parseDouble(missionEfficiencyFields[i].getText()) > 100) {
                    throw new IllegalArgumentException(
                            "Values for Assumed Mission Efficiency fields have to be at least 0 and at most 100.");
                }
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
                        "Please enter the amount of possible platoons per planet as comma separated numbers,\n e.g. '3, 6, 3' for 3 planets");
            }
            operationsPossible[i] = new int[expectedParts];
            for (int j = 0; j < expectedParts; j++) {
                int tempInt = Integer.parseInt(parts[j].trim());
                if (tempInt < 0 || tempInt > 6) {
                    throw new IllegalArgumentException(
                            "Invalid value for number of possible platoons. Each value has to be at least 0 and at most 6.");
                }
                operationsPossible[i][j] = tempInt;
            }
        }
    }

    private void handleStartRun() {
        try {
            parseAndStoreValues();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
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
                return simmer.executeSimulation(15);
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
                        JOptionPane.showMessageDialog(StrategyHelper.this,
                                "No result could be calculated. Total Guild Points or Assumed GP Efficiency might be too low.",
                                "Calculation Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        statusLabel.setText("Error: " + ex.getMessage());
                        JOptionPane.showMessageDialog(StrategyHelper.this,
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

    // ------------------------------------------------------------------
    // Manual Simulation
    // ------------------------------------------------------------------

    private void handleManualSimulation() {
        try {
            parseAndStoreValues();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedPhase = 1; // TODO: allow picking a later phase from an automated run
        prepareRun();
        manualSim = new ManualSimulation(activeGuild, selectedPhase);
        manualSim.setAddMissionPoints(addMissionPoints);
        manualSim.setUpTbRun(null);

        switchToManualSimView();
    }

    private void switchToManualSimView() {
        if (manualSimPanel != null) {
            cardPanel.remove(manualSimPanel);
        }
        manualSimPanel = buildManualSimPanel();
        cardPanel.add(manualSimPanel, MANUAL_SIM_VIEW);

        setMinimumSize(new Dimension(900, 550));
        setPreferredSize(new Dimension(900, 550));
        pack();
        setLocationRelativeTo(null);
        cardLayout.show(cardPanel, MANUAL_SIM_VIEW);
        refreshManualSimPanel();
    }

    private void switchToDataEntryFromManualSim() {
        setMinimumSize(new Dimension(550, 900));
        setPreferredSize(new Dimension(550, 900));
        pack();
        setLocationRelativeTo(null);
        switchToDataEntry();
    }

    private JPanel buildManualSimPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- NORTH: Info bar ---
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        msPhaseLabel = new JLabel();
        msStarLabel = new JLabel();
        msBudgetLabel = new JLabel();
        infoPanel.add(msPhaseLabel);
        infoPanel.add(msStarLabel);
        infoPanel.add(msBudgetLabel);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // --- CENTER: Planet rows ---
        JPanel planetsPanel = new JPanel();
        planetsPanel.setLayout(new BoxLayout(planetsPanel, BoxLayout.Y_AXIS));
        planetsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Planets",
                TitledBorder.LEFT, TitledBorder.TOP));
        msPlanetRows = new JPanel[5];
        msPlanetSeparators = new JSeparator[4];
        msPlanetNameLabels = new JLabel[5];
        msPlanetStarLabels = new JLabel[5];
        msPlanetPointsLabels = new JLabel[5];
        msCheckpointButtons = new JButton[5][4];
        msLeftoverButtons = new JButton[5];
        msCustomPointsButtons = new JButton[5];
        msCustomPointsFields = new JTextField[5];
        msCustomPointsRows = new JPanel[5];

        String[] checkpointLabels = { "Preload", "1 Star", "2 Stars", "3 Stars" };

        for (int i = 0; i < 5; i++) {
            msPlanetRows[i] = new JPanel(new BorderLayout());

            // Left side: labels
            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            msPlanetNameLabels[i] = new JLabel();
            msPlanetNameLabels[i].setPreferredSize(new Dimension(75, 20));
            msPlanetStarLabels[i] = new JLabel();
            msPlanetStarLabels[i].setPreferredSize(new Dimension(120, 20));
            msPlanetPointsLabels[i] = new JLabel();
            msPlanetPointsLabels[i].setPreferredSize(new Dimension(160, 20));
            labelPanel.add(msPlanetNameLabels[i]);
            labelPanel.add(msPlanetStarLabels[i]);
            labelPanel.add(msPlanetPointsLabels[i]);

            // Right side: buttons stacked vertically
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            final int planetIndex = i;
            for (int j = 0; j < 4; j++) {
                final int checkpoint = j;
                msCheckpointButtons[i][j] = new JButton(checkpointLabels[j]);
                msCheckpointButtons[i][j].addActionListener(e -> handleCheckpoint(checkpoint, planetIndex));
                buttonPanel.add(msCheckpointButtons[i][j]);
            }
            msLeftoverButtons[i] = new JButton("Add leftover points");
            msLeftoverButtons[i].addActionListener(e -> handleAddLeftover(planetIndex));
            msLeftoverButtons[i].setVisible(false);
            buttonPanel.add(msLeftoverButtons[i]);
            rightPanel.add(buttonPanel);

            // Custom points row below other buttons
            msCustomPointsRows[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            msCustomPointsFields[i] = new JTextField(8);
            msCustomPointsButtons[i] = new JButton("Add custom points");
            msCustomPointsButtons[i].addActionListener(e -> handleAddCustomPoints(planetIndex));
            msCustomPointsRows[i].add(new JLabel("Points (mil):"));
            msCustomPointsRows[i].add(msCustomPointsFields[i]);
            msCustomPointsRows[i].add(msCustomPointsButtons[i]);
            msCustomPointsRows[i].setVisible(false);
            rightPanel.add(msCustomPointsRows[i]);

            msPlanetRows[i].add(labelPanel, BorderLayout.WEST);
            msPlanetRows[i].add(rightPanel, BorderLayout.EAST);

            planetsPanel.add(msPlanetRows[i]);
            if (i < 4) {
                msPlanetSeparators[i] = new JSeparator();
                planetsPanel.add(msPlanetSeparators[i]);
            }
        }

        mainPanel.add(planetsPanel, BorderLayout.CENTER);

        // --- SOUTH: Controls ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton undoButton = new JButton("Undo");
        JButton resetButton = new JButton("Reset Phase");
        msAdvanceButton = new JButton("Advance Phase");
        msReviewButton = new JButton("Review Run");
        msReviewButton.setVisible(false);

        undoButton.addActionListener(e -> handleUndo());
        resetButton.addActionListener(e -> handleResetPhase());
        msAdvanceButton.addActionListener(e -> handleAdvancePhase());
        msReviewButton.addActionListener(e -> handleReviewRun());

        msStatusLabel = new JLabel(" ");
        msStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        msStatusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        controlPanel.add(msStatusLabel);

        buttonRow.add(undoButton);
        buttonRow.add(resetButton);
        buttonRow.add(msAdvanceButton);
        buttonRow.add(msReviewButton);
        controlPanel.add(buttonRow);

        JPanel returnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton returnButton = new JButton("Return to data entry");
        returnButton.addActionListener(e -> switchToDataEntryFromManualSim());
        returnPanel.add(returnButton);
        controlPanel.add(returnPanel);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // --- Review panel (second card) ---
        JPanel reviewPanel = new JPanel(new BorderLayout(10, 10));
        reviewPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel reviewTitle = new JLabel("Run Summary");
        reviewTitle.setFont(reviewTitle.getFont().deriveFont(18f));
        reviewTitle.setHorizontalAlignment(JLabel.CENTER);
        reviewPanel.add(reviewTitle, BorderLayout.NORTH);

        msReviewTextPane = new JTextPane();
        msReviewTextPane.setEditable(false);
        msReviewTextPane.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        JScrollPane reviewScroll = new JScrollPane(msReviewTextPane);
        reviewPanel.add(reviewScroll, BorderLayout.CENTER);

        JPanel reviewButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backToSimButton = new JButton("Back to simulation");
        backToSimButton.addActionListener(e -> {
            setMinimumSize(new Dimension(900, 550));
            setPreferredSize(new Dimension(900, 550));
            pack();
            setLocationRelativeTo(null);
            msCardLayout.show(msCardPanel, "simulation");
        });
        reviewButtonPanel.add(backToSimButton);
        JButton reviewReturnButton = new JButton("Return to data entry");
        reviewReturnButton.addActionListener(e -> switchToDataEntryFromManualSim());
        reviewButtonPanel.add(reviewReturnButton);
        reviewPanel.add(reviewButtonPanel, BorderLayout.SOUTH);

        // --- Card layout to switch between simulation and review ---
        msCardLayout = new CardLayout();
        msCardPanel = new JPanel(msCardLayout);
        msCardPanel.add(mainPanel, "simulation");
        msCardPanel.add(reviewPanel, "review");

        return msCardPanel;
    }

    private void handleCheckpoint(int checkpoint, int planetIndex) {
        try {
            double opsPoints = manualSim.attemptCheckpoint(checkpoint, planetIndex);
            if (opsPoints > 0) {
                msStatusLabel.setText("Operations triggered! " + opsPoints + " points added to the budget.");
            } else {
                msStatusLabel.setText(" ");
            }
        } catch (BudgetExceededException ex) {
            msStatusLabel.setText(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            msStatusLabel.setText(ex.getMessage());
        }
        refreshManualSimPanel();
    }

    private void handleUndo() {
        try {
            manualSim.undoAction();
            msStatusLabel.setText(" ");
        } catch (IndexOutOfBoundsException ex) {
            msStatusLabel.setText(ex.getMessage());
        }
        refreshManualSimPanel();
    }

    private void handleResetPhase() {
        manualSim.resetPhase();
        msStatusLabel.setText(" ");
        refreshManualSimPanel();
    }

    private void handleAddCustomPoints(int planetIndex) {
        String input = msCustomPointsFields[planetIndex].getText();
        if (input == null || input.isBlank()) {
            msStatusLabel.setText("Please enter a points value.");
            return;
        }
        try {
            double points = Double.parseDouble(input);
            double cost = manualSim.addCustomPoints(planetIndex, points);
            msCustomPointsFields[planetIndex].setText("");
            msStatusLabel.setText(String.format("Added points. Budget cost: %.2fM", cost));
        } catch (NumberFormatException ex) {
            msStatusLabel.setText("Invalid number format. Use dot as decimal separator.");
        } catch (BudgetExceededException | IllegalArgumentException ex) {
            msStatusLabel.setText(ex.getMessage());
        }
        refreshManualSimPanel();
    }

    private void handleAddLeftover(int planetIndex) {
        try {
            manualSim.addLeftoverPoints(planetIndex);
            msStatusLabel.setText(" ");
        } catch (IllegalArgumentException ex) {
            msStatusLabel.setText(ex.getMessage());
        }
        refreshManualSimPanel();
    }

    private void handleAdvancePhase() {
        manualSim.advancePhase();
        msStatusLabel.setText(" ");
        refreshManualSimPanel();
    }

    private void handleReviewRun() {
        String summary = manualSim.getRunSummary();

        msReviewTextPane.setText(summary);
        StyledDocument doc = msReviewTextPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        msReviewTextPane.setCaretPosition(0);

        int availableWidth = getWidth()
                - getInsets().left - getInsets().right - 30;
        msReviewTextPane.setSize(availableWidth, Integer.MAX_VALUE);
        int textHeight = msReviewTextPane.getPreferredSize().height;
        int totalHeight = textHeight + 120;
        int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        totalHeight = Math.min(totalHeight, screenHeight - 50);
        totalHeight = Math.max(totalHeight, 550);

        setMinimumSize(new Dimension(900, totalHeight));
        setPreferredSize(new Dimension(900, totalHeight));
        pack();
        setLocationRelativeTo(null);
        msCardLayout.show(msCardPanel, "review");
    }

    private void refreshManualSimPanel() {
        String[] state = manualSim.returnCurrentState();
        String[] planetNames = manualSim.getActivePlanetNames();
        int currentPhase = manualSim.getCurrentPhase();
        int totalStars = manualSim.getTotalStars();

        msPhaseLabel.setText("Phase: " + currentPhase + " of 6");
        msStarLabel.setText("Total Stars: " + totalStars);
        msBudgetLabel.setText(String.format("Budget: %.2fM", Double.parseDouble(state[10])));

        double currentBudget = Double.parseDouble(state[10]);

        for (int i = 0; i < 5; i++) {
            if (planetNames[i] == null) {
                msPlanetRows[i].setVisible(false);
                msCustomPointsRows[i].setVisible(false);
                continue;
            }
            String statusString = "";
            switch (Integer.parseInt(state[i])) {
                case -1 -> {
                    statusString = "Below preload";
                }
                case 0 -> {
                    statusString = "Fully preloaded";
                }
                case 1 -> {
                    statusString = "1 Star";
                }
                case 2 -> {
                    statusString = "2 Star";
                }
                case 3 -> {
                    statusString = "3 Star";
                }
            }

            msPlanetRows[i].setVisible(true);
            msPlanetNameLabels[i].setText(planetNames[i]);
            msPlanetStarLabels[i].setText(statusString);
            msPlanetPointsLabels[i].setText(state[i + 5] != null ? state[i + 5] : "");

            int[] reachable = manualSim.getReachableCheckpoints(i);
            for (int j = 0; j < 4; j++) {
                msCheckpointButtons[i][j].setVisible(reachable[j] == 1);
            }
            boolean belowPreload = state[i] != null && state[i].equals("-1");
            boolean threeStarUnreachable = reachable[3] == 0;
            msLeftoverButtons[i].setVisible(threeStarUnreachable && belowPreload && currentBudget > 0);
            boolean canAbsorbMore = state[i] != null && !state[i].equals("3");
            msCustomPointsRows[i].setVisible(canAbsorbMore && currentBudget > 0);
        }

        // Show separators only between visible planet rows
        for (int i = 0; i < 4; i++) {
            boolean currentVisible = planetNames[i] != null;
            boolean anyNextVisible = false;
            for (int k = i + 1; k < 5; k++) {
                if (planetNames[k] != null) {
                    anyNextVisible = true;
                    break;
                }
            }
            msPlanetSeparators[i].setVisible(currentVisible && anyNextVisible);
        }

        msAdvanceButton.setEnabled(currentPhase < 6);
        msReviewButton.setVisible(currentPhase == 6 && state[11].equals("1"));

        revalidate();
        repaint();
    }

    private void toggleMissionPoints() {
        this.addMissionPoints = !this.addMissionPoints;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StrategyHelper frame = new StrategyHelper();
            frame.setVisible(true);
        });
    }
}