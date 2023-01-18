package com.example.sensehatclienfinal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IIRFIlter {

    private Double[] feedforward_coefficients;  //!< Array of IIR filter feedforward coefficients
    private List<Double> stateforward;                 //!< List of IIR filter state values
    private Double[] feedbackward_coefficients;  //!< Array of IIR filter feedbackward coefficients
    private List<Double> statebackward;                 //!< List of IIR filter state values
    Double xf = new Double(0.0);

    public IIRFIlter(Double[] ffc, Double[] fbc, Double[] stf, Double[] stb) {
        feedforward_coefficients = ffc;
        feedbackward_coefficients = fbc;
        stateforward = new ArrayList<>(Arrays.asList(stf));
        statebackward = new ArrayList<>(Arrays.asList(stb));
    }

    public Double Execute(Double x) {
        // update state
        stateforward.add(0, x);
        stateforward.remove(stateforward.size() - 1);

        statebackward.add(0, xf);
        statebackward.remove(stateforward.size() - 1);
        // compute output
        xf = 0.0;
        for (int i = 0; i < stateforward.size(); i++) {
            if (i == 0){
                xf += (feedforward_coefficients[i]*stateforward.get(i));
            }
            else {
                xf += (feedforward_coefficients[i] * stateforward.get(i) - feedbackward_coefficients[i]*statebackward.get(i-1));
            }
        }
        return xf;
    }
}
