package remoteTest;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;

import skipGraph.NodeInfo;

public class TestingLog implements Serializable, Comparable<TestingLog>{
	
	private static final long serialVersionUID = 1L;

	private boolean malicious=false;
	private int Attempts;
	private int Success;
	private ArrayList<TransactionLog> transactionAttempts;
	private ArrayList<ViewUpdateLog> viewUpdateLog;
	private ViewUpdateLog curLog;
	private ArrayList<ExceptionLog> exceptions;
	private ArrayList<StackOverflowLog> overflows;
	
	public TestingLog(boolean malicious) {
		this.malicious=malicious;
		Attempts = 0;
		Success = 0;
		transactionAttempts = new ArrayList<>();
		viewUpdateLog = new ArrayList<>();
		curLog = new ViewUpdateLog();
		exceptions = new ArrayList<>();
		overflows = new ArrayList<>();
	}
	
	public void logException(Exception e, String st) {
		exceptions.add(new ExceptionLog(e,st));
	}
	
	public void logException(Exception e) {
		exceptions.add(new ExceptionLog(e));
	}
	
	public ArrayList<ExceptionLog> getExceptions(){
		return exceptions;
	}
	
	public void printExceptionLogs() {
		for(ExceptionLog cur : exceptions) {
			System.out.println(cur);
		}
	}
	
	public void printExceptionLogs(PrintWriter pw) {
		for(ExceptionLog cur : exceptions) {
			pw.println(cur);
		}
	}
	
	public void logOverflow(Error e, ArrayList<NodeInfo> graph, String st) {
		overflows.add(new StackOverflowLog(e,graph,st));
	}
	
	public void logOverflow(Error e, ArrayList<NodeInfo> graph, String st, ArrayList<NodeInfo> lst) {
		overflows.add(new StackOverflowLog(e,graph,st,lst));
	}
	
	public ArrayList<StackOverflowLog> getOverflows(){
		return overflows;
	}
	
	public void printOverflowLogs() {
		for(StackOverflowLog cur : overflows) {
			System.out.println(cur);
		}
	}
	
	public void printOverflowLogs(PrintWriter pw) {
		for(StackOverflowLog cur : overflows) {
			pw.println(cur);
		}
	}
	
	public void logTransaction(boolean success,int numAuthenticated,int numSound,int numCorrect,int hasBalance, long timeTaken) {
		transactionAttempts.add(new TransactionLog(success,numAuthenticated,numSound,numCorrect,hasBalance,timeTaken));
		Attempts++;
		if(success) Success++;
	}
	
	public void logBlockValidation(long time, boolean success) {
		curLog.setValidateBlockTime(time);
		curLog.setValidationSuccessful(success);
	}
	
	public void logViewUpdate(long time, boolean success) {
		curLog.setTimeTaken(time);
		curLog.setHadMoreThanThreshold(success);
		viewUpdateLog.add(curLog);
		curLog = new ViewUpdateLog();
	}

	
	
	
	
	public boolean isMalicious() {
		return malicious;
	}

	public int getAttempts() {
		return Attempts;
	}

	public int getSuccess() {
		return Success;
	}

	public ArrayList<TransactionLog> getTransactionAttempts() {
		return transactionAttempts;
	}

	public ArrayList<ViewUpdateLog> getViewUpdateLog() {
		return viewUpdateLog;
	}


	@Override
	public int compareTo(TestingLog o) {
		if(o.isMalicious()==malicious) return 0;
		if(o.isMalicious()) return -1;
		if(malicious) return 1;
		return 0;
	}
}

class ViewUpdateLog implements Serializable, Comparable<ViewUpdateLog>{
	private static final long serialVersionUID = 1L;

	private boolean hadMoreThanThreshold;
	private boolean validationSuccessful;
	private long timeTaken;
	private long validateBlockTime;
	
	
	public ViewUpdateLog(boolean hadMoreThanThreshold, boolean validationSuccessful, long timeTaken,
			long validateBlockTime) {
		super();
		this.hadMoreThanThreshold = hadMoreThanThreshold;
		this.validationSuccessful = validationSuccessful;
		this.timeTaken = timeTaken;
		this.validateBlockTime = validateBlockTime;
	}

	public ViewUpdateLog() {
		super();
	}
	
