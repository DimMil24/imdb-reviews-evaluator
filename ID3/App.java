import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class App 
{
	
    static int m=600;
    static int positiveTrainReviews = 12500;
    static int negativeTrainReviews = 12500;
    static int positiveTestReviews = 12500;
    static int negativeTestReviews = 12500;
    static int skipWords=100;

    private String getTime(double start) {
    	double totalSecs=((double)(System.currentTimeMillis()-start))/1000;
        int minutes = ((int)totalSecs % 3600) / 60;
        double seconds = totalSecs % 60;
        if (minutes>0)return (minutes + " minutes and "+String.format("%.1f", seconds)+" seconds.");
        else return (String.format("%.1f", seconds)+" seconds.");
    }

    
    class TreeNode {
        int value;
        TreeNode[] children;

        public TreeNode(TreeNode[] c,int val) {
            children = c;
            value = val;
        }

    }

    static double twoCEntropy(double cProb)
	{
		if(cProb == 0 || cProb == 1) return 0.0;
		else{
			return - ( cProb * log2(cProb) ) - ( (1.0 - cProb) * log2(1.0 - cProb));
		}
	}

    static double log2(double p)
	{
		return Math.log(p) / Math.log(2);
	}

    int calculateIG(int[][] data,String[] used)
	{
		int numOfExamples = data.length;
		int numOfFeatures = data[0].length-1;

		double[] IG = new double[numOfFeatures];

		int positives = 0;
		for (int i = 0; i < numOfExamples; i++)
		{
			if(data[i][numOfFeatures] == 1) positives++;
		}

		double PC1 = (double) positives / numOfExamples;
		double HC = twoCEntropy(PC1);
		double[] PX1 = new double[numOfFeatures];
		double[] PC1X1 = new double[numOfFeatures];
		double[] PC1X0 = new double[numOfFeatures];
		double[] HCX1 = new double[numOfFeatures];
		double[] HCX0 = new double[numOfFeatures];

		for (int j = 0; j < numOfFeatures; j++)
		{
            if (used[j].equals("usedAttr")) {
                IG[j] = -1;
            }
            else {   
                int cX1 = 0;
                int cC1X1 = 0;
                int cC1X0 = 0;
                for (int i = 0; i < numOfExamples; i++)
                {
                    if(data[i][j] == 1) cX1++;
                    if(data[i][j] == 1 && data[i][numOfFeatures] == 1) cC1X1++;
                    if(data[i][j] == 0 && data[i][numOfFeatures] == 1) cC1X0++;
                }

                PX1[j] = (double) cX1 / numOfExamples;
                if(cX1 == 0) PC1X1[j] = 0.0;
                else PC1X1[j] = (double) cC1X1 / cX1;

                if(cX1 == numOfExamples) PC1X0[j] = 0.0;
                else PC1X0[j] = (double) cC1X0 / (numOfExamples - cX1);

                HCX1[j] = twoCEntropy(PC1X1[j]);
                HCX0[j] = twoCEntropy(PC1X0[j]);

                IG[j] = HC - ( (PX1[j] * HCX1[j]) + ( (1.0 - PX1[j]) * HCX0[j]) );

                }
		    }
            double max = IG[0];
            int pos = 0;
            for (int i=1;i<IG.length;i++) {
                if (IG[i]>max) {max = IG[i]; pos = i;}
            }
        return pos;
	}

    public String[] getWords() {
        try{
        	double start=System.currentTimeMillis();
            String[] idef = new String[m];
            int counter = 0;
            File myObj = new File("aclImdb/imdb.vocab");
            Scanner myReader = new Scanner(myObj,"UTF-8");
            for (int i=0;i<skipWords;i++)myReader.nextLine();
            while (myReader.hasNextLine() && counter<m ) {
                String data = myReader.nextLine();
                idef[counter] = data;
                counter++;
            }
            myReader.close();
            System.out.println("Reading vocabulary time: "+getTime(start));
            return idef;
        }
        catch (FileNotFoundException e) {
            System.out.println("An error occurred while scanning the vocabulary.");
            e.printStackTrace();
        }
        return null;
    }

    public int[][] getDatabase(String[] vocabs, boolean train) {
        try {
            int [][] idef;
            double start=System.currentTimeMillis();
            String [] words;
            String pos="";
            String neg="";
            Scanner read;
            File myObj;
            int counterFailed=0;
            if (train) {
                myObj = new File("aclImdb/train/pos");
            }
            else {
                myObj = new File("aclImdb/test/pos");
            }
            int counterR = 0;
            int counterC;
            int posReviews;
            int negReviews;
            if (train) {
                posReviews = positiveTrainReviews;
                negReviews = negativeTrainReviews;
            }
            else {
            	posReviews = positiveTestReviews;
                negReviews = negativeTestReviews;
            }
            idef = new int[posReviews+negReviews][m+1];
            for (final File fileEntry : Arrays.copyOfRange(myObj.listFiles(), 0, posReviews)) {
                counterC = 0;
                if (!fileEntry.isDirectory()) {
                    read = new Scanner(new File(fileEntry.getPath()),"UTF-8");
                    if (read.hasNextLine()) {
                    	words = read.nextLine().replace("."," ").replace(","," ").replace("\"" ," ").replace("?"," ").replace(":"," ").replace("!"," ").replace("("," ").replace(")"," ").replace("<br /><br />"," ").split(" ");
                    	}
                    else {
                    	pos=pos+", "+fileEntry.getName();
                    	counterFailed++;
                    	continue;
                    	}
                    for (String vocab:vocabs) {
                        for (String word:words) {
                            if (word.equals(vocab))idef[counterR][counterC]=1;
                        }
                        counterC++;
                    }
                    if (train)idef[counterR][m] = 1;
                }
                counterR++;
            }   
            if (train) {
                myObj = new File("aclImdb/train/neg");
            }
            else {
                myObj = new File("aclImdb/test/neg");
            }
            for (final File fileEntry : Arrays.copyOfRange(myObj.listFiles(), 0, negReviews)) {
                counterC = 0;
                if (!fileEntry.isDirectory()) {
                    read = new Scanner(new File(fileEntry.getPath()),"UTF-8");
                    if (read.hasNextLine()) {
                    	words = read.nextLine().replace("."," ").replace(","," ").replace("\"" ," ").replace("?"," ").replace(":"," ").replace("!"," ").replace("("," ").replace(")"," ").replace("<br /><br />"," ").split(" ");
                    	}
                    else {
                    	neg=neg+", "+fileEntry.getName();
                    	counterFailed++;
                    	continue;
                    	}
                    for (String vocab:vocabs) {
                        for (String word:words) {
                            if (word.equals(vocab))idef[counterR][counterC]=1;
                        }
                        counterC++;
                    }
                    if (train)idef[counterR][m] = 0;
                }
                counterR++;
            }
            if (counterFailed>1)System.out.print("Failed to read "+counterFailed+" files from");
            else if (counterFailed==1)System.out.print("Failed to read "+counterFailed+" file from");
            else System.out.print("All reviews were scanned successfully from");
            if (train)System.out.println(" training dataset.");
            else System.out.println(" test dataset.");
            if (pos.length()>0)System.out.println("Failures from positive reviews: "+pos.substring(2,pos.length()));
            if (neg.length()>0)System.out.println("Failures from positive reviews: "+neg.substring(2,neg.length()));
            if (train)System.out.println("Reading training dataset time: "+getTime(start));
            else System.out.println("Reading test dataset time: "+getTime(start));
            return idef;
          } 
          catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
        System.out.println("Something went wrong with dataset reading.");
        return null;
    }

    public int countAttributes(int[][] data,int attribute, int value) {
        int count = 0;
        for (int i=0; i<data.length; i++) {
            if (data[i][attribute] == value)count++;
        }
        return count;
    }

    public int[][] getSubset(int[][] data ,int attribute, int value) {
        int[][] subset = new int[countAttributes(data,attribute, value)][data[0].length];
        int rows=0;
        for (int i=0; i<data.length; i++) {
            if (data[i][attribute] == value) {
                subset[rows] = data[i];
                rows++;
            }
        }
        return subset;
    }


    public TreeNode buildTree(int[][] data,String[] used ) {
        TreeNode node = new TreeNode(null,0);
        if (data.length==0) {
            node.value=1;
            return node;
        }
        if (countAttributes(data, data[0].length-1, 0)==data.length) {
            node.value = 0;
            return node;
        }
        if (countAttributes(data, data[0].length-1, 1)==data.length) {
            node.value = 1;
            return node;
        }
        boolean last = true;
        for (int i=0; i<used.length;i++) {
            if (!used[i].equals("usedAttr")) {
                last = false;
                break;
            }
        }
        if (last) {
            int answer = 1;
            int counter = 0;
            for (int i=0; i<2; i++) {
                if (counter < countAttributes(data, data[0].length-1, i)) {
                    counter = countAttributes(data, data[0].length-1, i);
                    answer = i;
                }
            }
            node.value=answer;
            return node;
        }
        else {
            int best = calculateIG(data, used);
            node.value = best;
            node.children = new TreeNode[2];
            for (int i=0; i<2; i++) {
                int[][] subset = getSubset(data, best,i );
                used[best] = "usedAttr";
                node.children[i] = buildTree(subset, used);
            }
            return node;
        }
    }

    public void testResults(int[][] data,TreeNode decision) {
        int cpos=0;
        int cneg=0;
        int truePositive = 0;
        int falsePositive = 0;
        int trueNegative = 0;
        int falseNegative = 0;
        for (int i=0; i<data.length; i++) {
            TreeNode currentNode = decision;
            while (currentNode.children!=null) {
                if (data[i][currentNode.value]==0) {
                    if (currentNode.children[0]!=null) {
                    currentNode = currentNode.children[0];
                    }
                }
                else if (data[i][currentNode.value]==1) {
                    if (currentNode.children[1]!=null) {
                    currentNode = currentNode.children[1];
                    }
                }
            }
            data[i][m] = currentNode.value;
            if (data[i][m]==1) {
                cpos++;
                if (i>=positiveTestReviews)falsePositive++;
                else truePositive++;
            }
            else if (data[i][m]==0) {
                cneg++;
                if (i<positiveTestReviews)falseNegative++;
                else trueNegative++;
            }
        }
        double Precision = (double)truePositive/(truePositive+falsePositive);
        double Recall = (double)truePositive/(truePositive+falseNegative);
        double F1 = 2 * (double)(Precision*Recall)/(Precision+Recall);
        System.out.println("Training reviews: "+ (positiveTrainReviews+negativeTrainReviews));
        System.out.println("Positive outcomes:" + cpos);
        System.out.println("Negative outcomes:" + cneg);
        System.out.println("Precision: " + String.format("%.3f", (Precision * 100))+"%");
        System.out.println("Recall: " + String.format("%.3f", (Recall * 100))+"%");
        System.out.println("F1: " + String.format("%.3f", (F1 * 100))+"%");
        System.out.println("Accuracy: " + String.format("%.3f", (1-(double)(falsePositive+falseNegative)/(positiveTestReviews+negativeTestReviews))*100)+"%");
    }


    public static void main(String[] args) throws Exception {
        App h = new App();
        double start=System.currentTimeMillis();
        String vocab[]=h.getWords();
        int[][] trainData = h.getDatabase(vocab,true);
        String[] used = new String[m];
        for (int i=0; i<used.length; i++) {
            used[i]="";
        }

        TreeNode training = h.buildTree(trainData, used);
        int[][] testData = h.getDatabase(vocab,false);
        h.testResults(testData, training);
        System.out.println("Vocabulary words: "+m);
        System.out.println("Skipped words: "+skipWords);
        System.out.println("Training reviews: "+ (positiveTrainReviews+negativeTrainReviews));
        System.out.println("Test reviews: "+ (positiveTestReviews+negativeTestReviews));
        System.out.println("Total time: "+h.getTime(start));
    }
}
