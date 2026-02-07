
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class Main {
        /*
         * double totalGuildPoints;
         * int activeGuildMembers;
         * double assumedGpEfficiency;
         * double[] missionEfficiency;
         * int[][] operationsPossible;
         */
        public static void main(String[] args) {

                GuildData SoN = new GuildData(395.78, 48, 0.9, new double[] { 0.30, 0.25, 0.15, 0.05, 0.0, 0.0 },
                                new int[][] { { 6, 6, 6 }, { 6, 5, 4 }, { 4, 2, 3 }, { 0, 0, 0 }, { 0, 0, 0 },
                                                { 0, 0, 0 }, { 0, 0 } });

                SimulationController simmer = new SimulationController(SoN);

                /*
                 * SwingUtilities.invokeLater(new Runnable() {
                 * public void run() {
                 * SimpleUI ui = new SimpleUI();
                 * ui.setVisible(true);
                 * }
                 * });
                 */

                JFrame window = new JFrame("RotE Calculator");
                window.setVisible(true);
                window.setSize(1000, 500);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JLabel label = new JLabel("No run started yet.");

                // JTextArea textArea = new JTextArea(30, 30);
                // textArea.setLineWrap(true);
                // textArea.setWrapStyleWord(true);

                // JScrollPane scrollPane = new JScrollPane(textArea);

                JButton button1 = new JButton("Start run");
                button1.addActionListener(e -> {
                        label.setText("RotE run calculating ...");
                        button1.setEnabled(false);

                        SwingWorker<String, Void> worker = new SwingWorker<>() {
                                @Override
                                protected String doInBackground() throws Exception {
                                        ArrayList<TbRun> results = simmer.executeSimulation();
                                        return "Stars achieved in the best run: " + results.get(0).starCounter;
                                }

                                @Override
                                protected void done() {
                                        // This runs on EDT when background work is done
                                        try {
                                                String result = get();
                                                label.setText(result);
                                        } catch (Exception e) {
                                                label.setText("Error: " + e.getMessage());
                                        } finally {
                                                button1.setEnabled(true);
                                        }
                                }

                                // textArea.setText(simmer.outputTopXRunString(results, 3));
                                // label.setText("Maximum stars achieved: " + results.get(0).starCounter);
                        };
                        worker.execute();
                });

                JPanel panel = new JPanel();
                panel.setBackground(Color.LIGHT_GRAY);
                panel.add(label);
                // panel.add(textArea);
                // panel.add(scrollPane);
                window.add(panel);
                panel.add(button1);
        }
}