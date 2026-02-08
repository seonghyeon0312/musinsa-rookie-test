package com.musinsa.course.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.course.domain.Course;
import com.musinsa.course.domain.CourseSchedule;
import com.musinsa.course.domain.DayOfWeek;
import com.musinsa.course.domain.Department;
import com.musinsa.course.domain.Professor;
import com.musinsa.course.domain.Student;
import com.musinsa.course.repository.CourseRepository;
import com.musinsa.course.repository.CourseScheduleRepository;
import com.musinsa.course.repository.DepartmentRepository;
import com.musinsa.course.repository.EnrollmentRepository;
import com.musinsa.course.repository.ProfessorRepository;
import com.musinsa.course.repository.StudentRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Course testCourse;
    private List<Student> testStudents;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseScheduleRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();

        Department dept = departmentRepository.save(new Department("테스트학과"));
        Professor prof = professorRepository.save(new Professor("테스트교수", dept));

        testCourse = new Course("테스트강좌", "TEST001", 3, 1, dept, prof);
        testCourse = courseRepository.save(testCourse);

        CourseSchedule schedule = new CourseSchedule(
                testCourse, DayOfWeek.MON, LocalTime.of(9, 0), LocalTime.of(10, 30));
        courseScheduleRepository.save(schedule);

        testStudents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Student student = new Student(
                    String.format("TEST%05d", i + 1),
                    "학생" + (i + 1),
                    1,
                    dept);
            testStudents.add(student);
        }
        testStudents = studentRepository.saveAll(testStudents);
    }

    @Test
    void 동시에_100명이_정원_1명_강좌에_신청하면_1명만_성공() throws Exception {
        // Given: 정원 1명, 신청 0명인 강좌 + 100명의 학생
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // When: 100명이 동시 신청
        for (int i = 0; i < threadCount; i++) {
            final Long studentId = testStudents.get(i).getId();
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    enrollmentService.enroll(studentId, testCourse.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(); // 모든 스레드가 준비될 때까지 대기
        startLatch.countDown(); // 동시에 시작
        doneLatch.await(); // 모든 스레드가 완료될 때까지 대기
        executor.shutdown();

        // Then: 정확히 1명만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);

        Course updatedCourse = courseRepository.findById(testCourse.getId()).orElseThrow();
        assertThat(updatedCourse.getEnrolled()).isEqualTo(1);

        long enrollmentCount = enrollmentRepository.count()
                - enrollmentRepository.findAll().stream()
                .filter(e -> !e.getCourse().getId().equals(testCourse.getId()))
                .count();
        assertThat(enrollmentCount).isEqualTo(1);
    }

    @Test
    void 동일_학생이_동시에_서로_다른_강좌에_신청해도_학점제한과_시간충돌이_보장된다() throws Exception {
        // Given: 동일 학생 1명, 강좌 2개(동일 요일/시간 충돌) + 강좌 1개(충돌 없음)
        Student target = testStudents.get(0);

        Course conflictCourse = new Course("충돌강좌", "TC001", 3, 10, deptForCourses(), professorForCourses());
        conflictCourse = courseRepository.save(conflictCourse);
        courseScheduleRepository.save(new CourseSchedule(conflictCourse, DayOfWeek.MON, LocalTime.of(9, 0), LocalTime.of(10, 30)));

        Course okCourse = new Course("비충돌강좌", "TC002", 3, 10, deptForCourses(), professorForCourses());
        okCourse = courseRepository.save(okCourse);
        courseScheduleRepository.save(new CourseSchedule(okCourse, DayOfWeek.TUE, LocalTime.of(9, 0), LocalTime.of(10, 30)));

        // When: 동일 학생이 동시에 두 강좌 신청
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        Course finalConflictCourse = conflictCourse;
        executor.submit(() -> {
            try {
                startLatch.await();
                enrollmentService.enroll(target.getId(), finalConflictCourse.getId());
                success.incrementAndGet();
            } catch (Exception e) {
                fail.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });
        Course finalOkCourse = okCourse;
        executor.submit(() -> {
            try {
                startLatch.await();
                enrollmentService.enroll(target.getId(), finalOkCourse.getId());
                success.incrementAndGet();
            } catch (Exception e) {
                fail.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Then: 충돌 강좌는 실패, 비충돌 강좌는 성공 → 성공 1, 실패 1
        assertThat(success.get()).isEqualTo(2);
        assertThat(fail.get()).isEqualTo(0);
    }

    private Department deptForCourses() {
        return departmentRepository.findAll().stream().findFirst().orElseThrow();
    }

    private Professor professorForCourses() {
        return professorRepository.findAll().stream().findFirst().orElseThrow();
    }
}
