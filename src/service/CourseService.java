package service;

import model.Course;
import model.Student;
import datastructures.CustomHashMap;
import datastructures.CustomArrayList;
import persistence.DataPersistence;
import ui.BoxUI;

public class CourseService {
    private CustomHashMap<String, Course> courses;
    private static CourseService instance;

    private CourseService() {
        courses = new CustomHashMap<>();
    }

    public static CourseService getInstance() {
        if (instance == null) {
            instance = new CourseService();
        }
        return instance;
    }

    public void addCourse(Course course) {
        courses.put(course.getCourseCode(), course);
        DataPersistence.saveCourses(courses);
    }

    public Course getCourse(String courseCode) {
        return courses.get(courseCode);
    }

    public void updateCourse(Course course) {
        courses.put(course.getCourseCode(), course);
        DataPersistence.saveCourses(courses);
    }

    public boolean deleteCourse(String courseCode) {
        Course removed = courses.remove(courseCode);
        if (removed != null) {
            DataPersistence.saveCourses(courses);
            return true;
        }
        return false;
    }

    public CustomArrayList<Course> getAllCourses() {
        return courses.values();
    }

    public CustomArrayList<Course> getAvailableCourses() {
        CustomArrayList<Course> available = new CustomArrayList<>();
        CustomArrayList<Course> allCourses = courses.values();

        for (Course course : allCourses) {
            if (!course.isFull()) {
                available.add(course);
            }
        }
        return available;
    }

    public boolean enrollStudent(String studentNumber, String courseCode) {
        Course course = courses.get(courseCode);
        Student student = StudentService.getInstance().getStudent(studentNumber);

        if (course != null && student != null && !course.isFull()) {
            if (course.enrollStudent(studentNumber)) {
                student.enrollCourse(courseCode);
                StudentService.getInstance().updateStudent(student);
                DataPersistence.saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    public boolean dropStudent(String studentNumber, String courseCode) {
        Course course = courses.get(courseCode);
        Student student = StudentService.getInstance().getStudent(studentNumber);

        if (course != null && student != null) {
            if (course.dropStudent(studentNumber)) {
                student.dropCourse(courseCode);
                StudentService.getInstance().updateStudent(student);
                DataPersistence.saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    public void setCourses(CustomHashMap<String, Course> loadedCourses) {
        this.courses = loadedCourses;
    }

    public CustomHashMap<String, Course> getCoursesMap() {
        return courses;
    }

    // UI-facing module flow methods
    public void showAdminCoursesMenu(BoxUI box) {
        box.printMenu("MANAGE COURSES", new String[]{
                "1. List Courses",
                "2. Add Course",
                "3. Remove Course",
                "4. Back"
        });
        int a = box.readInt("Choose option: ", 1, 4);
        if (a == 1) {
            listCourses(box);
        } else if (a == 2) {
            addCourse(box);
        } else if (a == 3) {
            removeCourse(box);
        }
    }

    public void showStudentCourseRegistration(BoxUI box, Student student) {
        CustomArrayList<Course> courses = getAvailableCourses();
        CustomArrayList<String> enrolledCodes = student.getEnrolledCourses();

        String[] previewLines = buildCourseRegistrationLines(courses, enrolledCodes);
        box.printSection("COURSE REGISTRATION", previewLines);

        box.line("Available Courses:");
        if (courses.isEmpty()) {
            box.line("- No courses available.");
        } else {
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                box.line((i + 1) + ". " + c + " | Credits: " + c.getCredits() + " | " + c.getSchedule());
            }
        }

        box.line("");
        box.line("My Registered Courses:");
        if (enrolledCodes.isEmpty()) {
            box.line("- None");
        } else {
            for (int i = 0; i < enrolledCodes.size(); i++) {
                String code = enrolledCodes.get(i);
                Course course = getCourse(code);
                if (course != null) {
                    box.line((i + 1) + ". " + course);
                } else {
                    box.line((i + 1) + ". " + code);
                }
            }
        }
        box.endSection();

        box.printMenu("COURSE ACTIONS", new String[]{
                "1. Register for a Course",
                "2. Drop a Course",
                "3. Back"
        });
        int a = box.readInt("Choose option: ", 1, 3);
        if (a == 3) {
            return;
        }

        String code = box.prompt("Course code: ");
        if (a == 1) {
            if (enrollStudent(student.getStudentNumber(), code)) {
                box.success("Registered.");
            } else {
                box.error("Could not register (full, duplicate, or invalid code).");
            }
        } else {
            if (dropStudent(student.getStudentNumber(), code)) {
                box.success("Dropped.");
            } else {
                box.error("Could not drop.");
            }
        }
    }

    private void listCourses(BoxUI box) {
        CustomArrayList<Course> all = getAllCourses();
        if (all.isEmpty()) {
            box.info("No courses.");
            return;
        }
        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            lines[i] = (i + 1) + ". " + all.get(i);
        }
        box.printSection("COURSE LIST", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private void addCourse(BoxUI box) {
        String code = box.prompt("Course code: ");
        String name = box.prompt("Course name: ");
        String instructor = box.prompt("Instructor: ");
        int credits = box.readInt("Credits: ", 1, 12);
        int maxCapacity = box.readInt("Max capacity: ", 1, 500);
        String schedule = box.prompt("Schedule (e.g. Mon 10:00): ");
        if (schedule.isEmpty()) {
            schedule = "TBA";
        }
        addCourse(new Course(code, name, instructor, credits, maxCapacity, schedule));
        box.success("Course added.");
    }

    private void removeCourse(BoxUI box) {
        String code = box.prompt("Course code to remove: ");
        if (deleteCourse(code)) {
            box.success("Removed.");
        } else {
            box.error("Not found.");
        }
    }

    private String[] buildCourseRegistrationLines(CustomArrayList<Course> courses,
                                                  CustomArrayList<String> enrolledCodes) {
        int count = 3 + courses.size() + Math.max(1, enrolledCodes.size());
        String[] lines = new String[count];
        int idx = 0;
        lines[idx++] = "Available Courses:";
        if (courses.isEmpty()) {
            lines[idx++] = "- No courses available.";
        } else {
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                lines[idx++] = (i + 1) + ". " + c + " | Credits: " + c.getCredits() + " | " + c.getSchedule();
            }
        }
        lines[idx++] = "My Registered Courses:";
        if (enrolledCodes.isEmpty()) {
            lines[idx++] = "- None";
        } else {
            for (int i = 0; i < enrolledCodes.size(); i++) {
                String code = enrolledCodes.get(i);
                Course course = getCourse(code);
                lines[idx++] = (i + 1) + ". " + (course != null ? course.toString() : code);
            }
        }
        return lines;
    }
}