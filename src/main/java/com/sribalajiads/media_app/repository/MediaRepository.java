package com.sribalajiads.media_app.repository;

import com.sribalajiads.media_app.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The MediaRepository now extends JpaSpecificationExecutor.
 * This gives it the ability to execute complex, dynamic queries built
 * using the JPA Criteria API (Specifications). This is a more robust
 * and maintainable pattern for handling multiple optional filter parameters
 * than using long, derived query method names.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long>, JpaSpecificationExecutor<Media> {


    /**
     * Finds all Media entities whose mediaCode is present in the given list of codes.
     * This is an efficient way to fetch multiple specific items from the database in a single query.
     *
     * @param mediaCodes A List of media codes to search for.
     * @return A List of matching Media entities. The order is not guaranteed.
     */
    List<Media> findAllByMediaCodeIn(List<String> mediaCodes);

}