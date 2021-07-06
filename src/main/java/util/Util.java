package util;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public  class Util {

    public static final int CLOSEST = 0;
    public static final int DISTANCE = 1;
    public static List<double[]> dataset;
    public static double computeDistance(double[] p1, double[] p2){
        double distance = 0;
        for(int j=0;j<p1.length;j++){
            distance = distance + Math.pow(p1[j] - p2[j],2);
        }
        distance = Math.sqrt(distance);
        return distance;
    }
    public static float computeDistance(float[] p1, double[] p2){
        float[] px = new float[p2.length];
        for(int i=0;i<p2.length;i++)
            px[i] = (float) p2[i];
        return computeDistance(p1,px);
    }
    public static float computeDistance(float[] p1, float[] p2){
        float distance = 0;
        for(int j=0;j<p1.length;j++){
            distance = distance + (float)Math.pow(p1[j] - p2[j],2);
        }
        distance = (float) Math.sqrt(distance);
        return distance;
    }

    public static double[] findClosestAndDistance(double[] point, double[][] centroids){
        double[] closestAndDistance = new double[2];
        double closestDistance = -1;
        double closest = -1;

        for(int i=0;i<centroids.length; i++){
            double[] centroidPoint = centroids[i];
            double distance = Util.computeDistance(centroidPoint, point);

            if(closestDistance < 0 || closestDistance> distance){
                closestDistance = distance;
                closest = i;
            }
        }

        closestAndDistance[CLOSEST] =  closest;
        closestAndDistance[DISTANCE] = closestDistance;

        return closestAndDistance;
    }

    public static double[] parseCommaDelimited(String s){
        String[] values = s.split(",");
        double[] data = new double[values.length];
        for (int i = 0; i < values.length; i++)
            data[i] = Double.parseDouble(values[i]);
        return data;
    }

    public static void setDataset(List<double[]> dataset1){
        dataset = dataset1;
    }

    public static void loadDatasetFromFile(String path){
        dataset = new ArrayList();
        try  {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
                while ((line = br.readLine()) != null) {
                        dataset.add(parseCommaDelimited(line));

                }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int findLessBoundary(TDoubleList arrayList, double val) {
        return findLessBoundary(new TDoubleArrayList(arrayList),val);
    }

    public static int findMoreBoundary(TDoubleList arrayList, double val) {
        return findMoreBoundary(new TDoubleArrayList(arrayList),val);
    }
    public static int findLessBoundary(TDoubleArrayList arrayList, double val){


        if(val<=arrayList.get(0)||arrayList.size()==1)
            return 0;
        int low = 0, high = arrayList.size()-1;
        int index = (low+high)/2;
        while(high-low>1) {
            //System.out.println(val + " ("+ arrayList.get(index-1)+ "," + (index-1) + ") -  (" + arrayList.get(index)+ "," + (index) + ")");
            if (arrayList.get(index - 1) < val && arrayList.get(index) >= val) {
                return index;
            }
            else if(val > arrayList.get(index))
                low = index;
            else
                high = index;
            index = (low+high)/2;
        }
        if(val>arrayList.get(low))
            return high;
        return low;

    }

    public static int findMoreBoundary(TDoubleArrayList arrayList, double val){

        if(val>=arrayList.get(arrayList.size()-1)||arrayList.size()==1)
            return arrayList.size()-1;
        int low = 0, high = arrayList.size()-1;
        int index = (low+high)/2;
        while(high-low>1) {
            //System.out.println(val + " ("+ arrayList.get(index-1)+ "," + (index-1) + ") -  (" + arrayList.get(index)+ "," + (index) + ")");
            if (arrayList.get(index) <= val && val < arrayList.get(index + 1)) {
                return index;
            }
            else if(val < arrayList.get(index+1))
                high = index;
            else
                low = index;
            index = (low+high)/2;
        }
        if(val<arrayList.get(high))
            return low;
        return high;

    }

    public static class PointDistancePair implements Comparable{
        public double[] point;
        public double distance;

        public PointDistancePair(double[] point, double distance){
            this.point = point;
            this.distance = distance;
        }

        public int compareTo(Object o) {
            Double distance0 = this.distance;
            Double distance1 = ((PointDistancePair) o).distance;

            return distance0.compareTo(distance1);
        }
    }

    public static class IntDblPair implements Comparable{
        public int intVal;
        public double dblVal;

        public IntDblPair(int intVal, double distance){
            this.intVal = intVal;
            this.dblVal = distance;
        }

        public int getIntVal(){return intVal;}

        public int compareTo(Object o) {
            Double distance0 = this.dblVal;
            Double distance1 = ((IntDblPair)o).dblVal;
            return distance0.compareTo(distance1);
        }

        public String toString(){
            String s = "(";
            s = s + intVal + ",\t";
            s = s + dblVal + ")";
            return s;
        }
    }
}
