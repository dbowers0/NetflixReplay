package com.example.NetflixData;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
public class NetflixService {

    public ArrayList<Object> test (MultipartFile multipart) throws FileNotFoundException, ExecutionException, InterruptedException, ParseException {
        ArrayList<Object> arrayList = new ArrayList<>();
        ArrayList<CSVData> csvData = getCSV(multipart);
        ArrayList<Object> usersDataAndRuntimeRanking = findTopShowsAndMovies(csvData);
        HashMap<String, Integer> rankingOfEpisodes = (HashMap<String, Integer>) usersDataAndRuntimeRanking.get(1);

        ArrayList<UserData> usersData = (ArrayList<UserData>) usersDataAndRuntimeRanking.get(0);
        ArrayList<Object> results = setGenreAndWatchTime(usersData);
        usersData = (ArrayList<UserData>) results.get(0);
        HashMap<String, Integer> rankingOfShowsByWatchTime = (HashMap<String, Integer>) results.get(1);
        HashMap<String, Integer> genreRanking = rankGenres(usersData);
        HashMap<String, Integer> overallStatistics = calculateStatistics(usersData, csvData);

        arrayList.add(usersData);
        arrayList.add(genreRanking);
        arrayList.add(overallStatistics);
        arrayList.add(usersDataAndRuntimeRanking);
        arrayList.add(rankingOfShowsByWatchTime);

        return arrayList;
    }

    private ArrayList<CSVData> getCSV(MultipartFile multipartFile) {
        ArrayList<CSVData> results = new ArrayList<>();
        ArrayList<CSVData> temp = new ArrayList<>();
        BufferedReader br;
        String line;
        try {
            InputStream is = multipartFile.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            int count = 0;
            //get up to 2nd semicolon
            while ((line = br.readLine()) != null) {
                CSVData csvData = new CSVData();
                //if string contains semicolon
                //split by semicolon and add to name field.
                //if 2nd split contains semicolon add first field to name field and add to full name
                String[] lines = line.split(",");
                if (lines.length > 1) {
                    csvData.setDate(lines[1]);
                }
                String movie = lines[0];
                movie = movie.replace("\"", "");
                movie = movie.trim();
                byte[] bytes = movie.getBytes(StandardCharsets.UTF_8);
                movie = new String(bytes, StandardCharsets.UTF_8);
                csvData.setTitle(movie);
                results.add(csvData);
                if(count < 30) {
                    temp.add(csvData);
                }
                count++;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        //return results;
        return temp;
    }
    private ArrayList<Object> findTopShowsAndMovies(ArrayList<CSVData> arrayList) {
        ArrayList<NetflixData> netflixDataArrayList = new ArrayList<>();
        HashMap<String, UserData> newMap = new HashMap<>();
        HashMap<String, Integer> map = new HashMap<>();
        int count = 0;

        CSVData csvData = new CSVData();
        String movie;
        for(int i = 0; i < arrayList.size(); i++) {
            csvData = arrayList.get(i);
            movie = csvData.getTitle();
            NetflixData netflixData = new NetflixData();
            netflixData.setFullname(movie);
            netflixData.setFullname(movie);
            String shows = "";
            String[] showName = null;
            //fix this
            if(movie.contains(":")) {
                showName = movie.split(":");
                if(showName[1].contains(":")) {
                    String shows1[] = showName[1].split(":");
                    shows = showName[0] + ": " + shows1[1];
                    shows = shows.replace("\"", "");
                    shows = shows.trim();
                    byte[] bytes1 = shows.getBytes(StandardCharsets.UTF_8);
                    shows = new String(bytes1, StandardCharsets.UTF_8);
                } else {
                    shows = showName[0];
                    shows = shows.replace("\"", "");
                    shows = shows.trim();
                    byte[] bytes1 = shows.getBytes(StandardCharsets.UTF_8);
                    shows = new String(bytes1, StandardCharsets.UTF_8);
                }
            } else {
                shows = movie;
                shows = shows.trim();
                shows = shows.replace("\"","");
                byte[] bytes1 = shows.getBytes(StandardCharsets.UTF_8);

                shows = new String(bytes1, StandardCharsets.UTF_8);
            }
            if(newMap.containsKey(shows)) {
                UserData userData = newMap.get(shows);
                int count1 = Integer.parseInt(userData.getNumberEpisodes());
                count1++;
                userData.setNumberEpisodes(String.valueOf(count1));
                newMap.put(shows, userData);
                map.put(shows, count1);
            } else {
                UserData userData = new UserData();
                userData.setFullname(movie);
                userData.setName(shows);
                userData.setNumberEpisodes(String.valueOf(1));
                newMap.put(shows, userData);
                map.put(shows, 1);
                //map.put(shows, u)
            }
        }
        map = sortByValue(map);
        ArrayList<String> alKeys = new ArrayList<String>(map.keySet());
        Collections.reverse(alKeys);

        Collection<UserData> values = newMap.values();

//Creating an ArrayList of values

        ArrayList<UserData> arrayList1 = new ArrayList<UserData>(values);

        UserData userData1 = new UserData();
        for(int i = 0; i < arrayList1.size(); i++) {
            userData1 = arrayList1.get(i);
            // System.out.println(userData1.getName());
        }
        //Collections.sort(arrayList1, Comparator.comparing(UserData::getTotalMinutes));
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(arrayList1);
        objects.add(map);
        return objects;
    }
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());
            // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

            // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }


        return temp;
    }

