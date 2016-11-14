package CryptAIS;
import java.util.*;

/**
 * @author Niallyoung
 * This class represents an Antibody used in the Artificial Immune System
 */
public class Antibody {
    
    //Key is cipher letter, Entry is plaintext letter, This Hashmap is ordered
    public LinkedHashMap<String,String> mapping = new LinkedHashMap();
    
    //The current Decryption of the Antibody
    public String currentDecryption = new String();
    
    //The current Affinity of the Antibody
    public double currentAffinity = 0;
    
    //The amount of mutations its gone through
    public int mutationCount = 0;
    
    //If this antibody has been subject to a Hypermutation
    public boolean hypermutated = false;

    //Constructor
    public Antibody(LinkedHashMap<String,String> mapping)
    {
        this.mapping.putAll(mapping);
    }
    
            
    //This will perform a swap mutation on the antibody's mapping
    //We'll be swapping the entries of the mapping hashmap (plaintext characters)
    public String swapMutate()
    {
        //Random Starting Position
        int initialPosition = randomPosition();

        //This is Step 2a as shown in the development report
        //Array for probabilities
        int[] probability = new int[26];
        //if the chosen letter is on the right hand side then we startfrom the left hand side of the array (index 0)
        if(initialPosition >= 13)
        {
            //For all points on the array (left to right)
            for(int i = 0; i<26; i++)
            {
                if(i < initialPosition)
                {
                    probability[i] = i+1;
                }
                else if(i == initialPosition)
                {
                    probability[i] = 0;
                }
                else
                {
                    int diff = i-initialPosition;
                    probability[i] = i-(diff*2)+1;
                }
            }
        
        }
        //Else if the chosen letter is on the left hand side we startfrom the right hand side of the array (index 25)
        else
        {
            int initialPositionDash = 25-initialPosition;
            //for all points on the array (right to left)
            for(int i = 25; i >=0; i--)
            {
                //find iDash
                int iDash = 25-i;
                if(i > initialPosition)
                {
                    probability[i] = iDash+1;
                }
                else if(i == initialPosition)
                {
                    probability[i] = 0;
                }
                else
                {
                    int diff = iDash-initialPositionDash;
                    probability[i] = iDash-(diff*2)+1;
                }
            }
        }
        
        //Create adjusted array
        double[] probabilityAdjusted = new double[26];
        
        //Populate adjusted Array
        for(int i = 0; i<probability.length; i++)
        {
            if(probability[i]!=0)
            {    
                probabilityAdjusted[i]= Math.pow(2, (probability[i]*0.5));
            }
        }
        
        //Calculate the total sum of the probabilities
        double sumOfProbabilityAdjusted = 0;
        
        for(double i: probabilityAdjusted)
        {
            sumOfProbabilityAdjusted += i;
            //System.out.println(i);
        }
        
        //Calculate a random number between 0 and 1
        Random random = new Random();
        double randomDouble = random.nextDouble();
        
        //Adjust this to the range of the sum
        double randomDoubleAdjusted = randomDouble*sumOfProbabilityAdjusted;
        
        //This will go through the adjusted probability array to find the index it's landed on.
        double counter = randomDoubleAdjusted;
        int swapIndex = 0; //The other number
        for(int i =0; i < probabilityAdjusted.length; i++)
        {
            counter -= probabilityAdjusted[i];
            if(counter <= 0)
            {
                swapIndex = i;
                i=27; //get out of loop
            }
        }
        
        //Calculate the distance of the swap
        int swapDistance = Math.abs(initialPosition-swapIndex);
                
        //This will build the CSV line that we'll return
        String builder = "" + initialPosition + "," + swapIndex + "," + swapDistance;

        //This will swap the entries of the hashmap, as selected in the swap mutation
        int j = 0;
        int k = 0;
        int l = 0;
        String initialHolder = "";
        String swapHolder = "";
        
        //Gets initial Holder
        for(String s:mapping.keySet())
        {
            if(j==initialPosition)
            {
                initialHolder = mapping.get(s);
                //System.out.println(s);
            }
            j++;
        }
        
        //Gets SwapHolder & Sets InitialHolder
        for(String s:mapping.keySet())
        {
            if(k==swapIndex)
            {
                swapHolder = mapping.get(s);
                //System.out.println(s);
                mapping.put(s, initialHolder);
            }
            k++;
        }
        
        //Sets SwapHolder
        for(String s:mapping.keySet())
        {
            if(l==initialPosition)
            {
                //initialHolder = mapping.get(s);
                //System.out.println(s);
                mapping.put(s, swapHolder);
            }
            l++;
        }

        String output = "The letter: " + initialHolder + " has been replaced with: " + swapHolder;
    
        //increment mutation count
        mutationCount++;
        //Can also return the variable 'output'
        return builder;
    }
    
