package mop;

import utilities.StringJoin;

import problems.AProblem;
//import org.apache.commons.Math.random.RandomGenerator;

public class CMoChromosome extends MoChromosome {
	
	public static final int id_cx = 20;
	public static final int id_mu = 20;
	

	protected void randomizeParameter() {
		for (int i = 0; i < genesDimesion; i++) {
			genes[i] = PRNG.nextDouble(range[i][0],range[i][1]);
		}
	}
	
	public CMoChromosome() {
		genesDimesion = AProblem.genesDimesion;
		objectiveDimesion = AProblem.objectiveDimesion;
		range = AProblem.range;
		objectiveValue = new double[objectiveDimesion];
		genes = new double[genesDimesion];
	}
	
	public static CMoChromosome getEmptyChromosome() {
		return new CMoChromosome();
	}
	
	public static CMoChromosome createChromosome() {
		CMoChromosome mc = new CMoChromosome();
		mc.randomizeParameter();
		return mc;
	}
	
	public void evaluate(AProblem problem){
		problem.evaluate(genes,objectiveValue);
	}

	public double[] calNormailize(double[] idealPoint, int hyperplaneIntercept) {
		double objRelativeSum = 0.0;
		double[] normailizedf = new double[objectiveDimesion];
		for( int j = 0; j < objectiveDimesion; j ++) {
			normailizedf[j] = objectiveValue[j] - idealPoint[j];
			objRelativeSum += normailizedf[j];
		} 
		if(objRelativeSum == 0) {
			objRelativeSum  = hyperplaneIntercept;
			normailizedf[0] = hyperplaneIntercept;
			for ( int j = 1; j < objectiveDimesion; j ++) {
					normailizedf[j] = 0.0;
			}
		} else {
			for (int j = 0 ; j < objectiveDimesion; j ++) {
				normailizedf[j] = normailizedf[j] / objRelativeSum * hyperplaneIntercept;
			}
		}
		kValue = objRelativeSum;
		return normailizedf;
	}

	// calc objectiveValue from int array , maybe wrong Nov 19
	public void calMoChObjValue(double[] idealPoint, int hyperplaneIntercept) {
		//objectiveValue = calVObj(idealPoint,hyperplaneIntercept);
		int[] vObj = calVObj(idealPoint,hyperplaneIntercept);
		for(int i = 0 ; i < vObj.length; i ++) objectiveValue[i] = vObj[i] + 0.0;
	}

	//public double[] calVObj(double[] idealPoint,int hyperplaneIntercept) { public int[] calVObj(double[] idealPoint,int hyperplaneIntercept) {
		//double[] vValue = new double[objectiveDimesion];
	public int[] calVObj(double[] idealPoint,int hyperplaneIntercept) {
		int[] vValue = new int[objectiveDimesion];
		double[] normailizedf = calNormailize(idealPoint,hyperplaneIntercept);
		boolean[] isCompleteBit = new boolean[objectiveDimesion];
		for ( int j = 0 ;j < objectiveDimesion; j ++) isCompleteBit[j] = false;
		int size = objectiveDimesion;
		int minIndex = 0;
		double minValue = 1e7;
		double[] lowDif = new double[objectiveDimesion];
		double[] upDif = new double[objectiveDimesion];
		double fix;
		double selectData;
		for (int i = 0; i < objectiveDimesion; i ++) {
			lowDif[i] = (double)Math.floor(normailizedf[i]) - normailizedf[i];
			upDif[i] = 1.0 + lowDif[i];
			selectData = (-lowDif[i]) < upDif[i] ? (-lowDif[i]) : upDif[i];
			if ( minValue > selectData) {
				minIndex = i;
				minValue = selectData;
			}
		}

		while(true) {
			selectData = (( -lowDif[minIndex] ) < upDif[minIndex] ? lowDif[minIndex] : upDif[minIndex]);
			vValue[minIndex] = (int)(selectData < 0 ? Math.floor(normailizedf[minIndex]) : Math.ceil( normailizedf[minIndex]));
			isCompleteBit[minIndex] = true;
			size --;

			if( size == 1) {
				int sum = 0 ;
				int leftIndex = 0 ;
				for (int i = 0; i < objectiveDimesion; i ++) {
					if (isCompleteBit[i]) sum += vValue[i];
					else leftIndex = i;
				}
				vValue[leftIndex] = hyperplaneIntercept - sum;
				break;
			}
			fix = selectData / size;
			minValue = 1e7;
			for(int i = 0 ; i < objectiveDimesion; i ++) {
				if (isCompleteBit[i] ) continue;
				lowDif[i] += fix;
				upDif[i] += fix;
				selectData = ( -lowDif[i] ) < upDif[i] ? -lowDif[i] : upDif[i];
				if ( minValue > selectData ) {
					minIndex = i;
					minValue = selectData;
				}
			}
		}
		/*
		for(int i = 0; i < objectiveDimesion; i ++) {
			isCompleteBit[i] = false;
			lowDif[i] = 0;
			upDif[i] = 0;
			normailizedf[i] = 0;
		}
		*/
		return vValue;
	}

