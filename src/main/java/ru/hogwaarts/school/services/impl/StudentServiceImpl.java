package ru.hogwaarts.school.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwaarts.school.models.Faculty;
import ru.hogwaarts.school.models.Student;
import ru.hogwaarts.school.repositories.StudentRepository;
import ru.hogwaarts.school.services.api.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Student addStudent(Student student) {
        logger.info("Was invoked method for create student");
        return studentRepository.save(student);
    }

    @Override
    public Optional<Student> findStudent(Long id) {
        logger.info("Was invoked method for find student");
        Student foundStudent = studentRepository.findById(id).get();
        logger.debug("Student {} was find", id);
        return Optional.of(foundStudent);
    }

    @Override
    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student");
        Student editedStudent = studentRepository.save(student);
        logger.debug("Student {} was edited", editedStudent);
        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(long id) {
        logger.info("Was invoked method for delete student");
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> ageFilter(int age) {
        logger.info("Was invoked method for filter students by age");
        return studentRepository.findByAge(age);
    }

    @Override
    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        logger.info("Was invoked method for filter students by age between");
        return studentRepository.findByAgeBetween(min, max);
    }

    @Override
    public Faculty getStudentFaculty(Long id) {
        logger.info("Was invoked method for get student faculty");
        return studentRepository.findById(id).map(Student::getFaculty).orElse(null);
    }

    @Override
    public Integer getNumberOfAllStudents() {
        logger.info("Was invoked method for get number of all students");
        return studentRepository.getNumberOfAllStudents();
    }

    @Override
    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked method for get average age of students");
        return studentRepository.getAverageAgeOfStudents();
    }

    @Override
    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        return studentRepository.getLastFiveStudents();
    }

    @Override
    public List<String> getStudentsNameStartingWithA() {

        logger.info("Was invoked method for getting all the names of all students whose name starts with the letter A");

        List<Student> students = studentRepository.findAll();

        return students.stream()
                .parallel()
                .filter(student -> student.getName().startsWith("A"))
                .map(student -> student.getName().toUpperCase())
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageAgeOfAllStudents() {
        logger.info("Was invoked method for get average age of students");
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }

    @Override
    public void printStudentsNonSynchronized() {

        List<Student> students = studentRepository.findAll();

        printStudentsName(students.get(0).getName());
        printStudentsName(students.get(1).getName());

        new Thread(() -> {
            printStudentsName(students.get(2).getName());
            printStudentsName(students.get(3).getName());
        }).start();

        new Thread(() -> {
            printStudentsName(students.get(4).getName());
            printStudentsName(students.get(5).getName());
        }).start();
    }

    @Override
    public void printStudentsSynchronized() throws InterruptedException {

        List<Student> students = studentRepository.findAll();

        printStudentsNameSynchronized(students);
        printStudentsNameSynchronized(students);

        new Thread(() -> {
            //Для проверки упорядоченности
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            printStudentsNameSynchronized(students);
            printStudentsNameSynchronized(students);
        }).start();

        new Thread(() -> {
            printStudentsNameSynchronized(students);
            printStudentsNameSynchronized(students);
        }).start();

    }

    private void printStudentsName(String name) {
            System.out.println(name);
        }

    int index;
    private void printStudentsNameSynchronized(List<Student> students) {
            System.out.println(students.get(index++));
    }
}
