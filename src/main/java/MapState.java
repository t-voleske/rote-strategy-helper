import java.util.LinkedList;
import java.util.Objects;

public class MapState {
    LinkedList<Planet> darkSide = new LinkedList<>();
    LinkedList<Planet> mixed = new LinkedList<>();
    LinkedList<Planet> lightSide = new LinkedList<>();
    LinkedList<Planet> mandalore = new LinkedList<>();
    LinkedList<Planet> zeffo = new LinkedList<>();

    int currentDsDepth;
    int currentMiDepth;
    int currentLsDepth;
    boolean mandaloreUnlocked = false;
    boolean zeffoUnlocked = false;
    GuildData currentGuild;
    int currentPhase;
    boolean isFinished;

    double phaseGpBudget;

    public MapState(GuildData inputGuild) {
        // Initialize Dark Side Planets
        darkSide.add(
                new Planet("Mustafar", 0, 0, 248.33, 186.25, 116.4, inputGuild.assumedMissionEfficiency[0], 4 * 0.200));
        darkSide.add(
                new Planet("Geonosis", 1, 0, 316.0, 237.0, 148.13, inputGuild.assumedMissionEfficiency[1], 4 * 0.250));
        darkSide.add(new Planet("Dathomir", 2, 0, 339.12, 254.34, 158.96, inputGuild.assumedMissionEfficiency[2],
                4 * 0.34125));
        darkSide.add(
                new Planet("Haven", 3, 0, 500.30, 400.24, 235.14, inputGuild.assumedMissionEfficiency[3], 4 * 0.4936));
        darkSide.add(new Planet("Malachor", 4, 0, 729.95, 620.46, 341.25, inputGuild.assumedMissionEfficiency[4],
                4 * 0.72174));
        darkSide.add(new Planet("Death Star", 5, 0, 1246.27, 1059.33, 582.63, inputGuild.assumedMissionEfficiency[5],
                4 * 1.15172 + 1 * 2.300));

        // Initialize Mixed Planets
        mixed.add(new Planet("Corellia", 0, 1, 238.33, 178.27, 111.72, inputGuild.assumedMissionEfficiency[0],
                3 * 0.200 + 1 * 0.400));
        mixed.add(new Planet("Felucia", 1, 1, 316.0, 237.0, 148.13, inputGuild.assumedMissionEfficiency[1],
                4 * 0.250 + 1 * 0.500));
        mixed.add(new Planet("Tatooine", 2, 1, 407.38, 305.53, 190.95, inputGuild.assumedMissionEfficiency[2],
                3 * 0.34125 + 1 * 0.6825));
        mixed.add(new Planet("Kessel", 3, 1, 500.30, 400.24, 235.14, inputGuild.assumedMissionEfficiency[3],
                3 * 0.4936 + 1 * 0.9872));
        mixed.add(new Planet("Vandor", 4, 1, 729.95, 620.46, 341.25, inputGuild.assumedMissionEfficiency[4],
                3 * 0.72174 + 1 * 1.44348));
        mixed.add(new Planet("Hoth", 5, 1, 1246.27, 1059.33, 582.63, inputGuild.assumedMissionEfficiency[5],
                4 * 1.15172 + 1 * 2.300));

        // Initialize Light Side Planets
        lightSide.add(new Planet("Coruscant", 0, 2, 248.33, 186.25, 116.4, inputGuild.assumedMissionEfficiency[0],
                4 * 0.200 + 1 * 0.400));
        lightSide.add(new Planet("Bracca", 1, 2, 303.5, 227.63, 142.27, inputGuild.assumedMissionEfficiency[1],
                3 * 0.250 + 1 * 0.500));
        lightSide.add(new Planet("Kashyyyk", 2, 2, 407.38, 305.53, 190.95, inputGuild.assumedMissionEfficiency[2],
                3 * 0.34125 + 1 * 0.6825));
        lightSide.add(new Planet("Lothal", 3, 2, 524.99, 419.99, 246.74, inputGuild.assumedMissionEfficiency[3],
                3 * 0.4936 + 1 * 0.9872));
        lightSide.add(new Planet("Ring of Kafrene", 4, 2, 729.95, 620.46, 341.25,
                inputGuild.assumedMissionEfficiency[4], 4 * 0.72174 + 1 * 1.44348));
        lightSide.add(new Planet("Scarif", 5, 2, 1188.69, 1010.38, 555.71, inputGuild.assumedMissionEfficiency[5],
                4 * 1.15172 + 1 * 2.300));

        // Initialize Bonus Planets
        mandalore.add(new Planet("Mandalore", 3, 3, 396.5, 316.4, 197.75, inputGuild.assumedMissionEfficiency[3],
                3 * 0.4936 + 1 * 0.9872));
        zeffo.add(new Planet("Zeffo", 3, 4, 287.18, 229.74, 143.59, inputGuild.assumedMissionEfficiency[2],
                3 * 0.34125 + 1 * 0.6825));
        // set operations from guild data
        setOperations(inputGuild.operationsArray);

        // Set first Planets as active initally
        darkSide.get(0).isActive = true;
        currentDsDepth = 0;
        mixed.get(0).isActive = true;
        currentMiDepth = 0;
        lightSide.get(0).isActive = true;
        currentLsDepth = 0;

        this.phaseGpBudget = inputGuild.assumedGpBudget;
        this.currentGuild = inputGuild;
        this.currentPhase = 1;
        this.isFinished = false;
    }

