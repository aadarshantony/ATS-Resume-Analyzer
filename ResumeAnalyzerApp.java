import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

// --------------------------- UI Components ---------------------------
class ModernButton extends JButton {
    private Color backgroundColor = new Color(79, 70, 229);
    private Color hoverColor = new Color(99, 90, 249);
    private Color pressedColor = new Color(67, 56, 202);
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text) {
        super(text);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color currentColor = isPressed ? pressedColor : (isHovered ? hoverColor : backgroundColor);
        g2.setColor(currentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.dispose();
        super.paintComponent(g);
    }
}

class ModernPanel extends JPanel {
    private Color backgroundColor = Color.WHITE;
    private int cornerRadius = 16;
    private boolean hasShadow = true;

    public ModernPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public ModernPanel(Color bg, int radius) {
        this();
        this.backgroundColor = bg;
        this.cornerRadius = radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hasShadow) {
            g2.setColor(new Color(0, 0, 0, 10));
            for (int i = 0; i < 4; i++) {
                g2.fill(new RoundRectangle2D.Float(i, i, getWidth() - i * 2, getHeight() - i * 2, cornerRadius + i,
                        cornerRadius + i));
            }
        }
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Float(hasShadow ? 5 : 0, hasShadow ? 5 : 0, getWidth() - (hasShadow ? 10 : 0),
                getHeight() - (hasShadow ? 10 : 0), cornerRadius, cornerRadius));
        g2.dispose();
    }
}

class ScoreCircle extends JComponent {
    private int score = 0;
    private Color primaryColor = new Color(16, 185, 129);
    private Color backgroundColor = new Color(229, 231, 235);
    private Font scoreFont = new Font("Segoe UI", Font.BOLD, 36);
    private Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);

    public ScoreCircle() {
        setPreferredSize(new Dimension(150, 150));
    }

    public void setScore(int s) {
        score = Math.max(0, Math.min(100, s));
        if (score >= 85)
            primaryColor = new Color(16, 185, 129);
        else if (score >= 70)
            primaryColor = new Color(245, 158, 11);
        else
            primaryColor = new Color(239, 68, 68);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int diameter = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - diameter) / 2, y = (getHeight() - diameter) / 2;
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(backgroundColor);
        g2.drawOval(x, y, diameter, diameter);
        g2.setColor(primaryColor);
        int angle = (int) (360 * score / 100.0);
        g2.drawArc(x, y, diameter, diameter, 90, -angle);
        g2.setFont(scoreFont);
        g2.setColor(new Color(31, 41, 55));
        String scoreText = String.valueOf(score);
        FontMetrics fm = g2.getFontMetrics();
        int tx = getWidth() / 2 - fm.stringWidth(scoreText) / 2;
        int ty = getHeight() / 2 + fm.getAscent() / 2 - 6;
        g2.drawString(scoreText, tx, ty);
        g2.setFont(labelFont);
        g2.setColor(new Color(107, 114, 128));
        String label = "out of 100";
        FontMetrics lfm = g2.getFontMetrics();
        g2.drawString(label, getWidth() / 2 - lfm.stringWidth(label) / 2, ty + 28);
        g2.dispose();
    }
}

class AnalysisCard extends ModernPanel {
    private JLabel titleLabel, scoreLabel;
    private JProgressBar progressBar;
    private JTextArea detailsArea;

    public AnalysisCard(String title) {
        super(Color.WHITE, 12);
        setupComponents(title);
    }

    private void setupComponents(String title) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(31, 41, 55));

        scoreLabel = new JLabel("0/100");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreLabel.setForeground(new Color(79, 70, 229));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.WEST);
        header.add(scoreLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setStringPainted(false);
        progressBar.setBorder(null);
        progressBar.setBackground(new Color(229, 231, 235));

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(header, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);
        add(detailsArea, BorderLayout.SOUTH);
    }

    public void updateCard(int score, String details) {
        scoreLabel.setText(score + "/100");
        progressBar.setValue(score);
        detailsArea.setText(details);
        if (score >= 85)
            progressBar.setForeground(new Color(16, 185, 129));
        else if (score >= 70)
            progressBar.setForeground(new Color(245, 158, 11));
        else
            progressBar.setForeground(new Color(239, 68, 68));
        revalidate();
        repaint();
    }
}

