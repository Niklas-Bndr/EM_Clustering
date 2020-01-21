package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

public class EMClustering {

    private final static int ITERATIONS = 100;

    // Number of data points (length of x array)
    private final int numData;
    // Number of clusters that should be approximated
    private final int numClusters;
    // Number of different attributes (numAttributes-vector)
    private final int numAttributes;

    // Data points with probability per cluster
    private ArrayList<DataElement> dataPoints; // array size = numData, vector size = numVariables

    // cluster with mean, whole probability and covariance matrix
    private ArrayList<Cluster> clusters;

    public EMClustering(ArrayList<DataElement> inputList, int cluster) {
        this.numClusters = cluster;
        this.dataPoints = inputList;
        numData = inputList.size();
        numAttributes = inputList.get(0).getAttributes().getMaxIndex() + 1;

        this.clusters = new ArrayList<>();

        // TODO: Comment
        // Choose a random point as mean for each cluster
        // Generate random start point for each cluster
        for (int i = 0; i < numClusters; ++i) {
            Cluster c = new Cluster(
                    new ArrayRealVector(dataPoints.get((int) (Math.random() * numData)).getAttributes()),
                    numClusters,
                    numAttributes);
            clusters.add(c);
        }

    }

    public void procedure() {
        for (int step = 0; step < ITERATIONS; step++) {
            // Expectation - determine probability for each dataPoint
            for (DataElement dataPoint : dataPoints) {
                for (int c = 0; c < numClusters; c++) {
                    dataPoint.getProbabilityPerCluster().set(c,calculateReposibility(c, dataPoint));
                }

            }

            // Maximization
            ArrayList<Double> responsibilityForEachCluster = new ArrayList<>();
            for (int c = 0; c < numClusters; c++) {
                // Accumulating how much each cluster is "responsible" for the data seen. Basically giving each cluster its combined weight
                responsibilityForEachCluster.add(0.0);
                for (int i = 0; i < numData; i++) {
                    responsibilityForEachCluster.set(c,
                            responsibilityForEachCluster.get(c) + dataPoints.get(i).getProbabilityPerCluster().get(c));
                }
                // Normalizing the responsibility resulting in an updated probability for each cluster between 0 and 1
                clusters.get(c).setProbability(responsibilityForEachCluster.get(c) / numData);

                // Calculating new mean for each cluster
                ArrayRealVector sumVec = new ArrayRealVector(numAttributes);
                for (int i = 0; i < numData; i++) {
                    sumVec = sumVec
                            .add(dataPoints.get(i).getAttributes().mapMultiply(dataPoints.get(i).getProbabilityPerCluster().get(c)));
                }
                clusters.get(c).setMean(sumVec.mapMultiplyToSelf(1 / responsibilityForEachCluster.get(c)));

                // Recalculate covariance matrix for each cluster
                RealMatrix sumMat = new Array2DRowRealMatrix(numClusters, numAttributes);
                for (int i = 0; i < numData; i++) {
                    RealVector diff = dataPoints.get(i).getAttributes().subtract(clusters.get(c).getMean());
                    sumMat = sumMat.add(diff.outerProduct(diff).scalarMultiply(dataPoints.get(i).getProbabilityPerCluster().get(c)));
                }
                clusters.get(c).setCovariance(sumMat.scalarMultiply(1 / responsibilityForEachCluster.get(c)));
            }
        }
    }

    public RealMatrix getCovarianceAtIndex(int index) {
        return clusters.get(index).getCovariance();
    }

    private double calculateReposibility(int c, DataElement dataElement) {
        double numerator, denominator = 0;
        numerator = multivariateGaussian(dataElement.getAttributes(), clusters.get(c).getMean(), clusters.get(c).getCovariance()) * clusters.get(c).getProbability();
        for (int i = 0; i < numClusters; i++) {
            denominator += multivariateGaussian(dataElement.getAttributes(), clusters.get(i).getMean(), clusters.get(i).getCovariance()) * clusters.get(c).getProbability();
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
        double determinant = Math.abs(new LUDecomposition(covariance).getDeterminant());
        double factorPow = Math.pow(2 * Math.PI, x.getMaxIndex());
        double v2 = Math.sqrt(factorPow * determinant);
        return Math.exp(exponent) / v2;


    }


}
