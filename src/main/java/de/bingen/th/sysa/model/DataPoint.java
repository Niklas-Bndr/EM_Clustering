package de.bingen.th.sysa.model;

import lombok.Data;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

/**
 * DataPoint: Includes the attributes of a data point and the responsibility per cluster
 */
@Data
public class DataPoint {
    // Attributes of the data point
    private RealVector attributes;
    // Contains information about how much the point contributes to each cluster in percent
    private ArrayList<Double> responsibilityPerCluster;

    /**
     * Constructor
     * @param numCluster necessary to initialize the responsibility per cluster
     */
    public DataPoint(int numCluster) {
        this.attributes = new ArrayRealVector();
        this.responsibilityPerCluster = new ArrayList<>();
        for (int i = 0; i<numCluster;i++) {
            this.responsibilityPerCluster.add(1.0/numCluster);
        }
    }

    /**
     * Help method to determine directly the responsibility of the given cluster
     * @param cluster used cluster
     * @return responsibility
     */
    public Double getResponsibilityPerCluster(Cluster cluster) {
        return getResponsibilityPerCluster().get(cluster.getIndex());
    }

}