// --------------------------- Enhanced NLP Analysis ---------------------------
class EnhancedNLPProcessor {
    // Comprehensive skill sets with variations
    public static final Set<String> TECHNICAL_SKILLS = new HashSet<>(Arrays.asList(
            "java", "python", "javascript", "typescript", "c++", "c#", "ruby", "php", "swift", "kotlin",
            "go", "rust", "scala", "r", "matlab", "sql", "nosql", "pl/sql",
            "react", "angular", "vue", "svelte", "next.js", "nuxt", "gatsby",
            "node.js", "express", "django", "flask", "spring", "spring boot", "hibernate",
            "asp.net", ".net", "laravel", "ruby on rails", "fastapi",
            "html", "html5", "css", "css3", "sass", "less", "tailwind", "bootstrap", "material-ui",
            "mysql", "postgresql", "mongodb", "redis", "cassandra", "oracle", "sql server",
            "dynamodb", "firebase", "supabase",
            "docker", "kubernetes", "jenkins", "gitlab ci", "github actions", "travis ci",
            "terraform", "ansible", "puppet", "chef",
            "aws", "azure", "gcp", "heroku", "vercel", "netlify", "digital ocean",
            "git", "svn", "mercurial", "github", "gitlab", "bitbucket",
            "rest", "graphql", "grpc", "soap", "api", "microservices", "monolith",
            "agile", "scrum", "kanban", "jira", "confluence",
            "junit", "jest", "mocha", "pytest", "selenium", "cypress", "testng",
            "machine learning", "deep learning", "ai", "neural networks", "tensorflow", "pytorch", "keras",
            "data science", "pandas", "numpy", "scikit-learn", "matplotlib",
            "blockchain", "ethereum", "solidity", "web3",
            "devops", "ci/cd", "linux", "unix", "bash", "powershell",
            "apache", "nginx", "tomcat", "iis",
            "elasticsearch", "kafka", "rabbitmq", "redis",
            "oauth", "jwt", "saml", "sso",
            "webpack", "vite", "rollup", "babel",
            "redux", "mobx", "vuex", "context api",
            "responsive design", "mobile-first", "progressive web app", "pwa"));

    public static final Set<String> SOFT_SKILLS = new HashSet<>(Arrays.asList(
            "leadership", "communication", "teamwork", "collaboration", "problem solving",
            "analytical thinking", "critical thinking", "decision making", "time management",
            "project management", "people management", "stakeholder management",
            "presentation", "public speaking", "negotiation", "conflict resolution",
            "creativity", "innovation", "adaptability", "flexibility", "resilience",
            "attention to detail", "organization", "multitasking", "prioritization",
            "customer service", "client relations", "interpersonal skills",
            "mentoring", "coaching", "training", "strategic thinking", "planning"));

    public static final Set<String> ACTION_VERBS = new HashSet<>(Arrays.asList(
            "achieved", "managed", "developed", "created", "implemented", "designed",
            "led", "supervised", "coordinated", "executed", "delivered", "optimized",
            "spearheaded", "enhanced", "streamlined", "monitored", "architected",
            "built", "analyzed", "reduced", "improved", "launched", "established",
            "initiated", "increased", "decreased", "generated", "resolved", "transformed",
            "automated", "collaborated", "facilitated", "negotiated", "presented",
            "trained", "mentored", "authored", "published", "engineered", "integrated",
            "migrated", "scaled", "secured", "tested", "debugged", "deployed",
            "maintained", "documented", "researched", "evaluated", "assessed"));

