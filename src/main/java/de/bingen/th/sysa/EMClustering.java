package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataPoint;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

/**
 * EM-Clustering Algorithm
 */
public class EMClustering {

    // number of data points
    private final int numData;
    // number of clusters
    private final int numClusters;
    // number of different attributes (vector elements)
    private final int numAttributes;
    // number of iterations
    private final int iterations;

    // data points with responsibility per cluster
    private ArrayList<DataPoint> dataPoints;
    // clusters with mean,  probability and covariance matrix
    private ArrayList<Cluster> clusters;

    /**
     * Constructor: initialization step of EM algorithm: choose random means for each cluster
     * @param iterations maximal iterations to break after
     * @param numClusters number of to calculate clusters
     * @param dataPoints all given data points
     */
    public EMClustering(int iterations, int numClusters, ArrayList<DataPoint> dataPoints) {
        this.iterations = iterations;
        this.numClusters = numClusters;
        this.dataPoints = dataPoints;
        numData = dataPoints.size();
        numAttributes = dataPoints.get(0).getAttributes().getDimension();
        clusters = new ArrayList<>();

        // Generate the clusters and give an random start point from the data points
        for (int i = 0; i < numClusters; i++) {
            Cluster c = new Cluster(
                    new ArrayRealVector(dataPoints.get((int) (Math.random() * numData)).getAttributes()),
                    numClusters,
                    numAttributes,
                    i);
            clusters.add(c);
        }

    }

    /**
     * EM-Algorithm: iterate expectation and maximization
     */
    public void procedure() {
        for (int step = 0; step < iterations; step++) {
            // Expectation - determine responsibility for each data point
            performExpectation();
            // Maximization - recalculate mean, covariance and probability for each cluster
            performMaximization();
        }
    }

    /**
     * Expectation step: calculate responsibility for each data point
     */
    private void performExpectation() {
        for (DataPoint dataPoint : dataPoints) {
            calculateResponsibility(dataPoint);
        }
    }

    /**
     * Calculate responsibility for a given data point
     * @param dataPoint data point to recalculate responsibility
     */
    private void calculateResponsibility(DataPoint dataPoint) {
        double denominator = 0.0;
        for (Cluster cluster: clusters) {
            denominator += multivariateGaussianDensity(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance()) * cluster.getProbability();
        }
        for (Cluster cluster: clusters) {
            double numerator = multivariateGaussianDensity(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance()) * cluster.getProbability();
            dataPoint.getResponsibilityPerCluster().set(cluster.getIndex(), numerator / denominator);
        }
    }

    /**
     * Determine the density form a given vector (attributes) to an other vector (mean) of the cluster
     * by using the multivariate gaussian distribution
     * used formel from:
     * * 1-dimension: https://en.wikipedia.org/wiki/Probability_density_function#Families_of_densities
     * * n-dimension: https://en.wikipedia.org/wiki/Multivariate_normal_distribution#Density_function
     * @param attributes data point vector
     * @param mean cluster mean point
     * @param covariance covariance of the cluster
     * @return density of distribution
     */
    private double multivariateGaussianDensity(RealVector attributes, RealVector mean, RealMatrix covariance) {
        /* Seems not to work, so we calculate the density manually:
        double[] mean_data = mean.toArray();
        double[][] covariance_data = covariance.getData();
        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(mean_data, covariance_data);
        double density = mnd.density(attributes.toArray()); */

        // if only one dimension/attribute exists, use an other calculation
        if (numAttributes == 1) {
            double front = 1.0 / Math.sqrt(2 * Math.PI * covariance.getEntry(0,0));
            double exp = Math.exp(-(Math.pow(attributes.getEntry(0)-mean.getEntry(0),2)/(2*covariance.getEntry(0,0))));
            return front * exp;
        }

        /* numerator */
        RealVector diff = attributes.subtract(mean);
        RealMatrix inverseCovariance = new LUDecomposition(covariance).getSolver().getInverse();
        RealVector vecTimesMat = inverseCovariance.preMultiply(diff);
        double dotProduct = vecTimesMat.dotProduct(diff);
        double exp = (-0.5) * dotProduct;
        /* denominator */
        double determinant = Math.abs(new LUDecomposition(covariance).getDeterminant());
        double factorPow = Math.pow(2 * Math.PI, numAttributes);
        double denominator = Math.sqrt(factorPow * determinant);
        return Math.exp(exp) / denominator;
    }

    /**
     * Maximization step: calculate mean, covariance and probability for each cluster
     */
    private void performMaximization() {
        for (Cluster cluster : clusters) {
            double clusterResponsibility = 0.0;
            ArrayRealVector sumMean = new ArrayRealVector(numAttributes);
            RealMatrix sumCovariance = new Array2DRowRealMatrix(numAttributes, numAttributes);
            for (DataPoint dataPoint : dataPoints) {
                // sum the responsibility of each data point
                clusterResponsibility += dataPoint.getResponsibilityPerCluster(cluster);

                // sum the new mean
                sumMean = sumMean
                        .add(dataPoint.getAttributes().mapMultiply(dataPoint.getResponsibilityPerCluster(cluster)));

                // sum the covariance matrix
                RealVector diff = dataPoint.getAttributes().subtract(clusters.get(cluster.getIndex()).getMean());
                sumCovariance = sumCovariance
                        .add(diff.outerProduct(diff).scalarMultiply(dataPoint.getResponsibilityPerCluster(cluster)));

            }
            // set probability by normalizing the responsibility for each cluster (0-1)
            cluster.setProbability(clusterResponsibility / numData);

            // set new mean for given cluster (sumMean/clusterResponsibility)
            cluster.setMean(sumMean.mapMultiplyToSelf(1 / clusterResponsibility));

            // set new covariance matrix for given cluster (sumCovariance/clusterResponsibility)
            cluster.setCovariance(sumCovariance.scalarMultiply(1 / clusterResponsibility));
        }
    }

    /**
     * Help method to get calculated clusters after algorithm
     * @return calculated clusters
     */
    public ArrayList<Cluster> getClusters() {
        return clusters;
    }
}