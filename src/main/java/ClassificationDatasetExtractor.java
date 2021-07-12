import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import util.Distance;
import util.TrajUtil;
import util.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ClassificationDatasetExtractor {
    public static void main(String[] args) {

//        TrajUtil.loadTrajDatasetFromFile("../dataset/atc-subset-f-400.csv");
//        int trSize = 400;
//        String dsName = "atc";
//        Distance.setParameters(0.2,3);

        TrajUtil.loadTrajDatasetFromFile("../dataset/chengdu-tiny.csv");
        String dsName = "chengdu";
        int trSize = 1000;
        Distance.setParameters(0.003,3);

        List<Integer> sample = sampling(trSize, TrajUtil.trajectoryDataset.size());

        double tauThresh1 = 0.001;
        double tauThresh2 = 1;
        double tau;

        int distances[] = {
                Distance.FRECHET,
                Distance.DTW,
                Distance.EDR,
                Distance.LCSS
        };

        String fileAdd = "";
        Locale currentLocale = Locale.ENGLISH;

        for(int d:distances) {
            Distance.setCurrentDistance(d);
            //Distance.setParameters(0.2,3);
            tau = tauThresh1;
            if(d == Distance.EDR || d == Distance.LCSS) {
                tau = tauThresh2;
                fileAdd = String.format(currentLocale,"%.3f",Distance.eps) + " ";
                if(d==Distance.LCSS)
                    fileAdd = fileAdd+ Distance.spaceConstraint+" ";
            }
            File groundTruth = new File("groundTruth "
                    + dsName+" " + Distance.getDistanceStr() +
                    " "+ fileAdd + "-"+System.currentTimeMillis() + ".csv");

            try {
                FileWriter fileWriter = new FileWriter(groundTruth);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                for (int q = 0; q < trSize; q++) {

                    List<double[]> qTraj = TrajUtil.trajectoryDataset.get(sample.get(q));
                    TIntList result = new TIntArrayList();
                    TDoubleList distList = new TDoubleArrayList();
                    for (int i = 0; i < TrajUtil.trajectoryDataset.size(); i++) {

                        //simple pruning
                        if(simplePrune(qTraj, TrajUtil.trajectoryDataset.get(i),tau))
                            continue;
                        double dist = TrajUtil.computeDistance(qTraj, TrajUtil.trajectoryDataset.get(i));
                        if (dist <= tau) {
                            result.add(i);
                            distList.add(dist);
                        }
                    }
                    System.out.println(Distance.getDistanceStr() + " " + sample.get(q) + "/" + trSize);
                    bufferedWriter.write(sample.get(q) + ",0;");

                    for (int i = 0; i < result.size(); i++)
                        if(Distance.currentDistance == Distance.FRECHET || Distance.currentDistance == Distance.DTW )
                            bufferedWriter.write(result.get(i) + "," + String.format(currentLocale,"%.5f",distList.get(i))+";");
                        else
                            bufferedWriter.write(result.get(i) + "," + String.format("%.0f",distList.get(i))+";");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                bufferedWriter.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    public static boolean simplePrune(List<double[]> qTraj, List<double[]> traj, double tau){
        if(Distance.currentDistance == Distance.FRECHET || Distance.currentDistance == Distance.DTW){
            double firstPointDistance = Util.computeDistance(qTraj.get(0),traj.get(0));
            double lastPointDistance = Util.computeDistance(qTraj.get(qTraj.size()-1),traj.get(traj.size()-1));
            return (firstPointDistance> tau || lastPointDistance > tau) ||
                    (Distance.currentDistance == Distance.DTW && firstPointDistance + lastPointDistance > tau);
        }
        return Math.abs(qTraj.size() - traj.size()) > tau;
    }


    public static List<Integer> sampling(int size, int fullSize){
        List<Integer> sample = new ArrayList<>();
        Random r = new Random();
        int i=0;
        double prob =size * 1.0 / fullSize;
        while(sample.size()<size){
            if(i>=fullSize)
                i=0;

            if(r.nextDouble()<=prob)
                if(!sample.contains(i))
                    sample.add(i);

            i++;
        }

        return sample;
    }
}
