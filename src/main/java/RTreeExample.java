import gnu.trove.procedure.TIntProcedure;
import gnu.trove.stack.array.TIntArrayStack;
import net.sf.jsi.Rectangle;
import net.sf.jsi.rtree.Node;
import net.sf.jsi.rtree.RTree;
import util.TrajUtil;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RTreeExample {

    public static void main(String[] args){
        TrajUtil.loadTrajDatasetFromFile("../dataset/chengdu-tiny.csv");

        long start = System.currentTimeMillis();
        RTree rTree = new RTree();
        rTree.init(null);
        int size = TrajUtil.trajectoryDataset.size();
        for(int i=0;i<size;i++){
            double[][] mbr = TrajUtil.findMBR(TrajUtil.trajectoryDataset.get(i));
            rTree.add(new Rectangle(mbr[0][0],mbr[0][1],mbr[1][0],mbr[1][1]),i);
        }

        System.out.println(size);
        int rootNodeId = rTree.getRootNodeId();
        int nLeaf = 0;
        TIntArrayStack stack = new TIntArrayStack();
        stack.push(rootNodeId);
        boolean[] doubleVisit = new boolean[10000000];
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
            System.out.print(n.getLevel() + ","+n.getEntryCount()+ "," +parentId +";");
            if(n.getLevel()==1)
                nLeaf +=n.getEntryCount();
            for (int i = 0; i < n.getEntryCount(); i++) {

                stack.push(n.getId(i));
                System.out.print(n.getId(i) + ", ");
            }
            System.out.println();
        }
        System.out.println(nLeaf);
        System.out.println(rTree.getNode(rTree.getRootNodeId()).getLevel());


        System.out.println("build time "+ (System.currentTimeMillis()-start)+" ms");
        WriteObjectToFile(rTree,"index/chengdu-tiny-mbr.iobj");
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
    };
}
