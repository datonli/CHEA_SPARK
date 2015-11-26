package chea;

import java.io.IOException;

import mop.MOP;
import mop.CHEAMOP;
import mop.IGD;

import problems.AProblem;
import problems.DTLZ1;
import problems.DTLZ2;



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
			double[] genDisIGD = new double[2];
			genDisIGD[0] = gen;
			genDisIGD[1] = igdOper.calcIGD(mop.sops);
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
			int iterations = 800;
			int neighbourNum = 2;		
			AProblem problem = DTLZ1.getInstance();
			MOP mop = CHEAMOP.getInstance(popSize,problem,hyperplaneIntercept,neighbourNum);
			mop.initial();
	        String filename = "/home/laboratory/workspace/moead_parallel/experiments/chea_init.txt";
	        mop.write2File(filename);
			for(int i = 0 ; i < iterations; i ++) {
				System.out.println("The " + i + "th iteration !!");
				mop.updatePop(1);
			}
	        filename = "/home/laboratory/workspace/moead_parallel/experiments/chea.txt";
	        mop.write2File(filename);
		    System.out.println("done!");
		}
}

