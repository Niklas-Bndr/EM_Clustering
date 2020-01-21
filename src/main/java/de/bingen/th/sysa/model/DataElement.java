package de.bingen.th.sysa.model;

import lombok.Data;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

@Data
public class DataElement {
    private RealVector attributes;

    // Contains information about how much each point contributes to each cluster
    private ArrayList<Double> probabilityPerCluster;//numData by numClusters Matrix

    public DataElement(int numCluster) {
        this.attributes = new ArrayRealVector();
        this.probabilityPerCluster = new ArrayList<>();
        for (int i = 0; i<numCluster;i++) {
            this.probabilityPerCluster.add(1.0/numCluster);
        }
    }

}
