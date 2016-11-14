package CryptAIS;
import java.util.*;
import java.io.*;

/**
 * @author Niallyoung
 * This class drives the computation of the Artificial Immune System
 */

public class CryptAIS 
{

    //Cipher Text File
    public static String cipherTextFileName = "src\\InputFiles\\cipherMDShorter.txt"; 
    
    //Plain Text File
    public static String plainTextFileName = "src\\InputFiles\\plainMDShorter.txt"; 
    
    //Dictionary Text File
    public static String dictionaryFileName = "src\\DictionaryFiles\\dictionary.txt";
    
    //Lines of the cipher text file
    public static ArrayList<String> input = new ArrayList<>();
    
    //Lines of the plain text file
    public static ArrayList<String> plainInput = new ArrayList<>();
    
    //Hashset of the dictionary list
    public static HashSet<String> dictionary = new HashSet<>();
    
    //Total number of characters in ciphertext
    public static int cipherCharacterCount = 0;
    
    //Total number of words in ciphertext
    public static int cipherWordCount = 0;
    
    //Total number of characters in ciphertext
    public static int plainCharacterCount = 0;
    
    //Chi SquaredValue of the input file
    public static double chiSquaredValue = 0;
    
    //This is the Map for the counting of characters in the Ciphertext.
    public static LinkedHashMap<String,Integer> cipherCharCounter = new LinkedHashMap<>();
    
    //This is the List of unidentified words in a decryption
    public static ArrayList<String> incorrectWords = new ArrayList<>();
    
    //This is the Map for the counting of characters in the Plaintext.
    public static LinkedHashMap<String,Integer> plainCharCounter = new LinkedHashMap<>();
    
    //This is the Map for the frequency of characters in the Ciphertext.
    public static LinkedHashMap<String,Double> ciphertextLetterFrequencies = new LinkedHashMap<>();
    
    //This is the Map for the standard count of characters in the english language.
    public static LinkedHashMap<String,Double> plaintextLetterFrequencies = new LinkedHashMap<>();
    
    //This is the Map for the historical best affinities
    public static LinkedHashMap<Integer,Double> affinityHistory = new LinkedHashMap();
    
    //This is the ArrayList of Probability Tuples for the Ordered list of cipher probabilities
    public static ArrayList<ProbabilityTuple> cipherOrderedProbabilities = new ArrayList<>();
    
    //This is the ArrayList of Probability Tuples for the Ordered list of plain probabilities
    public static ArrayList<ProbabilityTuple> plainOrderedProbabilities = new ArrayList<>();
    
    //This is the first letter mapping based on cipher and plain probabilities
    public static LinkedHashMap<String,String> firstLetterMapping = new LinkedHashMap<>();
    
    //This is the ArrayList to hold all active Antibodies
    public static ArrayList<Antibody> antibodies = new ArrayList<>();
    
    //This is the amount of hypermutated Antibodies in the population
    public final static int hypermutationPopulation = 12;
    
    //This is the threshold at which the system will stop destagnating and terminate
    public final static double affinityThreshold = 0.9;
    
    //This is the amount of stagnant generations before restarting
    public final static int generationTimeOut = 100;
    
    //This is the count of destagnations
    public static int destagnationCounter = 0;
    
    //This is the best ever affinity recorded
    public static double bestRecordedAffinity = 0;
    
    //This is the best Mutation Count
    public static int bestMutationCount = 0;
    
    //This is the best ever decryption recorded
    public static String bestRecordedDecryption = "";
    
    //This is the generation the best occured in
    public static int bestRecordedGeneration = 0;
    
    //This is whether the best has been subject to a hypermutation
    public static boolean bestHypermutated = false;
    
    //This is whether the Termination criteria has been met
    public static boolean terminate = false;
    
    //This will count up the amount of affinity checks performed
    public static int affinityCounter = 0;
    
    //English Alphabet
    public static String alphabet = "abcdefghijklmnopqrstuvwxyz";
    
