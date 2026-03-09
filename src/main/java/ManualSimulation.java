import java.util.ArrayList;
import java.util.Arrays;

public class ManualSimulation {

    int runState; // Indicates the current state of the run: 1 for fresh run, from 2-6 to pick the
                  // specific phase to start from
    GuildData currentGuild;
    TbRun activeRun;
    ArrayList<MapState> actionHistory = new ArrayList<>();
    int historyCursor = 0;
    PhaseSimulation pSim = new PhaseSimulation();
    boolean addMissionPoints = false;
    boolean printLogs = false;

    public final double round(double a) {
        double roundOff = (double) Math.round(a * 100) / 100;
        return roundOff;
    }

    public ManualSimulation(GuildData inputGuild, int runState) {
        this.currentGuild = inputGuild;
        this.runState = runState;
    }

    public void printHistoryLog(boolean visible) {
        if (!visible) {
            return;
        }
        System.out.println("--------------------\n");
        System.out.print("historyCursor" + historyCursor + "\n");
        System.out.print("actionHistory\n");
        for (MapState ms : actionHistory) {
            System.out.print(ms + "\n");
        }
    }

    public void setAddMissionPoints(boolean mp) {
        this.addMissionPoints = mp;
    }

    public void setUpTbRun(TbRun inputRun) {
        if (runState == 1) {
            TbRun freshRun = new TbRun(this.currentGuild);
            System.out.println(this.addMissionPoints);
            if (this.addMissionPoints) {
                pSim.phasePreparation(freshRun);
            }
            this.activeRun = freshRun;
            this.actionHistory.add(this.activeRun.roteRun[this.activeRun.currentPhase - 1].cloneMapState());
        } else if (runState > 1 && runState <= 6) {
            this.activeRun = pSim.prepareTbRun(inputRun, runState);
            this.actionHistory.add(this.activeRun.roteRun[this.activeRun.currentPhase - 1].cloneMapState());
        } else {
            throw new IllegalArgumentException(
                    "Invalid run state: " + runState + ". Please enter a number between 1 and 6.");
        }
    }

    // Call when action is performed, to save TbRun snapshot in actionHistory
    private void addActionToHistory() {
        this.actionHistory.add(this.activeRun.roteRun[this.activeRun.currentPhase - 1].cloneMapState());
        this.historyCursor++;
        printHistoryLog(printLogs);
    }

    public void undoAction() {
        System.out.print("\n -------------------------- \n");
        System.out.print("TbRun after Undo\n");
        System.out.print(this.activeRun + "\n");
        System.out.print(this.activeRun.roteRun[this.activeRun.currentPhase - 1].getMapStatusString());
        if (historyCursor > 0) {
            this.historyCursor--;
            this.activeRun.roteRun[this.activeRun.currentPhase - 1] = this.actionHistory.get(this.historyCursor)
                    .cloneMapState();
            this.actionHistory.removeLast();
            System.out.print("\n -------------------------- \n");
            System.out.print("TbRun after Undo\n");
            System.out.print(this.activeRun + "\n");
            System.out.print(this.activeRun.roteRun[this.activeRun.currentPhase - 1].getMapStatusString());
        } else {
            throw new IndexOutOfBoundsException("There are no actions to undo.");
        }

    }

    public void resetPhase() {
        System.out.print("\n -------------------------- \n");
        System.out.print("TbRun before Reset\n");
        System.out.print(this.activeRun + "\n");
        System.out.print(this.activeRun.roteRun[this.activeRun.currentPhase - 1].getMapStatusString());
        MapState initialState = this.actionHistory.getFirst().cloneMapState();
        this.actionHistory = new ArrayList<>();
        this.actionHistory.add(initialState);
        this.activeRun.roteRun[this.activeRun.currentPhase - 1] = initialState.cloneMapState();
        this.historyCursor = 0;

        System.out.print("\n -------------------------- \n");
        System.out.print("TbRun after Reset\n");
        System.out.print(this.activeRun + "\n");
        System.out.print(this.activeRun.roteRun[this.activeRun.currentPhase - 1].getMapStatusString());
        printHistoryLog(printLogs);
    }

    public void advancePhase() {
        this.activeRun = pSim.advancePhase(this.activeRun);
        if (addMissionPoints) {
            pSim.phasePreparation(this.activeRun);
        }
        this.historyCursor = 0;
        this.actionHistory = new ArrayList<>();
        this.actionHistory.add(this.activeRun.roteRun[this.activeRun.currentPhase - 1].cloneMapState());

    }

