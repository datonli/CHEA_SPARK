package mop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problems.AProblem;
import utilities.WrongRemindException;

public class CMOP extends AMOP {

	private final double F = 0.5;
	private final double CR = 1;
	
	private CMOP(int popSize,AProblem problem,int hyperplaneIntercept,int neighbourNum){
		this.popSize = popSize;
		this.neighbourNum = neighbourNum;
		this.hyperplaneIntercept = hyperplaneIntercept;
		this.objectiveDimesion = AProblem.objectiveDimesion;
		this.problem = problem;
		allocateAll();
	}

	
	public static AMOP getInstance(int popSize,int neighbourSize,AProblem problem){
		if(null == instance)
			instance = new CMOP(popSize,neighbourSize,problem);
		return instance;
	}
	
	
	public static AMOP getInstance() throws Exception{
		return new CMOP(popSize,neighbourSize,problem);
	}
	
	
	@Override
	public void initial() {
		//idealPoint = new double[objectiveDimesion];
		for(int i = 0; i < objectiveDimesion; i ++)
			idealPoint[i] = 1.0e+30;
		
		generateInitialPop();
		initNeighbour(neighbourSize);

		evaluateAfterInitial();

		// need to put the part of calculate the IGD  Nov 11
		// especially add after evalute the objectiveValue because IGD calculation need this data !

	}


	private void evaluateAfterInitial() {
		for(int i = 0 ;i < sops.size(); i ++){
			sops.get(i).ind.evaluate(problem);
		}
	}

	private double calcIGD() {
		double distanceIGD = 0.0;
		for (int i  = 0 ; i < realMop.size(); i ++) {
			double minDistance = 1.0e+10;
			for (int j = 0 ; j < popSize; j ++) {
				double d = distance(realMop.chromosomes.get(i).objectiveValue,chromosomes.get(i).objectiveValue);
				if(d < minDistance) minDistance = d;
			}
			distanceIGD += minDistance;
		}
		distanceIGD /= popSize;
		return distanceIGD;
	}

	
	// initial the neighbour point for neighbour's subproblems. Nov 11.
	private void initNeighbour(int neighbourNum) {
		for (int i = 0; i <  popSize ; i ++) {
			sops.get(i).getVicinity(vicinityRange,hyperplaneIntercept);
		}
	}

	private static double distance(double[] weight1, double[] weight2) {
		double sum = 0;
		for (int i = 0; i < weight1.length; i++) {
			sum += Math.pow((weight1[i] - weight2[i]), 2);
		}
		return Math.sqrt(sum);
	}

	private void initWeight() {
		//weights = new ArrayList<double[]>();
		for (int i = 0; i <= popSize; i++) {
			if (objectiveDimesion == 2) {
				double[] weight = new double[2];
				weight[0] = i / (double) popSize;
				weight[1] = (popSize - i) / (double) popSize;
				weights.add(weight);
			} else if (objectiveDimesion == 3) {
				int parts_num = 0;
				for(int f = 0; f <= popSize/2; f ++){
					if(popSize == f*(f-1)/2){
							parts_num = f;
							break;
					}
				}
				for (int j = 0; j <= parts_num; j++) {
					if (i + j <= parts_num) {
						int k = parts_num - i - j;
						double[] weight = new double[3];
						weight[0] = i / (double) parts_num;
						weight[1] = j / (double) parts_num;
						weight[2] = k / (double) parts_num;
						weights.add(weight);
					}
				}
			}
		}
		/*for (int i = 0; i <= 27; i++) {
			if (objectiveDimesion == 2) {
				double[] weight = new double[2];
				weight[0] = i / (double) popSize;
				weight[1] = (popSize - i) / (double) popSize;
				weights.add(weight);
			} else if (objectiveDimesion == 3) {
				for (int j = 0; j <= 27; j++) {
					if (i + j <= 27) {
						int k = 27 - i - j;
						double[] weight = new double[3];
						weight[0] = i / (double) 27;
						weight[1] = j / (double) 27;
						weight[2] = k / (double) 27;
						weights.add(weight);
					}
				}
			}
		}*/
	}


	

