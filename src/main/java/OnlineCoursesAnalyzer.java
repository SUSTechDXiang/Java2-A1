
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      InputStreamReader isr = new InputStreamReader(new FileInputStream(datasetPath), "UTF-8");
      br = new BufferedReader(isr);
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //1
  public Map<String, Integer> getPtcpCountByInst() {
    Map<String, Integer> map = courses.stream().collect(Collectors.groupingBy(Course::getInstitution,
            Collectors.summingInt(Course::getParticipants)));
    return map;
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {

    Map<String, Integer> PtcpCountByInstAndSubject = courses.stream().collect(
            Collectors.groupingBy(o -> o.getInstitution() + "-"
                    + o.getSubject(),LinkedHashMap::new, Collectors.summingInt(Course::getParticipants)));
    List<Map.Entry<String,Integer>> list = new ArrayList<>(PtcpCountByInstAndSubject.entrySet());

    list.sort((o1, o2) -> {
      int p1 = o1.getValue();
      int p2 = o2.getValue();
      return p2 - p1;
    });

    LinkedHashMap<String, Integer> newMap = new LinkedHashMap <>();
    list.forEach(o -> newMap.put(o.getKey(),o.getValue()));

    return newMap;
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> map = new HashMap<>();
    List<String> instructors = new ArrayList<>();
    for (Course cours : courses) {
      String[] ins = cours.instructors.split(", ");
      Collections.addAll(instructors, ins);
    }
    instructors = instructors.stream().distinct().collect(Collectors.toList());
    for (String instructor : instructors) {
      List<String> course0 = new ArrayList<>();
      List<String> course1 = new ArrayList<>();
      List<List<String>> course = new ArrayList<>();
      course.add(course0);
      course.add(course1);
      map.put(instructor, course);
    }
    for (Course cours : courses) {
      String[] ins = cours.instructors.split(", ");
      if (ins.length == 1){
        map.get(ins[0]).get(0).add(cours.title);
      } else {
        for (String in : ins) {
          map.get(in).get(1).add(cours.title);
        }
      }
    }
    Map<String, List<List<String>>> newmap = new LinkedHashMap<>();
    for (String in : map.keySet()){
      List<List<String>> listList = new ArrayList<>();
      listList.add(map.get(in).get(0).stream().distinct().sorted(String::compareTo).collect(Collectors.toList()));
      listList.add(map.get(in).get(1).stream().distinct().sorted(String::compareTo).collect(Collectors.toList()));
      newmap.put(in,listList);
    }
    return newmap;
  }

  //4
  public List<String> getCourses(int topK, String by) {
    if (by.equals("hours")){
      return courses.stream()
                .sorted(Comparator.comparing(Course::getTotalHours).reversed())
                .map(Course::getTitle)
                .distinct()
                .limit(topK)
                .collect(Collectors.toList());
    }
    if (by.equals("participants")){
      return courses.stream()
              .sorted(Comparator.comparing(Course::getParticipants).reversed())
              .map(Course::getTitle)
              .distinct()
              .limit(topK)
              .collect(Collectors.toList());
    }

    return null;
  }

  //5
  public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
    List<String> cours = courses.stream()
            .filter(course -> course.getSubject().toUpperCase().contains(courseSubject.toUpperCase()))
            .filter(course -> course.getPercentAudited() >= percentAudited )
            .filter(course -> course.getTotalHours() <= totalCourseHours )
            .map(course -> course.title)
            .distinct()
            .sorted(String::compareTo)
            .collect(Collectors.toList());

    return cours;
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Map<String, String> courseTitle = courses.stream()
            .sorted(Comparator.comparing(Course::getLaunchDate).reversed())
            .collect(Collectors.toMap(Course::getNumber,Course::getTitle,(t1,t2) -> t1));

    Map<String, Double> averageMedianAge = courses.stream()
            .collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getMedianAge)));

    Map<String, Double> averagePercentMale = courses.stream()
            .collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getPercentMale)));

    Map<String, Double> averagePercentDegree = courses.stream()
            .collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getPercentDegree)));

    Map<String, Double> similarityValue = new LinkedHashMap<>();
    for (String s: averageMedianAge.keySet()){
      double sv = (age - averageMedianAge.get(s)) * (age - averageMedianAge.get(s))
              + (gender * 100 - averagePercentMale.get(s)) * (gender * 100 - averagePercentMale.get(s))
              + (isBachelorOrHigher * 100 - averagePercentDegree.get(s)) * (isBachelorOrHigher * 100 - averagePercentDegree.get(s));
      similarityValue.put(s,sv);
    }
    List<String> recours = new ArrayList<>();
    List<Map.Entry<String,Double>> list = new ArrayList<>(similarityValue.entrySet());

    list.sort(Map.Entry.comparingByValue());
    list.sort((o1, o2) -> {
      Double p1 = o1.getValue();
      Double p2 = o2.getValue();
      if (p1.equals(p2)){
        return courseTitle.get(o1.getKey()).compareTo(courseTitle.get(o2.getKey()));
      }
      return p1.compareTo(p2);
    });
    recours = list.stream().map(Map.Entry::getKey).distinct().collect(Collectors.toList());
    List<String> r = new ArrayList<>();
    for (int i = 0; i < recours.size(); i++) {
      r.add(courseTitle.get(recours.get(i)));
    }
    r = r.stream().distinct().limit(10).collect(Collectors.toList());
    return r;
  }
}

