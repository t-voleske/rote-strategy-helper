public class GuildData {
    double totalGuildPoints;
    int activeGuildMembers;
    double[] assumedMissionEfficiency; // mission efficiency per planet depth as a decimal, e.g. [0.5, 0.6, 0.7, 0.8,
                                       // 0.9, 1.0]
    int[][] operationsArray; // 2D array representing number of operations possible per planet
    double assumedGpBudget;
    boolean zeffoReady;
    boolean mandaloreReady;

    public GuildData(double totalGuildPoints, int activeGuildMembers, double assumedGpEfficiency,
            double[] assumedMissionEfficiency, int[][] operationsArray, boolean zeffoReady, boolean mandaloreReady) {
        this.totalGuildPoints = round(totalGuildPoints);
        this.activeGuildMembers = activeGuildMembers;
        this.assumedMissionEfficiency = assumedMissionEfficiency;
        this.operationsArray = operationsArray; // Operations array passed in: [[DS_planet1_ops, MI_planet1_ops,
                                                // LS_planet1_ops],...,[Mandalore_planet_ops],[Zeffo_planet_ops]]
        this.assumedGpBudget = round(totalGuildPoints * assumedGpEfficiency);
        this.zeffoReady = zeffoReady;
        this.mandaloreReady = mandaloreReady;
    }

    public final double round(double a) {
        double roundOff = (double) Math.round(a * 100) / 100;
        return roundOff;
    }
}
