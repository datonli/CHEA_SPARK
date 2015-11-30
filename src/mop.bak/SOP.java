package mop;

import java.util.List;
import java.util.ArrayList;

public class SOP {
	public MoChromosome ind ;
	public int sectorialIndex;
	public List<Integer> neighbour;
	//public double[] vObj;
	public int[] vObj;
	public double[] fixWeight;
	public int objectiveDimesion;
	public double[] idealPoint;

	public SOP(MoChromosome ind) {
		this.ind = ind;
		//ind.copyTo(this.ind);
		//vObj = new double[ind.getObjectiveDimesion()];
		objectiveDimesion = ind.getObjectiveDimesion();
		//System.out.println("objectiveDimesion is : " + objectiveDimesion);
		vObj = new int[objectiveDimesion];
		fixWeight = new double[objectiveDimesion];
		idealPoint = new double[objectiveDimesion];
		neighbour = new ArrayList<Integer>(objectiveDimesion);
		for(int i = 0 ; i < objectiveDimesion; i ++) fixWeight[i] = 1.0;
	}

	public void getVicinity(int vicinityRange, int hyperplaneIntercept) {
		//System.out.println("begin getVicinity");
		int[] vicinity = new int[objectiveDimesion];
		//System.out.println("begin calVicinity");
		calVicinity(vicinityRange,hyperplaneIntercept,0,0,false,vicinity,neighbour);
	}

	// cal vObj and vicinity, save the result in neighbour
	void calVicinity(int vicinityRange, int hyperplaneIntercept, int calIndexNow, int leftRange, boolean isChangeBefore, int[] vicinity, List<Integer> neighbour) {
		int indexValue = vObj[calIndexNow];
		for (int k = indexValue - vicinityRange; k <= indexValue + vicinityRange; k ++) {
			if (k < 0 || k > hyperplaneIntercept ) continue;
			vicinity[calIndexNow] = k;
			if (calIndexNow == objectiveDimesion - 1) {
				if (isChangeBefore && (leftRange + (k - indexValue) == 0) ) {
					//System.out.println("calIndexNow : " + calIndexNow + "   , neighbour's size : " + neighbour.size());
					int index = ind.getIndexFromVObj(vicinity, hyperplaneIntercept);
					neighbour.add(new Integer(index));
				}
				continue;
			}
			boolean trueOrFalse = true;
			if ( !isChangeBefore && (k == indexValue) ) trueOrFalse = false;
			calVicinity(vicinityRange,hyperplaneIntercept,calIndexNow + 1,leftRange + (k - indexValue),trueOrFalse,vicinity,neighbour);
		}
	}

}
