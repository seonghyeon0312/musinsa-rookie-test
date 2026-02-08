package com.musinsa.course.service;

import com.musinsa.course.domain.Course;
import com.musinsa.course.domain.CourseSchedule;
import com.musinsa.course.domain.DayOfWeek;
import com.musinsa.course.domain.Department;
import com.musinsa.course.domain.Professor;
import com.musinsa.course.domain.Student;
import com.musinsa.course.repository.CourseRepository;
import com.musinsa.course.repository.CourseScheduleRepository;
import com.musinsa.course.repository.DepartmentRepository;
import com.musinsa.course.repository.ProfessorRepository;
import com.musinsa.course.repository.StudentRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String[] LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    };

    private static final String[] FIRST_NAMES = {
            "민준", "서윤", "지호", "서연", "도윤", "하윤", "시우", "하은",
            "주원", "지유", "예준", "수아", "건우", "지아", "현우", "유나",
            "준서", "채원", "은우", "소율", "우진", "다은", "승현", "예은",
            "지훈", "수빈", "태윤", "민서", "준혁", "하린"
    };

    private static final String[] DEPARTMENT_NAMES = {
            "컴퓨터공학과", "전자공학과", "기계공학과", "화학공학과", "건축공학과",
            "경영학과", "경제학과", "수학과", "물리학과", "생명과학과",
            "국어국문학과", "영어영문학과"
    };

    private static final String[][] COURSE_NAMES_BY_DEPT = {
            // 컴퓨터공학과
            {"자료구조", "알고리즘", "운영체제", "데이터베이스", "컴퓨터구조", "네트워크", "소프트웨어공학", "인공지능", "컴퓨터보안", "웹프로그래밍"},
            // 전자공학과
            {"회로이론", "전자기학", "신호처리", "통신이론", "반도체공학", "제어공학", "디지털시스템", "전력전자", "마이크로프로세서", "광공학"},
            // 기계공학과
            {"열역학", "유체역학", "고체역학", "동역학", "기계설계", "제조공학", "자동제어", "로봇공학", "에너지공학", "재료역학"},
            // 화학공학과
            {"유기화학", "무기화학", "물리화학", "분석화학", "반응공학", "분리공정", "공정제어", "촉매공학", "고분자공학", "생물화학공학"},
            // 건축공학과
            {"구조역학", "건축계획", "건축시공", "건축환경", "철근콘크리트", "건축설비", "도시계획", "건축법규", "건축재료", "토질역학"},
            // 경영학과
            {"경영학원론", "마케팅", "재무관리", "인사관리", "경영전략", "회계원리", "생산관리", "국제경영", "조직행동론", "경영정보시스템"},
            // 경제학과
            {"미시경제학", "거시경제학", "계량경제학", "국제경제학", "노동경제학", "재정학", "화폐금융론", "산업조직론", "경제수학", "경제사"},
            // 수학과
            {"미적분학", "선형대수학", "해석학", "대수학", "위상수학", "확률론", "통계학", "수치해석", "미분방정식", "이산수학"},
            // 물리학과
            {"일반물리학", "역학", "전자기학개론", "양자역학", "열통계물리학", "광학", "고체물리학", "핵물리학", "천체물리학", "현대물리학"},
            // 생명과학과
            {"일반생물학", "세포생물학", "유전학", "분자생물학", "생태학", "미생물학", "생화학", "면역학", "발생생물학", "신경과학"},
            // 국어국문학과
            {"한국어학개론", "한국문학개론", "현대소설론", "현대시론", "고전문학사", "국어음운론", "국어문법론", "비교문학", "문학비평론", "한국어교육론"},
            // 영어영문학과
            {"영어학개론", "영문학개론", "영어음성학", "영어통사론", "영미소설", "영미시", "번역론", "영어작문", "영어회화", "영미문화론"}
    };

    private static final String[] COURSE_SUFFIXES = {"기초", "심화", "응용", "실습", "특론"};

    private static final LocalTime[] TIME_SLOTS = {
            LocalTime.of(9, 0), LocalTime.of(10, 30),
            LocalTime.of(12, 0), LocalTime.of(13, 30),
            LocalTime.of(15, 0), LocalTime.of(16, 30)
    };

    private static final DayOfWeek[][] DAY_PAIRS = {
            {DayOfWeek.MON, DayOfWeek.WED},
            {DayOfWeek.TUE, DayOfWeek.THU},
            {DayOfWeek.WED, DayOfWeek.FRI},
            {DayOfWeek.MON, DayOfWeek.THU},
            {DayOfWeek.TUE, DayOfWeek.FRI}
    };

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private final DepartmentRepository departmentRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final StudentRepository studentRepository;

    public DataInitializer(DepartmentRepository departmentRepository,
                           ProfessorRepository professorRepository,
                           CourseRepository courseRepository,
                           CourseScheduleRepository courseScheduleRepository,
                           StudentRepository studentRepository) {
        this.departmentRepository = departmentRepository;
        this.professorRepository = professorRepository;
        this.courseRepository = courseRepository;
        this.courseScheduleRepository = courseScheduleRepository;
        this.studentRepository = studentRepository;
    }

    public boolean isReady() {
        return ready.get();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long startTime = System.currentTimeMillis();
        log.info("초기 데이터 생성 시작...");

        Random random = new Random(42);

        List<Department> departments = generateDepartments();
        departmentRepository.saveAll(departments);
        log.info("학과 {}개 생성 완료", departments.size());

        List<Professor> professors = generateProfessors(departments, random);
        professorRepository.saveAll(professors);
        log.info("교수 {}명 생성 완료", professors.size());

        List<Course> courses = generateCourses(departments, professors, random);
        courseRepository.saveAll(courses);

        List<CourseSchedule> schedules = generateSchedules(courses, random);
        courseScheduleRepository.saveAll(schedules);
        log.info("강좌 {}개, 시간표 {}개 생성 완료", courses.size(), schedules.size());

        List<Student> students = generateStudents(departments, random);
        studentRepository.saveAll(students);
        log.info("학생 {}명 생성 완료", students.size());

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("초기 데이터 생성 완료! (소요시간: {}ms)", elapsed);

        ready.set(true);
    }

    private List<Department> generateDepartments() {
        List<Department> departments = new ArrayList<>(DEPARTMENT_NAMES.length);
        for (String name : DEPARTMENT_NAMES) {
            departments.add(new Department(name));
        }
        return departments;
    }

    private List<Professor> generateProfessors(List<Department> departments, Random random) {
        List<Professor> professors = new ArrayList<>(120);
        for (int i = 0; i < departments.size(); i++) {
            Department dept = departments.get(i);
            int count = (i < 5) ? 10 : 8; // 공학계열은 10명, 나머지는 8명
            for (int j = 0; j < count; j++) {
                String name = LAST_NAMES[random.nextInt(LAST_NAMES.length)]
                        + FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                professors.add(new Professor(name, dept));
            }
        }
        return professors;
    }

    private List<Course> generateCourses(List<Department> departments,
                                          List<Professor> professors, Random random) {
        List<Course> courses = new ArrayList<>(500);
        int courseIndex = 0;
        int profIndex = 0;

        for (int deptIdx = 0; deptIdx < departments.size(); deptIdx++) {
            Department dept = departments.get(deptIdx);
            String[] courseNames = COURSE_NAMES_BY_DEPT[deptIdx];

            // 각 학과에서 기본 강좌 + 변형 강좌 생성
            int profStartIdx = profIndex;
            int profCount = (deptIdx < 5) ? 10 : 8;

            for (String baseName : courseNames) {
                // 기본 강좌
                int credits = 2 + random.nextInt(2); // 2 or 3
                int capacity = 20 + random.nextInt(31); // 20~50
                Professor prof = professors.get(profStartIdx + (courseIndex % profCount));
                String courseCode = String.format("%s%03d", dept.getName().substring(0, 2), courseIndex + 1);

                courses.add(new Course(baseName, courseCode, credits, capacity, dept, prof));
                courseIndex++;

                // 변형 강좌 (심화/응용/실습 등)
                for (int s = 0; s < 4 && courses.size() < 500; s++) {
                    String suffix = COURSE_SUFFIXES[random.nextInt(COURSE_SUFFIXES.length)];
                    String variantName = baseName + " " + suffix;
                    credits = 1 + random.nextInt(3); // 1~3
                    capacity = 20 + random.nextInt(31);
                    prof = professors.get(profStartIdx + (courseIndex % profCount));
                    courseCode = String.format("%s%03d", dept.getName().substring(0, 2), courseIndex + 1);

                    courses.add(new Course(variantName, courseCode, credits, capacity, dept, prof));
                    courseIndex++;

                    if (courses.size() >= 500) break;
                }
                if (courses.size() >= 500) break;
            }
            profIndex += profCount;
            if (courses.size() >= 500) break;
        }

        return courses;
    }

    private List<CourseSchedule> generateSchedules(List<Course> courses, Random random) {
        List<CourseSchedule> schedules = new ArrayList<>(courses.size() * 2);

        for (Course course : courses) {
            DayOfWeek[] dayPair = DAY_PAIRS[random.nextInt(DAY_PAIRS.length)];
            LocalTime startTime = TIME_SLOTS[random.nextInt(TIME_SLOTS.length)];
            LocalTime endTime = startTime.plusMinutes(90);

            // 모든 강좌는 주 2회 수업
            for (DayOfWeek day : dayPair) {
                schedules.add(new CourseSchedule(course, day, startTime, endTime));
            }
        }

        return schedules;
    }

    private List<Student> generateStudents(List<Department> departments, Random random) {
        List<Student> students = new ArrayList<>(10000);

        for (int i = 0; i < 10000; i++) {
            String name = LAST_NAMES[random.nextInt(LAST_NAMES.length)]
                    + FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String studentNumber = String.format("2024%05d", i + 1);
            int grade = 1 + random.nextInt(4); // 1~4학년
            Department dept = departments.get(random.nextInt(departments.size()));

            students.add(new Student(studentNumber, name, grade, dept));
        }

        return students;
    }
}
