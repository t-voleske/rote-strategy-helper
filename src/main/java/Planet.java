public class Planet {
    String name;
    int depth;
    int position;
    double neededPointsFull; // points needed in millions for 3 star on the Planet
    double neededPoints2Star; // points needed in millions for 2 star on the Planet
    double neededPoints1Star; // points needed in millions for 1 star on the Planet
    double fullPreloadPoints; // points needed in millions to reach full preload on the Planet
    double operationsPoints;
    double currentPoints;
    int numberOperationsPossible;
    public static final double[] OPERATIONS_WORTH = { 10, 11, 13.2, 18.48, 33.26, 86.49 }; // points worth in millions
                                                                                           // for each finished
    // operation on a Planet by depth
    double assumedMissionPoints;
    boolean isActive; // indicates that a Planet is currently open
    boolean isCompleted; // indicates that a Planet has been completed
    boolean operationsTriggered; // indicates that operations points have been used for the Planet
    double openMissionPoints;
    double[] avgPointsPerStar;
    double missionEfficiency;
    double totalMissionsWorth;

    public Planet(String name, int depth, int position, double neededPointsFull, double neededPoints2Star,
            double neededPoints1Star, double missionEfficiency, double totalMissionsWorth) {
        // basic Planet info
        this.name = name;
        this.depth = depth;
        this.position = position;
        this.neededPointsFull = round(neededPointsFull);
        this.neededPoints2Star = round(neededPoints2Star);
        this.neededPoints1Star = round(neededPoints1Star);
        this.fullPreloadPoints = round(neededPoints1Star) - 5; // default preload points is 5 million less than needed
                                                               // for 1 star
        this.isActive = false;
        this.isCompleted = false;
        this.currentPoints = 0;
        this.operationsTriggered = false;
        this.assumedMissionPoints = round(missionEfficiency * totalMissionsWorth);
        this.openMissionPoints = 0;
        this.avgPointsPerStar = calculateAveragePointsPerStar();
    }

    public final double round(double a) {
        double roundOff = (double) Math.round(a * 100) / 100;
        return roundOff;
    }

    public Planet clonePlanet() {
        Planet cloned = new Planet(
                this.name, this.depth, this.position,
                this.neededPointsFull, this.neededPoints2Star,
                this.neededPoints1Star, this.missionEfficiency, this.totalMissionsWorth);

        cloned.fullPreloadPoints = this.fullPreloadPoints;
        cloned.currentPoints = round(this.currentPoints);
        cloned.numberOperationsPossible = this.numberOperationsPossible;
        cloned.operationsPoints = this.operationsPoints;
        cloned.assumedMissionPoints = this.assumedMissionPoints;
        cloned.isActive = this.isActive;
        cloned.isCompleted = this.isCompleted;
        cloned.operationsTriggered = this.operationsTriggered;
        cloned.openMissionPoints = this.openMissionPoints;
        cloned.avgPointsPerStar = this.avgPointsPerStar.clone();

        return cloned;
    }

    public final double[] calculateAveragePointsPerStar() {
        double[] pointsPerStar = new double[3];
        pointsPerStar[0] = round(this.neededPoints1Star);
        pointsPerStar[1] = round(this.neededPoints2Star / 2);
        pointsPerStar[2] = round(this.neededPointsFull / 3);
        return pointsPerStar;
    }

    public void addOperations(int numberOperations) {
        this.numberOperationsPossible = numberOperations;
        this.operationsPoints = round(this.numberOperationsPossible * Planet.OPERATIONS_WORTH[this.depth]);
    }

    public double getOperationsPoints() {
        if (this.numberOperationsPossible == 0) {
            return 0;
        }
        return round(this.operationsPoints);
    }

    public double neededForNextCheckpoint(Planet p) {
        if (this.currentPoints < this.fullPreloadPoints) {
            return round(this.fullPreloadPoints - this.currentPoints);
        } else if (this.currentPoints < this.neededPoints1Star) {
            return round(this.neededPoints1Star - this.currentPoints);
        } else if (this.currentPoints < this.neededPoints2Star) {
            return round(this.neededPoints2Star - this.currentPoints);
        } else if (this.currentPoints < this.neededPointsFull) {
            return round(this.neededPointsFull - this.currentPoints);
        } else {
            return 0;
        }
    }

    public double neededForFullPreload() {
        if (this.currentPoints < this.fullPreloadPoints) {
            return round(this.fullPreloadPoints - this.currentPoints);
        } else {
            return 0;
        }
    }

    public double neededForNextStar(Planet p) {
        if (this.currentPoints < this.neededPoints1Star) {
            return round(this.neededPoints1Star - this.currentPoints);
        } else if (this.currentPoints < this.neededPoints2Star) {
            return round(this.neededPoints2Star - this.currentPoints);
        } else if (this.currentPoints < this.neededPointsFull) {
            return round(this.neededPointsFull - this.currentPoints);
        } else {
            return 0;
        }
    }

    public int getCurrentStar() {
        if (this.currentPoints >= this.neededPointsFull) {
            return 3;
        } else if (this.currentPoints >= this.neededPoints2Star) {
            return 2;
        } else if (this.currentPoints >= this.neededPoints1Star) {
            return 1;
        } else if (this.currentPoints >= this.fullPreloadPoints) {
            return 0;
        } else {
            return -1;
        }
    }

    public String getBasicInfoString() {
        return "Planet: " + this.name + ", Depth: " + this.depth + ", Position: " + this.position;
    }

    public String getPlanetShortString() {
        return "Planet: " + this.name;
    }

    public double addPoints(double pointsToAdd) {
        this.currentPoints = round(pointsToAdd + this.currentPoints);
        return this.currentPoints;
    }

    public void setActive() {
        this.isActive = true;
    }

    public void setCompleted() {
        this.isCompleted = true;
    }

    public double getCurrentPoints() {
        return round(this.currentPoints);
    }
}
