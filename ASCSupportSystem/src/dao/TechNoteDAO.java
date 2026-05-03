package dao;

import models.TechNote;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class TechNoteDAO implements FileDAO<TechNote> {

	private static final String FILE_NAME = "src/data/technote.txt";

	public TechNoteDAO() {
	    java.io.File file = new java.io.File(FILE_NAME);
	    file.getParentFile().mkdirs();

	    FileUtils.readLines(FILE_NAME);
	}

    @Override
    public List<TechNote> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<TechNote> notes = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|", 4);

            if (parts.length >= 4) {
                TechNote n = new TechNote();
                n.setId(parts[0]);
                n.setAppointmentId(parts[1]);
                n.setTechnicianId(parts[2]);
                n.setContent(parts[3].replace("<br>", "\n"));
                notes.add(n);
            }
        }
        return notes;
    }

    @Override
    public boolean save(TechNote note) {
        List<TechNote> all = readAll();

        if (note.getId() == null || note.getId().isEmpty()) {
            note.setId(IDGenerator.generateTechNoteID(all.size()));
        }

        all.add(note);
        return saveAll(all);
    }

    @Override
    public TechNote findById(String id) {
        return readAll().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean update(TechNote note) {
        List<TechNote> all = readAll();

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(note.getId())) {
                all.set(i, note);
                return saveAll(all);
            }
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        List<TechNote> all = readAll();

        boolean removed = all.removeIf(n -> n.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }

    private boolean saveAll(List<TechNote> list) {
        List<String> lines = new ArrayList<>();

        for (TechNote n : list) {
            lines.add(n.getId() + "|" +
                    n.getAppointmentId() + "|" +
                    n.getTechnicianId() + "|" +
                    n.getContent().replace("\n", "<br>"));
        }

        return FileUtils.writeLines(FILE_NAME, lines);
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}