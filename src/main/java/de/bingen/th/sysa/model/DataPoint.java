package de.bingen.th.sysa.model;

import lombok.Data;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

@Data
public class DataPoint {
    private RealVector attributes;

    // Contains information about how much the point contributes to each cluster
    private ArrayList<Double> responsibilityPerCluster;

    public DataPoint(int numCluster) {
        this.attributes = new ArrayRealVector();
        this.responsibilityPerCluster = new ArrayList<>();
        for (int i = 0; i<numCluster;i++) {
            this.responsibilityPerCluster.add(1.0/numCluster);
        }
    }

    public Double getResponsibilityPerCluster(Cluster cluster) {
        return getResponsibilityPerCluster().get(cluster.getIndex());
    }

}
