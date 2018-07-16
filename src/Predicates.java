import java.util.*;

public class Predicates{

    private FreebaseDBHandler db;
    private Map<String,List<NTriple>> allPreds = new HashMap<>(); 
    private Map<String,List<NTriple>> stringPreds = new HashMap<>();
    private String freebaseID;
    private String input = new String();
    private Scanner console = new Scanner(System.in);
    private Map<String,Map<String,List<NTriple>>> comparablePreds = new HashMap<>();

    public Predicates(String freebaseID, FreebaseDBHandler db ){
        this.db = db;
        this.freebaseID = freebaseID;
        ID2Predicates ();
    }

    //returns all the triples of this ID based on predicate
    private void ID2Predicates () { 
        List<NTriple> tagTriples = new ArrayList<>();
        tagTriples = db.ID2Triples(this.freebaseID, tagTriples);

        List<NTriple> temp2 = new ArrayList<>();

        if (tagTriples.size() == 0)
            return;
        for (NTriple p: tagTriples){
            temp2 = this.allPreds.get(p.getPredicate());
            if (temp2 == null)
                temp2 = new ArrayList<>();
            temp2.add(p);
            this.allPreds.put(p.getPredicate(),temp2);
        }
        tagTriples.clear();
    }

    public List<NTriple> getPredObjects (String predicate) { 
        if(!this.allPreds.keySet().contains(predicate))
            return null;

        List<NTriple> results = this.allPreds.get(predicate);

        if (results.size() == 0)
            return null;

        return results;
    }

    public List<NTriple> sortPredObjects (String predicate, List<NTriple> triples) { // triples are the initial ones, e.g. all the spouses
        //triples are output of getPredObjects
        List<NTriple> results = new ArrayList<>();
        List<NTriple> predTriples = new ArrayList<>(); // the triples containing the objects and specific predicate we want to sort
        if(triples == null)
            return null;

        for(NTriple p: triples){
            predTriples = db.ID2TriplesFull(p.getObjectID(), predTriples);
            for(NTriple t: predTriples){
                if(t.getPredicate().equals(predicate)){
                    if (t.isStringDateNTriple())
                        results.add(t);
                }
            }
            predTriples.clear();
        }
        // MergeSort.sort(results, 0, results.size()-1);
        return results; //now it's sorted
    }

    public NTriple argMax (String predicate, List<NTriple> triples) { 
        List<NTriple> predTriples = sortPredObjects(predicate,triples);
        if (predTriples == null)
            return null;
        if (predTriples.size() == 0)
            return null;
        return predTriples.get(predTriples.size()-1);
    }

    public NTriple argMin (String predicate, List<NTriple> triples) { 
        List<NTriple> predTriples = sortPredObjects(predicate,triples);
        if (predTriples == null)
            return null;
        if (predTriples.size() == 0)
            return null;
        return predTriples.get(0);
    }

    public int searchForObject (String objectID, List<NTriple> sortedTriples) { // triples are the initial ones, e.g. all the spouses.
        if(sortedTriples == null)
            return -1;

        for(NTriple p: sortedTriples){
            if(p.getSubjectID().equals(objectID))
                return sortedTriples.indexOf(p);
        }
        return -1;
    }

    public int countPredicate (String predicate){
        return this.allPreds.get(predicate).size();
    }

    public void comparablePredicate(Map<String,Map<String,List<NTriple>>> result) {
        List<String> results = new ArrayList<>();
        List<NTriple> sorted = new ArrayList<>();
        result.clear();
        if (this.allPreds == null)
            return;
        if (this.allPreds.size() == 0)
            return;
        for (String entry: this.allPreds.keySet()){
            if(countPredicate(entry)> 1){
                results = PredicateComparison.arePredicatesComparable(this.allPreds.get(entry),this.db);
                if(results.size() > 0){
                    Map<String,List<NTriple>> temp = new HashMap<>();
                    result.put(entry,temp);
                    for(String s: results){
                        sorted = sortPredObjects (s, getPredObjects (entry));
                        temp.put(s,sorted);
                    }
                }
            }
        }
    }

    public void printPredicate (){
        Set<String> objectIDs = new HashSet<>();
        List<String> results = new ArrayList<>();
        List<NTriple> sorted = new ArrayList<>();
        if (this.allPreds == null)
            return;
        if (this.allPreds.size() == 0)
            return;
        for (String entry: this.allPreds.keySet()){
            // System.out.println("    --------    "+entry);
            if(countPredicate(entry)> 1){
                // System.out.println("    "+entry+"       --> " + countPredicate(entry));
                results = PredicateComparison.arePredicatesComparable(this.allPreds.get(entry),this.db);
                if(results.size() > 0){
                    System.out.println(entry);
                    for(String s: results){
                        System.out.println("    ↪" + s);
                        sorted = sortPredObjects (s, getPredObjects (entry));
                        for(NTriple n: sorted){
                            System.out.println("        "+n);
                        }
                    }
                }
            }
            // objectIDs = new HashSet<>();
            // for (NTriple t: this.allPreds.get(entry)){
            //     objectIDs.add(t.getObjectID());
            // }
        }
    }

    public void cleanUp(){
        this.allPreds.clear();
        this.comparablePreds.clear();
        this.stringPreds.clear();
    }
}