    public MapState cloneMapState() {
        MapState cloned = new MapState(this.currentGuild);

        // Copy simple fields
        cloned.currentDsDepth = this.currentDsDepth;
        cloned.currentMiDepth = this.currentMiDepth;
        cloned.currentLsDepth = this.currentLsDepth;
        cloned.mandaloreUnlocked = this.mandaloreUnlocked;
        cloned.zeffoUnlocked = this.zeffoUnlocked;
        cloned.phaseGpBudget = this.phaseGpBudget;
        cloned.currentPhase = this.currentPhase;
        cloned.isFinished = this.isFinished;

        // Deep copy the LinkedLists of Planets
        cloned.darkSide = clonePlanetList(this.darkSide);
        cloned.mixed = clonePlanetList(this.mixed);
        cloned.lightSide = clonePlanetList(this.lightSide);
        cloned.mandalore = clonePlanetList(this.mandalore);
        cloned.zeffo = clonePlanetList(this.zeffo);

        return cloned;
    }

    public int getPhase() {
        return this.currentPhase;
    }

    private LinkedList<Planet> clonePlanetList(LinkedList<Planet> originalPlanets) {
        LinkedList<Planet> clonedPlanet = new LinkedList<>();
        for (Planet p : originalPlanets) {
            clonedPlanet.add(p.clonePlanet());
        }
        return clonedPlanet;
    }

    public Planet[] getCurrentActivePlanets() {
        Planet[] activePlanets = new Planet[5];
        activePlanets[0] = darkSide.get(currentDsDepth);
        activePlanets[1] = mixed.get(currentMiDepth);
        activePlanets[2] = lightSide.get(currentLsDepth);
        if (mandaloreUnlocked) {
            activePlanets[3] = mandalore.get(0);
        } else {
            activePlanets[3] = null;
        }
        if (zeffoUnlocked) {
            activePlanets[4] = zeffo.get(0);
        } else {
            activePlanets[4] = null;
        }

        return activePlanets;
    }

    public void advanceDarkSide() {
        if (currentDsDepth < darkSide.size() - 1) {
            darkSide.get(currentDsDepth).isActive = false;
            currentDsDepth++;
            darkSide.get(currentDsDepth).isActive = true;
        }
    }

    public void advanceMixed() {
        if (currentMiDepth < mixed.size() - 1) {
            mixed.get(currentMiDepth).isActive = false;
            if (currentLsDepth == 1 && currentGuild.mandaloreReady) {
                unlockMandalore();
                advanceMandalore();
            }
            currentMiDepth++;
            mixed.get(currentMiDepth).isActive = true;
        }
    }

