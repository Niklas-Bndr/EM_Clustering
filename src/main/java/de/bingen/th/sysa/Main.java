package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataPoint;
import org.apache.commons.math3.linear.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Main class to start the EM algorithm
 */
public class Main {

    // configuration parameters
    private final static String INPUT_FILE = "inputData2.dat";
    private final static String RESULT_FILE = "resultData2.dat";
    private final static String RESULT_FILE_FORMATED = "resultData2_formated.dat";
    private final static int NUM_CLUSTER = 2;
    private final static int ITERATIONS = 500;

    /**
     * Start routine to calculate clusters using the EM algorithm
     *
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
        try {
            printWriter = new PrintWriter(new FileWriter(RESULT_FILE_FORMATED));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Cluster cluster: em.getClusters()) {
            EigenDecomposition eD = new EigenDecomposition(cluster.getCovariance());
            printWriter.println(cluster.getIndex()+1 + ". Cluster: ");
            printWriter.println("- Mean: ");
            for (double mean: cluster.getMean().toArray()) {
                printWriter.println("  - " + mean);
            }
            printWriter.println("- RealEigenValues: ");
            for (double eigenValue: eD.getRealEigenvalues()) {
                printWriter.println("  - " + eigenValue);
            }
            // Get index of max Eigenvalue
            int index = IntStream.range(0, eD.getRealEigenvalues().length)
                        .reduce((i, j) -> eD.getRealEigenvalue(i) > eD.getRealEigenvalue(j) ? i : j)
                        .getAsInt();
            RealVector vector = eD.getEigenvector(index);
            /*double acosValue = vector.getEntry(1) / getMagnitude(vector) / 1;
            double toDegressValue = Math.acos(acosValue);
            double angle = Math.toDegrees(toDegressValue);*/
            //double angle = Math.toDegrees((Math.acos(-1*(vector.getEntry(1) / getMagnitude(vector) / 1))));
            double angle =0.0;
            if (vector.getDimension() > 1 ) {
                angle = Math.toDegrees((Math.acos((vector.getEntry(1) / getMagnitude(vector) / 1))));
            }
            printWriter.println("- AngleDegree: " + angle);
            printWriter.println("- EigenVectors: ");
            for (int j = 0; j < dataPoints.get(0).getAttributes().getMaxIndex() + 1; j++) {
                printWriter.println("  - " + eD.getEigenvector(j));
            }
            printWriter.println("");
        }
        printWriter.close();

        try {
            printWriter = new PrintWriter(new FileWriter(RESULT_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Cluster cluster: em.getClusters()) {
            EigenDecomposition eD = new EigenDecomposition(cluster.getCovariance());
            for (double mean: cluster.getMean().toArray()) {
                printWriter.print(" " + mean);
            }
            for (double eigenValue: eD.getRealEigenvalues()) {
                printWriter.print(" " + eigenValue);
            }
            // Get index of max Eigenvalue
            int index = IntStream.range(0, eD.getRealEigenvalues().length)
                    .reduce((i, j) -> eD.getRealEigenvalue(i) > eD.getRealEigenvalue(j) ? i : j)
                    .getAsInt();
            RealVector vector = eD.getEigenvector(index);
            double angle =0.0;
            if (vector.getDimension() > 1 ) {
                angle = Math.toDegrees((Math.acos((vector.getEntry(1) / getMagnitude(vector) / 1))));
            }
            printWriter.println(" " + angle);
        }
        printWriter.close();

    }

    private static double getMagnitude(RealVector vector) {
        double sumQuadrats = 0;
        for (double value: vector.toArray()) {
            sumQuadrats += value*value;
        }
        return Math.sqrt(sumQuadrats);
    }

    /**
     * Read input file and convert it in an array of DataElements
     *
     * @return null -> something went wrong
     * else: Array of DataElements (including Vector of double points)
     */
    private static ArrayList<DataPoint> convertInputData() {
        BufferedReader bufferedReader = checkAndGetInputfile(INPUT_FILE);
        if (bufferedReader == null) {
            return null;
        }

        ArrayList<DataPoint> returnList = new ArrayList();
        // TODO: create a more elegant way
        bufferedReader.lines().forEach(x -> {
            DataPoint e = new DataPoint(NUM_CLUSTER);
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
