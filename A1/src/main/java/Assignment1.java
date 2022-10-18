import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class Assignment1 {

    //Use the elements in the Set to store the actor's name without duplication
    private static Set<String> nameSet = new HashSet<>();
    //key is actor's name,value is movie List
    private static Map<String, List> movieMap = new HashMap<>();



    public static void main(String[] args) throws IOException, CsvValidationException {
        if (null == args || args.length == 0) {
            throw new IllegalArgumentException("file path is null");
        }
        List<RowSet> records = readCsv(args[0]);
        records.forEach(t -> {
            JSONArray jsonArray = JSON.parseArray(t.cast);
            jsonArray.forEach(m -> {
                String actName = ((JSONObject) m).getString("name");  //get actor's name of current jsonObject
                nameSet.add(actName);
                List movieList = null;
                if (!movieMap.containsKey(actName)) {   //avoid null point exception
                    movieList = new ArrayList();
                } else {
                    movieList = movieMap.get(actName);
                }
                movieList.add(t.title);   //the list is all of actor's movie
                movieMap.put(actName, movieList);
            });
        });
        startMenu();
    }

    /**
     * menu ui to interact with the user
     */
    private static void startMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Movie Wall!");
        while (true) {
            System.out.print("Enter the name of an actor (or \"EXIT\" to quit): ");
            String inputName = scanner.nextLine();
            if (inputName.equals("EXIT")) {
                System.out.println("Thanks for using the Movie Wall!");
                break;
            }
            if (!isNameExists(inputName)) {
                inputName = queryMostSimilarName(inputName);
                System.out.print("No such actor. Did you mean \"" + inputName + "\" (Y/N): ");
                String command = scanner.nextLine();
                if (!"Y".equals(command.toUpperCase())) {
                    System.out.println();
                    continue;
                }
            }
            System.out.println("Actor: " + inputName);
            List movieList = movieMap.get(inputName);
            movieList.forEach(t -> {
                System.out.println("* Movie: " + t);
            });
            System.out.println();
        }


    }

    /**
     * get alternate name
     *
     * @param inputName
     * @return
     */
    private static String queryMostSimilarName(String inputName) {
        AtomicReference<String> targetName = new AtomicReference<>("");
        AtomicReference<Float> similarValue = new AtomicReference<>((float) 0);
        nameSet.forEach(t -> {
            float curSimilarValue = getSimilarityRatio(t, inputName);
            if (curSimilarValue > similarValue.get()) {
                similarValue.set(curSimilarValue);
                targetName.set(t);
            }
        });
        return targetName.get();
    }

    /**
     * if nameSet contains inputname  return true,or return false
     *
     * @param inputName
     * @return
     */
    private static boolean isNameExists(String inputName) {

        return nameSet.contains(inputName);
    }

    /**
     * read csv file and return a list
     *
     * @param path
     * @return
     */
    public static List<RowSet> readCsv(String path) throws IOException, CsvValidationException {
        CSVParser csvParser = new CSVParserBuilder().withSeparator(',').withQuoteChar('\"')
                .withEscapeChar('|')
                .withIgnoreQuotations(false)
                .build();
        List<RowSet> recors = new ArrayList<>();
        try (CSVReader readerCsv = new CSVReaderBuilder(Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)).withCSVParser(csvParser).withSkipLines(1).build()) {
            String[] lines;
            while ((lines = readerCsv.readNext()) != null) {
                recors.add(new RowSet(lines[1], lines[2]));
            }
        } catch (Exception e) {
            throw e;
        }
        return recors;
    }


    static class RowSet {
        String title;
        String cast;

        public RowSet(String title, String cast) {
            this.title = title;
            this.cast = cast;
        }

        @Override
        public String toString() {
            return "RowSet{" +
                    "title='" + title + '\'' +
                    ", cast='" + cast + '\'' +
                    '}';
        }
    }


    private static int compare(String str, String target) {


        int d[][]; // matrix
        int n = str.length();
        int m = target.length();
        int i; // traverse the cursor of str
        int j; // traverse the cursor of target
        char ch1; // char of str
        char ch2; // char of target
        int temp; //the record is the same value, the record at the matrix position, either 0 or 1
        if (n == 0) {
            return m;
        }

        if (m == 0) {
            return n;
        }

        d = new int[n + 1][m + 1];
        // init
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        // init
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) {
            // traverse str
            ch1 = str.charAt(i - 1);
            // match target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {  //32 is the position difference between upper and lower case
                    temp = 0;
                } else {
                    temp = 1;
                }
                // left +1, top +1, upper left + temp take the smallest
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }


    /**
     * get minimum value
     */
    private static int min(int one, int two, int three) {

        return (one = Math.min(one, two)) < three ? one : three;
    }


    /**
     * get the similarity between two strings,
     * and returns a number less than 1,
     * the larger the value, the higher the similarity
     */
    public static float getSimilarityRatio(String str, String target) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(target)) {
            return 0;
        }
        int max = Math.max(str.length(), target.length());
        return 1 - (float) compare(str, target) / max;


    }


}
