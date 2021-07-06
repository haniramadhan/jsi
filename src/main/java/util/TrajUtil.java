package util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class TrajUtil {
    public static List<List<double[]>> trajectoryDataset;
    public static List<List<double[]>> samplePointsSet;
    public static double[][] distance;

    public static int getDim() throws Exception {
        if(trajectoryDataset == null)
            throw new Exception("Trajectory dataset is not loaded");
        return trajectoryDataset.get(0).get(0).length;
    }


    public static double computeDistance(List<double[]> trajQ, List<double[]> trajR){
        switch(Distance.currentDistance){
            case Distance.DTW:
                return computeDTW(trajQ,trajR);

            case Distance.FRECHET:
                return computeFrechet(trajQ,trajR);

            case Distance.EDR:
                return computeEDR(trajQ,trajR,Distance.eps);

            case Distance.LCSS:
                return computeLCSS(trajQ,trajR,Distance.eps,Distance.spaceConstraint);
        }
        return -1;
    }

    public static double computeDTW(List<double[]> t1, List<double[]> t2){
        initMinDistance(t1.size(),t2.size());

        for(int i=1;i<=t1.size();i++){
            for(int j=1;j<=t2.size();j++){
                computeDTW(i,j,t1,t2);
            }
        }

        return distance[t1.size()][t2.size()];
    }

    public static double computeEDR(List<double[]> t1, List<double[]> t2, double threshold){
        initMinDistance(t1.size(),t2.size());
        for(int i=0;i<=t1.size();i++)
            distance[i][0]=i;
        for(int i=0;i<=t2.size();i++)
            distance[0][i]=i;
        for(int i=0;i<t1.size();i++){
            for(int j=0;j<t2.size();j++){
                double dist = Util.computeDistance(t1.get(i),t2.get(j));
                int notMatch = 0;
                if(dist>threshold)
                    notMatch = 1;
                distance[i+1][j+1] = distance[i][j] + notMatch;
                distance[i+1][j+1] = Math.min(distance[i+1][j] + 1,distance[i+1][j+1]);
                distance[i+1][j+1] = Math.min(distance[i][j+1] + 1,distance[i+1][j+1]);
            }
        }
        return distance[t1.size()][t2.size()];
    }

    public static double computeLCSS(List<double[]> t1, List<double[]> t2, double threshold, int maxIndexDiff){
        initMinDistance(t1.size(),t2.size());

        distance[0][0] = 0;
        for(int i=1;i<=t1.size();i++)
            distance[i][0] = i;

        for(int j=1;j<=t2.size();j++)
            distance[0][j] = j;

        for(int i=1;i<=t1.size();i++){
            for(int j=1;j<=t2.size();j++){
                double dist = Util.computeDistance(t1.get(i-1),t2.get(j-1));
                if(dist>threshold || Math.abs(i-j)>maxIndexDiff)
                    distance[i][j] = Math.min(distance[i-1][j],distance[i][j-1])+1;
                else
                    distance[i][j] = distance[i-1][j-1];
            }
        }

        return distance[t1.size()][t2.size()];
    }

    public static double computeFrechet(List<double[]> t1, List<double[]> t2){
        initMinDistance(t1.size(),t2.size());

        distance[0][0] = Util.computeDistance(t1.get(0),t2.get(0));

        for(int i=0;i<t1.size();i++){
            for(int j=0;j<t2.size();j++){
                if(i==0&&j==0) continue;
                double dist = Util.computeDistance(t1.get(i),t2.get(j));
                if(i==0) {
                    distance[i][j] = Math.max(dist, distance[i][j-1]);
                    continue;
                }
                if(j==0){
                    distance[i][j] = Math.max(dist, distance[i-1][j]);
                    continue;
                }
                double distMin = Math.min(distance[i][j-1], distance[i-1][j]);
                distMin = Math.min(distMin, distance[i-1][j-1]);
                dist = Math.max(dist,distMin);
                distance[i][j] = dist;
            }
        }

        return distance[t1.size()-1][t2.size()-1];
    }
    public static void initMinDistance(int t1Size, int t2Size){
        distance = new double[t1Size+1][];
        for(int i=0;i<=t1Size;i++){
            distance[i] = new double[t2Size+1];
        }

    }

    public static double addMinimumOfThree(double distance, double distance1, double distance2, double distance3){

        if(distance1<distance2) {
            if (distance1 < distance3) {
                distance = distance + distance1;
            }
            else{
                distance = distance + distance3;
            }
        }
        else if(distance2<distance3){
            distance = distance + distance2;
        }
        else {
            distance = distance + distance3;
        }
        return distance;
    }

    public static void computeDTW(int i1, int i2, List<double[]> t1, List<double[]> t2){
        double distance = 0;
        if(i1==1){
            for(int i=0;i<i2;i++){
                distance = distance + Util.computeDistance(t1.get(0),t2.get(i));
            }
        }
        else if(i2 == 1){
            for(int i=0;i<i1;i++){
                distance = distance + Util.computeDistance(t1.get(i),t2.get(0));
            }
        }
        else{
            distance = distance + Util.computeDistance(t1.get(i1-1),t2.get(i2-1));
            distance = addMinimumOfThree(distance, TrajUtil.distance[i1-1][i2-1], TrajUtil.distance[i1][i2-1],
                    TrajUtil.distance[i1-1][i2]);
        }
        TrajUtil.distance[i1][i2] = distance;
    }



    public static List<double[]> findPivotPoint(List<double[]> traj, int numPivotPoints){
        //using first/last doubleVal strategy
        List<double[]> pivotPoints = new ArrayList<>();
        if(numPivotPoints == 0)
            return pivotPoints;
        PriorityQueue<Util.PointDistancePair> weights = new PriorityQueue<>();
        if(traj.size() < 3)
            return pivotPoints;

        double[] first = traj.get(0);
        double[] last = traj.get(traj.size()-1);
        for(int i=1;i<traj.size()-1;i++){
            double distFirst = 0;
            double distLast = 0;
            double[] point = traj.get(i);
            for(int j=0;j<point.length;j++){
                distFirst = distFirst  + Math.pow(point[j]-first[j],2);
                distLast  = distLast  + Math.pow(point[j]-last[j],2);
            }
            distFirst = Math.sqrt(distFirst);
            distLast = Math.sqrt(distLast);

            weights.add(new Util.PointDistancePair(point,-1*(distFirst+distLast)));
        }

        int listSize = numPivotPoints > weights.size() ? weights.size() : numPivotPoints;
        while(listSize>0){
            Util.PointDistancePair pdPair = weights.poll();
            double[] point = new double[pdPair.point.length];
            for(int i=0;i<point.length;i++){
                point[i] = 0 + pdPair.point[i];
            }
            pivotPoints.add(point);
            listSize--;
        }
        return pivotPoints;
    }


    public static void loadTrajDatasetFromFile(String path){
        trajectoryDataset = new ArrayList<>();
        try  {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {

                String[] values = line.split(";");
                List<double[]> traj = new ArrayList<>();

                for(String s:values){
                    traj.add(Util.parseCommaDelimited(s));
                }
                trajectoryDataset.add(traj);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildTrajPointsDataset(){
        buildTrajPointsDataset(0);
    }
    public static void buildTrajPointsDataset(int numPivotPoints){
        //transforming the trajectory dataset to sets of points.
        //A set of points contains either first points, last points, or pivot points of the trajectory set
        //When a trajectory only has 1 point, the set of last points will be set as null at that trajectory
        //This case will also apply for the sets of pivot points when length of trajectory - 2 < num of pivot points
        //Illustration
        //trajectory set: {a1,a2,a3,a4}, {b1,b2}, {c1,c2,c3,c4,c5}, {d1}
        //result set:
        // [0] ->{a1,b1,c1,d1}  (first point)
        // [1] -> {a4,b2,c5,null} (last point)
        // [2] -> {a2,null,c3,null} (pivot point)

        int pointLength = trajectoryDataset.get(0).get(0).length;
        List<List<double[]>> trajSampleSet = new ArrayList<>();
        samplePointsSet = new ArrayList<>();

        for(int i=0;i<trajectoryDataset.size();i++) {
            List<double[]> traj = trajectoryDataset.get(i);
            List<double[]> trajSamplePoints = new ArrayList<>();
            double[] first = new double[pointLength];
            for(int j=0;j<traj.get(0).length;j++)
                first[j] = traj.get(0)[j];
            trajSamplePoints.add(first);
            if(traj.size()>1) {
                int iLast = traj.size()-1;
                double[] last = new double[pointLength];
                for (int j = 0; j < traj.get(iLast).length; j++)
                    last[j] = 0 + traj.get(iLast)[j];
                trajSamplePoints.add(last);
            }
            trajSamplePoints.addAll(findPivotPoint(traj, numPivotPoints));
            trajSampleSet.add(trajSamplePoints);
        }

        for(int i=0;i<numPivotPoints+2;i++){
            List<double[]> samplePoints = new ArrayList<>();
            for(int j=0;j<trajSampleSet.size();j++){
                if(trajSampleSet.get(j).isEmpty())
                    samplePoints.add(null);
                else{
                    samplePoints.add(trajSampleSet.get(j).get(0));
                    trajSampleSet.get(j).remove(0);
                }
            }
            samplePointsSet.add(samplePoints);
        }
    }

    public static double[][] findMBR(List<double[]> traj){
        int dim = traj.get(0).length;
        double[][] mbr = new double[2][dim];

        for(int i=0;i<dim;i++){
            mbr[0][i] =  traj.get(0)[i];
            mbr[1][i] =  traj.get(0)[i];
        }

        for(int i=0;i<traj.size();i++){
            for(int d=0;d<dim;d++) {
                if (mbr[0][d] > traj.get(i)[d])//min
                    mbr[0][d] = traj.get(i)[d];
                if (mbr[1][d]< traj.get(i)[d])//max
                    mbr[1][d] = traj.get(i)[d];
            }
        }
        return mbr;
    }
}