    public static final Set<String> SECTION_HEADERS = new HashSet<>(Arrays.asList(
            "professional summary", "summary", "profile", "objective", "career objective",
            "skills", "technical skills", "core competencies", "expertise", "proficiencies",
            "experience", "work experience", "professional experience", "work history", "employment",
            "projects", "portfolio", "key projects", "notable projects",
            "education", "academic background", "qualifications", "academic qualifications",
            "certifications", "certificates", "licenses", "professional development",
            "awards", "honors", "achievements", "accomplishments",
            "volunteering", "volunteer experience", "community involvement",
            "publications", "research", "papers",
            "references", "contact", "contact information"));

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{2,5}\\)?[\\s.-]?)?\\d{3,4}[\\s.-]?\\d{4}\\b");
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?linkedin\\.com/in/([A-Za-z0-9-_%]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?github\\.com/([A-Za-z0-9-_%]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(?:/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BULLET_PATTERN = Pattern.compile("^\\s*[•●■▪▸►⦿⦾∙◦‣⁃-]\\s+", Pattern.MULTILINE);
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\.?\\s+\\d{4}\\b|\\b\\d{4}\\s*[-–—]\\s*(?:\\d{4}|Present|Current)\\b",
            Pattern.CASE_INSENSITIVE);

    private boolean containsWord(String text, String word) {
        String pattern = "\\b" + Pattern.quote(word) + "\\b";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    public AnalysisResult analyzeResume(String originalContent) {
        String content = (originalContent == null || originalContent.isBlank()) ? "" : originalContent.trim();
        String textLower = content.toLowerCase();

        Map<String, String> contact = extractContact(content);

        int skillsScore = scoreSkills(textLower, content);
        int grammarScore = scoreGrammarAndWriting(content, textLower);
        int atsScore = scoreATS(content, textLower, contact);
        int formatScore = scoreFormat(content, textLower);

        // Weighted average: ATS 30%, Skills 30%, Grammar 20%, Format 20%
        int overall = (int) Math.round(atsScore * 0.30 + skillsScore * 0.30 + grammarScore * 0.20 + formatScore * 0.20);

        return new AnalysisResult(overall, atsScore, skillsScore, grammarScore, formatScore, content, contact);
    }

    private Map<String, String> extractContact(String content) {
        Map<String, String> out = new HashMap<>();

        Matcher m = EMAIL_PATTERN.matcher(content);
        if (m.find())
            out.put("email", m.group());

        m = LINKEDIN_PATTERN.matcher(content);
        if (m.find())
            out.put("linkedin", "https://linkedin.com/in/" + m.group(1));

        m = GITHUB_PATTERN.matcher(content);
        if (m.find())
            out.put("github", "https://github.com/" + m.group(1));

        m = PHONE_PATTERN.matcher(content);
        if (m.find())
            out.put("phone", m.group().trim());

        // Extract portfolio/personal website
        m = URL_PATTERN.matcher(content);
        while (m.find()) {
            String url = m.group();
            if (!url.contains("linkedin.com") && !url.contains("github.com")) {
                out.put("website", url);
                break;
            }
        }

        return out;
    }

    private int scoreSkills(String textLower, String originalContent) {
        Set<String> foundTechSkills = new HashSet<>();
        Set<String> foundSoftSkills = new HashSet<>();

        // Find technical skills with word boundaries
        for (String skill : TECHNICAL_SKILLS) {
            if (containsWord(textLower, skill)) {
                foundTechSkills.add(skill);
            }
        }

        // Find soft skills
        for (String skill : SOFT_SKILLS) {
            if (containsWord(textLower, skill)) {
                foundSoftSkills.add(skill);
            }
        }

        int techCount = foundTechSkills.size();
        int softCount = foundSoftSkills.size();

        // Base score: tech skills are weighted more heavily
        int baseScore = Math.min(70, (techCount * 4) + (softCount * 2));

        // Bonus points for skill diversity
        if (techCount >= 10 && softCount >= 5)
            baseScore += 15;
        else if (techCount >= 7 && softCount >= 3)
            baseScore += 10;
        else if (techCount >= 5 && softCount >= 2)
            baseScore += 5;

        // Check for dedicated skills section
        boolean hasSkillsSection = SECTION_HEADERS.stream()
                .filter(h -> h.contains("skill") || h.contains("competenc") || h.contains("expertise"))
                .anyMatch(h -> textLower.contains(h));

        if (hasSkillsSection)
            baseScore += 10;

        // Bonus for modern/relevant skills
        Set<String> modernSkills = Set.of("docker", "kubernetes", "aws", "azure", "react",
                "node.js", "python", "machine learning", "devops", "microservices");
        long modernCount = modernSkills.stream().filter(s -> containsWord(textLower, s)).count();
        baseScore += (int) (modernCount * 2);

        return Math.max(0, Math.min(100, baseScore));
    }

    private int scoreGrammarAndWriting(String content, String textLower) {
        if (content.isBlank())
            return 10;

        int score = 40; // Base score for having content

        // Check for action verbs (strong indicator of professional writing)
        Set<String> foundVerbs = new HashSet<>();
        for (String verb : ACTION_VERBS) {
            if (containsWord(textLower, verb)) {
                foundVerbs.add(verb);
            }
        }

        int verbCount = foundVerbs.size();
        score += Math.min(30, verbCount * 2); // Up to 30 points for action verbs

        // Check for quantifiable achievements (numbers/percentages)
        Pattern numberPattern = Pattern.compile("\\b\\d+%|\\$\\d+|\\d+\\+|\\d{2,}\\b");
        Matcher numMatcher = numberPattern.matcher(content);
        int quantifiableCount = 0;
        while (numMatcher.find() && quantifiableCount < 10)
            quantifiableCount++;
        score += Math.min(15, quantifiableCount * 2);

        // Penalty for common issues
        if (content.contains("  "))
            score -= 3; // Double spaces
        if (Pattern.compile("[,.!?]{2,}").matcher(content).find())
            score -= 3; // Multiple punctuation
        if (Pattern.compile("\\b(I|i) ").matcher(content).find())
            score -= 5; // First person (should avoid)

        // Check sentence structure (look for bullet points)
        boolean hasBullets = BULLET_PATTERN.matcher(content).find();
        if (hasBullets)
            score += 10;

        // Bonus for professional tone indicators
        if (textLower.contains("responsible for") || textLower.contains("led team") ||
                textLower.contains("managed") || textLower.contains("developed")) {
            score += 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    private int scoreATS(String content, String textLower, Map<String, String> contact) {
        if (content.isBlank())
            return 5;

        int score = 20; // Base score

        // Contact information (critical for ATS - 25 points)
        if (contact.containsKey("email"))
            score += 8;
        if (contact.containsKey("phone"))
            score += 8;
        if (contact.containsKey("linkedin"))
            score += 6;
        if (contact.containsKey("github") || contact.containsKey("website"))
            score += 3;

        // Section headers (critical for ATS parsing - 30 points)
        Set<String> foundSections = new HashSet<>();
        for (String header : SECTION_HEADERS) {
            if (textLower.contains(header)) {
                foundSections.add(header);
            }
        }

        int sectionScore = Math.min(30, foundSections.size() * 5);
        score += sectionScore;

        // Standard keywords that ATS systems look for (20 points)
        String[] atsKeywords = { "experience", "education", "skills", "work", "project",
                "bachelor", "master", "university", "degree", "certification" };
        long keywordCount = Arrays.stream(atsKeywords)
                .filter(k -> containsWord(textLower, k))
                .count();
        score += Math.min(20, (int) (keywordCount * 3));

        // Date formatting (important for ATS date parsing - 10 points)
        Matcher dateMatcher = DATE_PATTERN.matcher(content);
        int dateCount = 0;
        while (dateMatcher.find() && dateCount < 5)
            dateCount++;
        if (dateCount >= 2)
            score += 10;
        else if (dateCount == 1)
            score += 5;

        // Penalties for ATS-unfriendly elements
        if (content.contains("©") || content.contains("®") || content.contains("™"))
            score -= 5;
        if (Pattern.compile("[^\\x00-\\x7F]").matcher(content).find())
            score -= 3; // Non-ASCII chars

        // Bonus for clean formatting
        boolean hasCleanBullets = BULLET_PATTERN.matcher(content).find();
        if (hasCleanBullets)
            score += 5;

        return Math.max(0, Math.min(100, score));
    }

    private int scoreFormat(String content, String textLower) {
        if (content.isBlank())
            return 5;

        int score = 15; // Base score

        // Word count analysis (20 points)
        String[] words = content.trim().split("\\s+");
        int wordCount = words.length;

        if (wordCount >= 300 && wordCount <= 800)
            score += 20;
        else if (wordCount >= 200 && wordCount <= 1000)
            score += 15;
        else if (wordCount >= 150 && wordCount <= 1200)
            score += 10;
        else if (wordCount < 100)
            score -= 10;
        else if (wordCount > 1500)
            score -= 5;

        // Bullet points usage (20 points)
        Matcher bulletMatcher = BULLET_PATTERN.matcher(content);
        int bulletCount = 0;
        while (bulletMatcher.find() && bulletCount < 50)
            bulletCount++;

        if (bulletCount >= 8 && bulletCount <= 30)
            score += 20;
        else if (bulletCount >= 5)
            score += 15;
        else if (bulletCount >= 3)
            score += 10;

        // Section organization (20 points)
        Set<String> foundSections = new HashSet<>();
        for (String header : SECTION_HEADERS) {
            if (textLower.contains(header))
                foundSections.add(header);
        }

        if (foundSections.size() >= 5)
            score += 20;
        else if (foundSections.size() >= 4)
            score += 15;
        else if (foundSections.size() >= 3)
            score += 10;
        else if (foundSections.size() >= 2)
            score += 5;

        // Whitespace and readability (15 points)
        String[] lines = content.split("\n");
        int nonEmptyLines = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty())
                nonEmptyLines++;
        }

        double avgLineLength = wordCount / (double) Math.max(1, nonEmptyLines);
        if (avgLineLength >= 5 && avgLineLength <= 15)
            score += 10;
        else if (avgLineLength >= 3 && avgLineLength <= 20)
            score += 5;

        // Check for excessive spacing issues
        if (!content.contains("\n\n\n\n"))
            score += 5;

        // Dates presence (good formatting indicator - 10 points)
        if (DATE_PATTERN.matcher(content).find())
            score += 10;

        // Consistent formatting bonus (10 points)
        boolean hasConsistentBullets = bulletCount > 0;
        boolean hasProperSections = foundSections.size() >= 3;
        boolean hasGoodLength = wordCount >= 250 && wordCount <= 900;

        if (hasConsistentBullets && hasProperSections && hasGoodLength)
            score += 10;

        return Math.max(0, Math.min(100, score));
    }
}

class AnalysisResult {
    public final int overallScore, atsScore, skillsScore, grammarScore, formatScore;
    public final String content;
    public final Map<String, String> contactInfo;

    public AnalysisResult(int overall, int ats, int skills, int grammar, int format,
            String content, Map<String, String> contactInfo) {
        this.overallScore = overall;
        this.atsScore = ats;
        this.skillsScore = skills;
        this.grammarScore = grammar;
        this.formatScore = format;
        this.content = content;
        this.contactInfo = contactInfo;
    }
}

// --------------------------- Text Extraction ---------------------------
class ResumeTextExtractor {
    private static final Tika tika = new Tika();

    public static String extractText(File f) {
        try (InputStream is = Files.newInputStream(f.toPath())) {
            String text = tika.parseToString(is);
            if (text == null || text.isBlank()) {
                // Fallback for plain text
                if (f.getName().toLowerCase().endsWith(".txt")) {
                    return Files.readString(f.toPath());
                }
                return "";
            }
            return text.trim();
        } catch (IOException | TikaException e) {
            System.err.println("Extraction failed: " + e.getMessage());
            if (f.getName().toLowerCase().endsWith(".txt")) {
                try {
                    return Files.readString(f.toPath());
                } catch (IOException ex) {
                    return "";
                }
            }
            return "";
        }
    }
}

// --------------------------- Main Application ---------------------------
public class ResumeAnalyzerApp extends JFrame {
    private JPanel mainPanel, uploadPanel, resultsPanel;
    private ModernButton uploadButton, analyzeButton;
    private JLabel fileLabel, uploadIconLabel;
    private ScoreCircle scoreCircle;
    private AnalysisCard atsCard, skillsCard, grammarCard, formatCard;
    private JTextArea suggestionsArea;
    private File selectedFile;
    private EnhancedNLPProcessor nlp;

    public ResumeAnalyzerApp() {
        nlp = new EnhancedNLPProcessor();
        initGUI();
    }

    private void initGUI() {
        setTitle("Smart Resume Analyzer - ATS Score Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        createComponents();
        layoutComponents();
        setupListeners();
        setVisible(true);
    }

    private void createComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(102, 126, 234),
                        getWidth(), getHeight(), new Color(118, 75, 162));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout());

        uploadPanel = createUploadPanel();
        resultsPanel = createResultsPanel();
    }

    private JPanel createUploadPanel() {
        ModernPanel panel = new ModernPanel(new Color(248, 250, 252), 16);
        panel.setLayout(new BorderLayout());

        ModernPanel uploadArea = new ModernPanel(Color.WHITE, 12);
        uploadArea.setLayout(new BorderLayout());
        uploadArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(203, 213, 225), 2, 5, 5, false),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        uploadArea.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel uploadContent = new JPanel();
        uploadContent.setOpaque(false);
        uploadContent.setLayout(new BoxLayout(uploadContent, BoxLayout.Y_AXIS));

        uploadIconLabel = new JLabel("📄");
        uploadIconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        uploadIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uploadText = new JLabel("Drop your resume here or click to browse");
        uploadText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        uploadText.setForeground(new Color(71, 85, 105));
        uploadText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uploadSubtext = new JLabel("Supports PDF, DOC, DOCX, TXT");
        uploadSubtext.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadSubtext.setForeground(new Color(148, 163, 184));
        uploadSubtext.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadContent.add(uploadIconLabel);
        uploadContent.add(Box.createVerticalStrut(20));
        uploadContent.add(uploadText);
        uploadContent.add(Box.createVerticalStrut(10));
        uploadContent.add(uploadSubtext);

        uploadArea.add(uploadContent, BorderLayout.CENTER);

        JPanel fileInfoPanel = new JPanel(new BorderLayout());
        fileInfoPanel.setOpaque(false);
        fileInfoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileLabel.setForeground(new Color(107, 114, 128));
        fileInfoPanel.add(fileLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        buttonPanel.setOpaque(false);
        uploadButton = new ModernButton("Choose File");
        analyzeButton = new ModernButton("Analyze Resume");
        analyzeButton.setEnabled(false);
        buttonPanel.add(uploadButton);
        buttonPanel.add(analyzeButton);

        panel.add(uploadArea, BorderLayout.CENTER);
        panel.add(fileInfoPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.PAGE_END);

        // Drag-and-drop support
        uploadArea.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }

            public boolean importData(TransferSupport support) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    if (files != null && !files.isEmpty()) {
                        setSelectedFile(files.get(0));
                        return true;
                    }
                } catch (Exception ex) {
                    /* ignore */ }
                return false;
            }
        });

        uploadArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseFile();
            }
        });

        return panel;
    }

    private JPanel createResultsPanel() {
        ModernPanel panel = new ModernPanel(Color.WHITE, 16);
        panel.setLayout(new BorderLayout());

        JPanel initial = new JPanel();
        initial.setOpaque(false);
        initial.setLayout(new BoxLayout(initial, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🎯");
        icon.setFont(new Font("Arial", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ready = new JLabel("Ready to Analyze");
        ready.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ready.setForeground(new Color(107, 114, 128));
        ready.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instr = new JLabel("Upload your resume to get started");
        instr.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instr.setForeground(new Color(156, 163, 175));
        instr.setAlignmentX(Component.CENTER_ALIGNMENT);

        initial.add(Box.createVerticalGlue());
        initial.add(icon);
        initial.add(Box.createVerticalStrut(20));
        initial.add(ready);
        initial.add(Box.createVerticalStrut(10));
        initial.add(instr);
        initial.add(Box.createVerticalGlue());

        panel.add(initial, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel scoreSection = new JPanel();
        scoreSection.setOpaque(false);
        scoreSection.setLayout(new BoxLayout(scoreSection, BoxLayout.Y_AXIS));
        scoreSection.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel scoreTitle = new JLabel("Job Readiness Score");
        scoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreTitle.setForeground(new Color(31, 41, 55));
        scoreTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreCircle = new ScoreCircle();
        scoreCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreSection.add(scoreTitle);
        scoreSection.add(Box.createVerticalStrut(20));
        scoreSection.add(scoreCircle);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        atsCard = new AnalysisCard("ATS Compatibility");
        skillsCard = new AnalysisCard("Skills Analysis");
        grammarCard = new AnalysisCard("Grammar & Writing");
        formatCard = new AnalysisCard("Format & Structure");

        cardsPanel.add(atsCard);
        cardsPanel.add(skillsCard);
        cardsPanel.add(grammarCard);
        cardsPanel.add(formatCard);

        ModernPanel suggestionsPanel = new ModernPanel(new Color(254, 243, 199), 12);
        suggestionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(16, 16, 16, 16),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(245, 158, 11), 1),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12))));

        JLabel suggestionsTitle = new JLabel("💡 Improvement Suggestions");
        suggestionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        suggestionsTitle.setForeground(new Color(146, 64, 14));

        suggestionsArea = new JTextArea(6, 0);
        suggestionsArea.setEditable(false);
        suggestionsArea.setLineWrap(true);
        suggestionsArea.setWrapStyleWord(true);
        suggestionsArea.setBackground(new Color(254, 243, 199));
        suggestionsArea.setBorder(null);

        suggestionsPanel.setLayout(new BorderLayout());
        suggestionsPanel.add(suggestionsTitle, BorderLayout.NORTH);
        suggestionsPanel.add(suggestionsArea, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(scoreSection, BorderLayout.NORTH);
        content.add(cardsPanel, BorderLayout.CENTER);
        content.add(suggestionsPanel, BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        mainPanel.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Smart Resume Analyzer", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Upload your resume and get instant ATS & job-readiness feedback",
                SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(255, 255, 255, 200));

        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        uploadPanel.setOpaque(false);
        resultsPanel.setOpaque(false);

        contentPanel.add(uploadPanel);
        contentPanel.add(resultsPanel);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        uploadButton.addActionListener(e -> chooseFile());
        analyzeButton.addActionListener(e -> analyzeResume());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Resume files (*.pdf, *.doc, *.docx, *.txt)",
                "pdf", "doc", "docx", "txt"));
        int r = chooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            setSelectedFile(chooser.getSelectedFile());
        }
    }

    private void setSelectedFile(File f) {
        selectedFile = f;
        fileLabel.setText("Selected: " + f.getName());
        analyzeButton.setEnabled(true);
        uploadIconLabel.setText("✅");
    }

    private void analyzeResume() {
        if (selectedFile == null)
            return;

        analyzeButton.setText("Analyzing...");
        analyzeButton.setEnabled(false);

        SwingWorker<AnalysisResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalysisResult doInBackground() {
                String text = ResumeTextExtractor.extractText(selectedFile);
                if (text == null || text.isBlank()) {
                    try {
                        text = Files.readString(selectedFile.toPath());
                    } catch (IOException ignored) {
                        text = "";
                    }
                }
                return nlp.analyzeResume(text);
            }

            @Override
            protected void done() {
                try {
                    AnalysisResult res = get();
                    displayResults(res);
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                            ResumeAnalyzerApp.this,
                            "Error analyzing file: " + ex.getMessage(),
                            "Analysis Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    analyzeButton.setText("Analyze Resume");
                    analyzeButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void displayResults(AnalysisResult res) {
        resultsPanel.removeAll();
        JPanel analysisPanel = createAnalysisPanel();
        resultsPanel.add(analysisPanel, BorderLayout.CENTER);

        scoreCircle.setScore(res.overallScore);
        atsCard.updateCard(res.atsScore, generateATSDetails(res));
        skillsCard.updateCard(res.skillsScore, generateSkillsDetails(res));
        grammarCard.updateCard(res.grammarScore, generateGrammarDetails(res));
        formatCard.updateCard(res.formatScore, generateFormatDetails(res));
        suggestionsArea.setText(generateSuggestions(res));

        resultsPanel.revalidate();
        resultsPanel.repaint();

        String desc = getScoreDescription(res.overallScore);
        JOptionPane.showMessageDialog(
                this,
                "Analysis complete!\n" + desc,
                "Results",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String generateATSDetails(AnalysisResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("ATS Compatibility: ");

        if (r.atsScore >= 85)
            sb.append("Excellent - Highly optimized for ATS systems\n\n");
        else if (r.atsScore >= 70)
            sb.append("Good - Well-structured for ATS parsing\n\n");
        else if (r.atsScore >= 50)
            sb.append("Fair - Some improvements needed\n\n");
        else
            sb.append("Poor - Significant ATS optimization required\n\n");

        sb.append("Contact Information:\n");
        sb.append(r.contactInfo.containsKey("email") ? "✓ Email: " + r.contactInfo.get("email") + "\n"
                : "✗ Email not found - CRITICAL\n");
        sb.append(r.contactInfo.containsKey("phone") ? "✓ Phone: " + r.contactInfo.get("phone") + "\n"
                : "✗ Phone not found - Important\n");
        sb.append(
                r.contactInfo.containsKey("linkedin") ? "✓ LinkedIn: Found\n" : "✗ LinkedIn not found - Recommended\n");
        sb.append(r.contactInfo.containsKey("github") ? "✓ GitHub/Portfolio: Found\n"
                : "○ GitHub/Portfolio not found - Optional\n");

        return sb.toString();
    }

    private String generateSkillsDetails(AnalysisResult r) {
        StringBuilder sb = new StringBuilder();
        String lower = r.content.toLowerCase();

        long techCount = EnhancedNLPProcessor.TECHNICAL_SKILLS.stream()
                .filter(s -> Pattern.compile("\\b" + Pattern.quote(s) + "\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(lower).find())
                .count();

        long softCount = EnhancedNLPProcessor.SOFT_SKILLS.stream()
                .filter(s -> Pattern.compile("\\b" + Pattern.quote(s) + "\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(lower).find())
                .count();

        sb.append("Skills Analysis:\n\n");
        sb.append("Technical Skills Detected: ").append(techCount).append("\n");
        sb.append("Soft Skills Detected: ").append(softCount).append("\n\n");

        if (techCount >= 10 && softCount >= 5) {
            sb.append("✓ Excellent skill diversity\n");
            sb.append("✓ Strong technical presence");
        } else if (techCount >= 7 && softCount >= 3) {
            sb.append("✓ Good skill coverage\n");
            sb.append("○ Consider adding more soft skills");
        } else if (techCount >= 5) {
            sb.append("○ Moderate skill presence\n");
            sb.append("⚠ Add more relevant technical keywords");
        } else {
            sb.append("⚠ Limited skill keywords detected\n");
            sb.append("⚠ Significantly expand skills section");
        }

        return sb.toString();
    }

    private String generateGrammarDetails(AnalysisResult r) {
        StringBuilder sb = new StringBuilder();
        String lower = r.content.toLowerCase();

        long verbCount = EnhancedNLPProcessor.ACTION_VERBS.stream()
                .filter(v -> Pattern.compile("\\b" + Pattern.quote(v) + "\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(lower).find())
                .count();

        Pattern numberPattern = Pattern.compile("\\b\\d+%|\\$\\d+|\\d+\\+|\\d{2,}\\b");
        Matcher numMatcher = numberPattern.matcher(r.content);
        int quantCount = 0;
        while (numMatcher.find() && quantCount < 20)
            quantCount++;

        sb.append("Writing Quality: ");
        if (r.grammarScore >= 85)
            sb.append("Excellent\n\n");
        else if (r.grammarScore >= 70)
            sb.append("Good\n\n");
        else if (r.grammarScore >= 50)
            sb.append("Fair\n\n");
        else
            sb.append("Needs Improvement\n\n");

        sb.append("Action Verbs Found: ").append(verbCount).append("\n");
        sb.append("Quantifiable Achievements: ").append(quantCount).append("\n\n");

        if (verbCount >= 10 && quantCount >= 5) {
            sb.append("✓ Strong professional language\n");
            sb.append("✓ Good use of metrics");
        } else if (verbCount >= 6 && quantCount >= 3) {
            sb.append("✓ Good use of action verbs\n");
            sb.append("○ Add more quantifiable results");
        } else if (verbCount >= 3) {
            sb.append("○ Use stronger action verbs\n");
            sb.append("⚠ Add metrics (%, $, numbers)");
        } else {
            sb.append("⚠ Lacking action verbs\n");
            sb.append("⚠ Missing quantifiable achievements");
        }

        return sb.toString();
    }

    private String generateFormatDetails(AnalysisResult r) {
        StringBuilder sb = new StringBuilder();

        int wordCount = r.content.trim().isEmpty() ? 0 : r.content.trim().split("\\s+").length;

        Matcher bulletMatcher = Pattern.compile("^\\s*[•●■▪▸►⦿⦾∙◦‣⁃-]\\s+", Pattern.MULTILINE)
                .matcher(r.content);
        int bulletCount = 0;
        while (bulletMatcher.find() && bulletCount < 50)
            bulletCount++;

        long sectionCount = EnhancedNLPProcessor.SECTION_HEADERS.stream()
                .filter(h -> r.content.toLowerCase().contains(h))
                .count();

        sb.append("Document Analysis:\n\n");
        sb.append("Word Count: ").append(wordCount).append(" words\n");
        sb.append("Bullet Points: ").append(bulletCount).append("\n");
        sb.append("Sections Detected: ").append(sectionCount).append("\n\n");

        if (wordCount >= 300 && wordCount <= 800) {
            sb.append("✓ Ideal length (300-800 words)\n");
        } else if (wordCount < 200) {
            sb.append("⚠ Resume too short - expand content\n");
        } else if (wordCount > 1000) {
            sb.append("⚠ Resume too long - be concise\n");
        } else {
            sb.append("○ Acceptable length\n");
        }

        if (bulletCount >= 8 && bulletCount <= 30) {
            sb.append("✓ Good use of bullet points\n");
        } else if (bulletCount < 5) {
            sb.append("⚠ Use more bullet points\n");
        } else {
            sb.append("○ Moderate bullet usage\n");
        }

        if (sectionCount >= 5) {
            sb.append("✓ Well-organized sections");
        } else if (sectionCount >= 3) {
            sb.append("○ Basic organization present");
        } else {
            sb.append("⚠ Add clear section headers");
        }

        return sb.toString();
    }

    private String generateSuggestions(AnalysisResult r) {
        List<String> suggestions = new ArrayList<>();

        // ATS-related suggestions
        if (!r.contactInfo.containsKey("email")) {
            suggestions.add("• CRITICAL: Add your email address at the top of the resume");
        }
        if (!r.contactInfo.containsKey("phone")) {
            suggestions.add("• IMPORTANT: Include your phone number for recruiter contact");
        }
        if (!r.contactInfo.containsKey("linkedin")) {
            suggestions.add("• Add LinkedIn profile URL to increase professional visibility");
        }
        if (!r.contactInfo.containsKey("github") && !r.contactInfo.containsKey("website")) {
            suggestions.add("• Include GitHub or portfolio link to showcase your work");
        }

        // Skills suggestions
        if (r.skillsScore < 70) {
            suggestions.add("• Expand skills section with relevant technical and soft skills");
            suggestions.add("• Mirror keywords from target job descriptions");
            suggestions.add("• Create a dedicated 'Skills' or 'Technical Skills' section");
        }

        // Grammar and writing suggestions
        if (r.grammarScore < 70) {
            suggestions.add("• Start bullet points with strong action verbs (e.g., Developed, Led, Implemented)");
            suggestions.add("• Add quantifiable achievements (e.g., 'Increased sales by 25%')");
            suggestions.add("• Avoid first-person pronouns (I, me, my)");
        }

        // Format suggestions
        if (r.formatScore < 70) {
            suggestions.add("• Use consistent bullet point formatting throughout");
            suggestions.add("• Add clear section headers: Experience, Education, Skills, Projects");
            suggestions.add("• Maintain proper spacing between sections");
            int wordCount = r.content.trim().split("\\s+").length;
            if (wordCount < 300) {
                suggestions.add("• Expand content - aim for 400-700 words for optimal length");
            } else if (wordCount > 900) {
                suggestions.add("• Condense content - keep resume concise (1-2 pages max)");
            }
        }

        // ATS-specific suggestions
        if (r.atsScore < 70) {
            suggestions.add("• Use standard section headers that ATS systems recognize");
            suggestions.add("• Include dates in standard format (e.g., 'Jan 2020 - Dec 2022')");
            suggestions.add("• Avoid special characters, images, and complex formatting");
        }

        // Positive reinforcement for high scores
        if (suggestions.isEmpty() || r.overallScore >= 85) {
            suggestions.add("• Excellent work! Your resume is well-optimized");
            suggestions.add("• Continue tailoring keywords for each specific job application");
            suggestions.add("• Keep your resume updated with latest skills and achievements");
        }

        return suggestions.stream()
                .limit(8) // Limit to top 8 suggestions
                .collect(Collectors.joining("\n"));
    }

    private String getScoreDescription(int score) {
        if (score >= 90)
            return "Outstanding — Your resume is exceptionally well-optimized!";
        if (score >= 80)
            return "Excellent — Strong resume with minor room for improvement";
        if (score >= 70)
            return "Good — Solid resume, some enhancements recommended";
        if (score >= 60)
            return "Fair — Decent foundation, multiple improvements needed";
        if (score >= 50)
            return "Below Average — Significant improvements required";
        return "Needs Work — Major revisions recommended before applying";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception ignored) {
            }
            new ResumeAnalyzerApp();
        });
    }
}