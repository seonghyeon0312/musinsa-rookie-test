package com.musinsa.course.service;

import com.musinsa.course.domain.Course;
import com.musinsa.course.domain.CourseSchedule;
import com.musinsa.course.domain.Enrollment;
import com.musinsa.course.domain.Student;
import com.musinsa.course.dto.EnrollmentResponse;
import com.musinsa.course.exception.CapacityExceededException;
import com.musinsa.course.exception.CreditLimitExceededException;
import com.musinsa.course.exception.DuplicateEnrollmentException;
import com.musinsa.course.exception.EnrollmentNotFoundException;
import com.musinsa.course.exception.CourseNotFoundException;
import com.musinsa.course.exception.StudentNotFoundException;
import com.musinsa.course.exception.TimeConflictException;
import com.musinsa.course.repository.CourseRepository;
import com.musinsa.course.repository.EnrollmentRepository;
import com.musinsa.course.repository.StudentRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);
    private static final int MAX_CREDITS = 18;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long courseId) {
        // 1. 강좌를 비관적 락과 함께 조회
        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 2. 정원 체크
        if (course.isFull()) {
            throw new CapacityExceededException(course.getName(), course.getCapacity());
        }

        // 3. 학생을 비관적 락과 함께 조회 (동일 학생의 동시 신청 직렬화)
        Student student = studentRepository.findByIdWithLock(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        // 4. 중복 수강 체크
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .ifPresent(e -> {
                    throw new DuplicateEnrollmentException(course.getName());
                });

        // 5. 학점 제한 체크
        validateCreditLimit(student, course);

        // 6. 시간 충돌 체크
        validateTimeConflict(student, course);

        // 7. 수강신청 처리
        course.incrementEnrolled();
        Enrollment enrollment = new Enrollment(student, course);
        enrollmentRepository.save(enrollment);

        log.info("수강신청 성공 - 학생: {} ({}), 강좌: {} ({})",
                student.getName(), student.getStudentNumber(),
                course.getName(), course.getCourseCode());

        return new EnrollmentResponse(enrollment);
    }

    @Transactional
    public void cancel(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 강좌를 락과 함께 조회하여 enrolled 감소
        Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(enrollment.getCourse().getId()));

        course.decrementEnrolled();
        enrollmentRepository.delete(enrollment);

        log.info("수강취소 완료 - 학생: {} ({}), 강좌: {} ({})",
                enrollment.getStudent().getName(), enrollment.getStudent().getStudentNumber(),
                course.getName(), course.getCourseCode());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException(studentId);
        }
        return enrollmentRepository.findByStudentIdWithCourseDetails(studentId).stream()
                .map(EnrollmentResponse::new)
                .toList();
    }

    private void validateCreditLimit(Student student, Course course) {
        int currentCredits = enrollmentRepository.sumCreditsByStudentId(student.getId());

        if (currentCredits + course.getCredits() > MAX_CREDITS) {
            throw new CreditLimitExceededException(currentCredits, course.getCredits());
        }
    }

    private void validateTimeConflict(Student student, Course newCourse) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        for (Enrollment enrollment : enrollments) {
            Course existingCourse = enrollment.getCourse();
            for (CourseSchedule existingSchedule : existingCourse.getSchedules()) {
                for (CourseSchedule newSchedule : newCourse.getSchedules()) {
                    if (existingSchedule.overlapsWith(newSchedule)) {
                        throw new TimeConflictException(
                                existingCourse.getName(),
                                existingSchedule.toDisplayString());
                    }
                }
            }
        }
    }
}
