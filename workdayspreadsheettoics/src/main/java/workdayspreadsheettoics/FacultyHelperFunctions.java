package workdayspreadsheettoics;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.ArrayList; // import the ArrayList class
import java.util.HashMap;

public class FacultyHelperFunctions {
    public static String readStringFromURL(URL url) throws IOException {
        try (Scanner scanner = new Scanner(url.openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            String result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            return result;
        }
    }

    public static ArrayList<String> URLtoFacultyJSONTexts(URL url) throws IOException {
        
        ArrayList<String> facultyClumps = new ArrayList<>();
        String str = readStringFromURL(url);
        final String regex = "(?<!\\\"componentsCollection\\\":\\{\\\"items\\\":\\[)\\{\\\"__typename\\\"((.+?)((?=,\\{\\\"__typename\\\")|(?=]\\}\\}\\}]\\},\\\"menu\\\")))";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            // System.out.println("Full match: " + matcher.group(0));
            // System.out.println("");
            facultyClumps.add(matcher.group(0));
        }
        
        return facultyClumps;
    }
    
    public static HashMap<String, String> matcherDepToDep = new HashMap<String, String>() {{
        put("MechanicalEngineering", "Mechanical Engineering");
        put("ComputerScience", "Computer Science");
        put("ChemicalEngineeringAndMaterialsScience", "Chemical Engineering and Materials Science");
        put("MathematicalSciences", "Mathematical Sciences");
        put("ChemistryAndChemicalBiology", "Chemistry and Chemical Biology");
        put("ElectricalAndComputerEngineering", "Electrical and Computer Engineering");
        put("CivilEnvironmentalAndOceanEngineering", "Civil Environmental and Ocean Engineering");
        put("Physics", "Physics");
        put("BiomedicalEngineering", "Biomedical Engineering");
    }};
    public static ArrayList<Faculty> facultyJSONTextsToFaculty(ArrayList<String> facultyJSONTexts, String originDepartment) {
        ArrayList<Faculty> faculty = new ArrayList<Faculty>();
        for (String fC : facultyJSONTexts) {
            String id = getInfoFromClump("id", fC);
            String department = "";
            try {
                final String regex = "(?<=sesDepartment)(.+?)(?= )";
                final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                ArrayList<String> listOfMajorTags = new ArrayList<>(); 
                Matcher matcher = pattern.matcher(id);
                matcher.find();
                //MechanicalEngineering
                //ComputerScience
                //ChemicalEngineeringAndMaterialsScience
                //MathematicalSciences
                //ChemistryAndChemicalBiology
                //ElectricalAndComputerEngineering
                //CivilEnvironmentalAndOceanEngineering
                //Physics
                //BiomedicalEngineering
                department = originDepartment + ", " + matcherDepToDep.get(matcher.group(0));
                
            } catch(IllegalStateException e) {
                department = originDepartment;
            }
            faculty.add(new Faculty(getInfoFromClump("title", fC), 
                                    getInfoFromClump("email", fC), 
                                    getInfoFromClump("status", fC), 
                                    getInfoFromClump("facultyCategory", fC), 
                                    getInfoFromClump("phone", fC), 
                                    getInfoFromClump("office", fC), 
                                    department, 
                                    getInfoFromClump("url", fC),
                                    id));
        }
        return faculty;
    } 
    public static String getInfoFromClump(String infoType, String clump) {
        //title
        //status
        //facultyCategory
        //email
        //phone
        //office
        //id
        //url
        try {
            String regex = "(?<=\\\"" + infoType + "\\\":\\\")(.+?)(?=\\\")";
            // System.out.println(infoType);
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(clump);
            String result = "";
            if (infoType == "id") {
                while (matcher.find()) {
                    // System.out.println(matcher.group(0));
                    result += result.length() == 0 ? matcher.group(0) : " " + matcher.group(0);
                }
                // System.out.println(result);
                return result;
            } else {
                matcher.find();
                return matcher.group(0);
            }
            
        } catch(IllegalStateException e) {
            return "None";
        }
        
    }
    public static Faculty loadFacultyFromString(String facultyString) {
        String[] facultyParts = facultyString.split("\\|");
        // for (int i = 0; i < facultyParts.length; i++) {
        //     System.out.println(facultyParts[i]);
        // }
        Faculty faculty = new Faculty(facultyParts[0], facultyParts[1], facultyParts[2], facultyParts[3], facultyParts[4], facultyParts[5], facultyParts[6], facultyParts[7], facultyParts[8]);
        return faculty;
    }
    
    public static ArrayList<Faculty> loadFacultyFromFile(File file) {
        ArrayList<Faculty> faculty = new ArrayList<>();
        try {
            File myObj = file;
            Scanner myReader = new Scanner(myObj);
            
            while (myReader.hasNextLine()) {
              faculty.add(loadFacultyFromString(myReader.nextLine()));
              //System.out.println(faculty.toString());
            }
            myReader.close();
            return faculty;
        } catch (FileNotFoundException e) {
            // System.out.println("An error occurred.");
            e.printStackTrace();
            return new ArrayList<Faculty>();
        }
    }

    public static File saveFacultyListToDatabase(ArrayList<Faculty> faculty) throws IOException {
        return saveFacultyListToDatabase(new File("database.txt"), faculty);
    }
    public static File saveFacultyListToDatabase(File databaseFile, ArrayList<Faculty> faculty) throws IOException {
        File outputDatabase = databaseFile;
        PrintStream fileStream = new PrintStream(outputDatabase);

        for (Faculty f : faculty) {
            fileStream.println(f.exportStringForFile());
        }
        
        fileStream.close();

        return databaseFile;
    }

    public static HashMap<String, String> URLtoOriginDepartment = new HashMap<String, String>() {{
        put("https://www.stevens.edu/school-engineering-science/faculty", "Engineering and Science");
        put("https://www.stevens.edu/hass/hass-faculty", "Humanities, Arts and Social Sciences");
        put("https://www.stevens.edu/school-business/faculty", "Business");
    }};
    public static ArrayList<Faculty> loadFacultyFromURLs() throws IOException {
        URL[] urls = {URI.create("https://www.stevens.edu/school-engineering-science/faculty").toURL(), URI.create("https://www.stevens.edu/hass/hass-faculty").toURL(), URI.create("https://www.stevens.edu/school-business/faculty").toURL()};
        return loadFacultyFromURLs(urls);
    }
    public static ArrayList<Faculty> loadFacultyFromURLs(URL[] urls) throws IOException {
        ArrayList<Faculty> faculty = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            ArrayList<String> facultyClumps = URLtoFacultyJSONTexts(urls[i]);
            faculty.addAll(facultyJSONTextsToFaculty(facultyClumps, URLtoOriginDepartment.get(urls[i].toExternalForm())));
        }

        return faculty;
    }
    public static Faculty getFacultyFromName(ArrayList<Faculty> faculty, String name) throws IOException {
        Faculty facultyMember = new Faculty();
        for (Faculty f : faculty) {
            if (f.getName().equals(name)) {
                facultyMember = f;
                break;
            }
        }
        return facultyMember;
    }
}