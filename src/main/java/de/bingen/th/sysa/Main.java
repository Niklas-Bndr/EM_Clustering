package de.bingen.th.sysa;

import org.apache.commons.math3.linear.*;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Main {

    private final static String INPUT_FILE = "inputData.dat";
    private final static String RESULT_FILE = "resultData.dat";

    private final static int CLUSTER = 2;


    public static void main(String... args)  {
        BufferedReader bufferedReader = checkAndGetInputfile(INPUT_FILE);
        ArrayList<DataElement> inputList = convertInputData(bufferedReader);
        EMClustering em = new EMClustering(inputList, CLUSTER);
        em.procedure();

        PrintWriter printWriter = null;
        try {
             printWriter = new PrintWriter(new FileWriter(RESULT_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        RealVector eigenValues = new ArrayRealVector();
        RealMatrix eigenVectors = new Array2DRowRealMatrix();

        for(int i = 0; i < CLUSTER; i++) {
            EigenDecomposition eD = new EigenDecomposition(em.getCovarianceAtIndex(i));

            double[] test = eD.getRealEigenvalues();
            double[] test2 = eD.getImagEigenvalues();


            /*eigenValues = eD.getEigenvector(i);
            eigenVectors = eD.
            EigenPair eigenPair = emClustering.getCovarianceAtIndex(i).eigen();
            eigenvalues = eigenPair.getEigenvalues();
            eigenvectors = eigenPair.getEigenvectors();

            int maxEvIndex = eigenvalues.getIndexOfMax();
            int secondMaxEvIndex = eigenvalues.getIndexOfSecondMax();

            double meanX = emClustering.getMeanAtIndex(i).get(0);
            double meanY = emClustering.getMeanAtIndex(i).get(1);

            double maxEv = eigenvalues.get(maxEvIndex);
            double secondMaxEv = eigenvalues.get(secondMaxEvIndex);

            Vec eigenvector = eigenvectors.getColumnVecOfIndex(maxEvIndex);

            double angle = eigenvector.angleInDeg(new Vec(0, 1)); // angle to y axis

            printWriter.println(meanX + " " + meanY + " " + maxEv + " " + secondMaxEv + " " + angle);*/
        }

        printWriter.close();

    }

    private static BufferedReader checkAndGetInputfile(String file) {
        try {
            return new BufferedReader(new FileReader(new File(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList convertInputData(BufferedReader bufferedReader) {
        ArrayList<DataElement> returnList = new ArrayList();
        // TODO: create a more elegant way
        bufferedReader.lines().forEach(x -> {
            DataElement e = new DataElement(CLUSTER);
            // TODO: chaos
            ArrayList<Double> values = new ArrayList<>();
            Stream.of(x.split(" ")).forEach(v -> {
                if (!v.isEmpty()) {
                    values.add(Double.parseDouble(v));
                }
            });

            Double[] tmp = new Double[values.size()];
            for (int i = 0; i<values.size();i++) {
                tmp[i] = values.get(i);
            }
            e.setAttributes(new ArrayRealVector(tmp));
            returnList.add(e);
        });

        return returnList;
    }
}
