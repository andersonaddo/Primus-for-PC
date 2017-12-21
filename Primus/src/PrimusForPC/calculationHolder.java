
package PrimusForPC;

//This class is just a data holder for the calculation types, and some other useful information
public class calculationHolder {
    
    public static final double PHI = 1.6180339887498948482045;
    
    public enum calculationType{
        determineIfPrime,
        factors,
        primeFactors,
        primesUpToX,
        XthPrime,
        primesWithinRange,
        
        findPiRatioWithX,
        piRatiosToX,
        XthPiRatio,
        
        findPhiRatioWithX,
        phiRatioToX,
        XthPhiRatio,       
    }
}
