package com.intercollege.service;

import com.intercollege.model.Event;
import com.intercollege.model.Notification;
import com.intercollege.model.Role;
import com.intercollege.model.User;
import com.intercollege.repository.EventRepository;
import com.intercollege.repository.NotificationRepository;
import com.intercollege.repository.UserRepository;
import com.intercollege.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service that bootstraps realistic sample data into users.json, events.json,
 * and notifications.json if the files are empty or newly initialized.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * CommandLineRunner is a Spring Boot interface with a run() method that executes
 * immediately after the Spring Application Context has loaded.
 * This is ideal for initial setup and creating sample records.
 */
@Service
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;

    public DataInitializationService(UserRepository userRepository,
                                     EventRepository eventRepository,
                                     NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        initUsersIfEmpty();
        initEventsIfEmpty();
        initNotificationsIfEmpty();
    }

    public synchronized void resetAllData() {
        initUsers(true);
        initEvents(true);
        initNotifications(true);
    }

    private void initUsersIfEmpty() {
        if (userRepository.findAll().isEmpty()) {
            initUsers(false);
        }
    }

    private void initEventsIfEmpty() {
        if (eventRepository.findAll().isEmpty()) {
            initEvents(false);
        }
    }

    private void initNotificationsIfEmpty() {
        if (notificationRepository.findAll().isEmpty()) {
            initNotifications(false);
        }
    }

    private void initUsers(boolean force) {
        List<User> users = new ArrayList<>();

        // 1. Sample Student User
        String studentSalt = PasswordUtil.generateSalt();
        String studentHash = PasswordUtil.hashPassword("student123", studentSalt);
        User student = new User("u-student-1", "Sreenanda Nair", "sreenanda", studentHash, studentSalt,
                "College of Engineering Trivandrum (CET)", Role.STUDENT,
                "Thiruvananthapuram", Arrays.asList("Technical", "Makeathon", "Workshop", "Quiz"));
        users.add(student);

        // 2. Coordinators across 5 colleges
        // College 1: CET
        String coord1Salt = PasswordUtil.generateSalt();
        String coord1Hash = PasswordUtil.hashPassword("coord123", coord1Salt);
        User coord1 = new User("u-coord-1", "Arjun Varma", "arjun_cet", coord1Hash, coord1Salt,
                "College of Engineering Trivandrum (CET)", Role.COORDINATOR, "Thiruvananthapuram", null);
        users.add(coord1);

        // College 2: MEC
        String coord2Salt = PasswordUtil.generateSalt();
        String coord2Hash = PasswordUtil.hashPassword("coord123", coord2Salt);
        User coord2 = new User("u-coord-2", "Sneha Menon", "sneha_mec", coord2Hash, coord2Salt,
                "Model Engineering College (MEC)", Role.COORDINATOR, "Kochi", null);
        users.add(coord2);

        // College 3: MBCET
        String coord3Salt = PasswordUtil.generateSalt();
        String coord3Hash = PasswordUtil.hashPassword("coord123", coord3Salt);
        User coord3 = new User("u-coord-3", "Rahul Krishnan", "rahul_mbcet", coord3Hash, coord3Salt,
                "Mar Baselios College of Engineering and Technology (MBCET)", Role.COORDINATOR, "Thiruvananthapuram", null);
        users.add(coord3);

        // College 4: GEC Thrissur
        String coord4Salt = PasswordUtil.generateSalt();
        String coord4Hash = PasswordUtil.hashPassword("coord123", coord4Salt);
        User coord4 = new User("u-coord-4", "Ananya Pillai", "ananya_gec", coord4Hash, coord4Salt,
                "Government Engineering College (GEC)", Role.COORDINATOR, "Thrissur", null);
        users.add(coord4);

        // College 5: RSET Kochi
        String coord5Salt = PasswordUtil.generateSalt();
        String coord5Hash = PasswordUtil.hashPassword("coord123", coord5Salt);
        User coord5 = new User("u-coord-5", "Karthik Joseph", "karthik_rset", coord5Hash, coord5Salt,
                "Rajagiri School of Engineering & Technology (RSET)", Role.COORDINATOR, "Kochi", null);
        users.add(coord5);

        userRepository.saveAll(users);
        System.out.println("Initialized " + users.size() + " sample users in data/users.json");
    }

    private void initEvents(boolean force) {
        LocalDate today = LocalDate.now();
        List<Event> events = new ArrayList<>();

        // Event 1: Makeathon (Soon - in 2 days)
        Event e1 = new Event();
        e1.setId("evt-1");
        e1.setName("HackKochi 2026: 36-Hour National Makeathon");
        e1.setDescription("Join over 400 collegiate developers, designers, and innovators for a 36-hour sprint tackling real-world sustainability, AI, and healthcare challenges. Huge prize pool, mentorship from industry leaders, and complimentary stay provided.");
        e1.setCollege("Model Engineering College (MEC)");
        e1.setCategory("Makeathon");
        e1.setCategories(Arrays.asList("Makeathon", "Technical", "Workshop"));
        e1.setDate(today.plusDays(2));
        e1.setStartTime(LocalTime.of(9, 0));
        e1.setEndTime(LocalTime.of(21, 0));
        e1.setVenue("Main Auditorium & Innovation Lab, MEC Thrikkakara");
        e1.setCity("Kochi");
        e1.setLatitude(10.0284);
        e1.setLongitude(76.3288);
        e1.setRegistrationLink("https://hackkochi2026.devpost.com");
        e1.setOrganizerName("Sneha Menon (MEC Innovation Club)");
        e1.setContactInfo("hackkochi@mec.ac.in | +91 98471 23456");
        e1.setPosterImage("https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800&auto=format&fit=crop&q=80");
        e1.setMaxParticipants(400);
        events.add(e1);

        // Event 2: Technical Symposium (Today)
        Event e2 = new Event();
        e2.setId("evt-2");
        e2.setName("Drishti AI & Deep Tech Symposium");
        e2.setDescription("Annual flagship tech summit exploring breakthrough advances in Generative AI, Edge Computing, and Quantum Algorithms. Features keynote speeches from Google and DeepMind researchers.");
        e2.setCollege("College of Engineering Trivandrum (CET)");
        e2.setCategory("Technical");
        e2.setCategories(Arrays.asList("Technical", "Talk Session"));
        e2.setDate(today);
        e2.setStartTime(LocalTime.of(14, 30));
        e2.setEndTime(LocalTime.of(18, 0));
        e2.setVenue("Diamond Jubilee Hall, CET Campus");
        e2.setCity("Thiruvananthapuram");
        e2.setLatitude(8.5456);
        e2.setLongitude(76.9063);
        e2.setRegistrationLink("https://drishti-cet.org/register");
        e2.setOrganizerName("Arjun Varma (CET Tech Cell)");
        e2.setContactInfo("drishti@cet.ac.in | +91 94470 12345");
        e2.setPosterImage("https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop&q=80");
        e2.setMaxParticipants(250);
        events.add(e2);

        // Event 3: Workshop (Tomorrow)
        Event e3 = new Event();
        e3.setId("evt-3");
        e3.setName("Autonomous Drone Robotics Hands-on Workshop");
        e3.setDescription("A rigorous hands-on technical workshop building autonomous flight controllers with ROS2, OpenCV obstacle avoidance, and PID tuning. Hardware kits provided for hands-on assembly in pairs.");
        e3.setCollege("Mar Baselios College of Engineering and Technology (MBCET)");
        e3.setCategory("Workshop");
        e3.setCategories(Arrays.asList("Workshop", "Technical"));
        e3.setDate(today.plusDays(1));
        e3.setStartTime(LocalTime.of(10, 0));
        e3.setEndTime(LocalTime.of(16, 30));
        e3.setVenue("Advanced Mechatronics Lab, MBCET Nalanchira");
        e3.setCity("Thiruvananthapuram");
        e3.setLatitude(8.5471);
        e3.setLongitude(76.9442);
        e3.setRegistrationLink("https://mbcet-tech.in/workshops/drones");
        e3.setOrganizerName("Rahul Krishnan (Robotics Society)");
        e3.setContactInfo("robotics@mbcet.ac.in | +91 97455 67890");
        e3.setPosterImage("https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800&auto=format&fit=crop&q=80");
        e3.setMaxParticipants(60);
        events.add(e3);

        // Event 4: Treasure Hunt (in 4 days)
        Event e4 = new Event();
        e4.setId("evt-4");
        e4.setName("The Cryptic Campus Quest: Intercollege Treasure Hunt");
        e4.setDescription("Test your wits, problem-solving speed, and physical endurance across a sprawling campus-wide cryptic trail. Clues involve cipher decryption, geolocation tags, and logic puzzles. Cash prize of INR 25,000 for winning team!");
        e4.setCollege("College of Engineering Trivandrum (CET)");
        e4.setCategory("Treasure Hunt");
        e4.setCategories(Arrays.asList("Treasure Hunt", "Cultural", "Other"));
        e4.setDate(today.plusDays(4));
        e4.setStartTime(LocalTime.of(15, 0));
        e4.setEndTime(LocalTime.of(18, 30));
        e4.setVenue("Central Quadrangle & Library Lawn, CET");
        e4.setCity("Thiruvananthapuram");
        e4.setLatitude(8.5456);
        e4.setLongitude(76.9063);
        e4.setRegistrationLink("https://drishti-cet.org/quest");
        e4.setOrganizerName("Arjun Varma (CET Student Council)");
        e4.setContactInfo("council@cet.ac.in | +91 94470 12345");
        e4.setPosterImage("https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=80");
        e4.setMaxParticipants(150);
        events.add(e4);

        // Event 5: Quiz Championship (in 9 days)
        Event e5 = new Event();
        e5.setId("evt-5");
        e5.setName("Inizio 2026: All-Kerala Open Intercollege Quiz Championship");
        e5.setDescription("Premier intercollegiate quiz spanning General Knowledge, Pop Culture, History, and Science & Technology. Hosted by renowned quizmaster Major Chandrakant. Exciting spot prizes for audience!");
        e5.setCollege("Government Engineering College (GEC)");
        e5.setCategory("Quiz");
        e5.setCategories(Arrays.asList("Quiz", "Technical"));
        e5.setDate(today.plusDays(9));
        e5.setStartTime(LocalTime.of(10, 0));
        e5.setEndTime(LocalTime.of(14, 0));
        e5.setVenue("Seminar Complex, GEC Ramavarmapuram");
        e5.setCity("Thrissur");
        e5.setLatitude(10.5534);
        e5.setLongitude(76.2223);
        e5.setRegistrationLink("https://gect.ac.in/inizio2026");
        e5.setOrganizerName("Ananya Pillai (GEC Quiz Club)");
        e5.setContactInfo("quizclub@gect.ac.in | +91 98950 45678");
        e5.setPosterImage("https://images.unsplash.com/photo-1606326608606-aa0b62935f2b?w=800&auto=format&fit=crop&q=80");
        e5.setMaxParticipants(200);
        events.add(e5);

        // Event 6: Public Speaking (in 6 days)
        Event e6 = new Event();
        e6.setId("evt-6");
        e6.setName("Collegiate Parliamentary Debate Clash");
        e6.setDescription("Asian Parliamentary style debate tournament. Compete against the sharpest student orators on pressing international relations, climate policy, and technological ethics topics.");
        e6.setCollege("Rajagiri School of Engineering & Technology (RSET)");
        e6.setCategory("Public Speaking");
        e6.setCategories(Arrays.asList("Public Speaking", "Cultural"));
        e6.setDate(today.plusDays(6));
        e6.setStartTime(LocalTime.of(13, 30));
        e6.setEndTime(LocalTime.of(17, 30));
        e6.setVenue("Chavara Hall, RSET Rajagiri Valley");
        e6.setCity("Kochi");
        e6.setLatitude(9.9934);
        e6.setLongitude(76.3582);
        e6.setRegistrationLink("https://rajagiritech.ac.in/debate-clash");
        e6.setOrganizerName("Karthik Joseph (Debating Society)");
        e6.setContactInfo("debate@rajagiritech.edu.in | +91 98460 78912");
        e6.setPosterImage("https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800&auto=format&fit=crop&q=80");
        e6.setMaxParticipants(80);
        events.add(e6);

        // Event 7: Cultural Fest (in 12 days)
        Event e7 = new Event();
        e7.setId("evt-7");
        e7.setName("Dhwani National Inter-Collegiate Cultural Fest");
        e7.setDescription("Kerala's biggest collegiate cultural spectacle featuring synchronized Western choreography, classical music recitals, street theatre, fashion show, and pro-night musical concerts!");
        e7.setCollege("College of Engineering Trivandrum (CET)");
        e7.setCategory("Cultural");
        e7.setCategories(Arrays.asList("Cultural", "Other"));
        e7.setDate(today.plusDays(12));
        e7.setStartTime(LocalTime.of(9, 30));
        e7.setEndTime(LocalTime.of(22, 0));
        e7.setVenue("Open Air Theatre (OAT) & Diamond Jubilee Stage, CET");
        e7.setCity("Thiruvananthapuram");
        e7.setLatitude(8.5456);
        e7.setLongitude(76.9063);
        e7.setRegistrationLink("https://dhwanifest.in");
        e7.setOrganizerName("Arjun Varma (Dhwani General Convenor)");
        e7.setContactInfo("info@dhwanifest.in | +91 94470 12345");
        e7.setPosterImage("https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80");
        e7.setMaxParticipants(3000);
        events.add(e7);

        // Event 8: Talk Session (Ongoing / Scheduled Today)
        Event e8 = new Event();
        e8.setId("evt-8");
        e8.setName("Tech Leaders Keynote: The Quantum Future");
        e8.setDescription("A fireside dialogue with prominent quantum researchers and venture capitalists discussing how quantum key distribution and quantum algorithms will reshape cyber defense.");
        e8.setCollege("Model Engineering College (MEC)");
        e8.setCategory("Talk Session");
        e8.setCategories(Arrays.asList("Talk Session", "Technical"));
        e8.setDate(today);
        e8.setStartTime(LocalTime.of(11, 0));
        e8.setEndTime(LocalTime.of(13, 30));
        e8.setVenue("CCF Auditorium & Live Stream, MEC");
        e8.setCity("Kochi");
        e8.setLatitude(10.0284);
        e8.setLongitude(76.3288);
        e8.setRegistrationLink("https://mec.ac.in/talks/quantum");
        e8.setOrganizerName("Sneha Menon (MEC IEDC)");
        e8.setContactInfo("iedc@mec.ac.in | +91 98471 23456");
        e8.setPosterImage("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop&q=80");
        e8.setMaxParticipants(180);
        events.add(e8);

        // Event 9: Sports Tournament (in 7 days)
        Event e9 = new Event();
        e9.setId("evt-9");
        e9.setName("Apex Futsal League: South India Inter-College Cup");
        e9.setDescription("High-octane 5-a-side futsal tournament under floodlights. 32 university squads battling for glory, trophy, and cash prizes. FIFA accredited referees supervising.");
        e9.setCollege("Government Engineering College (GEC)");
        e9.setCategory("Sports");
        e9.setCategories(Arrays.asList("Sports", "Other"));
        e9.setDate(today.plusDays(7));
        e9.setStartTime(LocalTime.of(8, 0));
        e9.setEndTime(LocalTime.of(18, 0));
        e9.setVenue("GEC Sports Arena & Floodlit Turf, Thrissur");
        e9.setCity("Thrissur");
        e9.setLatitude(10.5534);
        e9.setLongitude(76.2223);
        e9.setRegistrationLink("https://gect.ac.in/sports/futsal26");
        e9.setOrganizerName("Ananya Pillai (Physical Education Council)");
        e9.setContactInfo("sports@gect.ac.in | +91 98950 45678");
        e9.setPosterImage("https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&auto=format&fit=crop&q=80");
        e9.setMaxParticipants(320);
        events.add(e9);

        // Event 10: Other - CleanTech Expo (in 15 days)
        Event e10 = new Event();
        e10.setId("evt-10");
        e10.setName("CleanTech & Green Energy Innovation Expo");
        e10.setDescription("Showcase your working prototypes for renewable energy, zero-emission transport, and smart waste management. Angel investors and government grant committees will be evaluating demos.");
        e10.setCollege("Mar Baselios College of Engineering and Technology (MBCET)");
        e10.setCategory("Other");
        e10.setCategories(Arrays.asList("Other", "Technical", "Makeathon"));
        e10.setDate(today.plusDays(15));
        e10.setStartTime(LocalTime.of(10, 0));
        e10.setEndTime(LocalTime.of(16, 0));
        e10.setVenue("Innovation & Incubation Centre, MBCET");
        e10.setCity("Thiruvananthapuram");
        e10.setLatitude(8.5471);
        e10.setLongitude(76.9442);
        e10.setRegistrationLink("https://mbcet-greentech.org");
        e10.setOrganizerName("Rahul Krishnan (Green Tech Cell)");
        e10.setContactInfo("greentech@mbcet.ac.in | +91 97455 67890");
        e10.setPosterImage("https://images.unsplash.com/photo-1497435334941-8c899ee9e8e9?w=800&auto=format&fit=crop&q=80");
        e10.setMaxParticipants(120);
        events.add(e10);

        // Event 11: Cyber Security CTF (in 3 days)
        Event e11 = new Event();
        e11.setId("evt-11");
        e11.setName("CyberShield: Ethical Hacking & 24hr CTF Challenge");
        e11.setDescription("Jeopardy style Capture The Flag challenge covering Web Exploitation, Reverse Engineering, Binary Analysis, Cryptography, and Forensics. Ideal for cybersecurity enthusiasts!");
        e11.setCollege("Rajagiri School of Engineering & Technology (RSET)");
        e11.setCategory("Technical");
        e11.setCategories(Arrays.asList("Technical", "Makeathon", "Workshop"));
        e11.setDate(today.plusDays(3));
        e11.setStartTime(LocalTime.of(10, 0));
        e11.setEndTime(LocalTime.of(18, 0));
        e11.setVenue("Cyber Security Centre of Excellence, RSET");
        e11.setCity("Kochi");
        e11.setLatitude(9.9934);
        e11.setLongitude(76.3582);
        e11.setRegistrationLink("https://rajagiritech.ac.in/ctf-2026");
        e11.setOrganizerName("Karthik Joseph (Cyber Cell)");
        e11.setContactInfo("cybercell@rajagiritech.edu.in | +91 98460 78912");
        e11.setPosterImage("https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800&auto=format&fit=crop&q=80");
        e11.setMaxParticipants(150);
        events.add(e11);

        // Event 12: Completed Event (5 days ago)
        Event e12 = new Event();
        e12.setId("evt-12");
        e12.setName("Spring CodeStorm: Competitive Programming Sprint");
        e12.setDescription("ICPC-style algorithms contest on dynamic programming, graph theory, and string algorithms. Solutions judged real-time on our custom online judge platform.");
        e12.setCollege("College of Engineering Trivandrum (CET)");
        e12.setCategory("Technical");
        e12.setCategories(Arrays.asList("Technical", "Quiz"));
        e12.setDate(today.minusDays(5));
        e12.setStartTime(LocalTime.of(10, 0));
        e12.setEndTime(LocalTime.of(14, 0));
        e12.setVenue("Department of Computer Applications, CET");
        e12.setCity("Thiruvananthapuram");
        e12.setLatitude(8.5456);
        e12.setLongitude(76.9063);
        e12.setRegistrationLink("https://drishti-cet.org/codestorm-archive");
        e12.setOrganizerName("Arjun Varma (ACM Student Chapter)");
        e12.setContactInfo("acm@cet.ac.in | +91 94470 12345");
        e12.setPosterImage("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=80");
        e12.setMaxParticipants(100);
        events.add(e12);

        // Event 13: Battle of the Bands (in 2 days)
        Event e13 = new Event();
        e13.setId("evt-13");
        e13.setName("Rhythm & Beats: Intercollege Battle of the Bands");
        e13.setDescription("Live electric showdown between the top rock, indie, and fusion college music bands across South India. High-end stage sound, celebrity judges, and record label scouts present!");
        e13.setCollege("Mar Baselios College of Engineering and Technology (MBCET)");
        e13.setCategory("Cultural");
        e13.setCategories(Arrays.asList("Cultural", "Other"));
        e13.setDate(today.plusDays(2));
        e13.setStartTime(LocalTime.of(17, 0));
        e13.setEndTime(LocalTime.of(21, 30));
        e13.setVenue("MBCET Open Amphitheatre");
        e13.setCity("Thiruvananthapuram");
        e13.setLatitude(8.5471);
        e13.setLongitude(76.9442);
        e13.setRegistrationLink("https://mbcet-cultural.org/bands");
        e13.setOrganizerName("Rahul Krishnan (Music Club)");
        e13.setContactInfo("music@mbcet.ac.in | +91 97455 67890");
        e13.setPosterImage("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80");
        e13.setMaxParticipants(500);
        events.add(e13);

        eventRepository.saveAll(events);
        System.out.println("Initialized " + events.size() + " sample events in data/events.json");
    }

    private void initNotifications(boolean force) {
        List<Notification> notifs = new ArrayList<>();

        notifs.add(new Notification("notif-1", "ALL", "HackKochi 2026 is happening in 2 days!",
                "Check out the 36-hour national makeathon at Model Engineering College.", "URGENCY", "evt-1"));

        notifs.add(new Notification("notif-2", "ALL", "Drishti AI & Deep Tech Symposium is today!",
                "Starts at 2:30 PM at Diamond Jubilee Hall, CET.", "URGENCY", "evt-2"));

        notifs.add(new Notification("notif-3", "u-student-1", "Autonomous Drone Workshop matches your interests!",
                "Hands-on workshop at MBCET Nalanchira matches your Technical & Workshop preferences.", "INTEREST_MATCH", "evt-3"));

        notifs.add(new Notification("notif-4", "u-student-1", "New event added near your location in Thiruvananthapuram",
                "The Cryptic Campus Quest Treasure Hunt is now open for registration at CET.", "LOCATION_MATCH", "evt-4"));

        notificationRepository.saveAll(notifs);
        System.out.println("Initialized " + notifs.size() + " sample notifications in data/notifications.json");
    }
}
