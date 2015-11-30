package mop;


import problems.AProblem;


public abstract class MoChromosome {
	
	public static final double EPS = 1.2e-7;

	private static final long serialVersionUID = 1L;
	public static int objectiveDimesion;
	public static int genesDimesion;
	public double[] genes;
	public double[] objectiveValue;
	public static int[][] range;


	public double kValue ;
	public int hyperplaneIntercept;
	public int belongSubproblemIndex;


	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(
				"Chromosome cannot be cloned directly, it must be created from a pool");
	}

	public void copyTo(MoChromosome copyto) {
		//copyto.fitnessValue = this.fitnessValue;
		copyto.objectiveDimesion = this.objectiveDimesion;
		copyto.genesDimesion = this.genesDimesion;
		copyto.hyperplaneIntercept = this.hyperplaneIntercept;
		copyto.kValue = this.kValue;
		copyto.belongSubproblemIndex = this.belongSubproblemIndex;
		System.arraycopy(objectiveValue, 0, copyto.objectiveValue, 0,
				objectiveValue.length);
		System.arraycopy(genes, 0, copyto.genes, 0,
				genes.length);
		/*
		for(int i = 0 ; i < this.range.length; i ++) {
			System.arraycopy(range[i], 0, copyto.range[i], 0,
				range[i].length);
		}
		*/
	}

	public abstract double parameterDistance(MoChromosome another);

	public static double objectiveDistance(MoChromosome ch1, MoChromosome ch2) {
		double sum = 0;
		for (int i = 0; i < ch1.objectiveValue.length; i++) {
			sum += Math.pow(ch1.objectiveValue[i] - ch2.objectiveValue[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	public abstract void calKVal(double[] idealPoint,int hyperplaneIntercept);
	//public abstract double[] calVObj(double[] idealPoint,int hyperplaneIntercept);
	public abstract int[] calVObj(double[] idealPoint,int hyperplaneIntercept);
	public abstract void calMoChObjValue(double[] idealPoint,int hyperplaneIntercept);
	public abstract double[] calNormailize(double[] idealPoint, int hyperplaneIntercept);
	//public abstract int getIndexFromVObj(double[] vObj, int hyperplaneIntercept);
	public abstract int getIndexFromVObj(int[] vObj, int hyperplaneIntercept);
	public abstract void objIndex(double[] idealPoint,int hyperplaneIntercept);

	public abstract void evaluate(AProblem problem);
	
	public abstract void mutate(double mutationrate);
	public abstract void diff_xover(MoChromosome ind0, MoChromosome ind1,MoChromosome ind2);
	public abstract void crossover(MoChromosome ind0, MoChromosome ind1);
	public abstract String vectorString();
	
	public abstract String getParameterString();

	public abstract int compareInd(MoChromosome ind2);

	public int getObjectiveDimesion() {
		return objectiveDimesion;
	}
}
