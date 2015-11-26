package mop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problems.AProblem;
import utilities.WrongRemindException;
import utilities.StringJoin;

public class CHEAMOP extends MOP{

    private CHEAMOP(int popSize,AProblem problem,int hyperplaneIntercept,int neighbourNum) {
        this.popSize = popSize;   
        this.neighbourNum = neighbourNum;
        this.hyperplaneIntercept = hyperplaneIntercept;
        this.objectiveDimesion = AProblem.objectiveDimesion;
        this.problem = problem;   
		this.perIntercept = (double)1.0/hyperplaneIntercept;
        allocateAll();
    } 	
	
	public static MOP getInstance(int popSize,AProblem problem,int hyperplaneIntercept,int neighbourNum) {
		if(null == instance)
			instance = new CHEAMOP(popSize,problem,hyperplaneIntercept,neighbourNum);
		return instance;
	}

	public CHEAMOP(int objectiveDimesion) {
		objectiveDimesion = this.objectiveDimesion;
		allocate();
	}
	

	// init all data struct need to initial Nov 18
	public void initial() {
		initPopulation();
		initNeighbour(neighbourNum);
		//after this part , next would be step 2 Nov 19
	}

    // initial the neighbour point for neighbour's subProblems. Nov 11.
    public void initNeighbour(int neighbourNum) {
        for (int i = 0; i <  popSize ; i ++) { 
			//System.out.println("i is : " + i + ",  neighbourNum is : " + neighbourNum);
            sops.get(i).getVicinity(neighbourNum,hyperplaneIntercept);
        }
	}

	// generate subProblem, and add points to subpIndexOnEdge ... Nov 18
	private int genSubProblems(int startObjIndex,int maxValueLeft,int[] coordinate,int indexCount) { 
		if( 0 == startObjIndex ||  0 == maxValueLeft ) {
			indexCount ++;
			//System.out.println("indexCount : " + indexCount );
			coordinate[startObjIndex] = maxValueLeft;
			SOP subProblem = new SOP(CMoChromosome.createChromosome());
			subProblem.vObj = coordinate;
			subProblem.sectorialIndex = indexCount;
			// initial subProblem's ind . belongSubproblemIndex 
			// is useless Nov 19
			// why wrong in belongSubproblemIndex  Nov 21
			subProblem.ind.belongSubproblemIndex = -1;
			subProblem.ind.evaluate(problem);
			sops.add(subProblem);
			int count = 0 ;
			for(int p = 0; p < objectiveDimesion; p ++) {
				if( 0 == coordinate[p] ) {
					subpIndexOnEdge.add(new Integer(indexCount));
					break;
				}
			}
			return indexCount;
		}
		for( int i = maxValueLeft; i >= 0; i --) {
			coordinate[startObjIndex] = i;
			indexCount = genSubProblems(startObjIndex - 1, maxValueLeft - i, coordinate, indexCount);
		}
		return indexCount;
	}

	// complete all initiazation things . Nov 19
	public void initPopulation() {
		int[] coordinate = new int[objectiveDimesion];
		// generate subProblem , add them all into sops  , Nov 18
		int indexCount = -1;
	    indexCount = genSubProblems(objectiveDimesion-1,hyperplaneIntercept,coordinate,indexCount);
		//System.out.println("indexCount is : " + indexCount );
		sizeSubpOnEdge = subpIndexOnEdge.size();
		// initial the all points
		for(int i = 0; i < objectiveDimesion; i ++) {
			anchorPoint[i] = sops.get(0).ind.objectiveValue;
			trueNadirPoint[i] = sops.get(0).ind.objectiveValue[i];
			idealPoint[i] = anchorPoint[i][i];
			referencePoint[i] = trueNadirPoint[i] + 1e3 * (trueNadirPoint[i] - idealPoint[i]);
		}
		for(int n = 1 ; n < popSize; n ++) {
			updateExtremePoint(sops.get(n).ind);
			//sops.get(n).ind.evaluate(problem);
		}
		updatePartition();
	}

	public boolean updateExtremePoint(MoChromosome ind) {
		boolean bAnchorUpdated = false;
		boolean bTrueNadirUpdated = false;
		boolean bAnchorUpdatedItem = false;
		for(int j = 0; j < objectiveDimesion; j ++) {
			if(ind.objectiveValue[j] < anchorPoint[j][j]) {
				bAnchorUpdated = true;
				anchorPoint[j] = ind.objectiveValue;
				idealPoint[j] = anchorPoint[j][j];
				bAnchorUpdatedItem = true;
			}
			if(ind.objectiveValue[j] > trueNadirPoint[j]) {
				bTrueNadirUpdated = true;
				trueNadirPoint[j] = ind.objectiveValue[j];
			}
			if(bAnchorUpdatedItem || bTrueNadirUpdated ) {
				referencePoint[j] = trueNadirPoint[j] + 1e3 * (trueNadirPoint[j] - idealPoint[j]);
				bAnchorUpdatedItem = false;
				bTrueNadirUpdated = false;
			}
		}
		return bAnchorUpdated;
	}

