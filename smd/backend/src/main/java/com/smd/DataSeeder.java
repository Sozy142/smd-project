package com.smd;

import com.smd.entity.*;
import com.smd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SyllabusRepository syllabusRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository,
                      SyllabusRepository syllabusRepository,
                      ApprovalHistoryRepository historyRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.syllabusRepository = syllabusRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
            printDemoAccounts();
        }
        if (syllabusRepository.count() < 5) {
            User lecturer = userRepository.findByEmail("lecturer@smd.edu").orElseThrow();
            User hod      = userRepository.findByEmail("hod@smd.edu").orElseThrow();
            seedSyllabi(lecturer, hod);
        }
    }

    private void seedUsers() {
        createUser("admin@smd.edu",      "admin123",     "Admin",  "User",   Role.ADMIN,            "IT");
        createUser("lecturer@smd.edu",   "lecturer123",  "Alice",  "Nguyen", Role.LECTURER,         "Computer Science");
        createUser("hod@smd.edu",        "hod123",       "Bob",    "Tran",   Role.HOD,              "Computer Science");
        createUser("aa@smd.edu",         "aa123",        "Carol",  "Le",     Role.ACADEMIC_AFFAIRS, "Academic Affairs");
        createUser("principal@smd.edu",  "principal123", "David",  "Pham",   Role.PRINCIPAL,        "Rectorate");
        createUser("student@smd.edu",    "student123",   "Eve",    "Hoang",  Role.STUDENT,          "Computer Science");
    }

    private void seedSyllabi(User lecturer, User hod) {
        // ── Computer Science ──────────────────────────────────────────
        pub("CS101", "Introduction to Programming",     "Computer Science", 3,
            "Fundamental programming concepts using Python.",
            "CLO1: Write basic Python programs\nCLO2: Understand control flow\nCLO3: Apply functions and modules",
            "Midterm 30% · Final 40% · Labs 20% · Quizzes 10%",
            null, lecturer, hod);

        pub("CS102", "Web Basics",                      "Computer Science", 3,
            "Introduction to HTML, CSS, and basic JavaScript for building web pages.",
            "CLO1: Write valid HTML and CSS\nCLO2: Add interactivity with JavaScript\nCLO3: Understand the web request/response cycle",
            "Assignments 40% · Midterm 25% · Final 35%",
            null, lecturer, hod);

        pub("CS201", "Object-Oriented Programming",     "Computer Science", 3,
            "Design and implement programs using object-oriented principles in Java.",
            "CLO1: Define classes and objects\nCLO2: Apply inheritance and polymorphism\nCLO3: Use interfaces and abstract classes",
            "Projects 40% · Midterm 25% · Final 35%",
            "CS101", lecturer, hod);

        pub("CS202", "Data Structures",                 "Computer Science", 4,
            "Study of arrays, linked lists, stacks, queues, trees, and hash tables.",
            "CLO1: Implement common data structures\nCLO2: Analyse time and space complexity\nCLO3: Choose appropriate structures for problems",
            "Programming Projects 40% · Midterm 25% · Final 35%",
            "CS101", lecturer, hod);

        pub("CS203", "Database Systems",                "Computer Science", 3,
            "Relational database design, SQL, normalisation, and transaction management.",
            "CLO1: Design normalised schemas\nCLO2: Write complex SQL queries\nCLO3: Understand ACID properties",
            "Labs 30% · Midterm 30% · Final 40%",
            "CS101", lecturer, hod);

        pub("CS301", "Algorithms",                      "Computer Science", 4,
            "Algorithm design techniques: divide-and-conquer, dynamic programming, greedy, graph algorithms.",
            "CLO1: Analyse algorithm complexity\nCLO2: Apply dynamic programming\nCLO3: Implement graph algorithms",
            "Problem Sets 40% · Midterm 25% · Final 35%",
            "CS202", lecturer, hod);

        pub("CS302", "Software Engineering",            "Computer Science", 3,
            "Software development lifecycle, requirements, design patterns, testing, and agile practices.",
            "CLO1: Apply SDLC phases\nCLO2: Use design patterns\nCLO3: Write and run unit tests",
            "Group Project 50% · Midterm 20% · Final 30%",
            "CS201, CS202", lecturer, hod);

        pub("CS303", "Web Development",                 "Computer Science", 3,
            "Full-stack web development using modern frameworks for front-end and back-end.",
            "CLO1: Build REST APIs\nCLO2: Develop reactive front-ends\nCLO3: Integrate databases with web apps",
            "Projects 50% · Midterm 20% · Final 30%",
            "CS102, CS203", lecturer, hod);

        pub("CS401", "AI Fundamentals",                 "Computer Science", 4,
            "Search, knowledge representation, machine learning basics, and neural networks.",
            "CLO1: Implement search algorithms\nCLO2: Build a simple ML model\nCLO3: Understand neural network architectures",
            "Projects 40% · Midterm 25% · Final 35%",
            "CS301", lecturer, hod);

        // ── Business Administration ───────────────────────────────────
        pub("BA101", "Introduction to Business",        "Business Administration", 3,
            "Overview of business functions: management, marketing, finance, and operations.",
            "CLO1: Describe core business functions\nCLO2: Analyse business environments\nCLO3: Apply basic business terminology",
            "Case Studies 30% · Midterm 30% · Final 40%",
            null, lecturer, hod);

        pub("BA201", "Marketing Basics",                "Business Administration", 3,
            "Principles of marketing including the 4Ps, consumer behaviour, and market research.",
            "CLO1: Explain the marketing mix\nCLO2: Conduct basic market research\nCLO3: Develop a simple marketing plan",
            "Project 30% · Midterm 30% · Final 40%",
            "BA101", lecturer, hod);

        pub("BA202", "Financial Accounting",            "Business Administration", 3,
            "Recording, summarising, and reporting financial transactions; financial statement analysis.",
            "CLO1: Prepare journal entries\nCLO2: Produce financial statements\nCLO3: Interpret financial ratios",
            "Assignments 25% · Midterm 35% · Final 40%",
            "BA101", lecturer, hod);

        pub("BA301", "Digital Marketing",               "Business Administration", 3,
            "SEO, social media marketing, email campaigns, analytics, and digital advertising.",
            "CLO1: Plan a digital marketing campaign\nCLO2: Use analytics tools\nCLO3: Optimise content for SEO",
            "Campaign Project 40% · Midterm 25% · Final 35%",
            "BA201", lecturer, hod);

        // ── English ───────────────────────────────────────────────────
        pub("EN101", "English Fundamentals",            "English", 3,
            "Core grammar, vocabulary, reading comprehension, and basic writing skills.",
            "CLO1: Apply grammar rules correctly\nCLO2: Read and summarise texts\nCLO3: Write structured paragraphs",
            "Quizzes 20% · Midterm 30% · Final 50%",
            null, lecturer, hod);

        pub("EN201", "Business English",                "English", 3,
            "Professional writing, formal emails, reports, and business communication conventions.",
            "CLO1: Write professional emails\nCLO2: Produce business reports\nCLO3: Communicate in formal settings",
            "Assignments 30% · Midterm 30% · Final 40%",
            "EN101", lecturer, hod);

        pub("EN301", "Academic Writing",                "English", 3,
            "Research-based essay writing, citation conventions, critical thinking, and argumentation.",
            "CLO1: Write a well-structured essay\nCLO2: Cite sources correctly\nCLO3: Construct logical arguments",
            "Essays 50% · Midterm 20% · Final 30%",
            "EN201", lecturer, hod);
    }

    private void pub(String code, String name, String dept, int credits,
                     String desc, String clos, String assessment,
                     String prereqs, User lecturer, User hod) {
        Syllabus s = syllabusRepository.save(Syllabus.builder()
                .courseCode(code)
                .courseName(name)
                .department(dept)
                .credits(credits)
                .academicYear("2024-2025")
                .semester("1")
                .description(desc)
                .learningOutcomes(clos)
                .assessmentMethods(assessment)
                .prerequisites(prereqs)
                .status(SyllabusStatus.PUBLISHED)
                .versionNumber(1)
                .createdBy(lecturer)
                .reviewedBy(hod)
                .build());

        historyRepository.save(ApprovalHistory.builder()
                .syllabus(s).actor(lecturer).actorRole(Role.LECTURER)
                .action("SUBMITTED").comments(null).build());
        historyRepository.save(ApprovalHistory.builder()
                .syllabus(s).actor(hod).actorRole(Role.HOD)
                .action("APPROVED").comments("Approved.").build());
    }

    private User createUser(String email, String password, String firstName, String lastName,
                             Role role, String department) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .department(department)
                .isActive(true)
                .build());
    }

    private void printDemoAccounts() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              SMD — Demo Accounts                        ║");
        System.out.println("╠══════════════════╦══════════════╦═══════════════════════╣");
        System.out.println("║ Email            ║ Password     ║ Role                  ║");
        System.out.println("╠══════════════════╬══════════════╬═══════════════════════╣");
        System.out.println("║ admin@smd.edu    ║ admin123     ║ ADMIN                 ║");
        System.out.println("║ lecturer@smd.edu ║ lecturer123  ║ LECTURER              ║");
        System.out.println("║ hod@smd.edu      ║ hod123       ║ HOD                   ║");
        System.out.println("║ aa@smd.edu       ║ aa123        ║ ACADEMIC_AFFAIRS      ║");
        System.out.println("║ principal@smd.edu║ principal123 ║ PRINCIPAL             ║");
        System.out.println("║ student@smd.edu  ║ student123   ║ STUDENT               ║");
        System.out.println("╚══════════════════╩══════════════╩═══════════════════════╝");
        System.out.println("  Frontend: http://localhost:3000");
        System.out.println("  Backend:  http://localhost:8080\n");
    }
}
