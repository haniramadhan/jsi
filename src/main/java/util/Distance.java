package util;

public class Distance {
    public static final int DTW = 1;
    public static final int FRECHET = 0;
    public static final int LCSS = 2;
    public static final int EDR = 3;

    public static int currentDistance = FRECHET;
    public static double eps = 0.003;
    public static int spaceConstraint = 1;

    public static void setEps(double eps_){
        eps = eps_;
    }

    public static void setParameters(double eps_, int space){
        setEps(eps_);
        setSpaceConstraint(space);
    }
    public static void setSpaceConstraint(int space_){
        spaceConstraint = space_;
    }

    public static void setCurrentDistance(int distanceType){
        currentDistance = distanceType;
    }

    public static String getDistanceStr(){
        switch (currentDistance){
            case DTW:
                return "dtw";
            case LCSS:
                return "lcss";
            case EDR:
                return "edr";
            default:
                return "frechet";
            //case FRECHET:
            //return "frechet";
        }
    }

}
