package de.bingen.th.sysa.model;

import lombok.Data;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

@Data
public class Cluster {

    // Mean of each cluster
    private RealVector mean; // array size = numClusters, vector size = numVariables
    // Covariance matrix of each cluster
    private RealMatrix covariance; // array size = numClusters, matrix size = numVariables
    // TODO:
    // Probability that a random point in the given data is part of each cluster
    private double probability;

    public Cluster(RealVector mean, int numCluster, int numAttributes){
        this.mean = mean;
        this.probability = 1.0 / numCluster;

        // create a random symmetric matrix for the covariance
        this.covariance = new Array2DRowRealMatrix(numAttributes, numAttributes);
        for (int i = 0; i < numAttributes; i++) {
            for (int j = 0; j < numAttributes; j++) {
                if (j > i) {
                    this.covariance.setEntry(j, i, Math.random() * 100);
                } else {
                    this.covariance.setEntry(j, i, this.covariance.getEntry(i,j));
                }
            }
        }
    }
}
