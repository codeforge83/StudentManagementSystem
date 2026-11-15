import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/** A local, file-backed Student Management System built with Java Swing. */
@SuppressWarnings({"serial", "this-escape"})
public class StudentManagementSystem extends JFrame {
    private static final Color NAVY = new Color(20, 61, 112), BLUE = new Color(10, 94, 194), DANGER = new Color(169, 21, 44), NEUTRAL = new Color(64, 68, 74), DISABLED_BUTTON = new Color(82, 96, 116), FOCUS_RING = new Color(20, 103, 205), BACKGROUND = new Color(245, 248, 252), TEXT = new Color(35, 48, 65);
    private static final String[] COLUMNS = {"Student ID", "Name", "Age", "Course", "Gender"};
    private final Path dataFile = Path.of("Table.txt").toAbsolutePath();
    private final List<Student> students = new ArrayList<>();
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField idField = new JTextField(), nameField = new JTextField(), ageField = new JTextField(), courseField = new JTextField(), searchField = new JTextField(22);
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JLabel totalValue = statValue(), genderValue = statValue(), courseValue = statValue(), statusLabel = new JLabel("Ready");
    private String selectedStudentId;

    public StudentManagementSystem() {
        setTitle("Student Management System"); setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); setMinimumSize(new Dimension(900, 620)); setSize(1180, 760); setLocationByPlatform(true);
        buildInterface(); loadStudents();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(0, 16)); root.setBorder(new EmptyBorder(22, 26, 16, 26)); root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(16, 16)); content.setOpaque(false); content.add(createFormCard(), BorderLayout.NORTH); content.add(createRecordsCard(), BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER); root.add(createStatusBar(), BorderLayout.SOUTH); setContentPane(root);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0)); header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Student Management"); title.setFont(new Font("SansSerif", Font.BOLD, 30)); title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Manage student records clearly, safely, and efficiently"); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14)); subtitle.setForeground(new Color(93, 108, 126));
        titles.add(title); titles.add(Box.createVerticalStrut(5)); titles.add(subtitle); header.add(titles, BorderLayout.WEST); header.add(createStatistics(), BorderLayout.EAST); return header;
    }

    private JComponent createStatistics() {
        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0)); stats.setOpaque(false); stats.add(statCard("TOTAL STUDENTS", totalValue)); stats.add(statCard("GENDER DISTRIBUTION", genderValue)); stats.add(statCard("COURSES", courseValue)); return stats;
    }
    private JLabel statValue() { JLabel value = new JLabel("0"); value.setFont(new Font("SansSerif", Font.BOLD, 18)); value.setForeground(NAVY); return value; }
    private JComponent statCard(String label, JLabel value) {
        JPanel card = new JPanel(); card.setPreferredSize(new Dimension(160, 66)); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(211, 225, 242)), new EmptyBorder(9, 12, 9, 12))); card.setBackground(Color.WHITE); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(label); heading.setFont(new Font("SansSerif", Font.BOLD, 10)); heading.setForeground(new Color(96, 117, 142)); card.add(heading); card.add(Box.createVerticalStrut(3)); card.add(value); return card;
    }

    private JComponent createFormCard() {
        JPanel card = createCard(); card.setLayout(new BorderLayout(0, 14)); card.add(sectionTitle("Student details", "Enter information below, then save it to the records."), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0, 0, 0, 12); c.fill = GridBagConstraints.HORIZONTAL;
        String[] labels = {"Student ID", "Full name", "Age", "Course", "Gender"}; JComponent[] inputs = {idField, nameField, ageField, courseField, genderBox};
        for (int i = 0; i < labels.length; i++) { c.gridx = i; c.gridy = 0; c.weightx = i == 1 ? 1.4 : 1; fields.add(fieldPanel(labels[i], inputs[i]), c); }
        card.add(fields, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); actions.setOpaque(false);
        JButton add = createButton("+  Add student", BLUE), update = createButton("↻  Update selected", NAVY), delete = createButton("×  Delete selected", DANGER), clear = createButton("Clear form", NEUTRAL);
        actions.add(add); actions.add(update); actions.add(delete); actions.add(clear); card.add(actions, BorderLayout.SOUTH);
        add.addActionListener(e -> addStudent()); update.addActionListener(e -> updateStudent()); delete.addActionListener(e -> deleteStudent()); clear.addActionListener(e -> clearForm()); return card;
    }

    private JComponent createRecordsCard() {
        JPanel card = createCard(); card.setLayout(new BorderLayout(0, 12)); JPanel top = new JPanel(new BorderLayout(12, 0)); top.setOpaque(false); top.add(sectionTitle("Student records", "Select a row to edit or delete it."), BorderLayout.WEST);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0)); search.setOpaque(false); JLabel searchLabel = new JLabel("Search"); searchLabel.setForeground(new Color(80, 97, 118)); JButton clearSearch = createButton("Show all", NAVY); search.add(searchLabel); search.add(searchField); search.add(clearSearch); top.add(search, BorderLayout.EAST); card.add(top, BorderLayout.NORTH);
        configureTable(); JScrollPane pane = new JScrollPane(table); pane.setBorder(BorderFactory.createLineBorder(new Color(215, 226, 240))); pane.getViewport().setBackground(Color.WHITE); card.add(pane, BorderLayout.CENTER);
        clearSearch.addActionListener(e -> searchField.setText("")); searchField.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e) { applyFilter(); } public void removeUpdate(DocumentEvent e) { applyFilter(); } public void changedUpdate(DocumentEvent e) { applyFilter(); } }); return card;
    }
    private JPanel createCard() { JPanel card = new JPanel(); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215, 226, 240)), new EmptyBorder(16, 18, 16, 18))); return card; }
    private JComponent sectionTitle(String heading, String subheading) { JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); JLabel h = new JLabel(heading), s = new JLabel(subheading); h.setFont(new Font("SansSerif", Font.BOLD, 17)); h.setForeground(TEXT); s.setFont(new Font("SansSerif", Font.PLAIN, 12)); s.setForeground(new Color(109, 123, 141)); p.add(h); p.add(Box.createVerticalStrut(3)); p.add(s); return p; }
    private JPanel fieldPanel(String label, JComponent field) { JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false); JLabel l = new JLabel(label); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setForeground(TEXT); field.setFont(new Font("SansSerif", Font.PLAIN, 14)); p.add(l, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER); return p; }
    private JButton createButton(String text, Color color) {
        // The Windows system look and feel ignores setBackground() on buttons and paints its own light
        // face, which left the white text unreadable. A BasicButtonUI-based UI always paints the
        // background colour set below, so the colours are identical on every look and feel.
        JButton button = new JButton(text) {
            @Override public void updateUI() { setUI(new SolidButtonUI()); }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.getModel().addChangeListener(e -> updateButtonAppearance(button, color));
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent event) { updateButtonAppearance(button, color); }
            @Override public void focusLost(java.awt.event.FocusEvent event) { updateButtonAppearance(button, color); }
        });
        updateButtonAppearance(button, color);
        return button;
    }

    private void updateButtonAppearance(JButton button, Color baseColor) {
        ButtonModel state = button.getModel();
        Color background = !button.isEnabled() ? DISABLED_BUTTON : state.isPressed() ? shade(baseColor, 0.72) : state.isRollover() ? shade(baseColor, 0.86) : baseColor;
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        Color borderColor = button.isFocusOwner() ? FOCUS_RING : shade(baseColor, 0.68);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, button.isFocusOwner() ? 2 : 1), new EmptyBorder(8, 12, 8, 12)));
    }

    /** Paints a solid background with high-contrast text; disabled buttons stay readable but muted. */
    private static final class SolidButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private static final Color DISABLED_TEXT = new Color(228, 234, 242);
        @Override protected void paintText(Graphics g, AbstractButton button, Rectangle textRect, String text) {
            FontMetrics metrics = g.getFontMetrics(button.getFont());
            int mnemonic = button.getDisplayedMnemonicIndex();
            g.setColor(button.isEnabled() ? Color.WHITE : DISABLED_TEXT);
            javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonic, textRect.x + getTextShiftOffset(), textRect.y + metrics.getAscent() + getTextShiftOffset());
        }
    }

    private Color shade(Color color, double factor) {
        return new Color((int) (color.getRed() * factor), (int) (color.getGreen() * factor), (int) (color.getBlue() * factor));
    }
    private void configureTable() {
        table.setRowSorter(sorter); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setRowHeight(32); table.setFont(new Font("SansSerif", Font.PLAIN, 13)); table.setForeground(TEXT); table.setGridColor(new Color(231, 237, 245)); table.setShowVerticalLines(false); table.setFillsViewportHeight(true); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); table.getTableHeader().setBackground(NAVY); table.getTableHeader().setForeground(Color.WHITE); table.getTableHeader().setReorderingAllowed(false);
        // The Windows look and feel ignores header colours (white text on a light header = blank headings),
        // so the header cells are painted by our own opaque renderer. Sort direction is shown with an arrow.
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                String text = value == null ? "" : value.toString();
                int modelColumn = t.convertColumnIndexToModel(col);
                for (RowSorter.SortKey key : t.getRowSorter().getSortKeys()) {
                    if (key.getColumn() == modelColumn && key.getSortOrder() != SortOrder.UNSORTED) { text += key.getSortOrder() == SortOrder.ASCENDING ? "  \u25B2" : "  \u25BC"; break; }
                }
                super.getTableCellRendererComponent(t, text, false, false, row, col);
                setOpaque(true); setBackground(NAVY); setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.BOLD, 12)); setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 100, 150)), new EmptyBorder(8, 9, 8, 9)));
                return this;
            }
        });
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) { Component component = super.getTableCellRendererComponent(t, value, selected, focus, row, col); setBorder(new EmptyBorder(0, 9, 0, 9)); if (!selected) component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 254)); return component; } };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) populateForm(table.convertRowIndexToModel(table.getSelectedRow())); });
    }
    private JComponent createStatusBar() { statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12)); statusLabel.setForeground(new Color(92, 107, 126)); return statusLabel; }

    private void addStudent() { Student student = readForm(); if (student == null) return; if (findById(student.id()) != null) { showError("A student with this ID already exists."); return; } List<Student> candidate = new ArrayList<>(students); candidate.add(student); if (saveStudents(candidate)) { students.add(student); refreshTable(); clearForm(); setStatus("Student added successfully."); } }
    private void updateStudent() { if (selectedStudentId == null) { showError("Select a student record from the table first."); return; } Student revised = readForm(); if (revised == null) return; Student sameId = findById(revised.id()); if (sameId != null && !sameId.id().equalsIgnoreCase(selectedStudentId)) { showError("Another student already has this ID."); return; } int index = indexOfId(selectedStudentId); if (index < 0) { showError("The selected record no longer exists."); clearForm(); return; } List<Student> candidate = new ArrayList<>(students); candidate.set(index, revised); if (saveStudents(candidate)) { students.set(index, revised); refreshTable(); clearForm(); setStatus("Student record updated successfully."); } }
    private void deleteStudent() { if (selectedStudentId == null) { showError("Select a student record to delete."); return; } int choice = JOptionPane.showConfirmDialog(this, "Delete the selected student permanently?", "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); if (choice != JOptionPane.YES_OPTION) return; List<Student> candidate = students.stream().filter(s -> !s.id().equalsIgnoreCase(selectedStudentId)).collect(Collectors.toCollection(ArrayList::new)); if (saveStudents(candidate)) { students.clear(); students.addAll(candidate); refreshTable(); clearForm(); setStatus("Student record deleted."); } }
    private Student readForm() { String id = idField.getText().trim(), name = nameField.getText().trim(), ageText = ageField.getText().trim(), course = courseField.getText().trim(); if (id.isEmpty() || name.isEmpty() || ageText.isEmpty() || course.isEmpty()) { showError("Complete all student fields."); return null; } if (!id.matches("[A-Za-z0-9_-]{1,40}")) { showError("Student ID may use only letters, numbers, hyphens, and underscores (up to 40 characters)."); return null; } if (containsDelimiter(name) || containsDelimiter(course)) { showError("Name and course cannot contain tabs or line breaks."); return null; } int age; try { age = Integer.parseInt(ageText); } catch (NumberFormatException e) { showError("Age must be a whole number."); return null; } if (age < 1 || age > 120) { showError("Age must be between 1 and 120."); return null; } return new Student(id, name, age, course, Objects.toString(genderBox.getSelectedItem(), "Other")); }
    private boolean containsDelimiter(String value) { return value.indexOf('\t') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0; }

    private void loadStudents() {
        students.clear(); if (!Files.exists(dataFile)) { refreshTable(); setStatus("No data file found. A new one will be created when you add a student."); return; } int rejected = 0;
        try (BufferedReader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) { String line; boolean firstLine = true; while ((line = reader.readLine()) != null) { if (firstLine && line.startsWith("Student ID\t")) { firstLine = false; continue; } firstLine = false; String[] values = line.split("\\t", -1); if (values.length != 5) { rejected++; continue; } try { Student s = new Student(values[0].trim(), values[1].trim(), Integer.parseInt(values[2].trim()), values[3].trim(), values[4].trim()); if (s.id().isEmpty() || s.name().isEmpty() || s.course().isEmpty() || s.age() < 1 || s.age() > 120 || findById(s.id()) != null) rejected++; else students.add(s); } catch (NumberFormatException ex) { rejected++; } } } catch (IOException ex) { showError("Unable to load student records: " + ex.getMessage()); setStatus("Could not load " + dataFile.getFileName() + "."); } refreshTable(); if (rejected > 0) setStatus("Loaded " + students.size() + " student record(s); skipped " + rejected + " malformed row(s).");
    }
    /** Writes a complete temporary sibling file before replacing the saved records. */
    private boolean saveStudents(List<Student> source) {
        Path directory = dataFile.getParent(); try { if (directory != null) Files.createDirectories(directory); Path temporary = Files.createTempFile(directory, "students-", ".tmp"); try { try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { writer.write(String.join("\t", COLUMNS)); writer.newLine(); for (Student s : source) { writer.write(s.toTsv()); writer.newLine(); } } try { Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); } catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING); } return true; } finally { Files.deleteIfExists(temporary); } } catch (IOException ex) { showError("Unable to save records. No changes were applied: " + ex.getMessage()); return false; }
    }
    private void refreshTable() { model.setRowCount(0); for (Student s : students) model.addRow(s.toRow()); updateStatistics(); applyFilter(); }
    private void applyFilter() { String query = searchField.getText().trim(); sorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 0, 1, 3, 4)); setStatus(query.isEmpty() ? "Showing all " + students.size() + " student record(s)." : "Showing " + table.getRowCount() + " matching record(s)."); }
    private void updateStatistics() { totalValue.setText(String.valueOf(students.size())); Map<String, Long> genders = students.stream().collect(Collectors.groupingBy(Student::gender, LinkedHashMap::new, Collectors.counting())); genderValue.setText(genders.isEmpty() ? "—" : formatCounts(genders)); Map<String, Long> courses = students.stream().collect(Collectors.groupingBy(Student::course, LinkedHashMap::new, Collectors.counting())); courseValue.setText(courses.isEmpty() ? "—" : formatCounts(courses)); }
    private String formatCounts(Map<String, Long> counts) { return counts.entrySet().stream().map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining("  •  ")); }
    private void populateForm(int row) { selectedStudentId = String.valueOf(model.getValueAt(row, 0)); idField.setText(selectedStudentId); nameField.setText(String.valueOf(model.getValueAt(row, 1))); ageField.setText(String.valueOf(model.getValueAt(row, 2))); courseField.setText(String.valueOf(model.getValueAt(row, 3))); genderBox.setSelectedItem(String.valueOf(model.getValueAt(row, 4))); }
    private void clearForm() { selectedStudentId = null; idField.setText(""); nameField.setText(""); ageField.setText(""); courseField.setText(""); genderBox.setSelectedIndex(0); table.clearSelection(); idField.requestFocusInWindow(); }
    private Student findById(String id) { int index = indexOfId(id); return index < 0 ? null : students.get(index); }
    private int indexOfId(String id) { for (int i = 0; i < students.size(); i++) if (students.get(i).id().equalsIgnoreCase(id)) return i; return -1; }
    private void setStatus(String message) { statusLabel.setText(message); }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Student Management System", JOptionPane.ERROR_MESSAGE); }
    private record Student(String id, String name, int age, String course, String gender) { String[] toRow() { return new String[]{id, name, String.valueOf(age), course, gender}; } String toTsv() { return String.join("\t", toRow()); } }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { } new StudentManagementSystem().setVisible(true); }); }
}
