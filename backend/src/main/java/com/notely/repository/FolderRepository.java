package com.notely.repository;

import com.notely.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {
    List<Folder> findByNotebookId(UUID notebookId);
    List<Folder> findByNotebookIdAndParentFolderIdIsNull(UUID notebookId);
}
