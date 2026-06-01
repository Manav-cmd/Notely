package com.notely.repository;

import com.notely.entity.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, UUID> {
    List<Revision> findByNoteIdOrderByVersionNumberDesc(UUID noteId);
}
