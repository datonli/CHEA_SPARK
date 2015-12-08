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
import mop.IGD;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.NLineInputFormat;

import problems.AProblem;
import problems.DTLZ4;
import utilities.StringJoin;
import utilities.WrongRemindException;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;


public class CheaSpPartition {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException, WrongRemindException {
        int popSize = 406;
        int hyperplaneIntercept = 27;
        //int hyperplaneIntercept = popSize - 1;
        int neighbourNum = 2;
        int iterations = 800;
        int partitionNum = 2;
        int innerLoop = 10;
		int writeTime = 1;
        int loopTime = iterations / (writeTime * innerLoop);
        AProblem problem = DTLZ4.getInstance();
        MOP mop = CHEAMOP.getInstance(popSize, problem , hyperplaneIntercept, neighbourNum);
        mop.initial();
		// comment original code for running serial.
		// MOEAD.moead(mop,iterations);
		
		
		// Oct 30  writing another mopData to use as multi pop in one file.
		//MopData mopData = new MopData(mop);
		
		
		HdfsOper hdfsOper = new HdfsOper();
		hdfsOper.rm("spark/");

		MopData mopData = new MopData(mop,problem);
		String mopStr = mopData.mop2Str();
		//System.out.println("mopStr is : \n" + mopStr);

		SparkConf sparkConf = new SparkConf().setAppName("chea spark");
		JavaSparkContext cxt = new JavaSparkContext(sparkConf);

		long startTime = System.currentTimeMillis();
		List<String> pStr = new ArrayList<String>();
		List<String> mopList = new ArrayList<String>();

        IGD igdOper = new IGD(1500);
        String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ4(3).dat";
        try {
            igdOper.ps = igdOper.loadPfront(filename);
        } catch (IOException e) {}


