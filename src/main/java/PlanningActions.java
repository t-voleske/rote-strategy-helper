import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PlanningActions {

    public record planetRecord(int state, int stars, double neededPoints, Planet p) {
    }

    public record mapStateRecord(boolean operationPossible, MapState m) {
    }

    public record starCombinationRecord(int dsStars, int miStars, int lsStars, int mandaloreStars, int zeffoStars,
            double neededPoints) {
        public int totalStars() {
            return dsStars + miStars + lsStars + mandaloreStars + zeffoStars;
        }
    }

    public record mapStateCombinationRecord(starCombinationRecord starCombo, MapState mapState) {
        @Override
        public String toString() {
            return ("Stars: " + this.totalStars() + " | " + dsStars() + " " + miStars() + " " + lsStars() + " | "
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

    public List<planetRecord> allNeeded(Planet p) {
        List<planetRecord> neededList = new ArrayList<>();
        neededList.add(new planetRecord(-1, 0, 0, p));
        neededList.add(new planetRecord(0, 0, round(p.fullPreloadPoints - p.getCurrentPoints()), p));
        neededList
                .add(new planetRecord(1, 1, round(p.neededPoints1Star - p.getCurrentPoints() - p.operationsPoints), p));
        neededList
                .add(new planetRecord(2, 2, round(p.neededPoints2Star - p.getCurrentPoints() - p.operationsPoints), p));
        neededList
                .add(new planetRecord(3, 3, round(p.neededPointsFull - p.getCurrentPoints() - p.operationsPoints), p));
        // prune array, so that only needed planetRecords remain
        // Sizes indicate: 0: 3 Star, 1: 2 Star, 3: 1 Star, 4: under preload
        // neededList.removeIf(record -> record.neededPoints() == 0);
        return neededList;
    }

    public List<planetRecord> checkPossibleStarsIndividual(Planet p, double availablePoints) {
        List<planetRecord> possibleList = new ArrayList<>();
        List<planetRecord> neededList = allNeeded(p);
        for (planetRecord record : neededList) {
            if (record.neededPoints() <= availablePoints) {
                possibleList.add(record);
            }
        }
        return possibleList;
    }

    public List<List<planetRecord>> checkPossibleStarsSet(Planet[] activePlanets, double availablePoints) {
        List<List<planetRecord>> possibleSets = new ArrayList<>();
        for (Planet planet : activePlanets) {
            if (planet != null) {
                possibleSets.add(checkPossibleStarsIndividual(planet, availablePoints));
            } else {
                possibleSets.add(new ArrayList<>());
            }
        }

        /*
         * System.out.println("PossibleSets contents:");
         * for (int i = 0; i < possibleSets.size(); i++) {
         * System.out.println("Planet " + i + ":");
         * for (planetRecord rec : possibleSets.get(i)) {
         * System.out.println("  " + rec.stars() + " stars, needs " +
         * rec.neededPoints());
         * }
         * }
         */
        return possibleSets;
    }

    public List<starCombinationRecord> tryPossibleMapStates(MapState m, double availablePoints, int phase) {
        List<starCombinationRecord> possibleStarCombinations = new ArrayList<>();
        Planet[] activePlanets = m.getCurrentActivePlanets();
        List<List<planetRecord>> possibleSets = checkPossibleStarsSet(activePlanets, availablePoints);
        int count = ((int) Arrays.stream(activePlanets)
                .filter(Objects::nonNull)
                .count());

        switch (count) {
            case 3 -> { // 3 core planets are active
                for (planetRecord dsRecord : possibleSets.get(0)) {
                    for (planetRecord miRecord : possibleSets.get(1)) {
                        for (planetRecord lsRecord : possibleSets.get(2)) {
                            try {
                                double[] allocationArray = new double[] { dsRecord.neededPoints(),
                                        miRecord.neededPoints(),
                                        lsRecord.neededPoints() };
                                validateAllocation(allocationArray, availablePoints);

                                double totalAllocations = round(
                                        dsRecord.neededPoints() + miRecord.neededPoints() + lsRecord.neededPoints());
                                starCombinationRecord currentCombinationRecord = new starCombinationRecord(
                                        dsRecord.stars(), miRecord.stars(), lsRecord.stars(), 0, 0, totalAllocations);
                                possibleStarCombinations.add(currentCombinationRecord);

                            } catch (InvalidAllocationException e) {

                            }

                        }
                    }
                }

            }

            case 4 -> { // either Zeffo or Mandalore are active
                if (activePlanets[3] != null) {
                    for (planetRecord dsRecord : possibleSets.get(0)) {
                        for (planetRecord miRecord : possibleSets.get(1)) {
                            for (planetRecord lsRecord : possibleSets.get(2)) {
                                for (planetRecord mandaloreRecord : possibleSets.get(3)) {
                                    try {
                                        double[] allocationArray = new double[] { dsRecord.neededPoints(),
                                                miRecord.neededPoints(),
                                                lsRecord.neededPoints(), mandaloreRecord.neededPoints() };
                                        validateAllocation(allocationArray, availablePoints);

                                        double totalAllocations = round(
                                                dsRecord.neededPoints() + miRecord.neededPoints()
                                                        + lsRecord.neededPoints() + mandaloreRecord.neededPoints());
                                        starCombinationRecord currentCombinationRecord = new starCombinationRecord(
                                                dsRecord.stars(), miRecord.stars(), lsRecord.stars(),
                                                mandaloreRecord.stars(), 0,
                                                totalAllocations);
                                        possibleStarCombinations.add(currentCombinationRecord);

                                    } catch (InvalidAllocationException e) {

                                    }
                                }

                            }
                        }
                    }
                } else {
                    for (planetRecord dsRecord : possibleSets.get(0)) {
                        for (planetRecord miRecord : possibleSets.get(1)) {
                            for (planetRecord lsRecord : possibleSets.get(2)) {
                                for (planetRecord zeffoRecord : possibleSets.get(4)) {
                                    try {
                                        double[] allocationArray = new double[] { dsRecord.neededPoints(),
                                                miRecord.neededPoints(),
                                                lsRecord.neededPoints(), zeffoRecord.neededPoints() };
                                        validateAllocation(allocationArray, availablePoints);

                                        double totalAllocations = round(
                                                dsRecord.neededPoints() + miRecord.neededPoints()
                                                        + lsRecord.neededPoints() + zeffoRecord.neededPoints());
                                        starCombinationRecord currentCombinationRecord = new starCombinationRecord(
                                                dsRecord.stars(), miRecord.stars(), lsRecord.stars(),
                                                0, zeffoRecord.stars(),
                                                totalAllocations);
                                        possibleStarCombinations.add(currentCombinationRecord);

                                    } catch (InvalidAllocationException e) {

                                    }
                                }

                            }
                        }
                    }
                }
            }

            case 5 -> { // Zeffo & Mandalore are active

                for (planetRecord dsRecord : possibleSets.get(0)) {
                    for (planetRecord miRecord : possibleSets.get(1)) {
                        for (planetRecord lsRecord : possibleSets.get(2)) {
                            for (planetRecord mandaloreRecord : possibleSets.get(3)) {
                                for (planetRecord zeffoRecord : possibleSets.get(4)) {
                                    try {
                                        double[] allocationArray = new double[] { dsRecord.neededPoints(),
                                                miRecord.neededPoints(),
                                                lsRecord.neededPoints(), mandaloreRecord.neededPoints(),
                                                zeffoRecord.neededPoints() };
                                        validateAllocation(allocationArray, availablePoints);

                                        double totalAllocations = round(
                                                dsRecord.neededPoints() + miRecord.neededPoints()
                                                        + lsRecord.neededPoints() + mandaloreRecord.neededPoints()
                                                        + zeffoRecord.neededPoints());
                                        starCombinationRecord currentCombinationRecord = new starCombinationRecord(
                                                dsRecord.stars(), miRecord.stars(), lsRecord.stars(),
                                                mandaloreRecord.stars(), zeffoRecord.stars(),
                                                totalAllocations);
                                        possibleStarCombinations.add(currentCombinationRecord);

                                    } catch (InvalidAllocationException e) {

                                    }
                                }
                            }

                        }
                    }
                }
            }

        }

        possibleStarCombinations.sort(
                Comparator.comparing(
                        starCombinationRecord::totalStars).reversed()
                        .thenComparing(starCombinationRecord::neededPoints));
        return possibleStarCombinations;
    }

    void validateAllocation(double[] allocations, double budget) throws InvalidAllocationException {
        double total = 0;
        budget = round(budget);
        for (double allocation : allocations) {
            if (allocation < 0) {
                throw new InvalidAllocationException("Negative allocations not allowed!");
            }
            total += round(allocation);
        }
        if (total > budget) {
            throw new InvalidAllocationException("Allocation exceeds budget: " + total + " > " + budget);
        }
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

    public List<starCombinationRecord> pruneStarCombinations(List<starCombinationRecord> combinations) {
        combinations.sort(Comparator.comparingInt(starCombinationRecord::totalStars).reversed());
        List<starCombinationRecord> prunedList = new ArrayList<>();
        if (combinations.isEmpty()) {
            return prunedList;
        }
        int maxStars = combinations.get(0).totalStars();
        boolean zeroRecordAdded = false;
        for (starCombinationRecord record : combinations) {
            int[] starsArray = { record.dsStars(), record.miStars(), record.lsStars(), record.zeffoStars(),
                    record.mandaloreStars() };
            int countTwo = 0;
            int countOne = 0;
            int underPreload = 0;
            for (int s : starsArray) {
                if (s == 2)
                    countTwo++;
                if (s == 1)
                    countOne++;
                if (s <= 0)
                    underPreload++;
            }
            // Keep only combinations that are at least max total stars - 2
            /*
             * if (countTwo > 0 && countOne > 0 || countTwo > 1 || countOne > 1
             * || record.totalStars() < maxStars - 3) {
             * continue;
             * }
             */

            if (countTwo > 1 && countOne > 1 || countTwo > 2 || countOne > 1 || record.totalStars() < maxStars - 4
                    || zeroRecordAdded == true) {
                continue;
            } else if (underPreload > 2 && record.zeffoStars() != 0 && record.mandaloreStars() != 0
                    || underPreload > 3 && record.zeffoStars() != 0 || underPreload > 3 && record.mandaloreStars() != 0
                    || underPreload > 4) {
                prunedList.add(record);
                zeroRecordAdded = true;
            }

            prunedList.add(record);
        }
        // System.out.println("Size before prune: " + combinations.size());
        if (combinations.size() > 20 && prunedList.size() > 12) {
            prunedList = prunedList.subList(0, 12);
        } else if (combinations.size() > 10 && prunedList.size() > 6) {
            prunedList = prunedList.subList(0, 6);
        }
        // System.out.println("Size after prune: " + prunedList.size());
        // System.out.println("Stars: " + prunedList.get(0).totalStars());
        return prunedList;
    }

    private static final double EPSILON = 1e-9;

    public MapState buildMapStateFromCombination(starCombinationRecord combination, MapState currentMapState) {
        MapState newMapState = currentMapState.cloneMapState();
        Planet[] activePlanets = newMapState.getCurrentActivePlanets();

        int[] starsArray = { combination.dsStars(), combination.miStars(), combination.lsStars(),
                combination.mandaloreStars(), combination.zeffoStars() };
        // System.out.print("-------\nCombination: " + combination.toString() + "\n");
        for (int i = 0; i < activePlanets.length; i++) {
            Planet p = activePlanets[i];
            if (p != null) {
                int stars = starsArray[i];
                // System.out.print("-------\nPlanet: " + p.name + " | stars: " + stars + "\n");
                switch (stars) {
                    case 1 -> {
                        double pointsToAdd = needed1Star(p).neededPoints + p.operationsPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;
                        // System.out.print("Case 1 triggered\n");
                    }
                    case 2 -> {
                        double pointsToAdd = needed2Star(p).neededPoints + p.operationsPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;
                        // System.out.print("Case 2 triggered\n");
                    }
                    case 3 -> {
                        double pointsToAdd = needed3Star(p).neededPoints + p.operationsPoints;
                        pointsToAdd = Math.ceil(pointsToAdd - EPSILON);
                        p.addPoints(pointsToAdd);
                        p.operationsTriggered = true;
                        // System.out.print("Case 3 triggered\n");
                    }
                    case 0 -> {
                        // Preload case: Need to check how many points are available before adding
                        double preloadBudget = currentMapState.currentGuild.assumedGpBudget
                                - combination.neededPoints();
                        double preloadPoints = Math.min(preloadBudget, p.neededForFullPreload());
                        p.addPoints(preloadPoints);
                        // System.out.print("Case 0 triggered\n");
                    }
                }
            }
        }
        // System.out.print(newMapState.getMapState());
        // System.out.print(newMapState.toString() + "\n");
        return newMapState;
    }

    public mapStateCombinationRecord[] convertToMapStateCombinationRecords(List<starCombinationRecord> starCombinations,
            MapState inputMapState) {
        mapStateCombinationRecord[] recordsArray = new mapStateCombinationRecord[starCombinations.size()];
        MapState clonedMapState = inputMapState.cloneMapState();
        for (int i = 0; i < starCombinations.size(); i++) {
            recordsArray[i] = new mapStateCombinationRecord(starCombinations.get(i),
                    buildMapStateFromCombination(starCombinations.get(i), clonedMapState));
        }
        return recordsArray;
    }

    // Filter big records for now, so only the prime candidate record is returned
    // for development purposes
    public mapStateCombinationRecord[] getOnlyPrimeCandidates(mapStateCombinationRecord[] inputRecords) {
        return Arrays.stream(inputRecords)
                .filter(rec -> rec.dsStars() != 1 && rec.miStars() != 1 && rec.lsStars() != 1 && rec.zeffoStars() != 1
                        && rec.mandaloreStars() != 1)
                .toArray(mapStateCombinationRecord[]::new);
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

class InvalidAllocationException extends Exception {
    public InvalidAllocationException(String message) {
        super(message);
    }
}