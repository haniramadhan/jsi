import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.stack.array.TIntArrayStack;
import net.sf.jsi.Rectangle;
import net.sf.jsi.rtree.Node;
import net.sf.jsi.rtree.RTree;
import util.TrajUtil;

import java.io.*;
import java.util.*;

public class RTreeExample {

    public static void main(String[] args){
        TrajUtil.loadTrajDatasetFromFile("../dataset/chengdu-tiny.csv");

        long start = System.currentTimeMillis();
        RTree rTree = new RTree();
        rTree.init(null);
        int size = TrajUtil.trajectoryDataset.size();
        int rTreeSize = size * 10;
        for(int i=0;i<size;i++){
            double[][] mbr = TrajUtil.findMBR(TrajUtil.trajectoryDataset.get(i));
            rTree.add(new Rectangle(mbr[0][0],mbr[0][1],mbr[1][0],mbr[1][1]),i);
        }

        System.out.println(size);
        int rootNodeId = rTree.getRootNodeId();
        int nLeaf = 0;

        TIntArrayStack stack = new TIntArrayStack();
        TIntObjectHashMap partitionFeatureDistanceMap = new TIntObjectHashMap();
        stack.push(rootNodeId);
        boolean[] doubleVisit = new boolean[rTreeSize];
        int[] parents = new int[rTreeSize];
        int[] iEntry = new int[rTreeSize];
        int featureSize = 1;

        while(stack.size()>0){
            int parentId = stack.pop();

            if(parentId<doubleVisit.length) {
                if(doubleVisit[parentId]) {
                    continue;
                }
                doubleVisit[parentId] = true;
            }
            Node n = rTree.getNode(parentId);
            if(n==null)
                continue;
            List<Integer> node = new ArrayList<>();
            node.add(parentId);
            System.out.print(n.getLevel() + ","+n.getEntryCount()+ "," +parentId +";");

            if(n.getLevel()==1)
                nLeaf +=n.getEntryCount();

            List<double[]> dataset = new ArrayList<>();
            dataset.add(new double[featureSize + n.getEntryCount()]);
            dataset.get(0)[0] = -1;
            partitionFeatureDistanceMap.put(parentId, dataset);

            for (int i = 0; i < n.getEntryCount(); i++) {
                parents[n.getId(i)] = parentId;
                iEntry[n.getId(i)] = i;

                stack.push(n.getId(i));
                System.out.print(n.getId(i) + ", ");
            }
            System.out.println();
        }


        System.out.println(nLeaf);
        System.out.println(rTree.getNode(rTree.getRootNodeId()).getLevel());

        System.out.println("build time "+ (System.currentTimeMillis()-start)+" ms");
        WriteObjectToFile(rTree,"index/chengdu-tiny-mbr.iobj");

        File rawDataset = new File("groundTruth chengdu edr 0.003 -1626051758331.csv");
        try {
            FileReader fileReader = new FileReader(rawDataset);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            String[] idAndDistance = line.split(";");
            int qId = Integer.parseInt(idAndDistance[0]);
            for(int i=1;i<idAndDistance.length;i++) {
                int rId = Integer.parseInt(idAndDistance[i]);
                int parentId = parents[rId];
                while (parentId != 0) {
                    List<double[]> distanceDatasetMap = (List<double[]>) partitionFeatureDistanceMap.get(parentId);
                    int rowId = distanceDatasetMap.size();
                    distanceDatasetMap.add(new double[distanceDatasetMap.get(0).length]);
                    int j;
                    for(j=0;j<distanceDatasetMap.size();j++){
                        if(distanceDatasetMap.get(j)[0] == qId) {
                            rowId = j;
                            break;
                        }
                    }
                    double[] distanceEntry = distanceDatasetMap.get(rowId);;
                    distanceEntry[0] = qId;
                    distanceDatasetMap.get(rowId)[iEntry[rId]] = Double.parseDouble(idAndDistance[1]);

                    if (distanceDatasetMap.get(0)[0] == -1)
                        parentId = parents[rId];
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        TrajUtil.loadTrajDatasetFromFile("test_chengdu-tiny_1");
//        size = TrajUtil.trajectoryDataset.size();
//        double epsilon = 0.03;
//        for(int i=0;i<size;i++){
//            double[][] mbr = TrajUtil.findMBR(TrajUtil.trajectoryDataset.get(i));
//            long start1 = System.currentTimeMillis();
//            SaveToListProcedure proc = new SaveToListProcedure();
//            SaveToListProcedure proc2 = new SaveToListProcedure();
//
//            rTree.contains(new Rectangle(mbr[0][0]+epsilon,mbr[0][1]+epsilon,mbr[1][0]+epsilon,mbr[1][1]+epsilon),proc);
//            rTree.contains(new Rectangle(mbr[0][0]-epsilon,mbr[0][1]-epsilon,mbr[1][0]-epsilon,mbr[1][1]-epsilon),proc2);
//            BitSet idBigger = proc.getIds();
//            BitSet idSmaller = proc2.getIds();
//            idBigger.andNot(idSmaller);
//
//            System.out.println("intersection time "+ (System.currentTimeMillis()-start1)+" ms " + idBigger.cardinality());
//
//        }

    }


    public static void WriteObjectToFile(Object serObj,String filepath) {

        try {
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    static class SaveToListProcedure implements TIntProcedure {
        private BitSet ids = new BitSet();

        public boolean execute(int id) {
            ids.set(id);
            return true;
        };

        private BitSet getIds() {
            return ids;
        }
    }
}
