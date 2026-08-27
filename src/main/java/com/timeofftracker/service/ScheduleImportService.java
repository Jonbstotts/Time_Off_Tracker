package com.timeofftracker.service;

import com.timeofftracker.model.ScheduleImportItem;
import com.timeofftracker.model.TimeOffType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleImportService {
    private static final Pattern ISO_DATE = Pattern.compile("\\b(20\\d{2})-(0?[1-9]|1[0-2])-(0?[1-9]|[12]\\d|3[01])\\b");
    private static final Pattern NUMERIC_DATE = Pattern.compile("\\b(0?[1-9]|1[0-2])[/-](0?[1-9]|[12]\\d|3[01])(?:[/-](20\\d{2}|\\d{2}))?\\b");
    private static final Pattern WORD_DATE = Pattern.compile("(?i)\\b(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\.?\\s+(\\d{1,2})(?:st|nd|rd|th)?(?:\\s*[-–]\\s*(\\d{1,2})(?:st|nd|rd|th)?)?(?:,?\\s+(20\\d{2}))?\\b");
    private static final Pattern REVERSED_WORD_DATE = Pattern.compile("(?i)\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\.?(?:,?\\s+(20\\d{2}))?\\b");

    private static final Map<String, String> HOLIDAY_NAMES = new LinkedHashMap<>();
    static {
        HOLIDAY_NAMES.put("new year", "New Year's Day");
        HOLIDAY_NAMES.put("martin luther king", "Martin Luther King Jr. Day");
        HOLIDAY_NAMES.put("mlk", "Martin Luther King Jr. Day");
        HOLIDAY_NAMES.put("president", "Presidents' Day");
        HOLIDAY_NAMES.put("good friday", "Good Friday");
        HOLIDAY_NAMES.put("easter", "Easter");
        HOLIDAY_NAMES.put("memorial", "Memorial Day");
        HOLIDAY_NAMES.put("juneteenth", "Juneteenth");
        HOLIDAY_NAMES.put("independence", "Independence Day");
        HOLIDAY_NAMES.put("july 4", "Independence Day");
        HOLIDAY_NAMES.put("labor", "Labor Day");
        HOLIDAY_NAMES.put("columbus", "Columbus Day");
        HOLIDAY_NAMES.put("indigenous peoples", "Indigenous Peoples' Day");
        HOLIDAY_NAMES.put("veteran", "Veterans Day");
        HOLIDAY_NAMES.put("thanksgiving", "Thanksgiving");
        HOLIDAY_NAMES.put("christmas eve", "Christmas Eve");
        HOLIDAY_NAMES.put("christmas", "Christmas");
    }

    public List<ScheduleImportItem> readSchedule(File file, int defaultYear) throws IOException {
        String text = extractText(file);
        return parse(text, defaultYear);
    }

    public String extractText(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file)) {
                return new PDFTextStripper().getText(document);
            }
        }
        if (name.endsWith(".docx")) {
            try (FileInputStream in = new FileInputStream(file); XWPFDocument document = new XWPFDocument(in)) {
                StringBuilder out = new StringBuilder();
                for (XWPFParagraph p : document.getParagraphs()) out.append(p.getText()).append('\n');
                for (XWPFTable table : document.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) out.append(cell.getText()).append(" | ");
                        out.append('\n');
                    }
                }
                return out.toString();
            }
        }
        if (name.endsWith(".txt") || name.endsWith(".csv")) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
        throw new IOException("Unsupported file type. Please select a PDF, DOCX, TXT, or CSV schedule.");
    }

    public List<ScheduleImportItem> parse(String text, int defaultYear) {
        Map<LocalDate, ScheduleImportItem> items = new LinkedHashMap<>();
        String[] lines = text.replace('\r', '\n').split("\\n+");

        for (String raw : lines) {
            String line = raw.replaceAll("\\s+", " ").trim();
            if (line.isBlank()) continue;

            Classification classification = classify(line);
            if (classification == null) continue;

            List<LocalDate> dates = parseDates(line, defaultYear);
            for (LocalDate date : dates) {
                items.putIfAbsent(date, new ScheduleImportItem(date, classification.type(), classification.description(), line));
            }
        }

        return items.values().stream().sorted(Comparator.comparing(ScheduleImportItem::date)).toList();
    }

    private Classification classify(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        TimeOffType type;
        if (lower.contains("limited service") || lower.contains("limited-service")) {
            type = TimeOffType.LIMITED_SERVICE;
        } else if (lower.contains("working holiday") || lower.contains("work holiday")) {
            type = TimeOffType.WORKING_HOLIDAY;
        } else {
            boolean recognizedHoliday = HOLIDAY_NAMES.keySet().stream().anyMatch(lower::contains);
            boolean holidayWords = lower.contains("holiday") || lower.contains("company off") || lower.contains("closed") || lower.contains("shutdown") || lower.contains("non-working day") || lower.contains("nonworking day");
            if (!recognizedHoliday && !holidayWords) return null;
            type = TimeOffType.HOLIDAY;
        }

        String description = detectHolidayName(lower);
        if (description == null) {
            description = cleanDescription(line);
            if (description.isBlank()) description = type.getDisplayName();
        }
        return new Classification(type, description);
    }

    private String detectHolidayName(String lower) {
        for (Map.Entry<String, String> e : HOLIDAY_NAMES.entrySet()) {
            if (lower.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private String cleanDescription(String line) {
        String cleaned = line
                .replaceAll("(?i)\\b(20\\d{2})-(0?[1-9]|1[0-2])-(0?[1-9]|[12]\\d|3[01])\\b", "")
                .replaceAll("(?i)\\b(0?[1-9]|1[0-2])[/-](0?[1-9]|[12]\\d|3[01])(?:[/-](20\\d{2}|\\d{2}))?\\b", "")
                .replaceAll("(?i)\\b(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\.?\\s+\\d{1,2}(?:st|nd|rd|th)?(?:\\s*[-–]\\s*\\d{1,2}(?:st|nd|rd|th)?)?(?:,?\\s+20\\d{2})?\\b", "")
                .replaceAll("^[\\s|:;,-]+|[\\s|:;,-]+$", "")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned;
    }

    private List<LocalDate> parseDates(String line, int defaultYear) {
        List<LocalDate> result = new ArrayList<>();

        Matcher iso = ISO_DATE.matcher(line);
        while (iso.find()) addDate(result, Integer.parseInt(iso.group(1)), Integer.parseInt(iso.group(2)), Integer.parseInt(iso.group(3)));

        Matcher numeric = NUMERIC_DATE.matcher(line);
        while (numeric.find()) {
            int year = parseYear(numeric.group(3), defaultYear);
            addDate(result, year, Integer.parseInt(numeric.group(1)), Integer.parseInt(numeric.group(2)));
        }

        Matcher word = WORD_DATE.matcher(line);
        while (word.find()) {
            int month = monthNumber(word.group(1));
            int startDay = Integer.parseInt(word.group(2));
            int endDay = word.group(3) == null ? startDay : Integer.parseInt(word.group(3));
            int year = word.group(4) == null ? defaultYear : Integer.parseInt(word.group(4));
            for (int day = startDay; day <= endDay; day++) addDate(result, year, month, day);
        }

        Matcher reversed = REVERSED_WORD_DATE.matcher(line);
        while (reversed.find()) {
            int day = Integer.parseInt(reversed.group(1));
            int month = monthNumber(reversed.group(2));
            int year = reversed.group(3) == null ? defaultYear : Integer.parseInt(reversed.group(3));
            addDate(result, year, month, day);
        }

        return result.stream().distinct().toList();
    }

    private int parseYear(String yearText, int defaultYear) {
        if (yearText == null || yearText.isBlank()) return defaultYear;
        int year = Integer.parseInt(yearText);
        return year < 100 ? 2000 + year : year;
    }

    private int monthNumber(String text) {
        String normalized = text.replace(".", "");
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(normalized.length() <= 4 ? "MMM" : "MMMM")
                .parseDefaulting(ChronoField.YEAR, 2000)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH);
        try {
            return Month.from(formatter.parse(normalized)).getValue();
        } catch (DateTimeParseException ex) {
            if (normalized.equalsIgnoreCase("Sept")) return 9;
            throw ex;
        }
    }

    private void addDate(List<LocalDate> dates, int year, int month, int day) {
        try {
            dates.add(LocalDate.of(year, month, day));
        } catch (DateTimeException ignored) {
        }
    }

    private record Classification(TimeOffType type, String description) {}
}