    //This will perform a scramble mutation on the antibody's mapping
    //Based off of the GA handbook, take two points and permute elements between the points
    public void sublistScramble()
    {
        //The start of this shall be the same as the Swap mutate function
        //Random Starting Position
        int initialPosition = randomPosition();
        
        //This is Step 2a as discussed in the development manual
        //Array for probabilities
        int[] probability = new int[26];
        //if starting from the left hand side of the array(0) - The chosen letter is on the right hand side
        if(initialPosition >= 13)
        {
            //For all points on the array (left to right)
            for(int i = 0; i<26; i++)
            {
                if(i < initialPosition)
                {
                    probability[i] = i+1;
                }
                else if(i == initialPosition)
                {
                    probability[i] = 0;
                }
                else
                {
                    int diff = i-initialPosition;
                    probability[i] = i-(diff*2)+1;
                }
            }
        
        }
        //Else starting from the right hand side of the array (25) - the chosen letter is on the left hand side
        else
        {
            int initialPositionDash = 25-initialPosition;
            //for all points on the array (right to left)
            for(int i = 25; i >=0; i--)
            {
                //find i'
                int iDash = 25-i;
                if(i > initialPosition)
                {
                    probability[i] = iDash+1;
                }
                else if(i == initialPosition)
                {
                    probability[i] = 0;
                }
                else
                {
                    int diff = iDash-initialPositionDash;
                    probability[i] = iDash-(diff*2)+1;
                }
            }
        }
        
        //Create adjusted array
        double[] probabilityAdjusted = new double[26];
        
        //Populate adjusted Array
        for(int i = 0; i<probability.length; i++)
        {
            if(probability[i]!=0)
            {    
                probabilityAdjusted[i]= Math.pow(2, (probability[i]*0.5));
            }
        }
        
        //Calculate the total sum of the probabilities
        double sumOfProbabilityAdjusted = 0;
        
        for(double i: probabilityAdjusted)
        {
            sumOfProbabilityAdjusted += i;
        }

        //Calculates a random number between 0 and 1
        Random random = new Random();
        double randomDouble = random.nextDouble();
        
        //Adjusts this to the range of the sum
        double randomDoubleAdjusted = randomDouble*sumOfProbabilityAdjusted;
        
        //This will go through the adjusted probability array to find the value it's landed on.
        double counter = randomDoubleAdjusted;
        int swapIndex = 0; //The other number
        for(int i =0; i < probabilityAdjusted.length; i++)
        {
            counter -= probabilityAdjusted[i];
            if(counter <= 0)
            {
                swapIndex = i;
                i=27; //get out of loop
            }
        }
        
        //Gets the size of the sublist to be scrambled, +1 to include both values (see notes)
        int sublistSize = Math.abs(initialPosition-swapIndex)+1;
        
        //Calculates the lowest/largest values
        int largest;
        int smallest;
        if(initialPosition>swapIndex)
        {
            largest = initialPosition;
            smallest = swapIndex;
        }
        else
        {
            largest = swapIndex;
            smallest = initialPosition;
        }

        //Need to get an array of sublist size for key values and plain values.
        String[] keyTempArray = new String[sublistSize];
        String[] valueTempArray = new String[sublistSize];
        int i = 0;
        int j = 0;
        for(String s:mapping.keySet())
        {
            //if i corresponds to the keys in the swaplist
            if(i >= smallest && i <=largest)
            {
                keyTempArray[j] = s;
                valueTempArray[j] = mapping.get(s);
                j++;
            }
            i++;
        }
        
        //Shuffle ArrayList
        ArrayList<String> shuffler = new ArrayList<>();
        for(String v: valueTempArray)
        {
            shuffler.add(v);
        }
        Collections.shuffle(shuffler);
        
        //Reput shuffled values back into mapping
        for(int s = 0; s<keyTempArray.length; s++)
        {
            mapping.put(keyTempArray[s],shuffler.get(s));
        }

        mutationCount++;//Update mutation count
    }
    
    //returns a random int between 0-26 to be used in the mutation functions
    public int randomPosition()
    {
        Random random = new Random();
        int i = random.nextInt(26);
        return i;
    }
    
}