	// update sop 's idealPoint for reducer's update ind
	// Nov 23
	public void updateSopIdealPoint() {
		for(int i = 0; i < sops.size(); i ++) {
			for(int j = 0 ; j < objectiveDimesion; j ++) {
				sops.get(i).idealPoint[j] = idealPoint[j];
			}
		}
	}



    // tour select two points as parents for reproduction.  Nov 11
    public int tourSelectionHV(List<SOP> sops) {
        int p1 = (int)PRNG.nextDouble(0,1) * popSize;
        int p2 = (int)PRNG.nextDouble(0,1) * popSize;
        double hv1 = tourSelectionHVDifference(p1,sops);
        double hv2 = tourSelectionHVDifference(p2,sops);
        if(hv1 >= hv2) return p1;
        else return p2;
    }

    public double tourSelectionHVDifference(int p,List<SOP> sops){
            int num = 0 ;
            int index ;
            double hvSide = 0.0;
            double hvDifference = 0.0;
            
            // need to add a sub-problem class , CHEA must have a sub problem  Nov 13
            while(sops.get(p).ind.belongSubproblemIndex != sops.get(p).sectorialIndex) {
                p = sops.get(p).ind.belongSubproblemIndex;
            }
            SOP subProblem = sops.get(p);
            int subProblemNeighbourSize  = subProblem.neighbour.size();
            double hv0 = getHyperVolume(sops.get(p).ind, referencePoint);
            for(int i = 0 ; i < subProblemNeighbourSize; i ++) {
                SOP sop = sops.get(subProblem.neighbour.get(i).intValue());
                if( sop.sectorialIndex == sop.ind.belongSubproblemIndex) {
                    hvSide = getHyperVolume(sop.ind, referencePoint);
                    hvDifference += (hv0 - hvSide);
                    num ++;
                }
            }
            if(num != 0) hvDifference = hvDifference/num;
            return hvDifference;
    }
/*
	public double getHyperVolume(MoChromosome ind,double[] referencePointCalc) {
        double volume = 1;
        for(int j = 0 ; j < ind.objectiveDimesion; j ++) volume *= (referencePointCalc[j] - ind.objectiveValue[j]);
        return volume;	
	}
*/

	//public void excute(int run,List<double> igd) {}
/*
	public void updatePop(int iterations) {
		//initial IGD calc Nov 19
		IGD igdOper = new IGD(1500);
		String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ1(3).dat";
		try {
			ps = igdOper.loadPfront(filename);
		} catch (IOException e) {
		}

		for(int gen = 1 ; gen <= iterations; gen ++) {
			evolutionTourSelect2();
			// calc igd , using ps and sops's objectiveValue 
			// Nov 21
			double[] genDisIGD = new double[2];
			genDisIGD[0] = gen;
			genDisIGD[1] = calcIGD();
			igdOper.igd.add(genDisIGD);

			// add IGD value into a datastruct Nov 19
		}
		filename = "/home/laboratory/workspace/moead_parallel/experiments/CHEA_IGD_DTLZ1_3.txt";
		try {
			igdOper.saveIGD(filename);	
		} catch (IOException e) {
		}
	}

*/

    // update Pop part is main to excute the evolustion. Nov 14
    @Override
    public void updatePop(int innerTime) {
		for(int gen = 1 ; gen <= innerTime; gen ++) {
			evolutionTourSelect2();
		}
	}

	
	public void evolutionTourSelect2() {
        boolean isUpdate = false;
        int len = 0 ;
		MoChromosome offSpring ;
		//MoChromosome tmp;
        // need to add a part about calculating the IGD every 25 gen or 10 gen Nov 11
        for(int i = 0 ;i < popSize; i ++){
            // this is MOEAD part ; delete evolveNewInd(i);
            // select two indivduals to reproduce a new offspring. Nov 11
            int parentIndex1 ;
            int parentIndex2 ;
            int b = len % (popSize/7); 
            if(b < sizeSubpOnEdge) {
                parentIndex1 =  subpIndexOnEdge.get(b).intValue();
            } else {
                parentIndex1 = tourSelectionHV(sops);
            }
            parentIndex2 = tourSelectionHV(sops);
            offSpring = new CMoChromosome();
            offSpring.crossover((MoChromosome)sops.get(parentIndex1).ind,(MoChromosome)sops.get(parentIndex2).ind);
            offSpring.mutate(1d/offSpring.genesDimesion);
            
            offSpring.evaluate(problem);
			if(updateExtremePoint(offSpring)) updatePartition();
            //updatePoints(offSpring);
            offSpring.objIndex(idealPoint,hyperplaneIntercept);
			if(null != (offSpring = hyperVolumeCompareSectorialGrid(offSpring))) {
				updateFixWeight(sops.get(offSpring.belongSubproblemIndex),true);
			}
            len ++; 
        }

        // leave empty place for IGD
    }

