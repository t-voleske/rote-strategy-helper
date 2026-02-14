public class ManualSimulation {

    int runState; // Indicates the current state of the run: 1 for fresh run, from 2-6 to pick the
                  // specific phase to start from
    GuildData currentGuild;
    TbRun activeRun;
    PhaseSimulation pSim = new PhaseSimulation();

    public ManualSimulation(GuildData inputGuild, int runState) {
        this.currentGuild = inputGuild;
        this.runState = runState;
    }

    public void setUpTbRun(TbRun inputRun) {
        if (runState == 1) {
            TbRun freshRun = new TbRun(this.currentGuild);
            pSim.phasePreparation(freshRun);
            this.activeRun = freshRun;
        } else if (runState > 1 && runState <= 6) {
            this.activeRun = pSim.prepareTbRun(inputRun, runState);
        } else {
            throw new IllegalArgumentException(
                    "Invalid run state: " + runState + ". Please enter a number between 0 and 6.");
        }
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

    public void attemptCheckpoint(int checkpoint){
        //attempt to push to a specified checkpoint, eg. 0-3 stars 
    }
}
