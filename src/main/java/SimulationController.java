import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimulationController {
    public ArrayList<TbRun> runResults = new ArrayList<>();
    public TbRun bestRun;
    public GuildData currenGuildData;
    PhaseSimulation[] phaseContainer = new PhaseSimulation[6];
    int maxRunsPerPhase = 50000;
    private ExecutorService executor;
    private int numThreads;
    ArrayList<MapState> mapStateCollection = new ArrayList<>();

    public SimulationController(GuildData inputGuild) {
        this.currenGuildData = inputGuild;
    }

    public ArrayList<TbRun> executeSimulation() {

        numThreads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(numThreads);
        runResults = runFullSimulation(this.currenGuildData);
        System.out.print("\n");
        System.out.print(runResults.size());
        outputTopXRuns(runResults, 10);

        executor.shutdown();
        return runResults;
    }

    public final ArrayList<TbRun> runFullSimulation(GuildData guildToSim) {
        PhaseSimulation simulation = new PhaseSimulation();
        phaseContainer[0] = simulation;
        TbRun initialRun = new TbRun(guildToSim);
        simulation.phaseSim(initialRun);
        ArrayList<TbRun> toProcess = new ArrayList<>(simulation.toDo);

        for (int i = 1; i < 6; i++) {
            System.out.println("Phase " + i);
            final int phaseIndex = i;
            phaseContainer[i] = new PhaseSimulation();

            // Process runs in parallel
            List<Future<PhaseSimulation>> futures = new ArrayList<>();

            for (TbRun r : toProcess) {
                Future<PhaseSimulation> future = executor.submit(() -> {
                    PhaseSimulation sim = new PhaseSimulation();
                    sim.phaseSim(r);
                    return sim;
                });
                futures.add(future);
            }

            // Collect results from all threads
            for (Future<PhaseSimulation> future : futures) {
                try {
                    PhaseSimulation sim = future.get();
                    phaseContainer[phaseIndex].result.addAll(sim.result);
                    phaseContainer[phaseIndex].toDo.addAll(sim.toDo);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            toProcess = phaseContainer[i].toDo;
            toProcess.sort(Comparator.comparingInt((TbRun run) -> run.updateCurrentStars()).reversed());

            if (toProcess.size() > maxRunsPerPhase) {
                toProcess.subList(maxRunsPerPhase, toProcess.size()).clear();
            }

            if (i < 5) {
                phaseContainer[i - 1] = null;
            }
        }

        return phaseContainer[5].result;
    }

    public final void outputTopXRuns(ArrayList<TbRun> inputArrayList, int x) {
        inputArrayList.sort(Comparator.comparingInt((TbRun run) -> run.updateCurrentStars()).reversed());
        inputArrayList.subList(x, inputArrayList.size()).clear();
        for (int i = 0; i < inputArrayList.size(); i++) {
            System.out.println("\n==================================================");
            System.out.println("Top " + (i + 1) + " - " + inputArrayList.get(i).starCounter + " :");
            System.out.println("==================================================");
            for (MapState m : inputArrayList.get(i).roteRun) {
                System.out.print("- Phase " + m.currentPhase + "\n");
                System.out.print(m.getMapStatusString());
                System.out.println("----------------------");
            }
        }
    }

    public final String outputTopXRunString(ArrayList<TbRun> inputArrayList, int x) {
        StringBuilder output = new StringBuilder();
        inputArrayList.sort(Comparator.comparingInt((TbRun run) -> run.updateCurrentStars()).reversed());
        inputArrayList.subList(x, inputArrayList.size()).clear();
        for (int i = 0; i < inputArrayList.size(); i++) {
            output.append("\n==================================================");
            output.append("Top " + (i + 1) + " - " + inputArrayList.get(i).starCounter + " :");
            output.append("==================================================");
            for (MapState m : inputArrayList.get(i).roteRun) {
                output.append("- Phase " + m.currentPhase + "\n");
                output.append(m.getMapStatusString());
                output.append("----------------------");
            }
        }
        return output.toString();
    }
}