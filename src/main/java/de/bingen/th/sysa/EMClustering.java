package de.bingen.th.sysa;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

public class EMClustering {

    private final static int ITERATIONS = 100;

    // Number of data points (length of x array)
    private final int numData;
    // Number of clusters that should be approximated
    private final int numClusters;
    // Number of different variables (dimension of x vector)
    private final int numVariables;

    // Contains information about how much each point contributes to each cluster
    private RealMatrix responsibility; // numData by numClusters Matrix
    // Data points
    private ArrayList<DataElement> dataPoints; // array size = numData, vector size = numVariables
    // Mean of each cluster
    private ArrayList<RealVector> means; // array size = numClusters, vector size = numVariables
    // Covariance matrix of each cluster
    private ArrayList<RealMatrix> covariances; // array size = numClusters, matrix size = numVariables
    // Probability that a random point in the given data is part of each cluster
    private ArrayList<Double> probabilityForEachCluster;

    public EMClustering(ArrayList<DataElement> inputList, int cluster) {
        this.numClusters = cluster;
        this.dataPoints = inputList;
        numData = inputList.size();
        numVariables = inputList.get(0).getAttributes().getMaxIndex() + 1;

        responsibility = new Array2DRowRealMatrix(numData,numClusters);
        means = new ArrayList<>();
        covariances = new ArrayList<>();

        probabilityForEachCluster = new ArrayList<>();

        // Initialize probability for each cluster with a uniform probability
        for(int c = 0; c < numClusters; ++c) {
            probabilityForEachCluster.add(1.0 / numClusters);
        }


        // Generate random start point for each cluster

        for(int i = 0; i < numClusters; ++i) {
            // Choose a random point as mean for each cluster
            means.add(new ArrayRealVector(dataPoints.get((int)(Math.random() * numData)).getAttributes()));

            covariances.add(new Array2DRowRealMatrix(numVariables, numVariables));
            // Choose random symmetrical matrix for covariances
            for (int j = 0; j < numVariables; j++) {
                for (int k = 0; k < numVariables; k++) {
                    if (k < j) {
                        covariances.get(i).setEntry(k,j,covariances.get(i).getEntry(j,k));
                    } else {
                        covariances.get(i).setEntry(k,j, Math.random() * 100);
                    }
                }
            }
        }

    }

    public void procedure() {
        for (int step = 0; step < ITERATIONS; step++) {
            // Expectation
            for (int i = 0; i < numData; i++) {
                for (int c = 0; c < numClusters; c++) {
                    responsibility.setEntry(i,c,calculateReposibility(c,dataPoints.get(i)));
                }
            }

            // Maximization
            ArrayList<Double> responsibilityForEachCluster = new ArrayList<>();
            for (int c = 0; c < numClusters; c++) {
                // Accumulating how much each cluster is "responsible" for the data seen. Basically giving each cluster its combined weight
                responsibilityForEachCluster.add(0.0);
                for (int i = 0; i < numData; i++) {
                    responsibilityForEachCluster.set(c, responsibilityForEachCluster.get(c) + responsibility.getEntry(i,c));
                }
                // Normalizing the responsibility resulting in an updated probability for each cluster between 0 and 1
                probabilityForEachCluster.set(c,responsibilityForEachCluster.get(c) / numData);

                // Calculating new mean for each cluster
                ArrayRealVector sumVec = new ArrayRealVector(numVariables);
                for (int i = 0; i < numData; i++) {
                    sumVec = sumVec.add(dataPoints.get(i).getAttributes().mapMultiply(responsibility.getEntry(i,c)));
                }
                means.set(c, sumVec.mapMultiplyToSelf(1/responsibilityForEachCluster.get(c)));

                // Recalculate covariance matrix for each cluster
                RealMatrix sumMat = new Array2DRowRealMatrix(numClusters, numVariables);
                for (int i = 0; i < numData; i++) {
                    RealVector diff = dataPoints.get(i).getAttributes().subtract(means.get(c));
                    sumMat = sumMat.add(diff.outerProduct(diff).scalarMultiply(responsibility.getEntry(i,c)));
                }
                covariances.set(c,sumMat.scalarMultiply(1/responsibilityForEachCluster.get(c)));
            }
        }
    }

    public RealMatrix getCovarianceAtIndex(int index) {
        return covariances.get(index);
    }

    private double calculateReposibility(int c, DataElement dataElement) {
        double numerator, denominator = 0;
        numerator = multivariateGaussian(dataElement.getAttributes(), means.get(c), covariances.get(c)) * probabilityForEachCluster.get(c);
        for(int i = 0; i < numClusters; i++) {
            denominator += multivariateGaussian(dataElement.getAttributes(), means.get(i), covariances.get(i)) * probabilityForEachCluster.get(c);
        }
        return numerator / denominator;

    }

    private double multivariateGaussian(RealVector x, RealVector mean, RealMatrix covariance) {
        /*double[] mean_data = mean.toArray();
        double[][] covariance_data = covariance.getData();
        MultivariateNormalDistribution bal = new MultivariateNormalDistribution(mean_data, covariance_data);
        double test = bal.density(x.toArray());*/
        // Calculated in steps so it's easier to check with breakpoints for errors
        RealVector diff = x.subtract(mean);
        RealMatrix inverse = new LUDecomposition(covariance).getSolver().getInverse();
        RealVector vecTimesMat = inverse.preMultiply(diff);
        double dotProduct = vecTimesMat.dotProduct(diff);
        double exponent = dotProduct * (-0.5);
        double determinant =  Math.abs(new LUDecomposition(covariance).getDeterminant());
        double factorPow = Math.pow(2*Math.PI, x.getMaxIndex());
        double v2 = Math.sqrt(factorPow * determinant);
        return Math.exp(exponent) / v2;


    }


}
