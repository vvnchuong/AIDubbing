package com.tool.aidubbing.service;

import com.tool.aidubbing.dto.SubtitleItem;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubtitleService {

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})"
    );

    public List<SubtitleItem> readSubtitle(String workDir) {
        Path srcPath = Path.of(workDir, "output", "src.srt");
        Path transPath = Path.of(workDir, "output", "trans.srt");

        List<SrtEntry> srcEntries = parseSrt(srcPath);
        List<SrtEntry> transEntries = parseSrt(transPath);

        if (srcEntries.size() != transEntries.size()) {
            throw new AppException(ErrorCode.SUBTITLE_MISMATCH);
        }

        List<SubtitleItem> result = new ArrayList<>();
        for (int i = 0; i < srcEntries.size(); i++) {
            SrtEntry src = srcEntries.get(i);
            SrtEntry trans = transEntries.get(i);
            result.add(new SubtitleItem(src.index, src.start, src.end, src.text, trans.text));
        }
        return result;
    }

    public void writeSubtitle(String workDir, List<SubtitleItem> lines) {
        String content = buildSrtContent(lines);

        Path transPath = Path.of(workDir, "output", "trans.srt");
        Path transForAudioPath = Path.of(workDir, "output", "audio", "trans_subs_for_audio.srt");

        try {
            Files.writeString(transPath, content, StandardCharsets.UTF_8);

            if (Files.exists(transForAudioPath)) {
                Files.writeString(transForAudioPath, content, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.SUBTITLE_WRITE_FAILED);
        }
    }

    private String buildSrtContent(List<SubtitleItem> lines) {
        StringBuilder sb = new StringBuilder();
        for (SubtitleItem line : lines) {
            sb.append(line.getIndex()).append("\n");
            sb.append(formatTime(line.getStart())).append(" --> ").append(formatTime(line.getEnd())).append("\n");
            sb.append(line.getTranslatedSub()).append("\n\n");
        }
        return sb.toString();
    }

    private List<SrtEntry> parseSrt(Path path) {
        if (!Files.exists(path)) {
            throw new AppException(ErrorCode.SUBTITLE_NOT_FOUND);
        }

        List<SrtEntry> entries = new ArrayList<>();
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            // mỗi block cách nhau bởi dòng trống, format chuẩn SRT
            String[] blocks = content.split("\\r?\\n\\r?\\n");

            for (String block : blocks) {
                String[] lines = block.strip().split("\\r?\\n");
                if (lines.length < 2) continue;

                int index;
                try {
                    index = Integer.parseInt(lines[0].strip());
                } catch (NumberFormatException e) {
                    continue; // block rác/dòng trống thừa, bỏ qua
                }

                Matcher m = TIME_PATTERN.matcher(lines[1]);
                if (!m.find()) continue;

                double start = toSeconds(m.group(1), m.group(2), m.group(3), m.group(4));
                double end = toSeconds(m.group(5), m.group(6), m.group(7), m.group(8));

                String text = String.join("\n",
                        java.util.Arrays.copyOfRange(lines, 2, lines.length)).strip();

                entries.add(new SrtEntry(index, start, end, text));
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.SUBTITLE_NOT_FOUND);
        }

        return entries;
    }

    private double toSeconds(String h, String m, String s, String ms) {
        return Integer.parseInt(h) * 3600
                + Integer.parseInt(m) * 60
                + Integer.parseInt(s)
                + Integer.parseInt(ms) / 1000.0;
    }

    private String formatTime(double totalSeconds) {
        int h = (int) (totalSeconds / 3600);
        int m = (int) ((totalSeconds % 3600) / 60);
        int s = (int) (totalSeconds % 60);
        int ms = (int) Math.round((totalSeconds - Math.floor(totalSeconds)) * 1000);
        return String.format(Locale.ROOT, "%02d:%02d:%02d,%03d", h, m, s, ms);
    }

    private record SrtEntry(int index, double start, double end, String text) {
    }
}