	// generate population for CHEA. Nov 11
	// modify to add sops Nov 14
	void generateInitialPop() {
		//sops = new ArrayList<SOP>(popSize);
		for(int i = 0 ; i < popSize; i ++){
			sop = new SOP(CMoChromosome.createChromosome());
			sops.add(sop);
		}
	}
	
/*	
	@Override
	void generateInitialPop() {
		chromosomes = new ArrayList<MoChromosome>(popSize);
		for(int i = 0; i < popSize; i ++)
		{
			chromosomes.add(CMoChromosome.createChromosome());
		}
	}
*/

	private MoChromosome reproductionByCrossoverMutate(int i){
		int k, l;
		do
			k = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		while (k == i);
		do
			l = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		while (l == k || l == i);
		
		CMoChromosome chromosome1 =(CMoChromosome)chromosomes
				.get(k);
		CMoChromosome chromosome2 = (CMoChromosome)chromosomes
				.get(l);
		
		MoChromosome offSpring = new CMoChromosome();
		offSpring.crossover((MoChromosome)chromosome1,(MoChromosome)chromosome2);
		offSpring.mutate(1d / offSpring.genesDimesion);
		return offSpring;
	}
	
	private MoChromosome diffReproduction(int i){
		int k, l, m;
		do{
			k = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		}
		while (k == i);
		do{
			l = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		}
		while (l == k || l == i);
		do{
			m = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		}
		while (m == l || m == k || m == i);

		MoChromosome chromosome1 = (MoChromosome)chromosomes
				.get(k);
		MoChromosome chromosome2 = (MoChromosome)chromosomes
				.get(l);
		MoChromosome chromosome3 = (MoChromosome)chromosomes
				.get(m);

		// generic operation crossover and mutation.
		MoChromosome offSpring = (MoChromosome)CMoChromosome.getEmptyChromosome();
		offSpring.diff_xover(chromosome1,chromosome2,chromosome3);
		offSpring.mutate(1d / offSpring.genesDimesion);
//		offSpring.mutate(1);
		return offSpring;
	}
	
	private MoChromosome reproduction(int i) {
		int k, l, m;
		do
			k = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		while (k == i);
		do
			l = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		while (l == k || l == i);
		do
			m = neighbourTable.get(i)[PRNG.nextInt(0,
					neighbourSize - 1)];
		while (m == l || m == k || m == i);

		CMoChromosome chromosome1 = (CMoChromosome)chromosomes
				.get(k);
		CMoChromosome chromosome2 = (CMoChromosome)chromosomes
				.get(l);
		CMoChromosome chromosome3 = (CMoChromosome)chromosomes
				.get(m);

		// generic operation crossover and mutation.
		MoChromosome offSpring = (MoChromosome)CMoChromosome.createChromosome();
		MoChromosome current = (MoChromosome)chromosomes.get(i);
		int D = offSpring.genesDimesion;
		double jrandom = Math.floor(Math.random() * D);

		for (int index = 0; index < D; index++) {
			double value = 0;
			if (Math.random() < CR || index == jrandom)
				value = chromosome1.genes[index]
						+ F
						* (chromosome2.genes[index] - chromosome3.genes[index]);
			else
				value = current.genes[index];
			// REPAIR.

			double high = 1;
			double low = 0;
			if (value > high)
				value = high;
			else if (value < low)
				value = low;

			offSpring.genes[index] = value;
		}

		offSpring.mutate(1d / offSpring.genesDimesion);
//		offSpring.mutate(1d / popSize);
		return offSpring;
	}

	
	
	// update Pop part is main to excute the evolustion. Nov 14
	@Override
	public void updatePop() {
		boolean isUpdate = false;
		int len = 0 ;

		// need to add a part about calculating the IGD every 25 gen or 10 gen Nov 11
		for(int i = 0 ;i < popSize; i ++){
			// this is MOEAD part ; delete evolveNewInd(i);
			// select two indivduals to reproduce a new offspring. Nov 11
			int parentIndex1 = 0;
			int parentIndex2 = 0;
			int b = len % (popSize/7); 
			if(b < hyperplaneInterceptNum) {
				parentIndex1 =  hyperplaneInterceptPoints[b];
			} else {
				parentIndex1 = tourSelectionHV(sops);
			}
			parentIndex2 = tourSelectionHV(sops);
			MoChromosome offSpring = new CMoChromosome();
			offSpring.crossover((MoChromosome)sops.get(parentIndex1).ind,(MoChromosome)sops.get(parentIndex2).ind);
			offSpring.mutate(1d/offSpring.genesDimesion);
			
			offSpring.evaluate(problem);
			updatePoints(offSpring);
			
			len ++;	

		}

		// leave empty place for IGD
	}