    public void advanceLightSide() {
        if (currentLsDepth < lightSide.size() - 1) {
            lightSide.get(currentLsDepth).isActive = false;
            if (currentLsDepth == 1 && currentGuild.zeffoReady) {
                unlockZeffo();
                advanceZeffo();
            }
            currentLsDepth++;
            lightSide.get(currentLsDepth).isActive = true;
        }
    }

    public void unlockMandalore() {
        mandaloreUnlocked = true;
    }

    public void advanceMandalore() {
        if (mandaloreUnlocked && !mandalore.get(0).isActive && mandalore.get(0).getCurrentStar() < 1) {
            System.out.println("Mandalore unlocked and set to active");
            System.out.println("Mandalore current star: " + mandalore.get(0).getStarResult());
            mandalore.get(0).isActive = true;
        } else if (!(mandaloreUnlocked && mandalore.get(0).isActive && mandalore.get(0).getCurrentStar() < 1)) {
            mandalore.get(0).isActive = false; // set mandalore inactive, as it has been completed
            mandaloreUnlocked = false; // set mandalore as locked again, as it can only be done once

        } // else {
          // mandalore.get(0).isActive = false; // set mandalore inactive, as it has been
          // completed
          // mandaloreUnlocked = false; // set mandalore as locked again, as it can only
          // be done once
          // }

    }

    public void unlockZeffo() {
        zeffoUnlocked = true;
    }

    public void advanceZeffo() {
        if (zeffoUnlocked && !zeffo.get(0).isActive && zeffo.get(0).getCurrentStar() < 1) {
            System.out.println("Zeffo unlocked and set to active");
            System.out.println("Zeffo current star: " + zeffo.get(0).getCurrentStar());
            zeffo.get(0).isActive = true;
        } else if (!(zeffoUnlocked && zeffo.get(0).isActive && zeffo.get(0).getCurrentStar() < 1)) {
            zeffo.get(0).isActive = false; // set zeffo inactive, as it has been completed
            zeffoUnlocked = false; // set zeffo as locked again, as it can only be done once
        } // else {
          // zeffo.get(0).isActive = false; // set zeffo inactive, as it has been
          // completed
          // zeffoUnlocked = false; // set zeffo as locked again, as it can only be done
          // once
          // }

    }

    public void advancePhase(double newBudget) {
        if (this.currentPhase < 6) {
            this.currentPhase++;
            this.phaseGpBudget = newBudget;
        } else {
            this.isFinished = true;
            this.phaseGpBudget = 0;
        }
    }

    public final void setOperations(int[][] operationsArray) {
        for (int i = 0; i < operationsArray.length; i++) {
            if (i != 6) {
                darkSide.get(i).addOperations(operationsArray[i][0]);
                mixed.get(i).addOperations(operationsArray[i][1]);
                lightSide.get(i).addOperations(operationsArray[i][2]);
            } else if (i == 6) {
                mandalore.get(0).addOperations(operationsArray[i][0]);
                zeffo.get(0).addOperations(operationsArray[i][1]);
            }
        }
    }

