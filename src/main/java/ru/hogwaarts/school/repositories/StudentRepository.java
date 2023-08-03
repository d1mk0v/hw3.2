package ru.hogwaarts.school.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwaarts.school.models.Student;

import java.util.Collection;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByAge(int age);
    Collection<Student> findByAgeBetween(int min, int max);
}
