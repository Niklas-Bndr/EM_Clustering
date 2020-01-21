package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataElement;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

public class EMClustering {



    // Number of data points (length of x array)
    private final int numData;
    // Number of clusters that should be approximated
    private final int numClusters;
    // Number of different attributes (numAttributes-vector)
    private final int numAttributes;
    // number of iterations
    private final int iterations;

    // Data points with probability per cluster
    private ArrayList<DataElement> dataPoints; // array size = numData, vector size = numVariables

    // cluster with mean, whole probability and covariance matrix
    private ArrayList<Cluster> clusters;

    public EMClustering(int iterations, int cluster, ArrayList<DataElement> dataPoints) {
        this.iterations = iterations;
        this.numClusters = cluster;
        this.dataPoints = dataPoints;
        numData = dataPoints.size();
        numAttributes = dataPoints.get(0).getAttributes().getMaxIndex() + 1;

        clusters = new ArrayList<>();

        // TODO: Comment
        // Choose a random point as mean for each cluster
        // Generate random start point for each cluster
        for (int i = 0; i < numClusters; i++) {
            Cluster c = new Cluster(
                    new ArrayRealVector(dataPoints.get((int) (Math.random() * numData)).getAttributes()),
                    numClusters,
                    numAttributes,
                    i);
            clusters.add(c);
        }

    }

    public void procedure() {
        for (int step = 0; step < iterations; step++) {
            // Expectation - determine probability for each dataPoint
            performExpectation();

            // Maximization
            performMaximization();
        }
    }

    private void performExpectation() {
        for (DataElement dataPoint : dataPoints) {
            for (int c = 0; c < numClusters; c++) {
                dataPoint.getProbabilityPerCluster().set(c, calculateReposibility(c, dataPoint));
            }
        }
    }

    private void performMaximization() {
        for (Cluster cluster : clusters) {
            double clusterResponsibility = 0.0;
            ArrayRealVector sumVector = new ArrayRealVector(numAttributes);
            RealMatrix sumMatrix = new Array2DRowRealMatrix(numClusters, numAttributes);

            for (DataElement dataPoint : dataPoints) {
                // Accumulating how much the cluster is responsible for the data set.
                // Basically giving each cluster its combined weight
                clusterResponsibility += dataPoint.getProbabilityPerCluster().get(cluster.getIndex());

                // Calculating new mean for each cluster
                sumVector = sumVector
                        .add(dataPoint.getAttributes().mapMultiply(dataPoint.getProbabilityPerCluster().get(cluster.getIndex())));

                // Recalculate covariance matrix for each cluster
                RealVector diff = dataPoint.getAttributes().subtract(clusters.get(cluster.getIndex()).getMean());
                sumMatrix = sumMatrix
                        .add(diff.outerProduct(diff).scalarMultiply(dataPoint.getProbabilityPerCluster().get(cluster.getIndex())));

            }
            // set probability by normalizing the responsibility for each cluster (0-1)
            cluster.setProbability(clusterResponsibility / numData);

            // set new mean for each cluster
            cluster.setMean(sumVector.mapMultiplyToSelf(1 / clusterResponsibility));

            // Set new covariance matrix for given cluster
            cluster.setCovariance(sumMatrix.scalarMultiply(1 / clusterResponsibility));
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
