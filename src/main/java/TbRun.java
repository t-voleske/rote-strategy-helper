
public class TbRun {
    MapState[] roteRun;
    GuildData currentGuild;
    int currentPhase;
    int starCounter;
    boolean fullyProcessed;
    int processCounter;

    public TbRun(GuildData inputGuild) {
        this.roteRun = new MapState[6]; // Initialize array of the 6 MapState objects (1 for each phase)
        this.currentGuild = inputGuild;

        this.currentPhase = 1;

        this.roteRun[this.currentPhase - 1] = new MapState(inputGuild); // Create MapState object for phase 1
        this.starCounter = 0;
        // System.out.print(this.roteRun[0].getMapInfoString());
        this.fullyProcessed = false;
        this.processCounter = 0;
    }

    public TbRun cloneRun() {
        TbRun clonedRun = new TbRun(this.currentGuild);

        clonedRun.currentPhase = this.currentPhase;
        clonedRun.starCounter = this.starCounter;
        clonedRun.fullyProcessed = this.fullyProcessed;
        clonedRun.processCounter = this.processCounter;

        /*
         * clonedRun.roteRun = new MapState[6];
         * for (int i = 0; i < this.roteRun.length; i++) {
         * if (this.roteRun[i] != null) {
         * clonedRun.roteRun[i] = this.roteRun[i].cloneMapState();
         * } else {
         * clonedRun.roteRun[i] = null;
         * }
         * }
         */
        clonedRun.roteRun = this.roteRun.clone();
        return clonedRun;
    }

    public void getGuildInfo() {
        System.out.println("Guild GP: " + this.currentGuild.totalGuildPoints + "M, Active Members: "
                + this.currentGuild.activeGuildMembers);
    }

    public void getCurrentRunState() {
        System.out.println(this.roteRun[0].getMapInfoString());
    }

    public int[] evaluateActivePlanets(MapState currentMapState) {

        Planet[] activePlanets = currentMapState.getCurrentActivePlanets();
        int[] activePlanetStars = new int[activePlanets.length];
        for (int i = 0; i < activePlanets.length; i++) {
            if (activePlanets[i] != null) {
                activePlanetStars[i] = activePlanets[i].getCurrentStar();
                System.out.println("Active Planet " + i + ": " + activePlanets[i].name + " with " + activePlanetStars[i]
                        + " stars. Next star: " + activePlanets[i].neededForNextStar(activePlanets[i]) + "m GP.");
            } else {
                activePlanetStars[i] = -1;
            }
        }
        return activePlanetStars;
    }

    public int updateCurrentStars() {
        int tmpCounter = 0;
        for (MapState ms : roteRun) {
            if (ms == null) {
                continue;
            }
            for (Planet pl : ms.getCurrentActivePlanets()) {
                if (pl == null) {
                    continue;
                }
                tmpCounter += pl.getCurrentStar();
            }
        }
        this.starCounter = tmpCounter;
        return tmpCounter;
    }

}