	public void calKVal(double[] idealPoint,int hyperplaneIntercept) {
		kValue = 0.0;
		for (int j = 0 ; j < objectiveDimesion; j ++) 
				kValue += ( objectiveValue[j] - idealPoint[j]);
		if (0 == kValue) kValue = hyperplaneIntercept;
	}


	// calc the belongSubproblemIndex (INDEX) Nov 18
	public void objIndex(double[] idealPoint,int hyperplaneIntercept) {
		//double[] vObj;
		int[] vObj;
		vObj = calVObj(idealPoint,hyperplaneIntercept);
		belongSubproblemIndex = getIndexFromVObj(vObj,hyperplaneIntercept);
		//System.out.println("vObj = " + StringJoin.join(" ",vObj) + ", belongSubproblemIndex = " + belongSubproblemIndex);
	}

	public int getIndexFromVObj(int[] vObj, int hyperplaneIntercept) {
		if ( 2 == objectiveDimesion ) return vObj[0] ;
		else if ( 3 == objectiveDimesion ) return (hyperplaneIntercept - vObj[2] + 1) *  (hyperplaneIntercept - vObj[2] ) / 2 + vObj[0];
		int[] h = new int[objectiveDimesion-1];
		h[0] = hyperplaneIntercept - vObj[objectiveDimesion-1];
		for (int i = 1; i < objectiveDimesion -1; i ++) {
			h[i] = h[i-1] - vObj[objectiveDimesion-1-i];
		}
		int resultIndex = 0;
		for (int i = 0 ; i < objectiveDimesion - 1 ; i ++) {
			resultIndex += choose(h[i] + objectiveDimesion -2 -i , objectiveDimesion - 1 -i);
		}
		for(int i = 0 ; i < objectiveDimesion -1 ; i ++) h[i] = 0;
		return resultIndex;
	}

	int choose(int n,int m) {
		if (m > n) return 0;
		return (int) Math.floor(0.5 + Math.exp(lnchoose(n,m)));
	}

	double lnchoose(int n, int m) {
		if (m > n) return 0;
		if (m < n/2.0) m = n - m;
		double s1 =  0;
		for( int i = m + 1; i <= n; i ++) s1 += Math.log((double)i);
		double s2 = 0; 
		int ub = n - m;
		for (int i = 2; i <= ub; i ++) s1 += Math.log((double)i);
		return (s1 - s2);
	}

	
	@Override
	public void diff_xover(MoChromosome ind0, MoChromosome ind1,
			MoChromosome ind2){
		int nvar = ind0.range.length;
		// int idx_rnd = this.randomGenerator.nextInt(nvar);
		double rate = 0.5;
		for (int n = 0; n < nvar; n++) {
			/* Selected Two Parents */
			double lowBound = ind0.range[n][0];
			double upBound = ind0.range[n][1];
			genes[n] = ((CMoChromosome) ind0).genes[n]
					+ rate
					* (((CMoChromosome) ind2).genes[n] - ((CMoChromosome) ind1).genes[n]);

			if (genes[n] < lowBound) {
				genes[n] = PRNG.nextDouble(lowBound, upBound);
			}
			if (genes[n] > upBound) {
				genes[n] = PRNG.nextDouble(lowBound, upBound);
			}
		}
	}
	
