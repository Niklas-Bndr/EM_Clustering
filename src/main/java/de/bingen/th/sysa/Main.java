package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataPoint;
import org.apache.commons.math3.linear.*;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Main class to start the EM algorithm
 */
public class Main {

    // configuration parameters
    /*
    private final static String INPUT_FILE = "inputMouse.dat";
    private final static String RESULT_FILE = "resultMouse.dat";
    private final static String RESULT_FILE_FORMATED = "resultMouse_formated.dat";
    private final static int NUM_CLUSTER = 3;
    */
    private final static String INPUT_FILE = "inputFaithful.dat";
    private final static String RESULT_FILE = "resultFaithful.dat";
    private final static String RESULT_FILE_FORMATED = "resultFaithful_formated.dat";
    private final static int NUM_CLUSTER = 2;

    private final static int ITERATIONS = 500;

    /**
     * Start routine to calculate clusters using the EM algorithm
     * @param args Not in use.
     */
    public static void main(String... args) {
        ArrayList<DataPoint> dataPoints = convertInputData();
        if (dataPoints == null) {
            System.out.println("ATTENTION: can't handle input file. \n " +
                    "Please make sure, that the input file \"" + INPUT_FILE + "\" exists.");
            return;
        }

        EMClustering em = new EMClustering(ITERATIONS, NUM_CLUSTER, dataPoints);
        em.procedure();

        PrintWriter printWriter = null;
        PrintWriter printWriterFormated = null;
        try {
            printWriter = new PrintWriter(new FileWriter(RESULT_FILE));
            printWriterFormated = new PrintWriter(new FileWriter(RESULT_FILE_FORMATED));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Cluster cluster: em.getClusters()) {
            EigenDecomposition eD = new EigenDecomposition(cluster.getCovariance());
            printWriterFormated.println(cluster.getIndex()+1 + ". Cluster: ");
            printWriterFormated.println("- Mean: ");
            for (double mean: cluster.getMean().toArray()) {
                printWriter.print(" " + mean);
                printWriterFormated.println("  - " + mean);
            }
            printWriterFormated.println("- RealEigenValues: ");
            for (double eigenValue: eD.getRealEigenvalues()) {
                printWriter.print(" " + eigenValue);
                printWriterFormated.println("  - " + eigenValue);
            }
            // Get index of max Eigenvalue
            int index = IntStream.range(0, eD.getRealEigenvalues().length)
                        .reduce((i, j) -> eD.getRealEigenvalue(i) > eD.getRealEigenvalue(j) ? i : j)
                        .getAsInt();
            RealVector vector = eD.getEigenvector(index);
            double angel =0.0;
            // Only calculate angle on more than one dimensions
            if (vector.getDimension() > 1 ) {
                angel = Math.toDegrees((Math.acos((vector.getEntry(1) / getMagnitude(vector) / 1))));
            }
            printWriter.println(" " + angel);
            printWriterFormated.println("- AngleDegree: " + angel);
            printWriterFormated.println("- EigenVectors: ");
            for (int j = 0; j < dataPoints.get(0).getAttributes().getMaxIndex() + 1; j++) {
                printWriterFormated.println("  - " + eD.getEigenvector(j));
            }
            printWriterFormated.println("");
        }
        printWriter.close();
        printWriterFormated.close();
    }

    /**
     * Helper Method to calculate the magnitude of a vector
     * @param vector vector used to calculate magnitude
     * @return magnitude
     */
    private static double getMagnitude(RealVector vector) {
        double sumQuadrats = 0;
        for (double value: vector.toArray()) {
            sumQuadrats += value*value;
        }
        return Math.sqrt(sumQuadrats);
    }

    /**
     * Read input file and convert it in an array of DataElements
     * @return null -> something went wrong
     * else: Array of DataElements (including Vector of double points)
     */
    private static ArrayList<DataPoint> convertInputData() {
        BufferedReader bufferedReader = checkAndGetInputfile(INPUT_FILE);
        if (bufferedReader == null) {
            return null;
        }

        ArrayList<DataPoint> returnList = new ArrayList();
        bufferedReader.lines().forEach(x -> {
            DataPoint e = new DataPoint(NUM_CLUSTER);
            Stream.of(x.split(" ")).forEach(v -> {
                if (!v.isEmpty()) {
                    // need to set the attributes, because append-method creates a new vector
                    e.setAttributes(e.getAttributes().append(Double.parseDouble(v)));
                }
            });
            returnList.add(e);
        });

        return returnList;
    }

    /**
     * read given input file
     * @param file input file - most *.dat-file
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
