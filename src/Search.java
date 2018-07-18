import java.io.*;
import java.util.*;

public class Search {
    //---STATIC VARIABLES---
    // This is for every single question. 
    private String answer;
    private FreebaseDBHandler db;
    private Commons cm;
    private boolean exists, matched, check, foundText = false, contained = false;
    private int sizeLimit = 100000 , sizeLimit2 = 50000;

    private List<String> IDsList = new ArrayList<>(); //placeholder list for nameAlias2IDs method

    // Store the names in a seperate table, just so we don't have to search again for repeated ID's
    private Map<String, String> objectIDnames = new HashMap<>();
    private Map<String, String> tags = new HashMap<>(); 
    private Set<String> tagIDs = new HashSet<>();
    private List<NTriple> tagTriples = new ArrayList<>();
    // The other tag triples which are connected to strings not objects
    private List<NTriple> otherTagTriples = new ArrayList<>();

    private NTriple mediatorTriple;
    //matches are saved uniquely based on subject, predicate, mediatorPredicate, object
    private Set<List<String>> matches = new HashSet<>();
    private Set<NTriple> secondMatches = new HashSet<>();  
    private List<String> match = new ArrayList<>();

    // private Set<String> objectTypes = new HashSet<>();

    private Scanner console = new Scanner(System.in);
    private String input = new String();

    private Set<String> sortablePredicates = new HashSet<>();

    // private Map<String, List<NTriple>> secondTagNames = new HashMap<>(); // Store the triples that 
    // private Map<String, List<NTriple>> goodSecondTriples = new HashMap<>(); // ObjectName and Triple
    private Map<String, List<NTriple>> commonPredicates = new HashMap<>(); // Predicate --> Triples

    public Search(String answer, FreebaseDBHandler db, Map<String, String> tags) {
        this.answer = answer;
        this.db = db;
        this.matched = false;
        this.tags = tags;
        this.exists = false;
        this.check = false;
    }

// Top Down Search
    public void topDown(){
        Set<String> tagIDValues = new HashSet<>(); //not only keys
        cm = new Commons(db, tags);
        cm.CommonMap();
        for (String tag : this.tags.keySet()) {
            this.tagIDs = this.db.nameAlias2IDs(tag, this.IDsList, this.tagIDs);
            // why only the key set???!
            /*this.IDsList.clear();
            tagIDValues = this.db.nameAlias2IDs(this.tags.get(tag), this.IDsList, tagIDValues);
            this.tagIDs.addAll(tagIDValues);
            tagIDValues.clear();*/

            // System.out.println("This many tag ID "+tagIDs.size());
            for (String tagID : this.tagIDs) {
                this.tagTriples = this.db.ID2Triples(tagID, this.tagTriples);
                if (this.tagTriples == null) // can this happen? an ID has no triple associated with it!
                    continue;

                for (NTriple tagTriple : this.tagTriples) {
                    // if(this.commonPredicates.containsKey(tagTriple.getPredicate())){

                    // }
                }
                this.tagTriples.clear();
            }
            this.tagIDs.clear();
        }
        this.commonPredicates.clear();
    }

// Other methods

    private void storeGoodTriples (Map<String, List<NTriple>> triples,String name, NTriple triple){
            if(triples.get(name) == null){
            List<NTriple> temp = new ArrayList<>();
            temp.add(triple);
            triples.put(name,temp);
            }else{
                triples.get(name).add(triple);
            }
    }

    private void printGoodTriples (Map<String, List<NTriple>> triples){
        if (triples == null)
            return;
        if (triples.size() == 0)
            return;
        for (String entry: triples.keySet()){
            System.out.println("    "+entry);
            for (NTriple t: triples.get(entry)){
                System.out.println("        "+t.getPredicate());
            }
        }
    }

    private static Map<String, NTriple> deepCopyMap(Map<String, NTriple> original){
        Map<String, NTriple> copy = new HashMap<String, NTriple>();
        for (Map.Entry<String, NTriple> entry : original.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    private static Set<String> deepCopySet(Set<String> original){
        Set<String> copy = new HashSet<String>();
        for (String entry : original){
            copy.add(entry);
        }
        return copy;
    }

    private static Set<List<String>> deepCopyList(Set<List<String>> original){
        List<String> copy = new ArrayList<String>();
        Set<List<String>> copy2 = new HashSet<List<String>>();

        for (List<String> entry2 : original){
            for (String entry : entry2){
                copy.add(entry);
            }
            copy2.add(copy);
        }
        return copy2;
    }

    public void cleanUp(){
        this.IDsList.clear();
        this.tags.clear();
        this.objectIDnames.clear();
        System.gc(); //prompts Java's garbage collector to clean up data structures
    }

    public String getQuestionPackage(String question){
        String result = new String(question + " | " + this.answer);
        for (String tag : tags.keySet()) {
            result = result + (" | " + tag);
            result = result + (" | " + tags.get(tag));
        }
        return result;
    }
}