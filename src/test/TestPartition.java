package test;

import java.util.List;
import java.util.ArrayList;
import utilities.StringJoin;


public class TestPartition {

    public List<int[]> indexRangePartition(int popSize,double p,int partitionNum) {
        int increase =(int)(p*popSize);
        int part = (int)(popSize/partitionNum);
        List<int[]> partitions = new ArrayList<int[]>(partitionNum);
        int index = 0 ;
        for(int i = 0; i < partitionNum; i ++) {
			if(0 == i) ;
			else if(partitionNum - 1 == i) index = popSize - (part + increase);
			else index -= increase/2;
			int[] arr = new int[part + increase];                                                                                                                                                                  
            for(int j = 0; j < part + increase; j ++) {
                    arr[j] = index;
                    index ++;
            }
            partitions.add(arr);
        }
		return partitions;
    }

	public static void main(String[] args) {
		int popSize = 55;
		TestPartition p = new TestPartition();
		List<int[]> partitions = p.indexRangePartition(popSize,0.05,4);
		for(int i = 0 ; i < partitions.size(); i ++) {
			System.out.println("The part " + i + " 's length is : " + partitions.get(i).length + ", value is : " + StringJoin.join(" ",partitions.get(i)));
		}
	}
}
