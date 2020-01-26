package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataPoint;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

public class EMClustering {

    // number of data points
    private final int noData;
    // number of clusters
    private final int noClusters;
    // number of different attributes (vector elements)
    private final int noAttributes;
    // number of iterations
    private final int iterations;

    // data points with responsibility per cluster
    private ArrayList<DataPoint> dataPoints; // array size = numData, vector size = numVariables

    // cluster with mean, whole probability and covariance matrix
    private ArrayList<Cluster> clusters;

    public EMClustering(int iterations, int cluster, ArrayList<DataPoint> dataPoints) {
        this.iterations = iterations;
        this.noClusters = cluster;
        this.dataPoints = dataPoints;
        noData = dataPoints.size();
        noAttributes = dataPoints.get(0).getAttributes().getMaxIndex() + 1;

        clusters = new ArrayList<>();

        // TODO: Comment
        // Choose a random point as mean for each cluster
        // Generate random start point for each cluster
        for (int i = 0; i < noClusters; i++) {
            Cluster c = new Cluster(
                    new ArrayRealVector(dataPoints.get((int) (Math.random() * noData)).getAttributes()),
                    noClusters,
                    noAttributes,
                    i);
            clusters.add(c);
        }

    }

    public void procedure() {
        for (int step = 0; step < iterations; step++) {
            // Expectation - determine responsibility for each dataPoint
            performExpectation();

            // Maximization
            performMaximization();
        }
    }

    private void performExpectation() {
        for (DataPoint dataPoint : dataPoints) {
            calculateResponsibility(dataPoint);
        }
    }

    private void performMaximization() {
        for (Cluster cluster : clusters) {
            double clusterResponsibility = 0.0;
            ArrayRealVector sumVector = new ArrayRealVector(noAttributes);
            RealMatrix sumMatrix = new Array2DRowRealMatrix(noClusters, noAttributes);

            for (DataPoint dataPoint : dataPoints) {
                // Accumulating how much the cluster is responsible for the data set.
                // Basically giving each cluster its combined weight
                clusterResponsibility += dataPoint.getResponsibilityPerCluster(cluster);

                // Calculating new mean for each cluster
                sumVector = sumVector
                        .add(dataPoint.getAttributes().mapMultiply(dataPoint.getResponsibilityPerCluster(cluster)));

                // Recalculate covariance matrix for each cluster
                RealVector diff = dataPoint.getAttributes().subtract(clusters.get(cluster.getIndex()).getMean());
                sumMatrix = sumMatrix
                        .add(diff.outerProduct(diff).scalarMultiply(dataPoint.getResponsibilityPerCluster(cluster)));

            }
            // set probability by normalizing the responsibility for each cluster (0-1)
            cluster.setProbability(clusterResponsibility / noData);

            // set new mean for each cluster
            cluster.setMean(sumVector.mapMultiplyToSelf(1 / clusterResponsibility));

            // Set new covariance matrix for given cluster
            cluster.setCovariance(sumMatrix.scalarMultiply(1 / clusterResponsibility));
        }
    }

    public RealMatrix getCovarianceAtIndex(int index) {
        return clusters.get(index).getCovariance();
    }

    private void calculateResponsibility(DataPoint dataPoint) {
        double denominator = 0.0;
        for (Cluster cluster: clusters) {
            denominator += multivariateGaussian(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance());
        }
        for (Cluster cluster: clusters) {
            double numerator = multivariateGaussian(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance()) * cluster.getProbability();
            double responsibilityPerCluster = numerator / (denominator * cluster.getProbability());
            dataPoint.getResponsibilityPerCluster().set(cluster.getIndex(), responsibilityPerCluster);
        }
    }

    private double multivariateGaussian(RealVector attributes, RealVector mean, RealMatrix covariance) {
        /*double[] mean_data = mean.toArray();
        double[][] covariance_data = covariance.getData();
        MultivariateNormalDistribution bal = new MultivariateNormalDistribution(mean_data, covariance_data);
        double test = bal.density(attributes.toArray());*/
        // Calculated in steps so it's easier to check with breakpoints for errors
        RealVector diff = attributes.subtract(mean);
        RealMatrix inverse = new LUDecomposition(covariance).getSolver().getInverse();
        RealVector vecTimesMat = inverse.preMultiply(diff);
        double dotProduct = vecTimesMat.dotProduct(diff);
        double exponent = dotProduct * (-0.5);
        double determinant = Math.abs(new LUDecomposition(covariance).getDeterminant());
        double factorPow = Math.pow(2 * Math.PI, attributes.getMaxIndex());
        double v2 = Math.sqrt(factorPow * determinant);
        return Math.exp(exponent) / v2;


    }


}
