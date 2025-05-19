import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class App2 {

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

    public double countAttributes(int[][] data,int attribute, int value) {
        double count = 0.0;
        for (int i=0; i<data.length; i++) {
            if (data[i][attribute] == value)count++;
        }
        return count;
    }

    public double[][] getProbability(int [][]data,boolean positive) {
        double [][] prob = new double[2][data[0].length-1];
        int reviews;
        if (positive)reviews=positiveTrainReviews;
        else reviews=negativeTrainReviews;
        for (int i=0; i<2; i++) {
            for (int j=0; j<data[0].length-1; j++) {
                prob[i][j] = (countAttributes(data, j, i) + 1.0) / (double)(reviews + 2.0);
            }
        }
        return prob;
    }

    public double getAnswer(double[][] prob, int [][]data,int review,boolean positive) {
        double answer = 1.0;
        for (int i=0;i<data[0].length-1;i++) {
                if (data[review][i]==1){
                    answer = answer * (double)(prob[1][i]);
                }
                else if (data[review][i]==0) {
                    answer = answer * (double)(prob[0][i]);
                }
        }
        if (positive)answer=answer*((double)positiveTrainReviews/(positiveTrainReviews+negativeTrainReviews));
        else answer=answer*((double)negativeTrainReviews/(positiveTrainReviews+negativeTrainReviews));
        return answer;
    }

    public static void main(String[] args) throws Exception {
        App2 h = new App2();
        double start=System.currentTimeMillis();
        String vocab[]=h.getWords();
        int [][] trainData = h.getDatabase(vocab,true);
        double [][] trainingpos = h.getProbability(Arrays.copyOfRange(trainData, 0, positiveTrainReviews),true);
        double [][] trainingneg = h.getProbability(Arrays.copyOfRange(trainData, positiveTrainReviews, positiveTrainReviews+negativeTrainReviews),false);
        int [][] testData = h.getDatabase(vocab,false);
        double anspos;
        double ansneg;
        double ans;
        int cpos=0;
        int cneg=0;
        int truePositive = 0;
        int falsePositive = 0;
        int trueNegative = 0;
        int falseNegative = 0;
        for (int i=0; i<testData.length; i++) {
            anspos = h.getAnswer(trainingpos, testData, i,true);
            ansneg = h.getAnswer(trainingneg, testData, i,false);
            ans = (double)anspos/ansneg;
            if (ans > 1) {
                cpos++;
                if (i>=positiveTestReviews)falsePositive++;
                else truePositive++;
            }
            else {
                cneg++;
                if (i<positiveTestReviews)falseNegative++;
                else trueNegative++;
            }
        }
        double Precision = (double)truePositive/(truePositive+falsePositive);
        double Recall = (double)truePositive/(truePositive+falseNegative);
        double F1 = 2 * (double)(Precision*Recall)/(Precision+Recall);
        System.out.println("Vocabulary words: "+m);
        System.out.println("Skipped words: "+skipWords);
        System.out.println("Training reviews: "+ (positiveTrainReviews+negativeTrainReviews));
        System.out.println("Test reviews: "+ (positiveTestReviews+negativeTestReviews));
        System.out.println("Positive outcomes:" + cpos);
        System.out.println("Negative outcomes:" + cneg);
        System.out.println("Errors: " + (falsePositive+falseNegative));
        System.out.println("True Positive: " + truePositive);
        System.out.println("False Positive: " + falsePositive);
        System.out.println("True Negative: " + trueNegative);
        System.out.println("False Negative: " + falseNegative);
        System.out.println("Precision: " + String.format("%.3f", (Precision * 100))+"%");
        System.out.println("Recall: " + String.format("%.3f", (Recall * 100))+"%");
        System.out.println("F1: " + String.format("%.3f", (F1 * 100))+"%");
        System.out.println("Accuracy: " + String.format("%.3f", (1-(double)(falsePositive+falseNegative)/(positiveTestReviews+negativeTestReviews))*100)+"%");
        System.out.println("Total time: "+h.getTime(start));
    }
}
