import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import com.joestelmach.natty.*;

public class PredicateComparison{

    // a triple is comparable if there is more than one triple with the same predicate. 
    // AND (objects connected to that triple are either mediators which then have to have predicates which are numbers/dates
    // OR objects are normal triples and have a predicate that is number/date.
    public static List<String> arePredicatesComparable(List<NTriple> predicates, FreebaseDBHandler db ){
        List<NTriple> subjTriples = predicates;
        List<NTriple> objTriples = new ArrayList<>();
        List<NTriple> objStringTriples = new ArrayList<>();
        List<NTriple> objobjTriples = new ArrayList<>();
        List<String> results = new ArrayList<>();


        // we only need to check for one of them to know if it's possible to sort
        objTriples = db.ID2Triples(subjTriples.get(0).getObjectID(), objTriples);
        objStringTriples = db.ID2TriplesFull(subjTriples.get(0).getObjectID(), objStringTriples);

        for (NTriple c: objStringTriples){
            if(c.getObjectID().matches("\".*\\d+.*\""+"\\^"+".*")){
                if(c.getObjectID().charAt(0) > 47 || c.getObjectID().charAt(0) < 58)
                    results.add(c.getPredicate());
            }
        }
        // we are going 2nd layer deep
        // This will answer a question like how was the oldest person Tom Cruise was married to
        /*for (NTriple c: objTriples){
            objobjTriples = db.ID2TriplesFull(c.getObjectID(),objobjTriples);
        	if(c.getObjectID().equals(subjTriples.get(0).getSubjectID()))
        		continue;
            for (NTriple k: objobjTriples){
	        	if(k.getSubjectID().equals(subjTriples.get(0).getSubjectID()))
        			continue;
                // if(k.getObjectID().matches("\".*\\d+.*\"*")){ //has digits in quotation marks 
                if(k.getObjectID().matches("\".*\\d+.*\""+"\\^"+".*")){ //has digits in quotation marks and ^ character
                    // System.out.println("Object: " + k.getObjectID() + " Predicate: " +k.getPredicate());
                    // System.out.println("Do you see any dates? (y/n)");
                    // this.input = console.nextLine();
                    // if (this.input.toLowerCase().equals("y")){
                    // this.input = new String();
	                System.out.println(subjTriples.get(0).getPredicate());
	                System.out.println("	↪" + c.getPredicate());
	                System.out.println("		↪" + k.getPredicate());
                    // String []parts =  k.getObjectID().split("\"");
                    // // System.out.println(parts[1]);
                    // boolean dateRes = false;
                    // if(parts[1].length() == 4) // This 4 digits in the year (or irrelevent?) but for sure never time.
                    //     dateRes = isDate(parts[1]+"-04-01");
                    // else
                    //     dateRes =isDate(parts[1]);
                    // if(dateRes){
                    //     System.out.println("Object: " + k.getObjectID() + " Predicate: " +k.getPredicate());
                    // }
                }
            }
        }*/
        return results;
    }

    private static boolean isDate(String data){
        if(! data.matches(".*\\d+.*")){
            System.out.println("There is no date in " + data);
            return false;
        }
        else{
            if(new Parser().parse(data).size() > 0){
                List<Date> dates =new Parser().parse(data).get(0).getDates();
                // System.out.println("        " + dates.get(0));
                return true;
            }
            else 
                return false;
        }
    }
}