    // updatePoints including idealPoint points ,reference points and extrem points. Nov 11
    private void updatePoints(MoChromosome offSpring) {

		// update idealPoint  Nov 17
        for(int j = 0 ; j < offSpring.objectiveDimesion; j ++){
            if(offSpring.objectiveValue[j] < idealPoint[j]){
                idealPoint[j] = offSpring.objectiveValue[j];
            }
        }	

		//update reference points
		
		// update extrem points

	}

	// update after offspring 
	public void updatePartition() {
		for( int n = 0; n < popSize; n ++) {
			sops.get(n).ind.objIndex(idealPoint,hyperplaneIntercept);
		}
		boolean[] sopsFlag = new boolean[popSize];
		List<MoChromosome> initRestIndPop = new ArrayList<MoChromosome>(popSize);
		for(int n = 0 ; n < popSize; n ++) {
			if(sops.get(n).sectorialIndex == sops.get(n).ind.belongSubproblemIndex ){
				sopsFlag[sops.get(n).sectorialIndex] = true;
			} else {
				MoChromosome ind = sops.get(n).ind;
				// maybe something wrong happend cause two "ind" Nov 18
				// had fix it Nov 20
				while(true) {
					sopsFlag[ind.belongSubproblemIndex] = true;
					MoChromosome ind2;
					if(null == (ind2 = hyperVolumeCompareSectorialGrid(ind))) 	break;
					ind = ind2;
				}
				initRestIndPop.add(ind);
			}
		}
		int sizeOfRestInd = initRestIndPop.size();
		//List<double[]> vObjRestInd = new ArrayList<double[]>(sizeOfRestInd);
		List<int[]> vObjRestInd = new ArrayList<int[]>(sizeOfRestInd);
		//double[] calVObj ; // modify at Nov  19
		int[] calVObj ;
		for(int i = 0 ; i < sizeOfRestInd; i ++) {
			calVObj = initRestIndPop.get(i).calVObj(idealPoint,hyperplaneIntercept);
			vObjRestInd.add(calVObj);
		}
		for(int i = 0 ; i < popSize; i ++) {
			if(false  == sopsFlag[i] ) {
				SOP subProblem = sops.get(i);
				int minIndexDist = 0;
				for(int j = 0 ; j < objectiveDimesion; j ++) {
					// Nov 18 maybe wrong in this place because of wrong define minIndexDist.
					// for every objectiveDimesion value, should find the minium value, but below couldn't
					// I don't know the use of minIndexDist. so don't know howto modify.
					minIndexDist = (int)Math.pow(vObjRestInd.get(0)[j] - subProblem.vObj[j], 2.0);
				}
				int minDiffIndex = 0 ;
				int restSize = initRestIndPop.size();
				for( int k = 1; k < restSize; k ++ ) {
					int indexDist = 0 ;
					for (int j = 0 ; j < objectiveDimesion; j ++ ) {
						indexDist = (int)Math.pow(vObjRestInd.get(k)[j] - subProblem.vObj[j], 2.0);
					}
					if(indexDist < minIndexDist || (indexDist == minIndexDist && PRNG.nextDouble() > 0.5) ) {
						minIndexDist = indexDist;
						minDiffIndex = k;
					}
				}

				sops.get(i).ind = initRestIndPop.get(minDiffIndex);
				sopsFlag[i] = true;

				// ################ don't know the use.  Nov 18
				//initRestIndPop.get(minDiffIndex) = initRestIndPop.get(restSize - 1);
				//vObjRestInd.get(minDiffIndex) = vObjRestInd.get(restSize - 1);
				initRestIndPop.get(restSize-1).copyTo(initRestIndPop.get(minDiffIndex));
				for(int m = 0 ;m < vObjRestInd.get(minDiffIndex).length; m ++) vObjRestInd.get(minDiffIndex)[m] = vObjRestInd.get(restSize - 1)[m];
				initRestIndPop.remove(initRestIndPop.size() - 1);
				vObjRestInd.remove(vObjRestInd.size() - 1);
			}
		}
	}


