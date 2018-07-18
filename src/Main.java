//put -verbose:gc in VM options in Configurations to print GC data
import java.io.*;
import java.util.*;

public class Main {
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
        Search search;
        //uses a hash structure to ensure unique tags
        Map<String, String> tags = new HashMap<>(); 
        String spot; //stores a tag's corresponding spot when the tag get removed

	//variables for console output
        // unique matches are all the qs that had an answer
        int uniqueMatches = 0 , answers = 0, mediators = 0 , count = 0, matches = 0;
        long startTime = System.currentTimeMillis();
        long previousTime = System.currentTimeMillis();

        PrintWriter writer = null;
        try{
            writer = new PrintWriter("../outputs/output.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException: " + e.getMessage());
        }

        PrintWriter writer2 = null;
        try{
            writer2 = new PrintWriter("../outputs/foundputs.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException: " + e.getMessage());
        }

    //---Prep FUNCTIONS---
        processArgs(args);
        readConfigFile();

	// database
        db = new FreebaseDBHandler(dbURL, dbUser, dbPass);

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
            // System.out.printf("QUESTION %d. %s (%s)\n", i+1, question, answer);
            System.out.printf("------------------------------QUESTION %d -----------------------------\n", i+1);
            System.out.println(question + "\n The answer is: " + answer);
	    
            //skips the QA pair if Q or A is null
            if (question == null || answer == null) continue; 

            if (isTagged){
                tags.putAll(tagsBank.get(i));
            }
            else { // the file is not tagged find the tagges for each question 1 by 1
                System.out.println("QUERYING TagMe...");
                TagMe.tag(question);
                tags.putAll(TagMe.getTags());
            }

            if (tags.size() != 0) {
                //removes tags that are equivalent to the answer
                spot = tags.remove(answer.toLowerCase().trim()); 
                if (spot != null) { //if a tag was removed, the collected spot is used as the tag
                    tags.put(spot.toLowerCase().trim(), spot.toLowerCase().trim()); //in case the spot is also equivalent to the answer
                    tags.remove(answer.toLowerCase().trim()); 
                }
            }
            if (tags.size() == 0) {
                System.out.println("Skipping because no entities"); //prints an empty line for spacing
                System.out.println();
                continue; //skips the QA pair if there are no tags to use
            }
            System.out.println("TAGS: " + tags);

        //bottom-up
            search = new Search(answer,db,tags);

        //top-down
            search.topDown();
            search.cleanUp();
        }
        writer.close();
        writer2.close();
        tagsBank.clear();
    }

    private static void processArgs(String[] args) {
        if (args.length == 0 || args.length > 5) {
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
        configpath = args[0];
        filepath = args[1];
        if (filepath.contains(".txt")) {
            isRetrieved = true;
            if (filepath.contains("TagMe")) isTagged = true;
        }
    }

    private static void readConfigFile() {
        try {
            Properties prop = new Properties();
            InputStream input = new FileInputStream(configpath);
            prop.load(input);
            dbURL = prop.getProperty("dbURL");
            dbUser = prop.getProperty("dbUser");
            dbPass = prop.getProperty("dbPass");
            input.close();
            prop.clear();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
