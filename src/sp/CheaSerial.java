package sp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import mop.MOP;
import mop.SOP;
import mop.CHEAMOP;
import mop.MopData;


import problems.AProblem;
import problems.DTLZ2;
import problems.DTLZ1;
import utilities.StringJoin;
import utilities.WrongRemindException;



public class CheaSerial {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException, WrongRemindException {
        int popSize = 100;
        //int hyperplaneIntercept = 27;
        int hyperplaneIntercept = popSize - 1;
        int neighbourNum = 2;
        int iterations = 400;
        int writeTime = 1;
        int innerLoop = 1;
        int loopTime = iterations / (writeTime * innerLoop);
        AProblem problem = DTLZ1.getInstance();
        MOP mop = CHEAMOP.getInstance(popSize, problem , hyperplaneIntercept, neighbourNum);
        mop.initial();

		MopData mopData = new MopData(mop,problem);
		String mopStr = mopData.mop2Str();

		List<String> pStr = new ArrayList<String>();
		List<String> mopList = new ArrayList<String>();
		System.out.println("Timer start!!!");

		for (int i = 0; i < loopTime; i ++) {
			mopData.clear();
			mopData.str2Mop(mopStr);
			mopData.mop.initPartition(1);
			mopData.mop.updatePop(1);
			System.out.println("mop's referencePoint = " + StringJoin.join(" " ,mopData.mop.referencePoint) + ",idealPoint = " + StringJoin.join(" ",mopData.mop.idealPoint));
			mopStr = mopData.mop2Str();
		}
	    String filename = "/home/laboratory/workspace/moead_parallel/experiments/chea_serial.txt";
	    mopData.mop.write2File(filename);
		/*
    	String content = null;
    	List<String> col = new ArrayList<String>();
        for(int j = 0 ; j < mopData.mop.sops.size(); j ++) {
		    col.add(StringJoin.join(" ",mopData.mop.sops.get(j).ind.objectiveValue));
		}
    	content = StringJoin.join("\n", col);
    	mopData.write2File("/home/laboratory/workspace/moead_parallel/experiments/parallel/spark_chea.txt",content);
		*/
	}
}