		System.out.println("Timer start!!!");
		for (int i = 0; i < loopTime; i++) {
			System.out.println("The " + i + "th time!");
			//Thread.sleep(2500);
			pStr.clear();
			
			mopData.clear();
			mopData.str2Mop(mopStr);

            List<double[]> real = new ArrayList<double[]>(mop.sops.size()); 
            for(int j = 0; j < mop.sops.size(); j ++) {
               real.add(mop.sops.get(j).ind.objectiveValue);
            }
            double[] genDisIGD = new double[2];
            genDisIGD[0] = i*innerLoop;
            genDisIGD[1] = igdOper.calcIGD(real);
            igdOper.igd.add(genDisIGD);

			mopData.mop.initPartition(partitionNum);
			System.out.println(mopStr);
			for(int j = 0; j < partitionNum; j ++) {
				mopData.mop.setPartitionArr(j);
				mopStr = mopData.mop2Str();
				pStr.add(mopStr);
			}
			JavaRDD<String> p = cxt.parallelize(pStr,partitionNum);
			System.out.println("after union");
			JavaPairRDD<String,String> mopPair = p.mapPartitionsToPair(
													new PairFlatMapFunction<Iterator<String>,String,String>() {
															public Iterable<Tuple2<String,String>> call(Iterator<String> s) throws WrongRemindException{
																int aPopSize = 406;
																int aHyperplaneIntercept = 27;
																//int aHyperplaneIntercept = aPopSize - 1;
																int aNeighbourNum = 2;
																AProblem aProblem = DTLZ4.getInstance();
																MOP aMop = CHEAMOP.getInstance(aPopSize, aProblem, aHyperplaneIntercept,aNeighbourNum);
																aMop.allocateAll(aPopSize,aProblem.objectiveDimesion);
																MopData mmop = new MopData(aMop,aProblem);
																System.out.println("Map begin : ");

																// wrong in this place , Nov 26
																// it didn't work actully . I've found sops's size is zero
																String str = s.next();
																mmop.str2Mop(str);
																//System.out.println("str is : " + str);
																System.out.println("Mop end ! ");
																//System.out.println(mmop.mop.sops.size());
																//System.out.println(mmop.mop.idealPoint[0]);

																for(int i = 0 ; i < mmop.mop.sops.size(); i ++) {
																	//System.out.println("the " + i  + "th 's belongSubproblemIndex is : " + mmop.mop.sops.get(i).ind.belongSubproblemIndex);
																}
																// the second time is error happend ! Nov 27
																mmop.mop.updatePop(innerLoop);
																System.out.println("update Pop!");
																mmop.mop.updateSopIdealPoint(); 
																List<Tuple2<String,String>> lt = new ArrayList<Tuple2<String,String>>();
																for (int k = 0; k < mmop.mop.sops.size(); k ++) {
																		lt.add(new Tuple2<String,String>(String.valueOf(mmop.mop.sops.get(k).sectorialIndex),mmop.sop2Line(mmop.mop.sops.get(k))));
																}
																lt.add(new Tuple2<String,String>("111111111","111111111 " + mmop.mopAtr2Str()));
																mmop.clear();
																return lt;
															}
													}
											);
			/*
			List<Tuple2<String, String>> output = mopPair.collect();
			if(i == loopTime-1 )
			for(Tuple2<?,?> t : output) {
					//System.out.println("before reduce: " + t._1() + "##############" + t._2());
			}
			*/

			JavaPairRDD<String,String> mopPop = mopPair.reduceByKey(
														new Function2<String,String,String>() {
																public String call(String s1, String s2) throws WrongRemindException {
																		//System.out.println("enterrrrrrrrrrrrrrr reduce " );
																		//System.out.println("s1 = " + s1 + " ,\n s2 = " + s2);
																        AProblem problem = DTLZ4.getInstance();
																        int objectiveDimesion = problem.objectiveDimesion;
																		String[] s1split = s1.split(" ");
																		String[] s2split = s2.split(" ");
																		//System.out.println("s1 is : " + s1);
																		MopData mmopData = null;
																					
																		if("111111111".equals(s1split[0])) {
																			System.out.println("enter 111111111");
																			MOP mop1 = null;
																			MOP mop2 = null;
														                    try {
																				//System.out.println(s1split[1]);
														                        mop1 = str2MopAtr(s1split[1],objectiveDimesion);
														                        mop2 = str2MopAtr(s2split[1],objectiveDimesion);
														                        mop1 = compareAtr(mop1,mop2,objectiveDimesion);
														                    } catch (WrongRemindException e) {}
																			return "111111111 " + mopAtr2Str(mop1);
																		} else {
																			SOP sop1 = null;
																			SOP sop2 = null;
																			double[] idealPoint = new double[objectiveDimesion];
																			try {
																				//System.out.println("s1's is : " + s1);
																				sop1 = mmopData.str2Sop(s1);
																				//System.out.println("sop1's hyperplaneIntercept is : " + sop1.ind.hyperplaneIntercept);
																				//System.out.println("\n\n\nsop1 ind 's belong index is : " + sop1.ind.belongSubproblemIndex);
																				sop2 = mmopData.str2Sop(s2);
																				for(int i = 0 ; i < objectiveDimesion; i ++) idealPoint[i] = 1e+5;      
																				for(int i = 0 ; i < objectiveDimesion; i ++) {
																					if(sop1.idealPoint[i] < idealPoint[i]) idealPoint[i] = sop1.idealPoint[i];
																					if(sop2.idealPoint[i] < idealPoint[i]) idealPoint[i] = sop2.idealPoint[i];
																				}
																			} catch (WrongRemindException e) {}
																			sop1 = compareSops(sop1, sop2,idealPoint);
																			return mmopData.sop2Line(sop1);
																		}
																}


    private MOP str2MopAtr(String str,int objectiveDimesion) throws WrongRemindException {
        MOP cmop = new CHEAMOP(objectiveDimesion);
        String[] ss = str.split("_");
        if(12 != ss.length) throw new WrongRemindException("Wrong str2MopAtr");
        cmop.popSize = Integer.parseInt(ss[0]);
        cmop.hyperplaneIntercept = Integer.parseInt(ss[1]);
        cmop.neighbourNum = Integer.parseInt(ss[2]);
        cmop.perIntercept = Double.parseDouble(ss[3]);
        int r = 0;
        int c = 0;
        String[] anchorPointR = ss[4].split("#");
        r = anchorPointR.length;
        c = anchorPointR[0].split(",").length;
        double[][] a = new double[r][c];
        for(int i = 0 ; i < r; i ++) {
            String[] ap = anchorPointR[i].split(",");
            for(int j = 0; j < c ; j ++) {
                a[i][j] = Double.parseDouble(ap[j]);
            }
        }
        cmop.anchorPoint = a;
        cmop.trueNadirPoint = StringJoin.decodeDoubleArray("#",ss[5]);
        cmop.idealPoint = StringJoin.decodeDoubleArray("#",ss[6]);
        cmop.referencePoint = StringJoin.decodeDoubleArray("#",ss[7]);
        cmop.sizeSubpOnEdge = Integer.parseInt(ss[8]);
		MopData mmopData = null;
        cmop.subpIndexOnEdge = mmopData.IntArray2IntegerList(StringJoin.decodeIntArray("#",ss[9]));
        cmop.objectiveDimesion = Integer.parseInt(ss[10]);
        cmop.partitionArr = StringJoin.decodeIntArray("#",ss[11]); 
        return cmop;
    }

    public SOP compareSops(SOP s1,SOP s2,double[] idealPoint) {
        int objectiveDimesion = s1.ind.objectiveDimesion;
        int hyperplaneIntercept = s1.ind.hyperplaneIntercept;
        double[] refCal = new double[objectiveDimesion];
		
        s1.ind.calKVal(idealPoint,hyperplaneIntercept);
		s1.ind.objIndex(idealPoint,hyperplaneIntercept);
		s2.ind.calKVal(idealPoint,hyperplaneIntercept);
		s2.ind.objIndex(idealPoint,hyperplaneIntercept);

		/*
		for(int i = 0 ; i < objectiveDimesion; i ++) {
			refCal[i] = 1e+5;	
		}
        double c1 = MOP.getHyperVolume(s1.ind,refCal);
        double c2 = MOP.getHyperVolume(s2.ind,refCal);
        if(c1 > c2) {
           s2.ind.copyTo(s1.ind);
        }
        for(int i = 0; i < idealPoint.length; i ++) s1.idealPoint[i] = idealPoint[i];
        return s1;
		*/
	

		if(s1.sectorialIndex == s1.ind.belongSubproblemIndex && s2.sectorialIndex == s2.ind.belongSubproblemIndex) {
			if(s1.ind.kValue > s2.ind.kValue) {
        		for(int i = 0 ; i < objectiveDimesion; i ++) {
            		refCal[i] = (idealPoint[i] + s1.ind.kValue * (1/hyperplaneIntercept) * ( s1.vObj[i] + 1)) ; //s1.fixWeight[i]));
        		}
			} else {
				for(int i = 0 ; i < objectiveDimesion; i ++) {
            		refCal[i] = (idealPoint[i] + s2.ind.kValue * (1/hyperplaneIntercept) * ( s2.vObj[i] + 1)) ; //s1.fixWeight[i]));
        		}
			}
			double c1 = MOP.getHyperVolume(s1.ind,refCal);
    	    double c2 = MOP.getHyperVolume(s2.ind,refCal);
	        if(c1 > c2) {
				s2.ind.copyTo(s1.ind);
        	}
			for(int i = 0; i < idealPoint.length; i ++) s1.idealPoint[i] = idealPoint[i];
        	return s1;
		} else if(s1.sectorialIndex == s1.ind.belongSubproblemIndex && s2.sectorialIndex != s2.ind.belongSubproblemIndex) {
			for(int i = 0; i < idealPoint.length; i ++) s1.idealPoint[i] = idealPoint[i];
			return s1;
		} else if(s1.sectorialIndex != s1.ind.belongSubproblemIndex && s2.sectorialIndex == s2.ind.belongSubproblemIndex) {
			for(int i = 0; i < idealPoint.length; i ++) s2.idealPoint[i] = idealPoint[i];
			return s2;
		} else {
			for(int i = 0; i < idealPoint.length; i ++) s1.idealPoint[i] = idealPoint[i];
			return s1;
		}
    }

    private String mopAtr2Str(MOP cmop) {
        List<String> col = new ArrayList<String>(cmop.popSize + 1);
        col.add(String.valueOf(cmop.popSize));
        col.add(String.valueOf(cmop.hyperplaneIntercept));
        col.add(String.valueOf(cmop.neighbourNum));
        col.add(String.valueOf(cmop.perIntercept));
        List<String> tmp = new ArrayList<String>(); 
        for(int i = 0 ; i < cmop.anchorPoint.length; i ++) {
            tmp.add(StringJoin.join(",",cmop.anchorPoint[i]));
        }
        col.add(StringJoin.join("#",tmp));
        col.add(StringJoin.join("#",cmop.trueNadirPoint));
        col.add(StringJoin.join("#",cmop.idealPoint));
        col.add(StringJoin.join("#",cmop.referencePoint));
        col.add(String.valueOf(cmop.sizeSubpOnEdge));
		MopData mmopData = null;
        col.add(StringJoin.join("#",mmopData.IntegerList2IntArray(cmop.subpIndexOnEdge)));
        col.add(String.valueOf(cmop.objectiveDimesion));
		col.add(StringJoin.join("#",cmop.partitionArr));
        return StringJoin.join("_",col);
    }

    public MOP compareAtr(MOP m1,MOP m2,int objectiveDimesion) {
        boolean bAnchorUpdated = false;
        boolean bTrueNadirUpdated = false;
        boolean bIdealUpdated = false;
        for(int j = 0; j < objectiveDimesion; j ++) {
            if(m1.anchorPoint[j][j] > m2.anchorPoint[j][j]) {
                m1.anchorPoint[j][j] = m2.anchorPoint[j][j];
                bAnchorUpdated = true;
            }
            if(m1.trueNadirPoint[j] < m2.trueNadirPoint[j]) {
                bTrueNadirUpdated = true;
                m1.trueNadirPoint[j] = m2.trueNadirPoint[j];
            }
            if(m1.idealPoint[j] > m2.idealPoint[j]) {
                m1.idealPoint[j] = m2.idealPoint[j];
                bIdealUpdated = true;
            }
            if(bAnchorUpdated || bTrueNadirUpdated || bIdealUpdated) {
                m1.referencePoint[j] = m1.trueNadirPoint[j] + 1e3 * (m1.trueNadirPoint[j] - m1.idealPoint[j]);
                bAnchorUpdated = false;
                bTrueNadirUpdated = false;
                bIdealUpdated = false;
            }
        }
        return m1;
    }
														}
											);
			System.out.println("after reduceByKey!");

			List<Tuple2<String, String>> output = mopPop.collect();
			mopList.clear();
			int tt = 0 ;
			int e = 0 ;
			for(Tuple2<?,?> t : output) {
				//if(i == loopTime -1 )
				//	System.out.println(t._1() + "#############" + t._2());
				if("111111111".equals(t._1())) {
					e = tt;
					//System.out.println("\n have 111111111\n , value is : " + t._2() + "\n tt is : " + tt);
				}
				mopList.add(t._2().toString());
				tt ++;
			}
			mopStr = StringJoin.join("!",mopList);
			//System.out.println("mopList[" + e + "] is : " + mopList.get(e));
			//JavaRDD<String> mopValue = mopPop.values();
			// Nov. 3  need to add a function let all recoreds merge to one population.
			// and make it cycle

			System.out.println("After map");
			//pop = p;
			if(i == loopTime -1){
				hdfsOper.mkdir("spark/");
				hdfsOper.createFile("spark/spark_chea.txt", StringJoin.join("\n",mopList));
			}
		}


		mopData.clear();
		mopData.str2Mop(mopStr);

		System.out.println("last idealPoint is : " + StringJoin.join(" ",mopData.mop.idealPoint));

		filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ4/spark_chea_partition.txt";
		mopData.mop.write2File(filename);

		System.out.println("Out of loop");
		cxt.stop();
		System.out.println("Running time is : " + (System.currentTimeMillis() - startTime));
    	String content = null;
    	List<String> col = new ArrayList<String>();
        for(int j = 0 ; j < mopData.mop.sops.size(); j ++) {
		    col.add(StringJoin.join(" ",mopData.mop.sops.get(j).ind.objectiveValue));
		}
    	content = StringJoin.join("\n", col);
    	mopData.write2File("/home/laboratory/workspace/moead_parallel/experiments/DTLZ4/spark_chea2_partition.txt",content);


        filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ4/CHEA_SP_PARTITION_IGD_DTLZ4_3.txt";
        try {
            igdOper.saveIGD(filename);
        } catch (IOException e) {}

	}
}
