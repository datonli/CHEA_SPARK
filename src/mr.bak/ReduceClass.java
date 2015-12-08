package mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

import mop.MOP;
import mop.SOP;
import mop.CHEAMOP;
import mop.MopData;

import problems.AProblem;
import problems.DTLZ1;

import utilities.StringJoin;
import utilities.WrongRemindException;

public class ReduceClass extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {
	private Text result = new Text();
	private int objectiveDimesion ;
	public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		AProblem problem = DTLZ1.getInstance();
		objectiveDimesion = problem.objectiveDimesion;
		MOP mop = new CHEAMOP(problem.objectiveDimesion);
		//MOP mop;
		String value = null;
		String tmp = null;
		double[] idealPoint = new double[objectiveDimesion];
		for(int i = 0 ; i < objectiveDimesion; i ++) idealPoint[i] = 1e+5;
		boolean flag = false;
		List<SOP> sops = new ArrayList<SOP>(10);
		int cnt = 0 ;
		while(values.hasNext()) {
			tmp = values.next().toString();
			if (!"111111111".equals(key.toString())) {
				// update the mop's subProblem Nov 23
				try {
					SOP tmpSop = MopData.str2Sop(tmp); 
				
					for(int i = 0 ; i < objectiveDimesion; i ++) {
						if(tmpSop.idealPoint[i] < idealPoint[i]) idealPoint[i] = tmpSop.idealPoint[i];
					}
					sops.add(tmpSop);
				} catch (WrongRemindException e) {
				
				}
			} else {
				// update the mop's atr Nov 23
				if(0 != cnt) {
					System.out.println("tmp is : " + tmp);
					try {
						mop = str2MopAtr(tmp);
					} catch (WrongRemindException e) {
					
					}
				} else {
					try {
						MOP mopTmp = str2MopAtr(tmp);
						mop = compareAtr(mop,mopTmp);	
					} catch (WrongRemindException e) {
					
					}
				}
				flag = true;
				cnt ++;
			}
		}
		if(flag) {
			value ="111111111 " + mopAtr2Str(mop);
		}
		else {
			SOP subProblem = sops.get(0);
			for(int i = 1; i < sops.size(); i ++) {
				// compare the inds in one idealPoint
				subProblem = compareSops(subProblem, sops.get(i),idealPoint);
			}
			value = MopData.sop2Line(subProblem);
		}
		NullWritable nullw = null;
		result.set(value);
		output.collect(nullw, result);
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
}
