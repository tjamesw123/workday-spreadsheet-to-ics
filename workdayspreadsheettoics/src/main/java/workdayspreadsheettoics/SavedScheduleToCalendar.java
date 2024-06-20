package workdayspreadsheettoics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;




public class SavedScheduleToCalendar {
  private static ArrayList<Faculty> facultyList;

  public static void SavedScheduleToCalendar(ArrayList<Faculty> facultyList1, Sheet sheet) throws IOException, GeneralSecurityException, ParseException {
    // Load faculty list
    facultyList = facultyList1;

    // Create Data
    Map<Integer, List<String>> data = new HashMap<>();
    int i = 0;
    for (Row row : sheet) {
        data.put(i, new ArrayList<String>());
        for (Cell cell : row) {
            switch (cell.getCellType()) {
                case STRING: data.get(Integer.valueOf(i)).add(cell.getRichStringCellValue().getString()); break;
                case NUMERIC: 
                if (DateUtil.isCellDateFormatted(cell)) {
                    data.get(i).add(cell.getDateCellValue() + "");
                } else {
                    data.get(i).add(cell.getNumericCellValue() + "");
                } 
                break;
                case BOOLEAN: data.get(i).add(cell.getBooleanCellValue() + ""); break;
                case FORMULA: data.get(i).add(cell.getCellFormula() + ""); break;
                default: data.get(i).add(" ");
            }
        }
        i++;
    }

    Calendar calendar = new Calendar();
    int endRow = 0;
    int startingRow = 0;
    for (int r = 0; data.get(r).size() > 1 ? !data.get(r).get(0).equals("Course") : true; r++) {
      startingRow = r+2;
    }
    System.out.println("Starting row: " + startingRow);
    for (int g = startingRow; !(data.get(g) == null) && (!data.get(g).equals("Text Return Attribute")); g++) {
        System.out.println("First cell in current row " + data.get(g).get(0));
        System.out.println("Breaking: " + (data.get(g+1) == null || data.get(g+1).get(0).equals("Text Return Attribute")));
        if (data.get(g).get(0).equals("Text Return Attribute")) {
          continue;
        }
        List<String> row = data.get(g);
        System.out.println(row.get(7));
        for (int c = 0; c < row.size(); c++) {
          System.out.println((char)(65+c) + " " + c + ": " + "(" + row.get(c) + ")");
        }

        String[] temp = row.get(9).split("\n");
        if (temp.length > 1) {//Combination Classes Handler
            //System.out.println("Combination Classes");
            int sI = 0;
            for (String s : temp) {
              System.out.println(s);
              System.out.println(s.length());
              if (s.length() != 0) {
                sI++;
              }
            }
            String[] eventStrings = new String[sI];
            sI = 0;
            for (String s : temp) {
              if (s.length() != 0) {
                System.out.println(s);
                eventStrings[sI] = s;
                sI++;
              }
            }
            for (String s : eventStrings) {
              VEvent event1;
              String eventDeliveryMode = "";
              String eventDetails = "";
              try {
                event1 = setupAndMakeEvent(s, row.get(3), row.get(6), row.get(5), eventDeliveryMode, row.get(7), row.get(8), eventDetails);
              } catch (Exception e) {
                //This is to fix a bug
                //if the teacher is not present the program should just blank out the rows that do not exist
                event1 = setupAndMakeEvent(s, row.get(3), " ", row.get(5), eventDeliveryMode, row.get(7), row.get(8), eventDetails);
              }
    
              
              
              if (event1 != null) {
                calendar.add(event1);
              }
            }
            System.out.println("END");
          } else {//Every other class type
            //row.get(7) = Event Cell
            //row.get(4) = Event Name
            //row.get(9) = Instructor Cell
            //row.get(5) = Instrucational Format
            //row.get(6) = Delivery Mode Cell
            //row.get(10) = Start Cell
            //row.get(11) = End Cell
            //row.get(0) = Details

            // new cell class types
            //row.get(9) = Meeting Patterns
            //row.get(3) = Section Name
            //row.get(6) = Instructor Cell
            //row.get(5) = Instructional Format
            //row.get(7) = Start Cell
            //row.get(8) = End Cell
    
            String eventDeliveryMode = "";
            String eventDetails = "";

            VEvent event;
            try {
              event = setupAndMakeEvent(row.get(9), row.get(3), row.get(6), row.get(5), eventDeliveryMode, row.get(7), row.get(8), eventDetails);
            } catch (Exception e) {
              //This is to fix a bug
              event = setupAndMakeEvent(row.get(9), row.get(3), " ", row.get(5), eventDeliveryMode, row.get(7), row.get(8), eventDetails);
            }
            
            if (event != null) {
              calendar.add(event);
            }
          }
          endRow = g+1;
    }
    //Finding if there is a semester name and if so naming it so
    System.out.println(data.get(4));
    String semesterName = "";
    if (!(data.get(4).get(1) == null) && data.get(4).get(0).equals("Saved Schedule")) {
      try {
        semesterName = data.get(4).get(1);
        System.out.println("Found Semester Name: " + semesterName);
      } catch (Exception e) {
        System.out.println("No semester name");
        semesterName = semesterType;
      }
    }
    System.out.println("Getting semester name from classes");

    FileOutputStream fout;
    if (!semesterName.equals("")) {
      fout = new FileOutputStream(data.get(1).get(1).split(" - ")[0] + " " + semesterName + ".ics");
      System.out.println("Semster Name: " + "(" + semesterName + ")");
    } else {
      fout = new FileOutputStream(data.get(1).get(1).split(" - ")[0] + " " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + ".ics");
      System.out.println("Year");
    }

    CalendarOutputter outputter = new CalendarOutputter();
    outputter.output(calendar, fout);
     
}

private static String semesterType = ""; 
private static VEvent setupAndMakeEvent(String eventInput, String eventName, String instructorCell, String instructionalFormat, String deliveryModeCell, String startCell, String endCell, String details) throws IOException {
    //row.get(7) = Event Cell
    //row.get(4) = Event Name
    //row.get(9) = Instructor Cell
    //row.get(5) = Instrucational Format
    //row.get(6) = Delivery Mode Cell
    //row.get(10) = Start Cell
    //row.get(11) = End Cell
    //row.get(0) = Details

    // new cell class types
    //row.get(9) = Meeting Patterns
    //row.get(3) = Section Name
    //row.get(6) = Instructor Cell
    //row.get(5) = Instructional Format
    //row.get(7) = Start Cell
    //row.get(8) = End Cell
    
    //                                                                  012345
    //Name - Program - Date - Inactive/Active - ClassCode - ClassName - 2024 Fall Semester
    //0    - 1       - 2    - 3               - 4         - 5         - 6

    String[] detailsArr;
    if (!details.equals("")){
        detailsArr = details.split(" - ");
        semesterType = detailsArr[6];
    } else {
        detailsArr = new String[]{""};
    }

    
    //Addition For Getting Semester Type
    // if (meetingPatternsArr.length >= 6) {
    //   semesterType = meetingPatternsArr[5];//.substring(5).replaceAll(" ", "");
    // } else {
    //   System.out.println("EventDetailsNotFound");
    // }

    String[] meetingPatternsArr = eventInput.split("( \\| )|( \\|)");//Splits meeting patterns string into str componets to use later
      if (eventInput.equals(" ")) {
        System.out.println("Not scheduled take note");
        return null;
      }
      //System.out.println("Starting meeting patterns");
      for (String s : meetingPatternsArr) {
        System.out.println(s);
      }



      // some meeting times have the start and end date included, this corrects for that
      String[] daysMeetingStrArr;
      if (meetingPatternsArr.length > 3){
        daysMeetingStrArr = meetingPatternsArr[1].split("/");
      } else{
        daysMeetingStrArr = meetingPatternsArr[0].split("/");
      }
      

      
      
      //Makes it so that it always takes the last part of the array for location
      //Needed for cases where meetingPatternsArr contains Start/End date as well
      String location;
      int meetingPatternsArrLength = meetingPatternsArr.length;

      location = (meetingPatternsArr.length < 3 ? "" : CalendarHelperFunctions.shortBuildingToAddress.containsKey(meetingPatternsArr[meetingPatternsArrLength-1].split(" ")[0]) 
                                                     ? CalendarHelperFunctions.shortBuildingToAddress.get(meetingPatternsArr[meetingPatternsArrLength-1].split(" ")[0]) 
                                                     : meetingPatternsArr[meetingPatternsArrLength-1].split(" \\d")[0]);//Defaults to short address if long address is not in hashmap
      
      Faculty instructor = null;
      if (!instructorCell.substring(0, 1).equals(" ")) {
        instructor = FacultyHelperFunctions.getFacultyFromName(facultyList, instructorCell);

      }
      String description = "Instructor - " + (instructorCell.substring(0, 1).equals(" ") ? "?" : instructorCell) + "\n" + 
                        "Instructional Format - " + instructionalFormat + "\n" + 
                        "Delivery Mode - " + deliveryModeCell + 
                        (meetingPatternsArr.length < 3 ? "" : " (" + meetingPatternsArr[meetingPatternsArr.length-1] + ")");
      System.out.println(description);
      if (instructor != null) {
        description += "\n" + "---" +
                       "\n" + "Email - " + instructor.getEmail() +
                       "\n" + "Phone - " + instructor.getPhone() +
                       "\n" + "Instructor Position - " + instructor.getStatus() +
                       "\n" + "Department - " + instructor.getDepartment() +
                       "\n" + "Office - " + instructor.getOffice() +
                       "\n" + "---";
      }

      LocalDate classesStartReccuringDate = LocalDate.parse(startCell, DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy"));
      LocalDate classesEndReccuringDate = LocalDate.parse(endCell, DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy"));

      System.out.println(classesStartReccuringDate);
      System.out.println(classesEndReccuringDate);

      LocalDate startLocalDate = CalendarHelperFunctions.getNextDateOfDayFromDate(classesStartReccuringDate, CalendarHelperFunctions.getStartDay(daysMeetingStrArr, classesStartReccuringDate));
      LocalDate endLocalDate = CalendarHelperFunctions.getNextDateOfDayFromDate(classesStartReccuringDate, CalendarHelperFunctions.getStartDay(daysMeetingStrArr, classesStartReccuringDate));
    
      //fix for when meetingPatternsArr contains Start/End date as well
      String[] startAndEndTimesInHalfs;
      if (meetingPatternsArr.length > 3) {
        startAndEndTimesInHalfs = meetingPatternsArr[2].split(" - ");
      } else {
        startAndEndTimesInHalfs = meetingPatternsArr[1].split(" - ");
      }


      LocalTime startLocalTime = LocalTime.parse(startAndEndTimesInHalfs[0], DateTimeFormatter.ofPattern("H:mm"));
      LocalTime endLocalTime = LocalTime.parse(startAndEndTimesInHalfs[1], DateTimeFormatter.ofPattern("H:mm"));

      //System.out.println("Start");
      System.out.println(startLocalTime.format(DateTimeFormatter.ofPattern("H:mm")));
      System.out.println(endLocalTime.format(DateTimeFormatter.ofPattern("H:mm")));//2015-05-28T09:00:00-07:00

      // String startDateTimeStr = startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T" + 
      // startLocalTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "-04:00";
      // System.out.println(startDateTimeStr);
      // //DateTime startDateTime = new DateTime(startDateTimeStr);


      // String endDateTimeStr = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))  + "T" + 
      // endLocalTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "-04:00";
      // System.out.println(endDateTimeStr);
      // //DateTime endDateTime = new DateTime(endDateTimeStr);

      String recurrence = CalendarHelperFunctions.makeRecurrenceString(classesEndReccuringDate, daysMeetingStrArr);

      
      
      return CalendarHelperFunctions.makeEvent(eventName, location, description, recurrence, LocalDateTime.of(startLocalDate, startLocalTime), 
                                                                           LocalDateTime.of(endLocalDate, endLocalTime));
  }
}