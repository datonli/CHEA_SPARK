package chea;

import java.io.IOException;

import mop.MOP;
import mop.CHEAMOP;
import mop.IGD;

import problems.AProblem;
import problems.DTLZ1;
import problems.CDV;

import java.util.List;
import java.util.ArrayList;

public class chea {

    public void chea(MOP mop, int iterations) {
		int innerTime = 1;
		//initial IGD calc Nov 19
		IGD igdOper = new IGD(1500);
		String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ1(3).dat";
		try {
			igdOper.ps = igdOper.loadPfront(filename);
		} catch (IOException e) {
		}

		for(int gen = 1 ; gen <= iterations; gen ++) {
			mop.updatePop(innerTime);
			// calc igd , using ps and sops's objectiveValue 
			// Nov 21
			List<double[]> real = new ArrayList<double[]>(mop.sops.size());
			for(int i = 0; i < real.size(); i ++) {
				real.add(mop.sops.get(i).ind.objectiveValue);
			}
			double[] genDisIGD = new double[2];
			genDisIGD[0] = gen;
			genDisIGD[1] = igdOper.calcIGD(real);
			igdOper.igd.add(genDisIGD);

			// add IGD value into a datastruct Nov 19
		}
		filename = "/home/laboratory/workspace/moead_parallel/experiments/CHEA_IGD_DTLZ1_3.txt";
		try {
			igdOper.saveIGD(filename);	
		} catch (IOException e) {
		}
	}


		public static void main(String[] args) throws IOException{
			int popSize = 406;
			int hyperplaneIntercept = 27;
			//int hyperplaneIntercept = popSize - 1;
			int iterations = 400;
			int neighbourNum = 2;		
			AProblem problem = DTLZ1.getInstance();
			//AProblem problem = CDV.getInstance();
			MOP mop = CHEAMOP.getInstance(popSize,problem,hyperplaneIntercept,neighbourNum);
			mop.initial();
			IGD igdOper = new IGD(1500);
			String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ1(3).dat";
			try {
				igdOper.ps = igdOper.loadPfront(filename);
			} catch (IOException e) {}
			for(int i = 0 ; i < iterations; i ++) {
				//System.out.println("The " + i + "th iteration !!");
				mop.updatePop(1);
				List<double[]> real = new ArrayList<double[]>(mop.sops.size());
				for(int j = 0; j < mop.sops.size(); j ++) {
					real.add(mop.sops.get(j).ind.objectiveValue);
				}
				double[] genDisIGD = new double[2];
				genDisIGD[0] = i;
				genDisIGD[1] = igdOper.calcIGD(real);
				igdOper.igd.add(genDisIGD);
			}
	      	filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/CHEA_IGD_DTLZ1_3.txt";
			try {
				igdOper.saveIGD(filename);	
			} catch (IOException e) {}
			filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/chea.txt";
	        mop.write2File(filename);
			filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/chea_all.txt";
	        mop.writeAll2File(filename);
		    System.out.println("done!");
		}
}

