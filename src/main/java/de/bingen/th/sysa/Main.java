package de.bingen.th.sysa;

import de.bingen.th.sysa.model.DataElement;
import org.apache.commons.math3.linear.*;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;


/**
 * Main class to start the EM algorithm
 */
public class Main {

    // configuration parameters
    private final static String INPUT_FILE = "inputData.dat";
    private final static String RESULT_FILE = "resultData.dat";
    private final static int NUM_CLUSTER = 2;
    private final static int ITERATIONS = 100;

    /**
     * Start routine to calculate clusters using the EM algorithm
     *
     * @param args Not in use.
     */
    public static void main(String... args) {
        ArrayList<DataElement> dataPoints = convertInputData();
        if (dataPoints == null) {
            System.out.println("ATTENTION: can't handle input file. \n " +
                    "Please make sure, that the input file \"" + INPUT_FILE + "\" exists.");
            return;
        }

        EMClustering em = new EMClustering(ITERATIONS, NUM_CLUSTER, dataPoints);
        em.procedure();

        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new FileWriter(RESULT_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < NUM_CLUSTER; i++) {
            EigenDecomposition eD = new EigenDecomposition(em.getCovarianceAtIndex(i));
            printWriter.println(i+1 + ". Cluster: ");
            printWriter.println("- RealEigenValues: ");
            for (double eigenValue: eD.getRealEigenvalues()) {
                printWriter.println("  - " + eigenValue);
            }
            printWriter.println("- EigenVectors: ");
            for (int j = 0; j < dataPoints.get(0).getAttributes().getMaxIndex() + 1; j++) {
                printWriter.println("  - " + eD.getEigenvector(j));
            }
            printWriter.println("");
        }
        printWriter.close();

    }

    /**
     * Read input file and convert it in an array of DataElements
     *
     * @return null -> something went wrong
     * else: Array of DataElements (including Vector of double points)
     */
    private static ArrayList<DataElement> convertInputData() {
        BufferedReader bufferedReader = checkAndGetInputfile(INPUT_FILE);
        if (bufferedReader == null) {
            return null;
        }

        ArrayList<DataElement> returnList = new ArrayList();
        // TODO: create a more elegant way
        bufferedReader.lines().forEach(x -> {
            DataElement e = new DataElement(NUM_CLUSTER);
            // TODO: chaos
            ArrayList<Double> values = new ArrayList<>();
            Stream.of(x.split(" ")).forEach(v -> {
                if (!v.isEmpty()) {
                    values.add(Double.parseDouble(v));
                }
            });

            Double[] tmp = new Double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                tmp[i] = values.get(i);
            }
            e.setAttributes(new ArrayRealVector(tmp));
            returnList.add(e);
        });

        return returnList;
    }

    /**
     * read given input file
     *
     * @param file input file - most inputData.dat
     * @return Buffered Reader
     */
    private static BufferedReader checkAndGetInputfile(String file) {
        try {
            return new BufferedReader(new FileReader(new File(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