	// updatePoints including reference points and extrem points. Nov 11
	private void updatePoints(MoChromosome offSpring) {}

	// tour select two points as parents for reproduction.  Nov 11
	private int tourSelectionHV(List<SOP> sops) {
		int p1 = int(PRNG.nextDouble() * popSize);
		int p2 = int(PRNG.nextDouble() * popSize);
		double hv1 = tourSelectionHVDifference(p1,sops);
		double hv2 = tourSelectionHVDifference(p2,sops);
		if(hv1 >= hv2) return p1;
		else return p2;
	}

	private int tourSelectionHVDifference(int p,List<SOP> sops){
			int num = 0 ;
			int index ;
			double hvSide = 0.0;
			double hvDifference = 0.0;
			
			// need to add a sub-problem class , CHEA must have a sub problem  Nov 13
			while(sops.get(i).ind.belongSubproblemIndex != sops.get(i).sectorialIndex) {
				p = sops.get(i).ind.belongSubproblemIndex;
			}
			SOP subproblem = sops.get(p);
			int subproblemNeighbourSize  = subproblem.neighbour.size();
			double hv0 = getHyperVolume(sops.get(p).ind, referencePoint);
			for(int i = 0 ; i < subproblemNeighbourSize; i ++) {
				SOP sop = sops.get(subproblem.neighbour.get(i));
				if( sop.sectorialIndex == sop.ind.belongSubproblemIndex) {
					hvSide = getHyperVolume(sop.ind, referencePoint);
					hvDifference += (hv0 - hvSide);
					num ++;
				}
			}
			if(num != 0) hvDifference = hvDifference/num;
			return hvDifference;
			belongSubproblemIndex;
	}


	double getHyperVolume(MoChromosome ind , double[] referencePoint) {
		double volume = 1;
		for(int j = 0 ; j < objectiveDimesion; j ++) volume *= (referencePoint - ind.objectiveValue[j]);
		return volume;
	}


	private void evolveNewInd(int i) {
		
		MoChromosome offSpring = diffReproduction(i);
		improve(i,offSpring);
		offSpring.evaluate(problem);
		updateReference(offSpring);
		updateNeighbours(i,offSpring);
	}

	private void updateReference(MoChromosome offSpring){
		for(int j = 0 ; j < offSpring.objectiveDimesion; j ++){
			if(offSpring.objectiveValue[j] < idealPoint[j]){
				idealPoint[j] = offSpring.objectiveValue[j];
			}
		}
	}
	
	private void updateNeighbours(int i, MoChromosome offSpring) {
		for(int j = 0 ; j < neighbourSize; j ++){
			int neighbourIndex = neighbourTable.get(i)[j];
			MoChromosome neighbourSolution = chromosomes.get(neighbourIndex);
			double o = scalarOptimization(neighbourIndex,offSpring);
			double n = scalarOptimization(neighbourIndex,neighbourSolution);
			if(o < n){
				offSpring.copyTo(neighbourSolution);
			}
		}
	}
	
	private double scalarOptimization(int subproblemIndex, MoChromosome chrom) {
		double[] namda = weights.get(subproblemIndex);
		
		return techScalarObj(namda, chrom);
	}

	private double techScalarObj(double[] namda, MoChromosome chrom) {
		double max_fun = -1 * Double.MAX_VALUE;
		for(int n = 0; n < objectiveDimesion; n ++){
			double val = Math.abs(chrom.objectiveValue[n] - idealPoint[n]);
			if(0 == namda[n])
				val *= 0.00001;
			else
				val *= namda[n];
			if(val > max_fun)
				max_fun = val;
		}
		chrom.fitnessValue = max_fun;
		return max_fun;
	}


	private void improve(int i,MoChromosome offSpring) {
//		do nothing
	}
	
	
	public void write2File(String fileName) throws IOException{
		File file = new File(fileName);
		if(!file.exists()){
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int n = 0 ; n < popSize; n ++){
			StringBuffer sb = new StringBuffer();
			for(int od = 0; od < objectiveDimesion; od ++){
				if(0 != od)
					sb.append(" ");
				sb.append(chromosomes.get(n).objectiveValue[od]);
			}
			if(n != popSize)
				sb.append("\n");
			bw.write(sb.toString());
		}
		bw.close();
		fw.close();
	}

}
