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
import problems.DTLZ2;
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
		int iterations = 1600;
		int writeTime = 4;
		int innerLoop = 400;
		int loopTime = iterations / (writeTime * innerLoop);
		AProblem problem = DTLZ1.getInstance();
		MOP mop = CHEAMOP.getInstance(popSize, problem , hyperplaneIntercept, neighbourNum);
		mop.initial();
		
		MopData mopData = new MopData(mop,problem);

		String mopStr = mopData.mop2Str();

		HdfsOper hdfsOper = new HdfsOper();
		hdfsOper.mkdir("chea/");
		for(int i = 0; i < iterations + 1; i ++)
			hdfsOper.rm("chea/" + i + "/");
		hdfsOper.mkdir("chea/0/");
		hdfsOper.createFile("chea/chea.txt", mopStr, writeTime);
		hdfsOper.cp("chea/chea.txt","chea/0/part-00000");

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
			mopStr = mopData.mop2Str();
			hdfsOper.rm("chea/chea.txt");
			hdfsOper.createFile("chea/chea.txt", mopStr, writeTime);
		}
		System.out.println("Running time is : " + (System.currentTimeMillis() - startTime));

		mopData.mop.write2File("/home/laboratory/workspace/moead_parallel/experiments/parallel/mr_chea.txt");
		

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
				mopData.write2File("/home/laboratory/workspace/moead_parallel/experiments/parallel/mr_chea2.txt",content);
//			hdfsOper.createFile("/chea/" + i + "/objectiveValue.txt", content);
		//}
		System.out.println("LoopTime is : " + loopTime + "\n");
	}
}
