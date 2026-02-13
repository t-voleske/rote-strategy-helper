public class ManualSimulation {

    int runState; // Indicates the current state of the run: 1 for fresh run, from 2-6 to pick the
                  // specific phase to start from
    GuildData currentGuild;
    TbRun activeRun;

    public ManualSimulation(GuildData inputGuild, int runState) {
        this.currentGuild = inputGuild;
        this.runState = runState;
    }

    public void setUpTbRun(TbRun inputRun) {
        if (runState == 1) {
            TbRun freshRun = new TbRun(this.currentGuild);
            this.activeRun = freshRun;
        } else if (runState > 1 && runState <= 6) {
            this.activeRun = PhaseSimulation.prepareTbRun(inputRun, runState);
        } else {
            throw new IllegalArgumentException(
                    "Invalid run state: " + runState + ". Please enter a number between 0 and 6.");
        }
    }

    
}
