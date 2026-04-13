package service;

import model.Course;
import model.Student;
import datastructures.CustomHashMap;
import datastructures.CustomArrayList;
import persistence.DataPersistence;

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
}