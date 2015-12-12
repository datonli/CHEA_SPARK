package chea;

import java.io.IOException;
import java.io.FileWriter;

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


		public static void main(String[] args) throws IOException,InterruptedException {
			int popSize = 105;
			int hyperplaneIntercept = 13;
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
	        long startTime = System.currentTimeMillis();
	        long igdTime = 0;
			long sleepTime = 0;
			for(int i = 0 ; i < iterations; i ++) {
				long sleepTimeStart = System.currentTimeMillis();
				//Thread.sleep(2500);
				sleepTime += System.currentTimeMillis() - sleepTimeStart;
				System.out.println("The " + i + "th iteration !!");
				mop.updatePop(1);
				long igdStartTime = System.currentTimeMillis();
				List<double[]> real = new ArrayList<double[]>(mop.sops.size());
				for(int j = 0; j < mop.sops.size(); j ++) {
					real.add(mop.sops.get(j).ind.objectiveValue);
				}
				double[] genDisIGD = new double[2];
				genDisIGD[0] = i;
				genDisIGD[1] = igdOper.calcIGD(real);
				igdOper.igd.add(genDisIGD);
				igdTime += System.currentTimeMillis() - igdStartTime ;
			}
			long recordTime = System.currentTimeMillis()-startTime - igdTime;
			System.out.println("Running time is : " + recordTime);
			System.out.println("Sleep time is : " + sleepTime);
			recordTimeFile("/home/laboratory/workspace/moead_parallel/experiments/recordTime.txt","\nDTLZ1 chea serial time : " + recordTime + ",sleepTime is : " + sleepTime);

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

		  public static void recordTimeFile(String filename,String str) throws IOException {
		       FileWriter writer = new FileWriter(filename,true);
		       writer.write(str);
		       writer.close();
		  }
}

