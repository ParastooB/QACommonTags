import java.util.*;

public class FreebaseDBHandler extends MySQLHandler {
    //---DATABASE DETAILS---
    private static final String DATABASE_NAME = "freebase_mysql_db";
    private static final String DATADUMP_TABLE_NAME = "`freebase-onlymid_-_datadump`";
    private static final String ROWIDS_TABLE_NAME = "`freebase-onlymid_-_fb-id2row-id`";
    private static final String NAME2ID_TABLE_NAME = "`freebase-onlymid_-_en_name2id`";
    private static final String ALIAS2ID_TABLE_NAME = "`freebase-onlymid_-_en_alias2id`";

    //---(REUSED) FIELDS---
    private final long FACTS_THRESHOLD = 20;

    private List<String> queryResults = new ArrayList<>();
    private List<List<String>> query2DResults = new ArrayList<>();
    private List<Long> rowIDs = new ArrayList<>(); //used in ID2RowIDs

    public FreebaseDBHandler(String dbURL, String dbUser, String dbPass) {
        super(dbURL, dbUser, dbPass);
    }

    //---REUSED METHODS---
    //freebase ID -> freebase row IDs
    private void ID2RowIDs(String freebaseID) {
        rowIDs.clear(); //clears list since this method and list is reused

        //searches in the form of <http://rdf.freebase.com/ns/ID>
        String query = String.format("SELECT * FROM %s.%s WHERE `freebase_id` = '<http://rdf.freebase.com/ns/%s>'",
                DATABASE_NAME, ROWIDS_TABLE_NAME, freebaseID);
        queryTable(query);
        // System.out.println(query);
        query2DResults = doubleQueryResult(2, 3); //index 0 stores the min row ID and index 1 stores the max row ID

        if (query2DResults.get(0).size() != 0) {
            rowIDs.add(Long.parseLong(query2DResults.get(0).get(0)));
            rowIDs.add(Long.parseLong(query2DResults.get(1).get(0)));
        }
    }

    //name/alias -> freebase IDs
    public Set<String> nameAlias2IDs(String nameAlias, List<String> IDs, Set<String> freebaseIDs) {
        //searches in the form of "name"@en
        String query = String.format("SELECT * FROM %s.%s WHERE `en_name` = '\"%s\"@en'",
                DATABASE_NAME, NAME2ID_TABLE_NAME, escapeMetaCharacters(nameAlias));
        queryTable(query);
        freebaseIDs = collectIDs(IDs, freebaseIDs);

        //searches in the form of "alias"@en
        query = String.format("SELECT * FROM %s.%s WHERE `en_alias` = '\"%s\"@en'",
                DATABASE_NAME, ALIAS2ID_TABLE_NAME, escapeMetaCharacters(nameAlias));
        queryTable(query);
        freebaseIDs = collectIDs(IDs, freebaseIDs);

        return freebaseIDs;
    }
    private Set<String> collectIDs(List<String> IDs, Set<String> freebaseIDs) {
        queryResults = singleQueryResult(2);
        if (queryResults.size() != 0) { //ensures that queryResults is not empty
            for (String queryResult : queryResults) {
                IDs.addAll(Arrays.asList(queryResult.split(",")));
                for (String ID : IDs) { //splits the queryResult into individual IDs in a list
                    ID = ID.substring(0, ID.indexOf("(")); //chops off number of facts in brackets at the end of the ID
                    freebaseIDs.add(ID);
                }
                IDs.clear();
            }
        }
        return freebaseIDs;
    }

//---OTHER METHODS---
    //freebase ID -> (row IDs) -> (object freebase IDs) -> NTriples
    public List<NTriple> ID2Triples(String freebaseID, List<NTriple> triples) {
        ID2RowIDs(freebaseID);
        if (rowIDs.size() == 0)
            return null;

        //searches for all objectIDs from range of rows to get objectIDs that are freebase IDs
        String query = String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                DATABASE_NAME, DATADUMP_TABLE_NAME, rowIDs.get(0), rowIDs.get(1));