class Course {
  String institution;
  String number;
  Date launchDate;
  String title;
  String instructors;
  String subject;
  int year;
  int honorCode;
  int participants;
  int audited;
  int certified;
  double percentAudited;
  double percentCertified;
  double percentCertified50;
  double percentVideo;
  double percentForum;
  double gradeHigherZero;
  double totalHours;
  double medianHoursCertification;
  double medianAge;
  double percentMale;
  double percentFemale;
  double percentDegree;

  public Course(String institution, String number, Date launchDate,
              String title, String instructors, String subject,
              int year, int honorCode, int participants,
              int audited, int certified, double percentAudited,
              double percentCertified, double percentCertified50,
              double percentVideo, double percentForum, double gradeHigherZero,
              double totalHours, double medianHoursCertification,
              double medianAge, double percentMale, double percentFemale,
              double percentDegree) {
    this.institution = institution;
    this.number = number;
    this.launchDate = launchDate;
    if (title.startsWith("\"")) title = title.substring(1);
    if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
    this.title = title;
    if (instructors.startsWith("\"")) instructors = instructors.substring(1);
    if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
    this.instructors = instructors;
    if (subject.startsWith("\"")) subject = subject.substring(1);
    if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
    this.subject = subject;
    this.year = year;
    this.honorCode = honorCode;
    this.participants = participants;
    this.audited = audited;
    this.certified = certified;
    this.percentAudited = percentAudited;
    this.percentCertified = percentCertified;
    this.percentCertified50 = percentCertified50;
    this.percentVideo = percentVideo;
    this.percentForum = percentForum;
    this.gradeHigherZero = gradeHigherZero;
    this.totalHours = totalHours;
    this.medianHoursCertification = medianHoursCertification;
    this.medianAge = medianAge;
    this.percentMale = percentMale;
    this.percentFemale = percentFemale;
    this.percentDegree = percentDegree;
  }

  public String getInstitution() {
    return institution;
  }

  public int getParticipants() {
    return participants;
  }

  public String getSubject() {
    return subject;
  }

  public String getInstructors() {
    return instructors;
  }

  public double getTotalHours() {
    return totalHours;
  }

  public String getTitle() {
    return title;
  }

  public double getPercentAudited() {
    return percentAudited;
  }

  public double getMedianAge() {
    return medianAge;
  }

  public double getPercentMale() {
    return percentMale;
  }

  public double getPercentDegree() {
    return percentDegree;
  }

  public String getNumber() {
    return number;
  }

  public Date getLaunchDate() {
    return launchDate;
  }
}