    //This will run the majority of the logic of the AIS
    public static void main(String[] args) 
    {   
        //Gets Filename for CSV output
        String filename = cipherTextFileName.subSequence(15, cipherTextFileName.length()-4).toString();
        try 
        {
            //Directory for CSV outputs
            File file = new File("/Users/Niall/Documents/Dissertation/NetbeansTestRuns/" + filename + "DictionaryTestData.txt");

            // if file doesnt exists, then create it
            if (!file.exists()) 
            {
                file.createNewFile();
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsoluteFile(),true));
        
        //Start of multiple testing loop
        for(int run = 0; run<1; run++)
        {
            
        //Start of runtime    
        long startTime = System.nanoTime();
        
        //Setup instance of AIS
        CryptAIS cryptAIS = new CryptAIS();
                
        //Get inputs
        cryptAIS.getCipherInput();
        cryptAIS.getPlainInput();
        cryptAIS.getDictionaryInput();
               
        //cryptAIS.printInput();
        //cryptAIS.printPlainInput();
        
        //Set up character counters
        cryptAIS.cipherAlphabetCounter();
        cryptAIS.plainAlphabetCounter();
        cryptAIS.totalCipherCharCount();
        cryptAIS.totalPlainCharCount();
        
        //Calculate frequencies
        cryptAIS.setUpDefaultPlaintextFrequencies();
        //cryptAIS.setUpCornellPlaintextFrequencies();
        //cryptAIS.setUpPlaintextFrequencies();
        cryptAIS.setUpCiphertextFrequencies();
        
        //Optional printing of probabilities and variance
        //cryptAIS.printCipherProbability();
        //cryptAIS.printPlainProbability();
        //cryptAIS.varianceAnalysis();
        
        System.out.println("This input data has a Chi Squared Value of: " + cryptAIS.chiSquaredValue() + ", a total character length of: " + plainCharacterCount + " and a word count of: " + cipherWordCount);
        
        //Order the frequency analysis results
        cipherOrderedProbabilities = cryptAIS.frequencyAnalysisCipherOrdering();
        plainOrderedProbabilities = cryptAIS.frequencyAnalysisPlainOrdering();
        //cryptAIS.printOrdering(cipherOrderedProbabilities);
        //cryptAIS.printOrdering(plainOrderedProbabilities);
        
        //Set up first mapping
        cryptAIS.setupInitialMapping();
        //cryptAIS.printMapping(firstLetterMapping);
        
        //Set up initial global best antibody
        Antibody bestAntibody = new Antibody(firstLetterMapping);
        
        //Setup first population of 36 first Antibodies
        for(int i = 0; i<36;i++)
        {
            antibodies.add(new Antibody(firstLetterMapping));
        }
        
        //START OF AIS Process
        int generation = 0;
        while(!terminate)
        {
            //For all Antibodies mutate, decrypt and affinity check
            for(int i = 0; i<antibodies.size(); i++)
            {
                antibodies.get(i).swapMutate();
                antibodies.get(i).currentDecryption = cryptAIS.decryption(antibodies.get(i)).toString();
                antibodies.get(i).currentAffinity = cryptAIS.affinityCheck(antibodies.get(i).currentDecryption);
            }
            
            //Arraylist to hold antibodies
            ArrayList<Antibody> tempAntibodies = new ArrayList<>();

            //For 5 times go through all antibodies and pick out the top antibody
            for(int j=0;j<5;j++)
            {
                double tempMax = 0;
                //go through all antibodies and pick out the top
                for(int i = 0; i<antibodies.size();i++)
                {
                    if(antibodies.get(i).currentAffinity > tempMax)
                    {
                        tempMax = antibodies.get(i).currentAffinity;
                        //If its better than global best then overwrite global best
                        if(tempMax > bestRecordedAffinity)
                        {
                            bestRecordedAffinity = tempMax;
                            bestRecordedDecryption = antibodies.get(i).currentDecryption;
                            bestHypermutated = antibodies.get(i).hypermutated;
                            bestMutationCount = antibodies.get(i).mutationCount;
                            bestRecordedGeneration = generation;
                            bestAntibody = new Antibody(antibodies.get(i).mapping);
                            bestAntibody.currentAffinity = bestRecordedAffinity;
                            bestAntibody.currentDecryption = bestRecordedDecryption;
                            bestAntibody.mutationCount = bestMutationCount;
                            bestAntibody.hypermutated = bestHypermutated;
                            affinityHistory.put(generation, bestRecordedAffinity);
                        }
                    }
                }
                
                //Add the top then remove from antibodies
                for(int k = 0; k<antibodies.size();k++)
                {
                    if(antibodies.get(k).currentAffinity==tempMax)
                    {
                        tempAntibodies.add(antibodies.get(k));
                        antibodies.remove(antibodies.get(k));
                        k=antibodies.size()+10;
                    }
                }
            }
            
            //Pick out 'hypermutationpopulation' random numbers to be hypermutated, no reoccurance of the same number
            ArrayList<Integer> randomNumbers = new ArrayList<>();
            while(randomNumbers.size()!=hypermutationPopulation)
            {
                Random rand = new Random();
                int tempInt = rand.nextInt(antibodies.size());
                //Make sure it's a unique random number
                if(!randomNumbers.contains(tempInt))
                {
                    randomNumbers.add(tempInt);
                }
            }
            
            //Swap a random number of times between 1-10 then Add these random antibodies to a collection.
            ArrayList<Antibody> hyperMutatedAntibodies = new ArrayList<>();
            for(int z = 0; z<randomNumbers.size(); z++)
            {
                int index = randomNumbers.get(z); //Index for the antibody to be mutated
                
                
                /* COMMENTED OUT SECTION IS USED FOR MULTIPLE SWAP HYPERMUTATION RATHER THAN SCRAMBLE 
                Random rand = new Random();
                int mutationCount = rand.nextInt(10)+1;//Amount of mutations to take place
                
                for(int x = 0; x<mutationCount; x++)
                {
                    antibodies.get(index).swapMutate();
                }
                */
                
                antibodies.get(index).sublistScramble();
                
                antibodies.get(index).hypermutated = true;
                
                hyperMutatedAntibodies.add(antibodies.get(index));
            }

            //Replace the first generation of antibodies with the top 5 duplicated
            antibodies.clear();
            
            //Add the best antibody to the temp antibodies to have 4 additional copies made
            tempAntibodies.add(bestAntibody);
                        
            //For all of the 'x' hypermutated Antibodies, add them back into antibodies population
            for(Antibody a: hyperMutatedAntibodies)
            {
                antibodies.add(a);
            }            
            
            //For all of the top 5 Antibodies, create 4 copies of them and place them back into the population
            for(Antibody a: tempAntibodies)
            {
                for(int x = 0; x<4;x++)
                {
                    Antibody duplicateAntibody = new Antibody(a.mapping);
                    duplicateAntibody.currentAffinity = a.currentAffinity;
                    duplicateAntibody.currentDecryption = a.currentDecryption;
                    duplicateAntibody.mutationCount = a.mutationCount;
                    duplicateAntibody.hypermutated = a.hypermutated;
                    antibodies.add(duplicateAntibody);
                }
            } 

            //Run time outputs
            if(generation%10==0)
            {
                //System.out.println("Generation " + (generation) + " has completed.");
            }
            if(generation%100 == 0 && generation != 0)
            {
                System.out.println("The best decrytion by generation: " + generation + " - " + bestRecordedDecryption);
            }
            
            //This will add in the search destagnation to avoid local optima
            int stagnationPeriod = 0;
            int counter = 0;
            for (int i: affinityHistory.keySet())
            {
                if(counter+1 == affinityHistory.keySet().size())
                {
                    //This is where the computation should happen.
                    int lastGeneration = i;
                    stagnationPeriod = generation-lastGeneration;
                    double lastAffinity = affinityHistory.get(i);
                    
                    //If we've timed out on our search
                    if(stagnationPeriod >generationTimeOut && bestRecordedAffinity <affinityThreshold)
                    {
                        System.out.println("SEARCH DESTAGNATION HAS OCCURED, STAGNATION AFFINITY: " + bestRecordedAffinity );
                        destagnationCounter++;
                        antibodies.clear();

                        //Setup first population of 36 first Antibodies
                        for(int t = 0; t<36;t++)
                        {
                            antibodies.add(new Antibody(firstLetterMapping));
                        }
                        
                        //Reset all best antibody variables
                        bestRecordedAffinity = 0;
                        bestRecordedDecryption = "";
                        bestRecordedGeneration = 0;
                        bestMutationCount = 0;
                        bestHypermutated = false;
                    }
                }
                counter++;
            }
            
            //Termination Check
            //if affinity >90% and stagnant for 100 generations
            if(bestRecordedAffinity >= affinityThreshold && stagnationPeriod >100)
            {
                terminate = true;
                System.out.println("TERMINATION CRITERIA MET, SUCCESSFUL SEARCH");
            }
            generation++;
        }//END OF MAIN LOOP
        
        long stopTime = System.nanoTime();
        long runTime = (stopTime - startTime)/1000000; //This is in milliseconds
        double runTimeDouble = runTime;
        double runTimeSeconds = runTimeDouble/1000; //This is in seconds
        
        /* //OUTPUT FOR AFFINITY HISTORY
        double tempAffinity = 0;
        for(int i = 0; i<generation; i++)
        {
            if(affinityHistory.get(i) != null) tempAffinity = affinityHistory.get(i);
            
            System.out.println("" + i + "," + tempAffinity);
            
        }
        */
        
        //Print out history of best affinities
        System.out.println("History of best affinities");
        for(int i: affinityHistory.keySet())
        {
            System.out.println("Generation: " + i + " had new best affinity of: " + affinityHistory.get(i));
        }
        
        //Getting incorrect word list
        for(String s: bestRecordedDecryption.split(" "))
        {
            //trim off any punctuation
            s = s.replaceAll("^\\p{Punct}+|\\p{Punct}+$", "");
            
            //Check against dictionary
            if(dictionary.contains(s))
            {
                //rightCounter++;
            }
            else
            {
                //wrongCounter++;
                incorrectWords.add(s);                
                //System.out.println(s);
            }
        }
        
        //Output for end of run
        System.out.println("--------------------------------------------------------------------------------------");
        //System.out.println("THE NUMBER OF INCORRECT WORDS: " + incorrectWords.size());
        
        System.out.println("This run terminated after " + generation + " generations. In a time of: " +  runTimeSeconds + " seconds.");
        System.out.println("The input file had a Chi Squared Value of: " + chiSquaredValue + " and a total character length of: " + plainCharacterCount + " and a word count of: " + cipherWordCount);
        
        /*  FOR OUTPUTTING FINAL ANTIBODIES AFFINITIES
        for(int i = 0; i<antibodies.size(); i+=4)
        {
            System.out.println("The number " + ((i/4)+1) + " antibody has an affinity of: " + antibodies.get(i).currentAffinity);
        } 
        */
        
        System.out.println("The best historically achieved decryption had an affinity of: " + bestRecordedAffinity + " in generation: " + bestRecordedGeneration);
        if(bestHypermutated) System.out.println("This best recorded decryption was subject to hypermutation at least once.");
        System.out.println(bestRecordedDecryption);
        System.out.println("--------------------------------------------------------------------------------------");
        
        //Output CSV LINE
        out.write("" + filename + "," + generation + "," + bestRecordedGeneration + "," + runTimeSeconds + "," + bestHypermutated + "," + destagnationCounter);
        out.newLine();
        
        //RESET ALL GLOBAL VARIABLES FOR NEXT RUN
        input = new ArrayList<>();
        plainInput = new ArrayList<>();
        dictionary = new HashSet<>();
        cipherCharacterCount = 0;
        cipherWordCount = 0;
        plainCharacterCount = 0;
        chiSquaredValue = 0;
        cipherCharCounter = new LinkedHashMap<>();
        incorrectWords = new ArrayList<>();
        plainCharCounter = new LinkedHashMap<>();
        ciphertextLetterFrequencies = new LinkedHashMap<>();
        plaintextLetterFrequencies = new LinkedHashMap<>();
        affinityHistory = new LinkedHashMap();
        cipherOrderedProbabilities = new ArrayList<>();
        plainOrderedProbabilities = new ArrayList<>();
        firstLetterMapping = new LinkedHashMap<>();
        antibodies = new ArrayList<>();
        destagnationCounter = 0;
        bestRecordedAffinity = 0;
        bestMutationCount = 0;
        bestRecordedDecryption = "";
        bestRecordedGeneration = 0;
        bestHypermutated = false;
        terminate = false;
        alphabet = "abcdefghijklmnopqrstuvwxyz";
        
        }//END OF OVERALL LOOP

        //Close csv output
        out.close();
        } 
        catch (IOException e){;}
    } //END OF MAIN METHOD
    
    //This will return a %age of words in decryption that match word list
    public double affinityCheck(String input)
    {
        //long affinityStartTime = System.nanoTime();
        
        //Split the string into words
        String[] inputWords = input.split(" ");
        double rightCounter = 0;
        double wrongCounter = 0;
        //For each input word
        for(String s:inputWords)
        {
            //trim off any punctuation
            s = s.replaceAll("^\\p{Punct}+|\\p{Punct}+$", "");
            
            //Check against dictionary
            if(dictionary.contains(s))
            {
                rightCounter++;
            }
            else
            {
                wrongCounter++;

            }
        }
        
        double affinity = rightCounter/(rightCounter+wrongCounter);
        
        /*
        long stopTime = System.nanoTime();
        long runTime = (stopTime - affinityStartTime)/1000; //This is in 0.001 milliseconds
        
        System.out.println("" + runTime);
        */
        return affinity;
    }
    
    //This will print the mapping of cipher to plaintext characters
    public void printMapping(HashMap<String,String> input)
    {
        for(String s:input.keySet())
        {
            System.out.println("Cipher Character: " + s + " maps to Plain Character: " + input.get(s));
        }
    }
    
    //This will calculate the first letter mapping, this requires that frequencyAnalysisCipherOrdering() and frequencyAnalysisPlainOrdering() have been called before it.
    public void setupInitialMapping()
    {
        //For all letters in the cipher 
        for(int i = 0; i<cipherOrderedProbabilities.size(); i++)
        {
            firstLetterMapping.put(cipherOrderedProbabilities.get(i).letter, plainOrderedProbabilities.get(i).letter);
        }
    }
    
    //This will set up the first Antibody
    public Antibody setupFirstAntibody()
    {
        Antibody tempAntibody = new Antibody(firstLetterMapping);
        return tempAntibody;
    }
    
    //This will attempt a decryption from an Antibody
    public ArrayList<String> decryption(Antibody antibody)
    {
        //long decryptStartTime = System.nanoTime(); //Used for time testing
        ArrayList<String> tempList = input;
        ArrayList<String> returnList = new ArrayList<>();
        //For all lines of input cipher
        for(String s:tempList)
        {
            //Split the input into individual characters
            String[] tempArray = s.split("");
            //For all characters in the input
            for(int i = 0; i<tempArray.length; i++)
            {
                //replace all characters in string
                if(alphabet.contains(tempArray[i]))
                {
                    tempArray[i] = antibody.mapping.get(tempArray[i]);
                }
            }
            StringBuilder builder = new StringBuilder();
            
            //FIRST BLOCK WAS EMPTY WITH TEST DATA, MAY NEED TO CHANGE
            for(int i = 0; i<tempArray.length; i++)
            {
                builder.append(tempArray[i]);
            }
            
            s = builder.toString();
            returnList.add(s);
        }

        /* USED IN TIMED TESTING
        long stopTime = System.nanoTime();
        long runTime = (stopTime - decryptStartTime)/1000; //This is in 0.001 milliseconds
        System.out.println("" + runTime);
        */
        
        return returnList;
    }
    
    //This will print a letter ordering
    public void printOrdering(ArrayList<ProbabilityTuple> ordering)
    {
        //Print out Ordering
        for(ProbabilityTuple p: ordering)
        {
            System.out.println("letter: " + p.letter + " probability: " + p.probability);
        }
    }
        
    //This will create the letter ordering for the first Antibody
    public ArrayList frequencyAnalysisCipherOrdering()
    {
        //Arraylist to be returned
        ArrayList<ProbabilityTuple> cipherOrdering = new ArrayList();
        
        //For all letters in the alphabet (A-Z)
        for(String s:ciphertextLetterFrequencies.keySet())
        {
            //Check the probability
            double probability = ciphertextLetterFrequencies.get(s);
            
            //If there's no values insert the first tuple
            if(cipherOrdering.isEmpty())
            {
                ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                cipherOrdering.add(temp);
            }
            //Otherwise look for the first value that is equal or less than the current amount
            else
            {
                //for all current entries in the cipher ordering
                for(int i = 0; i<cipherOrdering.size(); i++)
                {
                    //If tuple probability is less, insert this value
                    if(cipherOrdering.get(i).probability<probability)
                    {
                        ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                        cipherOrdering.add(i,temp);
                        i = cipherOrdering.size()+1; //jump to end of loop
                    }
                    //Else if we're at the end of the list (-1 since address starts at 0 and size starts at 1), add the value
                    else if(i == cipherOrdering.size()-1)
                    {
                        ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                        cipherOrdering.add(temp);
                        i = cipherOrdering.size()+1; //jump to end of loop
                    }
                }
            }
        }
        return cipherOrdering;
    }
        
    //This will create the letter ordering for the first Antibody
    public ArrayList frequencyAnalysisPlainOrdering()
    {
        //Arraylist to be returned
        ArrayList<ProbabilityTuple> plainOrdering = new ArrayList();
        
        //For all letters in the alphabet
        for(String s:plaintextLetterFrequencies.keySet())
        {
            //Check the probability
            double probability = plaintextLetterFrequencies.get(s);
            
            //If there's no values insert the first tuple
            if(plainOrdering.isEmpty())
            {
                ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                plainOrdering.add(temp);
            }
            //Otherwise look for the first value that is equal or less than the current amount
            else
            {
                //for all current entries in the cipher ordering
                for(int i = 0; i<plainOrdering.size(); i++)
                {
                    //If tuple probability is less, insert this value
                    if(plainOrdering.get(i).probability<probability)
                    {
                        ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                        plainOrdering.add(i,temp);
                        i = plainOrdering.size()+1; //jump to end of loop
                    }
                    //Else if we're at the end of the list (-1 since address starts at 0 and size starts at 1), add the value
                    else if(i == plainOrdering.size()-1)
                    {
                        ProbabilityTuple temp = new ProbabilityTuple(s,probability);
                        plainOrdering.add(temp);
                        i = plainOrdering.size()+1; //jump to end of loop
                    }
                }
            }
        }
        return plainOrdering;
    }
        
    //This will print the probabilities of cipher characters. 
    public void printCipherProbability()
    {
        for(String S:ciphertextLetterFrequencies.keySet())
        {
            System.out.println("The Cipher Character: " + S + " has a " + ciphertextLetterFrequencies.get(S) + " occurance probability");
        }
    }
    
    //This will print the probabilities of plain characters. 
    public void printPlainProbability()
    {
        for(String S:plaintextLetterFrequencies.keySet())
        {
            System.out.println("The Plain Character: " + S + " has a " + plaintextLetterFrequencies.get(S) + " occurance probability");
        }
    }
    
    //This will calculate the variance between cipher and Plaintext frequencies
    public void varianceAnalysis()
    {
        //for each letter compare the plaintext/ciphertext
        for(String S:plaintextLetterFrequencies.keySet())
        {
            double percentage = (double)Math.round(((ciphertextLetterFrequencies.get(S)-plaintextLetterFrequencies.get(S))/ciphertextLetterFrequencies.get(S))*100);
            System.out.println("The Character: " + S + " has a " + percentage + "% variance between plaintext and ciphertext");
        }
    }
    
    //This will calculate the Chi Squared Value for a solution
    public double chiSquaredValue()
    {
        double chiValue = 0;
        //For all letters A-Z, sum the, count minus the expected count, squared, divided by the expected count
        for(String s:plainCharCounter.keySet())
        {
            //need to find total number of characters
            double c = plainCharCounter.get(s); //Actual Count
            double e = plainCharacterCount * plaintextLetterFrequencies.get(s); //Expected Count
            chiValue += ((Math.pow(c-e,2))/(e));
        }
        chiSquaredValue = chiValue;
        return chiValue;
    }
    
    //This will set up the Cornell Frequencies
    public void setUpCornellPlaintextFrequencies()
    {
        plaintextLetterFrequencies.put("a", 0.0812);
        plaintextLetterFrequencies.put("b", 0.0149);
        plaintextLetterFrequencies.put("c", 0.0271);
        plaintextLetterFrequencies.put("d", 0.0432);
        plaintextLetterFrequencies.put("e", 0.1202);
        plaintextLetterFrequencies.put("f", 0.0230);
        plaintextLetterFrequencies.put("g", 0.0203);
        plaintextLetterFrequencies.put("h", 0.0592);
        plaintextLetterFrequencies.put("i", 0.0731);
        plaintextLetterFrequencies.put("j", 0.0010);
        plaintextLetterFrequencies.put("k", 0.0069);
        plaintextLetterFrequencies.put("l", 0.0398);
        plaintextLetterFrequencies.put("m", 0.0261);
        plaintextLetterFrequencies.put("n", 0.0695);
        plaintextLetterFrequencies.put("o", 0.0768);
        plaintextLetterFrequencies.put("p", 0.0182);
        plaintextLetterFrequencies.put("q", 0.0011);
        plaintextLetterFrequencies.put("r", 0.0602);
        plaintextLetterFrequencies.put("s", 0.0628);
        plaintextLetterFrequencies.put("t", 0.0910);
        plaintextLetterFrequencies.put("u", 0.0288);
        plaintextLetterFrequencies.put("v", 0.0111);
        plaintextLetterFrequencies.put("w", 0.0209);
        plaintextLetterFrequencies.put("x", 0.0017);
        plaintextLetterFrequencies.put("y", 0.0211);
        plaintextLetterFrequencies.put("z", 0.0007);
    }
    
    //This will take the standard frequencies from the english language as found in Robert Lewand's book Cryptological mathematics: http://bit.ly/1UonJo9
    public void setUpDefaultPlaintextFrequencies()
    {
        plaintextLetterFrequencies.put("a", 0.08167);
        plaintextLetterFrequencies.put("b", 0.01492);
        plaintextLetterFrequencies.put("c", 0.02782);
        plaintextLetterFrequencies.put("d", 0.04253);
        plaintextLetterFrequencies.put("e", 0.12702);
        plaintextLetterFrequencies.put("f", 0.02228);
        plaintextLetterFrequencies.put("g", 0.02015);
        plaintextLetterFrequencies.put("h", 0.06094);
        plaintextLetterFrequencies.put("i", 0.06966);
        plaintextLetterFrequencies.put("j", 0.00153);
        plaintextLetterFrequencies.put("k", 0.00772);
        plaintextLetterFrequencies.put("l", 0.04025);
        plaintextLetterFrequencies.put("m", 0.02406);
        plaintextLetterFrequencies.put("n", 0.06749);
        plaintextLetterFrequencies.put("o", 0.07507);
        plaintextLetterFrequencies.put("p", 0.01929);
        plaintextLetterFrequencies.put("q", 0.00095);
        plaintextLetterFrequencies.put("r", 0.05987);
        plaintextLetterFrequencies.put("s", 0.06327);
        plaintextLetterFrequencies.put("t", 0.09056);
        plaintextLetterFrequencies.put("u", 0.02758);
        plaintextLetterFrequencies.put("v", 0.00978);
        plaintextLetterFrequencies.put("w", 0.02360);
        plaintextLetterFrequencies.put("x", 0.00150);
        plaintextLetterFrequencies.put("y", 0.01974);
        plaintextLetterFrequencies.put("z", 0.00074);
    }
    
    //This will calculate the frequencies from the ciphertext given
    public void setUpCiphertextFrequencies()
    {
        //for all letters in the alphabet calculate the %age(5 decimal places) probability        
        for(String S:cipherCharCounter.keySet())
        {
            //probability rounded to 5 decimal places
            double probability = (double)Math.round(   ((double) cipherCharCounter.get(S)/cipherCharacterCount) * 100000d  )/100000d;
            ciphertextLetterFrequencies.put(S,probability);
        }
        
    }
    
    //This will calculate the frequencies from the plaintext given
    public void setUpPlaintextFrequencies()
    {
        //for all letters in the alphabet calculate the %age(5 decimal places) probability        
        for(String S:plainCharCounter.keySet())
        {
            //probability rounded to 5 decimal places
            double probability = (double)Math.round(   ((double) plainCharCounter.get(S)/plainCharacterCount) * 100000d  )/100000d;
            plaintextLetterFrequencies.put(S,probability);
        }
        
    }
    
    //this method gets the total character count
    public void totalCipherCharCount()
    {
        for(String S:cipherCharCounter.keySet())
        {
            cipherCharacterCount += cipherCharCounter.get(S);
        }
    }
    
    //this method gets the total character count
    public void totalPlainCharCount()
    {
        for(String S:plainCharCounter.keySet())
        {
            plainCharacterCount += plainCharCounter.get(S);
        }
    }
    
    //this method populates the cipherCharCounter
    public void cipherAlphabetCounter()
    {        
        for(int i = 0; i<alphabet.length(); i++)
        {
            char temp = alphabet.charAt(i);
            cipherCharCounter.put(""+temp, 0);
        }
        
        //For each line of input
        for(String line:input)
        {
            //Setup word count in global variable cipherWordCount
            String[] wordArray = line.split(" ");
            for(String s: wordArray)
            {
                cipherWordCount++;
            }
            
            //For each letter
            for(int i = 0; i<line.length(); i++)
            {                
                //Increment letter total
                char temp = line.charAt(i);
                if(alphabet.contains("" + temp))
                {
                    cipherCharCounter.put(""+temp, cipherCharCounter.get(""+temp)+1 );
                }
            }
        }
    }
    
    //this method populates the plainCharCounter
    public void plainAlphabetCounter()
    {
        for(int i = 0; i<alphabet.length(); i++)
        {
            char temp = alphabet.charAt(i);
            plainCharCounter.put(""+temp, 0);
        }
        
        //For each line of input
        for(String line:plainInput)
        {
            //For each letter
            for(int i = 0; i<line.length(); i++)
            {
                //Increment letter total
                char temp = line.charAt(i);
                if(alphabet.contains("" + temp))
                {
                    plainCharCounter.put(""+temp, plainCharCounter.get(""+temp)+1 );
                }
            }
        }
    }
    
    //Print Cipher Input
    public void printInput()
    {
        for(int i = 0; i<input.size(); i++ )
        {
            System.out.println(input.get(i));
        }
    }
    
    //Print Plain Input
    public void printPlainInput()
    {
        for(int i = 0; i<plainInput.size(); i++ )
        {
            System.out.println(plainInput.get(i));
        }
    }
    
    //This method populates the raw arraylist input
    public void getCipherInput()
    {
		
        String line;
		
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(cipherTextFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //Values to keep place in the input
            int blankCounter = 0;
            //int lineCounter = 0;
            String currentVariableName = "";
            
            //While there are still lines to be read
            while((line = bufferedReader.readLine()) != null) 
            {
            	//add to arraylist
                input.add(line.toLowerCase());
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) 
	{
            System.out.println("Unable to open file '" + cipherTextFileName + "'");
        }
        catch(IOException ex) 
	{
        	System.out.println("Error reading file '" + cipherTextFileName + "'");
        }
    }
     
    //This method populates the raw arraylist input
    public void getPlainInput()
    {
		
        String line;
		
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(plainTextFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //Values to keep place in the input
            int blankCounter = 0;
            //int lineCounter = 0;
            String currentVariableName = "";
            
            //While there are still lines to be read
            while((line = bufferedReader.readLine()) != null) 
            {
            	//add to arraylist
                plainInput.add(line.toLowerCase());
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) 
	{
            System.out.println("Unable to open file '" + plainTextFileName + "'");
        }
        catch(IOException ex) 
	{
        	System.out.println("Error reading file '" + plainTextFileName + "'");
        }
    }

    //This method populates the raw arraylist input
    public void getDictionaryInput()
    {
		
        String line;
		
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(dictionaryFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //Values to keep place in the input
            int blankCounter = 0;
            //int lineCounter = 0;
            String currentVariableName = "";
            
            //While there are still lines to be read
            while((line = bufferedReader.readLine()) != null) 
            {
            	//add to Hashset
                dictionary.add(line.toLowerCase());
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) 
	{
            System.out.println("Unable to open file '" + dictionaryFileName + "'");
        }
        catch(IOException ex) 
	{
        	System.out.println("Error reading file '" + dictionaryFileName + "'");
        }
    }
}