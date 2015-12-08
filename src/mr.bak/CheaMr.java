package mr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chea.chea;
import mop.MOP;
import mop.CHEAMOP;
import mop.MopData;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.NLineInputFormat;

import problems.AProblem;
import problems.DTLZ1;
import utilities.StringJoin;
import utilities.WrongRemindException;

public class CheaMr{

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 * @throws WrongRemindException
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
		// MOEAD.chea(mop,iterations);
		
		
		// Oct 30  writing another mopData to use as multi pop in one file.
		//MopData mopData = new MopData(mop);
		
		MopData mopData = new MopData(mop,problem);

		// must have generate the mop's all inds. Nov 22
		String mopStr = mopData.mop2Str();

		HdfsOper hdfsOper = new HdfsOper();
		hdfsOper.mkdir("chea/");
		for(int i = 0; i < iterations + 1; i ++)
			hdfsOper.rm("chea/" + i + "/");
		hdfsOper.mkdir("chea/0/");
		hdfsOper.createFile("chea/chea.txt", mopStr, writeTime);
		hdfsOper.cp("chea/chea.txt","chea/0/part-00000");

        IGD igdOper = new IGD(1500);
        String filename = "/home/laboratory/workspace/TestData/PF_Real/DTLZ4(3).dat";
        try {
            igdOper.ps = igdOper.loadPfront(filename);
        } catch (IOException e) {}


		long startTime = System.currentTimeMillis();
		System.out.println("Timer start!!!");
		for (int i = 0; i < loopTime; i++) {
			System.out.println("The " + i + "th time!");
			JobConf jobConf = new JobConf(CheaMr.class);
			jobConf.setJobName("chea mapreduce");
			jobConf.setNumMapTasks(writeTime);
			jobConf.setNumReduceTasks(1);

			jobConf.setJarByClass(CheaMr.class);
			MapClass.setInnerLoop(innerLoop);
			//MyFileInputFormat.setReadFileTime(jobConf,writeTime);
			//jobConf.setInputFormat(MyFileInputFormat.class);
			//NLineInputFormat.setNumLinesPerSplit(jobConf,1);
			jobConf.setInputFormat(NLineInputFormat.class);
			jobConf.setOutputFormat(TextOutputFormat.class);
			jobConf.setMapperClass(MapClass.class);
			jobConf.setReducerClass(ReduceClass.class);
			jobConf.setOutputKeyClass(Text.class);
			jobConf.setOutputValueClass(Text.class);


			
			FileInputFormat.addInputPath(jobConf,new Path(
					"hdfs://master:8020/user/root/chea/chea.txt"));
			/*
			FileInputFormat.addInputPath(jobConf,new Path(
					"hdfs://master:8020/user/root/chea/"
					+ i + "/part-r-00000"));
			*/
			FileOutputFormat.setOutputPath(jobConf,new Path(
					"hdfs://master:8020/user/root/chea/"
					+ (i+1)));
			System.out.println("Run job begin ... ");
			JobClient.runJob(jobConf);
			// running Job util it ends Nov 22
			System.out.println("Run job end ... ");

			// read the output of reduce and write the pop in chea.txt
			// Nov 22, not clear() method
			mopData.clear();
			mopData.setDelimiter("\n");

			// Nov 25 
			// record the IGD and caculate


			// read the whole file
			mopData.str2Mop(hdfsOper.readWholeFile("chea/"+(i+1)+"/part-00000"));		
			mopStr = mopData.mop2Str();
			hdfsOper.rm("chea/chea.txt");
			hdfsOper.createFile("chea/chea.txt", mopStr, writeTime);
		}
		System.out.println("Running time is : " + (System.currentTimeMillis() - startTime));
		//for (int i = 0; i < loopTime + 1; i++) {
			//BufferedReader br = new BufferedReader(hdfsOper.open("chea/" + i + "/part-00000"));
			BufferedReader br = new BufferedReader(hdfsOper.open("chea/"+(loopTime-1)+"/part-00000"));
			String line = null;
			String content = null;
			List<String> col = new ArrayList<String>();
			for(int j = 0 ; j < mopData.mop.sops.size(); j ++) {
				col.add(StringJoin.join(" ",mopData.mop.sops.get(j).ind.objectiveValue));
			}
			content = StringJoin.join("\n", col);
			//mopData.write2File("/home/laboratory/workspace/chea_parallel/experiments/parallel/" + i + ".txt",content);
			//if(i == loopTime)
				mopData.write2File("/home/laboratory/workspace/chea_parallel/experiments/parallel/mr_chea.txt",content);
//			hdfsOper.createFile("/chea/" + i + "/objectiveValue.txt", content);
		//}
		System.out.println("LoopTime is : " + loopTime + "\n");
	}
}
