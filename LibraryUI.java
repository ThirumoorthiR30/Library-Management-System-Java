import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

class Book implements Serializable {
    int id;
    String title, author;
    boolean isIssued;

    Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isIssued = false;
    }
}

class Member implements Serializable {
    int id;
    String name;
    ArrayList<Integer> issuedBookIds = new ArrayList<>();

    Member(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Library implements Serializable {
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<Member> members = new ArrayList<>();
    ArrayList<String> returnHistory = new ArrayList<>();

    void addBook(Book b) { books.add(b); }
    void addMember(Member m) { members.add(m); }

    boolean issueBook(int bookId, int memberId) {
        for (Book b : books) {
            if (b.id == bookId && !b.isIssued) {
                b.isIssued = true;
                for (Member m : members) {
                    if (m.id == memberId) {
                        m.issuedBookIds.add(bookId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean returnBook(int bookId, int memberId) {
        for (Book b : books) {
            if (b.id == bookId && b.isIssued) {
                b.isIssued = false;
                for (Member m : members) {
                    if (m.id == memberId) {
                        m.issuedBookIds.remove((Integer) bookId);
                        returnHistory.add("Book ID " + b.id + " (" + b.title + ") returned by Member ID " + memberId);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

public class LibraryUI {
    private static final String FILE_NAME = "libraryData.enc";
    private static final String SECRET_KEY = "1234567812345678";

    private Library library;
    private JTextArea availableArea, issuedArea, returnedArea, membersArea;

    public LibraryUI() {
        library = loadData();
        createUI();
    }

    private void createUI() {
        JFrame frame = new JFrame("📚 Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Library Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Tabbed Pane for sections
        JTabbedPane tabbedPane = new JTabbedPane();
        availableArea = new JTextArea();
        issuedArea = new JTextArea();
        returnedArea = new JTextArea();
        membersArea = new JTextArea();

        availableArea.setEditable(false);
        issuedArea.setEditable(false);
        returnedArea.setEditable(false);
        membersArea.setEditable(false);

        tabbedPane.addTab("Available Books", new JScrollPane(availableArea));
        tabbedPane.addTab("Issued Books", new JScrollPane(issuedArea));
        tabbedPane.addTab("Return History", new JScrollPane(returnedArea));
        tabbedPane.addTab("Members", new JScrollPane(membersArea));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton btnAddBook = styledButton("Add Book");
        JButton btnAddMember = styledButton("Add Member");
        JButton btnIssue = styledButton("Issue Book");
        JButton btnReturn = styledButton("Return Book");
        JButton btnRefresh = styledButton("Refresh View");

        buttonPanel.add(btnAddBook);
        buttonPanel.add(btnAddMember);
        buttonPanel.add(btnIssue);
        buttonPanel.add(btnReturn);
        buttonPanel.add(btnRefresh);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        btnAddBook.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(frame, "Enter Book Title:");
            String author = JOptionPane.showInputDialog(frame, "Enter Author:");
            if (title != null && author != null) {
                library.addBook(new Book(library.books.size() + 1, title, author));
                saveData();
                updateViews();
            }
        });

        btnAddMember.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter Member Name:");
            if (name != null) {
                library.addMember(new Member(library.members.size() + 1, name));
                saveData();
                updateViews();
            }
        });

        btnIssue.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(JOptionPane.showInputDialog(frame, "Book ID:"));
                int memberId = Integer.parseInt(JOptionPane.showInputDialog(frame, "Member ID:"));
                JOptionPane.showMessageDialog(frame,
                        library.issueBook(bookId, memberId) ? "Book Issued!" : "Failed!");
                saveData();
                updateViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.");
            }
        });

        btnReturn.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(JOptionPane.showInputDialog(frame, "Book ID:"));
                int memberId = Integer.parseInt(JOptionPane.showInputDialog(frame, "Member ID:"));
                JOptionPane.showMessageDialog(frame,
                        library.returnBook(bookId, memberId) ? "Book Returned!" : "Failed!");
                saveData();
                updateViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.");
            }
        });

        btnRefresh.addActionListener(e -> updateViews());

        frame.setContentPane(mainPanel);
        updateViews();
        frame.setVisible(true);
    }

    private void updateViews() {
        StringBuilder available = new StringBuilder();
        StringBuilder issued = new StringBuilder();

        for (Book b : library.books) {
            if (b.isIssued) {
                issued.append(b.id).append(". ").append(b.title).append(" by ").append(b.author).append("\n");
            } else {
                available.append(b.id).append(". ").append(b.title).append(" by ").append(b.author).append("\n");
            }
        }

        availableArea.setText(available.toString());
        issuedArea.setText(issued.toString());

        StringBuilder returned = new StringBuilder();
        for (String entry : library.returnHistory) {
            returned.append(entry).append("\n");
        }
        returnedArea.setText(returned.toString());

        StringBuilder membersList = new StringBuilder();
        for (Member m : library.members) {
            membersList.append("ID: ").append(m.id)
                    .append(" | Name: ").append(m.name)
                    .append(" | Issued Books: ");
            if (m.issuedBookIds.isEmpty()) {
                membersList.append("None");
            } else {
                for (int id : m.issuedBookIds) {
                    Book b = findBookById(id);
                    if (b != null) {
                        membersList.append(b.title).append(", ");
                    }
                }
            }
            membersList.append("\n");
        }
        membersArea.setText(membersList.toString());
    }

    private Book findBookById(int id) {
        for (Book b : library.books) {
            if (b.id == id) return b;
        }
        return null;
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void saveData() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(library);
            oos.close();
            byte[] encrypted = encrypt(bos.toByteArray(), SECRET_KEY);
            Files.write(Paths.get(FILE_NAME), encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Library loadData() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return new Library();
            byte[] encrypted = Files.readAllBytes(Paths.get(FILE_NAME));
            byte[] decrypted = decrypt(encrypted, SECRET_KEY);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted));
            return (Library) ois.readObject();
        } catch (Exception e) {
            return new Library();
        }
    }

    private byte[] encrypt(byte[] data, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] data, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryUI::new);
    }
}