    public String getPhaseStatus() {
        if (this.activeRun == null) {
            return "No active run. Please set up a TbRun first.";
        }
        MapState currentPhaseState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        StringBuilder output = new StringBuilder();
        output.append("\n").append("==============================");
        output.append("\n- Phase ").append(currentPhaseState.currentPhase).append("\n");
        output.append(currentPhaseState.getMapStatusString());
        output.append("----------------------");
        return output.toString();
    }

    public double attemptCheckpoint(int targetCheckpoint, int planetIndex) {
        // Read current state to validate before cloning
        MapState currentState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet currentPlanet = currentState.getCurrentActivePlanets()[planetIndex];
        if (currentPlanet == null) {
            throw new IllegalArgumentException("Planet at index " + planetIndex + " is not active.");
        }
        double neededPoints = Double.MAX_VALUE;
        boolean triggerOperations = false;
        double operationsWorth = 0;
        switch (targetCheckpoint) {
            case 1 -> {
                neededPoints = Math.max(0, currentPlanet.neededPoints1Star - currentPlanet.getCurrentPoints());
                triggerOperations = currentPlanet.getCurrentStar() < 1;
            }
            case 2 -> {
                neededPoints = Math.max(0, currentPlanet.neededPoints2Star - currentPlanet.getCurrentPoints());
                triggerOperations = currentPlanet.getCurrentStar() < 1;
            }
            case 3 -> {
                neededPoints = Math.max(0, currentPlanet.neededPointsFull - currentPlanet.getCurrentPoints());
                triggerOperations = currentPlanet.getCurrentStar() < 1;
            }
            default -> neededPoints = currentPlanet.neededForFullPreload();
        }
        if (triggerOperations && targetCheckpoint >= 1) {
            operationsWorth = currentPlanet.getOperationsPoints();
        }
        if (currentState.phaseGpBudget < (neededPoints - operationsWorth)) {
            throw new BudgetExceededException("Operation is not possible, as it exceeds the remaining budget!");
        }

        // Clone before mutating so history entries stay intact
        this.activeRun = this.activeRun.cloneRun();
        MapState targetState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet targetPlanet = targetState.getCurrentActivePlanets()[planetIndex];
        targetPlanet.addPoints(neededPoints);
        targetState.phaseGpBudget = targetState.phaseGpBudget - (neededPoints - operationsWorth);
        addActionToHistory();
        return operationsWorth;
    }

