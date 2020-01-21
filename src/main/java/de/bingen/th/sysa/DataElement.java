package de.bingen.th.sysa;

import lombok.Data;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

@Data
public class DataElement {
    private RealVector attributes;
    private RealVector probabilityHiddenProperties;

    public DataElement() {
        this.attributes = new ArrayRealVector();
        this.probabilityHiddenProperties = new ArrayRealVector();
    }

    public DataElement(DataElement copyDataElement) {
        this.attributes = copyDataElement.getAttributes().copy();
        this.probabilityHiddenProperties = copyDataElement.getProbabilityHiddenProperties().copy();
    }
}
