CREATE TABLE students (
    student_id VARCHAR(50) PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    face_vector BYTEA -- PostgreSQL
);

-- ३. Teachers Table (शिक्षकांसाठी)
CREATE TABLE teachers (
    teacher_id VARCHAR(50) PRIMARY KEY,
    teacher_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ४. Classes Table
CREATE TABLE classes (
    class_id VARCHAR(50) PRIMARY KEY,
    class_name VARCHAR(100) NOT NULL,
    teacher_id VARCHAR(50) REFERENCES teachers(teacher_id)
);

-- ५. Attendance Table
CREATE TABLE attendance (
    attendance_id SERIAL PRIMARY KEY, --
    student_name VARCHAR(100),
    subject VARCHAR(100),
    semester VARCHAR(50),
    status VARCHAR(20) DEFAULT 'Present',
    date DATE DEFAULT CURRENT_DATE,
    time TIME DEFAULT CURRENT_TIME
);