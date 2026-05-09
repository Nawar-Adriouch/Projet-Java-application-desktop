package Test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * SOUTENANCE PLANNING SYSTEM — FIXED VERSION
 *
 * RULES RESPECTED:
 * ✅ No double booking (professor cannot be in two rooms at the same slot)
 * ✅ Jury != Encadrant
 * ✅ Jury1 != Jury2
 * ✅ 1 slot resting time between JURY assignments only
 * ✅ Balanced workload
 * ✅ Same professor cannot appear twice in same slot
 *
 * FIX APPLIED:
 * ─────────────────────────────────────────────────────────────────────────────
 * BEFORE (broken): A single `occupied` map stored ALL assignments (encadrant +
 * jury). The rest constraint |s - slot| <= 1 was applied to encadrant slots
 * too. With 21 encadrants filling slots 0 and 1, ALL professors were blocked
 * from jury duty at slot 1 via the rest rule → NO AVAILABLE PROF.
 *
 * AFTER (fixed): Two separate maps:
 *   - encadrantSlots: tracks only the encadrant role
 *   - jurySlots:      tracks only jury roles
 *
 * The "same slot" conflict check uses BOTH maps (a prof can't be in two places
 * at once regardless of role). But the "rest time" check ONLY looks at jurySlots
 * (a prof who is an encadrant at slot 0 is still available as a jury at slot 1
 * because attending your own student's defence doesn't count as a jury shift).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class projJavaVersion1 {

    // ─────────────────────────────────────────────
    // DATA STRUCTURE
    // ─────────────────────────────────────────────

    public static class Soutenance {

        public final String encadrant;
        public final String student;

        public String jury1;
        public String jury2;

        public int day;
        public int slot;
        public int room;

        Soutenance(String enc, String stu) {
            this.encadrant = enc;
            this.student = stu;
        }
    }

    // ─────────────────────────────────────────────
    // CONSTANTS
    // ─────────────────────────────────────────────

    public static final int ROOMS = 5;
    private static final int SLOTS_DAY = 7;
    private static final int MAX_DAY = ROOMS * SLOTS_DAY;

    private static final String[] TIME_SLOTS = {
            "08:00-09:00",
            "09:00-10:00",
            "10:00-11:00",
            "11:00-12:00",
            "14:00-15:00",
            "15:00-16:00",
            "16:00-17:00"
    };

    // ─────────────────────────────────────────────
    // GLOBAL STRUCTURES
    // ─────────────────────────────────────────────

    private final List<String> allProfs = new ArrayList<>();

    // ── FIX: two separate occupation maps ─────────────────────────────────────

    // prof → slots where they appear as ENCADRANT  (used for: same-slot conflict only)
    private final Map<String, Set<String>> encadrantSlots = new HashMap<>();

    // prof → slots where they appear as JURY        (used for: same-slot conflict + rest time)
    private final Map<String, Set<String>> jurySlots = new HashMap<>();

    // prof -> workload (total participations: encadrant + jury combined)
    private final Map<String, Integer> load = new HashMap<>();

    // ─────────────────────────────────────────────
    // MAIN BUILD
    // ─────────────────────────────────────────────

    public void buildAndPrint(String affectationPath) throws Exception {

        List<Soutenance> queue = loadAffectation(affectationPath);

        int total = queue.size();

        int days = (int) Math.ceil((double) total / MAX_DAY);

        assignPositions(queue, days);

        // VERY IMPORTANT: register encadrants first so they are blocked
        // from appearing in two rooms at the same slot
        registerEncadrants(queue);

        assignJuries(queue);

        System.out.println("═".repeat(140));
        System.out.printf("📋 PLANNING DES SOUTENANCES%n");
        System.out.printf("Étudiants : %d | Jours : %d | Salles : %d%n",
                total, days, ROOMS);
        System.out.println("═".repeat(140));

        int cursor = 0;

        for (int day = 1; day <= days; day++) {

            System.out.printf(
                    "%n┌─ JOUR %d ──────────────────────────────────────────────────────────────────────────┐%n",
                    day);

            printDayGrid(queue, cursor);

            cursor += Math.min(MAX_DAY, total - cursor);
        }

        printStats();
    }

    // ─────────────────────────────────────────────
    // ASSIGN POSITIONS
    // ─────────────────────────────────────────────

    private void assignPositions(List<Soutenance> queue, int days) {

        int idx = 0;

        for (int day = 1; day <= days; day++) {
            for (int slot = 0; slot < SLOTS_DAY; slot++) {
                for (int room = 0; room < ROOMS; room++) {

                    if (idx >= queue.size()) return;

                    Soutenance s = queue.get(idx);
                    s.day  = day;
                    s.slot = slot;
                    s.room = room;
                    idx++;
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // REGISTER ENCADRANTS
    // Records each prof's encadrant slot into encadrantSlots.
    // This blocks them from being assigned as a jury member
    // in that SAME slot (two rooms at once), but does NOT
    // trigger the rest-time check for adjacent slots.
    // ─────────────────────────────────────────────

    private void registerEncadrants(List<Soutenance> queue) {

        for (Soutenance s : queue) {
            String key = slotKey(s.day, s.slot);

            encadrantSlots
                    .computeIfAbsent(s.encadrant, k -> new HashSet<>())
                    .add(key);

            // Count being an encadrant as one participation in the workload
            load.merge(s.encadrant, 1, Integer::sum);
        }
    }

    // ─────────────────────────────────────────────
    // ASSIGN JURIES
    // ─────────────────────────────────────────────

    private void assignJuries(List<Soutenance> queue) {

        for (Soutenance s : queue) {

            String j1 = findBestJury(s.encadrant, null, s.day, s.slot);

            if (j1 == null) {
                s.jury1 = "NO AVAILABLE PROF";
                s.jury2 = "NO AVAILABLE PROF";
                continue;
            }

            s.jury1 = j1;
            registerJury(j1, s.day, s.slot);   // register BEFORE searching for j2

            String j2 = findBestJury(s.encadrant, j1, s.day, s.slot);

            if (j2 == null) {
                s.jury2 = "NO AVAILABLE PROF";
                continue;
            }

            s.jury2 = j2;
            registerJury(j2, s.day, s.slot);
        }
    }

    // ─────────────────────────────────────────────
    // FIND BEST JURY
    // ─────────────────────────────────────────────
    private String findBestJury(
            String encadrant,
            String forbidden,
            int day,
            int slot) {

        List<String> candidates = new ArrayList<>();

        for (String prof : allProfs) {

            if (prof.equals(encadrant))         continue; // jury ≠ encadrant
            if (prof.equals(forbidden))         continue; // jury2 ≠ jury1
            if (!isAvailable(prof, day, slot))  continue; // conflict / rest check

            candidates.add(prof);
        }

        if (candidates.isEmpty()) return null;

        // Pick the least-loaded eligible professor
        candidates.sort(Comparator.comparingInt(p -> load.getOrDefault(p, 0)));

        return candidates.get(0);
    }

    // ─────────────────────────────────────────────
    // AVAILABILITY CHECK  ← THE KEY FIX IS HERE
    //
    // Three independent checks:
    //
    // 1. SAME-SLOT ENCADRANT CONFLICT
    //    Is this prof already an encadrant somewhere at (day, slot)?
    //    → They cannot physically be in another room. Blocked.
    //
    // 2. SAME-SLOT JURY CONFLICT
    //    Is this prof already on a jury somewhere at (day, slot)?
    //    → Same physical impossibility. Blocked.
    //
    // 3. JURY REST TIME
    //    Is this prof on a jury in an ADJACENT slot on the same day?
    //    → |prevJurySlot - slot| <= 1 → Blocked.
    //    NOTE: encadrant slots do NOT contribute to rest time.
    //    A prof being an encadrant at slot 0 stays available as
    //    jury at slot 1 — their own supervision is not a jury shift.
    // ─────────────────────────────────────────────

    private boolean isAvailable(String prof, int day, int slot) {

        String target = slotKey(day, slot);

        // CHECK 1: already an encadrant at this exact slot → blocked
        Set<String> encSlots = encadrantSlots.getOrDefault(prof, Collections.emptySet());
        if (encSlots.contains(target)) return false;

        // CHECK 2 + 3: scan existing jury slots
        Set<String> jSlots = jurySlots.getOrDefault(prof, Collections.emptySet());

        for (String key : jSlots) {

            String[] parts = key.split("-");
            int d = Integer.parseInt(parts[0].substring(1));
            int s = Integer.parseInt(parts[1].substring(1));

            // CHECK 2: same slot (double-booking on a jury)
            if (d == day && s == slot) return false;

            // CHECK 3: adjacent jury slot on same day (rest time)
            if (d == day && Math.abs(s - slot) <= 1) return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────
    // REGISTER JURY (called only when a jury role is assigned)
    // ─────────────────────────────────────────────

    private void registerJury(String prof, int day, int slot) {
        jurySlots
                .computeIfAbsent(prof, k -> new HashSet<>())
                .add(slotKey(day, slot));

        load.merge(prof, 1, Integer::sum);
    }

    // ─────────────────────────────────────────────
    // SLOT KEY
    // ─────────────────────────────────────────────

    private String slotKey(int day, int slot) {
        return "D" + day + "-S" + slot;
    }

    // ─────────────────────────────────────────────
    // PRINT GRID
    // ─────────────────────────────────────────────

    private void printDayGrid(List<Soutenance> queue, int startIdx) {

        int total   = queue.size();
        int CELL_W  = 42;
        String SEP  = "─".repeat(CELL_W);

        System.out.print("┌" + "─".repeat(14) + "┬");
        for (int r = 0; r < ROOMS; r++)
            System.out.print(SEP + (r < ROOMS - 1 ? "┬" : "┐"));
        System.out.println();

        System.out.printf("│ %-12s │", "Créneau");
        for (int r = 1; r <= ROOMS; r++)
            System.out.printf(" %-" + (CELL_W - 2) + "s │", "Salle " + r);
        System.out.println();

        System.out.print("├" + "─".repeat(14) + "┼");
        for (int r = 0; r < ROOMS; r++)
            System.out.print(SEP + (r < ROOMS - 1 ? "┼" : "┤"));
        System.out.println();

        for (int slot = 0; slot < SLOTS_DAY; slot++) {

            int base = startIdx + slot * ROOMS;

            printLine(queue, total, base, "Enc: ", CELL_W, s -> s.encadrant, slot);
            printLine(queue, total, base, "Stu: ", CELL_W, s -> s.student,   -1);
            printLine(queue, total, base, "J1 : ", CELL_W, s -> s.jury1,     -1);
            printLine(queue, total, base, "J2 : ", CELL_W, s -> s.jury2,     -1);

            if (slot < SLOTS_DAY - 1) {
                System.out.print("├" + "─".repeat(14) + "┼");
                for (int r = 0; r < ROOMS; r++)
                    System.out.print(SEP + (r < ROOMS - 1 ? "┼" : "┤"));
                System.out.println();
            }
        }

        System.out.print("└" + "─".repeat(14) + "┴");
        for (int r = 0; r < ROOMS; r++)
            System.out.print(SEP + (r < ROOMS - 1 ? "┴" : "┘"));
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // PRINT LINE
    // ─────────────────────────────────────────────

    public interface TextExtractor {
        String get(Soutenance s);
    }

    private void printLine(
            List<Soutenance> queue,
            int total,
            int base,
            String prefix,
            int cellWidth,
            TextExtractor extractor,
            int slotIndex) {

        String label = slotIndex >= 0 ? TIME_SLOTS[slotIndex] : "";
        System.out.printf("│ %-12s │", label);

        for (int room = 0; room < ROOMS; room++) {
            int idx    = base + room;
            String txt = "";
            if (idx < total) {
                Soutenance s = queue.get(idx);
                txt = truncate(prefix + extractor.get(s), cellWidth - 2);
            }
            System.out.printf(" %-" + (cellWidth - 2) + "s │", txt);
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // LOAD EXCEL
    // ─────────────────────────────────────────────

    private List<Soutenance> loadAffectation(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        Workbook wb         = new XSSFWorkbook(fis);
        Sheet sheet         = wb.getSheetAt(0);

        List<String>        encNames  = new ArrayList<>();
        List<List<String>>  encStuds  = new ArrayList<>();

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            Cell nomCell    = row.getCell(0);
            Cell prenomCell = row.getCell(1);
            if (nomCell == null) continue;

            String fullName = nomCell.toString().trim()
                    + " "
                    + prenomCell.toString().trim();

            List<String> students = new ArrayList<>();

            for (int c = 2; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    String val = cell.toString().trim();
                    if (!val.isEmpty() && !val.equalsIgnoreCase("null"))
                        students.add(val);
                }
            }

            if (!students.isEmpty()) {
                encNames.add(fullName);
                encStuds.add(students);
                allProfs.add(fullName);
            }
        }

        wb.close();
        fis.close();

        // Round-robin ordering so students of different encadrants are interleaved
        List<Soutenance> queue = new ArrayList<>();

        int maxStudents = encStuds.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        for (int round = 0; round < maxStudents; round++) {
            for (int e = 0; e < encNames.size(); e++) {
                List<String> studs = encStuds.get(e);
                if (round < studs.size()) {
                    queue.add(new Soutenance(encNames.get(e), studs.get(round)));
                }
            }
        }

        System.out.printf(
                "✅ Affectation chargée : %d encadrants, %d étudiants%n",
                encNames.size(),
                queue.size());

        return queue;
    }

    // ─────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────

    private void printStats() {

        System.out.println("\n══════════════════════════════════════");
        System.out.println("📊 CHARGE DES PROFESSEURS");
        System.out.println("══════════════════════════════════════");

        List<String> profs = new ArrayList<>(load.keySet());
        profs.sort(Comparator.comparingInt(load::get));

        for (String p : profs) {
            System.out.printf("%-35s -> %2d participations%n", p, load.get(p));
        }
    }

    // ─────────────────────────────────────────────
    // TRUNCATE
    // ─────────────────────────────────────────────

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) throws Exception {

        String affectationPath = args.length > 0
                ? args[0]
                : "C:\\Users\\Surface Laptop\\Desktop\\java_data\\Affectation.xlsx";

        new projJavaVersion1().buildAndPrint(affectationPath);
    }
}