    private ArrayList<Object> setGenreAndWatchTime(ArrayList<UserData> arrayList) throws ExecutionException, InterruptedException {
        ArrayList<Object> fullResults = new ArrayList<>();
        String prefix = "";
        int count = 0;
        HashMap<String, Integer> rankingOfMoviesByTotalWatchTime = new HashMap<>();
        ArrayList<UserData> results = new ArrayList<>();
        UserData userData = new UserData();
        for(int i = 0; i < arrayList.size(); i++) {
            userData = arrayList.get(i);
            //System.out.println("FullName: " + userData.getFullname());
            prefix = userData.getFullname();
            NetflixDatabase netflixDatabase = query(prefix);
            if(netflixDatabase == null) {
                //System.out.println("Name: " + userData.getName());
                prefix = userData.getName();
                netflixDatabase = query(prefix);
                if(netflixDatabase == null) {
                    System.out.println(prefix); //we want this one
                    count++;
                }
            }

            if(!(netflixDatabase == null)) {
                userData.setRuntime(netflixDatabase.getRuntime());
                int sum = Integer.parseInt(userData.getNumberEpisodes());
                sum = sum * Integer.parseInt(netflixDatabase.getRuntime());
                userData.setTotalMinutes(String.valueOf(sum));
                rankingOfMoviesByTotalWatchTime.put(userData.getName(), sum);
                String genres = netflixDatabase.getGenres();

                ArrayList<String> genreList = new ArrayList<>();

                String split[] = genres.split(",");
                String genre = split[0];
                genre = genreSplit(genre);
                genreList.add(genre);
                while (split.length > 1 && split[1].contains(",")) {
                    split = split[1].split(",");
                    genre = split[0];
                    genre = genreSplit(genre);
                    genreList.add(genre);
                }

                if(split.length > 1) {
                    genre = genreSplit(split[1]);
                    genreList.add(genre);
                }

                userData.setGenres(genreList);
                userData.setType(netflixDatabase.getType());
                results.add(userData);
            }
        }
        rankingOfMoviesByTotalWatchTime = sortByValue(rankingOfMoviesByTotalWatchTime);
        System.out.println(count);
        fullResults.add(results);
        fullResults.add(rankingOfMoviesByTotalWatchTime);
        return fullResults;
    }

    private String genreSplit(String genre) {
        genre = genre.replace("[","");
        genre = genre.replace("]","");
        genre = genre.replace("\'","");
        genre = genre.trim();
        return genre;
    }
    private NetflixDatabase query(String prefix) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("NetflixDataProject")
                .whereEqualTo("title", prefix)
                .limit(1)
                .get();

        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            NetflixDatabase netflixDatabase = snapshot.getDocuments().get(0).toObject(NetflixDatabase.class);
            return netflixDatabase;
        } else {
            return null;
        }
    }

    public NetflixDatabase netflix() throws ExecutionException, InterruptedException {
        String prefix = "Naruto";
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("NetflixDataProject")
                .whereEqualTo("title", prefix)
                .limit(1)
                .get();

        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            NetflixDatabase netflixDatabase = snapshot.getDocuments().get(0).toObject(NetflixDatabase.class);
            return netflixDatabase;
        } else {
            return null;
        }
    }

    private HashMap<String, Integer> rankGenres(ArrayList<UserData> arrayList) {
        HashMap<String, Integer> map = new HashMap<>();
        ArrayList<String> genres = new ArrayList<>();
        UserData userData = new UserData();
        for(int i = 0; i < arrayList.size(); i++) {
           userData = arrayList.get(i);
           genres = userData.getGenres();
           for(int j = 0; j < genres.size(); j++) {
               if(map.containsKey(genres.get(j))) {
                   int count = map.get(genres.get(j)) + 1;
                   map.put(genres.get(j), count);
               } else {
                   map.put(genres.get(j), 1);
               }

           }
        }
        map = sortByValue(map);
        return map;
    }
    private HashMap<String, Integer> calculateStatistics(ArrayList<UserData> arrayList, ArrayList<CSVData> csvResults) throws ParseException {
        HashMap<String, Integer> statistics = new HashMap<>();
        ArrayList<Integer> results = new ArrayList<>();
        UserData userData = new UserData();
        int overallNumberofShows = 0;
        int overallMinutesWatched = 0;
        for(int i = 0; i < arrayList.size(); i++) {
            userData = arrayList.get(i);
            overallMinutesWatched += Integer.parseInt(userData.getTotalMinutes());
            overallNumberofShows += Integer.parseInt(userData.getNumberEpisodes());
        }
        statistics.put("OverallNumberOfShows", overallNumberofShows);
        statistics.put("OverallMinutesWatched", overallMinutesWatched);
        results.add(overallNumberofShows);
        results.add(overallMinutesWatched);

        int timePeriod = 0;
        CSVData csvData = new CSVData();
        CSVData csvData1 = new CSVData();

        csvData = csvResults.get(1);
        csvData1 = csvResults.get(csvResults.size()-1);

        String date1 = csvData.getDate();
        String date2 = csvData1.getDate();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        //System.out.println(date1);
        //System.out.println(date2);
        Date firstDate = sdf.parse(date1);
        Date secondDate = sdf.parse(date2);

        long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        statistics.put("OverallTimePeriodInDays", (int)diff);
        results.add((int)diff);
        return statistics;
    }

}

//Check up to second colon first
//Check regular
//Name of movie/show
//How many times watched
//Watch time of each

//Stats
//Most watched genre
