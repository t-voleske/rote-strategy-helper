
public class TbRun {
    MapState[] roteRun;
    GuildData currentGuild;
    int currentPhase;
    int starCounter;
    int runWorth;
    boolean fullyProcessed;
    int processCounter;

    public TbRun(GuildData inputGuild) {
        this.roteRun = new MapState[6];
        this.currentGuild = inputGuild;

        this.currentPhase = 1;

        this.roteRun[this.currentPhase - 1] = new MapState(inputGuild);
        this.starCounter = 0;
        this.runWorth = 0;

        this.fullyProcessed = false;
        this.processCounter = 0;
    }

    public TbRun cloneRun() {
        TbRun clonedRun = new TbRun(this.currentGuild);

        clonedRun.currentPhase = this.currentPhase;
        clonedRun.starCounter = this.starCounter;
        clonedRun.runWorth = this.runWorth;
        clonedRun.fullyProcessed = this.fullyProcessed;
        clonedRun.processCounter = this.processCounter;

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
                        + " stars. Next star: " + activePlanets[i].neededForNextStar() + "m GP.");
            } else {
                activePlanetStars[i] = -1;
            }
        }
        return activePlanetStars;
    }

    public int updateCurrentStars() {
        int tmpCounter = 0;
        for (MapState ms : this.roteRun) {
            if (ms == null) {
                continue;
            }
            for (Planet pl : ms.getCurrentActivePlanets()) {
                if (pl == null) {
                    continue;
                }
                int stars = pl.getStarResult(); // int stars = pl.getCurrentStar();
                if (stars > 0) {
                    tmpCounter += stars;
                }

            }
        }
        this.starCounter = tmpCounter;
        return tmpCounter;
    }

    public String getResultString(int runIndex) {
        StringBuilder output = new StringBuilder();
        output.append("\n").append("==============================");
        output.append("\n").append("Top ").append((runIndex + 1)).append(" - ").append(this.starCounter)
                .append(" Stars :");
        output.append("\n").append("==============================");
        for (MapState m : this.roteRun) {
            output.append("\n").append("- Phase ").append(m.currentPhase).append(" -").append("\n");
            for (String line : m.getMapStatusString().split("\n")) {
                output.append(line).append("\n");
            }
            output.append("----------------------");
        }
        return output.toString();
    }

}