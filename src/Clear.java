// Clears out the questions that can't be answered either becaause the answer name doesn't exists in freebase
// or because their is no entities for us to start from.
import java.io.*;
import java.util.*;

public class Clear {
    //---STATIC VARIABLES---
    private static String configpath;
    private static String filepath;
    private static String dbURL = null;
    private static String dbUser = null;
    private static String dbPass = null;
    private static boolean isRetrieved = false;
    private static boolean isTagged = false;
    private static int startIndex = 0;
    private static int endIndex = Integer.MAX_VALUE; //arbitrary value
    private static double rhoThreshold = 0.2;
    private static List<String> questionBank = new ArrayList<>();
    private static List<String> answerBank = new ArrayList<>();

    public static void main(String[] args) {

    //---LOCAL OBJECTS AND FIELDS---
        List<Map<String, String>> tagsBank = new ArrayList<>();
        String question, answer;
        FreebaseDBHandler db;
        List<String> IDsList = new ArrayList<>(); //placeholder list for nameAlias2IDs method
        //uses a hash structure to ensure unique tags
        Map<String, String> tags = new HashMap<>(); 
        String spot; //stores a tag's corresponding spot when the tag get removed
        Set<String> tagIDs = new HashSet<>();
        List<NTriple> tagTriples = new ArrayList<>();
        Map<String, NTriple> mediatorTriples = new HashMap<>();
        NTriple mediatorTriple;
        List<NTriple> answerTriples = new ArrayList<>();
        Set<String> answerIDs = new HashSet<>();
        //matches are saved uniquely based on subject, predicate, mediatorPredicate, object
        Set<List<String>> matches = new HashSet<>(); 
        List<String> match = new ArrayList<>();

	//variables for console output
        boolean matched;
        int uniqueMatches = 0 , answers = 0, mediators = 0;
        long startTime = System.currentTimeMillis();
        long previousTime = System.currentTimeMillis();

        PrintWriter writer = null;
        try{
            writer = new PrintWriter("../outputs/clearedqs.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException: " + e.getMessage());
        }
        

        //---Prep FUNCTIONS---
        processArgs(args);

        if (isRetrieved) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filepath));
                String line;
                String[] lineData;
                while ((line = reader.readLine()) != null) { //reads all QA lines from text file
                    lineData = line.split(" \\| ");
                    questionBank.add(lineData[0]);
                    answerBank.add(lineData[1]);
                    if (isTagged) { // has TagMe in the name aka has it's entities tagged
                        for (int i = 1; i < lineData.length/2; i++) {
                            //temporarily uses the tags HashMap
                            tags.put(lineData[i*2], lineData[i*2+1]); 
                        }
                        tagsBank.add(new HashMap<>(tags));
                        tags.clear();
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

	// boundry check
        if (startIndex < 0) startIndex = 0; //ensures startIndex has a minimum value of 0
        //ensures endIndex cannot be less than startIndex
        if (endIndex < startIndex || endIndex > questionBank.size()) 
            endIndex = questionBank.size(); 

    // loop on questions
        for (int i = startIndex; i < endIndex; i++) {
            question = questionBank.get(i);
            answer = answerBank.get(i);
            //System.out.printf("QUESTION %d. %s (%s)\n", i+1, question, answer);
            System.out.println(i);
	    
	        matched = false;
            //skips the QA pair if Q or A is null
            if (question == null || answer == null) continue; 

            if (isTagged){
                tags.putAll(tagsBank.get(i));
                if (tagsBank.get(i).size() != 0) {
                    try{
                        writer.printf("%s | %s\n", question, answer);
                        System.out.printf("%s | %s\n", question, answer);
                    } catch (NullPointerException  e) {
                        System.err.println("NullPointerException: " + e.getMessage());
                    }
                }
            }

            if (tags.size() != 0) {
                System.out.println(tags.size());
                continue;
            }
            if (tags.size() == 0) {
                try{
                    writer.printf("%s | %s\n", question, answer);
                    System.out.printf("%s | %s\n", question, answer);
                } catch (NullPointerException  e) {
                    System.err.println("NullPointerException: " + e.getMessage());
                }
            }
            tags.clear();
        }
        //System.out.printf("PROCESSING COMPLETE\nRESULTS: %d MATCHES (%d UNIQUE MATCHES)\n", matches.size(), uniqueMatches);
        writer.close();
    }

    private static void processArgs(String[] args) {
        if (args.length == 0 || args.length > 4) {
            System.out.printf("USAGE:\tjava Main [path to Freebase config file] [path to .JSON or .TXT file]\n\t" +
                    "java Main [path to Freebase config file] [path to .JSON or .TXT file] [start index]\n\t" +
                    "java Main [path to Freebase config file] [path to .JSON or .TXT file] [start index] [end index]\n\t" +
                    "java Main [path to Freebase config file] [path to .JSON or .TXT file] [start index] [end index] [rho threshold]\n");
            System.exit(1);
        }
        if (args.length >= 3) {
            startIndex = Integer.parseInt(args[2]);
            if (args.length >= 4) {
                endIndex = Integer.parseInt(args[3]);
                if (args.length == 5)
                    rhoThreshold = Double.parseDouble(args[4]);
            }
        }
        //configpath = args[0];
        filepath = args[0];
        if (filepath.contains(".txt")) {
            isRetrieved = true;
            if (filepath.contains("TagMe")) isTagged = true;
        }
    }
}
