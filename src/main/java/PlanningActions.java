import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public final class PlanningActions {

    public record planetRecord(int state, int stars, double neededPoints, Planet p) {
    }

    public record mapStateRecord(boolean operationPossible, MapState m) {
    }

    public record starCombinationRecord(int dsStars, int miStars, int lsStars, int mandaloreStars, int zeffoStars,
            double neededPoints) {
        public int totalStars() {
            int total = 0;
            if (dsStars != -1)
                total += dsStars;
            if (miStars != -1)
                total += miStars;
            if (lsStars != -1)
                total += lsStars;
            if (mandaloreStars == 3)
                total += 1;
            if (zeffoStars == 3)
                total += 1;
            return total;
        }

        public int totalWorth() {
            int total = 0;
            if (dsStars != -1)
                total += dsStars;
            if (miStars != -1)
                total += miStars;
            if (lsStars != -1)
                total += lsStars;
            if (mandaloreStars == 3)
                total += 1;
            if (zeffoStars == 3)
                total += 3;
            return total;
        }
    }

    public record mapStateCombinationRecord(starCombinationRecord starCombo, MapState mapState) {
        @Override
        public String toString() {
            return ("Stars: " + this.totalStars() + " | Worth: " + this.totalWorth() + " | " + dsStars() + " "
                    + miStars() + " " + lsStars() + " | "
                    + neededPoints() + " P: " + getCurrentPhase());
        }

        public int dsStars() {
            return starCombo.dsStars;
        }

        public int miStars() {
            return starCombo.miStars;
        }

        public int lsStars() {
            return starCombo.lsStars;
        }

        public int zeffoStars() {
            return starCombo.zeffoStars;
        }

        public int mandaloreStars() {
            return starCombo.mandaloreStars;
        }

        public double neededPoints() {
            return starCombo.neededPoints;
        }

        public int totalStars() {
            return starCombo.totalStars();
        }

        public int totalWorth() {
            return starCombo.totalWorth();
        }

        public MapState getMapState() {
            return mapState;
        }

        public int getCurrentPhase() {
            return mapState.getPhase();
        }
    }

    public final double round(double a) {
        double roundOff = (double) Math.round(a * 100) / 100;
        return roundOff;
    }

    public planetRecord needed3Star(Planet p) {
        if (p.getCurrentPoints() >= p.neededPointsFull) {
            return new planetRecord(3, 3, 0, p);
        }

        // System.out.println("Operations points for " + p.name + ": " +
        // p.operationsPoints);
        // System.out.println("Needed points for " + p.name + ": " + p.neededPointsFull
        // + " - " + p.getCurrentPoints() + " - " + p.operationsPoints + " = " +
        // (p.neededPointsFull - p.getCurrentPoints() - p.operationsPoints));
        return new planetRecord(3, 3, round(p.neededPointsFull - p.getCurrentPoints() - p.operationsPoints), p);

    }

    public planetRecord needed2Star(Planet p) {
        if (p.getCurrentPoints() >= p.neededPoints2Star) {
            return new planetRecord(2, 2, 0, p);
        }

        return new planetRecord(2, 2, round(p.neededPoints2Star - p.getCurrentPoints() - p.operationsPoints), p);

    }

    public planetRecord needed1Star(Planet p) {
        if (p.getCurrentPoints() >= p.neededPoints1Star) {
            return new planetRecord(1, 1, 0, p);
        }

        return new planetRecord(1, 1, round(p.neededPoints1Star - p.getCurrentPoints() - p.operationsPoints), p);

    }

    public planetRecord neededFullPreload(Planet p) {
        if (p.getCurrentPoints() >= p.fullPreloadPoints) {
            return new planetRecord(0, 0, 0, p);
        }
        return new planetRecord(0, 0, round(p.fullPreloadPoints - p.getCurrentPoints()), p);
    }

    public planetRecord emptyPlanet(Planet p) {
        return new planetRecord(-1, 0, 0, p);
    }

    /*
     * Best possible combinations. -1: No allocation, 0: Full preload
     * 1. All open planets 3 Star
     * 2. One 0 Star + rest of open planets 3 Star
     * (2.1. with -1 Star instead of 0 Star)
     * 3. One 2 Star + rest of open planets 3 Star
     * 4. Two 0 Star + rest of open planets 3 Star
     * (4.1. with one 0 Star and one -1 Star instead of two 0 Stars)
     * (4.2. with two -1 Stars instead of two 0 Stars)
     * 5. One 0 Star + One 2 Star + rest of open planets 3 Star
     * (5.1. with one -1 Star + One 2 Star + rest of open planets 3 Star)
     * 6. All open planets 0 star
     * (6.1. with all open planets -1 Star)
     * (6.2. with one 0 Star + rest of open planets -1 Star)
     * (6.3. with one -1 Star + rest of open planets 0 Star)
     * 7. One 2 Star + rest of open planets -1 star
     * (7.1. with one 2 Star + one 0 star + rest of open planets -1 star)
     * (7.2. with one 2 Star + two 0 star + rest of open planets -1 star)
     * 8. One 1 Star + rest of open planets 3 Star
     */

    List<starCombinationRecord> buildOptimalStarCombinations(Planet[] activePlanets, double availablePoints,
            int phase) {
        List<starCombinationRecord> optimalSetCandidates = new ArrayList<>();

        // 1. All open planets 3 Star
        int[] threeStarArray = fillArray(activePlanets.length, 3);
        for (int i = 0; i < activePlanets.length; i++) {
            if (activePlanets[i] == null) {
                threeStarArray[i] = -1;
            }
        }
        optimalSetCandidates.add(starComboGenerator(threeStarArray, activePlanets));

        // 2. One 0 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneZeroArray = fillArray(activePlanets.length, 3);
            if (activePlanets[i] != null) {
                oneZeroArray[i] = 0;
            } else if (activePlanets[i] == null) {
                oneZeroArray[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneZeroArray, activePlanets));
        }

        // 2.1. One -1 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] x = fillArray(activePlanets.length, 3);
            if (activePlanets[i] != null) {
                x[i] = -1;
            } else if (activePlanets[i] == null) {
                x[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(x, activePlanets));
        }

        // 3. One 2 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneTwoArray = fillArray(activePlanets.length, 3);
            if (activePlanets[i] != null) {
                oneTwoArray[i] = 2;
            } else if (activePlanets[i] == null) {
                oneTwoArray[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneTwoArray, activePlanets));
        }

        // 4. Two 0 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] twoZeroOneThree = fillArray(activePlanets.length, 3);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    twoZeroOneThree[i] = 0;
                } else if (activePlanets[i] == null) {
                    twoZeroOneThree[i] = -1;
                }
                if (activePlanets[j] != null) {
                    twoZeroOneThree[j] = 0;
                } else if (activePlanets[j] == null) {
                    twoZeroOneThree[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(twoZeroOneThree, activePlanets));
            }
        }

        // 4.1. One -1 Star + One 0 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] y = fillArray(activePlanets.length, 3);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    y[i] = 0;
                } else if (activePlanets[i] == null) {
                    y[i] = -1;
                }
                if (activePlanets[j] != null) {
                    y[j] = -1;
                } else if (activePlanets[j] == null) {
                    y[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(y, activePlanets));
            }
        }

        // 4.2. Two -1 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] z = fillArray(activePlanets.length, 3);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    z[i] = -1;
                } else if (activePlanets[i] == null) {
                    z[i] = -1;
                }
                if (activePlanets[j] != null) {
                    z[j] = -1;
                } else if (activePlanets[j] == null) {
                    z[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(z, activePlanets));
            }
        }

        // 5. One 0 Star + One 2 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneZeroOneTwoThrees = fillArray(activePlanets.length, 3);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    oneZeroOneTwoThrees[i] = 0;
                } else if (activePlanets[i] == null) {
                    oneZeroOneTwoThrees[i] = -1;
                }
                if (activePlanets[j] != null) {
                    oneZeroOneTwoThrees[j] = 2;
                } else if (activePlanets[j] == null) {
                    oneZeroOneTwoThrees[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(oneZeroOneTwoThrees, activePlanets));
            }
        }

        // 5.1. with one -1 Star + One 2 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] xy = fillArray(activePlanets.length, 3);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    xy[i] = -1;
                } else if (activePlanets[i] == null) {
                    xy[i] = -1;
                }
                if (activePlanets[j] != null) {
                    xy[j] = 2;
                } else if (activePlanets[j] == null) {
                    xy[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(xy, activePlanets));
            }
        }

        // 6. All open planets 0 star
        int[] threeZerosArray = fillArray(activePlanets.length, 0);
        for (int i = 0; i < activePlanets.length; i++) {
            if (activePlanets[i] == null) {
                threeZerosArray[i] = -1;
            }
        }
        optimalSetCandidates.add(starComboGenerator(threeZerosArray, activePlanets));

        // 6.1. all open planets -1 Star)
        int[] threeMinusOneArray = fillArray(activePlanets.length, -1);
        optimalSetCandidates.add(starComboGenerator(threeMinusOneArray, activePlanets));

        // 6.2. with one 0 Star + rest of open planets -1 Star)
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneZeroArrayX = fillArray(activePlanets.length, -1);
            if (activePlanets[i] != null) {
                oneZeroArrayX[i] = 0;
            } else if (activePlanets[i] == null) {
                oneZeroArrayX[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneZeroArrayX, activePlanets));
        }

        // 7. One 2 Star + rest of open planets 0 star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneTwoArray = fillArray(activePlanets.length, 0);
            if (activePlanets[i] != null) {
                oneTwoArray[i] = 2;
            } else if (activePlanets[i] == null) {
                oneTwoArray[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneTwoArray, activePlanets));
        }

        // 7.1. with one 2 Star + one 0 star + rest of open planets -1 star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] xy = fillArray(activePlanets.length, -1);
            for (int j = i + 1; j < activePlanets.length; j++) {
                if (activePlanets[i] != null) {
                    xy[i] = 2;
                } else if (activePlanets[i] == null) {
                    xy[i] = -1;
                }
                if (activePlanets[j] != null) {
                    xy[j] = 0;
                } else if (activePlanets[j] == null) {
                    xy[j] = -1;
                }
                optimalSetCandidates.add(starComboGenerator(xy, activePlanets));
            }
        }

        // 8. One 1 Star + rest of open planets 3 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneOneThreesArray = fillArray(activePlanets.length, 3);
            if (activePlanets[i] != null) {
                oneOneThreesArray[i] = 1;
            } else if (activePlanets[i] == null) {
                oneOneThreesArray[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneOneThreesArray, activePlanets));
        }

        // 9. One 1 Star + rest of open planets 0 Star
        for (int i = 0; i < activePlanets.length; i++) {
            int[] oneOneZerosArray = fillArray(activePlanets.length, 0);
            if (activePlanets[i] != null) {
                oneOneZerosArray[i] = 1;
            } else if (activePlanets[i] == null) {
                oneOneZerosArray[i] = -1;
            }
            optimalSetCandidates.add(starComboGenerator(oneOneZerosArray, activePlanets));
        }

        List<starCombinationRecord> optimalRecords = new ArrayList<>();

        for (starCombinationRecord candidate : optimalSetCandidates) {
            int[] starsArray = { candidate.dsStars(), candidate.miStars(), candidate.lsStars(),
                    candidate.zeffoStars(), candidate.mandaloreStars() };

            int countOne = 0;

            for (int s : starsArray) {

                if (s == 1)
                    countOne++;

            }

            if (candidate.neededPoints > availablePoints) {
                continue;
            }
            if (candidate.neededPoints() < (availablePoints - 100)) {
                continue;
            }
            if (phase == 1 && countOne > 0) {
                continue;
            }

            optimalRecords.add(candidate);
        }

        List<starCombinationRecord> sortList = new ArrayList<>(new HashSet<>(optimalRecords));

        if (sortList.isEmpty()) {
            // System.err.println("WARNING: No valid star combinations found for phase " +
            // phase +
            // " with budget " + availablePoints + ". Reducing filtering.");
            for (starCombinationRecord candidate : optimalSetCandidates) {

                if (candidate.neededPoints > availablePoints) {
                    continue;
                }

                optimalRecords.add(candidate);
            }
            sortList = new ArrayList<>(new HashSet<>(optimalRecords));
            if (sortList.isEmpty()) {
                System.err.println(
                        "CRITICAL: No valid candidates generated at all. Returning -1 -1 -1 -1 -1 starCombination.");
                int[] allMinusOneArray = fillArray(activePlanets.length, -1);
                sortList.add(starComboGenerator(allMinusOneArray, activePlanets));
                return sortList;
            }
            sortList.sort(Comparator.comparingInt(PlanningActions.starCombinationRecord::totalWorth) // Changed to
                                                                                                     // totalWorth
                    .reversed().thenComparingDouble(PlanningActions.starCombinationRecord::neededPoints));
        } else {
            sortList.sort(Comparator.comparingInt(PlanningActions.starCombinationRecord::totalWorth) // Changed to
                                                                                                     // totalWorth
                    .reversed().thenComparingDouble(PlanningActions.starCombinationRecord::neededPoints));
        }

        int maxWorth = sortList.get(0).totalWorth();

        List<starCombinationRecord> finalList = new ArrayList<>();
        for (starCombinationRecord rec : sortList) {
            if (rec.totalWorth() < maxWorth - 2) {
                continue;
            }
            finalList.add(rec);

            /*
             * System.out.println("\nOptimal candidate - Stars: " + rec.totalStars() +
             * " | DS: " + rec.dsStars()
             * + ", MI: " + rec.miStars() + ", LS: " + rec.lsStars() + ", Ma: " +
             * rec.mandaloreStars()
             * + ", Z: " + rec.zeffoStars() + " | Needed Points: " + rec.neededPoints());
             */
        }
        // System.exit(0);
        return finalList;
    }

    // Helper method to generate list of planetRecords for a given starsArray
    // configuration
    starCombinationRecord starComboGenerator(int[] starsArray, Planet[] activePlanets) {
        double totalNeededPoints = 0;
        for (int i = 0; i < starsArray.length; i++) {
            if (activePlanets[i] != null) {
                switch (starsArray[i]) {
                    case 3 -> totalNeededPoints += needed3Star(activePlanets[i]).neededPoints();
                    case 2 -> totalNeededPoints += needed2Star(activePlanets[i]).neededPoints();
                    case 1 -> totalNeededPoints += needed1Star(activePlanets[i]).neededPoints();
                    case 0 -> totalNeededPoints += neededFullPreload(activePlanets[i]).neededPoints();
                    case -1 -> totalNeededPoints += 0;
                }
            }
        }
        return new starCombinationRecord(
                starsArray[0], starsArray[1], starsArray[2],
                activePlanets[3] != null ? starsArray[3] : 0,
                activePlanets[4] != null ? starsArray[4] : 0,
                round(totalNeededPoints));

    }

    // Helper method to prefill arrays for combination generation
    private int[] fillArray(int size, int value) {
        int[] arr = new int[size];
        Arrays.fill(arr, value);
        return arr;
    }

    public void printStarCombinations(List<starCombinationRecord> combinations) {
        if (combinations.isEmpty()) {
            // System.out.println("No combinations to print!");
            return;
        }

        for (starCombinationRecord record : combinations) {
            System.out.println("TOTAL: " + record.totalStars() + " || DS Stars: " + record.dsStars() + ", MI Stars: "
                    + record.miStars() + ", LS Stars: " + record.lsStars()
                    + ", Ma Stars: " + record.mandaloreStars() + ", Z Stars: " + record.zeffoStars()
                    + ", Needed Points: " + record.neededPoints());
        }

    }

    private static final double EPSILON = 1e-9;

    public MapState buildMapStateFromCombination(starCombinationRecord combination, MapState currentMapState) {
        MapState newMapState = currentMapState.cloneMapState();
        Planet[] activePlanets = newMapState.getCurrentActivePlanets();
        double availablePoints = currentMapState.phaseGpBudget;

        int[] starsArray = { combination.dsStars(), combination.miStars(), combination.lsStars(),
                combination.mandaloreStars(), combination.zeffoStars() };
        for (int i = 0; i < starsArray.length; i++) {
            if (starsArray[i] == -1) {
                starsArray[i] = 0;
            }
        }

        for (int i = 0; i < activePlanets.length; i++) {
            Planet p = activePlanets[i];
            if (p != null) {
                int stars = starsArray[i];

                // Add open mission points first if they exist
                if (p.openMissionPoints > 0) {
                    p.addPoints(p.openMissionPoints);
                    p.openMissionPoints = 0; // Clear after using
                }

                switch (stars) {
                    case 1 -> {
                        double pointsToAdd = needed1Star(p).neededPoints + p.operationsPoints;
                        availablePoints -= needed1Star(p).neededPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;

                    }
                    case 2 -> {
                        double pointsToAdd = needed2Star(p).neededPoints + p.operationsPoints;
                        availablePoints -= needed2Star(p).neededPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;

                    }
                    case 3 -> {
                        double pointsToAdd = needed3Star(p).neededPoints + p.operationsPoints;
                        availablePoints -= needed3Star(p).neededPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;

                    }
                    case 0 -> {
                        /*
                         * double preloadBudget = currentMapState.phaseGpBudget -
                         * combination.neededPoints();
                         * double preloadPoints = Math.min(preloadBudget, p.neededForFullPreload());
                         * if (preloadPoints > 0) {
                         * p.addPoints(preloadPoints);
                         * }
                         */
                    }
                }
            }
        }
        // Phases 1-5:
        // Handle leftover points: Preload planet with lowest needed to full preload,
        // then next lowest, ...
        switch (newMapState.getPhase()) {
            case 1, 2, 3, 4, 5 -> {
                while (availablePoints > 0) {
                    double closestToPreload = Double.MAX_VALUE;
                    int closestIndex = -1;
                    for (int i = 0; i < activePlanets.length; i++) {
                        Planet p = activePlanets[i];
                        if (p != null && p.getCurrentPoints() < p.fullPreloadPoints
                                && p.neededForFullPreload() < closestToPreload) {
                            closestToPreload = Math.min(availablePoints, p.neededForFullPreload());
                            closestIndex = i;
                        }
                    }
                    if (closestIndex == -1) {
                        availablePoints = 0; // No more planets to preload
                        break;
                    } else if (closestToPreload > 0 && closestToPreload < Double.MAX_VALUE) {
                        activePlanets[closestIndex].addPoints(closestToPreload);
                        availablePoints -= closestToPreload;
                        closestToPreload = Double.MAX_VALUE; // Reset for next iteration
                        closestIndex = -1; // Reset for next iteration
                    }

                }
            }

            case 6 -> {
                while (availablePoints > 0) {
                    double closestToNextStar = Double.MAX_VALUE;
                    int closestStarIndex = -1;
                    for (int i = 0; i < activePlanets.length; i++) {
                        Planet p = activePlanets[i];
                        if (p != null && p.neededForNextStar() < closestToNextStar
                                && p.neededForNextStar() < availablePoints && p.neededForNextStar() > 0.0) {
                            closestToNextStar = Math.min(availablePoints, p.neededForNextStar());
                            closestStarIndex = i;
                        }
                    }
                    if (closestStarIndex == -1) {
                        availablePoints = 0;
                        break;
                    } else if (closestToNextStar > 0 && closestToNextStar < Double.MAX_VALUE) {
                        activePlanets[closestStarIndex].addPoints(closestToNextStar);
                        availablePoints -= closestToNextStar;
                    }

                }
            }
        }

        return newMapState;
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

    public boolean[] compareTbRuns(TbRun tr1, TbRun tr2) {
        boolean[] phases = new boolean[6];
        for (int i = 0; i < phases.length; i++) {
            phases[i] = false;
            if (compareMapStates(tr1.roteRun[i], tr2.roteRun[i]))
                phases[i] = true;

        }
        return phases;
    }

}
