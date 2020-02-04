package de.bingen.th.sysa;

import de.bingen.th.sysa.model.Cluster;
import de.bingen.th.sysa.model.DataPoint;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

/**
 *
 */
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

    /**
     *
     * @param iterations
     * @param cluster
     * @param dataPoints
     */
    public EMClustering(int iterations, int cluster, ArrayList<DataPoint> dataPoints) {
        this.iterations = iterations;
        this.noClusters = cluster;
        this.dataPoints = dataPoints;
        noData = dataPoints.size();
        noAttributes = dataPoints.get(0).getAttributes().getDimension();

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

    /**
     *
     */
    public void procedure() {
        for (int step = 0; step < iterations; step++) {
            // Expectation - determine responsibility for each dataPoint
            performExpectation();

            // Maximization
            performMaximization();
        }
    }

    /**
     *
     */
    private void performExpectation() {
        for (DataPoint dataPoint : dataPoints) {
            calculateResponsibility(dataPoint);
        }
    }

    /**
     *
     */
    private void performMaximization() {
        for (Cluster cluster : clusters) {
            double clusterResponsibility = 0.0;
            ArrayRealVector sumVector = new ArrayRealVector(noAttributes);
            RealMatrix sumMatrix = new Array2DRowRealMatrix(noAttributes, noAttributes);
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

    /**
     *
     * @param dataPoint
     */
    private void calculateResponsibility(DataPoint dataPoint) {
        double denominator = 0.0;
        for (Cluster cluster: clusters) {
            denominator += multivariateGaussianDensity(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance()) * cluster.getProbability();
        }
        for (Cluster cluster: clusters) {
            double numerator = multivariateGaussianDensity(dataPoint.getAttributes(), cluster.getMean(), cluster.getCovariance()) * cluster.getProbability();
            double responsibilityPerCluster = numerator / denominator;
            dataPoint.getResponsibilityPerCluster().set(cluster.getIndex(), responsibilityPerCluster);
        }
    }

    /**
     *
     * @param attributes
     * @param mean
     * @param covariance
     * @return
     */
    /* formel from https://en.wikipedia.org/wiki/Multivariate_normal_distribution#Density_function */
    private double multivariateGaussianDensity(RealVector attributes, RealVector mean, RealMatrix covariance) {
        /* Seems not to work, so we calculate the density manually:
        double[] mean_data = mean.toArray();
        double[][] covariance_data = covariance.getData();
        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(mean_data, covariance_data);
        double test = mnd.density(attributes.toArray()); */

        /* if only one dimension/attribute exists, use https://en.wikipedia.org/wiki/Probability_density_function#Families_of_densities */
        if (noAttributes == 1) {
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
        double factorPow = Math.pow(2 * Math.PI, noAttributes);
        double denominator = Math.sqrt(factorPow * determinant);
        return Math.exp(exp) / denominator;
    }

    /**
     *
     * @return
     */
    public ArrayList<Cluster> getClusters() {
        return clusters;
    }
}