    public String[] returnCurrentState() {
        MapState currentMS = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        String[] returnStringArray = new String[13];
        // 0-4: planet states: null -> inactive, stars -> -1 to 3
        // 5-9: current points / total needed for next checkpoint by planet
        // 10: open phase budget
        // 11: no more checkpoints reachable
        // 12: no more preloads possible
        Planet[] planetArray = currentMS.getCurrentActivePlanets();
        int activePlanets = 0;

        // Fill planet related entries 0-9, if planets are active
        for (int i = 0; i < planetArray.length; i++) {
            if (planetArray[i] == null) {
                continue;
            }
            activePlanets++;
            returnStringArray[i] = String.valueOf(planetArray[i].getCurrentWorth()); // Use worth here, because of
                                                                                     // Zeffo/Mandalore
            double totalCheckpoint = round(
                    planetArray[i].neededForNextCheckpoint() + planetArray[i].getCurrentPoints());
            returnStringArray[i + 5] = String.valueOf(planetArray[i].getCurrentPoints()) + "/"
                    + String.valueOf(totalCheckpoint);
        }

        returnStringArray[10] = String.valueOf(currentMS.phaseGpBudget); // 10: open phase budget
        returnStringArray[11] = "1"; // Default: assume no checkpoints reachable
        returnStringArray[12] = "0"; // Default value: 0 -> false
        for (int i = 0; i < planetArray.length; i++) {
            if (planetArray[i] == null) {
                continue;
            }
            int[] reachable = getReachableCheckpoints(i);
            for (int r : reachable) {
                if (r == 1) {
                    returnStringArray[11] = "0"; // At least one checkpoint is still reachable
                    break;
                }
            }
            if (returnStringArray[11].equals("0")) {
                break;
            }
        }

        String[] subArrStars = Arrays.copyOfRange(returnStringArray, 0, 5); // subset of the array for planet Stars
        int countB = (int) Arrays.stream(subArrStars).filter(s -> {
            if (s == null)
                return false;
            try {
                return Integer.parseInt(s) >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }).count();
        if (countB == activePlanets) {
            returnStringArray[12] = "1"; // No more preloads are reachable: 1 -> true
        }

        return returnStringArray;
    }

    // ------------------------------------------------------
    // GUI helper methods
    // ------------------------------------------------------
    public String[] getActivePlanetNames() {
        MapState currentMS = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        String[] returnResult = new String[5];
        Planet[] planetArray = currentMS.getCurrentActivePlanets();

        for (int i = 0; i < planetArray.length; i++) {
            if (planetArray[i] == null) {
                returnResult[i] = null;
                continue;
            }
            returnResult[i] = planetArray[i].name;
        }

        return returnResult;
    }

    public int getCurrentPhase() {
        return this.activeRun.currentPhase;
    }

    public int[] getReachableCheckpoints(int planetIndex) {
        int[] checkpointArray = new int[4]; // Stars that can be reached: [0, 1, 2, 3]. 0 or 1 -> true or false
        Double currentBudget = activeRun.roteRun[this.activeRun.currentPhase - 1].phaseGpBudget;
        Planet currentPlanet = activeRun.roteRun[this.activeRun.currentPhase - 1]
                .getCurrentActivePlanets()[planetIndex];

        int currentStars = currentPlanet.getCurrentStar();

        switch (currentStars) {
            case -1 -> {
                checkpointArray = new int[] { 1, 1, 1, 1 };
            }
            case 0 -> {
                checkpointArray = new int[] { 0, 1, 1, 1 };
            }
            case 1 -> {
                checkpointArray = new int[] { 0, 0, 1, 1 };
            }
            case 2 -> {
                checkpointArray = new int[] { 0, 0, 0, 1 };
            }
            case 3 -> {
                checkpointArray = new int[] { 0, 0, 0, 0 };
                return checkpointArray;
            }
        }

        for (int i = 3; i >= 0; i--) {
            if (checkpointArray[i] == 0) {
                return checkpointArray;
            }
            checkpointArray[i] = currentPlanet.starReachable(i, currentBudget);
        }

        return checkpointArray;
    }

    public int getTotalStars() {
        return this.activeRun.updateCurrentStars();
    }

    public String getRunSummary() {
        this.activeRun.updateCurrentStars();
        return this.activeRun.getResultString(0);
    }

    public double addCustomPoints(int planetIndex, double points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive.");
        }

        MapState currentState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet currentPlanet = currentState.getCurrentActivePlanets()[planetIndex];
        if (currentPlanet == null) {
            throw new IllegalArgumentException("Planet at index " + planetIndex + " is not active.");
        }

        // Cap points at what the planet can still absorb
        double maxAbsorbable = currentPlanet.neededPointsFull - currentPlanet.getCurrentPoints();
        points = Math.min(points, maxAbsorbable);

        // Check if operations will trigger (crossing 1-star threshold)
        double operationsWorth = 0;
        if (!currentPlanet.operationsTriggered
                && (currentPlanet.getCurrentPoints() + points) >= currentPlanet.neededPoints1Star) {
            operationsWorth = currentPlanet.getOperationsPoints();
        }

        double effectiveCost = points - operationsWorth;
        if (currentState.phaseGpBudget < effectiveCost) {
            throw new BudgetExceededException("Operation is not possible, as it exceeds the remaining budget!");
        }

        // Clone before mutating so history entries stay intact
        this.activeRun = this.activeRun.cloneRun();
        MapState targetState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet targetPlanet = targetState.getCurrentActivePlanets()[planetIndex];
        targetPlanet.addPoints(points);
        targetState.phaseGpBudget -= effectiveCost;
        addActionToHistory();
        return effectiveCost;
    }

    public void addLeftoverPoints(int planetIndex) {
        MapState currentState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet currentPlanet = currentState.getCurrentActivePlanets()[planetIndex];
        if (currentPlanet == null) {
            throw new IllegalArgumentException("Planet at index " + planetIndex + " is not active.");
        }

        // Clone before mutating so history entries stay intact
        this.activeRun = this.activeRun.cloneRun();
        MapState targetState = this.activeRun.roteRun[this.activeRun.currentPhase - 1];
        Planet targetPlanet = targetState.getCurrentActivePlanets()[planetIndex];
        double pointsToAdd = Math.min(targetState.phaseGpBudget, targetPlanet.neededForFullPreload());
        targetPlanet.addPoints(pointsToAdd);
        targetState.phaseGpBudget -= pointsToAdd;
        addActionToHistory();
    }

    // Reset history, so there is no carry-over from one manual simulation to
    // another
    public void resetHistory() {
        this.actionHistory = new ArrayList<>();
        this.historyCursor = 0;
    }
}
