# RotE Strategy Helper

A Java-based simulation tool for **Rise of the Empire** (RotE) Territory Battles in *Star Wars: Galaxy of Heroes*. It calculates a decent allocation strategy for your guild across all 6 phases, to give you an overview of how many stars you can expect to reach. 

---

## Overview
Planning a RotE run is complex and time consuming, even when you are experienced and know your guild's numbers by heart. Especially when starting out with RotE, there is a mountain of decisions to make, which can have varying implications for the stars your guild is able to reach in the end. What planets should you work on first and how many of them can your guild get done per phase? How do the possible platoons on specific planets change the path you should pick and what about missions and undeployed GP per phase? 
I wrote this program to make it possible for anyone to quickly get a decent estimate of what is possible, given they know some key numbers about their guild. 

*Getting your guild's numbers exactly right is tricky, especially regarding points gained through missions. From my experience, the results output by the helper usually are within one star of a well optimized RotE plan.*

### Key Features

- **Phase-wise simulation:** Simulates the entire TB run by phases, keeping track of all important key figures of all relevant planets.
- **Branching strategy mechanic:** Generates only desireable star allocation combinations (see [Why only chose the "desireable outcomes" each phase?](#why-only-chose-the-desireable-outcomes-each-phase)) per phase and discards impossible branches. Drastically recudes memory usage and needed calculation time.
- **Multi-threaded execution:** Parallelize phase simulations across available CPU cores, for faster execution.
- **Configurable guild parameters:** Input your guild's key numbers, as well as possible bonus planets, to get as close to in-game behaviour as possible.
- **Swing GUI:** Desktop interface for entering guild data, saving/loading guild configurations, running simulations and reviewing the top results.

---

### Planned Features

- **Manual simulation mode:** Start with a clean RotE run with the constraints of your guild's data. Manually decide on where to allocate points, finish platoons and points to add from missions.
- **Manual takeover of a run:** Initiate a manual simulation with any phase of an automatically generated run as the start point.
- **Mission points additions:** Allowing mission points from being based on planet depth or being based on individual planets, to allow for more control or keep simplicity.
- **In depth control of mission points:** Add a menu to the GUI that allows for exact control of mission point behaviour, like deciding on depth or planet based calculation, multipliers based on finished platoonc, etc.

## How It Works

1. **Guild data** (GP, members, GP/mission efficiency, operations) is entered through the GUI.
2. A RotE run is initialized with fresh starting planets in phase 1.
3. For each phase, a set of desireable outcomes (e.g. 3-Star Dark Side, 3-Star Mixed, 0-Star Light Side) are tested. If they fit the guild's constraints, a new run snapshot (TbRun object) is created, on which all desireable outcomes can be tested again in the next phase.
4. Each unique phase outcome produces a new `MapState` object, which is used to save the current state of the phase. Each snapshot has a history of the phase outcomes from all 6 phases.
5. After all 6 phases are done, runs are ranked by total worth and the top results are displayed in the output of the GUI. Fully completing Zeffo and Mandalore is valued the same as other 3-Star planets for both, even though they are only worth 1 star each, to account for the Kyros they grant every guild member. 

---
## Why only chose the "desireable outcomes" each phase?

I tried a few different approaches:
1. A greedy short term approach, which always allocated to the planet that needed the least points to the next star. 
2. A general algorithm that worked for all possibilities based on that proved unrealistic, so i switched to generating a list of all possible outcomes, checking if they were possible and then creating a branch for each of those. This approach used up to 8GB of memory and took far too long to compute.
3. Filtering all generated outcomes for different criteria lead to a list of the most optimal outcomes to check each phase, which lead to part one of the current strategy, only checking against a selected desireable outcomes each phase. This drastically cut down on branches created each phase. 
4. Finally, switching to the greedy approach from 1. for the last phase cut down on branching even more, reducing memory usage by over 99% from GBs needed earlier and making compute time barely noticeable. 

### These are the outcomes checked for phases 1-5:
1. All open planets 3 Star
2. One 0 Star + rest of open planets 3 Star\
    2.1. One -1 Star + rest of open planets 3 Star
3. One 2 Star + rest of open planets 3 Star
4. Two 0 Star + rest of open planets 3 Star\
    4.1. One -1 Star + One 0 Star + rest of open planets 3 Star\
    4.2. Two -1 Star + rest of open planets 3 Star
5. One 0 Star + One 2 Star + rest of open planets 3 Star\
    5.1. with one -1 Star + One 2 Star + rest of open planets 3 Star
6. All open planets 0 star\
    6.1. all open planets -1 Star\
    6.2. with one 0 Star + rest of open planets -1 Star
7. One 2 Star + rest of open planets 0 star\
    7.1. with one 2 Star + one 0 star + rest of open planets -1 star
8. One 1 Star + rest of open planets 3 Star
9. One 1 Star + rest of open planets 0 Star

Planets under 1 star are represented by a -1, as long as they haven't reached a full preload. When they reach [Points needed for 1 star] - [5m points], they are considered at full preload and represented by 0 star.

---

## Project Structure

```
src/
└── main/java/
    ├── StrategyCalculator.java   # Swing GUI: Main entry point
    ├── SimulationController.java # Control the simulation with thread pooling
    ├── PhaseSimulation.java      # Phase-level branching TbRun objets, using PlanningActions, maintaining a collection of phase states
    ├── PlanningActions.java      # Outcome generation & GP allocation logic
    ├── ManualSimulation.java     # WIP: Providing tools to allow manual control of a run (Planned )
    ├── MapState.java             # Saving states of the individual planets for a specific phase
    ├── Planet.java               # Individual planet data (thresholds, points, operations), as well as planet specific methods
    ├── TbRun.java                # Snapshot of a run at a specific phase
    └── GuildData.java            # Guild-level input parameters
```

Supporting files:

```
guild_save.txt              # Persisted guild configuration (only present after saving a guild)
pom.xml                     # Maven build configuration
```

---
## Getting Started (using the provided executable for Windows)
Download the .zip file from [output](output) folder and unpack it. Run the .exe file in the folder. 
You will most likely get a warning pop-up from windows defender, where you need to press on "More info" to get to option to "Run anyway".

---
## Getting Started (building yourself)

### Prerequisites

- **Java 17+** (records and modern switch expressions)
- **Maven** (for building)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/t-voleske/rote-strategy-helper.git
cd rote-strategy-helper

# Build with Maven
mvn clean package

# Run the GUI
java -jar target/RotE-Strategy-Helper-1.0-SNAPSHOT.jar
```

Or run directly from source:

```bash
mvn compile exec:java -Dexec.mainClass="StrategyHelper"
```

---

## Usage

1. Launch the application.
2. Either press "Load saved Guild", if you have already saved a guild's data, or fill in your guild's information:
   - **Total Guild Points** (in millions)
   - **Active Guild Members**
   - **Assumed GP Efficiency:** Percentage of total GP your guild realistically deploys
   - **Operations per depth:** Comma-separated values for each planet tier (DS, Mixed, LS)
   - **Mission efficiency:** (optional) Toggle on and set per-depth percentages. Give it a conservative estimate. Better to low-ball than to overshoot here.
   - **Zeffo / Mandalore readiness:** Check if your guild can unlock these bonus planets
3. (Optional) Press "Save guild data" to save your guild's information to a text file. You can load this again at a later time.
4. Click **Start Run** to begin the simulation.
5. Browse the top 10 results using the **Previous / Next** buttons.

---

## Configuration Reference

| Parameter | Description | Example |
|---|---|---|
| Total Guild Points | Guild's total GP in millions | `350.0` |
| Active Members | Number of participating members | `50` |
| GP Efficiency | Fraction of GP actually deployed (0–1) | `0.85` |
| Mission Efficiency | Per-depth mission completion rate (0–1) | `[0.5, 0.6, 0.7, 0.8, 0.9, 1.0]` |
| Operations Array | Operations possible per depth per alignment | `[[10,10,10], ..., [2,2]]` |
| Zeffo Ready | Guild can unlock Zeffo bonus planet | `true` / `false` |
| Mandalore Ready | Guild can unlock Mandalore bonus planet | `true` / `false` |

---

## Architecture

```
StrategyCalculator (GUI)
    └── SimulationController
            └── PhaseSimulation (×N threads)
                    ├── PlanningActions - generates star combinations
                    ├── MapState - Phase snapshot 
                    └── TbRun - Tracking status of a full run at a specific phase
```

The simulation uses a **breadth-first branching** approach: Each phase branches out into all desireable star outcomes. If necessary, those are then pruned to the top 50,000 candidates (by cumulative worth) before advancing to the next phase. Completed `MapState` objects are collected in a `HashSet` to drastically reduce memory usage from duplicates among all different branches.

---
## Acknowledgements
- The [interactive ROTE TB map](https://genskaar.github.io/tb_empire/html/main.html) was instrumental in building the simulation environment.

---
## License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

---

*This project is not affiliated with or endorsed by Electronic Arts Inc. or Lucasfilm Ltd. All trademarks are property of their respective owners.*