	public void crossover(MoChromosome p1, MoChromosome p2){
		double rand;
		double y1, y2, yl, yu;
		double c1, c2;
		double alpha, beta, betaq;
		double eta_c = id_cx;

		CMoChromosome parent1 = (CMoChromosome) p1;
		CMoChromosome parent2 = (CMoChromosome) p2;
		int numVariables = p1.range.length;
		if (PRNG.nextDouble() <= 1.0) {
			for (int i = 0; i < numVariables; i++) {
				if (PRNG.nextDouble() <= 0.5) {
					if (Math.abs(parent1.genes[i] - parent2.genes[i]) > EPS) {
						if (parent1.genes[i] < parent2.genes[i]) {
							y1 = parent1.genes[i];
							y2 = parent2.genes[i];
						} else {
							y1 = parent2.genes[i];
							y2 = parent1.genes[i];
						}
						yl = p1.range[i][0];
						yu = p1.range[i][1];
						rand = PRNG.nextDouble();
						beta = 1.0 + (2.0 * (y1 - yl) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));
						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow((rand * alpha),
									(1.0 / (eta_c + 1.0)));
						} else {
							betaq = Math.pow((1.0 / (2.0 - rand * alpha)),
									(1.0 / (eta_c + 1.0)));
						}
						c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
						beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));
						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow((rand * alpha),
									(1.0 / (eta_c + 1.0)));
						} else {
							betaq = Math.pow((1.0 / (2.0 - rand * alpha)),
									(1.0 / (eta_c + 1.0)));
						}
						c2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));
						if (c1 < yl)
							c1 = yl;
						if (c2 < yl)
							c2 = yl;
						if (c1 > yu)
							c1 = yu;
						if (c2 > yu)
							c2 = yu;
						if (PRNG.nextDouble() <= 0.5) {
							genes[i] = c2;
						} else {
							genes[i] = c1;
						}
					} else {
						genes[i] = parent1.genes[i];
					}
				} else {
					genes[i] = parent1.genes[i];
				}
			}
		} else {
			for (int i = 0; i < numVariables; i++) {
				genes[i] = parent1.genes[i];
			}
		}
	}
	

	@Override
	public double parameterDistance(MoChromosome another) {
		return 0;
	}

	@Override
	public String vectorString() {
		return null;
	}

	@Override
	public String getParameterString() {
		return null;
	}

	@Override
	public void copyTo(MoChromosome copyto) {
		super.copyTo(copyto);
	}

	@Override
	public void mutate(double mutationrate) {
		double rnd, delta1, delta2, mut_pow, deltaq;
		double y, yl, yu, val, xy;
		double eta_m = 20;

		for (int j = 0; j < genesDimesion; j++) {
			if (PRNG.nextDouble() <= mutationrate) {
				y = genes[j];
				yl = range[j][0];
				yu = range[j][1];

				delta1 = (y - yl) / (yu - yl);
				delta2 = (yu - y) / (yu - yl);

				rnd = PRNG.nextDouble();
				mut_pow = 1.0 / (eta_m + 1.0);
				if (rnd <= 0.5) {
					xy = 1.0 - delta1;
					val = 2.0 * rnd + (1.0 - 2.0 * rnd)
							* (Math.pow(xy, (eta_m + 1.0)));
					deltaq = Math.pow(val, mut_pow) - 1.0;
				} else {
					xy = 1.0 - delta2;
					val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5)
							* (Math.pow(xy, (eta_m + 1.0)));
					deltaq = 1.0 - (Math.pow(val, mut_pow));
				}
				y = y + deltaq * (yu - yl);
				if (y < yl)
					y = yl;
				if (y > yu)
					y = yu;
				genes[j] = y;
			}
		}
		
	}

	// equal : 0
	// dominating : 1
	// dominated : 2
	// non-dominated : 3
	public int compareInd(MoChromosome ind2) {
		boolean bBetter = false;
		boolean bWorst = false;
		int i = 0;
		do {
			if(objectiveValue[i] < ind2.objectiveValue[i]) 
				bBetter = true;
			if(objectiveValue[i] > ind2.objectiveValue[i])
				bWorst = true;
			i++;
		} while (!(bWorst && bBetter) && i < objectiveDimesion);
	
		if(bWorst) {
			if(bBetter) return 3;
			else return 2;
		} else {
			if(bBetter) return 1;
			else return 0;
		}
	}
}