	public boolean hadMoreThanThreshold() {
		return hadMoreThanThreshold;
	}

	public boolean isValidationSuccessful() {
		return validationSuccessful;
	}

	public long timeTaken() {
		return timeTaken;
	}
	
	public long validateBlockTimeTaken() {
		return this.validateBlockTime;
	}

	public void setHadMoreThanThreshold(boolean hadMoreThanThreshold) {
		this.hadMoreThanThreshold = hadMoreThanThreshold;
	}

	public void setValidationSuccessful(boolean validationSuccessful) {
		this.validationSuccessful = validationSuccessful;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public void setValidateBlockTime(long validateBlockTime) {
		this.validateBlockTime = validateBlockTime;
	}

	@Override
	public int compareTo(ViewUpdateLog o) {
		if(hadMoreThanThreshold == o.hadMoreThanThreshold) {
			if(validationSuccessful == o.validationSuccessful) return 0;
			if(validationSuccessful) return -1;
			else return 1;
		}
		if(hadMoreThanThreshold) return -1;
		else return 1;
	}
	public String toString() {
		return timeTaken()+","+hadMoreThanThreshold+","+validateBlockTimeTaken()+","+validationSuccessful;
	}
}


class TransactionLog implements Serializable, Comparable<TransactionLog>{
	private static final long serialVersionUID = 1L;
	
	private boolean success;
	private int isAuthenticated;
	private int isSound;
	private int isCorrect;
	private int hasBalance;
	private long timeTaken;
	
	public TransactionLog(boolean success,int isAuthenticated, int isSound, int isCorrect, int hasBalance, long timeTaken) {
		this.success=success;
		this.isAuthenticated = isAuthenticated;
		this.isSound = isSound;
		this.isCorrect = isCorrect;
		this.hasBalance = hasBalance;
		this.timeTaken=timeTaken;
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public long timeTaken() {
		return timeTaken;
	}

	@Override
	public int compareTo(TransactionLog o) {
		if(o.isSuccessful()==isSuccessful()) return 0;
		if(isSuccessful()) return -1;
		return 1;
	}
	
	public String toString() {
		return timeTaken+"," + isAuthenticated + "," + isSound + "," + isCorrect + "," + hasBalance + ","+success;
	}
}

class ExceptionLog implements Serializable{
	private static final long serialVersionUID = 1L;
	private Exception exception;
	private String description;
	
	public ExceptionLog(Exception e, String description) {
		this.exception=e;
		this.description=description;
	}
	
	public ExceptionLog(Exception e) {
		this.exception=e;
		this.description="No description provided.";
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return "Description:" + description + "\n Exception:\n" + sw.toString();
	}
}

class StackOverflowLog implements Serializable{
	private static final long serialVersionUID = 1L;
	private Error error;
	private ArrayList<NodeInfo> graph;
	private ArrayList<NodeInfo> path;
	private String description;
	
	public StackOverflowLog(Error error,ArrayList<NodeInfo> graph, String desc) {
		this.error = error;
		this.graph = new ArrayList<>();
		this.graph.addAll(graph);
		this.description = desc;
	}
	
	public StackOverflowLog(Error error,ArrayList<NodeInfo> graph, String desc,ArrayList<NodeInfo> path) {
		this.error = error;
		this.graph = new ArrayList<>();
		this.graph.addAll(graph);
		this.description = desc;
		this.path = path;
	}
	
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		StringBuilder sb = new StringBuilder();
		sb.append("Description: "+ description + "\n");
		sb.append("Nodes: \n");
		int cnt=1;
		for(NodeInfo nd : graph) {
			sb.append("\n"+cnt++ + ") "+nd.getAddress()+" " + nd.getNameID() + " " + nd.getNumID());
		}
		sb.append("Exception:\n ");
		String tmp = sw.toString();
		String[] toks = tmp.split("\n");
		for(int i=0;i<toks.length;i++) {
			if(i!=0 && !toks[i].equalsIgnoreCase(toks[i-1])) {
				sb.append(toks[i]+"\n");
			}
		}
		if(path!=null) {
			cnt = 1;
			for(NodeInfo nd : path) {
				sb.append("\n"+cnt++ + ") "+nd.getAddress()+" " + nd.getNameID() + " " + nd.getNumID());
			}
		}
		sb.append("\n");
		return sb.toString();
	}
}