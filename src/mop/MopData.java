package mop;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problems.AProblem;

import utilities.StringJoin;
import utilities.WrongRemindException;

public class MopData implements DataOperator {

	public MOP mop;
	public AProblem problem;

	public String DELIMITER = "!";

	public void setDelimiter(String delimiter) {
		DELIMITER = delimiter;
	}

	public MopData(MOP mop,AProblem problem) {
		this.mop = mop;
		this.problem = problem;
	}

	public MopData(MOP mop) {
		this.mop = mop;
	}


	public static String sop2Line(SOP subProblem) {
		List<String> col = new ArrayList<String>();
		col.add(StringJoin.join(",",subProblem.ind.genes));
		col.add(StringJoin.join(",",subProblem.ind.objectiveValue));
		col.add(String.valueOf(subProblem.ind.kValue));
		col.add(String.valueOf(subProblem.ind.hyperplaneIntercept));
		col.add(String.valueOf(subProblem.ind.belongSubproblemIndex));
		col.add(String.valueOf(subProblem.sectorialIndex));
		col.add(StringJoin.join(",",IntegerList2IntArray(subProblem.neighbour)));
		col.add(StringJoin.join(",",subProblem.vObj));
		col.add(StringJoin.join(",",subProblem.fixWeight));
		col.add(String.valueOf(subProblem.objectiveDimesion));
		col.add(StringJoin.join(",",subProblem.idealPoint));
		return StringJoin.join("_",col);
	}

	public static int[] IntegerList2IntArray(List<Integer> l) {
		int[] n = new int[l.size()];
		for(int i = 0 ; i < l.size(); i ++) {
			n[i] = l.get(i);
		}
		return n;
	}

	public static List<Integer> IntArray2IntegerList(int[] arr) {
		List<Integer> l = new ArrayList<Integer>(arr.length);
		for(int i = 0 ; i < arr.length; i ++) {
			l.add(new Integer(arr[i]));
		}
		return l;
	}

	public String mopAtr2Str() {
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
		col.add(StringJoin.join("#",IntegerList2IntArray(mop.subpIndexOnEdge)));
		col.add(String.valueOf(mop.objectiveDimesion));
		return StringJoin.join("_",col);
	}

	// mop transfer to String popStr Nov 22
	@Override
	public String mop2Str() {
		List<String> col = new ArrayList<String>(mop.popSize + 1);
		for(int i = 0; i < mop.popSize; i ++) {
			col.add(sop2Line(mop.sops.get(i)));
		}
		col.add("111111111 " + mopAtr2Str());
		return StringJoin.join(DELIMITER,col);
	}

	public void clear() {
		mop.clear();
	}

	// subProblem 's str transfer to SOP
	public void str2Sop(int i,String sopStr) throws WrongRemindException {
		String[] ss = sopStr.split("_");
		if(11 != ss.length) throw new WrongRemindException("Wrong str2Sop");
		MoChromosome ind = new CMoChromosome();
		ind.genes = StringJoin.decodeDoubleArray(",",ss[0]);
		ind.objectiveValue = StringJoin.decodeDoubleArray(",",ss[1]);
		ind.kValue = Double.parseDouble(ss[2]);
		ind.hyperplaneIntercept = Integer.parseInt(ss[3]);
		ind.belongSubproblemIndex = Integer.parseInt(ss[4]);
		SOP sop = new SOP(ind);
		mop.sops.get(i).sectorialIndex = Integer.parseInt(ss[5]);
		mop.sops.get(i).neighbour = IntArray2IntegerList(StringJoin.decodeIntArray(",",ss[6]));
		mop.sops.get(i).vObj = StringJoin.decodeIntArray(",",ss[7]);
		mop.sops.get(i).fixWeight = StringJoin.decodeDoubleArray(",",ss[8]);
		mop.sops.get(i).objectiveDimesion = Integer.parseInt(ss[9]);
		mop.sops.get(i).idealPoint = StringJoin.decodeDoubleArray(",",ss[10]);
		mop.sops.add(sop);
	}


	// subProblem 's str transfer to SOP
	public static SOP str2Sop(String sopStr) throws WrongRemindException {
		String[] ss = sopStr.split("_");
		if(11 != ss.length) throw new WrongRemindException("Wrong str2Sop");
		MoChromosome ind = new CMoChromosome();
		ind.genes = StringJoin.decodeDoubleArray(",",ss[0]);
		ind.objectiveValue = StringJoin.decodeDoubleArray(",",ss[1]);
		ind.kValue = Double.parseDouble(ss[2]);
		ind.hyperplaneIntercept = Integer.parseInt(ss[3]);
		ind.belongSubproblemIndex = Integer.parseInt(ss[4]);
		SOP sop = new SOP(ind);
		sop.sectorialIndex = Integer.parseInt(ss[5]);
		sop.neighbour = IntArray2IntegerList(StringJoin.decodeIntArray(",",ss[6]));
		sop.vObj = StringJoin.decodeIntArray(",",ss[7]);
		sop.fixWeight = StringJoin.decodeDoubleArray(",",ss[8]);
		sop.objectiveDimesion = Integer.parseInt(ss[9]);
		sop.idealPoint = StringJoin.decodeDoubleArray(",",ss[10]);
		return sop;
	}



	// if s[0] == 111111111 after split " ", then is must be MOP's Atr part Nov 22
	public void str2MopAtr(String str) throws WrongRemindException {
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
		mop.subpIndexOnEdge = IntArray2IntegerList(StringJoin.decodeIntArray("#",ss[9]));
		mop.objectiveDimesion = Integer.parseInt(ss[10]);
	}

	@Override
	public void str2Mop(String popStr) throws WrongRemindException {
		String[] ss = popStr.split(DELIMITER);
		// wrong in -1 before . Nov 26
		SOP sop = null;
		SOP[] sops = new SOP[ss.length-1];
		for(int i = 0 ; i < ss.length ; i ++) {
			String[] s = ss[i].split(" ");
			if("111111111".equals(s[0])) {
				str2MopAtr(s[1]);
			} else {
				sop = str2Sop(ss[i]);
				sops[sop.sectorialIndex] = sop;
				//mop.sops.add(str2Sop(ss[i]));
			}
		}
		for(int i = 0 ; i < sops.length; i ++) {
			mop.sops.add(sops[i]);
		}
	}
	


	@Override
	public boolean write2File(String filename, String str) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(
                new FileOutputStream(filename));
            dataOutputStream.writeBytes(str);
            dataOutputStream.writeBytes("\n");
        dataOutputStream.close();
        return true;
	}
}
