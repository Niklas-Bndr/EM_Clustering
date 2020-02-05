package de.bingen.th.sysa.model;

import lombok.Data;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Cluster: calculated cluster of data points with mean, covariance and probability
 */
@Data
public class Cluster {
    // calculated mean of the cluster
    private RealVector mean;
    // covariance matrix of the cluster
    private RealMatrix covariance;
    // probability of the responsibility to all data points proportional to all clusters
    private double probability;
    // index of the cluster
    private final int index;

    /**
     * Constructor
     * @param mean random mean given from a data point
     * @param numCluster number of clusters to calculate the start probability
     * @param numAttributes number of attributes, necessary to initialize the covariance matrix
     * @param index fix index position for responsibilityPerCluster information in dataPoint
     */
    public Cluster(RealVector mean, int numCluster, int numAttributes, int index){
        this.mean = mean;
        this.probability = 1.0 / numCluster;
        this.index = index;

        // create a random symmetric matrix for the covariance
        this.covariance = new Array2DRowRealMatrix(numAttributes, numAttributes);
        // only necessary on more dimensional data points
        if (numAttributes == 1) {
            this.covariance.setEntry(0,0, Math.random() * 10);
        } else {
            for (int i = 0; i < numAttributes; i++) {
                for (int j = 0; j < numAttributes; j++) {
                    if (j > i) {
                        this.covariance.setEntry(j, i, Math.random()*3);
                    } else {
                        this.covariance.setEntry(j, i, this.covariance.getEntry(i,j));
                    }
                }
            }
        }
    }
}
