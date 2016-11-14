package CryptAIS;

/**
 * @author Niallyoung
 * This class represents a tuple of a letter and a frequency probability.
 */
public class ProbabilityTuple {
    
    public String letter;
    public double probability;
    
    //Constructor
    public ProbabilityTuple(String letter,double probability)
    {
        this.letter = letter;
        this.probability = probability;
    }
}
