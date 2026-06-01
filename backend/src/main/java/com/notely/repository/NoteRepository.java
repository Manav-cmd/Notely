package com.notely.repository;

import com.notely.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Page<Note> findByWorkspaceIdAndIsArchived(UUID workspaceId, boolean isArchived, Pageable pageable);
    
    List<Note> findByWorkspaceIdAndIsArchived(UUID workspaceId, boolean isArchived);

    List<Note> findByNotebookIdAndIsArchived(UUID notebookId, boolean isArchived);

    List<Note> findByFolderIdAndIsArchived(UUID folderId, boolean isArchived);

    List<Note> findByOwnerId(UUID ownerId);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.workspace.id = :workspaceId AND n.isArchived = :isArchived AND t.id = :tagId")
    List<Note> findByWorkspaceIdAndIsArchivedAndTagId(@Param("workspaceId") UUID workspaceId, @Param("isArchived") boolean isArchived, @Param("tagId") UUID tagId);

    @Query("SELECT n FROM Note n WHERE n.workspace.id = :workspaceId AND n.isArchived = :isArchived AND n.createdAt BETWEEN :startDate AND :endDate")
    List<Note> findByWorkspaceIdAndIsArchivedAndDateRange(
            @Param("workspaceId") UUID workspaceId,
            @Param("isArchived") boolean isArchived,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT n FROM Note n WHERE n.workspace.id = :workspaceId AND n.isArchived = :isArchived " +
           "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Note> searchNotes(
            @Param("workspaceId") UUID workspaceId,
            @Param("isArchived") boolean isArchived,
            @Param("query") String query
    );

    long countByOwnerId(UUID ownerId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.owner.id = :ownerId AND n.createdAt >= :dateTime")
    long countByOwnerIdAndCreatedAtAfter(@Param("ownerId") UUID ownerId, @Param("dateTime") LocalDateTime dateTime);
}
