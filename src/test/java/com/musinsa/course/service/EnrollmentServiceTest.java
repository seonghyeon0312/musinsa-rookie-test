package com.musinsa.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.musinsa.course.domain.Course;
import com.musinsa.course.domain.CourseSchedule;
import com.musinsa.course.domain.DayOfWeek;
import com.musinsa.course.domain.Department;
import com.musinsa.course.domain.Professor;
import com.musinsa.course.domain.Student;
import com.musinsa.course.dto.EnrollmentResponse;
import com.musinsa.course.exception.CapacityExceededException;
import com.musinsa.course.exception.CourseNotFoundException;
import com.musinsa.course.exception.CreditLimitExceededException;
import com.musinsa.course.exception.DuplicateEnrollmentException;
import com.musinsa.course.exception.EnrollmentNotFoundException;
import com.musinsa.course.exception.StudentNotFoundException;
import com.musinsa.course.exception.TimeConflictException;
import com.musinsa.course.repository.CourseRepository;
import com.musinsa.course.repository.CourseScheduleRepository;
import com.musinsa.course.repository.DepartmentRepository;
import com.musinsa.course.repository.EnrollmentRepository;
import com.musinsa.course.repository.ProfessorRepository;
import com.musinsa.course.repository.StudentRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EnrollmentServiceTest {

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

    private Department dept;
    private Professor prof;
    private Student student;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseScheduleRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();

        dept = departmentRepository.save(new Department("컴퓨터공학과"));
        prof = professorRepository.save(new Professor("김교수", dept));
        student = studentRepository.save(new Student("202400001", "홍길동", 2, dept));
    }

    @Test
    void 수강신청_성공() {
        Course course = createCourse("자료구조", "CS001", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));

        EnrollmentResponse response = enrollmentService.enroll(student.getId(), course.getId());

        assertThat(response.getCourseName()).isEqualTo("자료구조");
        assertThat(response.getStudentName()).isEqualTo("홍길동");

        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updated.getEnrolled()).isEqualTo(1);
    }

    @Test
    void 정원초과시_수강신청_실패() {
        Course course = createCourse("알고리즘", "CS002", 3, 0, DayOfWeek.TUE, LocalTime.of(10, 30));

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course.getId()))
                .isInstanceOf(CapacityExceededException.class)
                .hasMessageContaining("정원이 초과");
    }

    @Test
    void 중복수강신청_실패() {
        Course course = createCourse("운영체제", "CS003", 3, 30, DayOfWeek.WED, LocalTime.of(12, 0));

        enrollmentService.enroll(student.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course.getId()))
                .isInstanceOf(DuplicateEnrollmentException.class)
                .hasMessageContaining("이미 수강신청");
    }

    @Test
    void 학점초과시_수강신청_실패() {
        // 18학점 꽉 채우기 (3학점 x 6과목)
        for (int i = 0; i < 6; i++) {
            DayOfWeek day = DayOfWeek.values()[i % 5];
            LocalTime time = LocalTime.of(9 + (i / 5) * 3, 0);
            Course course = createCourse("과목" + i, "CR" + String.format("%03d", i), 3, 30, day, time);
            enrollmentService.enroll(student.getId(), course.getId());
        }

        // 추가 1학점 과목 신청 시도 (총 19학점)
        Course extraCourse = createCourse("추가과목", "CR999", 1, 30, DayOfWeek.FRI, LocalTime.of(16, 30));

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), extraCourse.getId()))
                .isInstanceOf(CreditLimitExceededException.class)
                .hasMessageContaining("18학점");
    }

    @Test
    void 시간충돌시_수강신청_실패() {
        Course course1 = createCourse("자료구조", "CS010", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));
        enrollmentService.enroll(student.getId(), course1.getId());

        // 같은 시간대 다른 강좌
        Course course2 = createCourse("알고리즘", "CS011", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course2.getId()))
                .isInstanceOf(TimeConflictException.class)
                .hasMessageContaining("시간이 겹칩니다");
    }

    @Test
    void 수강취소_후_재신청_성공() {
        Course course = createCourse("네트워크", "CS020", 3, 1, DayOfWeek.THU, LocalTime.of(13, 30));

        EnrollmentResponse response = enrollmentService.enroll(student.getId(), course.getId());
        assertThat(courseRepository.findById(course.getId()).orElseThrow().getEnrolled()).isEqualTo(1);

        // 취소
        enrollmentService.cancel(response.getId());
        assertThat(courseRepository.findById(course.getId()).orElseThrow().getEnrolled()).isEqualTo(0);

        // 재신청
        EnrollmentResponse response2 = enrollmentService.enroll(student.getId(), course.getId());
        assertThat(response2).isNotNull();
        assertThat(courseRepository.findById(course.getId()).orElseThrow().getEnrolled()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_학생ID로_수강신청시_실패() {
        Course course = createCourse("자료구조", "CS030", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));

        assertThatThrownBy(() -> enrollmentService.enroll(99999L, course.getId()))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("학생을 찾을 수 없습니다");
    }

    @Test
    void 존재하지_않는_강좌ID로_수강신청시_실패() {
        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), 99999L))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining("강좌를 찾을 수 없습니다");
    }

    @Test
    void 존재하지_않는_수강신청ID로_취소시_실패() {
        assertThatThrownBy(() -> enrollmentService.cancel(99999L))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessageContaining("수강신청 내역을 찾을 수 없습니다");
    }

    @Test
    void 부분적으로_시간이_겹치는_강좌_수강신청시_실패() {
        // 09:00-10:30 강좌 신청
        Course course1 = createCourse("자료구조", "CS040", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));
        enrollmentService.enroll(student.getId(), course1.getId());

        // 10:00-11:30 강좌 신청 시도 (30분 겹침)
        Course course2 = createCourse("알고리즘", "CS041", 3, 30, DayOfWeek.MON, LocalTime.of(10, 0));

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course2.getId()))
                .isInstanceOf(TimeConflictException.class)
                .hasMessageContaining("시간이 겹칩니다");
    }

    @Test
    void 시간대만_같고_요일은_다르면_성공() {
        // 월 09:00-10:30
        Course course1 = createCourse("자료구조", "CS050", 3, 30, DayOfWeek.MON, LocalTime.of(9, 0));
        enrollmentService.enroll(student.getId(), course1.getId());

        // 화 09:00-10:30 (요일만 다름)
        Course course2 = createCourse("알고리즘", "CS051", 3, 30, DayOfWeek.TUE, LocalTime.of(9, 0));

        EnrollmentResponse response = enrollmentService.enroll(student.getId(), course2.getId());
        assertThat(response).isNotNull();
    }

    @Test
    void 학점_경계값_18학점에서_0학점_추가는_성공하고_1학점은_실패() {
        // 17학점까지 채우기 (3학점*5 + 2학점*1 = 17)
        for (int i = 0; i < 5; i++) {
            Course course = createCourse("과목A" + i, "CR1" + i, 3, 30,
                    DayOfWeek.values()[i % 5], LocalTime.of(8 + i, 0));
            enrollmentService.enroll(student.getId(), course.getId());
        }
        Course twoCredit = createCourse("과목B", "CR200", 3, 30, DayOfWeek.FRI, LocalTime.of(14, 0));
        enrollmentService.enroll(student.getId(), twoCredit.getId()); // 총 17학점

        // 0학점 강좌 가정 (세미나) -> 성공
        Course zeroCredit = createCourse("세미나", "CR201", 0, 10, DayOfWeek.MON, LocalTime.of(18, 0));
        EnrollmentResponse ok = enrollmentService.enroll(student.getId(), zeroCredit.getId());
        assertThat(ok).isNotNull();

        // 1학점 추가 -> 18을 넘기므로 실패
        Course oneCredit = createCourse("추가", "CR202", 1, 30, DayOfWeek.THU, LocalTime.of(19, 0));
        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), oneCredit.getId()))
                .isInstanceOf(CreditLimitExceededException.class);
    }

    @Test
    void 수강신청_ID_중복_취소시_실패() {
        Course course = createCourse("DB", "CS060", 3, 10, DayOfWeek.MON, LocalTime.of(11, 0));
        EnrollmentResponse resp = enrollmentService.enroll(student.getId(), course.getId());

        enrollmentService.cancel(resp.getId());

        assertThatThrownBy(() -> enrollmentService.cancel(resp.getId()))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }

    private Course createCourse(String name, String code, int credits, int capacity,
                                 DayOfWeek day, LocalTime startTime) {
        Course course = new Course(name, code, credits, capacity, dept, prof);
        course = courseRepository.save(course);
        CourseSchedule schedule = new CourseSchedule(course, day, startTime, startTime.plusMinutes(90));
        courseScheduleRepository.save(schedule);
        return course;
    }
}