        // System.out.println(query);
        queryTable(query);
        query2DResults = doubleQueryResult(2, 3); //index 0 stores predicates and index 1 stores objects
        for (int i = 0; i < query2DResults.get(1).size(); i++) {
            if (query2DResults.get(1).get(i).contains("<http://rdf.freebase.com/ns/m.")){ //only adds objectIDs that are freebase IDs
                if(query2DResults.get(0).get(i).contains("<http://rdf.freebase.com/ns/freebase.")){
                    // System.out.println(query2DResults.get(0).get(i));
                    continue;
                }
                triples.add(new NTriple(null, freebaseID, query2DResults.get(0).get(i), query2DResults.get(1).get(i), null));
                //null values to be filled out in the main method only when it's a match
            }
        }
        return triples;
    }

    // returns more node tan ID2Triples because it also includes some nodes that connect to string rather than freebase obejct
    public List<NTriple> ID2TriplesFull(String freebaseID, List<NTriple> triples) {
        ID2RowIDs(freebaseID);
        if (rowIDs.size() == 0)
            return null;

        String query = String.format (" SELECT * FROM %s.%s WHERE (`row_id` BETWEEN %d AND %d) AND `<predicate>` LIKE \'%s%%\'" + 
            " AND `<predicate>` != '%s' AND `<predicate>` != '%s' AND `<predicate>` != '%s' AND `<predicate>` != '%s'" +
            " AND (`<object>` LIKE '%%@en%%' OR `<object>` NOT LIKE '%%@%%')"+
            " AND `<object>` NOT LIKE '%s%%'; " 
            , DATABASE_NAME, DATADUMP_TABLE_NAME, rowIDs.get(0), rowIDs.get(1), "<http://rdf.freebase.com/ns/" , 
            "<http://rdf.freebase.com/ns/common.topic.topic_equivalent_webpage>" , "<http://rdf.freebase.com/ns/common.topic.description>", 
            "<http://rdf.freebase.com/ns/common.topic.webpage>" , 
            "<http://rdf.freebase.com/ns/type.object.key>", "<http://rdf.freebase.com/ns/m.");
        queryTable(query);
        query2DResults = doubleQueryResult(2, 3); //index 0 stores predicates and index 1 stores objects
        for (int i = 0; i < query2DResults.get(1).size(); i++) {
            String padpred = new String("<http://rdf.freebase.com/ns/"+query2DResults.get(1).get(i)+ ">");
            triples.add(new NTriple(null, freebaseID, query2DResults.get(0).get(i), padpred, null));
        }
        return triples;
    }

    public Set<String> ID2Type(String freebaseID, Set<String> list) {
        ID2RowIDs(freebaseID);
        if (rowIDs.size() == 0)
            return null;

        String query = String.format (" SELECT * FROM %s.%s WHERE (`row_id` BETWEEN %d AND %d) AND `<predicate>` LIKE \'%s\';" 
            , DATABASE_NAME, DATADUMP_TABLE_NAME, rowIDs.get(0), rowIDs.get(1), "<http://rdf.freebase.com/ns/type.object.type>");
        queryTable(query);
        queryResults = singleQueryResult(3); // index 3 stores objects
        for (int i = 0; i < queryResults.size(); i++) {
            list.add(queryResults.get(i));
        }
        return list;
    }

    //returns all the triples containing this predicate as connection.
    public List<NTriple> Predicate2Triples(String predicate, List<NTriple> triples) {

        String query = String.format("SELECT * from %s.%s WHERE `<predicate>` LIKE \'%%%s>\' limit 1;",
                DATABASE_NAME, DATADUMP_TABLE_NAME, predicate);
        // System.out.println(query);
        queryTable(query);
        query2DResults = doubleQueryResult(1, 3); //index 0 stores predicates and index 1 stores objects
        for (int i = 0; i < query2DResults.get(1).size(); i++) {
            if (query2DResults.get(1).get(i).contains("<http://rdf.freebase.com/ns/m.")){ //only adds objectIDs that are freebase IDs
                // System.out.println(triples);
                // System.out.println(freebaseID);
                // if (!freebaseID.equals("m.0qjf6n"))
                String padpred = new String("<http://rdf.freebase.com/ns/"+predicate + ">");
                String temp = query2DResults.get(0).get(i).toString();
                String ID = temp.substring(28, temp.length() - 1);
                triples.add(new NTriple(null, ID, padpred, query2DResults.get(1).get(i), null));
                //null values to be filled out in the main method only when it's a match
            } 
        }
        return triples;
    }  

    public String ID2name(String freebaseID){
        long startTime = System.currentTimeMillis();
        ID2RowIDs(freebaseID);
        if (rowIDs.size() == 0)
            return "unknown";

        freebaseID = freebaseID.replace("_", "\\_"); // "_" is a special character in SQL (one character filling)
        // SELECT * from freebase_mysql_db.`freebase-onlymid_-_datadump` WHERE (`row_id` BETWEEN 6936435 AND 6936534) 
        // AND `<predicate>` = '<http://rdf.freebase.com/ns/type.object.name>' AND `<object>` LIKE '%@en';
        String query = String.format("SELECT * FROM %s.%s WHERE (`row_id` BETWEEN %d AND %d) AND `<predicate>` = \'%s\' AND `<object>` LIKE \'%%@en\';",
                DATABASE_NAME, DATADUMP_TABLE_NAME, rowIDs.get(0), rowIDs.get(1),"<http://rdf.freebase.com/ns/type.object.name>");
        // String query = String.format("SELECT * FROM %s.%s WHERE `freebase_mids_n_ranks` LIKE \'%s%%\' limit 1", DATABASE_NAME, NAME2ID_TABLE_NAME, freebaseID);
        queryTable(query);
        queryResults = singleQueryResult(3); // only returning objects colomn
        if (queryResults.size() == 0) { //catch mediator
            // query = String.format("SELECT * FROM %s.%s WHERE `freebase_mids_n_ranks` LIKE \'%%,%s%%\' limit 1", DATABASE_NAME, NAME2ID_TABLE_NAME, freebaseID);
            // queryTable(query);
            // queryResults = singleQueryResult(1); // only returning names colomn
            // if (queryResults.size() == 0)
            return "null"; //if there is no name or alias, the triple is a mediator and the method returns null
        }
        String sv = queryResults.toString();
        String [] parts2 = sv.split("\"");
        // System.out.printf("TIME: %.3fs SINCE START\n\n", (System.currentTimeMillis() - startTime)/1000.0);
        return parts2[1].trim(); //if there are too many facts, the triple is assumed to not be a mediator
    }

    //checks if a triple has a mediator for its object
    public boolean isIDMediator(String freebaseID) {
        ID2RowIDs(freebaseID);
        if (rowIDs.size() == 0)
            return false;
	
        if (rowIDs.get(1) - rowIDs.get(0) + 1 < FACTS_THRESHOLD) {
            //searches for all predicates and corresponding row IDs from range of rows to get name/alias predicates
            String query = String.format("SELECT * FROM %s.%s WHERE `row_id` BETWEEN %d AND %d",
                    DATABASE_NAME, DATADUMP_TABLE_NAME, rowIDs.get(0), rowIDs.get(1));
            queryTable(query);
            queryResults = singleQueryResult(2);
            if (queryResults.size() != 0) { //ensures that predicate is not empty
                for (String queryResult : queryResults) {
                    if (queryResult.equals("<http://rdf.freebase.com/ns/type.object.name>") || queryResult.equals("<http://rdf.freebase.com/ns/common.topic.alias>"))
                        return false; //if there is a name or alias, the triple is not a mediator and the method returns false and exits
                }
            }
            return true; //if the loop finishes completion, there are no names/alias and the triple can be assumed to be a mediator
        }
        return false; //if there are too many facts, the triple is assumed to not be a mediator
    }
}