    public String getMapInfoString() {
        StringBuilder info = new StringBuilder();
        info.append("Dark Side Planets:\n");
        for (Planet p : darkSide) {
            info.append(p.getBasicInfoString()).append(", Operations Possible: ").append(p.numberOperationsPossible)
                    .append("\n");
            info.append("Avg Points per Star - 1: ").append(p.avgPointsPerStar[0]).append(", 2: ")
                    .append(p.avgPointsPerStar[2]).append(", 3: ").append(p.avgPointsPerStar[1]).append("\n");
        }
        info.append("Mixed Planets:\n");
        for (Planet p : mixed) {
            info.append(p.getBasicInfoString()).append(", Operations Possible: ").append(p.numberOperationsPossible)
                    .append("\n");
            info.append("Avg Points per Star - 1: ").append(p.avgPointsPerStar[0]).append(", 2: ")
                    .append(p.avgPointsPerStar[2]).append(", 3: ").append(p.avgPointsPerStar[1]).append("\n");
        }
        info.append("Light Side Planets:\n");
        for (Planet p : lightSide) {
            info.append(p.getBasicInfoString()).append(", Operations Possible: ").append(p.numberOperationsPossible)
                    .append("\n");
            info.append("Avg Points per Star - 1: ").append(p.avgPointsPerStar[0]).append(", 2: ")
                    .append(p.avgPointsPerStar[2]).append(", 3: ").append(p.avgPointsPerStar[1]).append("\n");
        }
        if (mandaloreUnlocked) {
            info.append("Mandalore Planet:\n");
            for (Planet p : mandalore) {
                info.append(p.getBasicInfoString()).append(", Operations Possible: ").append(p.numberOperationsPossible)
                        .append("\n");
                info.append("Avg Points per Star - 1: ").append(p.avgPointsPerStar[0]).append(", 2: ")
                        .append(p.avgPointsPerStar[2]).append(", 3: ").append(p.avgPointsPerStar[1]).append("\n");
            }
        }
        if (zeffoUnlocked) {
            info.append("Zeffo Planet:\n");
            for (Planet p : zeffo) {
                info.append(p.getBasicInfoString()).append(", Operations Possible: ").append(p.numberOperationsPossible)
                        .append("\n");
                info.append("Avg Points per Star - 1: ").append(p.avgPointsPerStar[0]).append(", 2: ")
                        .append(p.avgPointsPerStar[2]).append(", 3: ").append(p.avgPointsPerStar[1]).append("\n");
            }
        }
        return info.toString();
    }

    public String getMapState() {
        StringBuilder info = new StringBuilder();
        Planet[] activePlanets = getCurrentActivePlanets();
        info.append("MapState:\n");
        for (Planet p : activePlanets) {
            if (p != null) {
                info.append(p.name).append(" ").append(p.getCurrentPoints()).append("\n");
            }
        }
        return info.toString();
    }

    public String getMapStatusString() {
        StringBuilder info = new StringBuilder();
        Planet[] activePlanets = getCurrentActivePlanets();
        for (Planet p : activePlanets) {
            if (p != null) {
                info.append(p.name).append(" ").append(p.getStarResult()).append(" ").append(p.getCurrentPoints())
                        .append("\n");
            }
        }
        return info.toString();
    }

    // true -> identical, false -> different
    public boolean compareMapStates(MapState m1, MapState m2) {
        boolean identical = false;

        Planet[] activePlanets1 = m1.getCurrentActivePlanets();
        Planet[] activePlanets2 = m2.getCurrentActivePlanets();

        for (int i = 0; i < activePlanets1.length; i++) {
            if (activePlanets1[i] != null) {
                identical = activePlanets1[i].name.equals(activePlanets2[i].name);
                identical &= activePlanets1[i].getCurrentStar() == activePlanets2[i].getCurrentStar();
                identical &= activePlanets1[i].currentPoints == activePlanets2[i].currentPoints;
            }

        }

        return identical;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MapState other = (MapState) obj;
        return compareMapStates(this, other);
    }

    @Override
    public int hashCode() {
        Planet[] planets = this.getCurrentActivePlanets();

        int result = 17;

        for (int i = 0; i < 3; i++) {
            if (planets[i] != null) {
                result = 31 * result + Double.hashCode(planets[i].currentPoints);
                result = 31 * result + Objects.hashCode(planets[i].name);
                result = 31 * result + planets[i].getCurrentStar();
            }
        }

        if (planets[3] != null) {
            result = 31 * result + Double.hashCode(planets[3].currentPoints);
            result = 31 * result + Objects.hashCode(planets[3].name);
            result = 31 * result + planets[3].getCurrentStar();
        }

        if (planets[4] != null) {
            result = 31 * result + Double.hashCode(planets[4].currentPoints);
            result = 31 * result + Objects.hashCode(planets[4].name);
            result = 31 * result + planets[4].getCurrentStar();
        }

        return result;
    }

    public int[] getCurrentCheckpoints() {
        int[] checkpoints = new int[5];
        Planet[] activePlanets = getCurrentActivePlanets();
        for (int i = 0; i < activePlanets.length; i++) {
            if (activePlanets[i] != null) {
                checkpoints[i] = activePlanets[i].getCurrentStar();
            } else {
                checkpoints[i] = -9; // indicate no active planet in the slot
            }
        }
        return checkpoints;
    }

}
