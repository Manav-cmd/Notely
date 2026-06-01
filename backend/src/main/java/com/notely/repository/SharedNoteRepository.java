package com.notely.repository;

import com.notely.entity.SharedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SharedNoteRepository extends JpaRepository<SharedNote, UUID> {
    List<SharedNote> findBySharedWithId(UUID userId);
    List<SharedNote> findByNoteId(UUID noteId);
    Optional<SharedNote> findByNoteIdAndSharedWithId(UUID noteId, UUID userId);
    boolean existsByNoteIdAndSharedWithId(UUID noteId, UUID userId);
}
