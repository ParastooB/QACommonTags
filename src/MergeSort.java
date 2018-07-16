import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.joestelmach.natty.*;

public class MergeSort{

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

	static String extractDate (String date){
        String parts[] = date.split("\"");
        if(parts[1].length() == 4)
            parts[1] = parts[1] + "-01-01";
        if(parts[1].length() == 7)
            parts[1] = parts[1] + "-01";
        return parts[1];
    }

    static boolean isEalier (String d1, String d2){ // return true if the first one is ealier
        try{Thread.sleep(1000);}catch(InterruptedException e){System.out.println(e);}  
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = new Date();
        Date date2 = new Date();

        try{
            date1 = format.parse(d1);
            date2 = format.parse(d2);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        if (date1.compareTo(date2) <= 0) {
            return true;
        }
        return false;

    }

    static void merge(List<NTriple> arr, int l, int m, int r){
        // Find sizes of two subarrays to be merged
        int nl = m - l + 1;
        int nr = r - m;

        NTriple temp = null;
 
        /* Create temp arrays */
        List<NTriple> left = new ArrayList<>(arr.subList(l,m+1));
        List<NTriple> right = new ArrayList<>(arr.subList(m+1,r+1));
 
        /*Copy data to temp arrays*/
        // left = arr.subList(l,m+1);
        // right = arr.subList(m+1,r+1);
        int i = 0, j = 0;
 
        // Initial index of merged subarry array
        // sort from earlier to later
        int k = l;
        while (i < nl && j < nr){
            if (isLessThan(left.get(i),right.get(j))){ //if a <= b --> false, if a > b --> true
                arr.set(k,left.get(i));
                i++;
            }else{
                arr.set(k,right.get(j));
                j++;
            }
            k++;
        }
        while (i < nl){
            arr.set(k,left.get(i));
            i++;
            k++;
        }
        while (j < nr){
            arr.set(k,right.get(j));
            j++;
            k++;
        }
    }
 
    // Main function that sorts arr[l..r] using
    // merge()
    static void sort(List<NTriple> arr, int l, int r){
        if (l < r){
            // Find the middle point
            int m = (l+r)/2;
            // Sort first and second halves
            sort(arr, l, m);
            sort(arr , m+1, r);
            // Merge the sorted halves
            merge(arr, l, m, r);
        }
    }

    static boolean isLessThan(NTriple a, NTriple b){
        // return 
        String one = extractDate(a.getObjectID());
        String two = extractDate(b.getObjectID());
        return (isEalier(one,two));
    }
}