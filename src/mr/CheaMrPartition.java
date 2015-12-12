package mr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chea.chea;
import mop.MOP;
import mop.CHEAMOP;
import mop.MopData;
import mop.IGD;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.NLineInputFormat;

import problems.AProblem;
import problems.DTLZ1;
import problems.DTLZ1;
import utilities.StringJoin;
import utilities.WrongRemindException;

public class CheaMrPartition {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 * @throws WrongRemindException
	 */
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException, WrongRemindException {
		int popSize = 105;
		int hyperplaneIntercept = 13;
		int neighbourNum = 2;
		int iterations = 400;
		int writeTime = 1;
		int partitionNum = 2;
		int innerLoop = 20;
		int loopTime = iterations / (writeTime * innerLoop);
		AProblem problem = DTLZ1.getInstance();
		MOP mop = CHEAMOP.getInstance(popSize, problem , hyperplaneIntercept, neighbourNum);
		mop.initial();
		
		MopData mopData = new MopData(mop,problem);

		String mopStr = mopData.mop2Str();
		List<String> pStr = new ArrayList<String>(partitionNum);
		HdfsOper hdfsOper = new HdfsOper();
		hdfsOper.mkdir("chea/");
		for(int i = 0; i < iterations + 1; i ++)
			hdfsOper.rm("chea/" + i + "/");
		hdfsOper.mkdir("chea/0/");


        pStr.clear();
        mopData.mop.initPartition(partitionNum);
        for(int j = 0; j < partitionNum; j ++) {
            mopData.mop.setPartitionArr(j);
            mopStr = mopData.mop2Str();
            pStr.add(mopStr);
        }

		hdfsOper.addContentFile("chea/chea.txt",StringJoin.join("\n",pStr));
		hdfsOper.cp("chea/chea.txt","chea/0/part-00000");

        IGD igdOper = new IGD(1500);
        String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ1(3).dat";
        try {
            igdOper.ps = igdOper.loadPfront(filename);
        } catch (IOException e) {}


		long startTime = System.currentTimeMillis();
		long igdTime = 0;
		System.out.println("Timer start!!!");
		for (int i = 0; i < loopTime; i++) {
			System.out.println("The " + i + "th time!");

            //Thread.sleep(2500);
            
            long igdStartTime = System.currentTimeMillis();
            mopData.clear();
            mopData.str2Mop(mopStr);
            List<double[]> real = new ArrayList<double[]>(mopData.mop.sops.size()); 
            for(int j = 0; j < mopData.mop.sops.size(); j ++) {
               real.add(mopData.mop.sops.get(j).ind.objectiveValue);
            }
            double[] genDisIGD = new double[2];
            genDisIGD[0] = i*innerLoop;
            genDisIGD[1] = igdOper.calcIGD(real);
            igdOper.igd.add(genDisIGD);   
            igdTime += System.currentTimeMillis() - igdStartTime ;



			JobConf jobConf = new JobConf(CheaMr.class);
			jobConf.setJobName("chea mapreduce");
			jobConf.setNumMapTasks(writeTime);
			jobConf.setNumReduceTasks(1);

			jobConf.setJarByClass(CheaMr.class);
			MapClass.setInnerLoop(innerLoop);
			jobConf.setInputFormat(NLineInputFormat.class);
			jobConf.setOutputFormat(TextOutputFormat.class);
			jobConf.setMapperClass(MapClass.class);
			jobConf.setReducerClass(ReduceClass.class);
			jobConf.setOutputKeyClass(Text.class);
			jobConf.setOutputValueClass(Text.class);

			FileInputFormat.addInputPath(jobConf,new Path(
					"hdfs://master:8020/user/root/chea/chea.txt"));
			FileOutputFormat.setOutputPath(jobConf,new Path(
					"hdfs://master:8020/user/root/chea/"
					+ (i+1)));
			System.out.println("Run job begin ... ");
			JobClient.runJob(jobConf);
			System.out.println("Run job end ... ");
			// read the output of reduce and write the pop in chea.txt
			// Nov 22, not clear() method
			mopData.clear();
			mopData.setDelimiter("\n");
			mopData.str2Mop(hdfsOper.readWholeFile("chea/"+(i+1)+"/part-00000"));		
			mopData.setDelimiter("!");
            pStr.clear();
            mopData.mop.initPartition(partitionNum);
            for(int j = 0; j < partitionNum; j ++) {
                mopData.mop.setPartitionArr(j);
                mopStr = mopData.mop2Str();
                pStr.add(mopStr);                                                                                                                                                                                  
            }
			hdfsOper.rm("chea/chea.txt");
            hdfsOper.addContentFile("chea/chea.txt",StringJoin.join("\n",pStr));
		}


        long recordTime = System.currentTimeMillis()-startTime - igdTime;
        System.out.println("Running time is : " + recordTime);
        mopData.recordTimeFile("/home/laboratory/workspace/moead_parallel/experiments/recordTime.txt","\nDTLZ1,CheaMrPartition ,partitionNum_2,recordTime is " + recordTime);

		mopData.mop.write2File("/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/partitionNum_2_mr_part_chea.txt");

		BufferedReader br = new BufferedReader(hdfsOper.open("chea/"+(loopTime-1)+"/part-00000"));
		String line = null;
		String content = null;
		List<String> col = new ArrayList<String>();
		for(int j = 0 ; j < mopData.mop.sops.size(); j ++) {
			col.add(StringJoin.join(" ",mopData.mop.sops.get(j).ind.objectiveValue));
		}
		content = StringJoin.join("\n", col);
		mopData.write2File("/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/partitionNum_2_mr_part_chea_all.txt",content);

        filename = "/home/laboratory/workspace/moead_parallel/experiments/DTLZ1/partitionNum_2_CHEA_MR_PART_IGD_DTLZ1_3.txt";
        try {
            igdOper.saveIGD(filename);
        } catch (IOException e) {}

	}
}
