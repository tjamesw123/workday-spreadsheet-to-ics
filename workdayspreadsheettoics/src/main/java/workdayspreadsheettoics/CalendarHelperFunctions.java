package workdayspreadsheettoics;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

public class CalendarHelperFunctions {
    public static HashMap<String, DayOfWeek> strToDayOfWeek = new HashMap<String, DayOfWeek>(){{
    put("Sunday", DayOfWeek.SUNDAY);
    put("Monday", DayOfWeek.MONDAY);
    put("Tuesday", DayOfWeek.TUESDAY);
    put("Wednesday", DayOfWeek.WEDNESDAY);
    put("Thursday", DayOfWeek.THURSDAY);
    put("Friday", DayOfWeek.FRIDAY);
    put("Saturday", DayOfWeek.SATURDAY);
  }};

  public static HashMap<DayOfWeek, String> dayOfWeekToShortStr = new HashMap<DayOfWeek, String>(){{
    put(DayOfWeek.SUNDAY, "SU");
    put(DayOfWeek.MONDAY, "MO");
    put(DayOfWeek.TUESDAY, "TU");
    put(DayOfWeek.WEDNESDAY, "WE");
    put(DayOfWeek.THURSDAY, "TH");
    put(DayOfWeek.FRIDAY, "FR");
    put(DayOfWeek.SATURDAY, "SA");
  }};

  public static HashMap<String, String> shortBuildingToAddress = new HashMap<String, String>(){{//incomplete address list for whole school
    put("Howe", "Wesley J. Howe Center, 1 Castle Point Terrace, Hoboken, NJ 07030, USA");//McLean Hall, River St, Hoboken, NJ 07030, USA
    put("McLean", "McLean Hall, River St, Hoboken, NJ 07030, USA");
    put("Peirce", "Morton-Peirce-Kidde Complex, 607 River St, Hoboken, NJ 07030, USA");
    put("Carnegie", "Carnegie Laboratory, Hoboken, NJ 07030, USA");
    put("Burchard", "524 River St #713, Hoboken, NJ 07030");
    put("Kidde", "607 River St, Hoboken, NJ 07030");
    put("Gateway North", "601 Hudson St, Hoboken, NJ 07030");
    put("Edwin A. Stevens", "Hoboken, NJ 07030");
    put("Gateway South", "601 Hudson St, Hoboken, NJ 07030");
    put("Morton", "Morton-Peirce-Kidde Complex, 607 River St, Hoboken, NJ 07030, USA");
    put("Babbio", "525 River St, Hoboken, NJ 07030");
    put("North Building", "North Building, 1 Castle Point Terrace, Hoboken, NJ 07030, USA");//McLean Hall, River St, Hoboken, NJ 07030, USA
  }};
  public static DayOfWeek getStartDay(String[] daysMeetingStrArr, LocalDate classesStartReccuringDate) {
    // Needs to get the start day by checking which day is nearest to the classesStartReccuringDate
    // S M T W T F S

    //Loop through the days in the daysMeetingStrArr to check if they are on the classesStartReccuringDate or after it
    //if one of them is then pick that one
    //if not pick the first day in the list

    for (String s : daysMeetingStrArr) {
      DayOfWeek meetingDay = CalendarHelperFunctions.strToDayOfWeek.get(s);
      DayOfWeek reccuringStartDay = classesStartReccuringDate.getDayOfWeek();
      if (reccuringStartDay.getValue() <= meetingDay.getValue()) {
        return meetingDay;
      }
    }



    return CalendarHelperFunctions.strToDayOfWeek.get(daysMeetingStrArr[0]);
  }

  public static LocalDate getNextDateOfDayFromDate(LocalDate startingDate, DayOfWeek dayOfWeek) {//Bug that disallows for classes to start on the starting day given
    LocalDate result = startingDate;
    int currentDay = result.getDayOfWeek().getValue();
    int targetDay = dayOfWeek.getValue();
    int daysToAdd = targetDay - currentDay; 
    if (currentDay > targetDay) {
      daysToAdd += 7;
    }
    result = result.plusDays(daysToAdd);
    // while (result.getDayOfWeek() != dayOfWeek) {
    //   result = result.plusDays(1);
    // }
    return result;
  }

  public static String makeRecurrenceString(LocalDate localEndDate, String[] daysMeeting) {

    //RRULE:FREQ=WEEKLY;UNTIL=20230820T015615Z;WKST=SU;BYDAY=TU,TH //year month day
    String result = "FREQ=WEEKLY;UNTIL=" + localEndDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T000000Z;WKST=SU;BYDAY=";//SU
    for (int i = 0; i < daysMeeting.length; i++) {
      if (i == 0) {
        result += CalendarHelperFunctions.dayOfWeekToShortStr.get(CalendarHelperFunctions.strToDayOfWeek.get(daysMeeting[i]));
      } else {
        result += "," + CalendarHelperFunctions.dayOfWeekToShortStr.get(CalendarHelperFunctions.strToDayOfWeek.get(daysMeeting[i]));
      }
    }
    System.out.println(result);
    return result;
  }
  public static Set<String> listFilesUsingFilesList(String dir) throws IOException {
    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
        return stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toSet());
        }
    }
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
    public static VEvent makeEvent(String eventName, String location, String description, String recurrence, LocalDateTime startDateTime, LocalDateTime endDateTime) {
      UidGenerator ug = new RandomUidGenerator();
      Uid uid = ug.generateUid();
      
      Recur recur = new Recur(recurrence, false);
      RRule rrule = new RRule(recur);
      VEvent vEvent = new VEvent(startDateTime, endDateTime, eventName).withProperty(uid)
                                                                      .withProperty(new Description(description))
                                                                      .withProperty(new Location(location))
                                                                      .withProperty(rrule).getFluentTarget();
      return vEvent;
  }
}
