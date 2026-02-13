
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PhaseSimulation {

    ArrayList<TbRun> result = new ArrayList<>();
    ArrayList<TbRun> toDo = new ArrayList<>();
    Set<MapState> stateSet = new HashSet<>();

    public final double round(double a) {
        double roundOff = (double) Math.round(a * 100) / 100;
        return roundOff;
    }

    public MapState getMapState(MapState mapStateToCheck) {

        Optional<MapState> found = stateSet.stream()
                .filter(state -> state.equals(mapStateToCheck))
                .findFirst();
        if (found.isPresent()) {
            MapState retrieved = found.get();
            return retrieved;
        }
        return mapStateToCheck;
    }

    public MapState internMapState(MapState candidate) {
        Optional<MapState> existing = stateSet.stream()
                .filter(state -> state.equals(candidate))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get(); // Return the canonical instance
        } else {
            stateSet.add(candidate); // Add to pool and return it
            return candidate;
        }
    }

    // Phase change mechanic, including TbRun, MapState & Planets objects
    // Addition: Exchange MapStates from completed phases with references to a
    // MapState collection to save heap space
    public TbRun advancePhase(TbRun runToAdvance) {

        boolean exists = stateSet.contains(runToAdvance.roteRun[runToAdvance.currentPhase - 1]);
        if (exists) {
            runToAdvance.roteRun[runToAdvance.currentPhase - 1] = getMapState(
                    runToAdvance.roteRun[runToAdvance.currentPhase - 1]);
        } else {
            stateSet.add(runToAdvance.roteRun[runToAdvance.currentPhase - 1]);
        }

        // Get all the relevant objects
        TbRun tmpRun = runToAdvance.cloneRun();
        MapState tmpState = tmpRun.roteRun[tmpRun.currentPhase - 1].cloneMapState();
        Planet[] workingPlanets = tmpState.getCurrentActivePlanets();
        if (workingPlanets[0] != null && workingPlanets[0].getCurrentStar() > 0) {
            /*
             * System.out.println("Planet " + workingPlanets[0].name + " has " +
             * workingPlanets[0].getCurrentStar()
             * + " stars, advancing Dark Side depth.");
             */
            tmpState.advanceDarkSide();
        }
        if (workingPlanets[1] != null && workingPlanets[1].getCurrentStar() > 0) {
            /*
             * System.out.println("Planet " + workingPlanets[1].name + " has " +
             * workingPlanets[1].getCurrentStar()
             * + " stars, advancing Mixed depth.");
             */
            tmpState.advanceMixed();
        }
        if (workingPlanets[2] != null && workingPlanets[2].getCurrentStar() > 0) {
            /*
             * System.out.println("Planet " + workingPlanets[2].name + " has " +
             * workingPlanets[2].getCurrentStar()
             * + " stars, advancing Light Side depth.");
             */
            tmpState.advanceLightSide();
        }
        if (tmpState.mandaloreUnlocked || tmpState.mandalore.get(0).isActive
                && tmpState.mandalore.get(0).getCurrentStar() > 0) {
            tmpState.advanceMandalore();
        }
        if (tmpState.zeffoUnlocked || tmpState.zeffo.get(0).isActive
                && tmpState.zeffo.get(0).getCurrentStar() > 0) {
            tmpState.advanceZeffo();
        }

        // System.out.println("\ncurrentPhase: " + tmpRun.currentPhase);
        tmpState.advancePhase(tmpRun.currentGuild.assumedGpBudget);
        if (tmpRun.currentPhase < 6) {
            tmpRun.roteRun[tmpRun.currentPhase] = tmpState;
        }
        tmpRun.currentPhase = tmpState.currentPhase;
        return tmpRun;
    }

    public static TbRun prepareTbRun(TbRun runToAdvance, int targetPhase) {

        // Get all the relevant objects
        TbRun tmpRun = runToAdvance.cloneRun();
        MapState tmpState = tmpRun.roteRun[targetPhase - 2].cloneMapState();
        Planet[] workingPlanets = tmpState.getCurrentActivePlanets();
        if (workingPlanets[0] != null && workingPlanets[0].getCurrentStar() > 0) {

            tmpState.advanceDarkSide();
        }
        if (workingPlanets[1] != null && workingPlanets[1].getCurrentStar() > 0) {

            tmpState.advanceMixed();
        }
        if (workingPlanets[2] != null && workingPlanets[2].getCurrentStar() > 0) {

            tmpState.advanceLightSide();
        }
        if (tmpState.mandaloreUnlocked || tmpState.mandalore.get(0).isActive
                && tmpState.mandalore.get(0).getCurrentStar() > 0) {
            tmpState.advanceMandalore();
        }
        if (tmpState.zeffoUnlocked || tmpState.zeffo.get(0).isActive
                && tmpState.zeffo.get(0).getCurrentStar() > 0) {
            tmpState.advanceZeffo();
        }

        tmpState.advancePhase(tmpRun.currentGuild.assumedGpBudget);
        if (targetPhase <= 6) {
            tmpRun.roteRun[targetPhase - 1] = tmpState;
        }
        tmpRun.currentPhase = targetPhase;
        return tmpRun;
    }

    // Simulate adding mission points to all active planets
    public TbRun addMissionPoints(TbRun missionRun) {
        Planet[] workingPlanets = missionRun.roteRun[missionRun.currentPhase - 1].getCurrentActivePlanets();
        for (Planet p : workingPlanets) {
            if (p == null) {
                continue;
            }
            double totalMissionPoints = round(p.assumedMissionPoints * missionRun.currentGuild.activeGuildMembers);
            double overflowPoints;
            // Case 1: Planet is active & adding all mission points does not break a preload
            if (p.getCurrentPoints() + totalMissionPoints < p.neededPoints1Star) {
                p.addPoints(totalMissionPoints);
            }
            /*
             * Case 2: Planet is active & planet is under 1 star & adding totalMissionPoints
             * would push planet over preload.
             * Add up to just under 1 star, then save overflow points into openMissionPoints
             * for later in the phase.
             */
            else if (p.getCurrentPoints() + totalMissionPoints >= p.neededPoints1Star
                    && p.getCurrentPoints() < p.neededPoints1Star) {
                overflowPoints = round(p.getCurrentPoints() + totalMissionPoints - p.neededPoints1Star + 1);
                double under1Star = round(totalMissionPoints - overflowPoints); // adding this to currentPoints will be
                                                                                // 1m under 1 star
                p.addPoints(under1Star);
                p.openMissionPoints = overflowPoints;
            }
            /*
             * Case 3: Adding 1m mission points would push over 1 star. Add all mission
             * points to openMissionPoints.
             */
            else if (p.getCurrentPoints() + 1 >= p.neededPoints1Star
                    && totalMissionPoints >= 1) {
                p.openMissionPoints = totalMissionPoints;
            }
        }

        return missionRun;
    }

    // Add mission points, etc.
    public TbRun phasePreparation(TbRun prepRun) {
        addMissionPoints(prepRun);
        return prepRun;
    }

    // Take prepared TbRun & branch it into all possibe phase outcomes
    public void phaseSim(TbRun branchRun) {
        ArrayList<TbRun> resultsList = new ArrayList<>();
        ArrayList<TbRun> toDoList = new ArrayList<>();

        if (branchRun.currentPhase == 1) {
            System.out.println("------------------------------------------------");
            System.out.println("\n");
            System.out.println("------------------------------------------------");
            System.out.println("New run simulation started.");
            branchRun.evaluateActivePlanets(branchRun.roteRun[branchRun.currentPhase - 1]);
        }
        // prepare run to be branched
        phasePreparation(branchRun);
        int workingPhase = branchRun.currentPhase;
        MapState workingState = branchRun.roteRun[workingPhase - 1];
        // System.out.println("\n");
        // branchRun.evaluateActivePlanets(workingState);

        PlanningActions planner = new PlanningActions();
        // Get list of possible phase outcomes

        List<PlanningActions.starCombinationRecord> allOutcomes = planner
                .buildOptimalStarCombinations(workingState.getCurrentActivePlanets(), workingState.phaseGpBudget,
                        workingState.currentPhase);

        // Build TbRun object for each item in prunedOutcomes
        for (PlanningActions.starCombinationRecord rec : allOutcomes) {
            TbRun workingRun = branchRun.cloneRun();
            // This is already built on a cloned MapState object
            MapState outcomeState = planner.buildMapStateFromCombination(rec, workingState);
            outcomeState = internMapState(outcomeState);
            workingRun.roteRun[workingPhase - 1] = outcomeState;
            resultsList.add(workingRun);
            TbRun nextPhaseRun = workingRun.cloneRun();
            nextPhaseRun = advancePhase(nextPhaseRun);
            // System.out.println("After Advance");
            // for (MapState m : nextPhaseRun.roteRun) {
            // System.out.println(m);
            // }

            toDoList.add(nextPhaseRun);
        }
        if (!resultsList.isEmpty() && resultsList.get(0).currentPhase == 6) {
            this.result.addAll(resultsList);
        }

        this.toDo.addAll(toDoList);

    }

    public void outputResultsList() {
        if (!this.result.isEmpty()) {
            System.out.println("------------------------------------------------");
            System.out.println("Phase End:");
            System.out.println("------------------------------------------------");
            for (TbRun tR : this.result) {
                System.out.println(tR.evaluateActivePlanets(tR.roteRun[tR.currentPhase - 1]));
            }
        }

        System.out.println("------------------------------------------------");
        System.out.println("To Do:");
        System.out.println("------------------------------------------------");
        for (TbRun tR : this.toDo) {
            System.out.println(tR.evaluateActivePlanets(tR.roteRun[tR.currentPhase -
                    1]));
        }

    }
}