	public List<double[]> population2front(List<SOP> pop) {
		List<double[]> popFront = new ArrayList<double[]>(popSize);
		int[] nDominated = new int[pop.size()];
		for(int k = 0; k < pop.size(); k ++) 
			for(int j = k + 1; j < pop.size(); j ++) {
				int result = pop.get(k).ind.compareInd(pop.get(j).ind);
				if(2 == result) nDominated[k]++;
				else if (1 == result) nDominated[j]++;
			}
		for(int n = 0; n < pop.size(); n ++) {
			if( 0 == nDominated[n] ) popFront.add(pop.get(n).ind.objectiveValue);
		}
		return popFront;
	}

	public void updateFixWeight(SOP subProblem,boolean delivery) {
		if(subProblem.sectorialIndex != subProblem.ind.belongSubproblemIndex) {
			for(int k = 0 ; k < objectiveDimesion; k ++)  subProblem.fixWeight[k] = 1.0;
			return;
		}
		int size = subProblem.neighbour.size();
		int num = 0 ;
		double[] weight = new double[objectiveDimesion];
		for(int j = 0; j < size; j ++) {
			SOP subNeighbour = sops.get(subProblem.neighbour.get(j).intValue());
			if(subNeighbour.sectorialIndex == subNeighbour.ind.belongSubproblemIndex) {
				for(int k = 0 ; k < objectiveDimesion; k++ ) {
					weight[k] += Math.abs(subProblem.ind.objectiveValue[k] - subNeighbour.ind.objectiveValue[k]);
					if(delivery) updateFixWeight(subNeighbour,false);
				}
				num ++;
			}
		}
		if( 0 == num ) {
			for(int k = 0 ;k < objectiveDimesion; k ++ ) subProblem.fixWeight[k] = 1.0;
		} else {
			weight[0] /= num;
			double maxW = weight[0];
			int maxIndex = 0 ;
			for(int k = 1; k < objectiveDimesion; k ++) {
				weight[k] /= num;
				if(weight[k] > maxW) {
					maxW = weight[k];
					maxIndex = k;
				}
				double timeW = referencePoint[maxIndex] / maxW;
				for(int f = 0 ; f < objectiveDimesion; f ++) {
					subProblem.fixWeight[f] = 1.0 + timeW * weight[f];
				}
			}
		}
	}

	// ########### maybe cause error in this place #########
	// Nov 20
	public MoChromosome hyperVolumeCompareSectorialGrid(MoChromosome ind) {
		MoChromosome rInd = null;
		double c1;
		double c2;
		SOP subProblem = sops.get(ind.belongSubproblemIndex);
		if(subProblem.sectorialIndex == subProblem.ind.belongSubproblemIndex) {
			double[] refCal = new double[objectiveDimesion];
			subProblem.ind.calKVal(idealPoint,hyperplaneIntercept);
			double k = ind.kValue > subProblem.ind.kValue ? ind.kValue : subProblem.ind.kValue;
			for(int i = 0 ; i < objectiveDimesion; i ++) {
				refCal[i] = (idealPoint[i] + k * perIntercept * ( subProblem.vObj[i] + subProblem.fixWeight[i]));
			}
			c1 = getHyperVolume(ind,refCal);
			c2 = getHyperVolume(subProblem.ind,refCal);
			if(c1 > c2) {
				rInd = sops.get(ind.belongSubproblemIndex).ind;
				sops.get(ind.belongSubproblemIndex).ind = ind;
			}
		} else {
			rInd = sops.get(ind.belongSubproblemIndex).ind;
			sops.get(ind.belongSubproblemIndex).ind = ind;
		}
		return rInd;
	}

	/*
    private double calcIGD() {
        double distanceIGD = 0.0;
        for (int i  = 0 ; i < ps.size(); i ++) {
            double minDistance = 1.0e+10;
            for (int j = 0 ; j < popSize; j ++) {
            	double d = calcDistance(ps.get(i),sops.get(j).ind.objectiveValue);
            	if(d < minDistance) minDistance = d;
            }
            distanceIGD += minDistance;
        }
        distanceIGD /= popSize;
        return distanceIGD;
    }
*/

    public void write2File(String fileName) throws IOException{
        File file = new File(fileName);
        if(!file.exists()){
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
		List<double[]> popFront = population2front(sops);
        for(int n = 0 ; n < popFront.size(); n ++){
			bw.write(StringJoin.join(" ",popFront.get(n)));
            if(n < popSize - 1) bw.write("\n");
        }
        bw.close();
        fw.close();
    }
}
