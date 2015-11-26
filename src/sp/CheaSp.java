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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.NLineInputFormat;

import problems.AProblem;
import problems.DTLZ2;
import problems.DTLZ1;
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


public class CheaSp {

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
        int neighbourNum = 2;
        int iterations = 800;
        int writeTime = 4;
        int innerLoop = 1;
        int loopTime = iterations / (writeTime * innerLoop);
        AProblem problem = DTLZ1.getInstance();
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
		System.out.println("mopStr is : \n" + mopStr);
		SparkConf sparkConf = new SparkConf().setAppName("chea spark");
		JavaSparkContext cxt = new JavaSparkContext(sparkConf);

				//JavaRDD<String> pop = cxt.parallelize(Arrays.asList(mopStr));

		long startTime = System.currentTimeMillis();
		List<String> pStr = new ArrayList<String>();
		List<String> mopList = new ArrayList<String>();
		System.out.println("Timer start!!!");
		for (int i = 0; i < loopTime; i++) {
			System.out.println("The " + i + "th time!");
			//Thread.sleep(2500);
			pStr.clear();
			for(int j = 0; j < writeTime; j ++) {
				System.out.println("writeTime is " + j );
				pStr.add(mopStr);
			}
			JavaRDD<String> p = cxt.parallelize(pStr,writeTime);
			System.out.println("after union");
			JavaPairRDD<String,String> mopPair = p.mapPartitionsToPair(
													new PairFlatMapFunction<Iterator<String>,String,String>() {
															public Iterable<Tuple2<String,String>> call(Iterator<String> s) throws WrongRemindException{
																int aPopSize = 406;
																int aHyperplaneIntercept = 27;
																int aNeighbourNum = 2;
																AProblem aProblem = DTLZ1.getInstance();
																MOP aMop = CHEAMOP.getInstance(aPopSize, aProblem, aHyperplaneIntercept,aNeighbourNum);
																aMop.allocateAll(aPopSize,aProblem.objectiveDimesion);
																MopData mmop = new MopData(aMop,aProblem);
																System.out.println("Map begin : ");
																

																// wrong in this place , Nov 26
																// it didn't work actully . I've found sops's size is zero
																//
																mmop.str2Mop(s.next());
																System.out.println(mmop.mop.sops.size());
																System.out.println(mmop.mop.idealPoint[0]);
																mmop.mop.updatePop(1);
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
			List<Tuple2<String, String>> output = mopPair.collect();
			if(i == loopTime-1 )
			for(Tuple2<?,?> t : output)
					System.out.println(t._1() + "##############" + t._2());

			JavaPairRDD<String,String> mopPop = mopPair.reduceByKey(
														new Function2<String,String,String>() {
																int objectiveDimesion ;
																public String call(String s1, String s2) {

																        AProblem problem = DTLZ1.getInstance();
																        objectiveDimesion = problem.objectiveDimesion;
																        //MOP mop = new CHEAMOP(problem.objectiveDimesion);     
																		String[] s1split = s1.split(" ");
																		String[] s2split = s2.split(" ");
																		if("111111111".equals(s1split[0])) {
																			MOP mop1;
																			MOP mop2;
														                    try {
														                        mop1 = str2MopAtr(s1split[1]);
														                        mop2 = str2MopAtr(s2split[1]);
														                        mop1 = compareAtr(mop1,mop2);
														                    } catch (WrongRemindException e) {
                    
                    														}
																			return "111111111 " + mopAtr2Str(mop);
																		} else {
																			SOP sop1 = null;
																			SOP sop2 = null;
																			double[] idealPoint = new double[objectiveDimesion];
																			try {
																				sop1 = MopData.str2Sop(s1);
																				sop2 = MopData.str2Sop(s2);
																			for(int i = 0 ; i < objectiveDimesion; i ++) idealPoint[i] = 1e+5;      
																			for(int i = 0 ; i < objectiveDimesion; i ++) {
																				if(sop1.idealPoint[i] < idealPoint[i]) idealPoint[i] = sop1.idealPoint[i];
																				if(sop2.idealPoint[i] < idealPoint[i]) idealPoint[i] = sop2.idealPoint[i];
																			} 
																			} catch (WrongRemindException e) {}
																			sop1 = compareSops(sop1, sop2,idealPoint);
																			return MopData.sop2Line(sop1);
																		}
																}


    private MOP str2MopAtr(String str) throws WrongRemindException {
        MOP mop = new CHEAMOP(objectiveDimesion);
        String[] ss = str.split("_");
        if(11 != ss.length) throw new WrongRemindException("Wrong str2MopAtr");
        mop.popSize = Integer.parseInt(ss[0]);
        mop.hyperplaneIntercept = Integer.parseInt(ss[1]);
        mop.neighbourNum = Integer.parseInt(ss[2]);
        mop.perIntercept = Double.parseDouble(ss[3]);
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
        mop.anchorPoint = a;
        mop.trueNadirPoint = StringJoin.decodeDoubleArray("#",ss[5]);
        mop.idealPoint = StringJoin.decodeDoubleArray("#",ss[6]);
        mop.referencePoint = StringJoin.decodeDoubleArray("#",ss[7]);
        mop.sizeSubpOnEdge = Integer.parseInt(ss[8]);
        mop.subpIndexOnEdge = MopData.IntArray2IntegerList(StringJoin.decodeIntArray("#",ss[9]));
        mop.objectiveDimesion = Integer.parseInt(ss[10]);
        return mop;
    }    

    public SOP compareSops(SOP s1,SOP s2,double[] idealPoint) {
        int objectiveDimesion = s1.ind.objectiveDimesion;
        int hyperplaneIntercept = s1.ind.hyperplaneIntercept;
        double[] refCal = new double[objectiveDimesion];
        s1.ind.calKVal(idealPoint,hyperplaneIntercept);
        double k = s1.ind.kValue > s2.ind.kValue ? s1.ind.kValue : s2.ind.kValue;
        for(int i = 0 ; i < objectiveDimesion; i ++) {
            refCal[i] = (idealPoint[i] + k * (1/hyperplaneIntercept) * ( s1.vObj[i] + s1.fixWeight[i]));
        }
        double c1 = MOP.getHyperVolume(s1.ind,refCal);
        double c2 = MOP.getHyperVolume(s2.ind,refCal);
        if(c1 > c2) {
            s1.ind = s2.ind;
        }
        s1.idealPoint = idealPoint;
        return s1;
    }

    private String mopAtr2Str(MOP mop) {
        List<String> col = new ArrayList<String>(mop.popSize + 1);
        col.add(String.valueOf(mop.popSize));
        col.add(String.valueOf(mop.hyperplaneIntercept));
        col.add(String.valueOf(mop.neighbourNum));
        col.add(String.valueOf(mop.perIntercept));
        List<String> tmp = new ArrayList<String>(); 
        for(int i = 0 ; i < mop.anchorPoint.length; i ++) {
            tmp.add(StringJoin.join(",",mop.anchorPoint[i]));
        }
        col.add(StringJoin.join("#",tmp));
        col.add(StringJoin.join("#",mop.trueNadirPoint));
        col.add(StringJoin.join("#",mop.idealPoint));
        col.add(StringJoin.join("#",mop.referencePoint));
        col.add(String.valueOf(mop.sizeSubpOnEdge));
        col.add(StringJoin.join("#",MopData.IntegerList2IntArray(mop.subpIndexOnEdge)));
        col.add(String.valueOf(mop.objectiveDimesion));
        return StringJoin.join("_",col);
    }

    public MOP compareAtr(MOP m1,MOP m2) {
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
			output = mopPop.collect();
			mopList.clear();
			for(Tuple2<?,?> t : output) {
				if(i == loopTime -1 )
					System.out.println(t._1() + "#############" + t._2());
					mopList.add(t._2().toString());
			}
			mopStr = StringJoin.join("$",mopList);

			//JavaRDD<String> mopValue = mopPop.values();
			// Nov. 3  need to add a function let all recoreds merge to one population.
			// and make it cycle

			System.out.println("After map");
			//pop = p;
			if(i == loopTime -1){
					hdfsOper.mkdir("spark/");
					hdfsOper.createFile("spark/spark_moead.txt", StringJoin.join("\n",mopList));
			}
		}

		System.out.println("Out of loop");
		cxt.stop();
		System.out.println("Running time is : " + (System.currentTimeMillis() - startTime));
	    BufferedReader br = new BufferedReader(hdfsOper.open("spark/spark_moead.txt"));
    	String content = null;
    	List<String> col = new ArrayList<String>();
        for(int j = 0 ; j < mopData.mop.sops.size(); j ++) {
		    col.add(StringJoin.join(" ",mopData.mop.sops.get(j).ind.objectiveValue));
		}
    	content = StringJoin.join("\n", col);
    	mopData.write2File("/home/laboratory/workspace/moead_parallel/experiments/parallel/spark_moead.txt",content);
	}






}
